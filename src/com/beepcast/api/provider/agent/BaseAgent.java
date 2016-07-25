package com.beepcast.api.provider.agent;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.Connection;
import com.beepcast.api.provider.ConnectionCommon;
import com.beepcast.api.provider.ConnectionConf;
import com.beepcast.api.provider.ConnectionFactory;
import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderCommon;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.ProviderMessageOptionalMapParamUtils;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.DBManagerApp;
import com.beepcast.dbmanager.common.DnStatusMapCommon;
import com.beepcast.dbmanager.table.TDnStatusMap;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public abstract class BaseAgent implements Agent , ProcessExtToInt {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "BaseAgent" );

  public static final int MIN_AGENT_THREADS = 5;
  public static final int MAX_AGENT_THREADS = 100;

  public static final int METHOD_FAIL_OVER = 0;
  public static final int METHOD_ROUND_ROBIN = 1;

  public static final String INTSTATUS_ERROR_INTERNAL = "ERROR-INTERNAL";

  public static final String EXTSTATUS_ERROR_INTERNAL = "ERROR-INTERNAL";
  public static final String EXTSTATUS_ERROR_NOPROVIDER = "ERROR-NOPROVIDER";
  public static final String EXTSTATUS_ERROR_NOCONNECTION = "ERROR-NOCONNECTION";
  public static final String EXTSTATUS_ERROR_EMPTYAUTH = "ERROR-EMPTYAUTH";
  public static final String EXTSTATUS_ERROR_EMPTYNUMBER = "ERROR-EMPTYNUMBER";
  public static final String EXTSTATUS_ERROR_EMPTYMESSAGE = "ERROR-EMPTYMESSAGE";
  public static final String EXTSTATUS_ERROR_ENCODINGTYPE = "ERROR-ENCODINGTYPE";
  public static final String EXTSTATUS_ERROR_INVALIDURI = "ERROR-INVALIDURI";

  public static final String EXTSTATUS_FAILED_CONNECTION = "FAILED-CONNECTION";
  public static final String EXTSTATUS_FAILED_NORESPONSE = "FAILED-NORESPONSE";

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private LoadManagement ldMan;
  private DBManagerApp dbMan;
  private OnlinePropertiesApp opropsApp;

  protected MTRespWorker mtRespWorker;
  protected MTSendWorker mtSendWorker;

  private String providerId;
  private String headerLog;

  protected ProviderConf providerConf;
  protected ProviderAgentConf providerAgentConf;

  protected Map confScriptParams;
  protected Map confMessageParams;

  private BoundedLinkedQueue sendQueueExt; // external send queue
  private BoundedLinkedQueue sendQueueInt; // internal send queue
  private BoundedLinkedQueue respQueue;
  private ProcessExtToIntWorker processExtToIntWorker;

  protected int idxConn;
  protected List connections;
  protected int idxErrorConnection;

  private int curAgentThreads;
  private Object lckAgentThreads;
  protected boolean[] arrAgentThreads;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public BaseAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
      MTSendWorker mtSendWorker , String providerId ,
      ProviderAgentConf providerAgentConf ) {
    initialized = false;

    ldMan = LoadManagement.getInstance();
    dbMan = DBManagerApp.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();

    if ( providerConf == null ) {
      DLog.warning( lctx , "Can not perform initialized "
          + ", found null providerConf" );
      return;
    }

    if ( mtRespWorker == null ) {
      DLog.warning( lctx , "Can not perform initialized "
          + ", found null mtRespWorker" );
      return;
    }

    if ( mtSendWorker == null ) {
      DLog.warning( lctx , "Can not perform initialized "
          + ", found null mtSendWorker" );
      return;
    }

    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Can not perform initialized "
          + ", found null providerId" );
      return;
    }

    if ( providerAgentConf == null ) {
      DLog.warning( lctx , "Can not perform initialized "
          + ", found null providerAgentConf" );
      return;
    }

    // set data member
    this.mtRespWorker = mtRespWorker;
    this.mtSendWorker = mtSendWorker;
    this.providerId = providerId;
    this.providerConf = providerConf;
    this.providerAgentConf = providerAgentConf;

    // compose header log
    headerLog = ProviderCommon.headerLog( providerId );

    // load queue
    if ( !loadQueue() ) {
      DLog.warning( lctx , headerLog() + "Can not initialized "
          + ", failed to load response and/or send queue" );
      return;
    }

    // create internal process thread to migrate from ext channel to int channel
    processExtToIntWorker = new ProcessExtToIntWorker( providerId , this , 1 ,
        10 , 100 , 5000 );
    DLog.debug( lctx , headerLog() + "Created worker thread to migrate "
        + "message from external channel to internal channel" );

    // load all params
    loadAllParams();

    // generate connections
    generateConnections();

    // reset connections
    resetConnections();

    // prepared agent threads
    initAgentThreads();

    // log it
    DLog.debug( lctx , headerLog() + "Successfully created based agent " );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function : Agent
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String providerId() {
    return providerId;
  }

  public void verifyStatusConnections() {
    if ( !initialized ) {
      return;
    }

    // validate is it need to perform verify ?
    if ( !opropsApp.getBoolean(
        "ProviderAgent.EnableErrorCheck.".concat( providerId() ) ,
        providerAgentConf.isEnableErrorCheck() ) ) {
      return;
    }

    // verify status connections with thread runnable
    Thread thread = new Thread( new Runnable() {
      public void run() {

        int i , n = connections.size();
        for ( i = 0 ; i < n ; i++ ) {
          Connection conn = (Connection) connections.get( i );
          if ( conn == null ) {
            continue;
          }
          if ( conn.isActive() ) {
            // no need to verify status for active connection
            continue;
          }
          HttpURLConnection httpConn = null;
          try {

            // trap delta time
            long deltaTime = System.currentTimeMillis();

            // log first
            DLog.debug( lctx , headerLog()
                + "Verify provider's status connection " + conn.getHttpUrl() );

            // try to connect the url
            URL remoteUrl = new URL( conn.getHttpUrl() );
            httpConn = (HttpURLConnection) remoteUrl.openConnection();
            httpConn.connect();

            // read response code and message
            int respCode = httpConn.getResponseCode();
            String respMessage = httpConn.getResponseMessage();

            // update connection became active
            conn.startup();

            // send alert message to support team
            ProviderStatusAlert.sendAlertProviderStatusActive( providerId() ,
                conn );

            // trap delta time
            deltaTime = System.currentTimeMillis() - deltaTime;

            // log it
            DLog.debug( lctx , headerLog + "Read status reponse : code = "
                + respCode + " , message = " + respMessage
                + " , send alert message , and updated active status = "
                + "false -> true , take " + deltaTime + " ms" );

            // make sleep for a while
            Thread.sleep( 1000 );

          } catch ( Exception e ) {
            DLog.warning( lctx , headerLog()
                + "Failed to verify status connection , found " + e );
          } finally {
            try {
              if ( httpConn != null ) {
                // close the connection
                httpConn.disconnect();
              }
            } catch ( Exception e ) {
            }
          }
        } // for ( i = 0 ; i < n ; i++ ) {

      }
    } , "VerifyStatusConnectionWorker-" + providerId() );

    // thread start
    thread.start();
  }

  public int maxErrorConnection() {
    return (int) opropsApp.getLong(
        "ProviderAgent.MaxErrorTolerant.".concat( providerId() ) ,
        providerAgentConf.getMaxErrorTolerant() );
  }

  public boolean isEnabled() {
    boolean result = false;
    if ( providerAgentConf == null ) {
      return result;
    }
    result = providerAgentConf.isEnabled();
    return result;
  }

  public boolean setupAgentThreads() {
    return setupAgentThreads( getAgentWorkerSize() );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function : ProcessExtToInt
  //
  // ////////////////////////////////////////////////////////////////////////////

  public BoundedLinkedQueue getQueueExt() {
    return sendQueueExt;
  }

  public BoundedLinkedQueue getQueueInt() {
    return sendQueueInt;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function : Child
  //
  // ////////////////////////////////////////////////////////////////////////////

  protected BoundedLinkedQueue getSendQueueInt() {
    return sendQueueInt;
  }

  protected BoundedLinkedQueue getRespQueue() {
    return respQueue;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void startThread() {

    int agentWorkerSize = getAgentWorkerSize();
    setupAgentThreads( agentWorkerSize );
    DLog.debug( lctx , headerLog() + "Setup agent threads size = "
        + agentWorkerSize );

    if ( processExtToIntWorker != null ) {
      processExtToIntWorker.start();
      DLog.debug( lctx , headerLog() + "Worker to process msg "
          + "from ext to int channel is started" );
    }

  }

  public void stopThread() {

    int agentWorkerSize = 0;
    setupAgentThreads( agentWorkerSize );
    DLog.debug( lctx , headerLog() + "Setup agent threads size = "
        + agentWorkerSize );

    if ( processExtToIntWorker != null ) {
      processExtToIntWorker.stopThread();
      DLog.debug( lctx , headerLog() + "Worker to process msg "
          + "from ext to int channel is stopped" );
    }

  }

  public String headerLog() {
    return headerLog;
  }

  public TProvider verifyProviderConnection( ProviderMessage providerMessage ,
      Connection conn ) {
    TProvider provider = null;

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog() + "Failed to verify "
          + "provider connection , found null provider message" );
      return provider;
    }

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    if ( conn == null ) {
      DLog.warning( lctx , headerLog + "Failed to verify connection "
          + ", found empty connection" );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
      extractStatusMessage( providerMessage );
      return provider;
    }

    if ( !conn.isActive() ) {
      DLog.warning( lctx , headerLog + "Failed to verify connection "
          + ", found inactive connection" );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOCONNECTION );
      extractStatusMessage( providerMessage );
      return provider;
    }

    provider = com.beepcast.dbmanager.common.ProviderCommon
        .getProvider( providerMessage.getProviderId() );
    if ( provider == null ) {
      DLog.warning( lctx , headerLog + "Failed to verify provider "
          + ", found empty provider" );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOPROVIDER );
      extractStatusMessage( providerMessage );
      return provider;
    }

    if ( !provider.isActive() ) {
      provider = null;
      DLog.warning( lctx , headerLog + "Failed to verify provider "
          + ", found inactive provider" );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_NOPROVIDER );
      extractStatusMessage( providerMessage );
      return provider;
    }

    return provider;
  }

  public boolean setupAgentThreads( int workerSize ) {
    boolean result = false;
    synchronized ( lckAgentThreads ) {
      if ( workerSize < MIN_AGENT_THREADS ) {
        workerSize = MIN_AGENT_THREADS;
      }
      if ( workerSize > arrAgentThreads.length ) {
        workerSize = arrAgentThreads.length;
      }
      if ( workerSize == curAgentThreads ) {
        // nothing to do ...
      }
      if ( workerSize > curAgentThreads ) {
        for ( int idx = curAgentThreads ; idx < workerSize ; idx++ ) {
          arrAgentThreads[idx] = true;
          Thread thread = createAgentThread( idx );
          thread.start();
        }
        DLog.debug( lctx , headerLog() + "Updated new agent threads size : "
            + curAgentThreads + " -> " + workerSize );
        curAgentThreads = workerSize;
      }
      if ( workerSize < curAgentThreads ) {
        for ( int idx = workerSize ; idx < curAgentThreads ; idx++ ) {
          arrAgentThreads[idx] = false;
        }
        DLog.debug( lctx , headerLog() + "Updated new agent threads size : "
            + curAgentThreads + " -> " + workerSize );
        curAgentThreads = workerSize;
      }
    }
    result = true;
    return result;
  }

  public boolean verifyProviderParameters( ProviderMessage providerMessage ,
      TProvider providerOut ) {
    boolean result = false;

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog() + "Failed to verify provider "
          + "parameters , found null provider message" );
      return result;
    }
    if ( providerOut == null ) {
      DLog.warning( lctx , headerLog() + "Failed to verify provider "
          + "parameters , found null provider object" );
      return result;
    }

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    if ( StringUtils.isBlank( providerOut.getAccessUsername() ) ) {
      DLog.warning( lctx , headerLog + "Failed to verify parameters "
          + ", found empty provider access username" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_EMPTYAUTH );
      return result;
    }

    if ( StringUtils.isBlank( providerMessage.getDestinationAddress() ) ) {
      DLog.warning( lctx , headerLog + "Failed to verify parameters "
          + ", found empty destination address" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_EMPTYNUMBER );
      return result;
    }

    if ( StringUtils.isBlank( providerMessage.getMessage() ) ) {
      DLog.warning( lctx , headerLog + "Failed to verify parameters "
          + ", found empty message content" );
      providerMessage.setInternalStatus( INTSTATUS_ERROR_INTERNAL );
      providerMessage.setExternalStatus( EXTSTATUS_ERROR_EMPTYMESSAGE );
      return result;
    }

    result = true;
    return result;
  }

  public void extractStatusMessage( ProviderMessage providerMessage ) {

    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog() + "Failed to extract status message "
          + ", found null provider message" );
      return;
    }

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // find the dn status map based on external status
    TDnStatusMap dnStatusMap = parseStatus( providerMessage.getProviderId() ,
        providerMessage.getExternalStatus() );

    // when found missing dn map , will use the default value from conf file
    if ( dnStatusMap == null ) {
      String defaultInternalStatus = providerConf.getDefaultInternalStatus();
      DLog.warning( lctx ,
          headerLog + "Found unmap external dn status value = "
              + providerMessage.getExternalStatus()
              + " , force to set with the "
              + "default internal dn status value = " + defaultInternalStatus );
      providerMessage.setInternalStatus( defaultInternalStatus );
      return;
    }

    // log attributes
    DLog.debug(
        lctx ,
        headerLog + "Found matched dn status map : id = "
            + dnStatusMap.getStatusMapId() + " , providerId = "
            + dnStatusMap.getProviderId() + " , shutdown = "
            + dnStatusMap.getShutdown() + " , retry = "
            + dnStatusMap.getRetry() + " , delivered = "
            + dnStatusMap.getDelivered() + " , submitted = "
            + dnStatusMap.getSubmitted() + " , ignored = "
            + dnStatusMap.getSubmitted() + " , description = "
            + dnStatusMap.getDescription() );

    // process message based on the dn status map 's param
    // put message param in the message description field
    if ( dnStatusMap.getSubmitted() > 0 ) {
      DLog.debug( lctx , headerLog + "Enable submitted flag for this message "
          + "externalMessageId = " + providerMessage.getExternalMessageId() );
      ProviderMessageOptionalMapParamUtils
          .setCommandSubmitted( providerMessage );
    }
    if ( dnStatusMap.getDelivered() > 0 ) {
      DLog.debug( lctx , headerLog + "Enable delivered flag for this message "
          + "externalMessageId = " + providerMessage.getExternalMessageId() );
      ProviderMessageOptionalMapParamUtils
          .setCommandDelivered( providerMessage );
    }
    if ( dnStatusMap.getRetry() > 0 ) {
      DLog.debug( lctx , headerLog + "Enable retry flag for this message "
          + "externalMessageId = " + providerMessage.getExternalMessageId() );
      ProviderMessageOptionalMapParamUtils.setCommandRetry( providerMessage );
    }
    if ( dnStatusMap.getShutdown() > 0 ) {
      DLog.debug( lctx , headerLog + "Enable shutdown flag for this message "
          + "externalMessageId = " + providerMessage.getExternalMessageId() );
      ProviderMessageOptionalMapParamUtils.setCommandShutdown( providerMessage );
    }
    if ( dnStatusMap.getIgnored() > 0 ) {
      DLog.debug( lctx , headerLog + "Enable ignored flag for this message "
          + "externalMessageId = " + providerMessage.getExternalMessageId() );
      ProviderMessageOptionalMapParamUtils.setCommandIgnored( providerMessage );
    }

    // set internal status
    providerMessage.setInternalStatus( dnStatusMap.getInternalStatus() );

    // log it
    DLog.debug(
        lctx ,
        headerLog + "Found map status response "
            + providerMessage.getExternalStatus() + " -> "
            + providerMessage.getInternalStatus() + " , optParams = "
            + providerMessage.getOptionalParams() );
  }

  public TDnStatusMap parseStatus( String providerId , String externalStatus ) {
    TDnStatusMap dnStatusMap = null;
    if ( externalStatus == null ) {
      return dnStatusMap;
    }
    dnStatusMap = DnStatusMapCommon
        .getDnStatusMap( providerId , externalStatus );
    return dnStatusMap;
  }

  public void trapLoad() {
    ldMan.hitMt( providerId , 1 );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean loadQueue() {
    boolean result = false;

    // link external send queue from mt agent
    sendQueueExt = mtSendWorker.getMtAgentQueue( providerId );
    if ( sendQueueExt == null ) {
      DLog.warning( lctx , headerLog() + "Failed to get mt send queue" );
      return result;
    }
    DLog.debug( lctx , headerLog() + "Loaded external send queue "
        + ", with queue capacity = " + sendQueueExt.capacity() + " msg(s)" );

    // generate internal send queue
    sendQueueInt = new BoundedLinkedQueue(
        providerAgentConf.getWorkerSize() * 3 );
    if ( sendQueueInt.capacity() < 1 ) {
      DLog.warning( lctx , headerLog() + "Failed to create internal "
          + "send queue" );
      return result;
    }
    DLog.debug( lctx , headerLog() + "Generated internal send queue"
        + ", with queue capacity = " + sendQueueInt.capacity() + " msg(s)" );

    // link response queue from mt resp worker
    respQueue = (BoundedLinkedQueue) mtRespWorker.getMtRespQueue();
    if ( respQueue == null ) {
      DLog.warning( lctx , headerLog() + "Failed to get mt resp queue" );
      return result;
    }
    DLog.debug( lctx , headerLog() + "Loaded resp queue "
        + ", with queue capacity = " + respQueue.capacity() + " msg(s)" );

    result = true;
    return result;
  }

  private void loadAllParams() {

    // load script params
    confScriptParams = new HashMap( providerAgentConf.getScriptParams() );
    DLog.debug( lctx , headerLog() + "Successfully load total "
        + confScriptParams.size() + " script param(s) into provider agent : "
        + confScriptParams );

    // load message params
    confMessageParams = new HashMap( providerAgentConf.getMessageParams() );
    DLog.debug( lctx , headerLog() + "Successfully load total "
        + confMessageParams.size() + " message param(s) into provider agent : "
        + confMessageParams );

  }

  private void generateConnections() {
    connections = new ArrayList();
    List connectionConfs = providerAgentConf.getConnectionConfs();
    if ( connectionConfs == null ) {
      DLog.warning( lctx , headerLog() + "Failed to create connections "
          + ", found null connection conf" );
      return;
    }
    int sizeConn = connectionConfs.size();
    if ( sizeConn < 1 ) {
      DLog.warning( lctx , headerLog() + "Failed to create connections "
          + ", found zero connection conf" );
      return;
    }
    DLog.debug( lctx ,
        headerLog() + "Creating new connections : enableErrorCheck = "
            + providerAgentConf.isEnableErrorCheck() );
    for ( int i = 0 ; i < sizeConn ; i++ ) {
      ConnectionConf connectionConf = (ConnectionConf) connectionConfs.get( i );
      if ( connectionConf == null ) {
        continue;
      }
      Connection connection = ConnectionFactory.createConnection(
          providerAgentConf , connectionConf , providerId() + "." + i );
      if ( connection == null ) {
        continue;
      }
      String headerLog = ConnectionCommon.headerLog( connection );
      if ( connections.add( connection ) ) {
        DLog.debug( lctx ,
            headerLog + "Successfully created connection : " + "active = "
                + connection.isActive() + " , url = " + connection.getHttpUrl() );
      }
    } // iterate all connection(s)
  }

  private void resetConnections() {
    idxConn = 0;
    idxErrorConnection = 0;
    DLog.debug( lctx , headerLog() + "Reset connections : indexConnection = "
        + idxConn + " , indexErrorConnection = " + idxErrorConnection
        + " , maxErrorConnection = " + maxErrorConnection() );
  }

  private void initAgentThreads() {
    curAgentThreads = 0;
    lckAgentThreads = new Object();
    arrAgentThreads = new boolean[MAX_AGENT_THREADS];
    for ( int idx = 0 ; idx < arrAgentThreads.length ; idx++ ) {
      arrAgentThreads[idx] = false;
    }
    DLog.debug( lctx , headerLog() + "Prepared agent threads size : min = "
        + MIN_AGENT_THREADS + " , max = " + MAX_AGENT_THREADS );
  }

  private int getAgentWorkerSize() {
    int workerSize = MIN_AGENT_THREADS;
    if ( providerAgentConf != null ) {
      workerSize = providerAgentConf.getWorkerSize();
    }
    if ( opropsApp != null ) {
      String fieldName = "ProviderAgent.TotalWorkers.".concat( providerId );
      workerSize = (int) opropsApp.getLong( fieldName , workerSize );
    }
    return workerSize;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

}
