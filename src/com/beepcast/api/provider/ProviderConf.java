package com.beepcast.api.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderConf {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProviderConf" );

  public static final String DR_PROCESSPRIORITY_DEFAULT_NAME = "default";
  public static final String DR_PROCESSPRIORITY_SHUTDOWN_NAME = "shutdown";
  public static final String DR_PROCESSPRIORITY_RETRY_NAME = "retry";
  public static final String DR_PROCESSPRIORITY_DELIVERED_NAME = "delivered";
  public static final String DR_PROCESSPRIORITY_SUBMITTED_NAME = "submitted";
  public static final String DR_PROCESSPRIORITY_IGNORED_NAME = "ignored";

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

  private boolean debug;

  private int drSizeQueue;
  private int drWorkerSleep;
  private int drWorkerThread;
  private int drDataBatchCapacity;
  private int drDataBatchThreshold;
  private Map drProcessPriority;

  private String defaultInternalStatus;
  private String defaultExternalStatus;

  private Map providerDrBrokerConfs;

  private int moSizeQueue;
  private int moWorkerSleep;
  private int moWorkerThread;
  private int moDataBatchCapacity;
  private int moDataBatchThreshold;

  private Map providerMoBrokerConfs;

  private int mtSizeQueue;
  private int mtWorkerSleep;
  private int mtWorkerThread;
  private int mtDataBatchCapacity;
  private int mtDataBatchThreshold;

  private Map providerAgentConfs;

  private int minManagementThread;
  private int managementPeriod;
  private int managementCleanIdle;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProviderConf() {
    init();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void init() {

    debug = false;

    drSizeQueue = 1000;
    drWorkerSleep = 25;
    drWorkerThread = 1;
    drDataBatchCapacity = 100;
    drDataBatchThreshold = 70;

    drProcessPriority = new HashMap();

    defaultInternalStatus = "";
    defaultExternalStatus = "";

    providerDrBrokerConfs = new HashMap();

    moSizeQueue = 1000;
    moWorkerSleep = 25;
    moWorkerThread = 1;
    moDataBatchCapacity = 100;
    moDataBatchThreshold = 70;

    providerMoBrokerConfs = new HashMap();

    mtSizeQueue = 1000;
    mtWorkerSleep = 25;
    mtWorkerThread = 1;
    mtDataBatchCapacity = 100;
    mtDataBatchThreshold = 70;

    providerAgentConfs = new HashMap();

    minManagementThread = 1;
    managementPeriod = 5000;
    managementCleanIdle = 10;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProviderBrokerConf getProviderDrBrokerConf( String providerId ) {
    ProviderBrokerConf providerBrokerConf = null;
    if ( StringUtils.isBlank( providerId ) ) {
      return providerBrokerConf;
    }
    providerBrokerConf = (ProviderBrokerConf) providerDrBrokerConfs
        .get( providerId );
    return providerBrokerConf;
  }

  public ProviderBrokerConf getProviderMoBrokerConf( String providerId ) {
    ProviderBrokerConf providerBrokerConf = null;
    if ( StringUtils.isBlank( providerId ) ) {
      return providerBrokerConf;
    }
    providerBrokerConf = (ProviderBrokerConf) providerMoBrokerConfs
        .get( providerId );
    return providerBrokerConf;
  }

  public ProviderAgentConf getProviderAgentConf( String providerId ) {
    ProviderAgentConf providerAgentConf = null;
    if ( StringUtils.isBlank( providerId ) ) {
      return providerAgentConf;
    }
    providerAgentConf = (ProviderAgentConf) providerAgentConfs.get( providerId );
    return providerAgentConf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isDebug() {
    return debug;
  }

  public void setDebug( boolean debug ) {
    this.debug = debug;
  }

  public int getDrSizeQueue() {
    return drSizeQueue;
  }

  public void setDrSizeQueue( int drSizeQueue ) {
    this.drSizeQueue = drSizeQueue;
  }

  public int getDrWorkerSleep() {
    return drWorkerSleep;
  }

  public void setDrWorkerSleep( int drWorkerSleep ) {
    this.drWorkerSleep = drWorkerSleep;
  }

  public int getDrWorkerThread() {
    return drWorkerThread;
  }

  public void setDrWorkerThread( int drWorkerThread ) {
    this.drWorkerThread = drWorkerThread;
  }

  public int getDrDataBatchCapacity() {
    return drDataBatchCapacity;
  }

  public void setDrDataBatchCapacity( int drDataBatchCapacity ) {
    this.drDataBatchCapacity = drDataBatchCapacity;
  }

  public int getDrDataBatchThreshold() {
    return drDataBatchThreshold;
  }

  public void setDrDataBatchThreshold( int drDataBatchThreshold ) {
    this.drDataBatchThreshold = drDataBatchThreshold;
  }

  public Map getDrProcessPriority() {
    return drProcessPriority;
  }

  public void setDrProcessPriority( Map drProcessPriority ) {
    this.drProcessPriority = drProcessPriority;
  }

  public String getDefaultInternalStatus() {
    return defaultInternalStatus;
  }

  public void setDefaultInternalStatus( String defaultInternalStatus ) {
    this.defaultInternalStatus = defaultInternalStatus;
  }

  public String getDefaultExternalStatus() {
    return defaultExternalStatus;
  }

  public void setDefaultExternalStatus( String defaultExternalStatus ) {
    this.defaultExternalStatus = defaultExternalStatus;
  }

  public Map getProviderDrBrokerConfs() {
    return providerDrBrokerConfs;
  }

  public int getMoSizeQueue() {
    return moSizeQueue;
  }

  public void setMoSizeQueue( int moSizeQueue ) {
    this.moSizeQueue = moSizeQueue;
  }

  public int getMoWorkerSleep() {
    return moWorkerSleep;
  }

  public void setMoWorkerSleep( int moWorkerSleep ) {
    this.moWorkerSleep = moWorkerSleep;
  }

  public int getMoWorkerThread() {
    return moWorkerThread;
  }

  public void setMoWorkerThread( int moWorkerThread ) {
    this.moWorkerThread = moWorkerThread;
  }

  public int getMoDataBatchCapacity() {
    return moDataBatchCapacity;
  }

  public void setMoDataBatchCapacity( int moDataBatchCapacity ) {
    this.moDataBatchCapacity = moDataBatchCapacity;
  }

  public int getMoDataBatchThreshold() {
    return moDataBatchThreshold;
  }

  public void setMoDataBatchThreshold( int moDataBatchThreshold ) {
    this.moDataBatchThreshold = moDataBatchThreshold;
  }

  public Map getProviderMoBrokerConfs() {
    return providerMoBrokerConfs;
  }

  public int getMtSizeQueue() {
    return mtSizeQueue;
  }

  public void setMtSizeQueue( int mtSizeQueue ) {
    this.mtSizeQueue = mtSizeQueue;
  }

  public int getMtWorkerSleep() {
    return mtWorkerSleep;
  }

  public void setMtWorkerSleep( int mtWorkerSleep ) {
    this.mtWorkerSleep = mtWorkerSleep;
  }

  public int getMtWorkerThread() {
    return mtWorkerThread;
  }

  public void setMtWorkerThread( int mtWorkerThread ) {
    this.mtWorkerThread = mtWorkerThread;
  }

  public int getMtDataBatchCapacity() {
    return mtDataBatchCapacity;
  }

  public void setMtDataBatchCapacity( int mtDataBatchCapacity ) {
    this.mtDataBatchCapacity = mtDataBatchCapacity;
  }

  public int getMtDataBatchThreshold() {
    return mtDataBatchThreshold;
  }

  public void setMtDataBatchThreshold( int mtDataBatchThreshold ) {
    this.mtDataBatchThreshold = mtDataBatchThreshold;
  }

  public int getMinManagementThread() {
    return minManagementThread;
  }

  public void setMinManagementThread( int minManagementThread ) {
    this.minManagementThread = minManagementThread;
  }

  public int getManagementPeriod() {
    return managementPeriod;
  }

  public void setManagementPeriod( int managementPeriod ) {
    this.managementPeriod = managementPeriod;
  }

  public int getManagementCleanIdle() {
    return managementCleanIdle;
  }

  public void setManagementCleanIdle( int managementCleanIdle ) {
    this.managementCleanIdle = managementCleanIdle;
  }

  public Map getProviderAgentConfs() {
    return providerAgentConfs;
  }

}
