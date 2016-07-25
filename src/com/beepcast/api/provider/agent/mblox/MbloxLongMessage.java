package com.beepcast.api.provider.agent.mblox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.data.ContentType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.idgen.IdGenApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MbloxLongMessage {

  static final DLogContext lctx = new SimpleContext( "MbloxLongMessage" );

  static final IdGenApp idGenApp = IdGenApp.getInstance();

  public static boolean split( ProviderMessage msg , int msgRef ,
      ArrayList lstMsgs ) {
    boolean result = false;
    if ( msg == null ) {
      return result;
    }

    String headerLog = ProviderMessageCommon.headerLog( msg );

    List listMsgContents = split( msg.getContentType() , msg.getMessage() );
    if ( listMsgContents == null ) {
      DLog.warning( lctx , headerLog + "Failed to split message content "
          + ", found null result" );
      return result;
    }

    msgRef = ( msgRef < 1 ? 1 : ( msgRef > 255 ? 255 : msgRef ) );
    DLog.debug( lctx ,
        headerLog + "Splitted message content ( type = " + msg.getContentType()
            + " ) became " + listMsgContents.size()
            + " msg(s) , with msgRef = " + msgRef );

    int msgIdx = 1;
    Iterator iterMsgContents = listMsgContents.iterator();
    while ( iterMsgContents.hasNext() ) {
      String msgContentPart = (String) iterMsgContents.next();
      if ( msgContentPart == null ) {
        continue;
      }
      try {

        // clone from original message , assuming all the message params was
        // same by default
        ProviderMessage msgPart = (ProviderMessage) msg.clone();
        if ( msgPart == null ) {
          continue;
        }

        // compose the new split message
        String udh = ":05:00:03" + hex( msgRef ) + hex( msg.getMessageCount() )
            + hex( msgIdx );
        msgPart.addOptionalParam( "udh" , udh );
        msgPart.setMessage( msgContentPart );
        msgPart.setMessageCount( 1 );
        if ( msgIdx != msg.getMessageCount() ) {
          msgPart
              .setInternalMessageId( "INT".concat( idGenApp.nextIdentifier() ) );
          msgPart.setDebitAmount( 0 );
          msgPart.addOptionalParam( "delmsg" , "true" );
        } else {
          msgPart.addOptionalParam( "orimsg" , msg.getMessage() );
          msgPart.addOptionalParam( "orimsgcnt" ,
              Integer.toString( msg.getMessageCount() ) );
        }
        DLog.debug(
            lctx ,
            headerLog + "Splitted message part " + ": intMsgId = "
                + msgPart.getInternalMessageId() + " , debitAmount = "
                + msgPart.getDebitAmount() + " , udh = "
                + msgPart.getOptionalParam( "udh" ) + " , messageCount = "
                + msgPart.getMessageCount() + " , message = "
                + StringEscapeUtils.escapeJava( msgPart.getMessage() ) );

        // store the new split message back into the queue to continue
        // to process
        lstMsgs.add( msgPart );

        // for the next split message process
        msgIdx = msgIdx + 1;
      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to process split message , "
            + e );
      }
    }

    result = true;
    return result;
  }

  public static List split( int contentType , String messageContent ) {
    List list = null;

    if ( messageContent == null ) {
      return list;
    }

    list = new ArrayList();

    // http://spin.atomicobject.com/2011/04/20/
    // concatenated-sms-messages-and-character-counts/
    //
    // GSM phones use a 7-bit character encoding, each individual concatenated
    // SMS message can hold 153 characters : 1072 bits / (7 bits/character) =
    // 153 characters
    //
    // Unicode phones use a 16-bit character encoding, so each individual
    // concatenated SMS message can hold 67 characters : 1072 bits / (16
    // bits/character) = 67 characters

    int divider = 153;
    if ( contentType == ContentType.SMSUNICODE ) {
      divider = 67;
    }

    int messageLength = -1;
    if ( messageContent != null ) {
      messageLength = messageContent.length();
    }
    while ( messageLength > 0 ) {

      int endIndex = messageLength < divider ? messageLength : divider;

      String cropText = messageContent.substring( 0 , endIndex );
      if ( ( cropText != null ) && ( !cropText.equals( "" ) ) ) {
        list.add( cropText );
      }

      messageContent = messageContent.substring( endIndex );

      messageLength = -1;
      if ( messageContent != null ) {
        messageLength = messageContent.length();
      }

    }

    return list;
  }

  public static String hex( int val ) {
    String hex = ":";
    if ( val < 16 ) {
      hex += "0";
    }
    hex += Integer.toString( val , 16 ).toUpperCase();
    return hex;
  }

}
