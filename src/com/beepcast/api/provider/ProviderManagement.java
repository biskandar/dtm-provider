package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.util.QueueUtil;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderManagement implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProviderManagement" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private ProviderApp providerApp;
  private ProviderConf providerConf;

  private MOWorker moWorker;
  private MTSendWorker mtSendWorker;
  private MTRespWorker mtRespWorker;
  private DRWorker drWorker;

  private boolean activeThread;
  private Thread managementThread;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProviderManagement( ProviderApp providerApp ) {
    initialized = false;

    if ( providerApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null provider app" );
      return;
    }

    this.providerApp = providerApp;

    providerConf = providerApp.getProviderConf();

    if ( providerConf == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null provider conf" );
      return;
    }

    moWorker = providerApp.getMoWorker();
    mtSendWorker = providerApp.getMtSendWorker();
    mtRespWorker = providerApp.getMtRespWorker();
    drWorker = providerApp.getDrWorker();

    if ( ( moWorker == null ) || ( drWorker == null ) ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null moWorker and/or drWorker" );
      return;
    }

    if ( ( mtSendWorker == null ) || ( mtRespWorker == null ) ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null mtSendWorker and/or mtRespWorker" );
      return;
    }

    managementThread = new ManagementThread( "ProviderManagementThread" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , found not yet initialized" );
      return;
    }
    activeThread = true;
    managementThread.start();
  }

  public void stop() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop , found not yet initialized" );
      return;
    }
    activeThread = false;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class ManagementThread extends Thread {

    private BoundedLinkedQueue moQueue;
    private BoundedLinkedQueue mtRespQueue;
    private BoundedLinkedQueue drQueue;

    private BoundedLinkedQueue mtRespDataBatchQueue;
    private BoundedLinkedQueue drDataBatchQueue;

    private int tempSizeMoQueue;
    private int tempSizeMtRespQueue;
    private int tempSizeDrQueue;

    private int tempSizeMoBatchQueue;
    private int tempSizeMtRespBatchQueue;
    private int tempSizeDrBatchQueue;

    private int cleanIdleCtr = 0;
    private int cleanIdleMax = 0;

    private List providerIds;

    private int moQueueFullAlertCounter;
    private int mtRespQueueFullAlertCounter;
    private int drQueueFullAlertCounter;
    private Map mtProvidersQueueFullAlertCounter;

    public ManagementThread( String name ) {
      super( name );

      moQueue = (BoundedLinkedQueue) providerApp.getMoQueue();
      mtRespQueue = (BoundedLinkedQueue) mtRespWorker.getMtRespQueue();
      drQueue = (BoundedLinkedQueue) providerApp.getDrQueue();

      providerIds = new ArrayList();
      providerIds.addAll( mtSendWorker.listActiveMtAgentProviderIds() );
      DLog.debug( lctx , "Found total " + providerIds.size()
          + " provider(s) ready to monitor" );

      mtRespDataBatchQueue = mtRespWorker.getDataBatchQueue();
      drDataBatchQueue = drWorker.getDataBatchQueue();

      tempSizeMoQueue = 0;
      tempSizeMtRespQueue = 0;
      tempSizeDrQueue = 0;

      tempSizeMoBatchQueue = 0;
      tempSizeMtRespBatchQueue = 0;
      tempSizeDrBatchQueue = 0;

      cleanIdleCtr = 0;
      cleanIdleMax = providerConf.getManagementCleanIdle();

      moQueueFullAlertCounter = 0;
      mtRespQueueFullAlertCounter = 0;
      drQueueFullAlertCounter = 0;
      mtProvidersQueueFullAlertCounter = new HashMap();
    }

    private String infoMtAgentQueues() {
      String result = "";
      String providerId;
      BoundedLinkedQueue queue;
      Iterator iter = providerIds.iterator();
      while ( iter.hasNext() ) {
        providerId = (String) iter.next();
        if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
          continue;
        }
        queue = (BoundedLinkedQueue) mtSendWorker.getMtAgentQueue( providerId );
        if ( queue == null ) {
          continue;
        }
        result = result + ", " + providerId + "=" + queue.size();
      }
      return result;
    }

    private void alertMtAgentQueues( double threshold ) {
      String providerId;
      BoundedLinkedQueue mtSendQueue;
      Double sp;
      Iterator iter = providerIds.iterator();
      while ( iter.hasNext() ) {
        providerId = (String) iter.next();
        if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
          continue;
        }
        mtSendQueue = (BoundedLinkedQueue) mtSendWorker
            .getMtAgentQueue( providerId );
        if ( mtSendQueue == null ) {
          continue;
        }
        sp = QueueUtil.getSizePercentage( mtSendQueue );
        if ( sp == null ) {
          continue;
        }
        if ( sp.doubleValue() < threshold ) {
          mtProvidersQueueFullAlertCounter.put( providerId , new Integer( 0 ) );
          continue;
        }
        Integer alertCounter = (Integer) mtProvidersQueueFullAlertCounter
            .get( providerId );
        alertCounter = ( alertCounter == null ) ? new Integer( 0 )
            : alertCounter;
        if ( ( alertCounter != null ) && ( alertCounter.intValue() < 1 ) ) {
          ProviderStatusAlert.sendAlertProviderSendQueueFull( providerId ,
              mtSendQueue );
        }
        mtProvidersQueueFullAlertCounter.put( providerId , new Integer(
            alertCounter.intValue() + 1 ) );
      }
    }

    private void alertMtRespQueue( double threshold ) {
      Double sp = QueueUtil.getSizePercentage( mtRespQueue );
      if ( sp == null ) {
        return;
      }
      if ( sp.doubleValue() < threshold ) {
        mtRespQueueFullAlertCounter = 0;
        return;
      }
      if ( mtRespQueueFullAlertCounter < 1 ) {
        ProviderStatusAlert.sendAlertMtRespQueueFull( mtRespQueue );
      }
      mtRespQueueFullAlertCounter = mtRespQueueFullAlertCounter + 1;
    }

    private void alertDrQueue( double threshold ) {
      Double sp = QueueUtil.getSizePercentage( drQueue );
      if ( sp == null ) {
        return;
      }
      if ( sp.doubleValue() < threshold ) {
        drQueueFullAlertCounter = 0;
        return;
      }
      if ( drQueueFullAlertCounter < 1 ) {
        ProviderStatusAlert.sendAlertDrQueueFull( drQueue );
      }
      drQueueFullAlertCounter = drQueueFullAlertCounter + 1;
    }

    public void run() {
      DLog.debug( lctx , "Thread running" );

      // verify all worker connections status

      mtSendWorker.verifyAllStatusConnections();
      List listActiveOutgoingWorkerProviderIdsCur = ListInOuProviderIdsUtils
          .listOutgoingProviderIds( null , true , true , false ,
              providerConf.isDebug() );
      List listActiveIncomingWorkerProviderIdsCur = ListInOuProviderIdsUtils
          .listIncomingProviderIds( providerConf.isDebug() );
      DLog.debug( lctx , "List active worker providers : outgoing = "
          + listActiveOutgoingWorkerProviderIdsCur + " , incoming = "
          + listActiveIncomingWorkerProviderIdsCur );

      // refresh all worker threads

      moWorker.setupThreads();
      mtSendWorker.refreshAllAgentThreads();
      mtRespWorker.setupThreads();
      drWorker.setupThreads();

      long counter = 0;

      int sizeMoQueue = 0;
      int sizeMtRespQueue = 0;
      int sizeDrQueue = 0;

      int sizeMoBatchQueue = 0;
      int sizeMtRespBatchQueue = 0;
      int sizeDrBatchQueue = 0;

      while ( activeThread ) {

        try {
          // delay 1s
          Thread.sleep( 1000 );
        } catch ( InterruptedException e ) {
        }

        counter = counter + 1000;
        if ( counter < providerConf.getManagementPeriod() ) {
          continue;
        }
        counter = 0;

        sizeMoQueue = moQueue.size();
        sizeMtRespQueue = mtRespQueue.size();
        sizeDrQueue = drQueue.size();

        sizeMoBatchQueue = 0; // moDataBatchQueue.size();
        sizeMtRespBatchQueue = mtRespDataBatchQueue.size();
        sizeDrBatchQueue = drDataBatchQueue.size();

        // check idle ?
        if ( ( sizeMoQueue != 0 ) || ( sizeMtRespQueue != 0 )
            || ( sizeDrQueue != 0 ) || ( sizeMoBatchQueue != 0 )
            || ( sizeMtRespBatchQueue != 0 ) || ( sizeDrBatchQueue != 0 ) ) {

          DLog.info( lctx , "Queue MO=" + sizeMoQueue + ", MTResp="
              + sizeMtRespQueue + ", DR=" + sizeDrQueue + ", MOBatch="
              + sizeMoBatchQueue + ", MTRespBatch=" + sizeMtRespBatchQueue
              + ", DRBatch=" + sizeDrBatchQueue + infoMtAgentQueues() );

          if ( ( sizeMoQueue == tempSizeMoQueue )
              && ( sizeMtRespQueue == tempSizeMtRespQueue )
              && ( sizeDrQueue == tempSizeDrQueue )
              && ( sizeMoBatchQueue == tempSizeMoBatchQueue )
              && ( sizeMtRespBatchQueue == tempSizeMtRespBatchQueue )
              && ( sizeDrBatchQueue == tempSizeDrBatchQueue ) ) {

            cleanIdleCtr = cleanIdleCtr + 1;
            if ( cleanIdleCtr >= cleanIdleMax ) {
              DLog.debug( lctx , "Perform cleaning service for mt resp worker" );
              mtRespWorker.clean();
              DLog.debug( lctx , "Perform cleaning service for dr worker" );
              drWorker.clean();
            }

          } else {
            cleanIdleCtr = 0;
          }

        }

        // alert when found queue full ( threshold 75 % )

        alertMtAgentQueues( 75 );
        alertMtRespQueue( 75 );
        alertDrQueue( 75 );

        // verify all worker connections status

        mtSendWorker.verifyAllStatusConnections();
        List listActiveOutgoingWorkerProviderIdsNew = ListInOuProviderIdsUtils
            .listOutgoingProviderIds( null , true , true , false ,
                providerConf.isDebug() );
        List listActiveIncomingWorkerProviderIdsNew = ListInOuProviderIdsUtils
            .listIncomingProviderIds( providerConf.isDebug() );
        if ( ( listActiveOutgoingWorkerProviderIdsNew.size() != listActiveOutgoingWorkerProviderIdsCur
            .size() )
            || ( listActiveIncomingWorkerProviderIdsNew.size() != listActiveIncomingWorkerProviderIdsCur
                .size() ) ) {
          DLog.debug( lctx , "List active worker providers : outgoing = "
              + listActiveOutgoingWorkerProviderIdsCur + " -> "
              + listActiveOutgoingWorkerProviderIdsNew + " , incoming = "
              + listActiveIncomingWorkerProviderIdsCur + " -> "
              + listActiveIncomingWorkerProviderIdsNew );
          listActiveOutgoingWorkerProviderIdsCur = listActiveOutgoingWorkerProviderIdsNew;
          listActiveIncomingWorkerProviderIdsCur = listActiveIncomingWorkerProviderIdsNew;
        }

        // refresh all worker threads

        moWorker.setupThreads();
        mtSendWorker.refreshAllAgentThreads();
        mtRespWorker.setupThreads();
        drWorker.setupThreads();

        // renew queue size status

        tempSizeMoQueue = sizeMoQueue;
        tempSizeMtRespQueue = sizeMtRespQueue;
        tempSizeDrQueue = sizeDrQueue;

        tempSizeMoBatchQueue = sizeMoBatchQueue;
        tempSizeMtRespBatchQueue = sizeMtRespBatchQueue;
        tempSizeDrBatchQueue = sizeDrBatchQueue;

      }

      DLog.debug( lctx , "Thread stopped" );
    }
  } // ManagementThread

}
