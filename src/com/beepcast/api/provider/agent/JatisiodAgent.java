package com.beepcast.api.provider.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class JatisiodAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "JatisiodAgent" );

  static final Object lockObject = new Object();

  static final String DEFAULT_DESTINATION = "op.*";

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

  public JatisiodAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new JatisiodAgentThread( index );
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

  private Connection getAvailableConnection( ProviderMessage providerMessage ) {
    Connection conn = null;
    if ( providerMessage == null ) {
      return conn;
    }
    // resolved destination as connection name
    String destinationNode = providerMessage.getDestinationNode();
    if ( StringUtils.isBlank( destinationNode ) ) {
      destinationNode = DEFAULT_DESTINATION;
    }
    // get connection params
    Map connectionParams = providerAgentConf.getConnectionParams();
    if ( connectionParams == null ) {
      return conn;
    }
    // resolved connection id
    String connId = (String) connectionParams.get( destinationNode );
    if ( StringUtils.isBlank( connId ) ) {
      return conn;
    }
    // resolved connection
    synchronized ( lockObject ) {
      int i , n = connections.size();
      for ( i = 0 ; i < n ; i++ ) {
        Connection tconn = (Connection) connections.get( i );
        if ( tconn == null ) {
          continue;
        }
        if ( !tconn.isActive() ) {
          continue;
        }
        if ( StringUtils.equalsIgnoreCase( tconn.getId() , connId ) ) {
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

    // generate externalMessageId based on internalMessageId
    providerMessage.setExternalMessageId( providerMessage
        .getInternalMessageId() );

    // read and validate provider connection
    TProvider provider = verifyProviderConnection( providerMessage , conn );
    if ( provider == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to verify provider connection" );
      return;
    }

    // read and validate provider parameter
    if ( !verifyProviderParameters( providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found invalid provider parameters" );
      return;
    }

    // compose http body request
    String httpReqParams = createHttpReqParams( providerMessage , provider );
    if ( StringUtils.isBlank( httpReqParams ) ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found empty http body request" );
      return;
    }
    DLog.debug( lctx , headerLog + "Composed httpReqParams = "
        + StringEscapeUtils.escapeJava( httpReqParams ) );

    // compose remote url
    URL remoteUrl = null;
    try {
      remoteUrl = new URL( conn.getHttpUrl() + httpReqParams );
    } catch ( MalformedURLException e ) {
      DLog.warning( lctx ,
          headerLog + "Found malformed remote url - " + conn.getHttpUrl() );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }
    DLog.debug( lctx ,
        headerLog + "Created the request to remoteUrl " + conn.getHttpUrl() );

    // Submit Request and Read Response
    try {

      long deltaTime = System.currentTimeMillis();

      // prepare http url connection with method get

      HttpURLConnection httpConn = (HttpURLConnection) remoteUrl
          .openConnection();
      httpConn.setRequestMethod( "GET" );
      httpConn.setDoOutput( true );
      httpConn.setUseCaches( false );

      // open connection

      httpConn.connect();

      // read response

      StringBuffer sbLines = new StringBuffer();
      InputStream is = httpConn.getInputStream();
      InputStreamReader isr = new InputStreamReader( is );
      BufferedReader in = new BufferedReader( isr );
      String strLine;
      while ( ( strLine = in.readLine() ) != null ) {
        sbLines.append( strLine );
      }
      String bodyResponse = sbLines.toString();
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Processed the response ( " + deltaTime
          + " ms ) = " + StringEscapeUtils.escapeJava( bodyResponse ) );

      // close connection

      httpConn.disconnect();

      // parse reponse

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
    try {
      // get status code ( as number )
      int statusCode = Integer.parseInt( httpBody );
      // read externalStatus and externalMessageId
      String externalStatus = Integer.toString( statusCode );
      DLog.debug( lctx , headerLog + "Found external status = "
          + externalStatus );
      // update as external status
      providerMessage.setExternalStatus( externalStatus );
      // set result status as succeed
      result = true;
    } catch ( NumberFormatException e ) {
      DLog.warning( lctx , headerLog + "Failed to parse http body , " + e );
    }
    return result;
  }

  private String createHttpReqParams( ProviderMessage providerMessage ,
      TProvider providerProfile ) {
    String httpReqParams = null;

    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http request params "
          + ", found null providerMessage" );
      return httpReqParams;
    }

    if ( providerProfile == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http request params "
          + ", found null providerProfile" );
      return httpReqParams;
    }

    String uid = providerProfile.getAccessUsername();
    String pwd = providerProfile.getAccessPassword();
    String guid = "";
    String messagein = "";
    String messageout = providerMessage.getMessage();
    String msisdn = providerMessage.getDestinationAddress();
    String operator = "";
    String datesmsc = "";
    String shortcode = providerMessage.getOriginAddress();
    String datereceiver = "";
    String smscmessageid = "";
    String channel = StringUtils.trimToEmpty( (String) super.confMessageParams
        .get( "channel" ) );
    String servicetype = StringUtils
        .trimToEmpty( (String) super.confMessageParams.get( "servicetype" ) );
    String chargingid = StringUtils
        .trimToEmpty( (String) super.confMessageParams.get( "chargingid" ) );
    String retry = "0";
    String media = StringUtils.trimToEmpty( (String) super.confMessageParams
        .get( "media" ) );
    String resend = "0";
    String sidcontent = StringUtils
        .trimToEmpty( (String) super.confMessageParams.get( "sidcontent" ) );
    String smstype = StringUtils.trimToEmpty( (String) super.confMessageParams
        .get( "smstype" ) );

    String encType = "UTF-8";
    StringBuffer sbParams = new StringBuffer();
    try {
      sbParams.append( "?uid=" );
      sbParams.append( URLEncoder.encode( uid , encType ) );
      sbParams.append( "&pwd=" );
      sbParams.append( URLEncoder.encode( pwd , encType ) );
      sbParams.append( "&guid=" );
      sbParams.append( URLEncoder.encode( guid , encType ) );
      sbParams.append( "&esme=" );
      sbParams.append( "&datacoding=" );
      sbParams.append( "&messagein=" );
      sbParams.append( URLEncoder.encode( messagein , encType ) );
      sbParams.append( "&messageout=" );
      sbParams.append( URLEncoder.encode( messageout , encType ) );
      sbParams.append( "&msisdn=" );
      sbParams.append( URLEncoder.encode( msisdn , encType ) );
      sbParams.append( "&operator=" );
      sbParams.append( URLEncoder.encode( operator , encType ) );
      sbParams.append( "&datesmsc=" );
      sbParams.append( URLEncoder.encode( datesmsc , encType ) );
      sbParams.append( "&shortcode=" );
      sbParams.append( URLEncoder.encode( shortcode , encType ) );
      sbParams.append( "&datereceiver=" );
      sbParams.append( URLEncoder.encode( datereceiver , encType ) );
      sbParams.append( "&smscmessageid=" );
      sbParams.append( URLEncoder.encode( smscmessageid , encType ) );
      sbParams.append( "&contentcode=" );
      sbParams.append( "&err=" );
      sbParams.append( "&channel=" );
      sbParams.append( URLEncoder.encode( channel , encType ) );
      sbParams.append( "&servicetype=" );
      sbParams.append( URLEncoder.encode( servicetype , encType ) );
      sbParams.append( "&chargingid=" );
      sbParams.append( URLEncoder.encode( chargingid , encType ) );
      sbParams.append( "&retry=" );
      sbParams.append( URLEncoder.encode( retry , encType ) );
      sbParams.append( "&media=" );
      sbParams.append( URLEncoder.encode( media , encType ) );
      sbParams.append( "&content=" );
      sbParams.append( "&publisher=" );
      sbParams.append( "&artist=" );
      sbParams.append( "&Title=" );
      sbParams.append( "&resend=" );
      sbParams.append( URLEncoder.encode( resend , encType ) );
      sbParams.append( "&urlip=" );
      sbParams.append( "&sidcontent=" );
      sbParams.append( URLEncoder.encode( sidcontent , encType ) );
      sbParams.append( "&smstype=" );
      sbParams.append( URLEncoder.encode( smstype , encType ) );
      sbParams.append( "&key1=" );
      sbParams.append( "&key2=" );
      sbParams.append( "&key3=" );
    } catch ( UnsupportedEncodingException e ) {
      DLog.warning( lctx , headerLog + "Failed to create "
          + "http request params , " + e );
      return httpReqParams;
    }

    httpReqParams = sbParams.toString();
    return httpReqParams;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class JatisiodAgentThread extends Thread {

    private int idx;

    public JatisiodAgentThread( int idx ) {
      super( "JatisiodAgentThread-" + providerId() + "." + idx );
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

        // find available connection
        connection = getAvailableConnection( providerMessage );
        if ( connection == null ) {
          providerMessage.setInternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
          providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
          DLog.warning( lctx , headerLog + "Failed to send message "
              + ", found empty connection" );
        } else if ( !connection.isActive() ) {
          providerMessage.setInternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
          providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
          DLog.warning( lctx , headerLog + "Failed to send message "
              + ", found empty connection" );
        } else {

          // log it
          DLog.debug(
              lctx ,
              headerLog + "Resolved map destination "
                  + "to connection : destinationNode = "
                  + providerMessage.getDestinationNode()
                  + " , connection.id = " + connection.getId()
                  + " , connection.url = " + connection.getHttpUrl() );

          // trap load
          trapLoad();

          // send the message
          sendMessage( providerMessage , connection );

        }

        // create cdr mt
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

  } // JatisiodAgentThread Class

}
