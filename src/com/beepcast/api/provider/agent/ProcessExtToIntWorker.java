package com.beepcast.api.provider.agent;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.ProviderCommon;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.util.throttle.Throttle;
import com.beepcast.util.throttle.ThrottleProcess;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProcessExtToIntWorker extends Thread implements ThrottleProcess {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProcessExtToIntWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp onlinePropertiesApp;

  private String providerId;
  private String headerLog;

  private BoundedLinkedQueue queueExt; // external queue
  private BoundedLinkedQueue queueInt; // internal queue

  private ProcessExtToInt processExtToInt;

  private int defBurstSize;
  private int defMaxLoad;
  private int defThreshold;
  private int defSleep;

  private boolean activeThread;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProcessExtToIntWorker( String providerId ,
      ProcessExtToInt processExtToInt , int defBurstSize , int defMaxLoad ,
      int defThreshold , int defSleep ) {
    super( "ProcessExtToIntWorker." + providerId );

    initialized = false;

    onlinePropertiesApp = OnlinePropertiesApp.getInstance();

    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found blank provider id" );
      return;
    }
    this.providerId = providerId;

    headerLog = ProviderCommon.headerLog( providerId );

    if ( processExtToInt == null ) {
      DLog.warning( lctx , headerLog + "Failed to initialized "
          + " , found null processExtToInt" );
      return;
    }
    this.processExtToInt = processExtToInt;

    if ( processExtToInt.getQueueExt() == null ) {
      DLog.warning( lctx , headerLog + "Failed to initialized "
          + ", found null external queue " );
      return;
    }
    queueExt = processExtToInt.getQueueExt();
    DLog.debug( lctx ,
        "Loaded external queue : capacity = " + queueExt.capacity() + " msg(s)" );

    if ( processExtToInt.getQueueInt() == null ) {
      DLog.warning( lctx , headerLog + "Failed to initialized "
          + ", found null internal queue " );
      return;
    }
    queueInt = processExtToInt.getQueueInt();
    DLog.debug( lctx ,
        "Loaded internal queue : capacity = " + queueInt.capacity() + " msg(s)" );

    this.defBurstSize = defBurstSize;
    this.defMaxLoad = defMaxLoad;
    this.defThreshold = defThreshold;
    this.defSleep = defSleep;
    DLog.debug( lctx , "Read default : burstSize = " + defBurstSize
        + " pipe(s) , maxLoad = " + defMaxLoad + " msg(s)/sec , threshold = "
        + defThreshold + " msg(s) , sleep = " + defSleep + " ms" );

    activeThread = false;

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void stopThread() {

    if ( !initialized ) {
      DLog.warning( lctx , headerLog + "Failed to stop the thread "
          + ", found not yet initialized" );
      return;
    }

    activeThread = false;
    DLog.debug( lctx , headerLog + "Found flag to stop the thread" );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean processMessage() {
    boolean result = false;
    try {

      // method channel take() and put() will use
      // blocking thread to make all data safe .

      Object objectMessage = queueExt.take();
      if ( objectMessage == null ) {
        return result;
      }

      ProviderMessage providerMessage = null;
      if ( objectMessage instanceof ProviderMessage ) {
        providerMessage = (ProviderMessage) objectMessage;
      }
      if ( providerMessage == null ) {
        return result;
      }

      ArrayList listProviderMessages = null;
      if ( processExtToInt != null ) {
        listProviderMessages = processExtToInt
            .processExtToIntMessage( providerMessage );
      }
      if ( listProviderMessages == null ) {
        return result;
      }

      Iterator iterProviderMessages = listProviderMessages.iterator();
      while ( iterProviderMessages.hasNext() ) {
        queueInt.put( iterProviderMessages.next() );
      }

      result = true;

    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to process message , " + e );
    }
    return result;
  }

  private int getBurstSize() {
    int burstSize = 0;
    burstSize = (int) onlinePropertiesApp.getLong(
        "ProviderAgent.BurstSize.".concat( providerId ) , 0 );
    if ( burstSize > 0 ) {
      return burstSize;
    }
    burstSize = (int) onlinePropertiesApp.getLong( "ProviderAgent.BurstSize" ,
        0 );
    if ( burstSize > 0 ) {
      return burstSize;
    }
    burstSize = defBurstSize;
    return burstSize;
  }

  private int getMaxLoad() {
    int maxLoad = 0;
    maxLoad = (int) onlinePropertiesApp.getLong(
        "ProviderAgent.MessageThroughput.".concat( providerId ) , 0 );
    if ( maxLoad > 0 ) {
      return maxLoad;
    }
    maxLoad = (int) onlinePropertiesApp.getLong(
        "ProviderAgent.MessageThroughput" , 0 );
    if ( maxLoad > 0 ) {
      return maxLoad;
    }
    maxLoad = defMaxLoad;
    return maxLoad;
  }

  private int getThreshold() {
    int threshold = 0;
    threshold = (int) onlinePropertiesApp.getLong(
        "ProviderAgent.Threshold.".concat( providerId ) , 0 );
    if ( threshold > 0 ) {
      return threshold;
    }
    threshold = (int) onlinePropertiesApp.getLong( "ProviderAgent.Threshold" ,
        0 );
    if ( threshold > 0 ) {
      return threshold;
    }
    threshold = defThreshold;
    return threshold;
  }

  private int getSleep() {
    int sleep = 0;
    sleep = (int) onlinePropertiesApp.getLong(
        "ProviderAgent.SleepAfterChangeMessageThroughput" , 0 );
    if ( sleep > 0 ) {
      return sleep;
    }
    sleep = defSleep;
    return sleep;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean process() {
    if ( !activeThread ) {
      return false;
    }
    processMessage();
    return true;
  }

  public void run() {
    if ( !initialized ) {
      DLog.warning( lctx , headerLog + "Failed to run thread "
          + ", found not yet initialized" );
      return;
    }
    DLog.debug( lctx , headerLog + "Started thread" );

    int curBurstSize = -1;
    int curMaxLoad = -1;

    activeThread = true;
    while ( activeThread ) {
      try {

        int newBurstSize = getBurstSize();
        int newMaxLoad = getMaxLoad();
        if ( ( newBurstSize != curBurstSize ) || ( newMaxLoad != curMaxLoad ) ) {

          DLog.debug( lctx , headerLog
              + "Updated throttle params : burstSize = " + curBurstSize
              + " -> " + newBurstSize + " pipe(s) , maxLoad = " + curMaxLoad
              + " -> " + newMaxLoad + " msg(s)/sec " );
          curBurstSize = newBurstSize;
          curMaxLoad = newMaxLoad;

          // when found changed inside throttle params , sleep for awhile
          int sleep = getSleep();
          DLog.debug( lctx , headerLog + "Sleep in " + sleep + " ms "
              + ", trying to clean msg(s) from the internal channel agent" );
          Thread.sleep( sleep );

        }

        // wait until the throttle params exists
        if ( ( curBurstSize < 1 ) || ( curMaxLoad < 1 ) ) {
          int sleep = getSleep();
          DLog.debug( lctx , headerLog + "Sleep in " + sleep + " ms "
              + ", found empty throttle params : burstSize = " + curBurstSize
              + " pipe(s) , maxLoad = " + curMaxLoad + " msg(s)/sec , bypass" );
          Thread.sleep( sleep );
          continue;
        }

        // process the messages with throttle mechanism
        int threshold = getThreshold();
        DLog.debug( lctx , headerLog + "Processing the messages with "
            + "throttle mechanism : threshold = " + threshold
            + " msg(s) , burstSize = " + curBurstSize + " pipe(s) , maxLoad = "
            + curMaxLoad + " msg(s)/sec" );
        Throttle throttle = new Throttle( curBurstSize , curMaxLoad );
        throttle.run( threshold , this );

      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to process the messages  , "
            + e );
      }
    } // run while active thread

    DLog.debug( lctx , headerLog + "Stopped thread" );
  }

}
