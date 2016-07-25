package com.beepcast.api.provider.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomPriorityProviderGenerator {

  private static Random rand = new Random();

  class Provider {

    private int priority;
    private String providerId;

    public Provider( int priority , String providerId ) {
      this.priority = priority;
      this.providerId = providerId;
    }

    public int getPriority() {
      return priority;
    }

    public String getProviderId() {
      return providerId;
    }

  }

  private List list = null;

  public RandomPriorityProviderGenerator() {
    list = new ArrayList();
  }

  public void addProvider( int priority , String providerId ) {
    Provider provider = new Provider( priority , providerId );
    list.add( provider );
  }

  public String getNextProviderId() {
    String next = null;
    if ( ( list != null ) && ( list.size() > 0 ) ) {

      Iterator iter = null;
      Provider provider = null;

      int candidate , idx = 0 , len = 0;

      iter = list.iterator();
      while ( iter.hasNext() ) {
        provider = (Provider) iter.next();
        len += provider.getPriority();
      }

      candidate = rand.nextInt( len );

      iter = list.iterator();
      while ( iter.hasNext() ) {
        provider = (Provider) iter.next();
        idx += provider.getPriority();
        if ( candidate < idx ) {
          next = getProviderId( provider );
          break;
        }
      }

    }
    return next;
  }

  private String getProviderId( Provider provider ) {
    String result = null;
    if ( provider != null ) {
      result = provider.getProviderId();
    }
    return result;
  }

}
