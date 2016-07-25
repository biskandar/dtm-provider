package com.beepcast.api.provider.agent.mblox;

public abstract class InboundService {

  private String version;
  private String headerPartner;
  private String headerPassword;
  private String headerServiceId;

  public InboundService() {

  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  public String getHeaderPartner() {
    return headerPartner;
  }

  public void setHeaderPartner( String headerPartner ) {
    this.headerPartner = headerPartner;
  }

  public String getHeaderPassword() {
    return headerPassword;
  }

  public void setHeaderPassword( String headerPassword ) {
    this.headerPassword = headerPassword;
  }

  public String getHeaderServiceId() {
    return headerServiceId;
  }

  public void setHeaderServiceId( String headerServiceId ) {
    this.headerServiceId = headerServiceId;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "InboundService ( " + "version = " + this.version + TAB
        + "headerPartner = " + this.headerPartner + TAB + "headerPassword = "
        + this.headerPassword + TAB + "headerServiceId = "
        + this.headerServiceId + TAB + " )";
    return retValue;
  }

}
