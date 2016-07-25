package com.beepcast.api.provider.agent.mblox;

import org.w3c.dom.Node;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class ResponseServiceParser {

  static final DLogContext lctx = new SimpleContext( "ResponseServiceParser" );

  public static ResponseService parse( Node nodeRoot ) {
    ResponseService respService = new ResponseService();

    if ( !InboundServiceParser.parseHeader( nodeRoot , respService ) ) {
      DLog.warning( lctx , "Failed to parse response service "
          + ", found failed to parse header" );
      return respService;
    }

    Node nodeResponseList = TreeUtil.first( nodeRoot , "ResponseList" );
    if ( nodeResponseList != null ) {
      Node nodeResponse = TreeUtil.first( nodeResponseList , "Response" );
      if ( nodeResponse != null ) {
        respService.setRespSequenceNumber( TreeUtil.getAttribute( nodeResponse ,
            "SequenceNumber" ) );
        respService
            .setRespType( TreeUtil.getAttribute( nodeResponse , "Type" ) );
        respService.setRespFormat( TreeUtil.getAttribute( nodeResponse ,
            "Format" ) );
        Node nodeTransactionID = TreeUtil
            .first( nodeResponse , "TransactionID" );
        if ( nodeTransactionID != null ) {
          respService.setRespTransactionId( TreeUtil
              .childText( nodeTransactionID ) );
        }
        Node nodeOriginatingNumber = TreeUtil.first( nodeResponse ,
            "OriginatingNumber" );
        if ( nodeOriginatingNumber != null ) {
          respService.setRespOriginatingNumber( TreeUtil
              .childText( nodeOriginatingNumber ) );
        }
        Node nodeTime = TreeUtil.first( nodeResponse , "Time" );
        if ( nodeTime != null ) {
          respService.setRespTime( TreeUtil.childText( nodeTime ) );
        }
        Node nodeData = TreeUtil.first( nodeResponse , "Data" );
        if ( nodeData != null ) {
          respService.setRespData( TreeUtil.childText( nodeData ) );
        }
        Node nodeDeliverer = TreeUtil.first( nodeResponse , "Deliverer" );
        if ( nodeDeliverer != null ) {
          respService.setRespDeliverer( TreeUtil.childText( nodeDeliverer ) );
        }
        Node nodeDestination = TreeUtil.first( nodeResponse , "Destination" );
        if ( nodeDestination != null ) {
          respService
              .setRespDestination( TreeUtil.childText( nodeDestination ) );
        }
        Node nodeOperator = TreeUtil.first( nodeResponse , "Operator" );
        if ( nodeOperator != null ) {
          respService.setRespOperator( TreeUtil.childText( nodeOperator ) );
        }
        Node nodeTariff = TreeUtil.first( nodeResponse , "Tariff" );
        if ( nodeTariff != null ) {
          respService.setRespTariff( TreeUtil.childText( nodeTariff ) );
        }
        Node nodeSessionId = TreeUtil.first( nodeResponse , "SessionId" );
        if ( nodeSessionId != null ) {
          respService.setRespSessionId( TreeUtil.childText( nodeSessionId ) );
        }
      } // if ( nodeResponse != null )
    } // if ( nodeResponseList != null )

    return respService;
  }

}
