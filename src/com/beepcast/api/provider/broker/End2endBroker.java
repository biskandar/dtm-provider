package com.beepcast.api.provider.broker;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.End2endParser;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.mobileUser.MobileUserService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class End2endBroker extends BaseBroker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "End2endBroker" );

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

  public End2endBroker( MOWorker moWorker , DRWorker drWorker ,
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

    // extract map external params

    Map mapExternalParams = End2endParser
        .extractExternalParams( providerMessage.getExternalParams() );
    if ( mapExternalParams == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found null map external params" );
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

    // synchronized date status time

    DLog.debug( lctx , headerLog + "Synchronized status date time based "
        + "on tdiff and gateway log record" );
    End2endParser.sychStatusDateTime( providerMessage , gatewayLogBean ,
        mapExternalParams );

    // update mobile cc and nc if any

    DLog.debug( lctx , headerLog + "Updated mobile ccnc on the mobile "
        + "user table" );
    updateMobileCcnc( providerMessage , gatewayLogBean , mapExternalParams );

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean updateMobileCcnc( ProviderMessage providerMessage ,
      GatewayLogBean gatewayLogBean , Map mapExternalParams ) {
    boolean result = false;

    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    TEvent eventOut = EventCommon.getEvent( (int) gatewayLogBean.getEventID() );
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile cc and nc "
          + ", found invalid event id = " + gatewayLogBean.getEventID() );
      return result;
    }

    String phoneNumber = gatewayLogBean.getPhone();
    if ( StringUtils.isBlank( phoneNumber ) ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile cc and nc "
          + ", found blank phone number" );
      return result;
    }

    String mcc = (String) mapExternalParams.get( "mcc" );
    mcc = ( mcc == null ) ? "" : mcc;

    String mnc = (String) mapExternalParams.get( "mnc" );
    mnc = ( mnc == null ) ? "" : mnc;

    String mobileCcnc = mcc.concat( mnc );
    if ( mobileCcnc.equals( "" ) ) {
      // nothing to do , just bypass
      result = true;
      return result;
    }

    DLog.debug( lctx , headerLog + "Updating mobile user : clientId = "
        + eventOut.getClientId() + " , phoneNumber = " + phoneNumber
        + " , mobileCcnc = " + mobileCcnc );
    result = mobileUserService.updateMobileCcnc( eventOut.getClientId() ,
        phoneNumber , mobileCcnc );
    return result;
  }

}