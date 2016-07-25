package com.beepcast.api.provider.agent.generic;

import java.util.HashMap;
import java.util.Map;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.dbmanager.table.TProvider;

public class GenericVariables {

  public static final String PROVIDER_ACCESSURL = "PROVIDER_ACCESSURL";
  public static final String PROVIDER_ACCESSUSERNAME = "PROVIDER_ACCESSUSERNAME";
  public static final String PROVIDER_ACCESSPASSWORD = "PROVIDER_ACCESSPASSWORD";
  public static final String PROVIDER_LISTENERURL = "PROVIDER_LISTENERURL";

  public static final String MESSAGE_ID = "MESSAGE_ID";
  public static final String MESSAGE_DESTINATIONADDRESS = "MESSAGE_DESTINATIONADDRESS";
  public static final String MESSAGE_DESTINATIONADDRESS_NOPLUS = "MESSAGE_DESTINATIONADDRESS_NOPLUS";
  public static final String MESSAGE_ORIGINALADDRESSMASK = "MESSAGE_ORIGINALADDRESSMASK";
  public static final String MESSAGE_ORIGINALADDRESSMASK_NOPLUS = "MESSAGE_ORIGINALADDRESSMASK_NOPLUS";
  public static final String MESSAGE_CONTENT = "MESSAGE_CONTENT";

  public static Map setupMapVariables( TProvider provider ,
      ProviderMessage message , Map confScriptParams , Map confMessageParams ) {
    Map map = new HashMap();

    String str = null;

    if ( provider != null ) {
      map.put( PROVIDER_ACCESSURL , provider.getAccessUrl() );
      map.put( PROVIDER_ACCESSUSERNAME , provider.getAccessUsername() );
      map.put( PROVIDER_ACCESSPASSWORD , provider.getAccessPassword() );
      map.put( PROVIDER_LISTENERURL , provider.getListenerUrl() );
    }

    if ( message != null ) {

      map.put( MESSAGE_ID , message.getExternalMessageId() );

      str = message.getDestinationAddress();
      map.put( MESSAGE_DESTINATIONADDRESS , str );
      if ( ( str != null ) && ( str.startsWith( "+" ) ) ) {
        str = str.substring( 1 );
      }
      map.put( MESSAGE_DESTINATIONADDRESS_NOPLUS , str );

      str = message.getOriginAddrMask();
      map.put( MESSAGE_ORIGINALADDRESSMASK , str );
      if ( ( str != null ) && ( str.startsWith( "+" ) ) ) {
        str = str.substring( 1 );
      }
      map.put( MESSAGE_ORIGINALADDRESSMASK_NOPLUS , str );

      map.put( MESSAGE_CONTENT , message.getMessage() );

    }

    if ( confScriptParams != null ) {
      // nothing to do yet
    }

    if ( confMessageParams != null ) {
      // nothing to do yet
    }

    return map;
  }

}
