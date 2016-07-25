package com.beepcast.api.provider.broker;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.agent.mblox.NotificationService;
import com.beepcast.api.provider.common.MbloxParser;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.lookup.LookupApp;
import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.mobileUser.MobileUserService;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MbloxBroker extends BaseBroker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MbloxBroker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;
  private LookupApp lookupApp;
  private MobileUserService mobileUserService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MbloxBroker( MOWorker moWorker , DRWorker drWorker ,
      ProviderConf providerConf , ProviderBrokerConf providerBrokerConf ) {
    super( moWorker , drWorker , providerConf , providerBrokerConf );
    opropsApp = OnlinePropertiesApp.getInstance();
    lookupApp = LookupApp.getInstance();
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

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // extract map external params
    NotificationService notfService = (NotificationService) MbloxParser
        .extractExternalParams( providerMessage.getExternalParams() );
    if ( notfService == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found null notification service" );
      return result;
    }

    // read gateway log bean from optional params
    GatewayLogBean gatewayLogBean = (GatewayLogBean) providerMessage
        .getOptionalParam( "gatewayLogBean" );
    if ( gatewayLogBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found null gateway log bean" );
      return result;
    }

    // read event id
    int eventId = (int) gatewayLogBean.getEventID();
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found zero event id" );
      return result;
    }

    // read event profile
    TEvent eventOut = EventCommon.getEvent( eventId );
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found invalid event id = " + eventId );
      return result;
    }

    // resolve client id
    int clientId = eventOut.getClientId();
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found zero client id" );
      return result;
    }

    // resolve phone number
    String phoneNumber = gatewayLogBean.getPhone();
    if ( StringUtils.isBlank( phoneNumber ) ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found blank phone number" );
      return result;
    }

    // update mobile cc and nc if found any
    DLog.debug( lctx , headerLog + "Resolve and update mobile ccnc on "
        + "the mobile user table" );
    if ( !updateMobileCcnc( headerLog , clientId , phoneNumber , notfService ) ) {

      // found failed to update mobile cc and nc , will do the lookup
      // based on lookup provider

      DLog.debug( lctx , headerLog + "Failed to resolve mobile ccnc from "
          + "provider tag operator , will call lookup subscriber "
          + ": sendProvider = " + providerMessage.getProviderId()
          + " , clientId = " + clientId + " , eventId = " + eventId
          + " , phoneNumber = " + phoneNumber );

      if ( !drWorker.lookupSubscriber( providerMessage.getInternalMessageId() ,
          clientId , eventId , phoneNumber , providerMessage.getProviderId() ) ) {
        DLog.warning( lctx , headerLog + "Failed to resolve mobile ccnc "
            + ", found failed to lookup subscriber" );
      }

    }

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean updateMobileCcnc( String headerLog , int clientId ,
      String phoneNumber , NotificationService notfService ) {
    boolean result = false;

    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile ccnc "
          + ", found zero client id" );
      return result;
    }

    if ( StringUtils.isBlank( phoneNumber ) ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile ccnc "
          + ", found blank phone number" );
      return result;
    }

    String mobileCcnc = notfService.getMapTagOperator();
    if ( StringUtils.isBlank( mobileCcnc ) ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile ccnc "
          + ", found blank tag operator mblox value" );
      return result;
    }

    DLog.debug( lctx , headerLog + "Updating mobile user : clientId = "
        + clientId + " , phoneNumber = " + phoneNumber + " , mobileCcnc = "
        + mobileCcnc );

    result = mobileUserService.updateMobileCcnc( clientId , phoneNumber ,
        mobileCcnc );

    return result;
  }

}
