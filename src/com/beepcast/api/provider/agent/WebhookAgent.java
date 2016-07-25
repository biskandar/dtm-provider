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

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.util.http.HttpEngine;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class WebhookAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "WebhookAgent" );
  static final Object lockObject = new Object();

  static final String METHOD_GET = "GET";
  static final String METHOD_POST = "POST";

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

  public WebhookAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
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
    return new WebhookAgentThread( index );
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

    // set always true

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void request( ProviderMessage providerMessage ) {

    // compose header log
    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    // update submit date
    providerMessage.setSubmitDateTime( new Date() );

    // generate externalMessageId based on internalMessageId
    providerMessage.setExternalMessageId( providerMessage
        .getInternalMessageId() );

    // resolve httpParameters from provider message content
    String httpParameters = providerMessage.getMessage();
    if ( ( httpParameters == null ) || ( httpParameters.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Found empty httpParameters" );
      providerMessage.setInternalStatus( "FAILED" );
      providerMessage.setExternalStatus( "EMPTY HTTP PARAMETERS" );
      return;
    }

    // resolve httpMethod from provider message optional param
    String method = (String) providerMessage
        .getOptionalParam( TransactionMessageParam.HDR_WEBHOOK_METHOD );
    if ( ( method == null ) || ( method.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed empty http method "
          + ", set default to " + METHOD_POST );
      method = METHOD_POST;
    }
    if ( ( !method.equalsIgnoreCase( METHOD_POST ) )
        && ( !method.equalsIgnoreCase( METHOD_GET ) ) ) {
      DLog.warning( lctx , headerLog + "Found invalid http method "
          + ", set default to " + METHOD_POST );
      method = METHOD_POST;
    }
    DLog.debug( lctx , headerLog + "Resolved request http method " + method );

    // resolve httpUrl from provider message optional param
    String httpUrl = (String) providerMessage
        .getOptionalParam( TransactionMessageParam.HDR_WEBHOOK_URI );
    if ( ( httpUrl == null ) || ( httpUrl.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Found empty httpUrl" );
      providerMessage.setInternalStatus( "FAILED" );
      providerMessage.setExternalStatus( "EMPTY HTTP-URL" );
      return;
    }

    // apply for http parameters
    if ( ( httpParameters != null ) && ( !httpParameters.equals( "" ) ) ) {
      if ( method.equalsIgnoreCase( METHOD_GET ) ) {
        // add into url
        httpUrl = httpUrl.concat( "?" ).concat( httpParameters );
      }
      if ( method.equalsIgnoreCase( METHOD_POST ) ) {
        // just log it is enough
        DLog.debug( lctx , headerLog + "Resolved post data : " + httpParameters );
      }
    }

    // compose remote url
    URL remoteUrl = null;
    try {
      remoteUrl = new URL( httpUrl );
    } catch ( MalformedURLException e ) {
      DLog.warning( lctx , headerLog + "Found malformed remote url - "
          + httpUrl );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_INVALIDURI );
      return;
    }
    DLog.debug( lctx , headerLog + "Created the request to remoteUrl "
        + remoteUrl );

    // prepare external status
    int externalStatus = 0;

    // submit request and read response
    try {

      long deltaTime = System.currentTimeMillis();
      HttpURLConnection httpConn = (HttpURLConnection) remoteUrl
          .openConnection();

      httpConn.setRequestMethod( method );
      httpConn.setRequestProperty( "User-Agent" , "Webhook" );
      httpConn.setUseCaches( false );
      httpConn.setDoInput( true );

      if ( method.equalsIgnoreCase( METHOD_POST ) ) {
        httpConn.setRequestProperty( "Content-Type" ,
            "application/x-www-form-urlencoded" );
        httpConn.setDoOutput( true );
        DataOutputStream os = new DataOutputStream( httpConn.getOutputStream() );
        os.writeBytes( httpParameters );
        os.flush();
        os.close();
      }

      // set external status
      externalStatus = httpConn.getResponseCode();

      // read response
      StringBuffer sbLines = new StringBuffer();
      BufferedReader in = new BufferedReader( new InputStreamReader(
          httpConn.getInputStream() ) );
      String strLine;
      while ( ( strLine = in.readLine() ) != null ) {
        sbLines.append( strLine );
      }
      in.close();
      String bodyResponse = sbLines.toString();

      // log it
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Processed the response ( " + deltaTime
          + " ms ) = " + StringEscapeUtils.escapeJava( bodyResponse ) );

    } catch ( IOException e ) {
      DLog.warning( lctx , headerLog + "Found failed to connect "
          + "to remoteHost - " + httpUrl );
    }

    // extract message submitted status
    providerMessage.setInternalStatus( "SUBMITTED" );
    providerMessage.setExternalStatus( Integer.toString( externalStatus ) );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class WebhookAgentThread extends Thread {

    private int idx;
    private HttpEngine httpEngine;

    public WebhookAgentThread( int idx ) {
      super( "WebhookAgentThread-" + providerId() + "." + idx );
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

        trapLoad();
        request( providerMessage );
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

  } // WebhookAgentThread Class

}
