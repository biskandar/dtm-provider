package com.beepcast.api.provider.agent.sybase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.convert.Base64;

public class RequestParameterCommon {

  static final DLogContext lctx = new SimpleContext( "RequestParameterCommon" );

  public static String createHeaderAuthorization( String loginId ,
      String password ) {
    String result = null;
    if ( ( loginId == null ) || ( password == null ) ) {
      DLog.warning( lctx , "Failed to create header authorization "
          + ", found null loginId and/or password" );
      return result;
    }
    try {
      String auth = loginId + ":" + password;
      byte[] bytes = Base64.encode( auth.getBytes() );
      result = "Basic " + new String( bytes );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to create header authorization , " + e );
    }
    return result;
  }

  public static int buildParameters( RequestParameter requestParameter ,
      Map messageParams ) {
    int totalParams = 0;
    if ( requestParameter == null ) {
      return totalParams;
    }
    if ( messageParams == null ) {
      return totalParams;
    }
    String paramName , paramValue;
    List listParamNames = new ArrayList( messageParams.keySet() );
    Iterator iterParamNames = listParamNames.iterator();
    while ( iterParamNames.hasNext() ) {
      paramName = (String) iterParamNames.next();
      if ( StringUtils.isBlank( paramName ) ) {
        continue;
      }
      paramValue = (String) messageParams.get( paramName );
      if ( StringUtils.isBlank( paramValue ) ) {
        continue;
      }
      if ( !RequestParameterCommon.addParameter( requestParameter , paramName ,
          paramValue ) ) {
        continue;
      }
      totalParams = totalParams + 1;
    }
    return totalParams;
  }

  public static boolean addParameter( RequestParameter requestParameter ,
      String parameterId , String value ) {
    boolean added = false;
    if ( requestParameter == null ) {
      return added;
    }
    String[] arr = parameterId.split( "," );
    if ( arr.length < 2 ) {
      return added;
    }
    String sectionName = arr[0];
    if ( ( sectionName == null ) || ( sectionName.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to add parameter "
          + ", found null section name" );
      return added;
    }
    String parameterName = arr[1];
    if ( ( parameterName == null ) || ( parameterName.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to add parameter "
          + ", found null parameter name" );
      return added;
    }
    if ( ( value == null ) || ( value.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to add parameter "
          + ", found null parameter value " );
      return added;
    }
    Section section = requestParameter.getSection( sectionName );
    if ( section == null ) {
      DLog.warning( lctx , "Failed to add parameter "
          + ", found invalid section name : " + sectionName );
      return added;
    }
    Parameter parameter = section.getParameter( parameterName );
    if ( parameter == null ) {
      parameter = ParameterFactory.generateParameter( parameterName , value );
      if ( parameter != null ) {
        if ( section.addParameter( parameter ) ) {
          added = true;
        } else {
          DLog.warning( lctx , "Failed to store new parameter "
              + "in the section object" );
        }
      } else {
        DLog.warning( lctx , "Failed to add parameter "
            + ", found invalid parameter name" );
      }
    } else {
      parameter.setValue( value );
      added = true;
    }
    return added;
  }

}
