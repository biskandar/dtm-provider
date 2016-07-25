package com.beepcast.api.provider.agent.mblox;

import org.w3c.dom.Node;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class NotificationServiceParser {

  static final DLogContext lctx = new SimpleContext(
      "NotificationServiceParser" );

  public static NotificationService parse( Node nodeRoot ) {
    NotificationService notfService = new NotificationService();

    try {

      if ( !InboundServiceParser.parseHeader( nodeRoot , notfService ) ) {
        DLog.warning( lctx , "Failed to parse notification service "
            + ", found failed to parse header" );
        return notfService;
      }

      Node nodeNotificationList = TreeUtil
          .first( nodeRoot , "NotificationList" );
      if ( nodeNotificationList == null ) {
        return notfService;
      }

      Node nodeNotification = TreeUtil.first( nodeNotificationList ,
          "Notification" );
      if ( nodeNotification == null ) {
        return notfService;
      }

      notfService.setNotfBatchId( TreeUtil.getAttribute( nodeNotification ,
          "BatchID" ) );
      notfService.setNotfSequenceNumber( TreeUtil.getAttribute(
          nodeNotification , "SequenceNumber" ) );

      Node nodeSubscriber = TreeUtil.first( nodeNotification , "Subscriber" );
      if ( nodeSubscriber == null ) {
        return notfService;
      }

      Node nodeSubscriberNumber = TreeUtil.first( nodeSubscriber ,
          "SubscriberNumber" );
      if ( nodeSubscriberNumber != null ) {
        notfService.setSubscriberNumber( TreeUtil
            .childText( nodeSubscriberNumber ) );
      }

      Node nodeStatus = TreeUtil.first( nodeSubscriber , "Status" );
      if ( nodeStatus != null ) {
        notfService.setSubscriberStatus( TreeUtil.childText( nodeStatus ) );
      }

      Node nodeTimeStamp = TreeUtil.first( nodeSubscriber , "TimeStamp" );
      if ( nodeTimeStamp != null ) {
        notfService
            .setSubscriberTimeStamp( TreeUtil.childText( nodeTimeStamp ) );
      }

      Node nodeMsgReference = TreeUtil.first( nodeSubscriber , "MsgReference" );
      if ( nodeMsgReference != null ) {
        notfService.setSubscriberMsgReference( TreeUtil
            .childText( nodeMsgReference ) );
      }

      Node nodeReason = TreeUtil.first( nodeSubscriber , "Reason" );
      if ( nodeReason != null ) {
        notfService.setSubscriberReason( TreeUtil.childText( nodeReason ) );
      }

      Node nodeTags = TreeUtil.first( nodeSubscriber , "Tags" );
      if ( nodeTags != null ) {
        Node nodeTag = TreeUtil.first( nodeTags , "Tag" );
        while ( nodeTag != null ) {
          String tagField = TreeUtil.getAttribute( nodeTag , "Name" );
          String tagValue = TreeUtil.childText( nodeTag );
          notfService.getMapTags().put( tagField , tagValue );
          nodeTag = TreeUtil.next( nodeTag , "Tag" );
        }
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to parse notification service , " + e );
    }

    return notfService;
  }

}
