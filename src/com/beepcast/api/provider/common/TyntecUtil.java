package com.beepcast.api.provider.common;

import org.apache.commons.lang.StringUtils;

public class TyntecUtil {

  public static long latencyInMillis( String submitDate , String doneDate ) {
    long latency = 0;

    if ( StringUtils.isBlank( submitDate ) ) {
      return latency;
    }
    if ( StringUtils.isBlank( doneDate ) ) {
      return latency;
    }

    try {
      long ls = Long.parseLong( submitDate );
      long ld = Long.parseLong( doneDate );
      if ( ld > ls ) {
        latency = ld - ls;
      }
    } catch ( NumberFormatException e ) {
      return latency;
    }

    return latency;
  }

}
