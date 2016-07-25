package com.beepcast.api.provider.data;

import java.util.Date;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderMessageFactory {

  static final DLogContext lctx = new SimpleContext( "ProviderMessageFactory" );

  public static ProviderMessage createProviderMOMessage( String messageType ,
      String providerId , String externalMessageId , String originAddress ,
      String destinationAddress , String message , int messageCount ,
      double debitAmount , Date deliverDateTime , String externalParams ) {
    ProviderMessage providerMessage = createProviderMessage( null , null ,
        null , providerId , messageType , 0 , null , externalMessageId ,
        destinationAddress , originAddress , null , null , null , message ,
        messageCount , null , null , debitAmount , 0 , 0 , null ,
        deliverDateTime , null , null );
    providerMessage.setExternalParams( externalParams );
    return providerMessage;
  }

  public static ProviderMessage createProviderDRMessage( String messageType ,
      String providerId , String externalMessageId , String externalStatus ,
      Date statusDateTime , String externalParams ) {
    ProviderMessage providerMessage = createProviderMessage( null , null ,
        null , providerId , messageType , 0 , null , externalMessageId , null ,
        null , null , null , null , null , 0 , null , externalStatus , 0 , 0 ,
        0 , null , null , null , statusDateTime );
    providerMessage.setExternalParams( externalParams );
    return providerMessage;
  }

  public static ProviderMessage createProviderMessage( String eventId ,
      String channelSessionId , String providerId , String messageType ,
      int contentType , String internalMessageId , String destinationAddress ,
      String originAddress , String originAddrMask , String message ,
      int messageCount , double debitAmount , int priority , int retry ,
      String description ) {
    return createProviderMessage( null , eventId , channelSessionId ,
        providerId , messageType , contentType , internalMessageId , null ,
        destinationAddress , originAddress , originAddrMask , null , null ,
        message , messageCount , null , null , debitAmount , priority , retry ,
        description , null , null , null );
  }

  public static ProviderMessage createProviderMessage( String recordId ,
      String eventId , String channelSessionId , String providerId ,
      String messageType , int contentType , String internalMessageId ,
      String externalMessageId , String destinationAddress ,
      String originAddress , String originAddrMask , String destinationNode ,
      String originNode , String message , int messageCount ,
      String internalStatus , String externalStatus , double debitAmount ,
      int priority , int retry , String description , Date deliverDateTime ,
      Date submitDateTime , Date statusDateTime ) {

    ProviderMessage providerMessage = new ProviderMessage();

    if ( deliverDateTime == null ) {
      deliverDateTime = new Date();
    }
    if ( submitDateTime == null ) {
      submitDateTime = new Date();
    }
    if ( statusDateTime == null ) {
      statusDateTime = new Date();
    }

    providerMessage.setRecordId( recordId );
    providerMessage.setEventId( eventId );
    providerMessage.setChannelSessionId( channelSessionId );
    providerMessage.setProviderId( providerId );
    providerMessage.setMessageType( messageType );
    providerMessage.setContentType( contentType );
    providerMessage.setInternalMessageId( internalMessageId );
    providerMessage.setExternalMessageId( externalMessageId );
    providerMessage.setDestinationAddress( destinationAddress );
    providerMessage.setOriginAddress( originAddress );
    providerMessage.setOriginAddrMask( originAddrMask );
    providerMessage.setDestinationNode( destinationNode );
    providerMessage.setOriginNode( originNode );
    providerMessage.setMessage( message );
    providerMessage.setMessageCount( messageCount );
    providerMessage.setInternalStatus( internalStatus );
    providerMessage.setExternalStatus( externalStatus );
    providerMessage.setDebitAmount( debitAmount );
    providerMessage.setPriority( priority );
    providerMessage.setRetry( retry );
    providerMessage.setDescription( description );
    providerMessage.setDeliverDateTime( deliverDateTime );
    providerMessage.setSubmitDateTime( submitDateTime );
    providerMessage.setStatusDateTime( statusDateTime );

    return providerMessage;
  }

}
