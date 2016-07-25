package com.beepcast.api.provider;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.beepcast.onm.OnmApp;
import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderInitializer implements ServletContextListener {

  private static final String PROPERTY_FILE_PROVIDER = "provider.config.file";

  static final DLogContext lctx = new SimpleContext( "ProviderInitializer" );

  public void contextInitialized( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    ProviderConf providerConf = ProviderConfFactory
        .createProviderConf( PROPERTY_FILE_PROVIDER );
    logStr = this.getClass() + " : initialized " + providerConf;
    context.log( logStr );
    DLog.debug( lctx , logStr );

    ProviderApp providerApp = ProviderApp.getInstance();
    providerApp.init( providerConf );
    providerApp.start();
    logStr = this.getClass() + " : initialized " + providerApp;
    context.log( logStr );
    DLog.debug( lctx , logStr );

    DLog.debug( lctx , "Linking providerApp -> onmApp" );
    OnmApp onmApp = OnmApp.getInstance();
    onmApp.setProviderApp( providerApp );

  }

  public void contextDestroyed( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    ProviderApp providerApp = ProviderApp.getInstance();
    providerApp.stop();
    logStr = this.getClass() + " : destroyed ";
    context.log( logStr );
    DLog.debug( lctx , logStr );

  }

}
