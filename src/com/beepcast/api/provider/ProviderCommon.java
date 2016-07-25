package com.beepcast.api.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.dbmanager.table.TProviders;
import com.beepcast.model.provider.ProviderType;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderCommon {

  static final DLogContext lctx = new SimpleContext( "ProviderCommon" );

  public static String headerLog( String providerId ) {
    String headerLog = "";
    if ( ( providerId == null ) || ( providerId.equals( "" ) ) ) {
      return headerLog;
    }
    headerLog = "[Provider-" + providerId + "] ";
    return headerLog;
  }

  public static int addOutgoingProviderIdMembers( List listMasterProviderIds ) {
    int result = 0;
    if ( listMasterProviderIds == null ) {
      return result;
    }
    List listMemberProviderIds = new ArrayList();
    Iterator iterMasterProviderIds = listMasterProviderIds.iterator();
    while ( iterMasterProviderIds.hasNext() ) {
      String masterProviderId = (String) iterMasterProviderIds.next();
      if ( StringUtils.isBlank( masterProviderId ) ) {
        continue;
      }
      TProvider masterProvider = com.beepcast.dbmanager.common.ProviderCommon
          .getProvider( masterProviderId );
      if ( masterProvider == null ) {
        continue;
      }
      TProviders memberProviders = com.beepcast.dbmanager.common.ProviderCommon
          .getActiveOutgoingProviders( masterProvider.getId() );
      if ( ( memberProviders == null ) || ( memberProviders.sizeRecords() < 1 ) ) {
        continue;
      }
      Iterator iterMemberProviders = memberProviders.iterRecords();
      while ( iterMemberProviders.hasNext() ) {
        TProvider memberProvider = (TProvider) iterMemberProviders.next();
        if ( memberProvider == null ) {
          continue;
        }
        String memberProviderId = memberProvider.getProviderId();
        if ( StringUtils.isBlank( memberProviderId ) ) {
          continue;
        }
        listMemberProviderIds.add( memberProviderId );
      } // while ( iterMemberProviders.hasNext() )
    } // while ( iterMasterProviderIds.hasNext() )
    result = listMemberProviderIds.size();
    listMasterProviderIds.addAll( listMemberProviderIds );
    return result;
  }

  public static List listProviderIds( TProviders providers ) {
    return listProviderIds( providers , null );
  }

  public static List listProviderIds( TProviders providers , String filterByType ) {
    List list = new ArrayList();
    if ( providers == null ) {
      return list;
    }
    TProvider providerOut = null;
    String providerOutType = null;
    Iterator iter = providers.iterRecords();
    while ( iter.hasNext() ) {
      providerOut = (TProvider) iter.next();
      if ( providerOut == null ) {
        continue;
      }
      if ( ( filterByType == null ) || ( filterByType.equals( "" ) ) ) {
        list.add( providerOut.getProviderId() );
        continue;
      }
      providerOutType = providerOut.getType();
      if ( ( providerOutType == null )
          || ( !providerOutType.equalsIgnoreCase( filterByType ) ) ) {
        continue;
      }
      list.add( providerOut.getProviderId() );
    }
    return list;
  }

  public static List listOutgoingModemProviderIds() {
    return listProviderIds(
        com.beepcast.dbmanager.common.ProviderCommon
            .getActiveOutgoingProviders() ,
        ProviderType.TYPE_MODEM );
  }

  public static List listOutgoingInternalProviderIds() {
    return listProviderIds(
        com.beepcast.dbmanager.common.ProviderCommon
            .getActiveOutgoingProviders() ,
        ProviderType.TYPE_INTERNAL );
  }

  public static List listOutgoingExternalProviderIds() {
    return listProviderIds(
        com.beepcast.dbmanager.common.ProviderCommon
            .getActiveOutgoingProviders() ,
        ProviderType.TYPE_EXTERNAL );
  }

  public static List listOutgoingProviderIds() {
    return listProviderIds( com.beepcast.dbmanager.common.ProviderCommon
        .getActiveOutgoingProviders() );
  }

  public static List listIncomingProviderIds() {
    return listProviderIds( com.beepcast.dbmanager.common.ProviderCommon
        .getActiveIncomingProviders() );
  }

  public static TProvider getProvider( String providerId ) {
    return com.beepcast.dbmanager.common.ProviderCommon
        .getProvider( providerId );
  }

}
