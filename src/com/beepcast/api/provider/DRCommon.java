package com.beepcast.api.provider;

import java.util.Map;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DRCommon {

  static final DLogContext lctx = new SimpleContext( "DRCommon" );

  public static int getDrProcessPriority( ProviderConf conf ) {
    return getDrProcessPriority( conf , null );
  }

  public static int getDrProcessPriority( ProviderConf conf , String status ) {
    int result = 0;
    if ( conf == null ) {
      DLog.warning( lctx , "Failed to get dr process priority "
          + ", found null provider conf" );
      return result;
    }
    Map map = conf.getDrProcessPriority();
    if ( map == null ) {
      DLog.warning( lctx , "Failed to get dr process priority "
          + ", found null map of dr process priority" );
      return result;
    }
    Integer priority = null;
    if ( status != null ) {
      priority = (Integer) map.get( status );
    }
    if ( priority == null ) {
      priority = (Integer) map
          .get( ProviderConf.DR_PROCESSPRIORITY_DEFAULT_NAME );
    }
    if ( priority == null ) {
      DLog.warning( lctx , "Failed to get dr process priority "
          + ", found null priority" );
      return result;
    }
    result = priority.intValue();
    return result;
  }

}
