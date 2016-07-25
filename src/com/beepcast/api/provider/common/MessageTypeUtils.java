package com.beepcast.api.provider.common;

import com.beepcast.api.provider.data.ContentType;
import com.beepcast.model.transaction.MessageType;

public class MessageTypeUtils {

  public static String transformProviderContentTypeStrToGatewayMessageType(
      int providerContentType ) {
    return MessageType
        .messageTypeToString( transformProviderContentTypeIntToGatewayMessageType( providerContentType ) );
  }

  public static int transformProviderContentTypeIntToGatewayMessageType(
      int providerContentType ) {
    int gatewayMessageType = com.beepcast.model.transaction.MessageType.UNKNOWN_TYPE;
    switch ( providerContentType ) {
    case ContentType.SMSTEXT :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.TEXT_TYPE;
      break;
    case ContentType.SMSBINARY :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.TEXT_TYPE;
      break;
    case ContentType.SMSUNICODE :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.UNICODE_TYPE;
      break;
    case ContentType.MMS :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.MMS_TYPE;
      break;
    case ContentType.QRPNG :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRPNG_TYPE;
      break;
    case ContentType.QRGIF :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRGIF_TYPE;
      break;
    case ContentType.QRJPG :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRJPG_TYPE;
      break;
    case ContentType.WEBHOOK :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.WEBHOOK_TYPE;
      break;
    }
    return gatewayMessageType;
  }

}
