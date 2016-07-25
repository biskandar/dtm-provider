package com.beepcast.api.provider.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SybaseParser {

  static final DLogContext lctx = new SimpleContext( "SybaseParser" );

  public static String getExternalStatus( String status ) {
    String externalStatus = null;

    // validate status
    if ( StringUtils.isBlank( status ) ) {
      return externalStatus;
    }

    // clean status
    status = StringUtils.trim( status );

    // find for submitted status
    if ( StringUtils.containsIgnoreCase( status ,
        "has been correctly processed" ) ) {
      externalStatus = SybaseCode.STATUS_SYBASE_ACK;
      return externalStatus;
    }
    if ( StringUtils.containsIgnoreCase( status , "has been sent" ) ) {
      externalStatus = SybaseCode.STATUS_SMSC_ACK;
      return externalStatus;
    }

    // find for delivered status
    if ( StringUtils.containsIgnoreCase( status , "has been received" ) ) {
      externalStatus = SybaseCode.STATUS_DELIVERED;
      return externalStatus;
    }

    // find for filtered number
    if ( StringUtils.containsIgnoreCase( status , "has been filtered" ) ) {
      externalStatus = SybaseCode.STATUS_NUMBER_FILTERED;
      return externalStatus;
    }

    // find for failed status , in the first word
    int idx = StringUtils.indexOf( status , ' ' );
    if ( idx > 0 ) {
      externalStatus = StringUtils.left( status , idx );
    }
    if ( StringUtils.startsWith( externalStatus , "0x" ) ) {
      externalStatus = StringUtils.substring( externalStatus , 2 );
    }

    return externalStatus;
  }

  public static Date getStatusDate( String paramDate , String paramTime ,
      Date defaultDate ) {
    Date date = defaultDate;

    if ( StringUtils.isBlank( paramDate ) ) {
      DLog.warning( lctx , "Failed to get status date "
          + ", found blank string param date" );
      return date;
    }

    if ( StringUtils.isBlank( paramTime ) ) {
      DLog.warning( lctx , "Failed to get status date "
          + ", found blank string param time" );
      return date;
    }

    try {
      // compose param date time
      String paramDateTime = paramDate + " " + paramTime;
      // param date time format : dd-MM-yyyy HH:mm:ss
      DateFormat dfCET = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" );
      dfCET.setTimeZone( TimeZone.getTimeZone( SybaseCode.PROVIDER_TIME_ZONE ) );
      date = dfCET.parse( paramDateTime );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to parse param date time , " + e );
    }

    return date;
  }

}
