package com.beepcast.api.provider.util;

import com.beepcast.api.provider.data.ProviderMessage;

public class ProviderMessageOptionalMapParamUtils {

  // -----

  public static final String HDR_INTERNAL_MESSAGE = "internalMessage";
  public static final String HDR_COMMAND_SUBMITTED = "commandSubmitted";
  public static final String HDR_COMMAND_DELIVERED = "commandDelivered";
  public static final String HDR_COMMAND_RETRY = "commandRetry";
  public static final String HDR_COMMAND_SHUTDOWN = "commandShutdown";
  public static final String HDR_COMMAND_IGNORED = "commandIgnored";
  public static final String HDR_SYNCH_RETRY = "synchRetry";

  // -----

  public static boolean isInternalMessage( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage
        .getOptionalParam( HDR_INTERNAL_MESSAGE ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_INTERNAL_MESSAGE );
    }
    return returnValue;
  }

  public static boolean isCommandSubmitted( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage
        .getOptionalParam( HDR_COMMAND_SUBMITTED ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_COMMAND_SUBMITTED );
    }
    return returnValue;
  }

  public static boolean isCommandDelivered( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage
        .getOptionalParam( HDR_COMMAND_DELIVERED ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_COMMAND_DELIVERED );
    }
    return returnValue;
  }

  public static boolean isCommandRetry( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage.getOptionalParam( HDR_COMMAND_RETRY ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_COMMAND_RETRY );
    }
    return returnValue;
  }

  public static boolean isCommandShutdown( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage
        .getOptionalParam( HDR_COMMAND_SHUTDOWN ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_COMMAND_SHUTDOWN );
    }
    return returnValue;
  }

  public static boolean isCommandIgnored( ProviderMessage providerMessage ,
      boolean cleanAfterRead ) {
    boolean returnValue = providerMessage
        .getOptionalParam( HDR_COMMAND_IGNORED ) != null;
    if ( returnValue && cleanAfterRead ) {
      providerMessage.getOptionalParams().remove( HDR_COMMAND_IGNORED );
    }
    return returnValue;
  }

  public static int getSynchRetry( ProviderMessage providerMessage ,
      int defaultSynchRetry ) {
    int synchRetry = defaultSynchRetry;
    try {
      synchRetry = Integer.parseInt( (String) providerMessage
          .getOptionalParam( HDR_SYNCH_RETRY ) );
    } catch ( Exception e ) {
    }
    return synchRetry;
  }

  // -----

  public static void setInternalMessage( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_INTERNAL_MESSAGE , "true" );
  }

  public static void setCommandSubmitted( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_COMMAND_SUBMITTED , "true" );
  }

  public static void setCommandDelivered( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_COMMAND_DELIVERED , "true" );
  }

  public static void setCommandRetry( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_COMMAND_RETRY , "true" );
  }

  public static void setCommandShutdown( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_COMMAND_SHUTDOWN , "true" );
  }

  public static void setCommandIgnored( ProviderMessage providerMessage ) {
    providerMessage.addOptionalParam( HDR_COMMAND_IGNORED , "true" );
  }

  public static void setSynchRetry( ProviderMessage providerMessage ,
      int synchRetry ) {
    providerMessage.addOptionalParam( HDR_SYNCH_RETRY ,
        Integer.toString( synchRetry ) );
  }

}
