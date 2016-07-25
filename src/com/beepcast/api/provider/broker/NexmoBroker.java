package com.beepcast.api.provider.broker;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.DRWorker;
import com.beepcast.api.provider.MOWorker;
import com.beepcast.api.provider.ProviderBrokerConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.common.NexmoParser;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.DateTimeFormat;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.mobileUser.MobileUserService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class NexmoBroker extends BaseBroker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "NexmoBroker" );

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

  public NexmoBroker( MOWorker moWorker , DRWorker drWorker ,
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

    // nothing to do yet

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

    Map mapExternalParams = NexmoParser.extractExternalParams( providerMessage
        .getExternalParams() );
    if ( mapExternalParams == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found null map external params" );
    }

    // read gateway log bean from optional params

    GatewayLogBean gatewayLogBean = (GatewayLogBean) providerMessage
        .getOptionalParam( "gatewayLogBean" );
    if ( gatewayLogBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + ", found null gateway log bean" );
    }

    // update mobile country code and network code

    updateMobileCcNc( headerLog , gatewayLogBean , mapExternalParams );

    // update status date time

    updateStatusDateTime( headerLog , gatewayLogBean , providerMessage );

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean updateMobileCcNc( String headerLog ,
      GatewayLogBean gatewayLogBean , Map mapExternalParams ) {
    boolean result = false;

    if ( gatewayLogBean == null ) {
      return result;
    }
    if ( mapExternalParams == null ) {
      return result;
    }

    String mobileCcnc = (String) mapExternalParams.get( "network-code" );
    if ( StringUtils.isBlank( mobileCcnc ) ) {
      return result;
    }

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

    DLog.debug( lctx , headerLog + "Updating mobile user : clientId = "
        + eventOut.getClientId() + " , phoneNumber = " + phoneNumber
        + " , mobileCcnc = " + mobileCcnc );
    result = mobileUserService.updateMobileCcnc( eventOut.getClientId() ,
        phoneNumber , mobileCcnc );

    return result;
  }

  private boolean updateStatusDateTime( String headerLog ,
      GatewayLogBean gatewayLogBean , ProviderMessage providerMessage ) {
    boolean result = false;

    if ( providerMessage == null ) {
      return result;
    }

    // read gmt parameter
    int gmtInt = 0;
    try {
      gmtInt = Integer.parseInt( getMapParamValue( "gmt" ) );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to resolve gmt , " + e );
    }

    // read the current status date time
    Date statusDateTimeCur = providerMessage.getStatusDateTime();

    // setup status default as now
    Calendar calStatus = Calendar.getInstance();

    // update current status date time to calendar
    if ( statusDateTimeCur != null ) {
      calStatus.setTime( statusDateTimeCur );

      // convert gmt from the current received
      if ( gmtInt != 0 ) {
        calStatus.add( Calendar.HOUR , gmtInt );
      }

    }

    // add more one minute if found the submit and status was same or less
    Date submitDateTime = null;
    if ( gatewayLogBean != null ) {
      submitDateTime = gatewayLogBean.getDateTm();
    }
    if ( submitDateTime != null ) {
      if ( submitDateTime.getTime() >= calStatus.getTime().getTime() ) {
        calStatus.add( Calendar.MINUTE , 1 );
      }
    }

    // update the new status date time
    providerMessage.setStatusDateTime( calStatus.getTime() );
    DLog.debug( lctx , headerLog
        + "Updated status date time from dr provider message : "
        + DateTimeFormat.convertToString( statusDateTimeCur ) + " -> "
        + DateTimeFormat.convertToString( providerMessage.getStatusDateTime() ) );

    result = true;
    return result;
  }

}
