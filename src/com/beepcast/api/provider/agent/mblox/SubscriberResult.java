package com.beepcast.api.provider.agent.mblox;

public class SubscriberResult {

  private String subscriberNumber;
  private String subscriberResultCode;
  private String subscriberResultText;
  private int retry;

  public SubscriberResult() {
  }

  public SubscriberResult( String subscriberNumber ,
      String subscriberResultCode , String subscriberResultText , int retry ) {
    setSubscriberNumber( subscriberNumber );
    setSubscriberResultCode( subscriberResultCode );
    setSubscriberResultText( subscriberResultText );
    setRetry( retry );
  }

  public String getSubscriberNumber() {
    return subscriberNumber;
  }

  public void setSubscriberNumber( String subscriberNumber ) {
    this.subscriberNumber = subscriberNumber;
  }

  public String getSubscriberResultCode() {
    return subscriberResultCode;
  }

  public void setSubscriberResultCode( String subscriberResultCode ) {
    this.subscriberResultCode = subscriberResultCode;
  }

  public String getSubscriberResultText() {
    return subscriberResultText;
  }

  public void setSubscriberResultText( String subscriberResultText ) {
    this.subscriberResultText = subscriberResultText;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry( int retry ) {
    this.retry = retry;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "SubscriberResult ( " + "subscriberNumber = "
        + this.subscriberNumber + TAB + "subscriberResultCode = "
        + this.subscriberResultCode + TAB + "subscriberResultText = "
        + this.subscriberResultText + TAB + "retry = " + this.retry + TAB
        + " )";
    return retValue;
  }

}
