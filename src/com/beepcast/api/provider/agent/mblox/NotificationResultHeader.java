package com.beepcast.api.provider.agent.mblox;

public class NotificationResultHeader {

  private String partnerName;
  private String partnerRef;
  private String requestResultCode;
  private String requestResultText;

  public NotificationResultHeader() {

  }

  public NotificationResultHeader( String partnerName , String partnerRef ,
      String requestResultCode , String requestResultText ) {
    setPartnerName( partnerName );
    setPartnerRef( partnerRef );
    setRequestResultCode( requestResultCode );
    setRequestResultText( requestResultText );
  }

  public String getPartnerName() {
    return partnerName;
  }

  public void setPartnerName( String partnerName ) {
    this.partnerName = partnerName;
  }

  public String getPartnerRef() {
    return partnerRef;
  }

  public void setPartnerRef( String partnerRef ) {
    this.partnerRef = partnerRef;
  }

  public String getRequestResultCode() {
    return requestResultCode;
  }

  public void setRequestResultCode( String requestResultCode ) {
    this.requestResultCode = requestResultCode;
  }

  public String getRequestResultText() {
    return requestResultText;
  }

  public void setRequestResultText( String requestResultText ) {
    this.requestResultText = requestResultText;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "NotificationResultHeader ( " + "partnerName = "
        + this.partnerName + TAB + "partnerRef = " + this.partnerRef + TAB
        + "requestResultCode = " + this.requestResultCode + TAB
        + "requestResultText = " + this.requestResultText + TAB + " )";
    return retValue;
  }

}
