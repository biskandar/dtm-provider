package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.common.MessageTypeUtils;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.DateTimeFormat;
import com.beepcast.api.provider.util.ProviderMessageOptionalMapParamUtils;
import com.beepcast.billing.BillingApp;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.common.util.concurrent.Channel;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.encrypt.EncryptApp;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.model.transaction.TransactionCountryUtil;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.dr.ProcessRetryMessage;
import com.beepcast.router.dr.ProcessShutdownMessage;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MTRespWorker extends BaseWorker implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MTRespWorker" );
  static final Object lockBatch = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private LoadManagement loadMng;
  private DatabaseLibrary dbLib;
  private OnlinePropertiesApp opropsApp;
  private BillingApp billingApp;

  private ProviderApp providerApp;
  private ProviderConf providerConf;

  private String keyPhoneNumber;
  private String keyMessage;

  private BoundedLinkedQueue mtRespQueue;

  private int dataBatchCapacity;
  private int dataBatchThreshold;
  private BoundedLinkedQueue dataBatchQueue;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MTRespWorker( ProviderApp providerApp ) {
    super( "ProviderMTRespWorker" , 0 , 100 );

    loadMng = LoadManagement.getInstance();
    dbLib = DatabaseLibrary.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();
    billingApp = BillingApp.getInstance();

    this.providerApp = providerApp;
    this.providerConf = providerApp.getProviderConf();

    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );
    keyMessage = encryptApp.getKeyValue( EncryptApp.KEYNAME_MESSAGE );

    mtRespQueue = new BoundedLinkedQueue( providerConf.getMtSizeQueue() );

    dataBatchCapacity = providerConf.getMtDataBatchCapacity();
    dataBatchThreshold = providerConf.getMtDataBatchThreshold();
    dataBatchQueue = new BoundedLinkedQueue( dataBatchCapacity );
    DLog.debug( lctx , "Defined data batch : capacity = " + dataBatchCapacity
        + " msg(s) , threshold = " + dataBatchThreshold
        + " msg(s) , queue.capacity = " + dataBatchQueue.capacity() + " msg(s)" );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int threadSize() {
    return (int) opropsApp.getLong( "Provider.MTRespWorker.TotalWorkers" ,
        providerConf.getMtWorkerThread() );
  }

  public Thread createThread( int workerIdx ) {
    return new MTRespWorkerThread( workerIdx );
  }

  public void start() {
    setupThreads();
  }

  public void stop() {
    resetThreads();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Channel getMtRespQueue() {
    return mtRespQueue;
  }

  public BoundedLinkedQueue getDataBatchQueue() {
    return dataBatchQueue;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void clean() {
    extractMTRespMessage( null , true );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void extractMTRespMessage( ProviderMessage providerMtMessage ,
      boolean clean ) {

    if ( !clean ) {
      // store provider message into the mt resp batch queue
      storeToMtRespBatchQueue( providerMtMessage );
    }

    // prepare variable
    List providerMtMessages = null;

    synchronized ( lockBatch ) {
      int i , size = dataBatchQueue.size();
      if ( ( clean ) || ( size >= dataBatchThreshold ) ) {
        DLog.debug( lctx , "Will insert batch provider mt message(s) "
            + "into gateway log , found : " + size + " record(s)" );
        providerMtMessages = new ArrayList();
        for ( i = 0 ; i < size ; i++ ) {
          try {
            providerMtMessage = (ProviderMessage) dataBatchQueue.take();
            if ( providerMtMessage == null ) {
              continue;
            }
            providerMtMessages.add( providerMtMessage );
          } catch ( InterruptedException e ) {
            DLog.warning( lctx , "Failed to take provider mt message "
                + "from mt response batch queue , " + e );
          }
        } // end for
      }
    } // end synchronized

    // validate if there is providerMessages ?
    if ( providerMtMessages == null ) {
      return;
    }

    // clean params inside provider message
    cleanProviderMessages( providerMtMessages );

    // clear debit amount for failed response
    clearDebitAmountForFailedResponse( providerMtMessages );

    // perform the batch insert into gateway log tables
    insertIntoGatewayLog( providerMtMessages );

    // process based on the status message
    processStatusMessages( providerMtMessages );

  }

  private boolean storeToMtRespBatchQueue( ProviderMessage providerMessage ) {
    boolean result = false;

    if ( providerMessage == null ) {
      return result;
    }

    // compose header log
    String headerLog = ProviderCommon.headerLog( providerMessage
        .getProviderId() ) + ProviderMessageCommon.headerLog( providerMessage );

    // queue into mt response batch queue
    try {
      if ( dataBatchQueue.offer( providerMessage , 2500 ) ) {
        result = true;
        DLog.debug(
            lctx ,
            headerLog + "Queued into mt response batch queue : "
                + providerMessage.getDestinationAddress() + ","
                + providerMessage.getExternalMessageId() + ","
                + providerMessage.getExternalStatus() + ","
                + StringEscapeUtils.escapeJava( providerMessage.getMessage() ) );
      } else {
        DLog.warning(
            lctx ,
            headerLog + "Failed to put the provider message "
                + "into the mt response batch queue : "
                + providerMessage.getDestinationAddress() + ","
                + providerMessage.getExternalMessageId() + ","
                + providerMessage.getExternalStatus() + ","
                + StringEscapeUtils.escapeJava( providerMessage.getMessage() ) );
      }
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to put the provider message "
          + "into the mt response batch queue , " + e );
    }

    return result;
  }

  private void clearDebitAmountForFailedResponse( List providerMtMessages ) {
    if ( providerMtMessages == null ) {
      return;
    }

    // is this feature enable ?
    if ( !opropsApp.getBoolean( "ProviderAgent.ClearDebitOnFailedResponse" ,
        false ) ) {
      return;
    }

    // iterate and process all the messages
    int totalRecords = 0;
    Iterator iterProviderMtMessages = providerMtMessages.iterator();
    while ( iterProviderMtMessages.hasNext() ) {
      ProviderMessage providerMtMessage = (ProviderMessage) iterProviderMtMessages
          .next();
      if ( providerMtMessage == null ) {
        continue;
      }

      // header log
      String headerLog = ProviderMessageCommon.headerLog( providerMtMessage );

      // bypass for successfully submitted message
      if ( ProviderMessageOptionalMapParamUtils.isCommandSubmitted(
          providerMtMessage , false ) ) {
        continue;
      }
      if ( StringUtils.equalsIgnoreCase( providerMtMessage.getInternalStatus() ,
          "SUBMITTED" ) ) {
        continue;
      }

      // log when found as failed ( / not submitted ) message
      DLog.debug(
          lctx ,
          headerLog
              + "Perform clearing debit amount "
              + "property on this failed response message , set debit amount : "
              + providerMtMessage.getDebitAmount() + " -> 0 , found status = "
              + providerMtMessage.getInternalStatus() + " ( "
              + providerMtMessage.getExternalStatus() + " ) " );

      // do refund
      if ( !ProviderPayment.doCredit( providerMtMessage ) ) {
        DLog.warning( lctx , headerLog + "Failed to perform refund "
            + ", bypass to clear the debit amount parameter" );
        continue;
      }

      // clear debit amount property
      providerMtMessage.setDebitAmount( 0 );

      // track it
      totalRecords = totalRecords + 1;

    }

    // log summary
    if ( totalRecords > 0 ) {
      DLog.debug( lctx , "Cleared debit amount for failed response message "
          + ", is effected = " + totalRecords + " record(s)" );
    }

  }

  private void insertIntoGatewayLog( List listProviderMessages ) {
    if ( listProviderMessages == null ) {
      return;
    }

    // read and validate size of list provider messages
    int sizeProviderMessages = listProviderMessages.size();
    if ( sizeProviderMessages < 1 ) {
      return;
    }

    // intialized sql batch
    String sql = sqlBatchInsert();

    // prepare the array params
    int idxRec = 0 , maxRec = sizeProviderMessages;
    String[] arrIntMsgIds = new String[maxRec];
    Object[][] arrParams = new Object[maxRec][];

    // compose the array params for batch insert
    ProviderMessage providerMessage = null;
    Iterator iterProviderMessages = listProviderMessages.iterator();
    while ( iterProviderMessages.hasNext() ) {
      providerMessage = (ProviderMessage) iterProviderMessages.next();
      if ( providerMessage == null ) {
        continue;
      }
      Object[] objParams = providerMessageToBatchInsertParam( providerMessage );
      if ( objParams == null ) {
        continue;
      }
      arrParams[idxRec] = objParams;
      arrIntMsgIds[idxRec] = providerMessage.getInternalMessageId();
      idxRec = idxRec + 1;
    } // while ( iterProviderMessages.hasNext() ) {

    // no need to perform batch when there is no record available
    if ( idxRec < 1 ) {
      DLog.warning( lctx , "Failed to execute the batch insert "
          + "provider message(s) into gateway log , found empty record(s)" );
      return;
    }

    // clean array params when found invalid items
    if ( idxRec < maxRec ) {
      Object[][] arrParamsTemp = new Object[idxRec][];
      for ( int idx = 0 ; idx < arrParamsTemp.length ; idx++ ) {
        arrParamsTemp[idx] = arrParams[idx];
      }
      DLog.debug( lctx , "Cleaned params before the batch insert : "
          + arrParams.length + " -> " + arrParamsTemp.length );
      arrParams = arrParamsTemp;
    }

    // execute to perform insert batch
    int[] results = dbLib.executeBatchStatement( "transactiondb" , sql ,
        arrParams );
    if ( results == null ) {
      DLog.warning( lctx , "Failed to execute the batch insert "
          + "provider message(s) into gateway log , fould failed to persist" );
      return;
    }

    // debug and show the failed persist with internal message id
    StringBuffer sbLogPersistSucceed = null;
    StringBuffer sbLogPersistFailed = null;
    int idx , len = results.length;
    for ( idx = 0 ; idx < len ; idx++ ) {
      if ( results[idx] > 0 ) {
        if ( sbLogPersistSucceed == null ) {
          sbLogPersistSucceed = new StringBuffer(
              "Successfully persisted gateway log : " );
        } else {
          sbLogPersistSucceed.append( "," );
        }
        sbLogPersistSucceed.append( arrIntMsgIds[idx] );
      } else {
        if ( sbLogPersistFailed == null ) {
          sbLogPersistFailed = new StringBuffer(
              "Failed to persisted gateway log : " );
        } else {
          sbLogPersistFailed.append( "," );
        }
        sbLogPersistFailed.append( arrIntMsgIds[idx] );
      }
    }
    if ( sbLogPersistSucceed != null ) {
      DLog.debug( lctx , sbLogPersistSucceed.toString() );
    }
    if ( sbLogPersistFailed != null ) {
      DLog.warning( lctx , sbLogPersistFailed.toString() );
    }

  }

  private void processStatusMessages( List providerMtMessages ) {
    if ( providerMtMessages == null ) {
      return;
    }
    ProviderMessage providerMtMessage = null;
    Iterator iterProviderMtMessages = providerMtMessages.iterator();
    while ( iterProviderMtMessages.hasNext() ) {
      providerMtMessage = (ProviderMessage) iterProviderMtMessages.next();
      if ( providerMtMessage == null ) {
        continue;
      }

      // setup header log
      String headerLog = ProviderMessageCommon.headerLog( providerMtMessage );

      // put as internal message for all process report
      Map optionalParams = new HashMap();
      optionalParams.put(
          ProviderMessageOptionalMapParamUtils.HDR_INTERNAL_MESSAGE , "true" );

      // process for shutdown command
      if ( ProviderMessageOptionalMapParamUtils.isCommandShutdown(
          providerMtMessage , true ) ) {
        if ( opropsApp.getBoolean(
            "ProviderAgent.EnableShutdownOnFailedResponse" , false ) ) {
          DLog.debug( lctx , headerLog + "Found shutdown flag inside "
              + "provider mt message , will do the shutdown provider " );
          ProcessShutdownMessage.execute( providerMtMessage );
        }
      }

      // process for retry command
      if ( ProviderMessageOptionalMapParamUtils.isCommandRetry(
          providerMtMessage , true ) ) {
        if ( opropsApp.getBoolean( "ProviderAgent.EnableRetryOnFailedResponse" ,
            false ) ) {
          DLog.debug( lctx , headerLog + "Found retry flag inside "
              + "provider mt message , will do re-send message " );
          ProcessRetryMessage.execute( providerMtMessage , true );
        }
      }

      // process for ignored command
      if ( ProviderMessageOptionalMapParamUtils.isCommandIgnored(
          providerMtMessage , true ) ) {
        if ( opropsApp.getBoolean(
            "ProviderAgent.EnableIgnoredOnFailedResponse" , false ) ) {
          // bypass , nothing to do yet
        }
      }

    } // while ( iterProviderMessages.hasNext() )
  }

  private String sqlBatchInsert() {
    String sqlInsert = "INSERT INTO gateway_log ( status , external_status "
        + ", message_id , external_message_id , event_id , channel_session_id "
        + ", gateway_xipme_id , provider , date_tm , mode , retry , phone "
        + ", encrypt_phone , phone_country_id , message_type , message_count "
        + ", message , encrypt_message , debit_amount , short_code "
        + ", encrypt_short_code , senderID , encrypt_senderID ) ";
    String sqlValues = "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? "
        + ", ? , ? , ? , AES_ENCRYPT(?,'" + keyPhoneNumber
        + "') , ? , ? , ? , ? , " + "AES_ENCRYPT(?,'" + keyMessage
        + "') , ? , ? , AES_ENCRYPT(?,'" + keyPhoneNumber
        + "') , ? , AES_ENCRYPT(?,'" + keyPhoneNumber + "') ) ";
    String sql = sqlInsert + sqlValues;
    return sql;
  }

  private Object[] providerMessageToBatchInsertParam(
      ProviderMessage providerMtMessage ) {
    Object[] params = new Object[23];
    try {

      String gatewayXipmeId = (String) providerMtMessage
          .getOptionalParam( TransactionMessageParam.HDR_GATEWAY_XIPME_ID );
      gatewayXipmeId = ( gatewayXipmeId == null ) ? "" : gatewayXipmeId.trim();

      params[0] = providerMtMessage.getInternalStatus();
      params[1] = providerMtMessage.getExternalStatus();
      params[2] = providerMtMessage.getInternalMessageId();
      params[3] = providerMtMessage.getExternalMessageId();
      params[4] = providerMtMessage.getEventId();
      params[5] = providerMtMessage.getChannelSessionId();
      params[6] = gatewayXipmeId;
      params[7] = providerMtMessage.getProviderId();
      params[8] = DateTimeFormat.convertToString( providerMtMessage
          .getSubmitDateTime() );
      params[9] = "SEND";
      params[10] = new Integer( providerMtMessage.getRetry() );
      params[11] = "";
      params[12] = providerMtMessage.getDestinationAddress();
      params[13] = new Integer(
          TransactionCountryUtil.getCountryId( providerMtMessage
              .getDestinationAddress() ) );
      params[14] = MessageTypeUtils
          .transformProviderContentTypeStrToGatewayMessageType( providerMtMessage
              .getContentType() );
      params[15] = new Integer( providerMtMessage.getMessageCount() );
      params[16] = "";
      params[17] = providerMtMessage.getMessage();
      params[18] = Double.toString( providerMtMessage.getDebitAmount() );
      params[19] = "";
      params[20] = providerMtMessage.getOriginAddress();
      params[21] = "";
      params[22] = providerMtMessage.getOriginAddrMask();

    } catch ( Exception e ) {
      DLog.debug( lctx , "Failed to convert provider mt "
          + "message to batch param , " + e );
    }
    return params;
  }

  private void cleanProviderMessages( List listProviderMessages ) {
    if ( listProviderMessages == null ) {
      return;
    }
    ProviderMessage providerMessage = null;
    Iterator iterProviderMessages = listProviderMessages.iterator();
    while ( iterProviderMessages.hasNext() ) {
      providerMessage = (ProviderMessage) iterProviderMessages.next();
      if ( providerMessage == null ) {
        continue;
      }

      String internalStatus = providerMessage.getInternalStatus();
      String externalStatus = providerMessage.getExternalStatus();
      String internalMessageId = providerMessage.getInternalMessageId();
      String externalMessageId = providerMessage.getExternalMessageId();
      String eventId = providerMessage.getEventId();
      String channelSessionId = providerMessage.getChannelSessionId();
      String providerId = providerMessage.getProviderId();
      String destinationAddress = providerMessage.getDestinationAddress();
      String message = providerMessage.getMessage();
      String originAddress = providerMessage.getOriginAddress();
      String originAddrMask = providerMessage.getOriginAddrMask();

      internalStatus = ( internalStatus == null ) ? "" : internalStatus;
      externalStatus = ( externalStatus == null ) ? "" : externalStatus;
      internalMessageId = ( internalMessageId == null ) ? ""
          : internalMessageId;
      externalMessageId = ( externalMessageId == null ) ? ""
          : externalMessageId;
      eventId = ( eventId == null ) ? "0" : eventId;
      channelSessionId = ( channelSessionId == null ) ? "0" : channelSessionId;
      providerId = ( providerId == null ) ? "" : providerId;
      destinationAddress = ( destinationAddress == null ) ? ""
          : destinationAddress;
      message = ( message == null ) ? "" : message;
      originAddress = ( originAddress == null ) ? "" : originAddress;
      originAddrMask = ( originAddrMask == null ) ? "" : originAddrMask;

      providerMessage.setInternalStatus( internalStatus );
      providerMessage.setExternalStatus( externalStatus );
      providerMessage.setInternalMessageId( internalMessageId );
      providerMessage.setExternalMessageId( externalMessageId );
      providerMessage.setEventId( eventId );
      providerMessage.setChannelSessionId( channelSessionId );
      providerMessage.setProviderId( providerId );
      providerMessage.setDestinationAddress( destinationAddress );
      providerMessage.setMessage( message );
      providerMessage.setOriginAddress( originAddress );
      providerMessage.setOriginAddrMask( originAddrMask );

    } // while ( iterProviderMessages.hasNext() )
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class MTRespWorkerThread extends Thread {

    private int idx;

    public MTRespWorkerThread( int idx ) {
      super( "ProviderMTRespWorkerThread-" + idx );
      this.idx = idx;
    }

    public void run() {
      long counter = 0 , delay10ms = 10;
      DLog.debug( lctx , "Thread started" );
      while ( isThreadActive( idx ) ) {

        counter = counter + delay10ms;
        try {
          Thread.sleep( delay10ms );
        } catch ( Exception e ) {
        }
        if ( counter < opropsApp.getLong( "Provider.MTRespWorker.TimeSleep" ,
            providerConf.getMtWorkerSleep() ) ) {
          continue;
        }
        counter = 0;

        try {
          long deltaTime = System.currentTimeMillis();
          Object objectMessage = mtRespQueue.poll( 5000 );
          if ( objectMessage == null ) {
            continue;
          }
          if ( !( objectMessage instanceof ProviderMessage ) ) {
            continue;
          }
          ProviderMessage providerMessage = (ProviderMessage) objectMessage;
          // write to database
          extractMTRespMessage( providerMessage , false );
          // calculate the latency process
          deltaTime = System.currentTimeMillis() - deltaTime;
          if ( deltaTime > 500 ) {
            DLog.debug( lctx ,
                "Found delay to process mt response message " + ", take = "
                    + deltaTime + " ms : " + providerMessage.getProviderId()
                    + " , " + providerMessage.getDestinationAddress() + " , "
                    + providerMessage.getExternalMessageId() );
          }
        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to process mt response message "
              + "from the mt resp queue , " + e );
        }

      } // end loop
      DLog.debug( lctx , "Thread stopped" );
    }

  } // MTRespWorkerThread Class

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Util Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlEncryptPhoneNumber( String phoneNumber ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( phoneNumber );
    sb.append( "','" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlEncryptMessage( String message ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( StringEscapeUtils.escapeSql( message ) );
    sb.append( "','" );
    sb.append( keyMessage );
    sb.append( "')" );
    return sb.toString();
  }

}
