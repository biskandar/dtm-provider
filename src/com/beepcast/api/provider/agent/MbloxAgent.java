package com.beepcast.api.provider.agent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.agent.mblox.MbloxLongMessage;
import com.beepcast.api.provider.agent.mblox.Notification;
import com.beepcast.api.provider.agent.mblox.NotificationHeader;
import com.beepcast.api.provider.agent.mblox.NotificationRequest;
import com.beepcast.api.provider.agent.mblox.NotificationRequestResult;
import com.beepcast.api.provider.agent.mblox.NotificationRequestResultParser;
import com.beepcast.api.provider.agent.mblox.NotificationResult;
import com.beepcast.api.provider.agent.mblox.NotificationResultHeader;
import com.beepcast.api.provider.agent.mblox.SenderIdType;
import com.beepcast.api.provider.agent.mblox.SubscriberResult;
import com.beepcast.api.provider.common.MbloxUtil;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.ProviderMessageOptionalMapParamUtils;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.ThreadLocalParser;

public class MbloxAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MbloxAgent" );
  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;
  private Random random;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MbloxAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
      MTSendWorker mtSendWorker , String providerId ,
      ProviderAgentConf providerAgentConf ) {
    super( providerConf , mtRespWorker , mtSendWorker , providerId ,
        providerAgentConf );
    if ( !super.isInitialized() ) {
      DLog.error( lctx , "Failed to initialized" );
      return;
    }
    random = new Random();
    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Thread createAgentThread( int index ) {
    return new MbloxAgentThread( index );
  }

  public ArrayList processExtToIntMessage( ProviderMessage providerMessage ) {
    ArrayList listProviderMessages = new ArrayList();
    if ( providerMessage == null ) {
      return listProviderMessages;
    }
    if ( providerMessage.getMessageCount() < 2 ) {
      listProviderMessages.add( providerMessage );
      return listProviderMessages;
    }
    int msgRef = random.nextInt( 255 ) + 1;
    if ( MbloxLongMessage.split( providerMessage , msgRef ,
        listProviderMessages ) ) {
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

  private Connection getAvailableConnection( int method ) {
    Connection conn = null;
    synchronized ( lockObject ) {

      // method == 0 : fail over
      // method == 1 : round robin

      // composed active connections

      List activeConnections = new ArrayList();
      int i , n = connections.size();
      for ( i = 0 ; i < n ; i++ ) {
        Connection tconn = (Connection) connections.get( i );
        if ( ( tconn != null ) && ( tconn.isActive() ) ) {
          activeConnections.add( tconn );
        }
      }

      // validate and read connection ( at least one )

      int sizeActiveConnections = activeConnections.size();
      if ( sizeActiveConnections < 1 ) {
        idxConn = 0; // reset index connection
        return conn;
      }
      if ( sizeActiveConnections < 2 ) {
        idxConn = 0; // reset index connection
        conn = (Connection) activeConnections.get( 0 );
        return conn;
      }

      if ( method == BaseAgent.METHOD_FAIL_OVER ) {

        // fail over method

        idxConn = 0; // reset index connection
        conn = (Connection) activeConnections.get( idxConn );

      } else {

        // round robin method

        if ( idxConn >= sizeActiveConnections ) {
          idxConn = 0; // reset index connection
        }
        conn = (Connection) activeConnections.get( idxConn );
        idxConn = idxConn + 1;

      }

    } // synchronized ( lockObject )
    return conn;
  }

  private boolean parseBodyResponse( String headerLog , String bodyResponse ,
      ProviderMessage providerMessage ) {
    boolean result = false;

    if ( StringUtils.isBlank( bodyResponse ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", found null in the HTTP body response" );
      return result;
    }

    bodyResponse = bodyResponse.trim();

    if ( !bodyResponse.startsWith( "<?xml " ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", found can not find xml format in the HTTP body response" );
      return result;
    }

    Document document = null;
    Element element = null;
    try {
      document = ThreadLocalParser.parse( bodyResponse );
      if ( document != null ) {
        element = document.getDocumentElement();
      }
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog
          + "Failed to parse the HTTPBodyResponse , " + e );
    }
    if ( element == null ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", found null xml element" );
      return result;
    }

    NotificationRequestResult notfRequestResult = NotificationRequestResultParser
        .parse( element );
    if ( notfRequestResult == null ) {
      DLog.warning( lctx , headerLog + "Failed to parse xml into "
          + "NotificationRequestResult object" );
      return result;
    }

    String stemp = null;
    String externalStatusCode = null;
    int retry = 0;

    NotificationResultHeader notfResultHeader = notfRequestResult
        .getNotfResultHeader();
    if ( notfResultHeader != null ) {
      stemp = notfResultHeader.getRequestResultCode();
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        externalStatusCode = "RRC" + stemp;
      }
    }

    NotificationResult notfResult = null;
    if ( ( externalStatusCode != null )
        && ( externalStatusCode.equals( "RRC0" ) ) ) {
      List notfResultList = notfRequestResult.getNotfResultList();
      if ( notfResultList != null ) {
        Iterator notfResultIter = notfResultList.iterator();
        if ( notfResultIter.hasNext() ) {
          notfResult = (NotificationResult) notfResultIter.next();
        }
      }
    }
    if ( notfResult != null ) {
      stemp = notfResult.getNotfResultCode();
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        externalStatusCode = "NRC" + stemp;
      }
    }

    SubscriberResult subsResult = null;
    if ( ( externalStatusCode != null )
        && ( externalStatusCode.equals( "NRC0" ) ) ) {
      subsResult = notfResult.getSubscriberResult();
    }
    if ( subsResult != null ) {
      stemp = subsResult.getSubscriberResultCode();
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        externalStatusCode = "SRC" + stemp;
      }
      retry = subsResult.getRetry();
    }

    if ( StringUtils.isBlank( externalStatusCode ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", can not find external status code value" );
      return result;
    }
    if ( retry > 0 ) {
      DLog.debug( lctx , "Found retryable inside the HTTP body response "
          + ", will perform retry send" );
      ProviderMessageOptionalMapParamUtils.setCommandRetry( providerMessage );
    }

    providerMessage.setExternalStatus( externalStatusCode );

    result = true;
    return result;
  }

  private String createExtMessageId( String intMessageId ) {
    String extMessageId = Long.toString( System.currentTimeMillis() );
    if ( StringUtils.isBlank( intMessageId ) ) {
      return extMessageId;
    }
    // must be numeric format
    StringBuffer sbExtMessageId = null;
    for ( int idx = 0 ; idx < intMessageId.length() ; idx++ ) {
      if ( ( intMessageId.charAt( idx ) >= '0' )
          && ( intMessageId.charAt( idx ) <= '9' ) ) {
        if ( sbExtMessageId == null ) {
          sbExtMessageId = new StringBuffer();
        }
        sbExtMessageId.append( intMessageId.charAt( idx ) );
      }
    }
    if ( sbExtMessageId != null ) {
      extMessageId = sbExtMessageId.toString();
    }
    return extMessageId;
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

    // validate must be parameters
    if ( !verifyProviderParameters( providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found invalid provider parameters" );
      return;
    }

    // generate externalMessageId based on internalMessageId
    providerMessage.setExternalMessageId( createExtMessageId( providerMessage
        .getInternalMessageId() ) );
    DLog.debug( lctx , headerLog + "Generated external message id : "
        + providerMessage.getExternalMessageId() );

    // generate notification request
    NotificationRequest notfReq = null;
    try {
      notfReq = createNotificationRequest( headerLog , providerMessage ,
          provider );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog
          + "Failed to create notification request , " + e );
    }
    if ( notfReq == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to create notification request object" );
      return;
    }

    // compose http parameters
    String httpParameters = null;
    try {
      DLog.debug( lctx , headerLog + "Composed xml data parameter "
          + StringEscapeUtils.escapeJava( notfReq.toXml() ) );
      httpParameters = "XMLDATA="
          + URLEncoder.encode( notfReq.toXml() , "UTF-8" );
      DLog.debug( lctx , headerLog + "Composed http parameters "
          + httpParameters );
    } catch ( UnsupportedEncodingException e ) {
      DLog.warning( lctx , headerLog + "Found unsupported url encoding type" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_ENCODINGTYPE );
      return;
    }

    // compose remote url
    URL remoteUrl = null;
    try {
      remoteUrl = new URL( conn.getHttpUrl() );
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
      httpConn.setRequestProperty( "Content-Type" ,
          "application/x-www-form-urlencoded" );
      httpConn.setRequestMethod( "POST" );
      httpConn.setDoInput( true );
      httpConn.setDoOutput( true );
      httpConn.setUseCaches( false );
      DataOutputStream os = new DataOutputStream( httpConn.getOutputStream() );
      os.writeBytes( httpParameters );
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
        if ( parseBodyResponse( headerLog , bodyResponse , providerMessage ) ) {
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

  private NotificationRequest createNotificationRequest( String headerLog ,
      ProviderMessage providerMessage , TProvider providerOut ) {
    NotificationRequest notfRequest = null;

    Notification notf = createNotification( headerLog , providerMessage ,
        providerOut , 0 );
    if ( notf == null ) {
      DLog.warning( lctx , headerLog + "Failed to create notification request "
          + ", found failed to create notification" );
      return notfRequest;
    }

    notfRequest = new NotificationRequest();
    notfRequest.setVersion( "3.5" );
    notfRequest.setNotfHeader( new NotificationHeader( providerOut
        .getAccessUsername() , providerOut.getAccessPassword() , null ) );
    notfRequest.setBatchId( providerMessage.getExternalMessageId() );
    notfRequest.setNotfList( new ArrayList() );
    notfRequest.getNotfList().add( notf );

    return notfRequest;
  }

  private Notification createNotification( String headerLog ,
      ProviderMessage providerMessage , TProvider providerOut ,
      int sequenceNumber ) {
    Notification notf = new Notification();
    notf.setSequenceNumber( sequenceNumber );
    notf.setMessageType( "SMS" );

    // update message format based on content type
    switch ( providerMessage.getContentType() ) {
    case ContentType.SMSBINARY :
      notf.setMessageFormat( "Binary" );
      break;
    case ContentType.SMSUNICODE :
      notf.setMessageFormat( "Unicode" );
      break;
    case ContentType.SMSTEXT :
      notf.setMessageFormat( "UTF8" );
      break;
    default :
      DLog.warning( lctx , headerLog + "Failed to send message , found "
          + "anonymous content type = " + providerMessage.getContentType() );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      notf = null;
      return notf;
    }

    // update message content based on content type
    String udh = null;
    switch ( providerMessage.getContentType() ) {
    case ContentType.SMSBINARY :
      String[] messages = providerMessage.getMessage().split( "\\." );
      if ( messages.length < 1 ) {
        DLog.warning( lctx , headerLog + "Failed to parse binary message "
            + ", can not find udh and/or pdu" );
        providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
        providerMessage.setExternalStatus( "INVALID BINARY MESSAGE" );
        notf = null;
        return notf;
      }
      notf.setUdh( MbloxUtil.extractMbloxBinary( messages[0] ) );
      notf.setMessage( MbloxUtil.extractMbloxBinary( messages[1] ) );
      DLog.debug( lctx ,
          headerLog + "Defined : format = " + notf.getMessageFormat()
              + " , udh = " + notf.getUdh() + " , pdu = " + notf.getMessage() );
      break;
    case ContentType.SMSUNICODE :
      udh = (String) providerMessage.getOptionalParam( "udh" );
      if ( udh != null ) {
        notf.setUdh( udh );
      }
      notf.setMessage( providerMessage.getMessage() );
      DLog.debug( lctx ,
          headerLog + "Defined : format = " + notf.getMessageFormat()
              + " , udh = " + notf.getUdh() + " , message = "
              + StringEscapeUtils.escapeJava( notf.getMessage() ) );
      break;
    case ContentType.SMSTEXT :
      udh = (String) providerMessage.getOptionalParam( "udh" );
      if ( udh != null ) {
        notf.setUdh( udh );
      }
      notf.setMessage( providerMessage.getMessage() );
      DLog.debug( lctx ,
          headerLog + "Defined : format = " + notf.getMessageFormat()
              + " , udh = " + notf.getUdh() + " , message = "
              + StringEscapeUtils.escapeJava( notf.getMessage() ) );
      break;
    default :
      DLog.warning( lctx , headerLog + "Failed to send message , found "
          + "anonymous content type = " + providerMessage.getContentType() );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      notf = null;
      return notf;
    }

    // read profile id
    String providerDescription = providerOut.getDescription();
    if ( providerDescription != null ) {
      String[] providerDescriptions = providerDescription.split( "," );
      int i , n = providerDescriptions.length;
      for ( i = 0 ; i < n ; i++ ) {
        String str = providerDescriptions[i];
        if ( str.startsWith( "Profile=" ) ) {
          notf.setProfile( str.substring( 8 ).trim() );
        }
      }
    }
    if ( StringUtils.isBlank( notf.getProfile() ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found empty profileId" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_EMPTYAUTH );
      notf = null;
      return notf;
    }

    // read sender id
    String senderId = providerMessage.getOriginAddrMask();
    if ( !StringUtils.isBlank( senderId ) ) {
      String senderIdType = SenderIdType.parseType( senderId );
      if ( StringUtils.isBlank( senderIdType ) ) {
        DLog.warning( lctx , headerLog + "Found empty senderIdType - "
            + senderId );
        providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
        providerMessage.setExternalStatus( "EMPTY SENDERID TYPE" );
        notf = null;
        return notf;
      }
      if ( senderId.startsWith( "+" ) ) {
        senderId = senderId.substring( 1 );
      }
      notf.setSenderIdType( senderIdType );
      notf.setSenderId( senderId );
    }

    // read subscriber number
    String subscriberNumber = providerMessage.getDestinationAddress();
    if ( subscriberNumber.startsWith( "+" ) ) {
      subscriberNumber = subscriberNumber.substring( 1 );
    }
    notf.setSubscriberNumber( subscriberNumber );

    return notf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class MbloxAgentThread extends Thread {

    private int idx;

    public MbloxAgentThread( int idx ) {
      super( "MbloxAgentThread-" + providerId() + "." + idx );
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
        connection = getAvailableConnection( BaseAgent.METHOD_ROUND_ROBIN );
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

        // process only for long concatenated message
        String optParam = null;
        optParam = (String) providerMessage.getOptionalParam( "delmsg" );
        if ( optParam != null ) {
          DLog.debug( lctx , headerLog + "Found optionalParam : delmsg = "
              + optParam + " , the provider message process will stop here." );
          continue;
        }
        optParam = (String) providerMessage.getOptionalParam( "orimsgcnt" );
        if ( optParam != null ) {
          try {
            DLog.debug( lctx , headerLog + "Updated the new message count : "
                + providerMessage.getMessageCount() + " -> " + optParam );
            providerMessage.setMessageCount( Integer.parseInt( optParam ) );
          } catch ( NumberFormatException e ) {
            DLog.warning( lctx , headerLog
                + "Failed to update new message count , " + e );
          }
        }
        optParam = (String) providerMessage.getOptionalParam( "orimsg" );
        if ( optParam != null ) {
          DLog.debug( lctx , headerLog + "Updated the new message content : "
              + StringEscapeUtils.escapeJava( providerMessage.getMessage() )
              + " -> " + StringEscapeUtils.escapeJava( optParam ) );
          providerMessage.setMessage( optParam );
        }

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
  } // MbloxAgentThread Class

}
