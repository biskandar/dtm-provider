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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.DialogueParser;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DialogueAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DialogueAgent" );
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

  public DialogueAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new DialogueAgentThread( index );
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

    // read and validate provider parameter
    if ( !verifyProviderParameters( providerMessage , provider ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found invalid provider parameters" );
      return;
    }

    // compose http request parameters
    String httpReqParams = createHttpReqParams( getMessageParams() ,
        providerMessage , provider );
    if ( StringUtils.isBlank( httpReqParams ) ) {
      DLog.warning( lctx , headerLog + "Failed to create http body "
          + ", found empty http request params" );
      return;
    }
    DLog.debug( lctx , headerLog + "Composed httpReqParams = "
        + StringEscapeUtils.escapeJava( httpReqParams ) );

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
      os.writeBytes( httpReqParams );
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

    // get and validate tag

    int a = httpBody.indexOf( "<!--" );
    int b = httpBody.indexOf( "-->" );
    String strTags = StringUtils.substring( httpBody , a + 4 , b );
    if ( StringUtils.isBlank( strTags ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse http body "
          + "found blank reponse string inside the tag" );
      return result;
    }

    // parse into map

    Map mapTags = DialogueParser.parseResponseTags( strTags );
    if ( mapTags == null ) {
      DLog.warning( lctx , headerLog + "Failed to parse http body "
          + ", found null result dialog response parser" );
      return result;
    }

    // read params from the map

    String externalMessageId = (String) mapTags.get( "X-E3-ID" );
    String externalStatus = (String) mapTags.get( "X-E3-Submission-Report" );
    String xe3ErrorDescription = (String) mapTags
        .get( "X-E3-Error-Description" );

    // log it

    DLog.debug( lctx , headerLog + "Parsed params from http response body "
        + ": externalMessageId = " + externalMessageId + " , externalStatus = "
        + externalStatus + " , xe3ErrorDescription = " + xe3ErrorDescription );

    // validate externalStatus

    if ( StringUtils.isBlank( externalStatus ) ) {
      DLog.warning( lctx , headerLog + "Failed to parse body response "
          + ", can not extract external status" );
      return result;
    }

    // update externalStatus and externalMessageId

    providerMessage.setExternalStatus( externalStatus );
    if ( !StringUtils.isBlank( externalMessageId ) ) {
      providerMessage.setExternalMessageId( externalMessageId );
    }

    result = true;
    return result;
  }

  private String createHttpReqParams( Map messageParams ,
      ProviderMessage providerMessage , TProvider provider ) {
    String httpReqParams = null;

    // prepare header log

    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    // validate must be params

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http request params "
          + ", found null providerMessage" );
      return httpReqParams;
    }
    if ( provider == null ) {
      DLog.warning( lctx , headerLog + "Failed to create http request params "
          + ", found null provider" );
      return httpReqParams;
    }

    // prepare request params

    String httpLogin = provider.getAccessUsername();
    String httpPassword = provider.getAccessPassword();
    String replyPath = provider.getListenerUrl();
    String originatingAddress = providerMessage.getOriginAddrMask();
    String recipients = providerMessage.getDestinationAddress();
    String message = providerMessage.getMessage();
    String submissionId = providerMessage.getInternalMessageId();

    // clean request params

    httpLogin = ( httpLogin == null ) ? "" : httpLogin;
    httpPassword = ( httpPassword == null ) ? "" : httpPassword;
    originatingAddress = ( originatingAddress == null ) ? ""
        : originatingAddress;
    recipients = ( recipients == null ) ? "" : recipients;
    message = ( message == null ) ? "" : message;
    submissionId = ( submissionId == null ) ? "" : submissionId;

    // ensure add '+' in receiver number field

    if ( !recipients.startsWith( "+" ) ) {
      recipients = "+" + recipients;
    }

    // prepare additional http request params

    StringBuffer additionalParameters = new StringBuffer();

    // store additional request params from content type

    try {
      if ( providerMessage.getContentType() == ContentType.SMSBINARY ) {
        // content = StrUtil.substituteSymbol( content , "." , "" );
        DLog.warning( lctx , headerLog
            + "Failed to create http request params "
            + ", content type is not supported yet" );
        return httpReqParams;
      } else if ( providerMessage.getContentType() == ContentType.SMSUNICODE ) {
        // convert the message to 2 digit hex
        // content = StrUtil.convert2HexString( content , "" );
        // additionalParameters.append( "&messagetype=sms_unicode" );
        // additionalParameters.append( "&dataCoding=8" );
        DLog.warning( lctx , headerLog
            + "Failed to create http request params "
            + ", content type is not supported yet" );
        return httpReqParams;
      } else if ( providerMessage.getContentType() == ContentType.SMSTEXT ) {
        additionalParameters.append( "&X-E3-Message="
            + URLEncoder.encode( message , "UTF-8" ) );
      } else {
        DLog.warning( lctx , headerLog
            + "Failed to create http request params , found "
            + "anonymous content type = " + providerMessage.getContentType() );
        return httpReqParams;
      }
    } catch ( UnsupportedEncodingException e ) {
      DLog.warning( lctx , headerLog + "Failed to create http request params "
          + ", found unsupported url encoding type , " + e );
      return httpReqParams;
    }

    // store additional request params from configuration

    String paramName , paramValue;
    List listParamNames = new ArrayList( messageParams.keySet() );
    Iterator iterParamNames = listParamNames.iterator();
    while ( iterParamNames.hasNext() ) {
      try {
        paramName = (String) iterParamNames.next();
        if ( ( paramName == null ) || ( paramName.equals( "" ) ) ) {
          continue;
        }
        paramValue = (String) messageParams.get( paramName );
        if ( ( paramValue == null ) || ( paramValue.equals( "" ) ) ) {
          continue;
        }
        additionalParameters.append( "&" + paramName + "=" );
        additionalParameters.append( URLEncoder.encode( paramValue , "UTF-8" ) );
      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to store additional param "
            + "from conf , " + e );
      }
    }

    // compose http request params

    StringBuffer httpParameters = new StringBuffer();
    try {
      httpParameters.append( "X-E3-HTTP-Login=" );
      httpParameters.append( URLEncoder.encode( httpLogin , "UTF-8" ) );
      httpParameters.append( "&X-E3-HTTP-Password=" );
      httpParameters.append( URLEncoder.encode( httpPassword , "UTF-8" ) );
      httpParameters.append( "&X-E3-Reply-Path=" );
      httpParameters.append( URLEncoder.encode( replyPath , "UTF-8" ) );
      httpParameters.append( "&X-E3-Originating-Address=" );
      httpParameters.append( URLEncoder.encode( originatingAddress , "UTF-8" ) );
      httpParameters.append( "&X-E3-Recipients=" );
      httpParameters.append( URLEncoder.encode( recipients , "UTF-8" ) );
      httpParameters.append( "&X-E3-Submission-ID=" );
      httpParameters.append( URLEncoder.encode( submissionId , "UTF-8" ) );
      httpParameters.append( additionalParameters.toString() );
    } catch ( UnsupportedEncodingException e ) {
      DLog.warning( lctx , headerLog + "Found unsupported url encoding type , "
          + e );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_ENCODINGTYPE );
      return httpReqParams;
    }

    // create http request params

    httpReqParams = httpParameters.toString();

    return httpReqParams;
  }

  private Map getMessageParams() {
    Map messageParams = new HashMap();
    if ( super.confMessageParams != null ) {
      messageParams.putAll( super.confMessageParams );
    }
    return messageParams;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class DialogueAgentThread extends Thread {

    private int idx;

    public DialogueAgentThread( int idx ) {
      super( "DialogueAgentThread-" + providerId() + "." + idx );
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

  } // DialogueAgentThread Class

}
