package com.beepcast.api.provider.agent.mblox;

import com.beepcast.api.provider.util.XmlUtil;

public class NotificationHeader {

  private String partnerName;
  private String partnerPassword;
  private String subscriptionName;

  public NotificationHeader() {
  }

  public NotificationHeader( String partnerName , String partnerPassword ,
      String subscriptionName ) {
    setPartnerName( partnerName );
    setPartnerPassword( partnerPassword );
    setSubscriptionName( subscriptionName );
  }

  public String getPartnerName() {
    return partnerName;
  }

  public void setPartnerName( String partnerName ) {
    this.partnerName = partnerName;
  }

  public String getPartnerPassword() {
    return partnerPassword;
  }

  public void setPartnerPassword( String partnerPassword ) {
    this.partnerPassword = partnerPassword;
  }

  public String getSubscriptionName() {
    return subscriptionName;
  }

  public void setSubscriptionName( String subscriptionName ) {
    this.subscriptionName = subscriptionName;
  }

  public String toXml() {
    StringBuffer sbXml = new StringBuffer();
    sbXml.append( "<NotificationHeader>" );
    if ( partnerName != null ) {
      sbXml.append( "<PartnerName>" );
      sbXml.append( XmlUtil.encode( partnerName ) );
      sbXml.append( "</PartnerName>" );
    }
    if ( partnerPassword != null ) {
      sbXml.append( "<PartnerPassword>" );
      sbXml.append( XmlUtil.encode( partnerPassword ) );
      sbXml.append( "</PartnerPassword>" );
    }
    if ( subscriptionName != null ) {
      sbXml.append( "<SubscriptionName>" );
      sbXml.append( XmlUtil.encode( subscriptionName ) );
      sbXml.append( "</SubscriptionName>" );
    }
    sbXml.append( "</NotificationHeader>" );
    return sbXml.toString();
  }

}
