package com.beepcast.api.provider.agent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.agent.sybase.ParameterId;
import com.beepcast.api.provider.agent.sybase.RequestParameter;
import com.beepcast.api.provider.agent.sybase.RequestParameterCommon;
import com.beepcast.api.provider.agent.sybase.RequestParameterFactory;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.StrUtil;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SybaseAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SybaseAgent" );
  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public SybaseAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
      MTSendWorker mtSendWorker , String providerId ,
      ProviderAgentConf providerAgentConf ) {
    super( providerConf , mtRespWorker , mtSendWorker , providerId ,
        providerAgentConf );
    if ( !super.isInitialized() ) {
      DLog.error( lctx , "Failed to initialized" );
      return;
    }

    // nothing to do yet ...

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Thread createAgentThread( int index ) {
    return new SybaseAgentThread( index );
  }

  public ArrayList processExtToIntMessage( ProviderMessage providerMessage ) {
    ArrayList listProviderMessages = new ArrayList();
    if ( providerMessage == null ) {
      return listProviderMessages;
    }
    listProviderMessages.add( providerMessage );
    return listProviderMessages;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , not yet initialized" );
      return;
    }
    super.startThread();
  }

  public void stop() {
    super.stopThread();
    initialized = false;
  }

  public boolean isConnectionsAvailable() {
    boolean result = false;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to retrive connection status info "
          + ", found not initialized yet" );
      return result;
    }
    int i , n = connections.size();
    Connection conn;
    for ( i = 0 ; i < n ; i++ ) {
      conn = (Connection) connections.get( i );
      if ( ( conn != null ) && ( conn.isActive() ) ) {
        result = true;
        break;
      }
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private Connection getAvailableConnection() {
    Connection conn = null;
    synchronized ( lockObject ) {
      int i , n = connections.size();
      for ( i = 0 ; i < n ; i++ ) {
        Connection tconn = (Connection) connections.get( i );
        if ( ( tconn != null ) && ( tconn.isActive() ) ) {
          conn = tconn;
          break;
        }
      }
    }
    return conn;
  }

  private void sendMessage( ProviderMessage providerMessage , Connection conn ) {

    // compose header log
    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    // update submit date
    providerMessage.setSubmitDateTime( new Date() );

    // read and validate provider connection
    TProvider provider = verifyProviderConnection( providerMessage , conn );
    if ( provider == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to verify provider connection" );
      return;
    }

    // generate externalMessageId based on internalMessageId
    providerMessage.setExternalMessageId( providerMessage
        .getInternalMessageId() );

    // read and validate provider parameter
    if ( !verifyProviderParameters( providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found invalid provider parameters" );
      return;
    }

    // compose http body request
    String httpBodyReqs = createHttpBody( providerMessage , provider );
    if ( StringUtils.isBlank( httpBodyReqs ) ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found empty http body request" );
      return;
    }
    DLog.debug( lctx , headerLog + "Composed httpBodyReqs = "
        + StringEscapeUtils.escapeJava( httpBodyReqs ) );

    // compose remote url
    URL remoteUrl = null;
    try {
      remoteUrl = new URL( provider.getAccessUrl() );
    } catch ( MalformedURLException e ) {
      DLog.warning( lctx ,
          headerLog + "Found malformed remote url - " + conn.getHttpUrl() );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }
    DLog.debug( lctx , headerLog + "Created the request to remoteUrl "
        + remoteUrl );

    // Submit Request and Read Response
    try {

      long deltaTime = System.currentTimeMillis();
      HttpURLConnection httpConn = (HttpURLConnection) remoteUrl
          .openConnection();
      httpConn.addRequestProperty(
          "Authorization" ,
          RequestParameterCommon.createHeaderAuthorization(
              provider.getAccessUsername() , provider.getAccessPassword() ) );
      httpConn.setRequestMethod( "POST" );
      httpConn.setDoInput( true );
      httpConn.setDoOutput( true );
      httpConn.setUseCaches( false );
      DataOutputStream os = new DataOutputStream( httpConn.getOutputStream() );
      os.writeBytes( httpBodyReqs );
      os.flush();
      os.close();

      StringBuffer sbLines = new StringBuffer();
      BufferedReader in = new BufferedReader( new InputStreamReader(
          httpConn.getInputStream() ) );
      String strLine;
      while ( ( strLine = in.readLine() ) != null ) {
        sbLines.append( strLine );
      }
      String bodyResponse = sbLines.toString();
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Processed the response ( " + deltaTime
          + " ms ) = " + StringEscapeUtils.escapeJava( bodyResponse ) );

      if ( StringUtils.isBlank( bodyResponse ) ) {
        DLog.warning( lctx , headerLog + "Found empty response" );
        providerMessage.setExternalStatus( EXTSTATUS_FAILED_NORESPONSE );
      } else {
        if ( parseHttpBody( headerLog , bodyResponse , providerMessage ) ) {
          DLog.debug( lctx , headerLog + "Extracted external status = "
              + providerMessage.getExternalStatus() );
        } else {
          DLog.warning( lctx , headerLog + "Found failed to parse "
              + "the http querystring content" );
          providerMessage.setExternalStatus( EXTSTATUS_FAILED_NORESPONSE );
        }
      }

    } catch ( IOException e ) {
      DLog.warning( lctx , headerLog + "Found failed to connect "
          + "to remoteHost - " + conn.getHttpUrl() );
      providerMessage.setExternalStatus( EXTSTATUS_FAILED_CONNECTION );
      synchronized ( lockObject ) {
        if ( providerAgentConf.isEnableErrorCheck() ) {
          idxErrorConnection = idxErrorConnection + 1;
        }
        if ( idxErrorConnection > maxErrorConnection() ) {
          idxErrorConnection = 0;
          conn.shutdown(); // shutdown the connection
          DLog.warning( lctx ,
              headerLog + "Shutdown the connection - " + conn.getHttpUrl()
                  + " , sending alert message " );
          ProviderStatusAlert.sendAlertProviderStatusInactive( providerId() ,
              conn );
        }
      }
    }

    // extract message status
    extractStatusMessage( providerMessage );
  }

  private boolean parseHttpBody( String headerLog , String httpBody ,
      ProviderMessage providerMessage ) {
    boolean result = false;

    // get and validate tagBody
    int a = httpBody.indexOf( "<BODY>" );
    int b = httpBody.indexOf( "</BODY>" );
    String tagBody = StringUtils.substring( httpBody , a + 6 , b );
    if ( StringUtils.isBlank( tagBody ) ) {
      DLog.warning( lctx , headerLog + "Failed to read content "
          + "found empty inside body tag" );
      return result;
    }

    // clean tagBody
    tagBody = StringUtils.replace( tagBody , "<br>" , " " );
    tagBody = StringUtils.trim( tagBody );

    // split tagBody
    String arr0 = "" , arr1 = "";
    String[] arr = StringUtils.split( tagBody , "=" );
    if ( arr.length > 0 ) {
      arr0 = StringUtils.trimToEmpty( arr[0] );
    }
    if ( arr.length > 1 ) {
      arr1 = StringUtils.trimToEmpty( arr[1] );
    }

    // read externalStatus and externalMessageId
    String externalStatus = null;
    String externalMessageId = null;
    if ( arr0.indexOf( "ORDERID" ) > -1 ) {
      externalStatus = "0";
      String[] arrExternalMessageIds = StringUtils.split( arr1 , "," );
      if ( arrExternalMessageIds.length > 1 ) {
        DLog.debug( lctx , headerLog + "Read multiple messageIds = ["
            + StringUtils.join( arrExternalMessageIds , "," )
            + "] , will use the latest one" );
      }
      if ( arrExternalMessageIds.length > 0 ) {
        externalMessageId = arrExternalMessageIds[arrExternalMessageIds.length - 1];
      }
    } else {
      arr = arr0.split( " " );
      if ( arr.length > 0 ) {
        externalStatus = arr[0];
      }
    }

    DLog.debug( lctx , headerLog + "Found external status = " + externalStatus
        + ", external messageId = [" + externalMessageId + "] " );

    // validate externalStatus
    if ( StringUtils.isBlank( externalStatus ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", can not extract external status" );
      return result;
    }

    // update externalStatus and externalMessageId
    providerMessage.setExternalStatus( externalStatus );
    if ( ( externalMessageId != null ) && ( !externalMessageId.equals( "" ) ) ) {
      providerMessage.setExternalMessageId( externalMessageId );
    }

    result = true;
    return result;
  }

  private String createHttpBody( ProviderMessage providerMessage ,
      TProvider provider ) {
    String httpBody = null;

    // verify provider message
    if ( providerMessage == null ) {
      return httpBody;
    }

    // header log
    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    // build message parameters
    Map messageParams = buildMessageParameters();
    if ( messageParams == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found failed to build message parameters" );
      return httpBody;
    }

    // fill up message parameters
    if ( !composeMessageParameters( messageParams , providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found failed to compose message parameters" );
      return httpBody;
    }

    // build request parameters
    RequestParameter requestParams = RequestParameterFactory
        .generateRequestParameter();
    if ( requestParams == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found failed to create requestParams from factory" );
      return httpBody;
    }

    // compose request parameters based on message parameters
    int totalAdded = RequestParameterCommon.buildParameters( requestParams ,
        messageParams );
    if ( totalAdded < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found empty requestParams added from messageParams" );
      return httpBody;
    }

    // convert request parameters to http body
    if ( requestParams != null ) {
      httpBody = requestParams.toString();
    }

    return httpBody;
  }

  private boolean composeMessageParameters( Map messageParams ,
      ProviderMessage providerMessage , TProvider provider ) {
    boolean result = false;

    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog + "Failed to compose message parameters "
          + ", found null providerMessage" );
      return result;
    }

    // get core parameter(s) in the provider message
    String originAddressMask = providerMessage.getOriginAddrMask();
    String destinationAddress = providerMessage.getDestinationAddress();
    String messageContent = providerMessage.getMessage();

    // store origin address mask when exist
    if ( !StringUtils.isBlank( originAddressMask ) ) {
      messageParams.put( ParameterId.SETUP_ORIGINATING_ADDRESS ,
          originAddressMask );
    }

    // clean destination address
    if ( !destinationAddress.startsWith( "+" ) ) {
      destinationAddress = "+" + destinationAddress;
    }

    // store destination address
    messageParams.put( ParameterId.MSISDN_LIST , destinationAddress );

    // validate message based on content type
    if ( providerMessage.getContentType() == ContentType.SMSBINARY ) {
      // found binary message , rejected coz not yet supported
      DLog.warning( lctx , headerLog + "Failed to compose message parameters "
          + ", found sms binary ( not yet support )" );
      providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      return result;
    } else if ( providerMessage.getContentType() == ContentType.SMSUNICODE ) {
      // found message as unicode than encode with base64 encoder
      messageContent = StrUtil.convert2Base64( messageContent );
      // log encode message
      DLog.debug( lctx , headerLog + "Define unicode message "
          + ", encode with base64 = " + messageContent );
      // store message content
      messageParams.put( ParameterId.MESSAGE_TEXT , messageContent );
      messageParams.put( ParameterId.SETUP_DCS , "UTF8" );
    } else if ( providerMessage.getContentType() == ContentType.SMSTEXT ) {
      // clean the message
      messageContent = cleanPlainText( messageContent );
      // store message content
      messageParams.put( ParameterId.MESSAGE_TEXT , messageContent );
    } else {
      DLog.warning( lctx , headerLog
          + "Failed to compose message parameters , found "
          + "anonymous content type = " + providerMessage.getContentType() );
      providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      return result;
    }

    // replace ack_reply_addr with provider listener url when exist
    String providerListenerUrl = provider.getListenerUrl();
    if ( !StringUtils.isBlank( providerListenerUrl ) ) {
      messageParams.put( ParameterId.SETUP_ACK_REPLY_ADDRESS ,
          providerListenerUrl );
    }

    // store additional optional parameters
    Map optionalParams = providerMessage.getOptionalParams();
    if ( optionalParams != null ) {

      String oriProvider = (String) optionalParams.get( "oriProvider" );
      if ( !StringUtils.isBlank( oriProvider ) ) {
        DLog.debug( lctx , headerLog + "Define original provider = "
            + oriProvider );
        if ( StringUtils.equals( oriProvider , providerId() ) ) {
          String sessionId = "<msg_id>"
              + providerMessage.getInternalMessageId() + "</msg_id>";
          messageParams.put( ParameterId.SETUP_SESSION_ID , sessionId );
          DLog.debug( lctx , headerLog + "Added session id param = "
              + sessionId );
        }
      }

    } // if ( optionalParams != null )

    result = true;
    return result;
  }

  private Map buildMessageParameters() {
    Map messageParams = new HashMap();
    if ( super.confMessageParams != null ) {
      messageParams.putAll( super.confMessageParams );
    }
    return messageParams;
  }

  private String cleanPlainText( String messageContent ) {
    String result = null;
    if ( messageContent == null ) {
      return result;
    }

    // create new trim string
    result = StringUtils.trimToEmpty( messageContent );

    // CR+LF: DEC RT-11 and most other early non-Unix, non-IBM OSes, CP/M, MP/M,
    // DOS, OS/2, Microsoft Windows, Symbian OS
    result = StringUtils.replace( result , "\r\n" , "<LF>" );

    // LF: Multics, Unix and Unix-like systems (GNU/Linux, AIX, Xenix, Mac OS
    // X, FreeBSD, etc.), BeOS, Amiga, RISC OS
    result = StringUtils.replace( result , "\n" , "<LF>" );

    // CR: Commodore machines, Apple II family, Mac OS up to version 9 and OS-9
    result = StringUtils.replace( result , "\r" , "<LF>" );

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class SybaseAgentThread extends Thread {

    private int idx;

    public SybaseAgentThread( int idx ) {
      super( "SybaseAgentThread-" + providerId() + "." + idx );
      this.idx = idx;
    }

    public void run() {

      BoundedLinkedQueue respQueue = getRespQueue();
      if ( respQueue == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null resp queue" );
        return;
      }

      BoundedLinkedQueue sendQueueInt = getSendQueueInt();
      if ( sendQueueInt == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null send queue" );
        return;
      }

      Connection connection = null;
      Object objectMessage = null;
      ProviderMessage providerMessage = null;
      String headerLog = null;

      DLog.debug( lctx , "Thread started" );
      while ( arrAgentThreads[idx] ) {

        try {
          Thread.sleep( providerAgentConf.getWorkerSleep() );
        } catch ( InterruptedException e ) {
        }

        // validate the response queue
        int curQueueRespPercentage = ( respQueue.size() * 100 )
            / respQueue.capacity();
        if ( curQueueRespPercentage > 80 ) {
          continue;
        }

        // validate the connection
        connection = getAvailableConnection();
        if ( connection == null ) {
          continue;
        }
        if ( !connection.isActive() ) {
          continue;
        }

        // prepare the message
        try {
          objectMessage = sendQueueInt.poll( 5000 );
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , headerLog() + "Failed to take object "
              + "provider message from the send queue , " + e );
        }
        if ( objectMessage == null ) {
          continue;
        }
        if ( !( objectMessage instanceof ProviderMessage ) ) {
          DLog.warning( lctx , headerLog() + "Failed to take object "
              + "provider message , found anonymous type" );
          continue;
        }

        providerMessage = (ProviderMessage) objectMessage;
        headerLog = ProviderMessageCommon.headerLog( providerMessage );
        DLog.debug( lctx , headerLog + "Got a message from internal channel" );

        trapLoad();
        sendMessage( providerMessage , connection );
        mtSendWorker.createMTTicket( providerMessage );

        // put the response back in the queue
        try {
          respQueue.put( providerMessage );
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , headerLog + "Failed to put provider message "
              + "into the response queue , " + e );
        }

      } // while ( arrAgentThreads[idx] )
      DLog.debug( lctx , "Thread stopped" );

    }

  } // SybaseAgentThread Class

}
