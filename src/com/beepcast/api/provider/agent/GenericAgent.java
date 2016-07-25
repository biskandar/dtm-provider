package com.beepcast.api.provider.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.agent.generic.GenericVariables;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.util.http.HttpEngine;
import com.beepcast.util.http.HttpFactory;
import com.beepcast.util.http.HttpMessage;
import com.beepcast.util.http.HttpMethod;
import com.beepcast.util.http.HttpRequestParams;
import com.beepcast.util.http.HttpResponseParams;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class GenericAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "GenericAgent" );
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

  public GenericAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new GenericAgentThread( index );
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

  private void sendMessage( ProviderMessage providerMessage , Connection conn ,
      HttpEngine httpEngine ) {

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

    // generic is only for sms text type
    if ( providerMessage.getContentType() != ContentType.SMSTEXT ) {
      DLog.warning( lctx , headerLog + "Found anonymous content type = "
          + providerMessage.getContentType() );
      providerMessage.setInternalStatus( "INTERNAL SYSTEM ERROR" );
      providerMessage.setExternalStatus( "INVALID CONTENT TYPE" );
      return;
    }

    // compose http request params and http message
    HttpRequestParams httpRequestParams = null;
    try {
      int httpMethod = HttpMethod.resolveHttpMethod( (String) confScriptParams
          .get( "method" ) );
      String httpUrl = conn.getHttpUrl();
      Map mapProperties = new HashMap();
      httpRequestParams = HttpFactory.createHttpRequestParams( httpMethod ,
          httpUrl , mapProperties , null );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to compose http request params , " + e );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }
    if ( httpRequestParams == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to compose http request params" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }

    // compose http message
    HttpMessage httpMessage = null;
    try {
      String queryString = (String) confScriptParams.get( "queryString" );
      String requestBody = (String) confScriptParams.get( "requestBody" );
      String responseBody = null;
      httpMessage = HttpFactory.createHttpMessage( queryString , requestBody ,
          responseBody );
      httpMessage.addRequestMapParams( GenericVariables.setupMapVariables(
          provider , providerMessage , confScriptParams , confMessageParams ) );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to compose http message , " + e );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }
    if ( httpMessage == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to compose http message" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }

    try {

      // submit request
      HttpResponseParams httpResponseParams = httpEngine.send(
          httpRequestParams , httpMessage );

      // log it
      DLog.debug(
          lctx ,
          headerLog + "Created the request : method = "
              + httpRequestParams.getMethod() + " , remoteUrl = "
              + httpRequestParams.getUri() + " , queryString = "
              + httpMessage.getQueryString() );

      // validate http response
      if ( httpResponseParams == null ) {
        DLog.warning( lctx , headerLog + "Found empty response" );
        providerMessage.setExternalStatus( EXTSTATUS_FAILED_NORESPONSE );
      } else {

        // log it
        DLog.debug(
            lctx ,
            headerLog + "Processed the response ( "
                + httpMessage.getLatencyMillis() + " ms ) : status = "
                + httpResponseParams.getStatusCode() + " "
                + httpResponseParams.getStatusMessage() + " , content = "
                + StringEscapeUtils.escapeJava( httpMessage.getResponseBody() ) );

        // validate and extract response body
        if ( StringUtils.isBlank( httpMessage.getResponseBody() ) ) {
          DLog.warning( lctx , headerLog + "Found empty response" );
          providerMessage.setExternalStatus( EXTSTATUS_FAILED_NORESPONSE );
        } else {
          if ( parseBodyResponse( headerLog , httpMessage , providerMessage ) ) {
            DLog.debug( lctx , headerLog + "Extracted external status = "
                + providerMessage.getExternalStatus() );
          } else {
            DLog.warning( lctx , headerLog + "Found failed to parse "
                + "the http querystring content" );
            providerMessage.setExternalStatus( EXTSTATUS_FAILED_NORESPONSE );
          }
        }

      }

    } catch ( MalformedURLException e ) {

      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found malformed url exception , " + e );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );

    } catch ( IOException e ) {

      // found failed connection need to drop connection
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to connect to remoteHost - " + conn.getHttpUrl() );
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

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to send message , " + e );
    }

    // extract message status
    extractStatusMessage( providerMessage );

  }

  private boolean parseBodyResponse( String headerLog ,
      HttpMessage httpMessage , ProviderMessage providerMessage ) {
    boolean result = false;
    try {

      // read external status
      String externalStatus = httpMessage
          .getResponseMapParamValue( "MESSAGE_EXTERNALSTATUSCODE" );
      if ( ( externalStatus != null ) && ( !externalStatus.equals( "" ) ) ) {
        providerMessage.setExternalStatus( externalStatus );
      }

      // read external message id
      String externalMessageId = httpMessage
          .getResponseMapParamValue( "MESSAGE_ID" );
      if ( ( externalMessageId != null ) && ( !externalMessageId.equals( "" ) ) ) {
        providerMessage.setExternalMessageId( externalMessageId );
      }

      // log it
      DLog.debug( lctx , headerLog
          + "Resolved response body : externalStatus = " + externalStatus
          + " , externalMessageId = " + externalMessageId );

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

  class GenericAgentThread extends Thread {

    private int idx;
    private HttpEngine httpEngine;

    public GenericAgentThread( int idx ) {
      super( "GenericAgentThread-" + providerId() + "." + idx );
      this.idx = idx;
    }

    public void run() {

      try {

        // setup and start http engine
        String scriptFile = (String) confScriptParams.get( "file" );
        DLog.debug( lctx , headerLog() + "Setup http engine : scriptFile = "
            + scriptFile );
        httpEngine = new HttpEngine( scriptFile );
        httpEngine.start();
        DLog.debug( lctx , headerLog() + "Started http engine" );

      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found failed to create and/or start http engine , " + e );
        return;
      }

      // setup response queue
      BoundedLinkedQueue respQueue = getRespQueue();
      if ( respQueue == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null resp queue" );
        return;
      }

      // setup send queue
      BoundedLinkedQueue sendQueueInt = getSendQueueInt();
      if ( sendQueueInt == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null send queue" );
        return;
      }

      // prepare variables for the send worker
      Connection connection = null;
      Object objectMessage = null;
      ProviderMessage providerMessage = null;
      String headerLog = null;

      // worker start
      DLog.debug( lctx , headerLog() + "Thread started" );
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
        sendMessage( providerMessage , connection , httpEngine );
        mtSendWorker.createMTTicket( providerMessage );

        // put the response back in the queue
        try {
          respQueue.put( providerMessage );
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , headerLog + "Failed to put provider message "
              + "into the response queue , " + e );
        }

      } // while ( arrAgentThreads[idx] )

      // stop http engine
      try {
        httpEngine.stop();
        DLog.debug( lctx , headerLog() + "Stopped http engine" );
      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog() + "Failed to stop http engine , " + e );
      }

      // log thread stop
      DLog.debug( lctx , headerLog() + "Thread stopped" );

    }

  } // GenericAgentThread Class

}