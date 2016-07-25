package com.beepcast.api.provider.common;

import java.net.URLEncoder;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class End2endGsm7BitEncode {

  static final DLogContext lctx = new SimpleContext( "End2endGsm7BitEncode" );

  public static String convertToQueryString( String st ) {
    String qs = null;
    try {
      qs = URLEncoder.encode( st , "UTF-16" );
      qs = qs.replaceAll( "%FE%FF%00" , "" );
      qs = qs.replaceAll( "%00" , "" );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to convert to query string , " + e );
    }
    return qs;
  }

}
