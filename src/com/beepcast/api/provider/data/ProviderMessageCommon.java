package com.beepcast.api.provider.data;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderMessageCommon {

  static final DLogContext lctx = new SimpleContext( "ProviderMessageCommon" );

  public static String headerLog( ProviderMessage providerMessage ) {
    String headerLog = "";
    if ( providerMessage == null ) {
      return headerLog;
    }
    return headerLog( providerMessage.getInternalMessageId() );
  }

  public static String headerLog( String messageId ) {
    String headerLog = "";
    if ( ( messageId == null ) || ( messageId.equals( "" ) ) ) {
      return headerLog;
    }
    headerLog = "[ProviderMessage-" + messageId + "] ";
    return headerLog;
  }

  public static String getContentTypeInfo( int contentType ) {
    return ContentType.toString( contentType );
  }

}
