package com.beepcast.api.provider.util;

import com.beepcast.api.provider.common.MbloxUtil;

public class XmlUtil {

  public static String encode( String strIn ) {
    return encode( strIn , false );
  }

  public static String encode( String strIn , boolean unicode ) {
    String strOu = null;
    if ( strIn == null ) {
      return strOu;
    }
    strOu = strIn;
    strOu = strOu.replaceAll( "&" , "&amp;" );
    strOu = strOu.replaceAll( "<" , "&lt;" );
    strOu = strOu.replaceAll( ">" , "&gt;" );
    strOu = strOu.replaceAll( "'" , "&apos;" );
    strOu = strOu.replaceAll( "\"" , "&quot;" );
    if ( unicode ) {
      strOu = MbloxUtil.encodeUnicodeStr( strOu );
    }
    return strOu;
  }

}
