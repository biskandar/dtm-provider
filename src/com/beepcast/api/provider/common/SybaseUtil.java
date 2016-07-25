package com.beepcast.api.provider.common;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SybaseUtil {

  public static final String DCS_7BIT = "7b";
  public static final String DCS_8BIT = "8b";
  public static final String DCS_UCS2 = "UCS2";

  static final DLogContext lctx = new SimpleContext( "SybaseUtil" );

  public static String decodeIncomingMessage( String dcs , String messageContent ) {
    String result = null;
    if ( messageContent == null ) {
      DLog.warning( lctx , "Failed to decode incoming message "
          + ", found null content" );
      return result;
    }
    if ( StringUtils.equals( dcs , DCS_7BIT ) ) {
      // nothing to do just keep as original
      result = messageContent;
      return result;
    }
    if ( StringUtils.equals( dcs , DCS_UCS2 ) ) {
      result = decodeIncomingMessageUCS2( messageContent );
      return result;
    }
    DLog.debug( lctx , "Failed to decode incoming message "
        + ", found anonymous dcs = " + dcs + ", keep as original content" );
    result = messageContent;
    return result;
  }

  public static String decodeIncomingMessageUCS2( String messageContent ) {
    String result = null;
    try {
      StringBuffer sb = new StringBuffer();
      int len = messageContent.length() / 4;
      for ( int idx = 0 ; idx < len ; idx++ ) {
        String strChar = messageContent.substring( idx * 4 , ( idx + 1 ) * 4 );
        int intChar = Integer.parseInt( strChar , 16 );
        sb.append( (char) intChar );
      }
      result = sb.toString();
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to decode incoming message ucs2 , " + e );
    }
    return result;
  }

}
