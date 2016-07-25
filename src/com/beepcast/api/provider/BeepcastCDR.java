package com.beepcast.api.provider;

import java.util.HashMap;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.common.cdr.CDRAdapterWriter;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class BeepcastCDR {

  static final DLogContext lctx = new SimpleContext( "BeepcastCDR" );

  private CDRAdapterWriter cdrAdapterWriter = null;

  public BeepcastCDR() {
    cdrAdapterWriter = new CDRAdapterWriter( "beepcastCDR" );
  }

  public int createTicket( String contentType , String contentDirection ,
      String providerId , String eventId , String phoneNumber ,
      String shortCode , String senderId , String intMessageId ,
      String extMessageId , String status , String message ) {
    int result = -1;
    HashMap hashmap = new HashMap();
    hashmap.put( "contentType" , contentType );
    hashmap.put( "contentDirection" , contentDirection );
    hashmap.put( "providerId" , providerId );
    hashmap.put( "eventId" , eventId );
    hashmap.put( "phoneNumber" , phoneNumber );
    hashmap.put( "shortCode" , shortCode );
    hashmap.put( "senderId" , senderId );
    hashmap.put( "intMessageId" , intMessageId );
    hashmap.put( "extMessageId" , extMessageId );
    hashmap.put( "status" , status );
    hashmap.put( "message" , message );
    result = cdrAdapterWriter.write( hashmap );
    if ( result != 0 ) {
      DLog.warning( lctx , "Failed to write the cdr file - " + intMessageId );
    }
    return result;
  }

  public void createDRTicket( ProviderMessage providerMessage ) {
    String contentType = "SMS.DR";
    String contentDirection = "";
    String providerId = providerMessage.getProviderId();
    String eventId = "";
    String phoneNumber = "";
    String shortCode = "";
    String senderId = "";
    String intMessageId = providerMessage.getInternalMessageId();
    String extMessageId = providerMessage.getExternalMessageId();
    String status = providerMessage.getInternalStatus();
    String shortMessage = "";
    createTicket( contentType , contentDirection , providerId , eventId ,
        phoneNumber , shortCode , senderId , intMessageId , extMessageId ,
        status , shortMessage );
  }

  public void createMTTicket( ProviderMessage providerMessage ) {
    String contentType = "SMS";
    String contentDirection = "MT";
    String providerId = providerMessage.getProviderId();
    String eventId = providerMessage.getEventId();
    String phoneNumber = providerMessage.getDestinationAddress();
    String shortCode = providerMessage.getOriginAddress();
    String senderId = "";
    String intMessageId = providerMessage.getInternalMessageId();
    String extMessageId = providerMessage.getExternalMessageId();
    String status = providerMessage.getInternalStatus();
    String shortMessage = providerMessage.getMessage();
    createTicket( contentType , contentDirection , providerId , eventId ,
        phoneNumber , shortCode , senderId , intMessageId , extMessageId ,
        status , shortMessage );
  }

  public void createMOTicket( ProviderMessage providerMessage ) {
    String contentType = "SMS.MO";
    String contentDirection = "";
    String providerId = providerMessage.getProviderId();
    String eventId = "";
    String phoneNumber = providerMessage.getOriginAddress();
    String shortCode = providerMessage.getDestinationAddress();
    String senderId = "";
    String intMessageId = providerMessage.getInternalMessageId();
    String extMessageId = providerMessage.getExternalMessageId();
    String status = providerMessage.getInternalStatus();
    String shortMessage = providerMessage.getMessage();
    createTicket( contentType , contentDirection , providerId , eventId ,
        phoneNumber , shortCode , senderId , intMessageId , extMessageId ,
        status , shortMessage );
  }

}
