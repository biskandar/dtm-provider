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

public class SmsplusAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SmsplusAgent" );
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

  public SmsplusAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new SmsplusAgentThread( index );
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

    // prepare parameters
    String accessUsername = provider.getAccessUsername();
    String accessPassword = provider.getAccessPassword();
    String destinationAddress = providerMessage.getDestinationAddress();
    String messageContent = providerMessage.getMessage();
    String originAddressMask = providerMessage.getOriginAddrMask();

    // read and validate provider parameter
    if ( !verifyProviderParameters( providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found invalid provider parameters" );
      return;
    }

    // ensure add '+' in destinationAddress number field
    if ( !destinationAddress.startsWith( "+" ) ) {
      destinationAddress = "+" + destinationAddress;
    }

    // resolve the message content and type
    int messageType = -1;
    if ( providerMessage.getContentType() == ContentType.SMSTEXT ) {
      messageType = 0;
    } else if ( providerMessage.getContentType() == ContentType.SMSUNICODE ) {
      messageType = 2;
      messageContent = convertToUnicodeMessage( messageContent );
    } else {
      DLog.warning( lctx , headerLog + "Found anonymous content type = "
          + providerMessage.getContentType() );
      providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      return;
    }

    // username of smpp account
    String username = StringUtils.trimToEmpty( accessUsername );
    // password of smpp account
    String password = StringUtils.trimToEmpty( accessPassword );
    // type of message : 0 - Plain Text , 2 - Unicode
    String type = Integer.toString( messageType );
    // delivery report required
    String dlr = "1";
    // mobile number which to send , +659xx
    String destination = StringUtils.trimToEmpty( destinationAddress );
    // sender id , max 18 for num , max 11 alpanum
    String source = StringUtils.trimToEmpty( originAddressMask );
    // message content ( can be long message as well )
    String message = ( messageContent == null ) ? "" : messageContent;

    // compose parameters
    StringBuffer httpParameters = new StringBuffer();
    try {
      httpParameters.append( "username=" );
      httpParameters.append( URLEncoder.encode( username , "UTF-8" ) );
      httpParameters.append( "&password=" );
      httpParameters.append( URLEncoder.encode( password , "UTF-8" ) );
      httpParameters.append( "&type=" );
      httpParameters.append( URLEncoder.encode( type , "UTF-8" ) );
      httpParameters.append( "&dlr=" );
      httpParameters.append( URLEncoder.encode( dlr , "UTF-8" ) );
      httpParameters.append( "&destination=" );
      httpParameters.append( URLEncoder.encode( destination , "UTF-8" ) );
      httpParameters.append( "&source=" );
      httpParameters.append( URLEncoder.encode( source , "UTF-8" ) );
      httpParameters.append( "&message=" );
      httpParameters.append( URLEncoder.encode( message , "UTF-8" ) );
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

      // split per delivery report message
      String[] arrDrMsgs = bodyResponse.split( "[\\s]*,[\\s]*" );

      // split inside delivery report message
      String[] arrDrMsgIdx = null;
      if ( ( arrDrMsgs != null ) && ( arrDrMsgs.length > 0 ) ) {
        arrDrMsgIdx = arrDrMsgs[0].split( "[ |]" );
      }

      // define external status code and message id
      String externalStatusCode = null;
      String externalMessageId = null;
      if ( ( arrDrMsgIdx != null ) && ( arrDrMsgIdx.length > 2 ) ) {
        externalStatusCode = arrDrMsgIdx[0];
        externalMessageId = arrDrMsgIdx[2];
      }

      // update back to the provider message
      providerMessage.setExternalStatus( externalStatusCode );
      providerMessage.setExternalMessageId( externalMessageId );

      // return as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response , " + e );
    }
    return result;
  }

  private String convertToUnicodeMessage( String txt ) {
    String uni = null;
    if ( txt == null ) {
      return uni;
    }
    StringBuffer sbUni = new StringBuffer();
    int hexLength = 0;
    char[] chars = txt.toCharArray();
    for ( int idx = 0 ; idx < chars.length ; idx++ ) {
      String hex = Integer.toHexString( (int) chars[idx] );
      if ( ( hex == null ) || ( hex.equals( "" ) ) ) {
        sbUni.append( "0000" );
      } else {
        hexLength = hex.length();
        if ( hexLength == 1 ) {
          sbUni.append( "000" );
          sbUni.append( hex );
        } else if ( hexLength == 2 ) {
          sbUni.append( "00" );
          sbUni.append( hex );
        } else if ( hexLength == 3 ) {
          sbUni.append( "0" );
          sbUni.append( hex );
        } else if ( hexLength == 4 ) {
          sbUni.append( hex );
        } else {
          sbUni.append( "0000" );
        }
      }
    } // for ( int idx = 0 ; idx < chars.length ; idx++ )
    uni = sbUni.toString();
    return uni;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class SmsplusAgentThread extends Thread {

    private int idx;

    public SmsplusAgentThread( int idx ) {
      super( "SmsplusAgentThread-" + providerId() + "." + idx );
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

  } // SmsplusAgentThread Class

}
