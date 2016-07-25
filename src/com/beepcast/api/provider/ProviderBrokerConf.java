package com.beepcast.api.provider;

import java.util.HashMap;
import java.util.Map;

public class ProviderBrokerConf {

  private String providerId;
  private boolean enabled;
  private String className;
  private Map mapParams;

  public ProviderBrokerConf() {
    providerId = null;
    enabled = false;
    className = null;
    mapParams = new HashMap();
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId( String providerId ) {
    this.providerId = providerId;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName( String className ) {
    this.className = className;
  }

  public Map getMapParams() {
    return mapParams;
  }

  public void addMapParam( String name , String value ) {
    mapParams.put( name , value );
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ProviderBrokerConf ( " + "providerId = " + this.providerId
        + TAB + "enabled = " + this.enabled + TAB + "className = "
        + this.className + TAB + "mapParams = " + this.mapParams + TAB + " )";
    return retValue;
  }

}
