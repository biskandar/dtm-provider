package com.beepcast.api.provider.broker;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.model.mobileUser.MobileUserService;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class GenericBroker extends BaseBroker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "GenericBroker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private MobileUserService mobileUserService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public GenericBroker( MOWorker moWorker , DRWorker drWorker ,
      ProviderConf providerConf , ProviderBrokerConf providerBrokerConf ) {
    super( moWorker , drWorker , providerConf , providerBrokerConf );
    mobileUserService = new MobileUserService();
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
    if ( providerMessage == null ) {
      return result;
    }

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // nothing to do yet

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
