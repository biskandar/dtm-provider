package com.beepcast.api.provider;

public interface Worker {

  public boolean isThreadActive( int threadIdx );

  public int threadSize();

  public boolean setupThreads();

  public boolean resetThreads();

  public Thread createThread( int threadIdx );

}
