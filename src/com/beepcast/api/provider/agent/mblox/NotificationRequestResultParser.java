package com.beepcast.api.provider.agent.mblox;

import java.util.ArrayList;

import org.w3c.dom.Node;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class NotificationRequestResultParser {

  static final DLogContext lctx = new SimpleContext(
      "NotificationRequestResultParser" );

  public static NotificationRequestResult parse( Node nodeRoot ) {
    NotificationRequestResult notfRequestResult = null;

    if ( nodeRoot == null ) {
      return notfRequestResult;
    }

    notfRequestResult = new NotificationRequestResult();

    notfRequestResult
        .setVersion( TreeUtil.getAttribute( nodeRoot , "Version" ) );

    Node nodeNotificationResultHeader = TreeUtil.first( nodeRoot ,
        "NotificationResultHeader" );
    parseNotificationResultHeader( nodeNotificationResultHeader ,
        notfRequestResult );

    Node nodeNotificationResultList = TreeUtil.first( nodeRoot ,
        "NotificationResultList" );
    parseNodeNotificationResultList( nodeNotificationResultList ,
        notfRequestResult );

    return notfRequestResult;
  }

  private static boolean parseNodeNotificationResultList(
      Node nodeNotificationResultList ,
      NotificationRequestResult notfRequestResult ) {
    boolean result = false;
    if ( nodeNotificationResultList == null ) {
      return result;
    }

    ArrayList notfResultList = new ArrayList();
    notfRequestResult.setNotfResultList( notfResultList );

    Node nodeNotificationResult = TreeUtil.first( nodeNotificationResultList ,
        "NotificationResult" );
    parseNodeNotificationResult( nodeNotificationResult , notfResultList );

    result = true;
    return result;
  }

  private static boolean parseNodeNotificationResult(
      Node nodeNotificationResult , ArrayList notfResultList ) {
    boolean result = false;
    if ( nodeNotificationResult == null ) {
      return result;
    }

    NotificationResult notfResult = new NotificationResult();
    notfResultList.add( notfResult );

    try {
      notfResult.setSequenceNumber( Integer.parseInt( TreeUtil.getAttribute(
          nodeNotificationResult , "SequenceNumber" ) ) );
    } catch ( Exception e ) {
    }

    Node nodeNotificationResultCode = TreeUtil.first( nodeNotificationResult ,
        "NotificationResultCode" );
    if ( nodeNotificationResultCode != null ) {
      notfResult.setNotfResultCode( TreeUtil
          .childText( nodeNotificationResultCode ) );
    }

    Node nodeNotificationResultText = TreeUtil.first( nodeNotificationResult ,
        "NotificationResultText" );
    if ( nodeNotificationResultText != null ) {
      notfResult.setNotfResultText( TreeUtil
          .childText( nodeNotificationResultText ) );
    }

    Node nodeSubscriberResult = TreeUtil.first( nodeNotificationResult ,
        "SubscriberResult" );
    parseNodeSubscriberResult( nodeSubscriberResult , notfResult );

    result = true;
    return result;
  }

  private static boolean parseNodeSubscriberResult( Node nodeSubscriberResult ,
      NotificationResult notfResult ) {
    boolean result = false;
    if ( nodeSubscriberResult == null ) {
      return result;
    }

    SubscriberResult subscriberResult = new SubscriberResult();
    notfResult.setSubscriberResult( subscriberResult );

    Node nodeSubscriberNumber = TreeUtil.first( nodeSubscriberResult ,
        "SubscriberNumber" );
    if ( nodeSubscriberNumber != null ) {
      subscriberResult.setSubscriberNumber( TreeUtil
          .childText( nodeSubscriberNumber ) );
    }

    Node nodeSubscriberResultCode = TreeUtil.first( nodeSubscriberResult ,
        "SubscriberResultCode" );
    if ( nodeSubscriberResultCode != null ) {
      subscriberResult.setSubscriberResultCode( TreeUtil
          .childText( nodeSubscriberResultCode ) );
    }

    Node nodeSubscriberResultText = TreeUtil.first( nodeSubscriberResult ,
        "SubscriberResultText" );
    if ( nodeSubscriberResultText != null ) {
      subscriberResult.setSubscriberResultText( TreeUtil
          .childText( nodeSubscriberResultText ) );
    }

    Node nodeRetry = TreeUtil.first( nodeSubscriberResult , "Retry" );
    if ( nodeRetry != null ) {
      try {
        subscriberResult.setRetry( Integer.parseInt( TreeUtil
            .childText( nodeRetry ) ) );
      } catch ( Exception e ) {
      }
    }

    result = true;
    return result;
  }

  private static boolean parseNotificationResultHeader(
      Node nodeNotificationResultHeader ,
      NotificationRequestResult notfRequestResult ) {
    boolean result = false;
    if ( nodeNotificationResultHeader == null ) {
      return result;
    }

    NotificationResultHeader notfResultHeader = new NotificationResultHeader();
    notfRequestResult.setNotfResultHeader( notfResultHeader );

    Node nodePartnerName = TreeUtil.first( nodeNotificationResultHeader ,
        "PartnerName" );
    if ( nodePartnerName != null ) {
      notfResultHeader.setPartnerName( TreeUtil.childText( nodePartnerName ) );
    }

    Node nodePartnerRef = TreeUtil.first( nodeNotificationResultHeader ,
        "PartnerRef" );
    if ( nodePartnerRef != null ) {
      notfResultHeader.setPartnerRef( TreeUtil.childText( nodePartnerRef ) );
    }

    Node nodeRequestResultCode = TreeUtil.first( nodeNotificationResultHeader ,
        "RequestResultCode" );
    if ( nodeRequestResultCode != null ) {
      notfResultHeader.setRequestResultCode( TreeUtil
          .childText( nodeRequestResultCode ) );
    }

    Node nodeRequestResultText = TreeUtil.first( nodeNotificationResultHeader ,
        "RequestResultText" );
    if ( nodeRequestResultCode != null ) {
      notfResultHeader.setRequestResultText( TreeUtil
          .childText( nodeRequestResultText ) );
    }

    result = true;
    return result;
  }

}
