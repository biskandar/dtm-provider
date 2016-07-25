package com.beepcast.api.provider.agent.mblox;

import org.w3c.dom.Node;

import com.firsthop.common.util.xml.TreeUtil;

public class InboundServiceParser {

  public static boolean parseHeader( Node nodeRoot , InboundService inService ) {
    boolean result = false;

    if ( nodeRoot == null ) {
      return result;
    }
    if ( inService == null ) {
      return result;
    }

    inService.setVersion( TreeUtil.getAttribute( nodeRoot , "Version" ) );

    Node nodeHeader = TreeUtil.first( nodeRoot , "Header" );
    if ( nodeHeader == null ) {
      return result;
    }

    Node nodePartner = TreeUtil.first( nodeHeader , "Partner" );
    if ( nodePartner != null ) {
      inService.setHeaderPartner( TreeUtil.childText( nodePartner ) );
    }

    Node nodePassword = TreeUtil.first( nodeHeader , "Password" );
    if ( nodePassword != null ) {
      inService.setHeaderPassword( TreeUtil.childText( nodePassword ) );
    }

    Node nodeServiceId = TreeUtil.first( nodeHeader , "ServiceID" );
    if ( nodeServiceId != null ) {
      inService.setHeaderServiceId( TreeUtil.childText( nodeServiceId ) );
    }

    result = true;
    return result;
  }

}
