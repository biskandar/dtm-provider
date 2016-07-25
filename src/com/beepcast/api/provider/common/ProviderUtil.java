package com.beepcast.api.provider.common;

import com.beepcast.dbmanager.common.ProviderCommon;
import com.beepcast.dbmanager.table.TProvider;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderUtil {

  static final DLogContext lctx = new SimpleContext( "ProviderUtil" );

  public static TProvider resolveProvider( String headerLog , String providerId ) {
    TProvider providerOut = null;
    if ( providerId == null ) {
      DLog.warning( lctx , headerLog + "Failed to resolve provider "
          + ", found null providerId" );
      return providerOut;
    }

    // read provider based on provider id
    providerOut = ProviderCommon.getProvider( providerId );
    if ( ( providerOut == null ) || ( !providerOut.isActive() ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve provider "
          + ", found empty or inactive providerId = " + providerId );
      return providerOut;
    }

    // read master provider if any
    int masterId = providerOut.getMasterId();
    if ( masterId < 1 ) {
      DLog.debug(
          lctx ,
          headerLog + "Resolved provider : id = " + providerOut.getId()
              + " , providerId = " + providerOut.getProviderId()
              + " , direction = " + providerOut.getDirection() + " , type = "
              + providerOut.getType() + " , description = "
              + providerOut.getDescription() );
      return providerOut;
    }

    // read and validate master provider
    TProvider masterProviderOut = ProviderCommon.getProvider( masterId );
    if ( ( masterProviderOut == null ) || ( !masterProviderOut.isActive() ) ) {
      DLog.debug(
          lctx ,
          headerLog + "Found null or inactive master provider "
              + ", resolved provider : id = " + providerOut.getId()
              + " , providerId = " + providerOut.getProviderId()
              + " , direction = " + providerOut.getDirection() + " , type = "
              + providerOut.getType() + " , description = "
              + providerOut.getDescription() );
      return providerOut;
    }

    // use the master provider instead
    providerOut = masterProviderOut;
    DLog.debug(
        lctx ,
        headerLog + "Resolved master provider : id = " + providerOut.getId()
            + " , providerId = " + providerOut.getProviderId()
            + " , direction = " + providerOut.getDirection() + " , type = "
            + providerOut.getType() + " , description = "
            + providerOut.getDescription() );
    return providerOut;
  }

}
