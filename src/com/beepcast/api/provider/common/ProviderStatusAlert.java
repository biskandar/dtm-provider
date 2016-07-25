package com.beepcast.api.provider.common;

import java.util.Date;

import com.beepcast.api.provider.Connection;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.onm.OnmApp;
import com.beepcast.onm.alert.AlertMessageFactory;
import com.beepcast.onm.data.AlertMessage;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderStatusAlert {

  static final DLogContext lctx = new SimpleContext( "ProviderStatusAlert" );

  private static OnmApp onmApp = OnmApp.getInstance();

  public static void sendAlertProviderStatusSuspended( String providerId ,
      Connection conn , String reason ) {
    String subject = "Connection to provider [" + providerId + "] is suspended";
    String headerMessage = "Found provider [" + providerId
        + "] received suspended flag , reason : " + reason;
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , providerId ,
            null , new Boolean( false ) , conn ) );
  }

  public static void sendAlertProviderStatusActive( String providerId ,
      Connection conn ) {
    String subject = "Connection to provider [" + providerId + "] is active";
    String headerMessage = "Found provider [" + providerId
        + "] has active connection";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , providerId ,
            null , new Boolean( true ) , conn ) );
  }

  public static void sendAlertProviderStatusInactive( String providerId ,
      Connection conn ) {
    String subject = "Connection to provider [" + providerId + "] is inactive";
    String headerMessage = "Found provider [" + providerId
        + "] has inactive connection";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , providerId ,
            null , new Boolean( false ) , conn ) );
  }

  public static void sendAlertProviderSendQueueFull( String providerId ,
      BoundedLinkedQueue queue ) {
    String subject = "WARNING - Send Queue provider [" + providerId
        + "] is full";
    String headerMessage = "Found send queue provider [" + providerId
        + "] is full";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , providerId ,
            queue , null , null ) );
  }

  public static void sendAlertMtRespQueueFull( BoundedLinkedQueue queue ) {
    String subject = "WARNING - MtResponse Queue providers is full";
    String headerMessage = "Found mt response queue providers is full";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , null , queue ,
            null , null ) );
  }

  public static void sendAlertDrQueueFull( BoundedLinkedQueue queue ) {
    String subject = "WARNING - Dr Queue providers is full";
    String headerMessage = "Found dr queue providers is full";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , footerMessage , null , queue ,
            null , null ) );
  }

  private static String composeAlertBodyMessage( String headerMessage ,
      String footerMessage , String providerId , BoundedLinkedQueue queue ,
      Boolean connStatus , Connection conn ) {
    StringBuffer sb = new StringBuffer();
    sb.append( headerMessage + "\n\n" );
    if ( providerId != null ) {
      sb.append( "  Provider Id : " + providerId + "\n" );
    }
    if ( queue != null ) {
      sb.append( "  Queue Size : " + queue.size() + " msg(s)\n" );
      sb.append( "  Queue Capacity : " + queue.capacity() + " msg(s)\n" );
    }
    if ( connStatus != null ) {
      sb.append( "  Connection Status : "
          + ( connStatus.booleanValue() ? "Active" : "Inactive" ) + "\n" );
    }
    if ( conn != null ) {
      sb.append( "  Connection Url : " + conn.getHttpUrl() + "\n" );
    }
    sb.append( "  Date Alert : " + DateTimeFormat.convertToString( new Date() )
        + "\n" );
    sb.append( "\n" );
    sb.append( footerMessage + "\n\n" );
    return sb.toString();
  }

  private static boolean sendAlertMessage( String subject , String message ) {
    return sendAlertMessage( AlertMessageFactory.createAlertEmailMessage(
        AlertMessage.DESTNODE_SUPPORT_EMAIL , null , subject , message , 3 ) );
  }

  private static boolean sendAlertMessage( AlertMessage alertMessage ) {
    boolean result = false;
    if ( alertMessage == null ) {
      DLog.warning( lctx , "Failed to send alert message "
          + ", found null message" );
      return result;
    }
    try {
      result = onmApp.sendAlert( alertMessage );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to send alert message , " + e );
    }
    return result;
  }

}
