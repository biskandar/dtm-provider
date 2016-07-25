package com.beepcast.api.provider.util;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class QueueUtil {

  static final DLogContext lctx = new SimpleContext( "QueueUtil" );

  public static Double getSizePercentage( BoundedLinkedQueue queue ) {
    Double szp = null;
    if ( queue == null ) {
      DLog.warning( lctx , "Failed get queue size percentage "
          + ", found null queue" );
      return szp;
    }
    double capacity = queue.capacity();
    if ( capacity < 1 ) {
      DLog.warning( lctx , "Failed get queue size percentage "
          + ", found zero queue capacity" );
      return szp;
    }
    double size = queue.size();
    szp = new Double( size * 100.0 / capacity );
    return szp;
  }

}
