package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.beepcast.api.provider.data.ProviderMessage;

public interface ProviderApi {

  public boolean processMessage( String providerId , String externalMessageId ,
      String originAddress , String destinationAddress , String message ,
      Date dateDelivery , Map optionalParams , String externalParams );

  public boolean sendMessage( ProviderMessage providerMessage );

  public boolean processReport( String providerId , String externalMessageId ,
      String externalStatus , Date dateReport , Map optionalParams ,
      String externalParams );

  public boolean processReportEachBroker( ProviderMessage providerMessage );

  public ArrayList listOutgoingProviderIds();

  public ArrayList listOutgoingProviderIds( boolean filterByWorkers ,
      boolean addMembers , boolean filterBySuspend );

  public ArrayList listOutgoingProviderIds( List listFilterByTypes ,
      boolean filterByWorkers , boolean addMembers , boolean filterBySuspend );

  public ArrayList listIncomingProviderIds();

}
