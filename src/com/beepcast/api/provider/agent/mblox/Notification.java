package com.beepcast.api.provider.agent.mblox;

import com.beepcast.api.provider.util.XmlUtil;

public class Notification {

  private int sequenceNumber;
  private String messageType;
  private String messageFormat;
  private String message;
  private String profile;
  private String udh;
  private String senderIdType;
  private String senderId;
  private String expireDate;
  private String operator;
  private String tariff;
  private String subscriberNumber;
  private String subscriberSession;
  private String serviceDesc;
  private String contentType;
  private String serviceId;

  public Notification() {
  }

  public Notification( int sequenceNumber , String messageType ,
      String messageFormat , String message , String profile , String udh ,
      String senderIdType , String senderId , String expireDate ,
      String operator , String tariff , String subscriberNumber ,
      String subscriberSession , String serviceDesc , String contentType ,
      String serviceId ) {
    setSequenceNumber( sequenceNumber );
    setMessageType( messageType );
    setMessageFormat( messageFormat );
    setMessage( message );
    setProfile( profile );
    setUdh( udh );
    setSenderIdType( senderIdType );
    setSenderId( senderId );
    setExpireDate( expireDate );
    setOperator( operator );
    setTariff( tariff );
    setSubscriberNumber( subscriberNumber );
    setSubscriberSession( subscriberSession );
    setServiceDesc( serviceDesc );
    setContentType( contentType );
    setServiceId( serviceId );
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber( int sequenceNumber ) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType( String messageType ) {
    this.messageType = messageType;
  }

  public String getMessageFormat() {
    return messageFormat;
  }

  public void setMessageFormat( String messageFormat ) {
    this.messageFormat = messageFormat;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile( String profile ) {
    this.profile = profile;
  }

  public String getUdh() {
    return udh;
  }

  public void setUdh( String udh ) {
    this.udh = udh;
  }

  public String getSenderIdType() {
    return senderIdType;
  }

  public void setSenderIdType( String senderIdType ) {
    this.senderIdType = senderIdType;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId( String senderId ) {
    this.senderId = senderId;
  }

  public String getExpireDate() {
    return expireDate;
  }

  public void setExpireDate( String expireDate ) {
    this.expireDate = expireDate;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator( String operator ) {
    this.operator = operator;
  }

  public String getTariff() {
    return tariff;
  }

  public void setTariff( String tariff ) {
    this.tariff = tariff;
  }

  public String getSubscriberNumber() {
    return subscriberNumber;
  }

  public void setSubscriberNumber( String subscriberNumber ) {
    this.subscriberNumber = subscriberNumber;
  }

  public String getSubscriberSession() {
    return subscriberSession;
  }

  public void setSubscriberSession( String subscriberSession ) {
    this.subscriberSession = subscriberSession;
  }

  public String getServiceDesc() {
    return serviceDesc;
  }

  public void setServiceDesc( String serviceDesc ) {
    this.serviceDesc = serviceDesc;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType( String contentType ) {
    this.contentType = contentType;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId( String serviceId ) {
    this.serviceId = serviceId;
  }

  public String toXml() {
    StringBuffer sbXml = new StringBuffer();
    sbXml.append( "<Notification " );
    sbXml.append( "SequenceNumber=\"" + sequenceNumber + "\" " );
    if ( messageType != null ) {
      sbXml.append( "MessageType=\"" + XmlUtil.encode( messageType ) + "\" " );
    }
    if ( messageFormat != null ) {
      sbXml.append( "Format=\"" + XmlUtil.encode( messageFormat ) + "\" " );
    }
    sbXml.append( ">" );
    if ( message != null ) {
      sbXml.append( "<Message>" );
      boolean unicode = false;
      if ( ( messageFormat != null )
          && ( messageFormat.equalsIgnoreCase( "Unicode" ) ) ) {
        unicode = true;
      }
      sbXml.append( XmlUtil.encode( message , unicode ) );
      sbXml.append( "</Message>" );
    }
    if ( profile != null ) {
      sbXml.append( "<Profile>" );
      sbXml.append( XmlUtil.encode( profile ) );
      sbXml.append( "</Profile>" );
    }
    if ( udh != null ) {
      sbXml.append( "<Udh>" );
      sbXml.append( XmlUtil.encode( udh ) );
      sbXml.append( "</Udh>" );
    }
    if ( senderId != null ) {
      sbXml.append( "<SenderID " );
      if ( senderIdType != null ) {
        sbXml.append( "Type=\"" + XmlUtil.encode( senderIdType ) + "\" " );
      }
      sbXml.append( ">" );
      sbXml.append( XmlUtil.encode( senderId ) );
      sbXml.append( "</SenderID>" );
    }
    if ( expireDate != null ) {
      sbXml.append( "<ExpireDate>" );
      sbXml.append( XmlUtil.encode( expireDate ) );
      sbXml.append( "</ExpireDate>" );
    }
    if ( operator != null ) {
      sbXml.append( "<Operator>" );
      sbXml.append( XmlUtil.encode( operator ) );
      sbXml.append( "</Operator>" );
    }
    if ( tariff != null ) {
      sbXml.append( "<Tariff>" );
      sbXml.append( XmlUtil.encode( tariff ) );
      sbXml.append( "</Tariff>" );
    }
    if ( subscriberNumber != null ) {
      sbXml.append( "<Subscriber>" );
      sbXml.append( "<SubscriberNumber>" );
      sbXml.append( XmlUtil.encode( subscriberNumber ) );
      sbXml.append( "</SubscriberNumber>" );
      if ( subscriberSession != null ) {
        sbXml.append( "<SessionId>" );
        sbXml.append( XmlUtil.encode( subscriberSession ) );
        sbXml.append( "</SessionId>" );
      }
      sbXml.append( "</Subscriber>" );
    }
    if ( serviceDesc != null ) {
      sbXml.append( "<ServiceDesc>" );
      sbXml.append( XmlUtil.encode( serviceDesc ) );
      sbXml.append( "</ServiceDesc>" );
    }
    if ( contentType != null ) {
      sbXml.append( "<ContentType>" );
      sbXml.append( XmlUtil.encode( contentType ) );
      sbXml.append( "</ContentType>" );
    }
    if ( serviceId != null ) {
      sbXml.append( "<ServiceId>" );
      sbXml.append( XmlUtil.encode( serviceId ) );
      sbXml.append( "</ServiceId>" );
    }
    sbXml.append( "</Notification>" );
    return sbXml.toString();
  }

}
