package com.beepcast.api.provider.agent.sybase;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RequestParameterFactory {

  static final DLogContext lctx = new SimpleContext( "RequestParameterFactory" );

  public static RequestParameter generateRequestParameter() {
    return generateRequestParameter( null , null );
  }

  public static RequestParameter generateRequestParameter( String version ,
      String subject ) {
    RequestParameter requestParameter = new RequestParameter();

    // clean version parameter
    if ( ( version == null ) || ( version.equals( "" ) ) ) {
      requestParameter.setVersion( null );
    }

    // clean subject parameter
    if ( ( subject == null ) || ( subject.equals( "" ) ) ) {
      requestParameter.setSubject( null );
    }

    // generate default section
    generateDefaultSection( requestParameter );

    return requestParameter;
  }

  private static void generateDefaultSection( RequestParameter requestParameter ) {
    if ( requestParameter == null ) {
      return;
    }
    requestParameter.addSection( SectionFactory
        .generateSection( SectionName.MSISDN ) );
    requestParameter.addSection( SectionFactory
        .generateSection( SectionName.MESSAGE ) );
    requestParameter.addSection( SectionFactory
        .generateSection( SectionName.SETUP ) );
    requestParameter.addSection( SectionFactory
        .generateSection( SectionName.END ) );
  }

}
