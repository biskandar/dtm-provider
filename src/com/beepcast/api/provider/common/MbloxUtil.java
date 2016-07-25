package com.beepcast.api.provider.common;

import com.beepcast.api.provider.util.StrUtil;

public class MbloxUtil {

  public static String extractMbloxBinary( String data ) {
    String result = null;
    if ( data == null ) {
      return result;
    }
    int i , n = data.length();
    result = "";
    String hex = "";
    for ( i = 0 ; i < n ; i++ ) {
      if ( i % 2 == 0 ) {
        result = result + hex;
        hex = ":";
      }
      hex = hex + data.charAt( i );
    }
    if ( i % 2 == 0 ) {
      result = result + hex;
      hex = ":";
    }
    return result;
  }

  public static String encodeUnicodeStr( String strIn ) {
    String strOu = null;
    if ( strIn == null ) {
      return strOu;
    }
    try {
      StringBuffer sbStr = new StringBuffer();
      String strHex = StrUtil.convert2HexString( strIn , "," );
      if ( strHex != null ) {
        String[] arrHex = strHex.split( "," );
        if ( arrHex != null ) {
          for ( int i = 0 ; i < arrHex.length ; i++ ) {
            if ( arrHex[i] != null ) {
              sbStr.append( "&#x" );
              sbStr.append( arrHex[i] );
              sbStr.append( ";" );
            }
          }
        }
      }
      strOu = sbStr.toString();
    } catch ( Exception e ) {
    }
    return strOu;
  }

}
