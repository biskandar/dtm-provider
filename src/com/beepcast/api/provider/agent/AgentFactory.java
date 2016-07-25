package com.beepcast.api.provider.agent;

import java.lang.reflect.Constructor;

import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderCommon;
import com.beepcast.api.provider.ProviderConf;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class AgentFactory {

  static final DLogContext lctx = new SimpleContext( "AgentFactory" );

  public static Agent createAgent( ProviderConf providerConf ,
      ProviderAgentConf providerAgentConf , MTSendWorker mtSendWorker ,
      MTRespWorker mtRespWorker ) {
    Agent agent = null;
    if ( providerAgentConf == null ) {
      return agent;
    }

    // validate must be params
    String providerId = providerAgentConf.getProviderId();
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Can not construct agent worker object "
          + ", found empty providerId " );
      return agent;
    }

    // compose header log
    String headerLog = ProviderCommon.headerLog( providerId );

    // prepare class name
    String className = providerAgentConf.getClassName();
    if ( ( className == null ) || ( className.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Can not construct agent worker object "
          + ", found empty classname " );
      return agent;
    }

    // load the class
    Class agentClass = null;
    try {
      agentClass = Class.forName( className );
    } catch ( ClassNotFoundException e ) {
      DLog.warning( lctx , headerLog
          + "Can not construct agent worker object , " + e );
      return agent;
    }

    // load class and construct into object
    try {

      Class[] types = new Class[] { ProviderConf.class , MTRespWorker.class ,
          MTSendWorker.class , String.class , ProviderAgentConf.class };

      Object[] args = new Object[] { providerConf , mtRespWorker ,
          mtSendWorker , providerId , providerAgentConf };

      Constructor constructor = agentClass.getDeclaredConstructor( types );

      agent = (Agent) constructor.newInstance( args );

      DLog.debug( lctx , headerLog + "Successfully created "
          + "mt agent worker object" );

    } catch ( Exception e ) {

      DLog.warning( lctx , headerLog
          + "Failed to construct agent worker object , " + e );

    }

    return agent;
  }

}
