package com.beepcast.api.provider.agent.mblox;

public class ResponseService extends InboundService {

  private String respSequenceNumber;
  private String respType;
  private String respFormat;
  private String respTransactionId;
  private String respOriginatingNumber;
  private String respTime;
  private String respData;
  private String respDeliverer;
  private String respDestination;
  private String respOperator;
  private String respTariff;
  private String respSessionId;

  public ResponseService() {
  }

  public String getRespSequenceNumber() {
    return respSequenceNumber;
  }

  public void setRespSequenceNumber( String respSequenceNumber ) {
    this.respSequenceNumber = respSequenceNumber;
  }

  public String getRespType() {
    return respType;
  }

  public void setRespType( String respType ) {
    this.respType = respType;
  }

  public String getRespFormat() {
    return respFormat;
  }

  public void setRespFormat( String respFormat ) {
    this.respFormat = respFormat;
  }

  public String getRespTransactionId() {
    return respTransactionId;
  }

  public void setRespTransactionId( String respTransactionId ) {
    this.respTransactionId = respTransactionId;
  }

  public String getRespOriginatingNumber() {
    return respOriginatingNumber;
  }

  public void setRespOriginatingNumber( String respOriginatingNumber ) {
    this.respOriginatingNumber = respOriginatingNumber;
  }

  public String getRespTime() {
    return respTime;
  }

  public void setRespTime( String respTime ) {
    this.respTime = respTime;
  }

  public String getRespData() {
    return respData;
  }

  public void setRespData( String respData ) {
    this.respData = respData;
  }

  public String getRespDeliverer() {
    return respDeliverer;
  }

  public void setRespDeliverer( String respDeliverer ) {
    this.respDeliverer = respDeliverer;
  }

  public String getRespDestination() {
    return respDestination;
  }

  public void setRespDestination( String respDestination ) {
    this.respDestination = respDestination;
  }

  public String getRespOperator() {
    return respOperator;
  }

  public void setRespOperator( String respOperator ) {
    this.respOperator = respOperator;
  }

  public String getRespTariff() {
    return respTariff;
  }

  public void setRespTariff( String respTariff ) {
    this.respTariff = respTariff;
  }

  public String getRespSessionId() {
    return respSessionId;
  }

  public void setRespSessionId( String respSessionId ) {
    this.respSessionId = respSessionId;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "ResponseService ( " + super.toString() + TAB
        + "respSequenceNumber = " + this.respSequenceNumber + TAB
        + "respType = " + this.respType + TAB + "respFormat = "
        + this.respFormat + TAB + "respTransactionId = "
        + this.respTransactionId + TAB + "respOriginatingNumber = "
        + this.respOriginatingNumber + TAB + "respTime = " + this.respTime
        + TAB + "respData = " + this.respData + TAB + "respDeliverer = "
        + this.respDeliverer + TAB + "respDestination = "
        + this.respDestination + TAB + "respOperator = " + this.respOperator
        + TAB + "respTariff = " + this.respTariff + TAB + "respSessionId = "
        + this.respSessionId + TAB + " )";
    return retValue;
  }

}
