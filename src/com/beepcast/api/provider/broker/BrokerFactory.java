package com.beepcast.api.provider.broker;

import java.lang.reflect.Constructor;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderCommon;
import com.beepcast.api.provider.ProviderConf;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class BrokerFactory {

  static final DLogContext lctx = new SimpleContext( "BrokerFactory" );

  public static Broker createBroker( MOWorker moWorker , DRWorker drWorker ,
      ProviderConf providerConf , ProviderBrokerConf providerBrokerConf ) {
    Broker broker = null;

    // validate must be params
    String providerId = providerBrokerConf.getProviderId();
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Can not construct broker object "
          + ", found empty providerId" );
      return broker;
    }

    // compose header log
    String headerLog = ProviderCommon.headerLog( providerId );

    // prepare class name
    String className = providerBrokerConf.getClassName();
    if ( ( className == null ) || ( className.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Can not construct broker object "
          + ", found empty classname" );
      return broker;
    }

    // load the class
    Class agentClass = null;
    try {
      agentClass = Class.forName( className );
    } catch ( ClassNotFoundException e ) {
      DLog.warning( lctx , headerLog + "Can not construct broker object , " + e );
      return broker;
    }

    // load class and construct into object
    try {
      Class[] types = new Class[] { MOWorker.class , DRWorker.class ,
          ProviderConf.class , ProviderBrokerConf.class };
      Object[] args = new Object[] { moWorker , drWorker , providerConf ,
          providerBrokerConf };
      Constructor constructor = agentClass.getDeclaredConstructor( types );
      broker = (Broker) constructor.newInstance( args );
      DLog.debug( lctx , headerLog + "Successfully created broker object" );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to construct broker object , "
          + e );
    }

    return broker;
  }

}
