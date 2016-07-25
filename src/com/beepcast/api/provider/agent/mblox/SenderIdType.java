package com.beepcast.api.provider.agent.mblox;

public class SenderIdType {

  public static String parseType( String senderId ) {
    String result = null;
    if ( senderId == null ) {
      return result;
    }

    senderId = senderId.trim();

    boolean isNumeric = false;
    boolean isShortcode = false;
    boolean isAlpha = false;

    int idx = 0 , len = senderId.length();
    if ( senderId.startsWith( "+" ) ) {
      idx = 1;
      isNumeric = true;
    }

    boolean isAllNumber = true;
    for ( ; idx < len ; idx++ ) {
      if ( ( senderId.charAt( idx ) < '0' ) || ( senderId.charAt( idx ) > '9' ) ) {
        isAllNumber = false;
      }
    }

    if ( isNumeric ) {
      isNumeric = isAllNumber;
    } else {
      isShortcode = isAllNumber;
      isAlpha = !isShortcode;
    }

    if ( isNumeric && ( len < 16 ) ) {
      result = "Numeric";
    }
    if ( isShortcode && ( len < 16 ) ) {
      result = "Shortcode";
    }
    if ( isAlpha && ( len < 12 ) ) {
      result = "Alpha";
    }

    return result;
  }

}
