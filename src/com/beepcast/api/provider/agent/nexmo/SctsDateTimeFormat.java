package com.beepcast.api.provider.agent.nexmo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SctsDateTimeFormat {

  public static String CanonicalDateTimeFormat = "yyMMddHHmm";

  public static Date convertToDate( String str ) {
    Date date = null;
    if ( str == null ) {
      return date;
    }
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(
          SctsDateTimeFormat.CanonicalDateTimeFormat );
      date = sdf.parse( str );
    } catch ( Exception e ) {
    }
    return date;
  }

  public static String convertToString( Date date ) {
    String str = null;
    if ( date == null ) {
      return str;
    }
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(
          SctsDateTimeFormat.CanonicalDateTimeFormat );
      str = sdf.format( date );
    } catch ( Exception e ) {
    }
    return str;
  }

}
