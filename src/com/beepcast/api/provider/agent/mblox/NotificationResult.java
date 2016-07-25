package com.beepcast.api.provider.agent.mblox;

public class NotificationResult {

  private int sequenceNumber;
  private String notfResultCode;
  private String notfResultText;
  private SubscriberResult subscriberResult;

  public NotificationResult() {
  }

  public NotificationResult( int sequenceNumber , String notfResultCode ,
      String notfResultText , SubscriberResult subscriberResult ) {
    setSequenceNumber( sequenceNumber );
    setNotfResultCode( notfResultCode );
    setNotfResultText( notfResultText );
    setSubscriberResult( subscriberResult );
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber( int sequenceNumber ) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getNotfResultCode() {
    return notfResultCode;
  }

  public void setNotfResultCode( String notfResultCode ) {
    this.notfResultCode = notfResultCode;
  }

  public String getNotfResultText() {
    return notfResultText;
  }

  public void setNotfResultText( String notfResultText ) {
    this.notfResultText = notfResultText;
  }

  public SubscriberResult getSubscriberResult() {
    return subscriberResult;
  }

  public void setSubscriberResult( SubscriberResult subscriberResult ) {
    this.subscriberResult = subscriberResult;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "NotificationResult ( " + "sequenceNumber = "
        + this.sequenceNumber + TAB + "notfResultCode = " + this.notfResultCode
        + TAB + "notfResultText = " + this.notfResultText + TAB
        + "subscriberResult = " + this.subscriberResult + TAB + " )";
    return retValue;
  }

}
