package com.beepcast.api.provider.broker;

import com.beepcast.api.provider.data.ProviderMessage;

public interface Broker {

  public String getProviderId();

  public String getClassName();

  public boolean processMessage( ProviderMessage providerMessage );

  public boolean processReport( ProviderMessage providerMessage );

}
