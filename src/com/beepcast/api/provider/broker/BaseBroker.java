package com.beepcast.api.provider.broker;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public abstract class BaseBroker implements Broker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "BaseBroker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  protected MOWorker moWorker;
  protected DRWorker drWorker;

  private ProviderConf providerConf;
  private ProviderBrokerConf providerBrokerConf;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public BaseBroker( MOWorker moWorker , DRWorker drWorker ,
      ProviderConf providerConf , ProviderBrokerConf providerBrokerConf ) {
    this.moWorker = moWorker;
    this.drWorker = drWorker;
    this.providerConf = providerConf;
    this.providerBrokerConf = providerBrokerConf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String getProviderId() {
    String providerId = null;
    if ( providerBrokerConf != null ) {
      providerId = providerBrokerConf.getProviderId();
    }
    return providerId;
  }

  public String getClassName() {
    String className = null;
    if ( providerBrokerConf != null ) {
      className = providerBrokerConf.getClassName();
    }
    return className;
  }

  public String getMapParamValue( String key ) {
    String value = null;
    if ( providerBrokerConf != null ) {
      value = (String) providerBrokerConf.getMapParams().get( key );
    }
    return value;
  }

}
