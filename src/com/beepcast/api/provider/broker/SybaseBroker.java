package com.beepcast.api.provider.broker;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.data.ProviderMessage;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SybaseBroker extends BaseBroker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SybaseBroker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public SybaseBroker( MOWorker moWorker , DRWorker drWorker ,
      ProviderConf providerConf , ProviderBrokerConf providerBrokerConf ) {
    super( moWorker , drWorker , providerConf , providerBrokerConf );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean processMessage( ProviderMessage providerMessage ) {
    boolean result = false;

    result = true;
    return result;
  }

  public boolean processReport( ProviderMessage providerMessage ) {
    boolean result = false;

    result = true;
    return result;
  }

}
