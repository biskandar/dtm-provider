package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.agent.Agent;
import com.beepcast.api.provider.agent.AgentFactory;
import com.beepcast.api.provider.common.ProviderUtil;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.QueueUtil;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.common.util.concurrent.Channel;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MTSendWorker implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MTSendWorker" );
  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ProviderApp providerApp;
  private ProviderConf providerConf;

  private Map mtAgentQueues;
  private Map mtAgentWorkers;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MTSendWorker( ProviderApp providerApp ) {

    this.providerApp = providerApp;
    this.providerConf = providerApp.getProviderConf();

    createMtAgentQueues();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {

    createMtAgentWorkers();

    String providerId;
    String headerLog;
    Agent agent;
    Iterator iter = mtAgentWorkers.keySet().iterator();
    while ( iter.hasNext() ) {
      providerId = (String) iter.next();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      headerLog = ProviderCommon.headerLog( providerId );
      agent = (Agent) mtAgentWorkers.get( providerId );
      if ( agent == null ) {
        continue;
      }
      if ( !agent.isEnabled() ) {
        DLog.warning( lctx , headerLog + "Failed to start Mt Agent Worker "
            + ", found set to disabled" );
        continue;
      }
      DLog.debug( lctx , headerLog + "Starting Mt Agent Worker" );
      agent.start();
    }

  }

  public void stop() {

    String providerId;
    String headerLog;
    Agent agent;
    Iterator iter = mtAgentWorkers.keySet().iterator();
    while ( iter.hasNext() ) {
      providerId = (String) iter.next();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      headerLog = ProviderCommon.headerLog( providerId );
      agent = (Agent) mtAgentWorkers.get( providerId );
      if ( agent == null ) {
        continue;
      }
      DLog.debug( lctx , headerLog + "Stopping Mt Agent Worker" );
      agent.stop();
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean sendMessage( ProviderMessage providerMessage ) {
    boolean result = false;
    long deltaTime = System.currentTimeMillis();

    // validate must be parameters
    if ( providerMessage == null ) {
      DLog.warning( lctx , "Failed to send message "
          + ", found null provider message" );
      return result;
    }
    String providerId = providerMessage.getProviderId();
    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , "Failed to send message "
          + ", found blank provider id" );
      return result;
    }

    // compose header log
    String headerLog = ProviderCommon.headerLog( providerId );

    // every message must have an internal message id
    // when found null the system will reject it
    if ( StringUtils.isBlank( providerMessage.getInternalMessageId() ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found blank internal message id" );
      return result;
    }

    // compose header log
    headerLog = headerLog + ProviderMessageCommon.headerLog( providerMessage );

    // resolve destination node
    if ( !resolveDestinationNode( providerMessage ) ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found unresolved destination node" );
      return result;
    }

    // get provider agent queue
    Channel providerAgentQueue = getMtAgentQueue( providerMessage
        .getDestinationNode() );
    if ( providerAgentQueue == null ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found null provider agent channel" );
      return result;
    }

    // queued the message into specific provider's queue
    try {
      result = providerAgentQueue.offer( providerMessage , 2500 );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to send message "
          + ", found failed to store into the provider agent channel , " + e );
    }

    // track delta time
    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it
    StringBuffer sbLog = new StringBuffer();
    sbLog.append( result ? "Successfully stored " : "Failed to store " );
    sbLog.append( "provider message into provider agent channel , with : " );
    sbLog.append( providerMessage.getDestinationNode() );
    sbLog.append( ", " );
    sbLog.append( providerMessage.getDestinationAddress() );
    sbLog.append( ", " );
    sbLog.append( providerMessage.getMessageType() );
    sbLog.append( ", " );
    sbLog.append( ProviderMessageCommon.getContentTypeInfo( providerMessage
        .getContentType() ) );
    sbLog.append( ", " );
    sbLog.append( StringEscapeUtils.escapeJava( providerMessage.getMessage() ) );
    sbLog.append( ", takes = " );
    sbLog.append( deltaTime );
    sbLog.append( " ms" );
    if ( result ) {
      DLog.debug( lctx , headerLog + sbLog.toString() );
    } else {
      DLog.warning( lctx , headerLog + sbLog.toString() );
    }

    return result;
  }

  public ArrayList listActiveMtAgentProviderIds() {
    ArrayList list = listAllMtAgentProviderIds();
    if ( list == null ) {
      list = new ArrayList();
      return list;
    }
    Iterator iter = list.iterator();
    while ( iter.hasNext() ) {
      String providerId = (String) iter.next();
      if ( StringUtils.isBlank( providerId ) ) {
        iter.remove();
        continue;
      }
      Agent agent = getMtAgentWorker( providerId );
      if ( agent == null ) {
        iter.remove();
        continue;
      }
      if ( !agent.isEnabled() ) {
        iter.remove();
        continue;
      }
      if ( !agent.isConnectionsAvailable() ) {
        iter.remove();
        continue;
      }
    }
    return list;
  }

  public void verifyAllStatusConnections() {
    ArrayList list = listAllMtAgentProviderIds();
    if ( list == null ) {
      return;
    }
    Iterator iterProviderIds = list.iterator();
    while ( iterProviderIds.hasNext() ) {
      String providerId = (String) iterProviderIds.next();
      if ( StringUtils.isBlank( providerId ) ) {
        continue;
      }
      Agent agent = getMtAgentWorker( providerId );
      if ( agent == null ) {
        continue;
      }
      if ( !agent.isEnabled() ) {
        continue;
      }
      agent.verifyStatusConnections();
    }
  }

  public void refreshAllAgentThreads() {
    ArrayList list = listAllMtAgentProviderIds();
    if ( list == null ) {
      return;
    }
    Iterator iterProviderIds = list.iterator();
    while ( iterProviderIds.hasNext() ) {
      String providerId = (String) iterProviderIds.next();
      if ( StringUtils.isBlank( providerId ) ) {
        continue;
      }
      Agent agent = getMtAgentWorker( providerId );
      if ( agent == null ) {
        continue;
      }
      if ( !agent.isEnabled() ) {
        continue;
      }
      agent.setupAgentThreads();
    }
  }

  public ArrayList listAllMtAgentProviderIds() {
    ArrayList list = new ArrayList();
    if ( mtAgentQueues == null ) {
      return list;
    }
    list = new ArrayList( mtAgentQueues.keySet() );
    return list;
  }

  public Agent getMtAgentWorker( String providerId ) {
    Agent agent = null;
    if ( providerId == null ) {
      DLog.warning( lctx , "Failed to find mt agent worker "
          + ", found null providerId" );
      return agent;
    }
    String headerLog = ProviderCommon.headerLog( providerId );
    if ( mtAgentWorkers == null ) {
      DLog.warning( lctx , headerLog + "Failed to find mt agent worker "
          + ", found null mtAgentWorkers" );
      return agent;
    }
    agent = (Agent) mtAgentWorkers.get( providerId );
    return agent;
  }

  public BoundedLinkedQueue getMtAgentQueue( String providerId ) {
    BoundedLinkedQueue queue = null;
    if ( providerId == null ) {
      DLog.warning( lctx , "Failed to find mt agent queue "
          + ", found null providerId" );
      return queue;
    }
    String headerLog = ProviderCommon.headerLog( providerId );
    if ( mtAgentQueues == null ) {
      DLog.warning( lctx , headerLog + "Failed to find mt agent queue "
          + ", found null mtAgentQueues" );
      return queue;
    }
    queue = (BoundedLinkedQueue) mtAgentQueues.get( providerId );
    return queue;
  }

  public Double percentageFillMtAgentQueue( String providerId ) {
    Double percentage = null;
    if ( providerId == null ) {
      DLog.warning( lctx , "Failed to get percentage fill mt agent queue "
          + ", found null providerId" );
      return percentage;
    }
    String headerLog = ProviderCommon.headerLog( providerId );
    BoundedLinkedQueue queue = getMtAgentQueue( providerId );
    if ( queue == null ) {
      DLog.warning( lctx , headerLog + "Failed to get percentage fill mt "
          + "agent queue , found can not find the queue" );
      return percentage;
    }
    percentage = QueueUtil.getSizePercentage( queue );
    return percentage;
  }

  public Boolean activeConnectionMtAgentWorker( String providerId ) {
    Boolean active = null;
    if ( providerId == null ) {
      DLog.warning( lctx , "Failed to get active connection mt agent worker "
          + ", found null providerId" );
      return active;
    }
    String headerLog = ProviderCommon.headerLog( providerId );
    Agent agent = getMtAgentWorker( providerId );
    if ( agent == null ) {
      DLog.warning( lctx , headerLog + "Failed to get active connection mt "
          + "agent worker , found null agent" );
      return active;
    }
    active = new Boolean( agent.isConnectionsAvailable() );
    return active;
  }

  public boolean createDRTicket( ProviderMessage msg ) {
    return providerApp.createDRTicket( msg );
  }

  public boolean createMTTicket( ProviderMessage msg ) {
    return providerApp.createMTTicket( msg );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean resolveDestinationNode( ProviderMessage providerMessage ) {
    boolean result = false;
    if ( providerMessage == null ) {
      return result;
    }

    // header log
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // set default destination node as current provider id
    String destinationNode = providerMessage.getProviderId();

    // resolve provider
    TProvider providerOut = ProviderUtil.resolveProvider( headerLog ,
        destinationNode );
    if ( providerOut != null ) {
      destinationNode = providerOut.getProviderId();
    }

    // update destination node into provider message
    providerMessage.setDestinationNode( destinationNode );
    result = true;
    return result;
  }

  private void createMtAgentQueues() {
    DLog.debug( lctx , "Trying to create mt agent queue(s)" );

    if ( providerConf == null ) {
      DLog.warning( lctx , "Failed to create mt agent queues "
          + ", found null provider conf" );
      return;
    }
    Map providerAgentConfs = providerConf.getProviderAgentConfs();
    if ( providerAgentConfs == null ) {
      DLog.warning( lctx , "Failed to create mt agent queues "
          + ", found null provider agent conf" );
      return;
    }

    // prepare the map
    mtAgentQueues = new HashMap();

    ProviderAgentConf providerAgentConf;
    String providerId;
    String headerLog;
    BoundedLinkedQueue queue;
    Iterator iter = providerAgentConfs.keySet().iterator();
    while ( iter.hasNext() ) {
      providerAgentConf = (ProviderAgentConf) providerAgentConfs
          .get( (String) iter.next() );
      if ( providerAgentConf == null ) {
        continue;
      }
      providerId = providerAgentConf.getProviderId();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      headerLog = ProviderCommon.headerLog( providerId );
      if ( !providerAgentConf.isEnabled() ) {
        DLog.warning( lctx , headerLog + "Failed to create mt agent queue "
            + ", found provider is disabled" );
        continue;
      }
      if ( mtAgentQueues.get( providerId ) != null ) {
        DLog.warning( lctx , headerLog + "Failed to create mt agent queue "
            + ", found is that the queue is already exist" );
        continue;
      }
      queue = createMtAgentQueue( providerAgentConf );
      if ( queue != null ) {
        mtAgentQueues.put( providerId , queue );
        DLog.debug( lctx , headerLog + "Successfully created "
            + "mt agent queue , max size = " + queue.capacity() + " msg(s)" );
      }
    } // iterate all provider(s)

    DLog.debug( lctx , "Successfully created mt agent queue(s)" );
  }

  private void createMtAgentWorkers() {
    DLog.debug( lctx , "Trying to create mt agent worker(s)" );

    if ( providerConf == null ) {
      DLog.warning( lctx , "Failed to create mt agent workers "
          + ", found null provider conf" );
      return;
    }
    Map providerAgentConfs = providerConf.getProviderAgentConfs();
    if ( providerAgentConfs == null ) {
      DLog.warning( lctx , "Failed to create mt agent workers "
          + ", found null provider agent conf" );
      return;
    }

    // prepare the map
    mtAgentWorkers = new HashMap();

    // create agent workers
    Iterator iter = providerAgentConfs.keySet().iterator();
    while ( iter.hasNext() ) {
      ProviderAgentConf providerAgentConf = (ProviderAgentConf) providerAgentConfs
          .get( (String) iter.next() );
      if ( providerAgentConf == null ) {
        continue;
      }
      String providerId = providerAgentConf.getProviderId();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      if ( mtAgentWorkers.get( providerId ) != null ) {
        continue;
      }
      Agent agent = AgentFactory.createAgent( providerConf , providerAgentConf ,
          this , providerApp.getMtRespWorker() );
      if ( agent == null ) {
        continue;
      }
      mtAgentWorkers.put( agent.providerId() , agent );
    }

    // log it
    DLog.debug( lctx , "Successfully created total " + mtAgentWorkers.size()
        + " mt agent worker(s)" );
  }

  private BoundedLinkedQueue createMtAgentQueue(
      ProviderAgentConf providerAgentConf ) {
    BoundedLinkedQueue queue = null;
    if ( providerAgentConf == null ) {
      DLog.warning( lctx , "Can not construct agent queue object "
          + ", found empty conf" );
      return queue;
    }

    String providerId = providerAgentConf.getProviderId();
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Can not construct agent worker object "
          + ", found empty providerId " );
      return queue;
    }

    // compose header log
    String headerLog = ProviderCommon.headerLog( providerId );

    int queueSize = providerAgentConf.getQueueSize();
    if ( queueSize < 1 ) {
      queueSize = 100;
      DLog.warning( lctx , headerLog + "Found zero queue size "
          + ", will using default value ~ 100 msg(s)" );
    }

    // create queue object
    queue = new BoundedLinkedQueue( queueSize );
    return queue;
  }

}
