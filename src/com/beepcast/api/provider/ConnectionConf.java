package com.beepcast.api.provider;

public class ConnectionConf {

  private String protocol;
  private String remoteHost;
  private String remotePort;
  private String remotePath;

  public ConnectionConf() {
    protocol = "http";
    remotePort = "80";
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol( String protocol ) {
    this.protocol = protocol;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost( String remoteHost ) {
    this.remoteHost = remoteHost;
  }

  public String getRemotePort() {
    return remotePort;
  }

  public void setRemotePort( String remotePort ) {
    this.remotePort = remotePort;
  }

  public String getRemotePath() {
    return remotePath;
  }

  public void setRemotePath( String remotePath ) {
    this.remotePath = remotePath;
  }

  public String toString() {
    return "ConnectionConf [protocol=" + protocol + ", remoteHost="
        + remoteHost + ", remotePort=" + remotePort + ", remotePath="
        + remotePath + "]";
  }

}
