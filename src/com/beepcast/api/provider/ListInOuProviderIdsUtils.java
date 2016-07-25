package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.model.provider.ProviderType;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListInOuProviderIdsUtils {

  static final DLogContext lctx = new SimpleContext( "ListInOuProviderIdsUtils" );

  public static ArrayList listOutgoingProviderIds( List listFilterByTypes ,
      boolean filterByWorkers , boolean addMembers , boolean filterBySuspend ,
      boolean debug ) {
    ArrayList listProviderIds = new ArrayList();
    try {

      // delta time
      long deltaTime = System.currentTimeMillis();

      // prepare modules
      ProviderApp providerApp = ProviderApp.getInstance();
      OnlinePropertiesApp opropsApp = OnlinePropertiesApp.getInstance();

      // load active outgoing provider type : modem
      ArrayList listActiveOutgoingModemProviderIds = new ArrayList();
      if ( ( listFilterByTypes == null )
          || ( listFilterByTypes.indexOf( ProviderType.TYPE_MODEM ) > -1 ) ) {
        listActiveOutgoingModemProviderIds.addAll( (ArrayList) ProviderCommon
            .listOutgoingModemProviderIds() );
      }

      // load active outgoing provider type : internal
      ArrayList listActiveOutgoingInternalProviderIds = new ArrayList();
      if ( ( listFilterByTypes == null )
          || ( listFilterByTypes.indexOf( ProviderType.TYPE_INTERNAL ) > -1 ) ) {
        listActiveOutgoingInternalProviderIds
            .addAll( (ArrayList) ProviderCommon
                .listOutgoingInternalProviderIds() );
      }

      // load active outgoing provider type : external
      ArrayList listActiveOutgoingExternalProviderIds = new ArrayList();
      if ( ( listFilterByTypes == null )
          || ( listFilterByTypes.indexOf( ProviderType.TYPE_EXTERNAL ) > -1 ) ) {
        listActiveOutgoingExternalProviderIds
            .addAll( (ArrayList) ProviderCommon
                .listOutgoingExternalProviderIds() );
      }

      // list active current worker external providers
      ArrayList listActiveOutgoingWorkerProviderIds = new ArrayList();
      if ( filterByWorkers ) {
        listActiveOutgoingWorkerProviderIds.addAll( providerApp
            .getMtSendWorker().listActiveMtAgentProviderIds() );
      }

      // is need to filter by active worker providers ?
      if ( filterByWorkers ) {

        // matched between active worker and internal & external

        int indexOfInternal = -1 , indexOfExternal = -1;
        String activeOutgoingWorkerProviderId = null;
        Iterator iterActiveOutgoingWorkerProviderIds = listActiveOutgoingWorkerProviderIds
            .iterator();
        while ( iterActiveOutgoingWorkerProviderIds.hasNext() ) {
          activeOutgoingWorkerProviderId = (String) iterActiveOutgoingWorkerProviderIds
              .next();
          if ( activeOutgoingWorkerProviderId == null ) {
            continue;
          }
          if ( activeOutgoingWorkerProviderId.equals( "" ) ) {
            continue;
          }
          indexOfInternal = listActiveOutgoingInternalProviderIds
              .indexOf( activeOutgoingWorkerProviderId );
          indexOfExternal = listActiveOutgoingExternalProviderIds
              .indexOf( activeOutgoingWorkerProviderId );
          if ( ( indexOfInternal < 0 ) && ( indexOfExternal < 0 ) ) {
            continue;
          }
          listProviderIds.add( activeOutgoingWorkerProviderId );
        }

      } else {

        // compose list provider ids by all types
        if ( listActiveOutgoingModemProviderIds != null ) {
          listProviderIds.addAll( listActiveOutgoingModemProviderIds );
        }
        if ( listActiveOutgoingInternalProviderIds != null ) {
          listProviderIds.addAll( listActiveOutgoingInternalProviderIds );
        }
        if ( listActiveOutgoingExternalProviderIds != null ) {
          listProviderIds.addAll( listActiveOutgoingExternalProviderIds );
        }

      }

      // add provider members , if found any
      if ( addMembers ) {
        ProviderCommon.addOutgoingProviderIdMembers( listProviderIds );
      }

      // clean list provider ids
      ArrayList listSuspendProviderIds = new ArrayList();
      Iterator iterProviderIds = listProviderIds.iterator();
      while ( iterProviderIds.hasNext() ) {
        String providerId = (String) iterProviderIds.next();

        // clean for empty provider id
        if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
          iterProviderIds.remove();
          continue;
        }

        // clean for suspend provider id ( if need it )
        if ( filterBySuspend
            && opropsApp.getBoolean(
                "ProviderAgent.Suspend.".concat( providerId ) , false ) ) {
          listSuspendProviderIds.add( providerId );
          iterProviderIds.remove();
          continue;
        }

      }

      // calculate delta time
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log it if debug
      if ( debug ) {
        DLog.debug( lctx , "listOutgoingProviderIds : listFilterByTypes = "
            + listFilterByTypes + " , filterByWorkers = " + filterByWorkers
            + " , addMembers = " + addMembers + " , filterBySuspend = "
            + filterBySuspend + " , listActiveOutgoingModemProviderIds = "
            + listActiveOutgoingModemProviderIds
            + " , listActiveOutgoingInternalProviderIds = "
            + listActiveOutgoingInternalProviderIds
            + " , listActiveOutgoingExternalProviderIds = "
            + listActiveOutgoingExternalProviderIds
            + " , listActiveOutgoingWorkerProviderIds = "
            + listActiveOutgoingWorkerProviderIds
            + " , listSuspendProviderIds = " + listSuspendProviderIds
            + " , listResultProviderIds = " + listProviderIds + " , take = "
            + deltaTime + " ms" );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to list outgoing provider ids , " + e );
    }
    return listProviderIds;
  }

  public static ArrayList listIncomingProviderIds( boolean debug ) {
    ArrayList listProviderIds = new ArrayList();
    try {

      // delta time
      long deltaTime = System.currentTimeMillis();

      // read default value from cache provider table
      ArrayList listActiveIncomingProviderIds = (ArrayList) ProviderCommon
          .listIncomingProviderIds();
      if ( listActiveIncomingProviderIds != null ) {
        listProviderIds.addAll( listActiveIncomingProviderIds );
      }

      // calculate delta time
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log it if debug
      if ( debug ) {
        DLog.debug( lctx ,
            "listIncomingProviderIds : listActiveIncomingProviderIds = "
                + listActiveIncomingProviderIds + " , take = " + deltaTime
                + " ms" );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to list incoming provider ids , " + e );
    }
    return listProviderIds;
  }

}
