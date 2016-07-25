package com.beepcast.api.provider.agent.sybase;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SectionFactory {

  static final DLogContext lctx = new SimpleContext( "SectionFactory" );

  public static Section generateSection( String sectionName ) {
    Section section = null;
    if ( ( sectionName == null ) || ( sectionName.equals( "" ) ) ) {
      return section;
    }
    section = new Section();
    section.setName( sectionName );
    return section;
  }

}
