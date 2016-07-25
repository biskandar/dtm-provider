package com.beepcast.api.provider.util;

import com.firsthop.common.util.convert.Base64;

public class StrUtil {

  public static String convert2HexString( String data , String delimiter ) {
    String result = null;
    if ( data == null ) {
      return result;
    }
    int i , n = data.length();
    StringBuffer sb = null;
    for ( i = 0 ; i < n ; i++ ) {
      String hexStr = Integer.toHexString( data.charAt( i ) );
      if ( hexStr == null ) {
        continue;
      }
      int hexLen = hexStr.length();
      if ( sb == null ) {
        sb = new StringBuffer();
      } else {
        sb.append( delimiter );
      }
      if ( hexLen == 1 ) {
        sb.append( "000" );
      }
      if ( hexLen == 2 ) {
        sb.append( "00" );
      }
      if ( hexLen == 3 ) {
        sb.append( "0" );
      }
      sb.append( hexStr );
    }
    if ( sb != null ) {
      result = sb.toString();
    }
    return result;
  }

  public static String convert2Base64( String data ) {
    String result = null;
    if ( data == null ) {
      return result;
    }
    try {
      byte[] ibytes = data.getBytes();
      byte[] obytes = Base64.encode( ibytes );
      result = new String( obytes );
    } catch ( Exception e ) {
    }
    return result;
  }

  public static String substituteSymbol( String text , String symbol ,
      String value ) {
    StringBuffer buffer;
    while ( text.indexOf( symbol ) >= 0 ) {
      buffer = new StringBuffer( text );
      buffer.replace( text.indexOf( symbol ) ,
          text.indexOf( symbol ) + symbol.length() , value );
      text = buffer.toString();
    }
    return text;
  }

}
