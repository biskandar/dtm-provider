package com.beepcast.api.provider.agent.sybase;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ParameterFactory {

  static final DLogContext lctx = new SimpleContext( "ParameterFactory" );

  public static Parameter generateParameter( String parameterName , String value ) {
    Parameter parameter = null;
    if ( ( parameterName == null ) || ( parameterName.equals( "" ) ) ) {
      return parameter;
    }
    if ( ( value == null ) || ( value.equals( "" ) ) ) {
      return parameter;
    }
    parameter = new Parameter();
    parameter.setName( parameterName );
    parameter.setValue( value );
    return parameter;
  }

}
