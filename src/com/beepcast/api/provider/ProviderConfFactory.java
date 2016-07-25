package com.beepcast.api.provider;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class ProviderConfFactory {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProviderConfFactory" );

  static final GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static ProviderConf createProviderConf( String propertyFileProvider ) {
    ProviderConf providerConf = new ProviderConf();

    if ( StringUtils.isBlank( propertyFileProvider ) ) {
      return providerConf;
    }

    DLog.debug( lctx , "Loading from property = " + propertyFileProvider );

    Element element = globalEnv.getElement( ProviderConf.class.getName() ,
        propertyFileProvider );
    if ( element != null ) {
      boolean result = validateTag( element );
      if ( result ) {
        extractElement( element , providerConf );
      }
    }

    return providerConf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static boolean validateTag( Element element ) {
    boolean result = false;

    if ( element == null ) {
      DLog.warning( lctx , "Found empty in element xml" );
      return result;
    }

    Node node = TreeUtil.first( element , "provider" );
    if ( node == null ) {
      DLog.warning( lctx , "Can not find root tag <provider>" );
      return result;
    }

    result = true;
    return result;
  }

  private static boolean extractElement( Element element ,
      ProviderConf providerConf ) {
    boolean result = false;

    String stemp;

    Node nodeProvider = TreeUtil.first( element , "provider" );
    if ( nodeProvider == null ) {
      DLog.warning( lctx , "Can not find tag of provider , using default ." );
      return result;
    }

    stemp = TreeUtil.getAttribute( nodeProvider , "debug" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerConf.setDebug( stemp.equalsIgnoreCase( "true" ) );
      DLog.debug( lctx , "Defined debug = " + providerConf.isDebug() );
    }

    Node nodeDeliveryNotification = TreeUtil.first( nodeProvider ,
        "deliveryNotification" );
    extractNodeDeliveryNotification( nodeDeliveryNotification , providerConf );

    Node nodeMessageOriginator = TreeUtil.first( nodeProvider ,
        "messageOriginator" );
    extractNodeMessageOriginator( nodeMessageOriginator , providerConf );

    Node nodeMessageTerminated = TreeUtil.first( nodeProvider ,
        "messageTerminated" );
    extractNodeMessageTerminated( nodeMessageTerminated , providerConf );

    Node nodeManagement = TreeUtil.first( nodeProvider , "management" );
    extractNodeManagement( nodeManagement , providerConf );

    result = true;
    return result;
  }

  private static boolean extractNodeDeliveryNotification(
      Node nodeDeliveryNotification , ProviderConf providerConf ) {
    boolean result = false;
    if ( nodeDeliveryNotification == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodeDeliveryNotification , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setDrSizeQueue( itemp );
        DLog.debug( lctx ,
            "Defined dr queue size = " + providerConf.getDrSizeQueue()
                + " msg(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load drSizeQueue  "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeDeliveryNotification , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setDrWorkerThread( itemp );
        DLog.debug( lctx ,
            "Defined dr worker thread = " + providerConf.getDrWorkerThread()
                + " worker(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load minDRWorkerThread "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeDeliveryNotification , "sleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setDrWorkerSleep( itemp );
        DLog.debug( lctx ,
            "Defined dr worker sleep = " + providerConf.getDrWorkerSleep()
                + " millis" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load drWorkerSleep "
            + "in the conf File" );
      }
    }

    Node nodePriority = TreeUtil.first( nodeDeliveryNotification , "priority" );
    extractDrNodePriority( nodePriority , providerConf );

    Node nodeDataBatch = TreeUtil
        .first( nodeDeliveryNotification , "dataBatch" );
    if ( nodeDataBatch != null ) {
      stemp = TreeUtil.getAttribute( nodeDataBatch , "capacity" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setDrDataBatchCapacity( itemp );
          DLog.debug(
              lctx ,
              "Defined dr data batch capacity = "
                  + providerConf.getDrDataBatchCapacity() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load drDataBatchCapacity "
              + "in the conf File" );
        }
      }
      stemp = TreeUtil.getAttribute( nodeDataBatch , "threshold" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setDrDataBatchThreshold( itemp );
          DLog.debug( lctx , "Defined dr data batch threshold = "
              + providerConf.getDrDataBatchThreshold() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load drDataBatchThreshold "
              + "in the conf File" );
        }
      }
    }

    Node nodeDefault = TreeUtil.first( nodeDeliveryNotification , "default" );
    if ( nodeDefault != null ) {
      stemp = TreeUtil.getAttribute( nodeDefault , "internalStatus" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        providerConf.setDefaultInternalStatus( stemp );
        DLog.debug( lctx , "Defined dr default internal status = "
            + providerConf.getDefaultInternalStatus() );
      }
      stemp = TreeUtil.getAttribute( nodeDefault , "externalStatus" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        providerConf.setDefaultExternalStatus( stemp );
        DLog.debug( lctx , "Defined dr default external status = "
            + providerConf.getDefaultExternalStatus() );
      }
    }

    extractNodeProviderBrokers( nodeDeliveryNotification ,
        providerConf.getProviderDrBrokerConfs() );
    DLog.debug( lctx , "Successfully added "
        + providerConf.getProviderDrBrokerConfs().size()
        + " provider dr broker conf(s) : "
        + providerConf.getProviderDrBrokerConfs().keySet() );

    result = true;
    return result;
  }

  private static boolean extractDrNodePriority( Node nodePriority ,
      ProviderConf providerConf ) {
    boolean result = false;
    if ( nodePriority == null ) {
      return result;
    }

    Map drProcessPriority = providerConf.getDrProcessPriority();
    if ( drProcessPriority == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodePriority , "default" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        drProcessPriority.put( ProviderConf.DR_PROCESSPRIORITY_DEFAULT_NAME ,
            new Integer( itemp ) );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load priority "
            + "default in the conf File" );
      }
    }

    Node nodeStatus = TreeUtil.first( nodePriority , "status" );
    while ( nodeStatus != null ) {
      String name = TreeUtil.getAttribute( nodeStatus , "name" );

      Integer value = null;
      try {
        stemp = TreeUtil.getAttribute( nodeStatus , "value" );
        itemp = Integer.parseInt( stemp );
        value = new Integer( itemp );
      } catch ( NumberFormatException e ) {
      }

      if ( StringUtils.isBlank( name ) || ( value == null ) ) {
        nodeStatus = TreeUtil.next( nodeStatus , "status" );
        continue;
      }

      boolean valid = false;
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_DEFAULT_NAME );
      }
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_SHUTDOWN_NAME );
      }
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_RETRY_NAME );
      }
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_DELIVERED_NAME );
      }
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_SUBMITTED_NAME );
      }
      if ( !valid ) {
        valid = name.equals( ProviderConf.DR_PROCESSPRIORITY_IGNORED_NAME );
      }
      if ( valid ) {
        drProcessPriority.put( name , value );
        DLog.debug( lctx , "Defined dn priority : " + name + " = " + value );
      } else {
        DLog.warning( lctx , "Found anonymous dn priority conf : " + name
            + " = " + value );
      }

      nodeStatus = TreeUtil.next( nodeStatus , "status" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeMessageOriginator(
      Node nodeMessageOriginator , ProviderConf providerConf ) {
    boolean result = false;
    if ( nodeMessageOriginator == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodeMessageOriginator , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMoSizeQueue( itemp );
        DLog.debug( lctx ,
            "Defined mo queue size = " + providerConf.getMoSizeQueue()
                + " msg(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load moSizeQueue  "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeMessageOriginator , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMoWorkerThread( itemp );
        DLog.debug( lctx ,
            "Defined mo worker thread = " + providerConf.getMoWorkerThread()
                + " worker(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load moWorkerThread "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeMessageOriginator , "sleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMoWorkerSleep( itemp );
        DLog.debug( lctx ,
            "Defined mo sleep = " + providerConf.getMoWorkerSleep() + " millis" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load moWorkerSleep "
            + "in the conf File" );
      }
    }

    Node nodeDataBatch = TreeUtil.first( nodeMessageOriginator , "dataBatch" );
    if ( nodeDataBatch != null ) {
      stemp = TreeUtil.getAttribute( nodeDataBatch , "capacity" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setMoDataBatchCapacity( itemp );
          DLog.debug(
              lctx ,
              "Defined mo data batch capacity = "
                  + providerConf.getMoDataBatchCapacity() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load "
              + "moDataBatchCapacity in the conf File" );
        }
      }
      stemp = TreeUtil.getAttribute( nodeDataBatch , "threshold" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setMoDataBatchThreshold( itemp );
          DLog.debug( lctx , "Defined mo data batch threshold = "
              + providerConf.getMoDataBatchThreshold() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load "
              + "moDataBatchThreshold in the conf File" );
        }
      }
    }

    extractNodeProviderBrokers( nodeMessageOriginator ,
        providerConf.getProviderMoBrokerConfs() );
    DLog.debug( lctx , "Successfully added "
        + providerConf.getProviderMoBrokerConfs().size()
        + " provider mo broker conf(s) : "
        + providerConf.getProviderMoBrokerConfs().keySet() );

    result = true;
    return result;
  }

  private static boolean extractNodeProviderBrokers( Node node ,
      Map mapProviderBrokerConfs ) {
    boolean result = false;
    if ( node == null ) {
      return result;
    }
    if ( mapProviderBrokerConfs == null ) {
      return result;
    }

    Node nodeProviderBroker = TreeUtil.first( node , "providerBroker" );
    while ( nodeProviderBroker != null ) {

      String className = TreeUtil.getAttribute( nodeProviderBroker , "class" );
      String providerId = TreeUtil.getAttribute( nodeProviderBroker ,
          "providerId" );
      String enabled = TreeUtil.getAttribute( nodeProviderBroker , "enabled" );

      ProviderBrokerConf pbConf = new ProviderBrokerConf();
      pbConf.setClassName( className );
      pbConf.setProviderId( providerId );
      pbConf.setEnabled( ( enabled != null )
          && ( enabled.equalsIgnoreCase( "true" ) ) );

      if ( ( pbConf != null ) && ( pbConf.isEnabled() ) ) {
        mapProviderBrokerConfs.put( pbConf.getProviderId() , pbConf );
      } // if ( ( pbConf != null ) && ( pbConf.isEnabled() ) )

      Node nodeMapParams = TreeUtil.first( nodeProviderBroker , "mapParams" );
      if ( nodeMapParams != null ) {
        Node nodeMapParam = TreeUtil.first( nodeMapParams , "mapParam" );
        while ( nodeMapParam != null ) {
          String mapParamName = TreeUtil.getAttribute( nodeMapParam , "name" );
          String mapParamValue = TreeUtil.getAttribute( nodeMapParam , "value" );
          if ( ( mapParamName != null ) && ( mapParamValue != null ) ) {
            pbConf.addMapParam( mapParamName , mapParamValue );
          } // if ( ( mapParamName != null ) && ( mapParamValue != null ) )
          nodeMapParam = TreeUtil.next( nodeMapParam , "mapParam" );
        } // while ( nodeMapParam != null )
      } // if ( nodeMapParams != null )

      nodeProviderBroker = TreeUtil
          .next( nodeProviderBroker , "providerBroker" );

    } // while ( nodeProviderBroker != null )

    result = true;
    return result;
  }

  private static boolean extractNodeMessageTerminated(
      Node nodeMessageTerminated , ProviderConf providerConf ) {
    boolean result = false;
    if ( nodeMessageTerminated == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodeMessageTerminated , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMtSizeQueue( itemp );
        DLog.debug( lctx ,
            "Defined mt queue size = " + providerConf.getMtSizeQueue()
                + " msg(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load mtSizeQueue  "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeMessageTerminated , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMtWorkerThread( itemp );
        DLog.debug( lctx ,
            "Defined mt worker thread = " + providerConf.getMtWorkerThread()
                + " worker(s)" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load mtWorkerThread "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeMessageTerminated , "sleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setMtWorkerSleep( itemp );
        DLog.debug( lctx ,
            "Defined mt worker sleep = " + providerConf.getMtWorkerSleep()
                + " millis" );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load mtWorkerSleep "
            + "in the conf File" );
      }
    }

    Node nodeDataBatch = TreeUtil.first( nodeMessageTerminated , "dataBatch" );
    if ( nodeDataBatch != null ) {
      stemp = TreeUtil.getAttribute( nodeDataBatch , "capacity" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setMtDataBatchCapacity( itemp );
          DLog.debug(
              lctx ,
              "Defined mt data batch capacity = "
                  + providerConf.getMtDataBatchCapacity() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load "
              + "mtDataBatchCapacity in the conf File" );
        }
      }
      stemp = TreeUtil.getAttribute( nodeDataBatch , "threshold" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          providerConf.setMtDataBatchThreshold( itemp );
          DLog.debug( lctx , "Defined mt data batch threshold = "
              + providerConf.getMtDataBatchThreshold() + " msg(s)" );
        } catch ( NumberFormatException e ) {
          DLog.warning( lctx , "Failed to load "
              + "mtDataBatchThreshold in the conf File" );
        }
      }
    }

    extractNodeProviderAgents( nodeMessageTerminated ,
        providerConf.getProviderAgentConfs() );
    DLog.debug( lctx , "Successfully added "
        + providerConf.getProviderAgentConfs().size()
        + " provider agent conf(s) : "
        + providerConf.getProviderAgentConfs().keySet() );

    result = true;
    return result;
  }

  private static boolean extractNodeProviderAgents( Node nodeMessageTerminated ,
      Map mapProviderAgentConfs ) {
    boolean result = false;
    if ( nodeMessageTerminated == null ) {
      return result;
    }
    if ( mapProviderAgentConfs == null ) {
      return result;
    }

    Node nodeProviderAgent = TreeUtil.first( nodeMessageTerminated ,
        "providerAgent" );
    while ( nodeProviderAgent != null ) {

      String className = TreeUtil.getAttribute( nodeProviderAgent , "class" );
      String providerId = TreeUtil.getAttribute( nodeProviderAgent ,
          "providerId" );
      String enabled = TreeUtil.getAttribute( nodeProviderAgent , "enabled" );

      ProviderAgentConf paConf = new ProviderAgentConf();
      paConf.setClassName( className );
      paConf.setProviderId( providerId );
      paConf.setEnabled( ( enabled != null )
          && ( enabled.equalsIgnoreCase( "true" ) ) );

      try {
        paConf.setQueueSize( Integer.parseInt( TreeUtil.getAttribute(
            nodeProviderAgent , "queueSize" ) ) );
      } catch ( Exception e ) {
        paConf.setQueueSize( 100 );
      }
      try {
        paConf.setWorkerSize( Integer.parseInt( TreeUtil.getAttribute(
            nodeProviderAgent , "worker" ) ) );
      } catch ( Exception e ) {
        paConf.setWorkerSize( 1 );
      }
      try {
        paConf.setWorkerSleep( Integer.parseInt( TreeUtil.getAttribute(
            nodeProviderAgent , "sleep" ) ) );
      } catch ( Exception e ) {
        paConf.setWorkerSleep( 1000 );
      }

      String headerLog = ProviderCommon.headerLog( providerId );

      Node nodeConnections = TreeUtil.first( nodeProviderAgent , "Connections" );
      extractNodeConnections( headerLog , nodeConnections , paConf );

      Node nodeConnectionParams = TreeUtil.first( nodeProviderAgent ,
          "connectionParams" );
      extractNodeConnectionParams( headerLog , nodeConnectionParams , paConf );

      Node nodeScriptParams = TreeUtil.first( nodeProviderAgent ,
          "scriptParams" );
      extractNodeScriptParams( headerLog , nodeScriptParams , paConf );

      Node nodeMessageParams = TreeUtil.first( nodeProviderAgent ,
          "messageParams" );
      extractNodeMessageParams( headerLog , nodeMessageParams , paConf );

      if ( ( paConf != null ) && ( paConf.isEnabled() ) ) {
        mapProviderAgentConfs.put( paConf.getProviderId() , paConf );
      }

      nodeProviderAgent = TreeUtil.next( nodeProviderAgent , "providerAgent" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeConnections( String headerLog ,
      Node nodeConnections , ProviderAgentConf providerAgentConf ) {
    boolean result = false;
    if ( nodeConnections == null ) {
      return result;
    }
    if ( providerAgentConf == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodeConnections , "enableErrorCheck" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerAgentConf.setEnableErrorCheck( stemp.equalsIgnoreCase( "true" ) );
    }
    stemp = TreeUtil.getAttribute( nodeConnections , "maxErrorTolerant" );
    itemp = 0;
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
      } catch ( NumberFormatException e ) {
      }
    }
    providerAgentConf.setMaxErrorTolerant( itemp );

    DLog.debug( lctx , headerLog + "Set enableErrorCheck = "
        + providerAgentConf.isEnableErrorCheck() + " , maxErrorTolerant = "
        + providerAgentConf.getMaxErrorTolerant() );

    String protocol;
    String remoteHost;
    String remotePort;
    String remotePath;
    Node nodeConnection = TreeUtil.first( nodeConnections , "Connection" );
    while ( nodeConnection != null ) {
      ConnectionConf connectionConf = new ConnectionConf();
      protocol = TreeUtil.getAttribute( nodeConnection , "protocol" );
      if ( ( protocol != null ) && ( !protocol.equals( "" ) ) ) {
        connectionConf.setProtocol( protocol );
      }
      remoteHost = TreeUtil.getAttribute( nodeConnection , "remoteHost" );
      if ( ( remoteHost != null ) && ( !remoteHost.equals( "" ) ) ) {
        connectionConf.setRemoteHost( remoteHost );
      }
      remotePort = TreeUtil.getAttribute( nodeConnection , "remotePort" );
      if ( ( remotePort != null ) && ( !remotePort.equals( "" ) ) ) {
        connectionConf.setRemotePort( remotePort );
      }
      remotePath = TreeUtil.getAttribute( nodeConnection , "remotePath" );
      if ( ( remotePath != null ) && ( !remotePath.equals( "" ) ) ) {
        connectionConf.setRemotePath( remotePath );
      }
      providerAgentConf.addConnectionConf( connectionConf );
      DLog.debug( lctx , headerLog + "Added " + connectionConf );
      nodeConnection = TreeUtil.next( nodeConnection , "Connection" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeConnectionParams( String headerLog ,
      Node nodeConnectionParams , ProviderAgentConf providerAgentConf ) {
    boolean result = false;
    if ( nodeConnectionParams == null ) {
      return result;
    }
    if ( providerAgentConf == null ) {
      return result;
    }

    Node nodeConnectionParam = TreeUtil.first( nodeConnectionParams ,
        "connectionParam" );
    String name , value;
    while ( nodeConnectionParam != null ) {
      name = TreeUtil.getAttribute( nodeConnectionParam , "name" );
      value = TreeUtil.getAttribute( nodeConnectionParam , "value" );
      if ( ( name == null ) || ( name.equals( "" ) ) ) {
        continue;
      }
      if ( ( value == null ) || ( value.equals( "" ) ) ) {
        continue;
      }
      providerAgentConf.addConnectionParam( name , value );
      DLog.debug( lctx , headerLog + "Added connection param , name = " + name
          + " , value = " + value );
      nodeConnectionParam = TreeUtil.next( nodeConnectionParam ,
          "connectionParam" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeScriptParams( String headerLog ,
      Node nodeScriptParams , ProviderAgentConf providerAgentConf ) {
    boolean result = false;
    if ( nodeScriptParams == null ) {
      return result;
    }
    if ( providerAgentConf == null ) {
      return result;
    }

    Node nodeScriptParam = TreeUtil.first( nodeScriptParams , "scriptParam" );
    String name , value;
    while ( nodeScriptParam != null ) {
      name = TreeUtil.getAttribute( nodeScriptParam , "name" );
      value = TreeUtil.getAttribute( nodeScriptParam , "value" );
      if ( !StringUtils.isBlank( name ) && !StringUtils.isBlank( value ) ) {
        providerAgentConf.addScriptParam( name , value );
        DLog.debug( lctx , headerLog + "Added script param , name = " + name
            + " , value = " + value );
      }
      nodeScriptParam = TreeUtil.next( nodeScriptParam , "scriptParam" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeMessageParams( String headerLog ,
      Node nodeMessageParams , ProviderAgentConf providerAgentConf ) {
    boolean result = false;
    if ( nodeMessageParams == null ) {
      return result;
    }
    if ( providerAgentConf == null ) {
      return result;
    }

    Node nodeMessageParam = TreeUtil.first( nodeMessageParams , "messageParam" );
    String name , value;
    while ( nodeMessageParam != null ) {
      name = TreeUtil.getAttribute( nodeMessageParam , "name" );
      value = TreeUtil.getAttribute( nodeMessageParam , "value" );
      if ( !StringUtils.isBlank( name ) && !StringUtils.isBlank( value ) ) {
        providerAgentConf.addMessageParam( name , value );
        DLog.debug( lctx , headerLog + "Added message param , name = " + name
            + " , value = " + value );
      }
      nodeMessageParam = TreeUtil.next( nodeMessageParam , "messageParam" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeManagement( Node nodeManagement ,
      ProviderConf providerConf ) {
    boolean result = false;
    if ( nodeManagement == null ) {
      return result;
    }

    int itemp;
    String stemp;

    stemp = TreeUtil.getAttribute( nodeManagement , "period" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setManagementPeriod( itemp );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load managementPeriod "
            + "in the conf File" );
      }
    }
    stemp = TreeUtil.getAttribute( nodeManagement , "cleanIdle" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        providerConf.setManagementCleanIdle( itemp );
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to load managementCleanIdle "
            + "in the conf File" );
      }
    }

    result = true;
    return result;
  }

}
