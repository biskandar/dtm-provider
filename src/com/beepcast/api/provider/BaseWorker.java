package com.beepcast.api.provider;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public abstract class BaseWorker implements Worker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "BaseWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private final String headerLog;
  private final int minThreads;
  private final int maxThreads;

  private Object lckThreads;
  private int curThreads;
  protected boolean[] arrThreads;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public BaseWorker( String headerLog , int minThreads , int maxThreads ) {
    this.headerLog = headerLog + " ";
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
    init();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isThreadActive( int threadIdx ) {
    boolean result = false;
    synchronized ( lckThreads ) {
      if ( threadIdx < minThreads ) {
        return result;
      }
      if ( threadIdx >= curThreads ) {
        return result;
      }
      result = arrThreads[threadIdx];
    }
    return result;
  }

  public boolean setupThreads() {
    return setupThreads( threadSize() );
  }

  public boolean resetThreads() {
    return setupThreads( 0 );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void init() {
    lckThreads = new Object();
    synchronized ( lckThreads ) {
      curThreads = 0;
      arrThreads = new boolean[maxThreads];
      for ( int idx = 0 ; idx < arrThreads.length ; idx++ ) {
        arrThreads[idx] = false;
      }
      DLog.debug( lctx , headerLog + "Prepared worker : minThreads = "
          + minThreads + " , maxThreads = " + maxThreads );
    }
  }

  private boolean setupThreads( int threadSize ) {
    boolean result = false;
    synchronized ( lckThreads ) {
      if ( threadSize < minThreads ) {
        threadSize = minThreads;
      }
      if ( threadSize > arrThreads.length ) {
        threadSize = arrThreads.length;
      }
      if ( threadSize == curThreads ) {
        // nothing to do ...
      }
      if ( threadSize > curThreads ) {
        for ( int threadIdx = curThreads ; threadIdx < threadSize ; threadIdx++ ) {
          arrThreads[threadIdx] = true;
          Thread thread = createThread( threadIdx );
          thread.start();
        }
        DLog.debug( lctx , headerLog + "Updated worker threads size : "
            + curThreads + " -> " + threadSize );
        curThreads = threadSize;
      }
      if ( threadSize < curThreads ) {
        for ( int threadIdx = threadSize ; threadIdx < curThreads ; threadIdx++ ) {
          arrThreads[threadIdx] = false;
        }
        DLog.debug( lctx , headerLog + "Updated worker threads size : "
            + curThreads + " -> " + threadSize );
        curThreads = threadSize;
      }
    }
    result = true;
    return result;
  }

}
