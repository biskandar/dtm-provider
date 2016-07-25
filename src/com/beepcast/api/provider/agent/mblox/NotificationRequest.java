package com.beepcast.api.provider.agent.mblox;

import java.util.Iterator;
import java.util.List;

public class NotificationRequest {

  private String version;
  private NotificationHeader notfHeader;
  private String batchId;
  private List notfList;

  public NotificationRequest() {
  }

  public NotificationRequest( String version , NotificationHeader notfHeader ,
      String batchId , List notfList ) {
    setVersion( version );
    setNotfHeader( notfHeader );
    setBatchId( batchId );
    setNotfList( notfList );
  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  public NotificationHeader getNotfHeader() {
    return notfHeader;
  }

  public void setNotfHeader( NotificationHeader notfHeader ) {
    this.notfHeader = notfHeader;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId( String batchId ) {
    this.batchId = batchId;
  }

  public List getNotfList() {
    return notfList;
  }

  public void setNotfList( List notfList ) {
    this.notfList = notfList;
  }

  public String toXml() {
    StringBuffer sbXml = new StringBuffer();
    sbXml.append( "<?xml version=\"1.0\"?>" );
    sbXml.append( "<NotificationRequest Version=\"" + version + "\">" );
    if ( notfHeader != null ) {
      sbXml.append( notfHeader.toXml() );
    }
    if ( notfList != null ) {
      sbXml.append( "<NotificationList BatchID=\"" );
      sbXml.append( batchId );
      sbXml.append( "\" >" );
      Iterator notfIter = notfList.iterator();
      while ( notfIter.hasNext() ) {
        Notification notf = (Notification) notfIter.next();
        if ( notf != null ) {
          sbXml.append( notf.toXml() );
        }
      }
      sbXml.append( "</NotificationList>" );
    }
    sbXml.append( "</NotificationRequest>" );
    return sbXml.toString();
  }

}
