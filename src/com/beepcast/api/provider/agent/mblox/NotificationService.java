package com.beepcast.api.provider.agent.mblox;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends InboundService {

  public static final String MAPTAGS_HDR_OPERATOR = "Operator";

  private String notfBatchId;
  private String notfSequenceNumber;
  private String subscriberNumber;
  private String subscriberStatus;
  private String subscriberTimeStamp;
  private String subscriberMsgReference;
  private String subscriberReason;

  private Map mapTags;

  public NotificationService() {
    mapTags = new HashMap();
  }

  public String getNotfBatchId() {
    return notfBatchId;
  }

  public void setNotfBatchId( String notfBatchId ) {
    this.notfBatchId = notfBatchId;
  }

  public String getNotfSequenceNumber() {
    return notfSequenceNumber;
  }

  public void setNotfSequenceNumber( String notfSequenceNumber ) {
    this.notfSequenceNumber = notfSequenceNumber;
  }

  public String getSubscriberNumber() {
    return subscriberNumber;
  }

  public void setSubscriberNumber( String subscriberNumber ) {
    this.subscriberNumber = subscriberNumber;
  }

  public String getSubscriberStatus() {
    return subscriberStatus;
  }

  public void setSubscriberStatus( String subscriberStatus ) {
    this.subscriberStatus = subscriberStatus;
  }

  public String getSubscriberTimeStamp() {
    return subscriberTimeStamp;
  }

  public void setSubscriberTimeStamp( String subscriberTimeStamp ) {
    this.subscriberTimeStamp = subscriberTimeStamp;
  }

  public String getSubscriberMsgReference() {
    return subscriberMsgReference;
  }

  public void setSubscriberMsgReference( String subscriberMsgReference ) {
    this.subscriberMsgReference = subscriberMsgReference;
  }

  public String getSubscriberReason() {
    return subscriberReason;
  }

  public void setSubscriberReason( String subscriberReason ) {
    this.subscriberReason = subscriberReason;
  }

  public Map getMapTags() {
    return mapTags;
  }

  public String getMapTagOperator() {
    return (String) mapTags.get( MAPTAGS_HDR_OPERATOR );
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "NotificationService ( " + super.toString() + TAB
        + "notfBatchId = " + this.notfBatchId + TAB + "notfSequenceNumber = "
        + this.notfSequenceNumber + TAB + "subscriberNumber = "
        + this.subscriberNumber + TAB + "subscriberStatus = "
        + this.subscriberStatus + TAB + "subscriberTimeStamp = "
        + this.subscriberTimeStamp + TAB + "subscriberMsgReference = "
        + this.subscriberMsgReference + TAB + "subscriberReason = "
        + this.subscriberReason + TAB + " )";
    return retValue;
  }

}
