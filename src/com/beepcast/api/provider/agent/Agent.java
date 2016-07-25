package com.beepcast.api.provider.agent;

public interface Agent {

  public void start();

  public void stop();

  public String providerId();

  public boolean isConnectionsAvailable();

  public void verifyStatusConnections();

  public int maxErrorConnection();

  public boolean isEnabled();

  public boolean setupAgentThreads();

  public Thread createAgentThread( int index );

}
