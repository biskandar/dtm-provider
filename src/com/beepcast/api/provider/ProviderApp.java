package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderApp implements Module , ProviderApi {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProviderApp" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private BeepcastCDR cdrMan;

  private boolean initialized;

  private ProviderConf conf;
  private BoundedLinkedQueue drQueue;
  private BoundedLinkedQueue moQueue;

  private MOWorker moWorker;
  private MTRespWorker mtRespWorker;
  private MTSendWorker mtSendWorker;
  private DRWorker drWorker;

  private ProviderManagement management;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public synchronized void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start "
          + ", found app is not yet initialized" );
      return;
    }

    // starting engine worker(s)

    DLog.debug( lctx , "Starting mo worker module" );
    moWorker.start();
    DLog.debug( lctx , "Started mo worker module" );

    DLog.debug( lctx , "Starting mt resp worker module" );
    mtRespWorker.start();
    DLog.debug( lctx , "Started mt resp worker module" );

    DLog.debug( lctx , "Starting mt send worker module" );
    mtSendWorker.start();
    DLog.debug( lctx , "Started mt send worker module" );

    DLog.debug( lctx , "Starting dr worker module" );
    drWorker.start();
    DLog.debug( lctx , "Started dr worker module" );

    DLog.debug( lctx , "Starting provider management module" );
    management.start();
    DLog.debug( lctx , "Started provider management module" );

    // debug list current providers

    List listOutgoingProviders = listOutgoingProviderIds();
    DLog.debug( lctx , "Refreshed list outgoing providers : "
        + listOutgoingProviders );

    List listIncomingProviders = listIncomingProviderIds();
    DLog.debug( lctx , "Refreshed list incoming providers : "
        + listOutgoingProviders );

  }

  public synchronized void stop() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop "
          + ", found app is not yet initialized" );
      return;
    }

    // stopping engine worker(s)

    management.stop();
    DLog.debug( lctx , "Stopped provider management worker module" );

    drWorker.stop();
    DLog.debug( lctx , "Stopped dr worker module" );

    mtSendWorker.stop();
    DLog.debug( lctx , "Stopped mt send worker module" );

    mtRespWorker.stop();
    DLog.debug( lctx , "Stopped mt resp worker module" );

    moWorker.stop();
    DLog.debug( lctx , "Stopped mo worker module" );

  }

  public boolean processMessage( String providerId , String externalMessageId ,
      String originAddress , String destinationAddress , String message ,
      Date dateDelivery , Map optionalParams , String externalParams ) {
    boolean result = false;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to process message "
          + ", found app is not yet initialized" );
      return result;
    }
    if ( !moWorker.storeMoMessage( providerId , externalMessageId ,
        originAddress , destinationAddress , message , dateDelivery ,
        optionalParams , externalParams ) ) {
      DLog.warning( lctx , "Failed to process message "
          + ", found failed to store mo message" );
      return result;
    }
    result = true;
    return result;
  }

  public boolean sendMessage( ProviderMessage providerMessage ) {
    boolean result = false;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to send message "
          + ", found app is not yet initialized" );
      return result;
    }
    // validate must be parameters
    if ( providerMessage == null ) {
      DLog.warning( lctx , "Failed to send message "
          + ", found null providerMessage parameter" );
      return result;
    }
    // log it first
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );
    DLog.debug(
        lctx ,
        headerLog + "Send provider message : dstAddr = "
            + providerMessage.getDestinationAddress() + ", oriAddr = "
            + providerMessage.getOriginAddress() + ", oriAddrMask = "
            + providerMessage.getOriginAddrMask() + ", debitAmount = "
            + providerMessage.getDebitAmount() + ", cntType = "
            + providerMessage.getContentType() + ", msgCount = "
            + providerMessage.getMessageCount() + ", msgContent = "
            + StringEscapeUtils.escapeJava( providerMessage.getMessage() ) );
    // process thru mt send worker
    result = mtSendWorker.sendMessage( providerMessage );
    return result;
  }

  public boolean processReport( String providerId , String externalMessageId ,
      String externalStatus , Date dateReport , Map optionalParams ,
      String externalParams ) {
    boolean result = false;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to process report "
          + ", found app is not yet initialized" );
      return result;
    }
    if ( !drWorker.storeDrMessage( providerId , externalMessageId ,
        externalStatus , dateReport , optionalParams , externalParams ) ) {
      DLog.warning( lctx , "Failed to process report "
          + ", found failed to store dr message" );
      return result;
    }
    result = true;
    return result;
  }

  public boolean processReportEachBroker( ProviderMessage providerMessage ) {
    return drWorker.processReportEachBroker( providerMessage );
  }

  public ArrayList listOutgoingProviderIds() {
    return listOutgoingProviderIds( null , false , true , true );
  }

  public ArrayList listOutgoingProviderIds( boolean filterByWorkers ,
      boolean addMembers , boolean filterBySuspend ) {
    return listOutgoingProviderIds( null , filterByWorkers , addMembers ,
        filterBySuspend );
  }

  public ArrayList listOutgoingProviderIds( List listFilterByTypes ,
      boolean filterByWorkers , boolean addMembers , boolean filterBySuspend ) {
    ArrayList listProviderIds = new ArrayList();
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to list outgoing provider ids "
          + ", found app is not yet initialized" );
      return listProviderIds;
    }
    listProviderIds = ListInOuProviderIdsUtils.listOutgoingProviderIds(
        listFilterByTypes , filterByWorkers , addMembers , filterBySuspend ,
        conf.isDebug() );
    return listProviderIds;
  }

  public ArrayList listIncomingProviderIds() {
    ArrayList listProviderIds = new ArrayList();
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to list incoming provider ids "
          + ", found app is not yet initialized" );
      return listProviderIds;
    }
    listProviderIds = ListInOuProviderIdsUtils.listIncomingProviderIds( conf
        .isDebug() );
    return listProviderIds;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProviderConf getProviderConf() {
    return conf;
  }

  public BoundedLinkedQueue getDrQueue() {
    return drQueue;
  }

  public BoundedLinkedQueue getMoQueue() {
    return moQueue;
  }

  // ////////////////////////////////////////////////////////////////////////////

  public MOWorker getMoWorker() {
    return moWorker;
  }

  public MTSendWorker getMtSendWorker() {
    return mtSendWorker;
  }

  public MTRespWorker getMtRespWorker() {
    return mtRespWorker;
  }

  public DRWorker getDrWorker() {
    return drWorker;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void init( ProviderConf conf ) {
    initialized = false;

    if ( conf == null ) {
      DLog.error( lctx , "Can not init the providerApp "
          + ", found null provider conf" );
      return;
    }

    // set a new conf

    this.conf = conf;
    DLog.debug( lctx , "Use conf " + conf );

    // log debug

    DLog.debug( lctx , "Debug mode = " + conf.isDebug() );

    // validate http timeout

    verifyHttpTimeout();

    // created mo and dr queue

    drQueue = new BoundedLinkedQueue( this.conf.getDrSizeQueue() );
    DLog.debug( lctx , "Successfullly created dr queue , max size = "
        + this.conf.getDrSizeQueue() + " msg(s)" );

    moQueue = new BoundedLinkedQueue( this.conf.getMoSizeQueue() );
    DLog.debug( lctx , "Successfullly created mo queue , max size = "
        + this.conf.getMoSizeQueue() + " msg(s)" );

    // generate engine worker(s)

    moWorker = new MOWorker( this );
    DLog.debug( lctx , "Created mo worker module" );

    mtRespWorker = new MTRespWorker( this );
    DLog.debug( lctx , "Created mt resp worker module" );

    mtSendWorker = new MTSendWorker( this );
    DLog.debug( lctx , "Created mt send worker module" );

    drWorker = new DRWorker( this );
    DLog.debug( lctx , "Created dr worker module" );

    management = new ProviderManagement( this );
    DLog.debug( lctx , "Created provider management module" );

    initialized = true;
  }

  public boolean createDRTicket( ProviderMessage msg ) {
    boolean result = false;
    if ( msg == null ) {
      DLog.warning( lctx , "Failed to create dr ticket "
          + ", found null provider message" );
      return result;
    }
    cdrMan.createDRTicket( msg );
    result = true;
    return result;
  }

  public boolean createMTTicket( ProviderMessage msg ) {
    boolean result = false;
    if ( msg == null ) {
      DLog.warning( lctx , "Failed to create mt ticket "
          + ", found null provider message" );
      return result;
    }
    cdrMan.createMTTicket( msg );
    result = true;
    return result;
  }

  public boolean createMOTicket( ProviderMessage msg ) {
    boolean result = false;
    if ( msg == null ) {
      DLog.warning( lctx , "Failed to create mo ticket "
          + ", found null provider message" );
      return result;
    }
    cdrMan.createMOTicket( msg );
    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void init() {
    cdrMan = new BeepcastCDR();
  }

  private void verifyHttpTimeout() {
    verifyHttpTimeout( "sun.net.client.defaultConnectTimeout" , "60000" );
    verifyHttpTimeout( "sun.net.client.defaultReadTimeout" , "60000" );
  }

  private void verifyHttpTimeout( String propertyTimeout , String defaultTimeout ) {
    String timeout = System.getProperty( propertyTimeout );
    if ( timeout == null ) {
      System.setProperty( propertyTimeout , defaultTimeout );
      timeout = System.getProperty( propertyTimeout );
    }
    DLog.debug( lctx , "Set " + propertyTimeout + " = " + timeout + " ms" );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Singleton Pattern
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static final ProviderApp INSTANCE = new ProviderApp();

  private ProviderApp() {
    init();
  }

  public static final ProviderApp getInstance() {
    return INSTANCE;
  }

}
