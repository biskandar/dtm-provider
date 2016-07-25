package com.beepcast.api.provider.agent.mblox;

import java.util.List;

public class NotificationRequestResult {

  private String version;
  private NotificationResultHeader notfResultHeader;
  private List notfResultList;

  public NotificationRequestResult() {
  }

  public NotificationRequestResult( String version ,
      NotificationResultHeader notfResultHeader , List notfResultList ) {
    setVersion( version );
    setNotfResultHeader( notfResultHeader );
    setNotfResultList( notfResultList );
  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  public NotificationResultHeader getNotfResultHeader() {
    return notfResultHeader;
  }

  public void setNotfResultHeader( NotificationResultHeader notfResultHeader ) {
    this.notfResultHeader = notfResultHeader;
  }

  public List getNotfResultList() {
    return notfResultList;
  }

  public void setNotfResultList( List notfResultList ) {
    this.notfResultList = notfResultList;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "NotificationRequestResult ( " + "version = " + this.version
        + TAB + "notfResultHeader = " + this.notfResultHeader + TAB
        + "notfResultList = " + this.notfResultList + TAB + " )";
    return retValue;
  }

}
