package com.beepcast.api.provider.common;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.beepcast.api.provider.agent.mblox.InboundService;
import com.beepcast.api.provider.agent.mblox.NotificationServiceParser;
import com.beepcast.api.provider.agent.mblox.ResponseServiceParser;
import com.beepcast.encrypt.EncryptApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.ThreadLocalParser;

public class MbloxParser {

  static final DLogContext lctx = new SimpleContext( "MbloxParser" );

  public static InboundService extractExternalParams( String externalParams ) {
    InboundService inService = null;

    if ( externalParams == null ) {
      DLog.warning( lctx , "Failed to extract external params "
          + ", found null" );
      return inService;
    }

    EncryptApp encryptApp = EncryptApp.getInstance();
    String plainText = encryptApp.base64Decode( externalParams );
    if ( StringUtils.isBlank( plainText ) ) {
      DLog.warning( lctx , "Failed to extract external params "
          + ", found empty plain text" );
      return inService;
    }

    inService = parse( plainText );
    return inService;
  }

  public static InboundService parse( String xmlData ) {
    InboundService inService = null;
    try {
      Document document = ThreadLocalParser.parse( xmlData );
      if ( document == null ) {
        DLog.warning( lctx , "Failed to parse xml data "
            + ", found null document" );
        return inService;
      }
      Element element = document.getDocumentElement();
      if ( element == null ) {
        DLog.warning( lctx , "Failed to parse xml data "
            + ", found null element" );
        return inService;
      }
      if ( inService == null ) {
        if ( element.getLocalName().equals( "NotificationService" ) ) {
          inService = NotificationServiceParser.parse( element );
        }
      }
      if ( inService == null ) {
        if ( element.getLocalName().equals( "ResponseService" ) ) {
          inService = ResponseServiceParser.parse( element );
        }
      }
      if ( inService == null ) {
        DLog.warning( lctx , "Failed to parse xml data "
            + ", found unknown node root" );
        return inService;
      }
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to parse xml data , " + e );
    }
    return inService;
  }

}
