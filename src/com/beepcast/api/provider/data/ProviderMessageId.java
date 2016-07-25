package com.beepcast.api.provider.data;

import com.beepcast.idgen.IdGenApp;

public class ProviderMessageId {

  private static IdGenApp idGenApp = IdGenApp.getInstance();

  public static String newMessageId( String prefix ) {
    prefix = ( prefix == null ) ? "" : prefix;
    return prefix.concat( idGenApp.nextIdentifier() );

  }

}
