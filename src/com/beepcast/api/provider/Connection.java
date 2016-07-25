package com.beepcast.api.provider;

public class Connection {

  private static Object lockObject = new Object();

  private boolean initialized;

  private String id;
  private boolean active;
  private ConnectionConf conf;

  public Connection( ConnectionConf conf ) {
    initialized = false;
    if ( conf == null ) {
      return;
    }
    this.conf = conf;
    initialized = true;
  }

  public void shutdown() {
    if ( !initialized ) {
      return;
    }
    synchronized ( lockObject ) {
      active = false;
    }
  }

  public void startup() {
    if ( !initialized ) {
      return;
    }
    synchronized ( lockObject ) {
      active = true;
    }
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getProtocol() {
    return conf.getProtocol();
  }

  public String getRemoteHost() {
    return conf.getRemoteHost();
  }

  public String getRemotePort() {
    return conf.getRemotePort();
  }

  public String getRemotePath() {
    return conf.getRemotePath();
  }

  public String getHttpUrl() {
    StringBuffer sb = new StringBuffer();
    sb.append( conf.getProtocol() );
    sb.append( "://" );
    sb.append( conf.getRemoteHost() );
    sb.append( ":" );
    sb.append( conf.getRemotePort() );
    sb.append( conf.getRemotePath() );
    return sb.toString();
  }

  public boolean isActive() {
    return active;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "Connection ( id = " + this.id + ", active = " + this.active
        + TAB + ", url = " + getHttpUrl() + " )";
    return retValue;
  }

}
