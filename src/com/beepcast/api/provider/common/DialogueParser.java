package com.beepcast.api.provider.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DialogueParser {

  private static final String XE3TIMESTAMP_DTMFMT = "yyyy-MM-dd HH:mm:ss";

  public static Date parseXe3TimeStamp( String xe3TimeStamp ) {
    Date date = null;
    if ( xe3TimeStamp == null ) {
      return date;
    }
    {
      String[] arr = xe3TimeStamp.split( "\\." );
      if ( ( arr != null ) && ( arr.length > 1 ) ) {
        xe3TimeStamp = arr[0];
      }
    }
    try {
      SimpleDateFormat sdf = new SimpleDateFormat( XE3TIMESTAMP_DTMFMT );
      date = sdf.parse( xe3TimeStamp );
    } catch ( Exception e ) {
    }
    return date;
  }

  public static Map parseResponseTags( String strTags ) {
    Map mapTags = null;
    if ( strTags == null ) {
      return mapTags;
    }
    mapTags = new HashMap();
    String[] arrTags = strTags.split( "(X-E3-)" );
    // System.out.println( Arrays.asList( arrTags ) );
    for ( int idx = 0 ; idx < arrTags.length ; idx++ ) {
      String strTag = arrTags[idx];
      if ( ( strTag == null ) || ( strTag.equals( "" ) ) ) {
        continue;
      }
      String[] arrTag = strTag.split( ":" , 2 );
      if ( ( arrTag == null ) || ( arrTag.length < 2 ) ) {
        continue;
      }
      String fieldTag = "X-E3-".concat( arrTag[0] );
      String valueTag = arrTag[1].replace( '"' , ' ' ).trim();
      // System.out.println( fieldTag + " : " + valueTag );
      mapTags.put( fieldTag , valueTag );
    }
    return mapTags;
  }

}
