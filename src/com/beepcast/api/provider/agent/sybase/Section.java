package com.beepcast.api.provider.agent.sybase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Section {

  private String name;
  private Map parameters;

  public Section() {
    parameters = new LinkedHashMap();
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Parameter getParameter( String parameterName ) {
    Parameter parameter = null;
    if ( ( parameterName == null ) || ( parameterName.equals( "" ) ) ) {
      return parameter;
    }
    parameter = (Parameter) parameters.get( parameterName );
    return parameter;
  }

  public boolean addParameter( Parameter parameter ) {
    boolean result = false;
    if ( parameter == null ) {
      return result;
    }
    String parameterName = parameter.getName();
    if ( ( parameterName == null ) || ( parameterName.equals( "" ) ) ) {
      return result;
    }
    if ( getParameter( parameterName ) != null ) {
      return result;
    }
    result = true;
    parameters.put( parameterName , parameter );
    return result;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    // store header
    sb.append( name );
    sb.append( "\n" );
    // store body
    String parameterName;
    Parameter parameter;
    Iterator iter = parameters.keySet().iterator();
    while ( iter.hasNext() ) {
      parameterName = (String) iter.next();
      parameter = getParameter( parameterName );
      if ( parameter != null ) {
        sb.append( parameter.toString() );
      }
    }
    return sb.toString();
  }

}
