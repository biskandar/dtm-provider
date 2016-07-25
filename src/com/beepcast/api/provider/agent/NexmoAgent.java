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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.ThreadLocalParser;
import com.firsthop.common.util.xml.TreeUtil;

public class NexmoAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "NexmoAgent" );
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

  public NexmoAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new NexmoAgentThread( index );
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

    // prepare parameters
    String apiKey = provider.getAccessUsername();
    String apiSecret = provider.getAccessPassword();
    String from = providerMessage.getOriginAddrMask();
    String to = providerMessage.getDestinationAddress();
    String type = null;
    String text = providerMessage.getMessage();
    String statusReportReq = "1";
    String clientRef = providerMessage.getExternalMessageId();
    String ttl = (String) confMessageParams.get( "ttl" );

    // ensure add '+' in to number field
    if ( !to.startsWith( "+" ) ) {
      to = "+" + to;
    }

    // resolve type and text
    if ( providerMessage.getContentType() == ContentType.SMSTEXT ) {
      // by pass for plain text
    } else if ( providerMessage.getContentType() == ContentType.SMSUNICODE ) {
      type = "unicode";
    } else {
      DLog.warning( lctx , headerLog + "Found anonymous content type = "
          + providerMessage.getContentType() );
      providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      return;
    }

    // compose parameters
    StringBuffer httpParameters = new StringBuffer();
    try {
      httpParameters.append( "api_key=" );
      httpParameters.append( URLEncoder.encode( apiKey , "UTF-8" ) );
      httpParameters.append( "&api_secret=" );
      httpParameters.append( URLEncoder.encode( apiSecret , "UTF-8" ) );
      httpParameters.append( "&from=" );
      httpParameters.append( URLEncoder.encode( from , "UTF-8" ) );
      httpParameters.append( "&to=" );
      httpParameters.append( URLEncoder.encode( to , "UTF-8" ) );
      if ( type != null ) {
        httpParameters.append( "&type=" );
        httpParameters.append( URLEncoder.encode( type , "UTF-8" ) );
      }
      if ( text != null ) {
        httpParameters.append( "&text=" );
        httpParameters.append( URLEncoder.encode( text , "UTF-8" ) );
      }
      if ( statusReportReq != null ) {
        httpParameters.append( "&status-report-req=" );
        httpParameters.append( URLEncoder.encode( statusReportReq , "UTF-8" ) );
      }
      if ( clientRef != null ) {
        httpParameters.append( "&client-ref=" );
        httpParameters.append( URLEncoder.encode( clientRef , "UTF-8" ) );
      }
      if ( ttl != null ) {
        httpParameters.append( "&ttl=" );
        httpParameters.append( URLEncoder.encode( ttl , "UTF-8" ) );
      }
      DLog.debug( lctx , headerLog + "Composed httpParameters "
          + httpParameters.toString() );
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
      httpConn.setRequestMethod( "POST" );
      httpConn.setRequestProperty( "Content-Type" ,
          "application/x-www-form-urlencoded" );
      httpConn.setDoInput( true );
      httpConn.setDoOutput( true );
      httpConn.setUseCaches( false );
      DataOutputStream os = new DataOutputStream( httpConn.getOutputStream() );
      os.writeBytes( httpParameters.toString() );
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

  private boolean parseBodyResponse( String headerLog , String bodyResponse ,
      ProviderMessage providerMessage ) {
    boolean result = false;
    try {

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

      // clean xml tag
      bodyResponse = bodyResponse.replaceFirst( "<\\?xml.+\\?>\\s*" , "" );

      // parse xml
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

      // prepare parameters
      int messageCount = providerMessage.getMessageCount();
      String externalStatus = providerMessage.getExternalStatus();
      String externalMessageId = providerMessage.getExternalMessageId();

      // extract xml node
      Node nodeMessages = TreeUtil.first( element , "messages" );
      if ( nodeMessages != null ) {
        try {
          messageCount = Integer.parseInt( TreeUtil.getAttribute( nodeMessages ,
              "count" ) );
        } catch ( Exception e ) {
        }
        Node nodeMessage = TreeUtil.first( nodeMessages , "message" );
        if ( nodeMessage != null ) {
          Node nodeStatus = TreeUtil.first( nodeMessage , "status" );
          if ( nodeStatus != null ) {
            externalStatus = TreeUtil.childText( nodeStatus );
          }
          Node nodeClientRef = TreeUtil.first( nodeMessage , "clientRef" );
          if ( nodeClientRef != null ) {
            externalMessageId = TreeUtil.childText( nodeClientRef );
          }
        } // if ( nodeMessage != null )
      } // if ( nodeMessages != null )

      // update back to the provider message
      providerMessage.setMessageCount( messageCount );
      providerMessage.setExternalStatus( externalStatus );
      providerMessage.setExternalMessageId( externalMessageId );

      // return as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response , " + e );
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class NexmoAgentThread extends Thread {

    private int idx;

    public NexmoAgentThread( int idx ) {
      super( "NexmoAgentThread-" + providerId() + "." + idx );
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

  } // NexmoAgentThread Class

}
