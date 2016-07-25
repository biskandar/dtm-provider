package com.beepcast.api.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DRServlet extends HttpServlet {

  private static final long serialVersionUID = 3462466970015311590L;

  static final DLogContext lctx = new SimpleContext( "DRServlet" );

  public void doGet( HttpServletRequest request , HttpServletResponse response )
      throws ServletException , IOException {
    processRequest( request , response );
  }

  public void doPost( HttpServletRequest request , HttpServletResponse response )
      throws ServletException , IOException {
    processRequest( request , response );
  }

  public void processRequest( HttpServletRequest request ,
      HttpServletResponse response ) throws ServletException , IOException {

    // calculate the latency
    long deltaTime = System.currentTimeMillis();

    // compose default response
    response.setContentType( "text/html; charset=utf-8" );
    PrintWriter out = response.getWriter();
    String respBody = "OK";

    // get must be parameters
    String providerId = request.getParameter( "providerId" );
    String status = request.getParameter( "status" );
    String messageId = request.getParameter( "messageId" );

    // temporary
    String stemp;
    long ltemp;

    // get status date ( optional )
    Date statusDate = null;
    stemp = request.getParameter( "statusDate" );
    if ( !StringUtils.isBlank( stemp ) ) {
      try {
        ltemp = Long.parseLong( stemp );
        if ( ltemp > 0 ) {
          statusDate = new Date( ltemp );
        }
      } catch ( NumberFormatException e ) {
        DLog.warning( lctx , "Failed to parse statusDate param "
            + "into long type format , " + e );
      }
    }

    // get external params ( optional )
    String externalParams = request.getParameter( "externalParams" );

    // log parameters
    DLog.debug(
        lctx ,
        "Process the dr request parameter(s) : status = " + status
            + " , messageId = " + messageId + " , providerId = " + providerId
            + " , statusDate = " + DateTimeFormat.convertToString( statusDate )
            + " , externalParams = "
            + StringEscapeUtils.escapeJava( externalParams ) );

    // compose header log
    String headerLog = "[" + providerId + "-" + messageId + "] ";

    // get application object
    ProviderApp providerApp = ProviderApp.getInstance();
    if ( providerApp == null ) {
      DLog.warning( lctx , headerLog + "Failed to process dr message "
          + ", found null object of provider app" );
      respBody = "INTERNAL ERROR";
      out.println( respBody );
      return;
    }

    // prepare for the optional params
    Map optionalParams = new HashMap();
    // ...

    // store the dr message
    if ( !providerApp.processReport( providerId , messageId , status ,
        statusDate , optionalParams , externalParams ) ) {
      DLog.warning( lctx , headerLog + "Failed to process dr message "
          + ", found failed to process the report thru provider app" );
      respBody = "NOK";
      out.println( respBody );
      return;
    }

    // calculate the latency
    deltaTime = System.currentTimeMillis() - deltaTime;

    // for the debug trace
    DLog.debug(
        lctx ,
        headerLog + "Created the response ["
            + StringEscapeUtils.escapeJava( respBody )
            + "] to provider , take = " + deltaTime + " ms " );

    // response back the result
    out.println( respBody );
  }

}
