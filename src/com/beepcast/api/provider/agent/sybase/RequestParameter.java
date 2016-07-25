package com.beepcast.api.provider.agent.sybase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestParameter {

  private String version;
  private String subject;
  private Map sections;

  public RequestParameter() {
    sections = new LinkedHashMap();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject( String subject ) {
    this.subject = subject;
  }

  public Section getSection( String sectionName ) {
    Section section = null;
    if ( ( sectionName == null ) || ( sectionName.equals( "" ) ) ) {
      return section;
    }
    section = (Section) sections.get( sectionName );
    return section;
  }

  public boolean addSection( Section section ) {
    boolean result = false;
    if ( section == null ) {
      return result;
    }
    String sectionName = section.getName();
    if ( ( sectionName == null ) || ( sectionName.equals( "" ) ) ) {
      return result;
    }
    if ( getSection( sectionName ) != null ) {
      return result;
    }
    result = true;
    sections.put( sectionName , section );
    return result;
  }

  public boolean delSection( String sectionName ) {
    boolean result = false;
    if ( ( sectionName == null ) || ( sectionName.equals( "" ) ) ) {
      return result;
    }
    result = sections.remove( sectionName ) != null;
    return result;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    String sectionName;
    Section section;
    Iterator iter = sections.keySet().iterator();
    while ( iter.hasNext() ) {
      sectionName = (String) iter.next();
      section = getSection( sectionName );
      if ( section != null ) {
        sb.append( section.toString() );
      }
    }
    return sb.toString();
  }
}
