package com.beepcast.api.provider.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFormat {

  public static String CanonicalDateTimeFormat = "yyyy-MM-dd HH:mm:ss";

  public static Date convertToDate( String data ) {
    Date date = null;
    if ( data != null ) {
      SimpleDateFormat sdf = new SimpleDateFormat(
          DateTimeFormat.CanonicalDateTimeFormat );
      try {
        date = sdf.parse( data );
      } catch ( Exception e ) {
      }
    }
    return date;
  }

  public static String convertToString( Date date ) {
    String data = null;
    if ( date != null ) {
      SimpleDateFormat sdf = new SimpleDateFormat(
          DateTimeFormat.CanonicalDateTimeFormat );
      try {
        data = sdf.format( date );
      } catch ( Exception e ) {

      }
    }
    return data;
  }

}
