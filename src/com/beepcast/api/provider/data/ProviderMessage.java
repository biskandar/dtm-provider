package com.beepcast.api.provider.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProviderMessage implements Cloneable {

  private String recordId;
  private String eventId;
  private String channelSessionId;
  private String providerId;
  private String messageType;
  private int contentType;
  private String internalMessageId;
  private String externalMessageId;
  private String destinationAddress;
  private String originAddress;
  private String originAddrMask;
  private String destinationNode;
  private String originNode;
  private String message;
  private int messageCount;
  private String internalStatus;
  private String externalStatus;

  private double debitAmount;

  private int priority;
  private int retry;
  private String description;
  private String externalParams;

  private Map optionalParams;

  private Date deliverDateTime;
  private Date submitDateTime;
  private Date statusDateTime;

  public ProviderMessage() {
    optionalParams = new HashMap();
    deliverDateTime = new Date();
    submitDateTime = new Date();
    statusDateTime = new Date();
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId( String recordId ) {
    this.recordId = recordId;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId( String eventId ) {
    this.eventId = eventId;
  }

  public String getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( String channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId( String providerId ) {
    this.providerId = providerId;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType( String messageType ) {
    this.messageType = messageType;
  }

  public int getContentType() {
    return contentType;
  }

  public void setContentType( int contentType ) {
    this.contentType = contentType;
  }

  public String getInternalMessageId() {
    return internalMessageId;
  }

  public void setInternalMessageId( String internalMessageId ) {
    this.internalMessageId = internalMessageId;
  }

  public String getExternalMessageId() {
    return externalMessageId;
  }

  public void setExternalMessageId( String externalMessageId ) {
    this.externalMessageId = externalMessageId;
  }

  public String getDestinationAddress() {
    return destinationAddress;
  }

  public void setDestinationAddress( String destinationAddress ) {
    this.destinationAddress = destinationAddress;
  }

  public String getOriginAddress() {
    return originAddress;
  }

  public void setOriginAddress( String originAddress ) {
    this.originAddress = originAddress;
  }

  public String getOriginAddrMask() {
    return originAddrMask;
  }

  public void setOriginAddrMask( String originAddrMask ) {
    this.originAddrMask = originAddrMask;
  }

  public String getDestinationNode() {
    return destinationNode;
  }

  public void setDestinationNode( String destinationNode ) {
    this.destinationNode = destinationNode;
  }

  public String getOriginNode() {
    return originNode;
  }

  public void setOriginNode( String originNode ) {
    this.originNode = originNode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public int getMessageCount() {
    return messageCount;
  }

  public void setMessageCount( int messageCount ) {
    this.messageCount = messageCount;
  }

  public String getInternalStatus() {
    return internalStatus;
  }

  public void setInternalStatus( String internalStatus ) {
    this.internalStatus = internalStatus;
  }

  public String getExternalStatus() {
    return externalStatus;
  }

  public void setExternalStatus( String externalStatus ) {
    this.externalStatus = externalStatus;
  }

  public double getDebitAmount() {
    return debitAmount;
  }

  public void setDebitAmount( double debitAmount ) {
    this.debitAmount = debitAmount;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry( int retry ) {
    this.retry = retry;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getExternalParams() {
    return externalParams;
  }

  public void setExternalParams( String externalParams ) {
    this.externalParams = externalParams;
  }

  public Map getOptionalParams() {
    return optionalParams;
  }

  public void setOptionalParams( Map optionalParams ) {
    this.optionalParams = optionalParams;
  }

  public Date getDeliverDateTime() {
    return deliverDateTime;
  }

  public void setDeliverDateTime( Date deliverDateTime ) {
    this.deliverDateTime = deliverDateTime;
  }

  public Date getSubmitDateTime() {
    return submitDateTime;
  }

  public void setSubmitDateTime( Date submitDateTime ) {
    this.submitDateTime = submitDateTime;
  }

  public Date getStatusDateTime() {
    return statusDateTime;
  }

  public void setStatusDateTime( Date statusDateTime ) {
    this.statusDateTime = statusDateTime;
  }

  public void addOptionalParam( String key , Object value ) {
    optionalParams.put( key , value );
  }

  public Object getOptionalParam( String key ) {
    return optionalParams.get( key );
  }

  public Object clone() throws CloneNotSupportedException {
    ProviderMessage cloned = (ProviderMessage) super.clone();

    // deep copy for optional params
    cloned.setOptionalParams( new HashMap() );
    Set setKey = optionalParams.keySet();
    Iterator iterKey = setKey.iterator();
    while ( iterKey.hasNext() ) {
      String key = (String) iterKey.next();
      cloned.addOptionalParam( key , optionalParams.get( key ) );
    }

    return cloned;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ProviderMessage ( " + "recordId = " + this.recordId + TAB
        + "eventId = " + this.eventId + TAB + "channelSessionId = "
        + this.channelSessionId + TAB + "providerId = " + this.providerId + TAB
        + "messageType = " + this.messageType + TAB + "contentType = "
        + this.contentType + TAB + "internalMessageId = "
        + this.internalMessageId + TAB + "externalMessageId = "
        + this.externalMessageId + TAB + "destinationAddress = "
        + this.destinationAddress + TAB + "originAddress = "
        + this.originAddress + TAB + "originAddrMask = " + this.originAddrMask
        + TAB + "destinationNode = " + this.destinationNode + TAB
        + "originNode = " + this.originNode + TAB + "message = " + this.message
        + TAB + "internalStatus = " + this.internalStatus + TAB
        + "externalStatus = " + this.externalStatus + TAB + "debitAmount = "
        + this.debitAmount + TAB + "priority = " + this.priority + TAB
        + "retry = " + this.retry + TAB + "description = " + this.description
        + TAB + "deliverDateTime = " + this.deliverDateTime + TAB
        + "submitDateTime = " + this.submitDateTime + TAB + "statusDateTime = "
        + this.statusDateTime + TAB + " )";
    return retValue;
  }

}
