package com.beepcast.api.provider.agent.sybase;

public class Parameter {

  private String name;
  private String value;

  public Parameter() {
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    if ( name != null ) {
      sb.append( name );
      sb.append( "=" );
      if ( value != null ) {
        sb.append( value );
      }
      sb.append( "\n" );
    }
    return sb.toString();
  }

}
