package com.beepcast.api.provider.agent;

import java.util.ArrayList;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;

public interface ProcessExtToInt {

  public BoundedLinkedQueue getQueueExt();

  public BoundedLinkedQueue getQueueInt();

  public ArrayList processExtToIntMessage( ProviderMessage providerMessage );

}
