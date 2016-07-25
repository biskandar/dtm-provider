package com.beepcast.api.provider;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ConnectionFactory {

  static final DLogContext lctx = new SimpleContext( "ConnectionFactory" );

  public static Connection createConnection(
      ProviderAgentConf providerAgentConf , ConnectionConf connectionConf ,
      String connectionId ) {
    Connection connection = null;

    // validate must be params

    if ( providerAgentConf == null ) {
      DLog.warning( lctx , "Failed to create connection "
          + ", found null providerAgentConf" );
      return connection;
    }

    if ( connectionConf == null ) {
      DLog.warning( lctx , "Failed to create connection "
          + ", found null connectionConf" );
      return connection;
    }

    // create new connection

    connection = new Connection( connectionConf );
    connection.setId( connectionId );
    if ( providerAgentConf.isEnableErrorCheck() ) {
      connection.shutdown();
    } else {
      connection.startup();
    }

    return connection;
  }

}
