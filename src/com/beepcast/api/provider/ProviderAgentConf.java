package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderAgentConf {

  private String providerId;
  private boolean enabled;
  private String className;
  private int queueSize;
  private int workerSize;
  private int workerSleep;
  private boolean enableErrorCheck;
  private int maxErrorTolerant;
  private List connectionConfs;
  private Map connectionParams;
  private Map scriptParams;
  private Map messageParams;

  public ProviderAgentConf() {
    providerId = null;
    enabled = false;
    className = null;
    queueSize = 100;
    workerSize = 1;
    workerSleep = 250;
    enableErrorCheck = true;
    maxErrorTolerant = 5;
    connectionConfs = new ArrayList();
    connectionParams = new HashMap();
    scriptParams = new HashMap();
    messageParams = new HashMap();
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

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize( int queueSize ) {
    this.queueSize = queueSize;
  }

  public int getWorkerSize() {
    return workerSize;
  }

  public void setWorkerSize( int workerSize ) {
    this.workerSize = workerSize;
  }

  public int getWorkerSleep() {
    return workerSleep;
  }

  public void setWorkerSleep( int workerSleep ) {
    this.workerSleep = workerSleep;
  }

  public boolean isEnableErrorCheck() {
    return enableErrorCheck;
  }

  public void setEnableErrorCheck( boolean enableErrorCheck ) {
    this.enableErrorCheck = enableErrorCheck;
  }

  public int getMaxErrorTolerant() {
    return maxErrorTolerant;
  }

  public void setMaxErrorTolerant( int maxErrorTolerant ) {
    this.maxErrorTolerant = maxErrorTolerant;
  }

  public List getConnectionConfs() {
    return connectionConfs;
  }

  public void addConnectionConf( ConnectionConf connConf ) {
    connectionConfs.add( connConf );
  }

  public Map getConnectionParams() {
    return connectionParams;
  }

  public void addConnectionParam( String name , String value ) {
    connectionParams.put( name , value );
  }

  public Map getScriptParams() {
    return scriptParams;
  }

  public void addScriptParam( String name , String value ) {
    scriptParams.put( name , value );
  }

  public Map getMessageParams() {
    return messageParams;
  }

  public void addMessageParam( String name , String value ) {
    messageParams.put( name , value );
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ProviderAgentConf ( " + "providerId = " + this.providerId + TAB
        + "enabled = " + this.enabled + TAB + "className = " + this.className
        + TAB + "queueSize = " + this.queueSize + TAB + "workerSize = "
        + this.workerSize + TAB + "workerSleep = " + this.workerSleep + TAB
        + "enableErrorCheck = " + this.enableErrorCheck + TAB
        + "maxErrorTolerant = " + this.maxErrorTolerant + TAB + " )";
    return retValue;
  }

}
