package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.broker.Broker;
import com.beepcast.api.provider.broker.BrokerFactory;
import com.beepcast.api.provider.common.ProviderUtil;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageCommon;
import com.beepcast.api.provider.data.ProviderMessageFactory;
import com.beepcast.api.provider.util.DateTimeFormat;
import com.beepcast.api.provider.util.ProviderMessageOptionalMapParamUtils;
import com.beepcast.api.provider.util.QueueUtil;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.dbmanager.common.DnStatusMapCommon;
import com.beepcast.dbmanager.table.TDnStatusMap;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.lookup.LookupApp;
import com.beepcast.lookup.LookupListener;
import com.beepcast.lookup.data.LookupMessage;
import com.beepcast.model.mobileUser.MobileUserService;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.common.DrBufferBeanTransformUtils;
import com.beepcast.router.dr.DrBufferBean;
import com.beepcast.router.dr.DrBuffersService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DRWorker extends BaseWorker implements Module , LookupListener {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DRWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private LoadManagement loadMng;
  private DatabaseLibrary dbLib;
  private OnlinePropertiesApp opropsApp;
  private LookupApp lookupApp;

  private ProviderApp providerApp;
  private ProviderConf providerConf;

  private MobileUserService mobileUserService;
  private DrBuffersService drBuffersService;

  private BoundedLinkedQueue dataQueue;

  private int dataBatchCapacity;
  private int dataBatchThreshold;
  private BoundedLinkedQueue dataBatchQueue;

  private Map mapDrBroker;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public DRWorker( ProviderApp providerApp ) {
    super( "ProviderDRWorker" , 0 , 100 );

    loadMng = LoadManagement.getInstance();
    dbLib = DatabaseLibrary.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();
    lookupApp = LookupApp.getInstance();

    this.providerApp = providerApp;
    this.providerConf = providerApp.getProviderConf();

    mobileUserService = new MobileUserService();
    DLog.debug( lctx , "Created mobile user service" );

    drBuffersService = new DrBuffersService();
    DLog.debug( lctx , "Created dr buffers service" );

    dataQueue = providerApp.getDrQueue();
    DLog.debug( lctx ,
        "Defined data queue , with capacity " + dataQueue.capacity()
            + " msg(s)" );

    dataBatchCapacity = providerConf.getDrDataBatchCapacity();
    dataBatchThreshold = providerConf.getDrDataBatchThreshold();
    dataBatchQueue = new BoundedLinkedQueue( dataBatchCapacity );
    DLog.debug( lctx , "Defined data batch : capacity = " + dataBatchCapacity
        + " msg(s) , threshold = " + dataBatchThreshold
        + " msg(s) , queue.capacity = " + dataBatchQueue.capacity() + " msg(s)" );

    createMapDrBroker();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int threadSize() {
    return (int) opropsApp.getLong( "Provider.DRWorker.TotalWorkers" ,
        providerConf.getDrWorkerThread() );
  }

  public Thread createThread( int workerIdx ) {
    return new DRWorkerThread( workerIdx );
  }

  public void start() {

    // add this class to lookup app as listener
    DLog.debug( lctx , "Add this class to lookup app as listener" );
    lookupApp.addListener( this );

    // start worker(S)
    setupThreads();

  }

  public void stop() {

    // stop worker(s)
    resetThreads();

  }

  public String getName() {
    return "ProviderDrWorker";
  }

  public void receiveLookupMessage( LookupMessage lookupMessage ) {

    if ( lookupMessage == null ) {
      return;
    }

    int clientId = lookupMessage.getClientId();
    String phoneNumber = lookupMessage.getPhone();
    String mobileCcnc = lookupMessage.getNetworkCode();

    DLog.debug( lctx , "Update mobile user : clientId = " + clientId
        + " , phoneNumber = " + phoneNumber + " , mobileCcnc = " + mobileCcnc );

    if ( ( clientId < 1 ) || ( StringUtils.isBlank( phoneNumber ) ) ) {
      return;
    }

    String headerLog = "[MobileUser-" + clientId + "-" + phoneNumber + "] ";

    if ( StringUtils.isBlank( mobileCcnc ) ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile user "
          + ", found blank mobile cc and nc" );
      return;
    }

    if ( !mobileUserService.updateMobileCcnc( clientId , phoneNumber ,
        mobileCcnc ) ) {
      DLog.warning( lctx , headerLog + "Failed to update mobile user "
          + ", found failed to update to mobile user table" );
      return;
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean storeDrMessage( String providerId , String externalMessageId ,
      String externalStatus , Date dateReport , Map optionalParams ,
      String externalParams ) {
    boolean result = false;

    // calculate the latency
    long deltaTime = System.currentTimeMillis();

    // log first
    DLog.debug( lctx ,
        "Store dr message into the queue , with : providerId = " + providerId
            + " , externalMessageId = " + externalMessageId
            + " , externalStatus = " + externalStatus + " , dateReport = "
            + DateTimeFormat.convertToString( dateReport )
            + " , optionalParams = " + optionalParams + " , externalParams = "
            + StringEscapeUtils.escapeJava( externalParams ) );

    // validate parameters
    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , "Failed to store dr message "
          + ", found blank in providerId parameter" );
      return result;
    }
    if ( StringUtils.isBlank( externalMessageId ) ) {
      DLog.warning( lctx , "Failed to store dr message "
          + ", found blank in messageId parameter" );
      return result;
    }
    if ( StringUtils.isBlank( externalStatus ) ) {
      DLog.warning( lctx , "Failed to store dr message "
          + ", found blank in externalStatus parameter" );
      return result;
    }

    // clean parameters
    dateReport = ( dateReport == null ) ? new Date() : dateReport;
    externalParams = ( externalParams == null ) ? "" : externalParams.trim();

    // compose header log
    String headerLog = ProviderMessageCommon.headerLog( providerId + "-"
        + externalMessageId );

    try {

      // build provider message
      ProviderMessage providerMessage = ProviderMessageFactory
          .createProviderDRMessage( "SMS.DR" , providerId , externalMessageId ,
              externalStatus , dateReport , externalParams );
      if ( providerMessage == null ) {
        DLog.warning( lctx , headerLog + "Failed to store dr message "
            + ", found failed to build provider dr message" );
        return result;
      }

      // set optional params
      if ( optionalParams != null ) {
        providerMessage.getOptionalParams().putAll( optionalParams );
      }

      // trap the load of provider's dr
      loadMng.hitDr( providerMessage.getProviderId() , 1 );

      // put latency when found queue almost full ( 90 % )
      Double curQueueSize = QueueUtil.getSizePercentage( dataQueue );
      if ( ( curQueueSize != null ) && ( curQueueSize.doubleValue() > 90 ) ) {
        DLog.warning( lctx , headerLog + "Found dr queue is almost full "
            + "( " + curQueueSize + " % ) , put delay : 1000 ms" );
        Thread.sleep( 1000 );
      }

      // store provider message into the queue
      if ( !dataQueue.offer( providerMessage , 180000 ) ) {
        DLog.warning( lctx , headerLog + "Failed to store dr message "
            + "into the queue , found reached queue timeout : status = "
            + providerMessage.getExternalStatus() );
        return result;
      }

      // calculate the latency
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log
      DLog.debug( lctx , headerLog + "Stored dr message into the queue ( take "
          + deltaTime + " ms ) " );

      // return as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to store dr message "
          + "into the queue , " + e );
    }
    return result;
  }

  public BoundedLinkedQueue getDataBatchQueue() {
    return dataBatchQueue;
  }

  public void clean() {
    persistToDrBufferTable( "" , null , true );
  }

  public boolean processReportEachBroker( ProviderMessage providerMessage ) {
    boolean result = false;
    if ( providerMessage == null ) {
      return result;
    }
    String headerLog = ProviderMessageCommon.headerLog( providerMessage );
    TProvider providerOut = ProviderUtil.resolveProvider( headerLog ,
        providerMessage.getProviderId() );
    if ( providerOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to do broker process report "
          + ", found invalid provider" );
      return result;
    }
    Broker broker = (Broker) mapDrBroker.get( providerOut.getProviderId() );
    if ( broker != null ) {
      DLog.debug( lctx , headerLog + "Processed report broker : providerId = "
          + broker.getProviderId() + " , class = " + broker.getClassName() );
      broker.processReport( providerMessage );
    }
    result = true;
    return result;
  }

  public boolean lookupSubscriber( String messageId , int clientId ,
      int eventId , String phoneNumber , String sendProvider ) {
    boolean result = false;
    if ( messageId == null ) {
      return result;
    }
    String headerLog = ProviderMessageCommon.headerLog( messageId );
    try {

      String lookupProvider = opropsApp.getString(
          "ProviderAgent.MapLookup.".concat( sendProvider ) , null );
      if ( StringUtils.isBlank( lookupProvider ) ) {
        DLog.warning( lctx , headerLog + "Failed to lookup subscriber "
            + ", found failed to find lookup provider based on "
            + "send provider = " + sendProvider );
        return result;
      }

      DLog.debug( lctx , messageId + "Lookup subscriber : clientId = "
          + clientId + " , eventId = " + eventId + " , phoneNumber = "
          + phoneNumber + " , sendProvider = " + sendProvider );

      LookupMessage lookupMessage = lookupApp.sendMessage( messageId ,
          clientId , eventId , phoneNumber , lookupProvider );

      if ( ( lookupMessage == null ) || ( !lookupMessage.isQueued() ) ) {
        DLog.warning( lctx , headerLog + "Failed to send lookup number "
            + ", found failed to send it thru lookup module " );
      }

      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to lookup subscriber , " + e );
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean createMapDrBroker() {
    boolean result = false;
    DLog.debug( lctx , "Trying to create map dr broker(s)" );

    if ( providerConf == null ) {
      DLog.warning( lctx , "Failed to create map dr broker(s) "
          + ", found null provider conf" );
      return result;
    }
    Map providerDrBrokerConfs = providerConf.getProviderDrBrokerConfs();
    if ( providerDrBrokerConfs == null ) {
      DLog.warning( lctx , "Failed to create map dr broker(s) "
          + ", found null provider dr broker confs" );
      return result;
    }

    mapDrBroker = new HashMap();

    Iterator iter = providerDrBrokerConfs.keySet().iterator();
    while ( iter.hasNext() ) {
      ProviderBrokerConf providerBrokerConf = (ProviderBrokerConf) providerDrBrokerConfs
          .get( (String) iter.next() );
      if ( providerBrokerConf == null ) {
        continue;
      }
      String providerId = providerBrokerConf.getProviderId();
      if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
        continue;
      }
      if ( mapDrBroker.get( providerId ) != null ) {
        continue;
      }
      Broker broker = BrokerFactory.createBroker( null , this , providerConf ,
          providerBrokerConf );
      if ( broker == null ) {
        continue;
      }
      mapDrBroker.put( broker.getProviderId() , broker );
    }

    DLog.debug( lctx , "Successfully created " + mapDrBroker.size()
        + " map dr broker(s) : " + mapDrBroker.keySet() );
    result = true;
    return result;
  }

  private void processDrMessage( ProviderMessage providerMessage ) {
    long deltaTime = System.currentTimeMillis();

    // get providerId and externalMessageId
    String providerId = providerMessage.getProviderId();
    String externalMessageId = providerMessage.getExternalMessageId();

    // validate must be params
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to process dr message "
          + ", found empty providerId" );
      return;
    }
    if ( ( externalMessageId == null ) || ( externalMessageId.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to process dr message "
          + ", found empty externalMessageId" );
      return;
    }

    // compose headerLog
    String headerLog = ProviderCommon.headerLog( providerId );
    headerLog += ProviderMessageCommon.headerLog( externalMessageId );

    // status map
    updateStatus( headerLog , providerMessage );

    // write to cdr
    createTicket( headerLog , providerMessage );

    // trap dr load status
    trapDrLoadStatus( headerLog , providerMessage );

    // process status
    processStatus( headerLog , providerMessage );

    // persist to dr buffer table
    persistToDrBufferTable( headerLog , providerMessage , false );

    // log and calculate the latency process
    deltaTime = System.currentTimeMillis() - deltaTime;
    if ( deltaTime > 500 ) {
      DLog.warning( lctx , headerLog + "Found delay to proccess "
          + "dr message , take = " + deltaTime + " ms " );
    }

  }

  private void updateStatus( String headerLog , ProviderMessage message ) {

    // get parameter needs
    String providerId = message.getProviderId();
    String externalMessageId = message.getExternalMessageId();
    String externalStatus = message.getExternalStatus();

    // validate parameter needs
    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , headerLog + "Failed to map the dr status "
          + ", found empty provider id" );
      return;
    }

    if ( StringUtils.isBlank( externalMessageId ) ) {
      DLog.warning( lctx , headerLog + "Failed to map the dr status "
          + ", found empty external message id" );
      return;
    }

    if ( StringUtils.isBlank( externalStatus ) ) {
      DLog.warning( lctx , headerLog + "Failed to map the dr status "
          + ", found empty external status" );
      return;
    }

    // first , set default parameters from provider conf
    message.setInternalStatus( providerConf.getDefaultInternalStatus() );
    message.setPriority( DRCommon.getDrProcessPriority( providerConf ) );

    // get dn status map from input parameters
    TDnStatusMap dnStatusMapOut = DnStatusMapCommon.getDnStatusMap( providerId ,
        externalStatus );
    if ( dnStatusMapOut == null ) {
      DLog.warning(
          lctx ,
          headerLog
              + "Can not find the map for external status "
              + externalStatus
              + " , will use default values from provider conf : internalStatus = "
              + message.getInternalStatus() + " , priority = "
              + message.getPriority() );
      return;
    }

    // compose output parameters based on dn status map result
    message.setInternalStatus( dnStatusMapOut.getInternalStatus() );
    if ( dnStatusMapOut.getShutdown() > 0 ) {
      ProviderMessageOptionalMapParamUtils.setCommandShutdown( message );
      message.setPriority( DRCommon.getDrProcessPriority( providerConf ,
          ProviderConf.DR_PROCESSPRIORITY_SHUTDOWN_NAME ) );
      DLog.debug( lctx , headerLog + "Found shutdown enable "
          + "for this particular message" );
    }
    if ( dnStatusMapOut.getRetry() > 0 ) {
      ProviderMessageOptionalMapParamUtils.setCommandRetry( message );
      message.setPriority( DRCommon.getDrProcessPriority( providerConf ,
          ProviderConf.DR_PROCESSPRIORITY_RETRY_NAME ) );
      DLog.debug( lctx , headerLog + "Found retry enable "
          + "for this particular message" );
    }
    if ( dnStatusMapOut.getIgnored() > 0 ) {
      ProviderMessageOptionalMapParamUtils.setCommandIgnored( message );
      message.setPriority( DRCommon.getDrProcessPriority( providerConf ,
          ProviderConf.DR_PROCESSPRIORITY_IGNORED_NAME ) );
      DLog.debug( lctx , headerLog + "Found ignored status "
          + "for this particular message" );
    }

    // log it
    DLog.debug( lctx , headerLog + "Resolved dr status map : code = "
        + externalStatus + " -> " + message.getInternalStatus()
        + " , description = " + dnStatusMapOut.getDescription()
        + " , priority = " + message.getPriority() + " , optionalParams = "
        + message.getOptionalParams() );

  }

  private void createTicket( String headerLog , ProviderMessage providerMessage ) {
    providerApp.createDRTicket( providerMessage );
  }

  private void trapDrLoadStatus( String headerLog ,
      ProviderMessage providerMessage ) {
    if ( loadMng == null ) {
      return;
    }
    if ( providerMessage == null ) {
      return;
    }
    String hdrProf = LoadManagement.HDRPROF_PROVIDER;
    String conType = LoadManagement.CONTYPE_SMSDR;
    StringBuffer sbName = new StringBuffer();
    sbName.append( providerMessage.getProviderId() );
    sbName.append( "-" );
    sbName.append( providerMessage.getInternalStatus() );
    loadMng.hit( hdrProf , conType , sbName.toString() , 1 , true , true );
  }

  private void processStatus( String headerLog , ProviderMessage providerMessage ) {

    // nothing to do yet ...

  }

  private void persistToDrBufferTable( String headerLog ,
      ProviderMessage providerMessage , boolean clean ) {

    // trap delta time

    long deltaTime = System.currentTimeMillis();

    // store provider message to batch queue ( if found any )

    if ( ( !clean ) && ( providerMessage != null ) ) {
      storeMessageInDataBatchQueue( headerLog , providerMessage , true );
    }

    // read and verify the queue size

    int queueSize = dataBatchQueue.size();
    if ( clean ) {
      if ( queueSize < 1 ) {
        return;
      }
    } else {
      if ( queueSize < dataBatchThreshold ) {
        return;
      }
    }

    // do the cleaning records from the queue

    List listDrBufferBeans = new ArrayList();
    for ( int i = 0 ; i < queueSize ; i++ ) {
      try {
        // read provider message from the queue
        providerMessage = (ProviderMessage) dataBatchQueue.poll( 5000 );
        if ( providerMessage == null ) {
          continue;
        }
        // convert provider message to dr buffer bean
        DrBufferBean drBufferBean = DrBufferBeanTransformUtils
            .transformProviderMessageToDrBufferBean( providerMessage );
        if ( drBufferBean == null ) {
          continue;
        }
        // store dr buffer bean into the list
        listDrBufferBeans.add( drBufferBean );
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to take provider message "
            + "from data batch queue , " + e );
      }
    }

    // insert records into dn buffer table

    int totalInserted = 0;
    if ( listDrBufferBeans.size() > 0 ) {
      totalInserted = drBuffersService.insert( listDrBufferBeans );
    }

    // log it

    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , "Moved batch dn records from the batch queue "
        + "into dn buffer table , queue = " + queueSize + " bean(s) , list = "
        + listDrBufferBeans.size() + " message(s) , inserted = "
        + totalInserted + " record(s) , took " + deltaTime + " ms" );

  }

  private boolean storeMessageInDataBatchQueue( String headerLog ,
      ProviderMessage providerMessage , boolean filter ) {
    boolean result = false;
    if ( providerMessage == null ) {
      DLog.warning( lctx , headerLog + "Can not put in the data batch queue "
          + ", found null dr message" );
      return result;
    }

    // need to filter ?
    if ( filter ) {

      // check when the message need to be ignored
      if ( ProviderMessageOptionalMapParamUtils.isCommandIgnored(
          providerMessage , true ) ) {
        DLog.debug( lctx , headerLog + "Found ignored command "
            + "in this dr message , no need to store into the batch queue" );
        result = true;
        return result;
      }

    }

    // store message into data batch queue , and ready to persist
    try {
      dataBatchQueue.put( providerMessage );
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to put the message in "
          + "the Data Batch Queue , " + e );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class DRWorkerThread extends Thread {

    private int idx;

    public DRWorkerThread( int idx ) {
      super( "ProviderDRWorkerThread-" + idx );
      this.idx = idx;
    }

    public void run() {
      ProviderMessage providerMessage = null;
      long counter = 0 , delay10ms = 10;
      DLog.debug( lctx , "Thread started" );
      while ( isThreadActive( idx ) ) {

        counter = counter + delay10ms;
        try {
          Thread.sleep( delay10ms );
        } catch ( Exception e ) {
        }
        if ( counter < opropsApp.getLong( "Provider.DRWorker.TimeSleep" ,
            providerConf.getDrWorkerSleep() ) ) {
          continue;
        }
        counter = 0;

        try {
          // fetch dr message from dr queue
          providerMessage = (ProviderMessage) dataQueue.poll( 5000 );
          if ( providerMessage == null ) {
            continue;
          }
          // process dr message
          processDrMessage( providerMessage );
        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to process dr provider message "
              + "from the dr queue , " + e );
        }

      } // while ( isThreadActive( idx ) )
      DLog.debug( lctx , "Thread stopped" );
    }

  }

}
