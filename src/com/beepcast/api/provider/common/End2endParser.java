package com.beepcast.api.provider.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.util.DateTimeFormat;
import com.beepcast.encrypt.EncryptApp;
import com.beepcast.model.gateway.GatewayLogBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class End2endParser {

  static final DLogContext lctx = new SimpleContext( "End2endParser" );

  public static boolean sychStatusDateTime( ProviderMessage providerMessage ,
      GatewayLogBean gatewayLogBean , Map mapExternalParams ) {
    boolean result = false;
    if ( providerMessage == null ) {
      DLog.warning( lctx , "Failed to synch status date "
          + ", found null provider message" );
      return result;
    }
    if ( gatewayLogBean == null ) {
      DLog.warning( lctx , "Failed to synch status date "
          + ", found null gateway log bean" );
      return result;
    }
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    Date statusDateTimeOld = providerMessage.getStatusDateTime();
    Date statusDateTimeNew = ( statusDateTimeOld == null ) ? new Date()
        : statusDateTimeOld;

    try {
      String stdif = (String) mapExternalParams.get( "tdif" );
      Calendar statusCalendarNew = Calendar.getInstance();
      statusCalendarNew.setTime( statusDateTimeNew );
      statusCalendarNew.add( Calendar.SECOND , -Integer.parseInt( stdif ) );
      statusDateTimeNew = statusCalendarNew.getTime();
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to synch status date time , "
          + e );
    }

    providerMessage.setStatusDateTime( statusDateTimeNew );
    DLog.debug( lctx , headerLog
        + "Synched status date time from dr provider message : "
        + DateTimeFormat.convertToString( statusDateTimeOld ) + " -> "
        + DateTimeFormat.convertToString( statusDateTimeNew ) );

    result = true;
    return result;
  }

  public static Map extractExternalParams( String externalParams ) {
    Map map = new HashMap();

    if ( externalParams == null ) {
      DLog.warning( lctx , "Failed to extract external params "
          + ", found null" );
      return map;
    }

    EncryptApp encryptApp = EncryptApp.getInstance();
    String plainText = encryptApp.base64Decode( externalParams );
    if ( StringUtils.isBlank( plainText ) ) {
      DLog.warning( lctx , "Failed to extract external params "
          + ", found empty plain text" );
      return map;
    }

    try {

      String[] qs = StringUtils.split( plainText , "&" );
      if ( ( qs == null ) || ( qs.length < 1 ) ) {
        DLog.warning( lctx , "Failed to extract external params "
            + ", failed to extract into query string array" );
        return map;
      }

      for ( int i = 0 ; i < qs.length ; i++ ) {
        String[] qsa = StringUtils.split( qs[i] , "=" );
        if ( ( qsa == null ) || ( qsa.length < 1 ) ) {
          continue;
        }
        if ( qsa.length < 2 ) {
          String tf = StringUtils.trimToEmpty( qsa[0] );
          map.put( tf , "" );
          continue;
        }
        if ( qsa.length < 3 ) {
          String tf = StringUtils.trimToEmpty( qsa[0] );
          String tv = StringUtils.trimToEmpty( qsa[1] );
          try {
            tv = URLDecoder.decode( tv , End2endCode.PROVIDER_ENCODING );
          } catch ( UnsupportedEncodingException e ) {
          }
          map.put( tf , tv );
          continue;
        }
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to parse external params , " + e );
    }

    return map;
  }

}
