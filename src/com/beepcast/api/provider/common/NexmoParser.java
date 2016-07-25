package com.beepcast.api.provider.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.encrypt.EncryptApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class NexmoParser {

  static final DLogContext lctx = new SimpleContext( "NexmoParser" );

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
            tv = URLDecoder.decode( tv , NexmoCode.PROVIDER_ENCODING );
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
