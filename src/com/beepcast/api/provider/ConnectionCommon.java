package com.beepcast.api.provider;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ConnectionCommon {

  static final DLogContext lctx = new SimpleContext( "ConnectionCommon" );

  public static String headerLog( Connection connection ) {
    String headerLog = "";
    if ( connection == null ) {
      return headerLog;
    }
    return headerLog( connection.getId() );
  }

  public static String headerLog( String connectionId ) {
    String headerLog = "";
    if ( connectionId == null ) {
      return headerLog;
    }
    headerLog = "[Connection-" + connectionId + "] ";
    return headerLog;
  }

}
