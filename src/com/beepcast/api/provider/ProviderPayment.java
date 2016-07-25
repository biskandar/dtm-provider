package com.beepcast.api.provider;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.billing.BillingApp;
import com.beepcast.billing.BillingResult;
import com.beepcast.billing.BillingStatus;
import com.beepcast.billing.profile.PaymentType;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.transaction.billing.AccountProfile;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderPayment {

  static final DLogContext lctx = new SimpleContext( "ProviderPayment" );

  public static boolean doDebit( ProviderMessage providerMessage ) {
    return doPayment( providerMessage , "debit" );
  }

  public static boolean doCredit( ProviderMessage providerMessage ) {
    return doPayment( providerMessage , "credit" );
  }

  public static boolean doPayment( ProviderMessage providerMessage ,
      String command ) {
    boolean result = false;

    if ( providerMessage == null ) {
      return result;
    }

    if ( command == null ) {
      return result;
    }

    // compose header log
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );

    // get event id
    int eventId = 0;
    try {
      eventId = Integer.parseInt( providerMessage.getEventId() );
    } catch ( NumberFormatException e ) {
    }

    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to perform " + command
          + " payment , found unknown event id" );
      return result;
    }

    // get event bean
    TEvent eventOut = EventCommon.getEvent( eventId );
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to perform " + command
          + " payment , found unregister event object" );
      return result;
    }

    // get client id
    int clientId = eventOut.getClientId();
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to perform " + command
          + " payment , found zero client id" );
      return result;
    }

    // get client bean
    TClient clientOut = ClientCommon.getClient( clientId );
    if ( clientOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to perform " + command
          + " payment , found unregister client object" );
      return result;
    }

    // get client payment type
    int clientPaymentType = clientOut.getPaymentType();
    if ( ( clientPaymentType != PaymentType.PREPAID )
        && ( clientPaymentType != PaymentType.POSTPAID ) ) {
      DLog.warning( lctx , headerLog + "Failed to perform " + command
          + " payment , found unregister client payment type" );
      return result;
    }

    try {

      // prepare billing app and result
      BillingApp billingApp = BillingApp.getInstance();
      BillingResult billingResult = null;

      // do the payment based on payment type
      if ( StringUtils.equalsIgnoreCase( command , "debit" ) ) {
        if ( clientPaymentType == PaymentType.PREPAID ) {
          DLog.debug( lctx , headerLog + "Performed debit to client prepaid "
              + ", for id = " + clientId + " , with amount = "
              + providerMessage.getDebitAmount() + " unit(s)" );
          billingResult = billingApp.doDebit( AccountProfile.CLIENT_PREPAID ,
              new Integer( clientId ) ,
              new Double( providerMessage.getDebitAmount() ) );
        }
        if ( clientPaymentType == PaymentType.POSTPAID ) {
          DLog.debug( lctx , headerLog + "Performed debit to client postpaid "
              + ", for id = " + clientId + " , with amount = "
              + providerMessage.getDebitAmount() + " unit(s)" );
          billingResult = billingApp.doDebit( AccountProfile.CLIENT_POSTPAID ,
              new Integer( clientId ) ,
              new Double( providerMessage.getDebitAmount() ) );
        }
      }
      if ( StringUtils.equalsIgnoreCase( command , "credit" ) ) {
        if ( clientPaymentType == PaymentType.PREPAID ) {
          DLog.debug( lctx , headerLog + "Performed credit to client prepaid "
              + ", for id = " + clientId + " , with amount = "
              + providerMessage.getDebitAmount() + " unit(s)" );
          billingResult = billingApp.doCredit( AccountProfile.CLIENT_PREPAID ,
              new Integer( clientId ) ,
              new Double( providerMessage.getDebitAmount() ) );
        }
        if ( clientPaymentType == PaymentType.POSTPAID ) {
          DLog.debug( lctx , headerLog + "Performed credit to client postpaid "
              + ", for id = " + clientId + " , with amount = "
              + providerMessage.getDebitAmount() + " unit(s)" );
          billingResult = billingApp.doCredit( AccountProfile.CLIENT_POSTPAID ,
              new Integer( clientId ) ,
              new Double( providerMessage.getDebitAmount() ) );
        }
      }

      // verify the billing result
      if ( billingResult == null ) {
        DLog.warning( lctx , headerLog + "Failed to perform " + command
            + " payment , found null billing result" );
        return result;
      }
      if ( billingResult.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
        DLog.warning(
            lctx ,
            headerLog + "Failed to perform " + command
                + " payment , found billing result status = "
                + billingResult.getPaymentResult() );
        return result;
      }

      // log it
      DLog.debug( lctx , headerLog + "Successfully performed " + command
          + " with amount total = " + providerMessage.getDebitAmount()
          + " unit(s) for client id = " + clientOut.getClientId() );

      // result as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to perform payment , " + e );
    }

    return result;
  }

}
