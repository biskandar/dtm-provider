package com.beepcast.api.provider.agent;

import java.util.ArrayList;
import java.util.Date;

import com.beepcast.api.provider.MTRespWorker;
import com.beepcast.api.provider.MTSendWorker;
import com.beepcast.api.provider.ProviderAgentConf;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.api.provider.agent.qr.QREngine;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class QRAgent extends BaseAgent {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "QRAgent" );
  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private QREngine qrEngine;
  private boolean initialized;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public QRAgent( ProviderConf providerConf , MTRespWorker mtRespWorker ,
      MTSendWorker mtSendWorker , String providerId ,
      ProviderAgentConf providerAgentConf ) {
    super( providerConf , mtRespWorker , mtSendWorker , providerId ,
        providerAgentConf );
    if ( !super.isInitialized() ) {
      DLog.error( lctx , "Failed to initialized" );
      return;
    }

    qrEngine = new QREngine( confMessageParams );

    // nothing to do yet ...

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Thread createAgentThread( int index ) {
    return new QRAgentThread( index );
  }

  public ArrayList processExtToIntMessage( ProviderMessage providerMessage ) {
    ArrayList listProviderMessages = new ArrayList();
    if ( providerMessage == null ) {
      return listProviderMessages;
    }
    listProviderMessages.add( providerMessage );
    return listProviderMessages;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , not yet initialized" );
      return;
    }
    super.startThread();
  }

  public void stop() {
    super.stopThread();
    initialized = false;
  }

  public boolean isConnectionsAvailable() {
    boolean result = false;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to retrive connection status info "
          + ", found not initialized yet" );
      return result;
    }

    // set always true

    result = true;
    return result;
  }

  public QREngine qrEngine() {
    return qrEngine;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void createQRImage( ProviderMessage providerMessage ) {

    // compose header log
    String headerLog = headerLog()
        + ProviderMessageCommon.headerLog( providerMessage );

    // update submit date
    providerMessage.setSubmitDateTime( new Date() );

    // generate externalMessageId based on internalMessageId
    providerMessage.setExternalMessageId( providerMessage
        .getInternalMessageId() );

    // resolve qr text
    String qrText = providerMessage.getMessage();
    if ( ( qrText == null ) || ( qrText.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Found empty qr text" );
      providerMessage.setInternalStatus( "FAILED" );
      providerMessage.setExternalStatus( "EMPTY QR TEXT" );
      return;
    }

    // resolve qr image file size : width x height
    int imageFileSizeWidth = 0 , imageFileSizeHeight = 0;
    try {
      String optParamKey = TransactionMessageParam.HDR_QR_IMAGE_FILE_SIZE;
      String optParamVal = (String) providerMessage
          .getOptionalParam( optParamKey );
      if ( ( optParamVal != null ) && ( !optParamVal.equals( "" ) ) ) {
        imageFileSizeWidth = imageFileSizeHeight = Integer
            .parseInt( optParamVal );
      }
    } catch ( Exception e ) {
    }

    // resolve qr image file name
    String imageFileName = (String) providerMessage
        .getOptionalParam( TransactionMessageParam.HDR_QR_IMAGE_FILE_NAME );
    if ( ( imageFileName == null ) || ( imageFileName.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Found empty qr file name" );
      providerMessage.setInternalStatus( "FAILED" );
      providerMessage.setExternalStatus( "EMPTY QR FILE NAME" );
      return;
    }

    // resolve event id
    int eventId = 0;
    try {
      eventId = Integer.parseInt( providerMessage.getEventId() );
    } catch ( Exception e ) {
    }

    // create qr image with qr engine
    if ( !qrEngine.createQrImageFile( providerMessage.getInternalMessageId() ,
        eventId , providerMessage.getContentType() , imageFileName ,
        imageFileSizeWidth , imageFileSizeHeight , qrText ) ) {
      DLog.warning( lctx , headerLog + "Found failed to create qr image file" );
      providerMessage.setInternalStatus( "FAILED" );
      providerMessage.setExternalStatus( "FAILED QR ENGINE" );
      return;
    }

    // extract message status
    providerMessage.setInternalStatus( "SUBMITTED" );
    providerMessage.setExternalStatus( "SUBMITTED" );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class QRAgentThread extends Thread {

    private int idx;

    public QRAgentThread( int idx ) {
      super( "QRAgentThread-" + providerId() + "." + idx );
      this.idx = idx;
    }

    public void run() {

      BoundedLinkedQueue respQueue = getRespQueue();
      if ( respQueue == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null resp queue" );
        return;
      }

      BoundedLinkedQueue sendQueueInt = getSendQueueInt();
      if ( sendQueueInt == null ) {
        DLog.warning( lctx , headerLog() + "Failed to run "
            + ", found null send queue" );
        return;
      }

      Object objectMessage = null;
      ProviderMessage providerMessage = null;
      String headerLog = null;

      DLog.debug( lctx , "Thread started" );
      while ( arrAgentThreads[idx] ) {

        try {
          Thread.sleep( providerAgentConf.getWorkerSleep() );
        } catch ( InterruptedException e ) {
        }

        // validate the response queue
        int curQueueRespPercentage = ( respQueue.size() * 100 )
            / respQueue.capacity();
        if ( curQueueRespPercentage > 80 ) {
          continue;
        }

        // prepare the message
        try {
          objectMessage = sendQueueInt.poll( 5000 );
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , headerLog() + "Failed to take object "
              + "provider message from the send queue , " + e );
        }
        if ( objectMessage == null ) {
          continue;
        }
        if ( !( objectMessage instanceof ProviderMessage ) ) {
          DLog.warning( lctx , headerLog() + "Failed to take object "
              + "provider message , found anonymous type" );
          continue;
        }

        providerMessage = (ProviderMessage) objectMessage;
        headerLog = ProviderMessageCommon.headerLog( providerMessage );
        DLog.debug( lctx , headerLog + "Got a message from internal channel" );

        trapLoad();
        createQRImage( providerMessage );
        mtSendWorker.createMTTicket( providerMessage );

        // put the response back in the queue
        try {
          respQueue.put( providerMessage );
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , headerLog + "Failed to put provider message "
              + "into the response queue , " + e );
        }

      } // while ( arrAgentThreads[idx] )
      DLog.debug( lctx , "Thread stopped" );

    }

  } // QRAgentThread Class

}
