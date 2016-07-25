package com.beepcast.api.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.broker.Broker;
import com.beepcast.api.provider.broker.BrokerFactory;
import com.beepcast.api.provider.common.ProviderUtil;
import com.beepcast.api.provider.data.MessageType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.data.ProviderMessageFactory;
import com.beepcast.api.provider.data.ProviderMessageId;
import com.beepcast.api.provider.util.DateTimeFormat;
import com.beepcast.api.provider.util.QueueUtil;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.model.transaction.TransactionProcessType;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.RouterApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MOWorker extends BaseWorker implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MOWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private LoadManagement loadMng;
  private DatabaseLibrary dbLib;
  private OnlinePropertiesApp opropsApp;

  private ProviderApp providerApp;
  private ProviderConf providerConf;

  private BoundedLinkedQueue dataQueue;

  private Map mapMoBroker;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MOWorker( ProviderApp providerApp ) {
    super( "ProviderMOWorker" , 0 , 100 );

    loadMng = LoadManagement.getInstance();
    dbLib = DatabaseLibrary.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();

    this.providerApp = providerApp;
    this.providerConf = providerApp.getProviderConf();

    dataQueue = providerApp.getMoQueue();
    DLog.debug( lctx ,
        "Defined data queue , with capacity " + dataQueue.capacity()
            + " msg(s)" );

    createMapMoBroker();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int threadSize() {
    return (int) opropsApp.getLong( "Provider.MOWorker.TotalWorkers" ,
        providerConf.getMoWorkerThread() );
  }

  public Thread createThread( int workerIdx ) {
    return new MOWorkerThread( workerIdx );
  }

  public void start() {
    setupThreads();
  }

  public void stop() {
    resetThreads();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean storeMoMessage( String providerId , String externalMessageId ,
      String originAddress , String destinationAddress , String message ,
      Date dateDelivery , Map optionalParams , String externalParams ) {
    boolean result = false;

    // calculate the latency
    long deltaTime = System.currentTimeMillis();

    // log first
    DLog.debug( lctx ,
        "Store mo message into the queue , with : providerId = " + providerId
            + " , externalMessageId = " + externalMessageId
            + " , originAddress = " + originAddress
            + " , destinationAddress = " + destinationAddress + " , message = "
            + StringEscapeUtils.escapeJava( message ) + " , dateDelivery = "
            + DateTimeFormat.convertToString( dateDelivery )
            + " , optionalParams = " + optionalParams + " , externalParams = "
            + StringEscapeUtils.escapeJava( externalParams ) );

    // validate parameters
    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , "Failed to store mo message "
          + ", found blank in providerId parameter" );
      return result;
    }
    if ( StringUtils.isBlank( originAddress ) ) {
      DLog.warning( lctx , "Failed to store mo message "
          + ", found blank in originAddress parameter" );
      return result;
    }
    if ( StringUtils.isBlank( message ) ) {
      DLog.warning( lctx , "Failed to store mo message "
          + ", found blank in message parameter" );
      return result;
    }

    // prepare and clean provider message parameters
    int messageCount = 0;
    double debitAmount = 0;
    dateDelivery = ( dateDelivery == null ) ? new Date() : dateDelivery;
    externalParams = ( externalParams == null ) ? "" : externalParams.trim();

    // generate new message id
    String internalMessageId = ProviderMessageId.newMessageId( "EXT" );

    // compose header log
    String headerLog = ProviderMessageCommon.headerLog( internalMessageId );

    try {

      // build provider message
      ProviderMessage providerMessage = ProviderMessageFactory
          .createProviderMOMessage( MessageType.SMSMO , providerId ,
              externalMessageId , originAddress , destinationAddress , message ,
              messageCount , debitAmount , dateDelivery , externalParams );
      if ( providerMessage == null ) {
        DLog.warning( lctx , headerLog + "Failed to store mo message "
            + ", found failed to build provider mo message" );
        return result;
      }

      // set message id
      providerMessage.setInternalMessageId( internalMessageId );

      // set internal and external status
      providerMessage.setInternalStatus( "DELIVERED" );
      providerMessage.setExternalStatus( "DELIVERED" );

      // set optional params
      if ( optionalParams != null ) {
        providerMessage.getOptionalParams().putAll( optionalParams );
      }

      // trap the load of provider's mo
      loadMng.hitMo( providerMessage.getProviderId() , 1 );

      // put latency when found queue almost full ( 90 % )
      Double curQueueSize = QueueUtil.getSizePercentage( dataQueue );
      if ( ( curQueueSize != null ) && ( curQueueSize.doubleValue() > 90 ) ) {
        DLog.warning( lctx , headerLog + "Found mo queue is almost full "
            + "( " + curQueueSize + " % ) , put delay : 1000 ms" );
        Thread.sleep( 1000 );
      }

      // store provider message into the queue
      if ( !dataQueue.offer( providerMessage , 180000 ) ) {
        DLog.warning( lctx , headerLog + "Failed to store mo message "
            + "into the queue , found reached queue timeout : status = "
            + providerMessage.getExternalStatus() );
        return result;
      }

      // calculate the latency
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log
      DLog.debug( lctx , headerLog + "Stored mo message into the queue ( take "
          + deltaTime + " ms ) " );

      // return as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to store mo message "
          + "into the queue , " + e );
    }

    return result;
  }

  public boolean processMessageEachBroker( ProviderMessage providerMessage ) {
    boolean result = false;
    if ( providerMessage == null ) {
      return result;
    }
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );
    TProvider providerOut = ProviderUtil.resolveProvider( headerLog ,
        providerMessage.getProviderId() );
    if ( providerOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to do broker process message "
          + ", found invalid provider" );
      return result;
    }
    Broker broker = (Broker) mapMoBroker.get( providerOut.getProviderId() );
    if ( broker != null ) {
      DLog.debug( lctx , headerLog + "Processed message broker : providerId = "
          + broker.getProviderId() + " , class = " + broker.getClassName() );
      broker.processMessage( providerMessage );
    }
    result = true;
    return result;
  }

  public boolean processMoMessage( ProviderMessage providerMessage ) {
    boolean result = false;

    // validate must be params

    if ( providerMessage == null ) {
      DLog.warning( lctx , "Failed to process mo message "
          + ", found null provider message" );
      return result;
    }

    // read and validate provider id
    String providerId = providerMessage.getProviderId();
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to process mo message "
          + ", found empty provider id" );
      return result;
    }

    // header log
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // trap delta time
    long deltaTime = System.currentTimeMillis();

    try {

      // define router app
      RouterApp routerApp = RouterApp.getInstance();
      if ( routerApp == null ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + ", found null router app" );
        return result;
      }

      // prepare params
      int transProcType = TransactionProcessType.STANDARD;
      String messageId = providerMessage.getInternalMessageId();
      String messageType = providerMessage.getMessageType();
      String originAddress = providerMessage.getOriginAddress();
      String messageContent = providerMessage.getMessage();
      String destinationAddress = providerMessage.getDestinationAddress();
      Map externalMapParams = new HashMap();
      Date deliverDateTime = providerMessage.getDeliverDateTime();

      // first log
      DLog.debug(
          lctx ,
          headerLog + "Process mo message : transProcType = " + transProcType
              + " , messageId = " + messageId + " , messageType = "
              + messageType + " , providerId = " + providerId
              + " , originAddress = " + originAddress + " , messageContent = "
              + StringEscapeUtils.escapeJava( messageContent )
              + " , destinationAddress = " + destinationAddress
              + " , externalMapParams = " + externalMapParams
              + " , deliverDateTime = "
              + DateTimeFormat.convertToString( deliverDateTime ) );

      // create ticket
      createTicket( headerLog , providerMessage );

      // trap mo load status
      trapMoLoadStatus( headerLog , providerMessage );

      // process mo message via router app
      if ( !routerApp.procMoMessage( transProcType , 0 , 0 , null , messageId ,
          messageType , null , providerId , originAddress , messageContent ,
          null , destinationAddress , externalMapParams , deliverDateTime ) ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + "thru router app" );
        return result;
      }

      // return result as true
      result = true;

      // log and calculate the latency process
      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( deltaTime > 500 ) {
        DLog.warning( lctx , headerLog + "Found delay to proccess "
            + "mo message , take = " + deltaTime + " ms " );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to process mo message , " + e );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean createMapMoBroker() {
    boolean result = false;
    DLog.debug( lctx , "Trying to create map mo broker(s)" );

    if ( providerConf == null ) {
      DLog.warning( lctx , "Failed to create map mo broker(s) "
          + ", found null provider conf" );
      return result;
    }
    Map providerMoBrokerConfs = providerConf.getProviderMoBrokerConfs();
    if ( providerMoBrokerConfs == null ) {
      DLog.warning( lctx , "Failed to create map mo broker(s) "
          + ", found null provider dr broker confs" );
      return result;
    }

    mapMoBroker = new HashMap();

    Iterator iter = providerMoBrokerConfs.keySet().iterator();
    while ( iter.hasNext() ) {
      ProviderBrokerConf providerBrokerConf = (ProviderBrokerConf) providerMoBrokerConfs
          .get( (String) iter.next() );
      if ( providerBrokerConf == null ) {
        continue;
      }
      String providerId = providerBrokerConf.getProviderId();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      if ( mapMoBroker.get( providerId ) != null ) {
        continue;
      }
      Broker broker = BrokerFactory.createBroker( this , null , providerConf ,
          providerBrokerConf );
      if ( broker == null ) {
        continue;
      }
      mapMoBroker.put( broker.getProviderId() , broker );
    }

    DLog.debug( lctx , "Successfully created " + mapMoBroker.size()
        + " map mo broker(s) : " + mapMoBroker.keySet() );
    result = true;
    return result;
  }

  private void createTicket( String headerLog , ProviderMessage providerMessage ) {
    providerApp.createMOTicket( providerMessage );
  }

  private void trapMoLoadStatus( String headerLog ,
      ProviderMessage providerMessage ) {
    if ( loadMng == null ) {
      return;
    }
    if ( providerMessage == null ) {
      return;
    }
    String hdrProf = LoadManagement.HDRPROF_PROVIDER;
    String conType = LoadManagement.CONTYPE_SMSMO;
    StringBuffer sbName = new StringBuffer();
    sbName.append( providerMessage.getProviderId() );
    sbName.append( "-" );
    sbName.append( providerMessage.getInternalStatus() );
    loadMng.hit( hdrProf , conType , sbName.toString() , 1 , true , true );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class MOWorkerThread extends Thread {

    private int idx;

    public MOWorkerThread( int idx ) {
      super( "ProviderMOWorkerThread-" + idx );
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
        if ( counter < opropsApp.getLong( "Provider.MOWorker.TimeSleep" ,
            providerConf.getMoWorkerSleep() ) ) {
          continue;
        }
        counter = 0;

        try {

          // trap delta time
          long deltaTime = System.currentTimeMillis();

          // load provider message from queue
          Object objectMessage = dataQueue.poll( 5000 );
          if ( objectMessage == null ) {
            continue;
          }
          if ( !( objectMessage instanceof ProviderMessage ) ) {
            continue;
          }
          ProviderMessage providerMessage = (ProviderMessage) objectMessage;

          // header log
          String headerLog = ProviderMessageCommon.headerLog( providerMessage );

          // log it
          DLog.debug(
              lctx ,
              headerLog
                  + "Process mo provider message : providerId = "
                  + providerMessage.getProviderId()
                  + " , originAddress = "
                  + providerMessage.getOriginAddress()
                  + " , externalMessageId = "
                  + providerMessage.getExternalMessageId()
                  + " , deliverDateTime = "
                  + DateTimeFormat.convertToString( providerMessage
                      .getDeliverDateTime() ) );

          // process mo provider message
          boolean processed = processMoMessage( providerMessage );

          // calculate the latency process
          deltaTime = System.currentTimeMillis() - deltaTime;
          if ( ( !processed ) || ( deltaTime > 500 ) ) {
            DLog.warning( lctx , "Found failed or delay to process mo "
                + "provider message ( take = " + deltaTime
                + " ms ) , result = " + processed );
          }

        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to process mo provider message "
              + "from the mo queue , " + e );
        }

      } // end loop
      DLog.debug( lctx , "Thread stopped" );
    }
  }

}
