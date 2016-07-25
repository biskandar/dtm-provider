# dtm-provider
Directtomobile Provider Module

v2.2.32

- Add inbound external provider NX2 , number : (+62) 85574670764 .

- Fixed bug to provide list outgoing providers : to include the external and internal as well

- Setup for generic provider to support configuration thru code

- Centralized the libraries

- Use Jdk 1.7

- Add new library :
  . client2beepcast-v2.3.10.jar
  . lookup2beepcast-v1.0.00.jar
  . channel2beepcast-v1.0.69.jar
  . transaction2beepcast-v1.1.39.jar

v2.2.31

- Not to forward nya mt message that cames from MODEM , keep store into send buffer

    DEBUG   {RouterMTInOuWorker}ProviderApp [ProviderMessage-MDM20234000] Send provider message : dstAddr = +6598394294, oriAddr = null, oriAddrMask = 90102337, debitAmount = 1.0, cntType = 0, msgCount = 1, msgContent = Test Incoming Basic Modem Event 01
    DEBUG   {RouterMTInOuWorker}ProviderUtil [ProviderMessage-MDM20234000] Resolved provider : id = 14 , providerId = SINGTELMDM01 , direction = IO , type = MODEM , description = Modem Gateway
    WARNING {RouterMTInOuWorker}MTSendWorker [Provider-SINGTELMDM01] [ProviderMessage-MDM20234000] Failed to send message , found null provider agent channel
    WARNING {RouterMTInOuWorker}ProviderService [RouterMessage-MDM20234000] Failed to process mt message , found failed to send message thru provider app
    WARNING {RouterMTInOuWorker}RouterMTWorker [RouterMessage-MDM20234000] Failed to submit mt message to provider app , set to increase the buffer retry value became 1
    DEBUG   {RouterMTInOuWorker}RouterMTWorker [RouterMessage-MDM20234000] Found mt message buffer retry value = 1 , still hasn't reached the max yet , trying to store back into send buffer
    DEBUG   {RouterMTInOuWorker}RouterMTWorker [RouterMessage-MDM20234000] Inserted unstored mt message into the mt batch queue back

- Add new library :
  . channel2beepcast-v1.0.68.jar
  . router2beepcast-v1.0.43.jar
  . transaction2beepcast-v1.1.38.jar
  . client2beepcast-v2.3.08.jar
  . util2beepcast-v1.0.01.jar
  . subscriber2beepcast-v1.2.40.jar
  . beepcast_loadmanagement-v1.2.05.jar
  . beepcast_database-v1.2.02.jar
  . beepcast_properties-v2.0.01.jar
  . beepcast_clientapi-v1.1.00.jar

v2.2.30

- Support for proxy server for http client

- Support xl provider

- Add new library : 
  . jaf/jaf-1.1.1/activation.jar
	. stax/stax-api-1.0.1.jar
	. stax/stax-1.2.0.jar
	. jettison/jettison-1.2.jar
	. xstream-1.4.8/lib/xstream/xpp3_min-1.1.4c.jar
  . xstream-1.4.8/lib/xstream/xmlpull-1.1.3.1.jar
  . xstream-1.4.8/lib/xstream-1.4.8.jar

v2.2.29

- Update Nexmo API to support secure http posts. https://help.nexmo.com/hc/en-us/articles/213877768
  Enforcing mandatory use of secure HTTP (HTTPS) on Dec 15th 2015

- Found bug to set always as http on provider's connection
  
- Failed to verify the provider's connection

    16.12.2015 18.01.52:526 1866 WARNING {VerifyStatusConnectionWorker-NX}BaseAgent [Provider-NX] 
      Failed to verify status connection , found java.net.SocketException: Connection reset
  
- Change to 

    https://rest.nexmo.com/sms/json?api_key=xxxxxxxx&api_secret=xxxxxxxx&to=xxxxxxxxxxxx&from=NexmoWorks&text=hello+from+Nexmo

- Add new library :
  . 

v2.2.28

- Use database mysql 5.6 port 3307

- There is changed in file ./conf/provider.xml

      <providerAgent class="com.beepcast.api.provider.agent.QRAgent"
        providerId="QR" enabled="true" queueSize="300" worker="10" sleep="250">
        <Connections enableErrorCheck="false" maxErrorTolerant="0" />
        <messageParams>
          <messageParam name="imageFileLocation" value="${provider.qr.image_file_location}" />
          <messageParam name="imageFileFormat" value="${provider.qr.image_file_format}" />
          <messageParam name="imageFileSizeWidth" value="${provider.qr.image_file_size_width}" />
          <messageParam name="imageFileSizeHeight" value="${provider.qr.image_file_size_height}" />
        </messageParams>
      </providerAgent>

- There is changed in file ./conf/baGlobalEnv.properties

    platform.dir.message_attachments=${beepadmin.home}/beepfiles/message_attachments/

    provider.qr.image_file_location=${platform.dir.message_attachments}
    provider.qr.image_file_format=png
    provider.qr.image_file_size_width=128
    provider.qr.image_file_size_height=128

- Create folder ${beepadmin.home}/beepfiles/message_attachments/
    
- Build a QR Generator Provider with parameters :
  . eventId
  . text 
  . size
  . file

- Use Jdk 1.7

- Add new library :

  . stax-api-1.0.1.jar
  . stax-1.2.0.jar
  . jettison-1.2.jar
  . xpp3_min-1.1.4c.jar
  . jdom-1.1.3.jar
  . dom4j-1.6.1.jar
  . xom-1.1.jar
  . cglib-nodep-2.2.jar
  . xmlpull-1.1.3.1.jar
  . xstream-1.4.8.jar

  . commons-logging-1.2.jar
  . commons-pool2-2.4.2.jar
  . commons-dbcp2-2.1.1.jar
  . mysql-connector-java-5.1.35-bin.jar
  . beepcast_database-v1.2.00.jar
  
  . beepcast_clientapi-v1.0.04.jar
  . xipme_api-v1.0.29.jar
  . model2beepcast-v1.0.82.jar
  . router2beepcast-v1.0.40.jar
  . transaction2beepcast-v1.1.34.jar


v2.2.27

- There is a change inside file ./conf/oproperties.xml

      <property field="ProviderAgent.MapLookup.MB1" value="MBLUP1"
        description="Map lookup provider based on provider agent" />

- Add feature for mblox delivery report ( MbloxBroker ) to call lookup module

- Add mapping from sms to lookup provider

- Add new library :
  . channel2beepcast-v1.0.66.jar
  . transaction2beepcast-v1.1.32.jar
  . client2beepcast-v2.3.07.jar
  . model2beepcast-v1.0.80.jar
  . subscriber2beepcast-v1.2.36.jar
  . dwh_model-v1.0.33.jar
  . beepcast_dbmanager-v1.1.36.jar
  . beepcast_onm-v1.2.09.jar
  . lookup2beepcast-v1.0.00.jar

v2.2.26

- Migrate all the existing projects to use IDE MyEclipse Pro 2014
  . Build as java application ( not as web project )

- Add new library :
  . http-v1.0.01.jar
  . router2beepcast-v1.0.38.jar

v2.2.25

- Found bug in the delivery report process date field from nexmo provider , it seems
  can not get down to the seconds, it always appear as 00

- Add generic provider , with list reserved variables in the ./conf/provider.xml

  . $PROVIDER_ACCESSURL
  . $PROVIDER_ACCESSUSERNAME
  . $PROVIDER_ACCESSPASSWORD
  . $PROVIDER_LISTENERURL
  . $MESSAGE_ID
  . $MESSAGE_DESTINATIONADDRESS
  . $MESSAGE_DESTINATIONADDRESS_NOPLUS
  . $MESSAGE_ORIGINALADDRESSMASK
  . $MESSAGE_ORIGINALADDRESSMASK_NOPLUS
  . $MESSAGE_CONTENT

- Add new file ./js/gn1.js

- Changes inside file ./conf/baGlobalEnv.properties

    provider.gn1.remote_host=apus
    provider.gn1.remote_port=80
    provider.gn1.remote_path=/provider_gn1.php
    provider.gn1.script_file=${beepadmin.js}/gn1.js

- Changes inside file ./conf/provider.xml

      <providerAgent class="com.beepcast.api.provider.agent.GenericAgent"
        providerId="GN1" enabled="true" queueSize="300" worker="10" sleep="250">
        <Connections enableErrorCheck="false" maxErrorTolerant="3">
          <Connection protocol="http" remoteHost="${provider.gn1.remote_host}"
            remotePort="${provider.gn1.remote_port}" remotePath="${provider.gn1.remote_path}" />
        </Connections>
        <scriptParams>
          <scriptParam name="file" value="${provider.gn1.script_file}" />
          <scriptParam name="method" value="get" />
          <scriptParam name="queryString" value="" />
          <scriptParam name="requestBody" value="" />
        </scriptParams>
      </providerAgent>
    
- Add new library :
  . js-14.jar
  . http-v1.0.00.jar

v2.2.24 

- Test nexmo delivery report with telco code 

    DRWorker Store dr message into the queue , with : providerId = NX1 , externalMessageId = MDM20096000 
    , externalStatus = delivered , dateReport = 2015-02-13 16:42:00 , optionalParams = null 
    , externalParams = dG89JTJCNjU5MTA5MzQyOSZuZXR3b3JrLWNvZGU9NTI1MDEmbWVzc2FnZUlkPSZtc2lzZG49JTJCNjU5ODM5NDI5NCZzdGF0dXM9ZGVsaXZlcmVkJmVyci1jb2RlPTAmcHJpY2U9JnNjdHM9MTUwMjEzMTY0MiZtZXNzYWdlLXRpbWVzdGFtcD0yMDE1LTAyLTEzKzE2JTNBNDIlM0EzMyZjbGllbnQtcmVmPU1ETTIwMDk2MDAw
    http://apus:8080/beepadmin/provider_nx1.jsp?to=%2B6591093429&network-code=52501&messageId=&msisdn=%2B6598394294&status=delivered&err-code=0&price=&scts=1502131642&message-timestamp=2015-02-13+16%3A42%3A33&client-ref=MDM20096000
  
- Execute sql below :

    INSERT INTO dn_status_map
    (provider_id,external_status,internal_status,`shutdown`,retry,delivered,submitted,ignored,description,active,date_inserted,date_updated)
    VALUES

    ('TT1','FAILED-CONNECTION','FAILED'          ,0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('TT1','FAILED-RETRYABLE' ,'FAILED-RETRYABLE',0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('DG1','FAILED-CONNECTION','FAILED'          ,0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('DG1','FAILED-RETRYABLE' ,'FAILED-RETRYABLE',0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('SP1','FAILED-CONNECTION','FAILED'          ,0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('SP1','FAILED-RETRYABLE' ,'FAILED-RETRYABLE',0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('NX1','FAILED-CONNECTION','FAILED'          ,0,1,0,0,0,'',1,NOW(),NOW() ) ,
    ('NX1','FAILED-RETRYABLE' ,'FAILED-RETRYABLE',0,1,0,0,0,'',1,NOW(),NOW() ) ,

    ('CG3','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('CG4','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('CG5','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('DG1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('EE1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('EE2','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('EE3','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('EE4','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('MB1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('NX1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SB1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SB2','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SB3','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SB4','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SB5','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('SP1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ,
    ('TT1','FAILED-SHUTDOWN'  ,'FAILED'          ,1,0,0,0,0,'',1,NOW(),NOW() ) ;

- There is a changed inside ./conf/oproperties.xml

      <property field="ProviderAgent.EnableRetryOnFailedResponse" value="false"
        description="Will process the retry command for failed status response" />
      <property field="ProviderAgent.EnableShutdownOnFailedResponse" value="false"
        description="Will process the shutdown command for failed status response" />

- Add new library :
  . router2beepcast-v1.0.36.jar
  . channel2beepcast-v1.0.64.jar
  . model2beepcast-v1.0.76.jar
  . transaction2beepcast-v1.1.28.jar
  . beepcast_dbmanager-v1.1.35.jar

v2.2.23

- There is a change inside file ./conf/baGlobalEnv.properties

    provider.nx.remote_host=apus
    provider.nx.remote_port=9708
    provider.nx.remote_path=/sms/xml

- There is a change inside file ./conf/provider.xml

      <providerBroker class="com.beepcast.api.provider.broker.NexmoBroker" 
        providerId="NX" enabled="true">
        <mapParams>
          <mapParam name="gmt" value="8" />
        </mapParams>        
      </providerBroker>

      <providerAgent class="com.beepcast.api.provider.agent.NexmoAgent"
        providerId="NX" enabled="true" queueSize="300" worker="10" sleep="250">
        <Connections enableErrorCheck="false" maxErrorTolerant="3">
          <Connection protocol="http" remoteHost="${provider.nx.remote_host}"
            remotePort="${provider.nx.remote_port}" remotePath="${provider.nx.remote_path}" />
        </Connections>
        <messageParams>
          <messageParam name="ttl" value="86400000" />
          <messageParam name="gmt" value="8" />
        </messageParams>
      </providerAgent>

- Use tdiff to calculate delivery report status for Mach provider

- Add new library :
  . router2beepcast-v1.0.35.jar
  . transaction2beepcast-v1.1.27.jar
  . dwh_model-v1.0.32.jar
  . 

v2.2.22

- Increase the external message id length
    
    08.12.2014 14.39.50:382 2191 INFO    {ProviderManagementThread}ProviderManagement Queue MO=0, MTResp=0, DR=0, MOBatch=0, MTRespBatch=0, DRBatch=2
    08.12.2014 14.39.50:382 2192 DEBUG   {ProviderManagementThread}ProviderManagement Perform cleaning service for mt resp worker
    08.12.2014 14.39.50:382 2193 DEBUG   {ProviderManagementThread}MTRespWorker Will insert batch provider message(s) into gateway log , found : 0 record(s)
    08.12.2014 14.39.50:382 2194 DEBUG   {ProviderManagementThread}ProviderManagement Perform cleaning service for dr worker
    08.12.2014 14.39.50:382 2195 DEBUG   {ProviderManagementThread}DRWorker Moving batch dn records from the batch queue into dn buffer table , found candidate = 2 record(s)
    08.12.2014 14.39.50:383 2196 INFO    {ProviderManagementThread}DatabaseEngine [transactiondb] Loaded batch record(s) total = 2
    08.12.2014 14.39.50:383 2197 ERROR   {ProviderManagementThread}DatabaseEngine [transactiondb] Failed to execute the batch , java.sql.BatchUpdateException: Data truncation: Data too long for column 'message_id' at row 1
    08.12.2014 14.39.50:383 2198 INFO    {ProviderManagementThread}DatabaseEngine [transactiondb] Executed batch record(s) total = 0

- Execute sql below :

    ALTER TABLE `dn_buffer` 
      MODIFY COLUMN `message_id` VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL ; 

- Sample request delivery report from router sms plug 

    08.12.2014 10.44.10:707 2663 DEBUG   {Http[Rmt-54.76.44.218]}provider_sp1.jsp Received new request : sSender = +6591093429 , sMobileNo = 6598394294 , sStatus = UNDELIV , sMessageId = d260aac0-13cb-49a9-ae11-df7e84a6d9c7 , dtDone = 2014-12-08 08:12:41 , dtSubmit = 2014-12-08 07:32:35
    08.12.2014 10.44.10:707 2664 DEBUG   {Http[Rmt-54.76.44.218]}DRWorker Store dr message with : providerId = SP1 , externalMessageId = d260aac0-13cb-49a9-ae11-df7e84a6d9c7 , externalStatus = UNDELIV , dateReport = 2014-12-08 08:12:41 , externalParams =
    08.12.2014 10.44.10:707 2666 DEBUG   {Http[Rmt-54.76.44.218]}DRWorker [ProviderMessage-SP1-d260aac0-13cb-49a9-ae11-df7e84a6d9c7] Stored dr message into the queue ( take 0 ms ) : d260aac0-13cb-49a9-ae11-df7e84a6d9c7 , UNDELIV , 2014-12-08 08:12:41
    08.12.2014 10.44.10:709 2667 DEBUG   {ProviderDRWorkerThread-5}DRWorker [Provider-SP1] [ProviderMessage-d260aac0-13cb-49a9-ae11-df7e84a6d9c7] Found map dr status code UNDELIV -> FAILED ( UNDELIV )
    08.12.2014 10.44.10:709 2668 DEBUG   {ProviderDRWorkerThread-5}DRWorker [Provider-SP1] [ProviderMessage-d260aac0-13cb-49a9-ae11-df7e84a6d9c7] Added dr info params , internalStatus = FAILED , priority = 0 , description =
    08.12.2014 10.44.10:709 2670 DEBUG   {ProviderDRWorkerThread-5}DRWorker Found delay to process dr provider message , take = 4670 ms : SP1 , d260aac0-13cb-49a9-ae11-df7e84a6d9c7 , UNDELIV

- Add new provider Route Sms Plus , but found bug : external message id is too long
    d260aac0-13cb-49a9-ae11-df7e84a6d9c7 ( at least 45 characters )

    08.12.2014 10.02.34:919 2044 DEBUG   {SmsplusAgentThread-SP.0}SmsPlusAgent [Provider-SP] [ProviderMessage-MDM3795000] Composed httpParameters username=beepcast&password=v5xf5re&type=0&dlr=1&destination=%2B6598394294&source=%2B6591093429&message=Hello+Benny%2C+%0AThis+message+acknowledges+receipt+of+your+test+request+http%3A%2F%2Faw5.xip.me%3A9883%2FQAab2
    08.12.2014 10.02.34:919 2045 DEBUG   {SmsplusAgentThread-SP.0}SmsPlusAgent [Provider-SP] [ProviderMessage-MDM3795000] Created the request to remoteUrl http://smsplus3.routesms.com:8080/bulksms/bulksms
    08.12.2014 10.02.35:134 2046 DEBUG   {SmsplusAgentThread-SP.0}SmsPlusAgent [Provider-SP] [ProviderMessage-MDM3795000] Processed the response ( 215 ms ) = 1701|6598394294|d260aac0-13cb-49a9-ae11-df7e84a6d9c7
    08.12.2014 10.02.35:134 2047 DEBUG   {SmsplusAgentThread-SP.0}SmsPlusAgent [Provider-SP] [ProviderMessage-MDM3795000] Extracted external status = 1701
    08.12.2014 10.02.35:137 2048 DEBUG   {SmsplusAgentThread-SP.0}BaseAgent [ProviderMessage-MDM3795000] Found map status response 1701 -> SUBMITTED

    08.12.2014 10.03.15:345 2078 INFO    {ProviderManagementThread}ProviderManagement Queue MO=0, MTResp=0, DR=0, MOBatch=0, MTRespBatch=1, DRBatch=0
    08.12.2014 10.03.15:345 2079 DEBUG   {ProviderManagementThread}ProviderManagement Perform cleaning service for mt resp worker
    08.12.2014 10.03.15:345 2080 DEBUG   {ProviderManagementThread}MTRespWorker Will insert batch provider message(s) into gateway log , found : 1 record(s)
    08.12.2014 10.03.15:349 2081 INFO    {ProviderManagementThread}DatabaseEngine [transactiondb] Loaded batch record(s) total = 1
    08.12.2014 10.03.15:357 2082 ERROR   {ProviderManagementThread}DatabaseEngine [transactiondb] Failed to execute the batch , java.sql.BatchUpdateException: Data truncation: Data too long for column 'external_message_id' at row 1
    08.12.2014 10.03.15:357 2083 INFO    {ProviderManagementThread}DatabaseEngine [transactiondb] Executed batch record(s) total = 0
    08.12.2014 10.03.15:357 2084 WARNING {ProviderManagementThread}MTRespWorker Failed to execute the batch insert provider message(s) into gateway log , fould failed to persist
    08.12.2014 10.03.15:358 2085 DEBUG   {ProviderManagementThread}ProviderManagement Perform cleaning service for dr worker

  Execute sql below :
  
    ALTER TABLE `gateway_log` 
      MODIFY COLUMN `message_id` VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL ,
      MODIFY COLUMN `external_message_id` VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL ;
    
    ALTER TABLE `transaction` 
      MODIFY COLUMN `internal_message_id` VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL ,
      MODIFY COLUMN `external_message_id` VARCHAR(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL ;    
    
- There is a changed in file ./conf/baGlobalEnv.properties

    provider.sp.remote_host=corvus
    provider.sp.remote_port=9707
    provider.sp.remote_path=/bulksms/bulksms

- There is a changed in file ./conf/provider.xml

      <providerAgent class="com.beepcast.api.provider.agent.SmsplusAgent"
        providerId="SP" enabled="true" queueSize="300" worker="15" sleep="250">
        <Connections enableErrorCheck="false" maxErrorTolerant="3">
          <Connection protocol="http" remoteHost="${provider.sp.remote_host1}"
            remotePort="${provider.sp.remote_port1}" remotePath="${provider.sp.remote_path1}" />
        </Connections>
      </providerAgent>

- Execute sql below

    INSERT INTO dn_status_map
    ( provider_id , external_status , internal_status
    , `shutdown` , retry , delivered , submitted , ignored
    , description , active , date_inserted , date_updated )
    VALUES
    ( 'SP1' , '1701' , 'SUBMITTED'      , 0 , 0 , 0 , 1 , 0  , 'Message Submitted Successfully' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1702' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid URL Error' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1703' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Username Password' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1704' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Type' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1705' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Message' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1706' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Destination' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1707' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Source' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1708' , 'INVALID PARAMS' , 0 , 0 , 0 , 0 , 0  , 'Invalid Dlr' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1709' , 'AUTH FAILED'    , 0 , 0 , 0 , 0 , 0  , 'User Validation Failed' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1710' , 'ERROR'          , 0 , 0 , 0 , 0 , 0  , 'Internal Error' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1715' , 'TIMEOUT'        , 0 , 0 , 0 , 0 , 0  , 'Response Timeout' , 1 , NOW() , NOW() ) ,
    ( 'SP1' , '1025' , 'FAILED'         , 0 , 0 , 0 , 0 , 0  , 'Insufficient Credit' , 1 , NOW() , NOW() ) ;

    INSERT INTO dn_status_map
    ( provider_id , external_status , internal_status
    , `shutdown` , retry , delivered , submitted , ignored
    , description , active , date_inserted , date_updated )
    VALUES
    ( 'SP1', 'UNKNOWN' , 'FAILED'   , 0, 0, 0, 0, 0, 'UNKNOWN' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'ACKED'   , 'SUBMITTED', 0, 0, 0, 1, 1, 'ACKED'   , 1, NOW(), NOW() ) ,
    ( 'SP1', 'ENROUTE' , 'SUBMITTED', 0, 0, 0, 1, 1, 'ENROUTE' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'DELIVRD' , 'DELIVERED', 0, 0, 1, 0, 0, 'DELIVRD' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'EXPIRED' , 'FAILED'   , 0, 0, 0, 0, 0, 'EXPIRED' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'DELETED' , 'FAILED'   , 0, 0, 0, 0, 0, 'DELETED' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'UNDELIV' , 'FAILED'   , 0, 0, 0, 0, 0, 'UNDELIV' , 1, NOW(), NOW() ) ,
    ( 'SP1', 'ACCEPTED', 'SUBMITTED', 0, 0, 0, 1, 1, 'ACCEPTED', 1, NOW(), NOW() ) ,
    ( 'SP1', 'REJECTD' , 'FAILED'   , 0, 0, 0, 0, 0, 'REJECTD' , 1, NOW(), NOW() ) ;

    
- Put more information about inserting transaction into gateway log into log file ,
  like example below :
  
  ../../log/beepadmin-20141111-38.log:11.11.2014 11.41.01:253 18493333 DEBUG   
  {ProviderMTRespWorkerThread-11}MTRespWorker [Provider-EE4] [ProviderMessage-INT23140454] 
  Queued into mt response batch queue : +6598558406,INT23140454,+OK01,p.me\/QhyZD to download.
  ../../log/beepadmin-20141111-39.log:11.11.2014 11.47.11:159 18664165 DEBUG   
  {Http[Rmt-54.76.44.218]}provider_ee4.jsp Received new request , dnr = +6598558406 
  , dtag = INT23140454 , status = 0 , reason = 0 , tdif = 302 , mnc = 06,05 , mcc = 525

- Add new library :
  . transaction2beepcast-v1.1.26.jar
  . model2beepcast-v1.0.74.jar
  . subscriber2beepcast-v1.2.35.jar
  . beepcast_dbmanager-v1.1.34.jar
  . 

v2.2.21

- Failed to send unicode character for mblox provider

- Add new library :
  . router2beepcast-v1.0.34.jar
  . transaction2beepcast-v1.1.25.jar
  . model2beepcast-v1.0.73.jar
  . 

v2.2.20

- Failed to send enter for the unicode message to mach provider

- Add new library :
  . 

v2.2.19

- Be more specific on the delivery report of the MBlox Provider , please find 
  the sample below :
  
  09.09.2014 01.36.36:318 89177397 DEBUG   {HttpProcessor[8080][8]}provider_mb1.jsp Received new request , XMLDATA = 
  <?xml version="1.0" encoding="ISO-8859-1"?>
  <NotificationService Version="2.3">
  <Header>
          <Partner>BeepCastPte</Partner>
          <Password>6Er2G9bK</Password>
          <ServiceID>1</ServiceID>
  </Header>
  <NotificationList>
          <Notification BatchID="22223875" SequenceNumber="0">
                  <Subscriber>
                          <SubscriberNumber>6591912886</SubscriberNumber>
                          <Status>Non Delivered</Status>
                          <TimeStamp>201409081736</TimeStamp>
                          <MsgReference>32187891410145869274</MsgReference>
                          <Reason>5</Reason>
                          <Tags>
                                  <Tag Name="Operator">52503</Tag>
                          </Tags>
                  </Subscriber>
          </Notification>
  </NotificationList>
  </NotificationService>
  09.09.2014 01.36.36:318 89177398 DEBUG   {HttpProcessor[8080][8]}provider_mb1.jsp Read as notification service NotificationService ( Inbound Service ( version = 2.3 headerPartner = BeepCastPte headerPassword = 6Er2G9bK headerServiceId = 1  ) notfBatchId = 22223875 notfSequenceNumber = 0 subscriberNumber = 6591912886 subscriberStatus = Non Delivered subscriberTimeStamp = 201409081736 subscriberMsgReference = 32187891410145869274 subscriberReason = 5 )
  09.09.2014 01.36.36:318 89177399 DEBUG   {HttpProcessor[8080][8]}DRWorker Store dr message with : providerId = MB1 , externalMessageId = 22223875 , externalStatus = Non Delivered , dateReport = 2014-09-09 01:36:36 , externalParams = PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iSVNPLTg4NTktMSI\/Pgo8Tm90aWZpY2F0aW9u\nU2VydmljZSBWZXJzaW9uPSIyLjMiPgo8SGVhZGVyPgoJPFBhcnRuZXI+QmVlcENhc3RQdGU8L1Bh\ncnRuZXI+Cgk8UGFzc3dvcmQ+NkVyMkc5Yks8L1Bhc3N3b3JkPgoJPFNlcnZpY2VJRD4xPC9TZXJ2\naWNlSUQ+CjwvSGVhZGVyPgo8Tm90aWZpY2F0aW9uTGlzdD4KCTxOb3RpZmljYXRpb24gQmF0Y2hJ\nRD0iMjIyMjM4NzUiIFNlcXVlbmNlTnVtYmVyPSIwIj4KCQk8U3Vic2NyaWJlcj4KCQkJPFN1YnNj\ncmliZXJOdW1iZXI+NjU5MTkxMjg4NjwvU3Vic2NyaWJlck51bWJlcj4KCQkJPFN0YXR1cz5Ob24g\nRGVsaXZlcmVkPC9TdGF0dXM+CgkJCTxUaW1lU3RhbXA+MjAxNDA5MDgxNzM2PC9UaW1lU3RhbXA+\nCgkJCTxNc2dSZWZlcmVuY2U+MzIxODc4OTE0MTAxNDU4NjkyNzQ8L01zZ1JlZmVyZW5jZT4KCQkJ\nPFJlYXNvbj41PC9SZWFzb24+CgkJCTxUYWdzPgoJCQkJPFRhZyBOYW1lPSJPcGVyYXRvciI+NTI1\nMDM8L1RhZz4KCQkJPC9UYWdzPgoJCTwvU3Vic2NyaWJlcj4KCTwvTm90aWZpY2F0aW9uPgo8L05v\ndGlmaWNhdGlvbkxpc3Q+CjwvTm90aWZpY2F0aW9uU2VydmljZT4K\n
  09.09.2014 01.36.36:319 89177400 DEBUG   {ProviderDRWorkerThread-3}DRWorker [Provider-MB1] [ProviderMessage-22223875] Found map dr status code Non Delivered -> FAILED (  )
  09.09.2014 01.36.36:319 89177401 DEBUG   {ProviderDRWorkerThread-3}DRWorker [Provider-MB1] [ProviderMessage-22223875] Added dr info params , internalStatus = FAILED , priority = 0 , description =

- Clean out all library file that use package sun.* , replace with beepcast.encrypt

- Add new library :
  . transaction2beepcast-v1.1.23.jar
  . client2beepcast-v2.3.06.jar
  . beepcast_loadmanagement-v1.2.04.jar
  . beepcast_encrypt-v1.0.04.jar
  . 

v2.2.18

- Support to store new tag operator code from MBlox's delivery request , like example below :
  
    <NotificationService Version="2.3">
      <Header>
        <Partner>BeepCastPte</Partner>
        <Password>6Er2G9bK</Password>
        <ServiceID>1</ServiceID>
      </Header>
      <NotificationList>
        <Notification BatchID="21462793" SequenceNumber="0">
          <Subscriber>
            <SubscriberNumber>6598394294</SubscriberNumber>
            <Status>Delivered</Status>
            <TimeStamp>201407090625</TimeStamp>
            <MsgReference>1222372261404887139436</MsgReference>
            <Reason>4</Reason>
            <Tags>
              <Tag Name="Operator">52501</Tag>
            </Tags>
          </Subscriber>
        </Notification>
      </NotificationList>
    </NotificationService>  

- Add new library :
  . router2beepcast-v1.0.33.jar
  . transaction2beepcast-v1.1.22.jar
  . client2beepcast-v2.3.05.jar
  . model2beepcast-v1.0.70.jar
  . subscriber2beepcast-v1.2.34.jar
  . dwh_model-v1.0.30.jar
  . beepcast_dbmanager-v1.1.33.jar
  . beepcast_encrypt-v1.0.03.jar
  . beepcast_onm-v1.2.08.jar
  . beepcast_online_properties-v1.0.05.jar

v2.2.17

- Test

- Add new library :
  . router2beepcast-v1.0.31.jar
  . transaction2beepcast-v1.1.20.jar
  . client2beepcast-v2.3.04.jar
  . billing2beepcast-v1.1.07.jar
  . model2beepcast-v1.0.68.jar
  . dwh_model-v1.0.29.jar
  . beepcast_online_properties-v1.0.04.jar
  . beepcast_onm-v1.2.07.jar
  . 

v2.2.16

- For unicode message put additional xml attribute "Format='UTF8'" , with sample below 

  <?xml version="1.0"?>
  <NotificationRequest Version="3.5">
    <NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader>
    <NotificationList BatchID="20159047" >
      <Notification SequenceNumber="0" MessageType="SMS" Format="UTF8">
        <Message>Testing Accents</Message>
        <Profile>19821</Profile>
        <SenderID Type="Numeric" >6591093429</SenderID>
        <Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber>
      </Notification>
    </NotificationList>
  </NotificationRequest>
  
  <?xml version="1.0"?>
  <NotificationRequest Version="3.5">
    <NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader>
    <NotificationList BatchID="20159047" >
      <Notification SequenceNumber="0" MessageType="SMS" Format="UTF8">
        <Message>Testing Accents - £¥èéùìòäöñüà</Message>
        <Profile>19821</Profile>
        <SenderID Type="Numeric" >6591093429</SenderID>
        <Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber>
      </Notification>
    </NotificationList>
  </NotificationRequest>

- Add new library :
  . 

v2.2.15

- Add additional field "gatewayXipmeId" store into the gateway log

- Add new library :
  . router2beepcast-v1.0.30.jar
  . transaction2beepcast-v1.1.16.jar
  . model2beepcast-v1.0.66.jar
  . subscriber2beepcast-v1.2.27.jar
  . dwh_model-v1.0.28.jar
  . beepcast_encrypt-v1.0.02.jar

v2.2.14

- Add feature to convert special GSM 7 Bit Characters for provider EE

- Add new library :
  . 

v2.2.13

- Clean up the gateway and mobile user table ( phone and message )

  gatewayLogDAO
  mobileUserDAO
  
- Add new library :
  . router2beepcast-v1.0.28.jar
  . transaction2beepcast-v1.1.12.jar
  . client2beepcast-v2.3.02.jar
  . model2beepcast-v1.0.62.jar
  . subscriber2beepcast-v1.2.25.jar
  . beepcast_dbmanager-v1.1.31.jar
  . 

v2.2.12

- Found bug inside provider mb1 , only applied for long message

    10.09.2013 14.24.43:197 201812 DEBUG   {ProcessExtToIntWorker.MB1}MbloxLongMessage [ProviderMessage-MDM18101003] Splitted message part : intMsgId = 18101004 , debitAmount = 0.0 , udh = :05:00:03:08:03:01 , messageCount = 1 , message = Begin Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam
    10.09.2013 14.24.43:197 201813 DEBUG   {ProcessExtToIntWorker.MB1}MbloxLongMessage [ProviderMessage-MDM18101003] Splitted message part : intMsgId = 18101005 , debitAmount = 0.0 , udh = :05:00:03:08:03:02 , messageCount = 1 , message = , quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.Duis aute irure dolor in reprehenderit in voluptate velit esse cillum
    10.09.2013 14.24.43:197 201814 DEBUG   {ProcessExtToIntWorker.MB1}MbloxLongMessage [ProviderMessage-MDM18101003] Splitted message part : intMsgId = MDM18101003 , debitAmount = 3.0 , udh = :05:00:03:08:03:03 , messageCount = 1 , message =  dolore eu fugiat nulla pariatur.Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum End.
    
    10.09.2013 14.24.43:199 201824 DEBUG   {MbloxAgentThread-MB1.9}MbloxAgent [Provider-MB1] [ProviderMessage-18101004] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="1378794283198" ><Notification SequenceNumber="0" MessageType="SMS" ><Message>Begin Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam</Message><Profile>19821</Profile><Udh>:05:00:03:08:03:01</Udh><SenderID Type="Numeric" >6591093429</SenderID><Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    10.09.2013 14.24.43:200 201825 DEBUG   {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-18101005] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="1378794283198" ><Notification SequenceNumber="0" MessageType="SMS" ><Message>, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.Duis aute irure dolor in reprehenderit in voluptate velit esse cillum</Message><Profile>19821</Profile><Udh>:05:00:03:08:03:02</Udh><SenderID Type="Numeric" >6591093429</SenderID><Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    10.09.2013 14.24.43:200 201826 DEBUG   {MbloxAgentThread-MB1.0}MbloxAgent [Provider-MB1] [ProviderMessage-MDM18101003] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="18101003" ><Notification SequenceNumber="0" MessageType="SMS" ><Message> dolore eu fugiat nulla pariatur.Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum End.</Message><Profile>19821</Profile><Udh>:05:00:03:08:03:03</Udh><SenderID Type="Numeric" >6591093429</SenderID><Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    
    10.09.2013 14.24.43:258 201833 DEBUG   {MbloxAgentThread-MB1.9}MbloxAgent [Provider-MB1] [ProviderMessage-18101004] Processed the response ( 56 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"0\">            <NotificationResultCode>11<\/NotificationResultCode>            <NotificationResultText>malformed BatchID<\/NotificationResultText>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>
    10.09.2013 14.24.43:258 201834 DEBUG   {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-18101005] Processed the response ( 56 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"0\">            <NotificationResultCode>11<\/NotificationResultCode>            <NotificationResultText>malformed BatchID<\/NotificationResultText>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>
    10.09.2013 14.24.43:266 201843 DEBUG   {MbloxAgentThread-MB1.0}MbloxAgent [Provider-MB1] [ProviderMessage-MDM18101003] Processed the response ( 63 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"0\">            <NotificationResultCode>0<\/NotificationResultCode>            <NotificationResultText>OK<\/NotificationResultText>            <SubscriberResult>                <SubscriberNumber>6598394294<\/SubscriberNumber>                <SubscriberResultCode>0<\/SubscriberResultCode>                <SubscriberResultText>OK<\/SubscriberResultText>                <Retry>0<\/Retry>                <Operator>0<\/Operator>            <\/SubscriberResult>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>

- Add new library :
  . 
    
v2.2.11

- Found bug inside provider mb1 , found invalid batch id format especially came from api ( must be numeric format )

    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Generated external message id : 18097012-0-0
    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="18097012-0-0" ><Notification SequenceNumber="0" MessageType="SMS" ><Message>Testing api setup</Message><Profile>19821</Profile><SenderID Type="Numeric" >6591093429</SenderID><Subscriber><SubscriberNumber>6596328785</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Composed http parameters XMLDATA=%3C%3Fxml+version%3D%221.0%22%3F%3E%3CNotificationRequest+Version%3D%223.5%22%3E%3CNotificationHeader%3E%3CPartnerName%3EBeepCastPte%3C%2FPartnerName%3E%3CPartnerPassword%3E6Er2G9bK%3C%2FPartnerPassword%3E%3C%2FNotificationHeader%3E%3CNotificationList+BatchID%3D%2218097012-0-0%22+%3E%3CNotification+SequenceNumber%3D%220%22+MessageType%3D%22SMS%22+%3E%3CMessage%3ETesting+api+setup%3C%2FMessage%3E%3CProfile%3E19821%3C%2FProfile%3E%3CSenderID+Type%3D%22Numeric%22+%3E6591093429%3C%2FSenderID%3E%3CSubscriber%3E%3CSubscriberNumber%3E6596328785%3C%2FSubscriberNumber%3E%3C%2FSubscriber%3E%3C%2FNotification%3E%3C%2FNotificationList%3E%3C%2FNotificationRequest%3E
    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Created the request to remoteUrl http://xml11.mblox.com:8180/send
    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Processed the response ( 56 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"0\">            <NotificationResultCode>11<\/NotificationResultCode>            <NotificationResultText>malformed BatchID<\/NotificationResultText>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>
    {MbloxAgentThread-MB1.10}MbloxAgent [Provider-MB1] [ProviderMessage-API18097012-0-0] Extracted external status = NRC11
    {MbloxAgentThread-MB1.10}FetchDnStatusMap_ProviderId_ExternalStatus Generated a new record TDnStatusMap ( statusMapId = 23 providerId = MB1 externalStatus = NRC11 internalStatus = INVALID PARAMS shutdown = 0 retry = 0 delivered = 0 submitted = 0 ignored = 0 description = Malformed BatchID active = true  )
    {MbloxAgentThread-MB1.10}FetchBase Added a new record in the data cache , with cacheKey = ae4a9344ad5999572fb07878d0963a6f
    {MbloxAgentThread-MB1.10}BaseAgent [ProviderMessage-API18097012-0-0] Found map status response NRC11 -> INVALID PARAMS

- Add new library :
  . transaction2beepcast-v1.1.11.jar
  . client2beepcast-v2.3.01.jar
  . model2beepcast-v1.0.61.jar
  . subscriber2beepcast-v1.2.22.jar
  . dwh_model-v1.0.25.jar
  . beepcast_dbmanager-v1.1.30.jar
  . beepcast_onm-v1.2.04.jar

v2.2.10

- Add feature to flag suspend or disable provider agent from online properties

- Do the verify status connections with each thread 

- There is a changes inside file ./conf/provider.xml

    <deliveryNotification queueSize="4000" worker="5" sleep="100">
    <messageTerminated queueSize="4000" worker="10" sleep="100">

- There is a changes inside file ./conf/oproperties.xml

      <property field="ProviderAgent.EnableErrorCheck.EE" value="true"
        description="Enable to perform verification of EE provider's connection periodically" />
      <property field="ProviderAgent.MaxErrorTolerant.EE" value="3"
        description="Maximum Error tolerance to confirm EE provider's connection status" />
      <property field="ProviderAgent.Suspend.EE4" value="false"
        description="Publish the active status of the EE4 provider agent" />
      <property field="ProviderAgent.Suspend.SB1" value="false"
        description="Publish the active status of the SB1 provider agent" />
      <property field="Provider.DRWorker.TotalWorkers" value="5"
        description="Total thread workers to process dr provider message" />
      <property field="Provider.DRWorker.TimeSleep" value="100"
        description="Latency inside the thread worker to process dr provider messsage" />
      <property field="Provider.MOWorker.TotalWorkers" value="2"
        description="Total thread workers to process mo provider message" />
      <property field="Provider.MOWorker.TimeSleep" value="100"
        description="Latency inside the thread worker to process mo provider messsage" />
      <property field="Provider.MTRespWorker.TotalWorkers" value="5"
        description="Total thread workers to process mt response message" />
      <property field="Provider.MTRespWorker.TimeSleep" value="100"
        description="Latency inside the thread worker to process mt response messsage" />

- Validate the verify status connections from online properties

- Add new library :
  . transaction2beepcast-v1.1.06.jar
  . client2beepcast-v2.3.00.jar
  . beepcast_online_properties-v1.0.03.jar
  . beepcast_dbmanager-v1.1.29.jar

v2.2.09

- There is a change configuration in file ./conf/provider.xml

    <deliveryNotification queueSize="2000" worker="5" sleep="100">
      <priority default="0">
        <status name="shutdown" value="6" />
        <status name="retry" value="5" />
        <status name="delivered" value="0" />
        <status name="submitted" value="0" />
        <status name="ignored" value="0" />
      </priority>
      <dataBatch capacity="1000" threshold="50"/>
      <default internalStatus="NON-ROUTEABLE" externalStatus="NON-ROUTEABLE" />
      <providerBroker class="com.beepcast.api.provider.broker.DialogBroker" 
        providerId="DG" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.End2endBroker" 
        providerId="EE" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.End2endBroker" 
        providerId="EE4" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.MbloxBroker" 
        providerId="MB1" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.SybaseBroker" 
        providerId="SB" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.SybaseBroker" 
        providerId="SB1" enabled="true" />
      <providerBroker class="com.beepcast.api.provider.broker.TyntecBroker" 
        providerId="TT" enabled="true" />
    </deliveryNotification>
    
    <messageOriginator queueSize="2000" worker="2" sleep="100">
      <dataBatch capacity="1000" threshold="50"/>
      <providerBroker class="com.beepcast.api.provider.broker.DialogBroker" 
        providerId="DG" enabled="true" />
    </messageOriginator>

- Add broker instead of agent to support the processing mo and/or dr per provider

- Add feature to pass mobile country code and mobile network code from delivery report to router
  with legacy fields

- Add new library :
  . model2beepcast-v1.0.60.jar
  . transaction2beepcast-v1.1.05.jar
  . router2beepcast-v1.0.27.jar

v2.2.08

- Execute this sql to add status code for dialogue provider

  INSERT INTO dn_status_map
    (provider_id,external_status,internal_status,description,active,date_inserted,date_updated)
  VALUES
    ('DG1','00','SUBMITTED','Successful',1,NOW(),NOW()) ,
    ('DG1','01','SUBMITTED','Sent to SME but unable to confirm',1,NOW(),NOW()) ,
    ('DG1','02','SUBMITTED','replaced at the SMSC',1,NOW(),NOW()) ,
    ('DG1','20','FAILED','Congestion',1,NOW(),NOW()) ,
    ('DG1','21','FAILED','SME Busy',1,NOW(),NOW()) ,
    ('DG1','22','FAILED','No response from the SME',1,NOW(),NOW()) ,
    ('DG1','23','FAILED','Phone rejected message',1,NOW(),NOW()) ,
    ('DG1','24','FAILED','Quality of service not available',1,NOW(),NOW()) ,
    ('DG1','25','FAILED','Error in phone',1,NOW(),NOW()) ,
    ('DG1','26','FAILED','Unknown Subscriber',1,NOW(),NOW()) ,
    ('DG1','27','FAILED','Error in SMSC',1,NOW(),NOW()) ,
    ('DG1','30','FAILED','Unable to confirm',1,NOW(),NOW()) ,
    ('DG1','31','FAILED','Unable to deliver, unknown error',1,NOW(),NOW()) ,
    ('DG1','32','FAILED','System error',1,NOW(),NOW()) ,
    ('DG1','33','FAILED','PLMN error',1,NOW(),NOW()) ,
    ('DG1','34','FAILED','HLR unknown error',1,NOW(),NOW()) ,
    ('DG1','35','FAILED','VLR unknown error',1,NOW(),NOW()) ,
    ('DG1','36','FAILED','Authentication failure',1,NOW(),NOW()) ,
    ('DG1','37','FAILED','Plugin subsystem not responding',1,NOW(),NOW()) ,
    ('DG1','38','FAILED','Plugin subsystem failed',1,NOW(),NOW()) ,
    ('DG1','39','FAILED','Insufficient credit',1,NOW(),NOW()) ,
    ('DG1','3A','FAILED','Rejected by operator. Validity period expired',1,NOW(),NOW()) ,
    ('DG1','3B','FAILED','Message queue full',1,NOW(),NOW()) ,
    ('DG1','3C','FAILED','Driver initialisation. Driver link failure',1,NOW(),NOW()) ,
    ('DG1','3D','FAILED','Message was spooled',1,NOW(),NOW()) ,
    ('DG1','40','FAILED','Remote procedure error',1,NOW(),NOW()) ,
    ('DG1','41','FAILED','Incompatible destination',1,NOW(),NOW()) ,
    ('DG1','42','FAILED','Connection rejected by SME',1,NOW(),NOW()) ,
    ('DG1','43','FAILED','Not obtainable',1,NOW(),NOW()) ,
    ('DG1','44','FAILED','Quality of service not available',1,NOW(),NOW()) ,
    ('DG1','45','FAILED','No interworking available',1,NOW(),NOW()) ,
    ('DG1','46','FAILED','Validity period expired',1,NOW(),NOW()) ,
    ('DG1','47','FAILED','Message deleted by originating SME',1,NOW(),NOW()) ,
    ('DG1','48','FAILED','Message deleted by SMSC admin',1,NOW(),NOW()) ,
    ('DG1','49','FAILED','Message does not exist',1,NOW(),NOW()) ,
    ('DG1','4A','FAILED','Unknown subscriber',1,NOW(),NOW()) ,
    ('DG1','4B','FAILED','SMSC error',1,NOW(),NOW()) ,
    ('DG1','50','FAILED','Max submission attempt reached',1,NOW(),NOW()) ,
    ('DG1','51','FAILED','Max Time To Live for message reached',1,NOW(),NOW()) ,
    ('DG1','52','FAILED','Invalid data in message',1,NOW(),NOW()) ,
    ('DG1','53','FAILED','Non routable',1,NOW(),NOW()) ,
    ('DG1','54','FAILED','Authentication failure',1,NOW(),NOW()) ,
    ('DG1','55','FAILED','No response from SME',1,NOW(),NOW()) ,
    ('DG1','56','FAILED','SME rejected message',1,NOW(),NOW()) ,
    ('DG1','57','FAILED','Unknown error',1,NOW(),NOW()) ,
    ('DG1','58','FAILED','Operator bar',1,NOW(),NOW()) ,
    ('DG1','59','FAILED','Request ID not found',1,NOW(),NOW()) ,
    ('DG1','5A','FAILED','Premium charge routing error',1,NOW(),NOW()) ,
    ('DG1','5B','FAILED','Service ID not provisioned',1,NOW(),NOW()) ,
    ('DG1','5C','FAILED','MSISDN disconnected',1,NOW(),NOW()) ,
    ('DG1','5D','FAILED','Validity period expired',1,NOW(),NOW()) ,
    ('DG1','60','FAILED','Congestion',1,NOW(),NOW()) ,
    ('DG1','61','FAILED','SME busy',1,NOW(),NOW()) ,
    ('DG1','62','FAILED','No response from SME',1,NOW(),NOW()) ,
    ('DG1','63','FAILED','Service rejected',1,NOW(),NOW()) ,
    ('DG1','64','FAILED','Quality of service not available',1,NOW(),NOW()) ,
    ('DG1','65','FAILED','Error in SME',1,NOW(),NOW()) ,
    ('DG1','70','FAILED','Max submission attempt reached',1,NOW(),NOW()) ,
    ('DG1','71','FAILED','Max TTL for message reached',1,NOW(),NOW()) ,
    ('DG1','72','FAILED','Database sub-system error',1,NOW(),NOW()) ,
    ('DG1','73','FAILED','Core dependency missing',1,NOW(),NOW()) ,
    ('DG1','74','FAILED','Insufficient prepay credit',1,NOW(),NOW()) ,
    ('DG1','75','FAILED','Core configuration error',1,NOW(),NOW()) ,
    ('DG1','76','FAILED','Plug-in sub-system error',1,NOW(),NOW()) ,
    ('DG1','77','FAILED','Routing loop detected',1,NOW(),NOW()) ,
    ('DG1','78','FAILED','Age verification failure',1,NOW(),NOW()) ,
    ('DG1','79','FAILED','Age verification failure',1,NOW(),NOW()) ,
    ('DG1','7A','FAILED','Message in flight with unknown status',1,NOW(),NOW()) ,
    ('DG1','7B','FAILED','Expenditure limit reached',1,NOW(),NOW()) ;

- There is a change inside file ./conf/baGlobalEnv.properties

    provider.dg.remote_host=apus
    provider.dg.remote_port=9706
    provider.dg.remote_path=/cgi-bin/messaging/messaging.mpl

- There is a change inside file ./conf/provider.xml
      
      <providerAgent class="com.beepcast.api.provider.agent.DialogueAgent"
        providerId="DG" enabled="true" queueSize="300" worker="20" sleep="250">
        <Connections enableErrorCheck="false" maxErrorTolerant="10">
          <Connection protocol="http" remoteHost="${provider.dg.remote_host}"
            remotePort="${provider.dg.remote_port}" remotePath="${provider.dg.remote_path}" />
        </Connections>
        <messageParams>
          <messageParam name="X-E3-Concatenation-Limit" value="10" />
          <messageParam name="X-E3-Validity-Period" value="1d" />
          <messageParam name="X-E3-Confirm-Delivery" value="on" />
          <messageParam name="X-E3-Verbose" value="off" />
          <messageParam name="X-E3-No-Retry" value="off" />
          <messageParam name="X-E3-Silent-Message" value="off" />
        </messageParams>
      </providerAgent>
    
- Add new provider DG1 ( Dialogue )

- Add new library :
  . 

v2.2.07

- Optimized the delivery report message for case "pending" dr 

- Add new library :
  . transaction2beepcast-v1.1.04.jar
  . client2beepcast-v2.2.15.jar
  . model2beepcast-v1.0.59.jar
  . subscriber2beepcast-v1.2.20.jar
  . beepcast_dbmanager-v1.1.27.jar
  . beepcast_onm-v1.2.x/jar/beepcast_onm-v1.2.01.jar
  . router2beepcast-v1.0.26.jar

v2.2.06

- There is a changed inside file ./conf/oproperties.xml

      <property field="ProviderAgent.BurstSize" value="2"
        description="Total bursting pipes" />
      <property field="ProviderAgent.Threshold" value="100"
        description="Total messages threshold to refresh the message throughput" />

- Support for splitting long message for mblox provider

- Add new library :
  . 

v2.2.05

- Fixed mall formed profile

    08.11.2012 11.18.22:752 1689 DEBUG   {MbloxAgentThread-MB1.9}MbloxAgent [Provider-MB1] [ProviderMessage-MDM2614000] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="2614000" ><Notification SequenceNumber="1" MessageType="SMS" ><Message>This is test incoming basic response.</Message><Profile> 19821</Profile><SenderID Type="Numeric">6591093429</SenderID><Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    08.11.2012 11.18.22:885 1692 DEBUG   {MbloxAgentThread-MB1.9}MbloxAgent [Provider-MB1] [ProviderMessage-MDM2614000] Processed the response ( 132 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"1\">            <NotificationResultCode>12<\/NotificationResultCode>            <NotificationResultText>malformed Profile<\/NotificationResultText>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>
    <Profile> 19821</Profile>
    <NotificationResultText>malformed Profile<\/NotificationResultText>

- Fixed originating mask address on mblox provider

    08.11.2012 11.07.20:625 1690 DEBUG   {MbloxAgentThread-MB1.0}MbloxAgent [Provider-MB1] [ProviderMessage-MDM2613000] Composed xml data parameter <?xml version="1.0"?><NotificationRequest Version="3.5"><NotificationHeader><PartnerName>BeepCastPte</PartnerName><PartnerPassword>6Er2G9bK</PartnerPassword></NotificationHeader><NotificationList BatchID="2613000" ><Notification SequenceNumber="1" MessageType="SMS" ><Message>This is test incoming basic response.</Message><Profile> 19821</Profile><SenderID Type="Numeric">+6591093429</SenderID><Subscriber><SubscriberNumber>6598394294</SubscriberNumber></Subscriber></Notification></NotificationList></NotificationRequest>
    08.11.2012 11.07.21:067 1693 DEBUG   {MbloxAgentThread-MB1.0}MbloxAgent [Provider-MB1] [ProviderMessage-MDM2613000] Processed the response ( 440 ms ) = <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><NotificationRequestResult Version=\"3.5\">    <NotificationResultHeader>        <PartnerName>BeepCastPte<\/PartnerName>        <PartnerRef><\/PartnerRef>        <RequestResultCode>0<\/RequestResultCode>        <RequestResultText>OK<\/RequestResultText>    <\/NotificationResultHeader>    <NotificationResultList>        <NotificationResult SequenceNumber=\"1\">            <NotificationResultCode>5<\/NotificationResultCode>            <NotificationResultText>Sender id not numeric <\/NotificationResultText>        <\/NotificationResult>    <\/NotificationResultList><\/NotificationRequestResult>
    <SenderID Type="Numeric">+6591093429</SenderID>
    <NotificationResultText>Sender id not numeric <\/NotificationResultText>

- Add mblox provider parser

- Add new provider mblox

- Add new library :
  . transaction2beepcast-v1.0.80.jar
  . model2beepcast-v1.0.55.jar
  . subscriber2beepcast-v1.2.19.jar
  . dwh_model-v1.0.20.jar
  . beepcast_database-v1.1.05.jar

v2.2.04

- Handle new delivery status from sybase , here the sample format :

    Received new request , customerId = 23477 , orderId = 1403184264 
    , status = Message 1 of orderid 1403184264 for number +6590660744 has been filtered on customer request 
    , nmMessage = 1 , subject = , date = 05-07-2012 , time = 05:30:13 , xmlMsg = null
    
- Add extra dn status map code

  INSERT INTO dn_status_map ( provider_id , external_status , internal_status , description , active , date_inserted , date_updated )
  VALUES
  ('SB1','4','FAILED-INVALID','Filtered on customer request',1,NOW(),NOW()),
  ('SB2','4','FAILED-INVALID','Filtered on customer request',1,NOW(),NOW()),
  ('SB3','4','FAILED-INVALID','Filtered on customer request',1,NOW(),NOW()),
  ('SB4','4','FAILED-INVALID','Filtered on customer request',1,NOW(),NOW()),
  ('SB5','4','FAILED-INVALID','Filtered on customer request',1,NOW(),NOW());

- Add new library :
  . model2beepcast-v1.0.53.jar
  . transaction2beepcast-v1.0.75.jar
  . dwh_model-v1.0.19.jar
  . throttle-v1.0.01.jar

v2.2.03

- Store phone country id field inside gateway log

- Add feature to encrypt phone number and content message before store into gateway log table

- Add feature to encrypt short code and sender id before store into gateway log table

- Add new library :
  . transaction2beepcast-v1.0.71.jar
  . model2beepcast-v1.0.52.jar
  . subscriber2beepcast-v1.2.18.jar
  . dwh_model-v1.0.16.jar
  . beepcast_encrypt-v1.0.01.jar

v2.2.02

- The numbers of the provider agent workers can be adjust on the fly

- There is changed inside oproperties.xml file

      <property field="ProviderAgent.TotalWorkers.EE2" value="25"
        description="Total agent workers apply for EE2 provider" />
      <property field="ProviderAgent.TotalWorkers.SB1" value="25"
        description="Total agent workers apply for EE2 provider" />

- Add new library :
  . client2beepcast-v2.2.12.jar
  . subscriber2beepcast-v1.2.13.jar
  . beepcast_online_properties-v1.0.01.jar

v2.2.01

- Provide internal java api to handle all incoming delivery report request

- Support all gsm 7 bit characters 

- Add new library :
  . transaction2beepcast-v1.0.65.jar
  . model2beepcast-v1.0.51.jar
  . billing2beepcast-v1.1.05.jar
  . client2beepcast-v2.2.11.jar
  . subscriber2beepcast-v1.2.12.jar
  . dwh_model-v1.0.15.jar
  . beepcast_onm-v1.1.09.jar
  . beepcast_loadmanagement-v1.2.03.jar

v2.2.00

- Fixed on the providerApp.listOutgoingProviderIds() method , to include submembers
  of the active master provider id .

- Differentiate between providerMessage's providerId and destinationNode.
  . providerId is more about profiling
  . destinationNode is more about the agent worker
  
- Read the connectivity parameters thru the table first as first priority 
  , and use the configuration as a default ( low priority ).
  
  The connectivity parameters are access_url (MT) , access_username , access_password
  , and listener_url (MO/DR)

- Copied all the source code from v2.1.26

- Add new library :
  . beepcast_dbmanager-v1.1.26.jar

================================================================================

v2.1.26

- Support library to calculate latency inside tyntec provider

- Here the sample delivery notification from tyntec

    01.09.2011 13.58.14:598 18393265 DEBUG   {HttpProcessor[8080][27]}provider_tt1.jsp Received new request , submitdate = 1314847815000 , sender = +6597761798 , stat = DELIVRD , err = 0000 , msgid = 70800-1314847814589+6597761798 , receiver = McDelivery , donedate = 1314856690000
    01.09.2011 13.58.14:598 18393266 DEBUG   {HttpProcessor[8080][27]}provider_tt1.jsp Setup uri http://116.12.52.21:8080/beepadmin/api/provider/DRServlet
    01.09.2011 13.58.14:598 18393267 DEBUG   {HttpProcessor[8080][27]}provider_tt1.jsp Composed http parameters providerId=TT1&status=DELIVRD&statusDate=1314856690000&messageId=70800-1314847814589%2B6597761798

- Add new library :
  . 

v2.1.25

- Clean the dr worker log , put escape java in the messageparams

- Add new library :
  . transaction2beepcast-v1.0.55.jar
  . model2beepcast-v1.0.45.jar
  . beepcast_dbmanager-v1.1.25.jar

v2.1.24

- Here the sample response from tyntec , fixed the bug of <br>

    <HTML><HEAD><TITLE>Tech-On-Air sender<\/TITLE><\/HEAD><BODY>OK<p>MessageID(s):<br>70800-1313051590553+6598394294<br><\/BODY><\/HTML>

- There is a change inside baGlobalEnv.properties and provider.xml file

- Add new provider connector to tyntec

- Add new library :
  . transaction2beepcast-v1.0.54.jar
  . client2beepcast-v2.2.09.jar
  . billing2beepcast-v1.1.04.jar
  . model2beepcast-v1.0.44.jar
  . subscriber2beepcast-v1.2.05.jar
  . dwh_model-v1.0.12.jar
  . beepcast_dbmanager-v1.1.24.jar
  . beepcast_onm-v1.1.06.jar

v2.1.23

- Add feature to track the load of the delivery report from each provider

  . Use below code hit params to track the load dr internal status :
  
    String hdrProf = LoadManagement.HDRPROF_PROVIDER;
    String conType = LoadManagement.CONTYPE_SMSDR;
    StringBuffer sbName = new StringBuffer();
    sbName.append( providerMessage.getProviderId() );
    sbName.append( "-" );
    sbName.append( providerMessage.getInternalStatus() );
    loadMng.hit( hdrProf , conType , sbName.toString() , 1 , true , true );
     

- Add new library :
  . transaction2beepcast-v1.0.47.jar
  . client2beepcast-v2.2.06.jar
  . model2beepcast-v1.0.40.jar
  . subscriber2beepcast-v1.2.02.jar
  . beepcast_dbmanager-v1.1.21.jar
  . beepcast_encrypt-v1.0.00.jar

v2.1.22

- On sybase provider , checked if the response orderid has many it will capture only the latest
  message id reference

- Add new library :
  . transaction2beepcast-v1.0.45.jar
  . client2beepcast-v2.2.03.jar
  . model2beepcast-v1.0.38.jar
  . subscriber2beepcast-v1.1.04.jar

v2.1.21

- Change Provider Api , differentiate which are list of incoming and outgoing provider ids .

- Add new library :
  . beepcast_dbmanager-v1.1.19.jar
  . model2beepcast-v1.0.37.jar

v2.1.20

- Add util function to decode character ( ucs2 ) from sybase .

- Found bugs :

    24.08.2010 16.29.05:456 2064 ERROR {ProviderManagementThread?}DatabaseLibrary? [transactiondb]
    Failed to execute the batch , java.sql.BatchUpdateException?: No value specified for parameter 8
    
- Add session id inside sybase provider , when there is a message with reference id .
  Put field name "SYBASE_SESSION_ID" inside optional params of provider message , this field will
  trigger SybaseAgent to store on the request body message .

- Restructure provider message , put optional params as map to handle costumized params for particular
  provider
    
- Add new library :
  . transaction2beepcast-v1.0.34.jar
  . client2beepcast-v2.2.02.jar
  . billing2beepcast-v1.1.03.jar
  . model2beepcast-v1.0.34.jar
  . subscriber2beepcast-v1.1.03.jar
  . dwh_model-v1.0.09.jar
  . beepcast_dbmanager-v1.1.15.jar

v2.1.19

- will used providerMessage.destination property to identified which connection will be used 
  ( this only apply for jatis provider )

- fixed message unicode encoding for mach , found bugs inside StrUtil.converToHexString() function

- found provider conf is too complex , separated became : providerConf and providerConfFactory

- there are changes inside the way of init provider conf call

- create jatisiodAgent java class , it will do the jatis mt transaction

- update status table :
  
  INSERT INTO dn_status_map
    ( provider_id , external_status , internal_status , delivered , submitted , description , active , date_inserted , date_updated ) 
  VALUES
    ( 'JI1' ,  '0' , 'DELIVERED' , 1 , 1 , 'Success' , 1 , NOW() , NOW() ) ,
    ( 'JI1' , '-1' , 'FAILED'    , 0 , 0 , 'Failed' , 1 , NOW() , NOW() ) ,
    ( 'JI1' , '-2' , 'ERROR'     , 0 , 0 , 'Invalid User ID and Password' , 1 , NOW() , NOW() ) ,
    ( 'JI1' , '-3' , 'ERROR'     , 0 , 0 , 'Parameters not valid' , 1 , NOW() , NOW() ) ,
    ( 'JI1' , '-4' , 'ERROR'     , 0 , 0 , 'Guid not valid' , 1 , NOW() , NOW() ) ,
    ( 'JI1' , 'ERROR-NOPROVIDER'   , 'ERROR'  , 0 , 0 , 'Internal - No Available Provider'         , 1 , NOW() , NOW() ) ,
    ( 'JI1' , 'ERROR-NOCONNECTION' , 'ERROR'  , 0 , 0 , 'Internal - No Available Connection'       , 1 , NOW() , NOW() ) ,
    ( 'JI1' , 'FAILED-CONNECTION'  , 'FAILED' , 0 , 0 , 'Internal - Failed to connect to provider' , 1 , NOW() , NOW() ) ;

- update provider table :

  INSERT INTO provider 
    ( provider_id , provider_name , short_code , access_url , access_username , access_password , description , active , date_inserted , date_updated )
  VALUES 
    ( 'JI1' , 'JatisIod' , '9333' , 'http://' , 'goodboy' , 'goodboy', 'Jatis IOD Provider' , 0 , NOW() , NOW() ) ;

- change configuration provider file , add jatis iod provider agent :

      <providerAgent class="com.beepcast.api.provider.agent.JatisiodAgent"
        providerId="JI1" enabled="true" queueSize="500" worker="5" sleep="100">
        <Connections enableErrorCheck="false" maxErrorTolerant="10">
          <Connection protocol="http" remoteHost="${provider.sb.remote_host1}"
            remotePort="${provider.sb.remote_port1}" remotePath="${provider.sb.remote_path1}" />
          <Connection protocol="http" remoteHost="${provider.sb.remote_host2}"
            remotePort="${provider.sb.remote_port2}" remotePath="${provider.sb.remote_path2}" />
          <Connection protocol="http" remoteHost="${provider.sb.remote_host3}"
            remotePort="${provider.sb.remote_port3}" remotePath="${provider.sb.remote_path3}" />
          <Connection protocol="http" remoteHost="${provider.sb.remote_host4}"
            remotePort="${provider.sb.remote_port4}" remotePath="${provider.sb.remote_path4}" />
        </Connections>
        <messageParams>
          <messageParam name="channel" value="SMS" />
          <messageParam name="servicetype" value="DTM" />
          <messageParam name="chargingid" value="9333_1000" />
          <messageParam name="media" value="GOODBOY" />
          <messageParam name="sidcontent" value="ALL" />
        </messageParams>
      </providerAgent>

- add new Jatis provider :
  . will named JI for "Text Based Info On Demand API"
  . will named JP for "Push Message API"

  . support for mo message
  . support for mt message
  . support for the session flow ( mo + mt ) .

v2.1.18

- restructure DRServlet :
  . add new parameter to store provider status date time , statusDate ( long format )
  . add new parameter to store legacy provider querystring , externalParams( base64 format )

  . execute sql below
  
    ALTER TABLE `dn_buffer` 
      ADD COLUMN `external_params` VARCHAR(512) AFTER `description` ;
  
- add new library :
  . transaction2beepcast-v1.0.24.jar
  . model2beepcast-v1.0.26.jar

v2.1.17

- add new status map with sql below :

  INSERT INTO dn_status_map
  ( provider_id , external_status , internal_status , retry , description , active , date_inserted , date_updated )
  VALUES
  ( 'CG4' , 'ERROR-NOPROVIDER' , 'ERROR' , 1 , 'Internal - No Available Provider' , 1 , NOW() , NOW() ) ,
  ( 'CG4' , 'ERROR-NOCONNECTION' , 'ERROR' , 1 , 'Internal - No Available Connection' , 1 , NOW() , NOW() ) ,
  ( 'CG4' , 'FAILED-CONNECTION' , 'FAILED' , 1 , 'Internal - Failed to connect to provider' , 1 , NOW() , NOW() ) ,
  ( 'MB1' , 'ERROR-NOPROVIDER' , 'ERROR' , 1 , 'Internal - No Available Provider' , 1 , NOW() , NOW() ) ,
  ( 'MB1' , 'ERROR-NOCONNECTION' , 'ERROR' , 1 , 'Internal - No Available Connection' , 1 , NOW() , NOW() ) ,
  ( 'MB1' , 'FAILED-CONNECTION' , 'FAILED' , 1 , 'Internal - Failed to connect to provider' , 1 , NOW() , NOW() ) ,
  ( 'EE1' , 'ERROR-NOPROVIDER' , 'ERROR' , 1 , 'Internal - No Available Provider' , 1 , NOW() , NOW() ) ,
  ( 'EE1' , 'ERROR-NOCONNECTION' , 'ERROR' , 1 , 'Internal - No Available Connection' , 1 , NOW() , NOW() ) ,
  ( 'EE1' , 'FAILED-CONNECTION' , 'FAILED' , 1 , 'Internal - Failed to connect to provider' , 1 , NOW() , NOW() ) ,
  ( 'SB1' , 'ERROR-NOPROVIDER' , 'ERROR' , 1 , 'Internal - No Available Provider' , 1 , NOW() , NOW() ) ,
  ( 'SB1' , 'ERROR-NOCONNECTION' , 'ERROR' , 1 , 'Internal - No Available Connection' , 1 , NOW() , NOW() ) ,
  ( 'SB1' , 'FAILED-CONNECTION' , 'FAILED' , 1 , 'Internal - Failed to connect to provider' , 1 , NOW() , NOW() ) ;


- when found failed connect to provider , the transaction shall be retryable .
  but still configurable .
  
- add new library :
  . transaction2beepcast-v1.0.23.jar
  . model2beepcast-v1.0.24.jar
  . subscriber2beepcast-v1.0.02.jar
  . dwh_model-v1.0.08.jar  

v2.1.16

- There is a race condition when the diff between old and new throughput 
  small , so when there is a diff throughput , there shall be 
  a delay ( sleep ) to clean all the internal agent channel and than
  start the process with the new throughput .

- Create feature to setup maximum throughput in every provider ,
  the variable througput can be changed on the fly .

- Make the capacity of internal agent queue is based on the
  total worker thread x ( n times ) , n times is a variable
  more less than more sensitif to recognize the througput .
  
v2.1.15

- set retry configuration on the fly .

- need to expand field description in the dn_buffer table , with sql below :

    ALTER TABLE `dn_buffer` 
      MODIFY COLUMN `description` VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL ;

  this description field will be used for message params field .
  
- add debug configurable in the provider xml

- when the provider machine response with failed status , it shall set debit = 0
  
  Because the debit perform in the transaction module , so this message shall also 
  do the refund ( credit ) , and it will re-debit if found the submitted response
  message ,

  But this mechanism is configurable on the fly .


v2.1.14

- fix bugs when type enter character for sybase provider

- add new library :
  . model2beepcast-v1.0.10.jar
  . transaction2beepcast-v1.0.12.jar

- create provider message factory

- update provider message constant field :

    public static final int CONTENTTYPE_SMSTEXT = 0;
    public static final int CONTENTTYPE_SMSBINARY = 1;
    public static final int CONTENTTYPE_SMSUNICODE = 2;
    public static final int CONTENTTYPE_MMS = 3;
  
    public static final String MESSAGETYPE_SMSMO = "SMS.MO";
    public static final String MESSAGETYPE_SMSMT = "SMS.MT";
    public static final String MESSAGETYPE_SMSDR = "SMS.DR";
    public static final String MESSAGETYPE_MMSMO = "MMS.MO";
    public static final String MESSAGETYPE_MMSMT = "MMS.MT";
    public static final String MESSAGETYPE_MMSDR = "MMS.DR";
  
- change the way to insert into gateway log table :
  . there is a changes inside the gateway log fields
  . change inside the MTRespWorker batch  
  
v2.1.13

- Add additional parameter to support maximum split only for EE provider :
  . change the configuration provider.xml file , add split tag in the EE Provider Agent
          <messageParam name="split" value="5" />
  
- Add additional parameter to support split enable for SB provider :
  . change the configuration provider.xml file , add setup split tag int the SB Provider Agent
          <messageParam name="[SETUP],SplitText" value="yes" />

v2.1.12

- Fix the bugs when found queue full , the capacity and size shall be matched .

- Add management email alert when :
  . provider is inactive
  . provider is active
  . provider is queue full

- Change the order of onm library , and set provider app into onm ( dependent library )  
  
- Add new library :
  . onm library v1.1.00

v2.1.11

- Differentiate between status SybaseAck and SMSCAck

    public static final String EXTERNAL_STATUS_SUBMITTED = "0";
    public static final String EXTERNAL_STATUS_DELIVERED = "1";
    public static final String EXTERNAL_STATUS_SYBASE_ACK = "2";
    public static final String EXTERNAL_STATUS_SMSC_ACK = "3";

  add more status for those :
  
  INSERT INTO dn_status_map 
  ( provider_id , external_status , internal_status , retry , delivered , submitted , ignored ,
    description , active , date_inserted , date_updated )
  VALUES
  ( 'SB1' , '2' , 'SUBMITTED' , 0 , 0 , 1 , 1 , 'Sybase Ack' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '3' , 'SUBMITTED' , 0 , 0 , 1 , 1 , 'SMSC Ack' , 1 , NOW() , NOW() ) ;

- Found invalid delivery notification's status code , there is failed to parse only for invalid status 

  0x ( o octal ) -> 0x ( zero )

- Found invalid external message id :

  02.07.2009 14.42.51:465 580 DEBUG   {SybaseAgentThread-0}SybaseAgent [Provider-SB1] [ProviderMessage-MDM43000] Processed the response ( 793
   ms ) = <!DOCTYPE HTML PUBLIC \"-\/\/W3C\/\/DTD HTML 3.2 Final\/\/EN\"><HTML><HEAD><\/HEAD><BODY><br>#Message Receive correctly<br>ORDERID=
  1149607999<br><\/BODY><\/HTML>
  02.07.2009 14.42.51:465 581 DEBUG   {SybaseAgentThread-0}SybaseAgent [Provider-SB1] [ProviderMessage-MDM43000] Found external status = 0, e
  xternal messageId = 1149607999<br>

  The problem because the messageId has tag <br> .  
  
- Can not connect to sybase provider :

  02.07.2009 11.09.59:804 456 DEBUG   {SybaseAgentThread-2}SybaseAgent [Provider-SB1] [ProviderMessage-MDM42000] Composed httpBodyReqs = [MS
  ISDN]\nList=+6590517715\n[MESSAGE]\nText=Test Event Basic Beepcast 1\n[SETUP]\nAckType=Message\nMobileNotification=Yes\nValidityPeriod=1d\
  nAckReplyAddress=http:\/\/116.12.52.21:9080\/beepadmin\/sybase_mo.jsp\nOriginatingAdress=91114589\n[END]\n
  02.07.2009 11.10.00:612 459 WARNING {SybaseAgentThread-2}SybaseAgent [Provider-SB1] [ProviderMessage-MDM42000] Found failed to connect to
  remoteHost - http://messaging.mobileway.com:80/beepcast_p36501/beepcast_p36501.sms
  
  Found the problem , there is invalid field named : OriginatingAdress , shall be : OriginatingAddress

- Create library to parse delivery status code from sybase

- When shows the message content shall escape with java

- Add authorization encode base64 in the sybase agent

- Fixed bugs that system load management always shows zero

- Add new api to provide list of avilable provider ids

- Add new record retry able

  INSERT INTO dn_status_map
  ( provider_id , external_status , internal_status , retry ,
    description , active , date_inserted , date_updated )
  VALUES
  ( 'SB1' , 'FAILED-RETRYABLE' , 'FAILED-RETRYABLE' , 1 , '' , 1 , NOW() , NOW() ) ;

- Added dn_status_map record in the table for SB1 provider ...

  INSERT INTO dn_status_map 
  ( provider_id , external_status , internal_status , retry , delivered , submitted , ignored ,
    description , active , date_inserted , date_updated )
  VALUES
  ( 'SB1' , '0' , 'SUBMITTED' , 0 , 0 , 1 , 0 , 'Queued' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '1' , 'DELIVERED' , 0 , 1 , 0 , 0 , 'Delivered' , 1 , NOW() , NOW() ) , 
  
  ( 'SB1' , '5001' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '5002' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '5003' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '5004' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '5005' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '5006' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '600F' , 'AUTH FAILED' , 0 , 0 , 0 , 0 , 'Authentication failure' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '6010' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field DCS' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6011' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field PID' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6012' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field MobileAck' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6013' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field AckType' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6017' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format or missing field Text' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6018' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format or missing field List' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6019' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field List' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '601A' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field Class' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6024' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format for field ValidityPeriod' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6026' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format or missing field DestinationPort' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6028' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format or missing field Length' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6029' , 'ERROR' , 0 , 0 , 0 , 0 , 'Bad format or missing field Binary' , 1 , NOW() , NOW() ) , 
  
  ( 'SB1' , '602A' , 'ERROR' , 0 , 0 , 0 , 0 , 'Found invalid character' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '602B' , 'ERROR' , 0 , 0 , 0 , 0 , 'Found invalid length' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6030' , 'ERROR' , 0 , 0 , 0 , 0 , 'Error when process the message' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '6100' , 'ERROR' , 0 , 0 , 0 , 0 , 'Error when process the message' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '8000' , 'FAILED-RETRYABLE' , 1 , 0 , 0 , 0 , 'Server currently unavailable' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '450E' , 'SUBMITTED' , 0 , 0 , 0 , 1 , 'Sybase Ack' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '450A' , 'SUBMITTED' , 0 , 0 , 0 , 1 , 'SMSC Ack' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '451A' , 'DELIVERED' , 0 , 1 , 0 , 0 , 'Handset Ack' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , 'DBF3' , 'FAILED' , 0 , 0 , 0 , 1 , 'Sybase is retrying delivery' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , '4524' , 'FAILED-INVALID' , 0 , 0 , 0 , 0 , 'MSISDN is blacklisted' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , '4503' , 'FAILED-INVALID' , 0 , 0 , 0 , 0 , 'No operator found for current MSISDN' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , 'DB05' , 'FAILED' , 0 , 0 , 0 , 0 , 'Call bared by operator' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , 'DB52' , 'FAILED' , 0 , 0 , 0 , 0 , 'Failed Message Delivery' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , 'DB76' , 'FAILED-INVALID' , 0 , 0 , 0 , 0 , 'Subscriber does not permit service' , 1 , NOW() , NOW() ) , 

  ( 'SB1' , 'DB99' , 'FAILED' , 0 , 0 , 0 , 0 , 'Ported Number' , 1 , NOW() , NOW() ) , 
  
  ( 'SB1' , 'DB62' , 'FAILED' , 1 , 0 , 0 , 0 , 'Failed Message Delivery' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , 'DB65' , 'FAILED' , 1 , 0 , 0 , 0 , 'Validity Period Expired' , 1 , NOW() , NOW() ) , 
  ( 'SB1' , 'DB78' , 'FAILED' , 1 , 0 , 0 , 0 , 'Subscriber is temporarily out of credit' , 1 , NOW() , NOW() ) ;

- Add configuration to enable / disable monitoring with url ping

- Create ini file library to support sybase api

- Add new provider record in the table , named sybase .

  INSERT INTO provider
    ( provider_id , provider_name , short_code , access_url , access_username , access_password , 
      description , active , date_inserted , date_updated )
  VALUES
    ( 'SB1' , 'Sybase365' , '' , 'http://messaging.mobileway.com/beepcast_p36501/beepcast_p36501.sms' , 'beepcast_p36501' , 'mafMdK6e' ,
      'Sybase365 Provider' , 1 , NOW() , NOW() ) ;
      
- Created a module library to perform base64 result ( http digest )

- Restructure the provider app in order to ease to manage :
  . Created MtSendWorker object to handle the sender agent
  . Created provider API to standard the interface , with example client request below :
  
      String providerId = "SB1";
      ProviderApp providerApp = ProviderApp.getInstance();
      ProviderMessage providerMessage = generateProviderMessage( providerId );
      if ( providerApp.sendMessage( providerMessage ) ) {
        System.out.println( "Successfully send message" );
      }
      
- Create provider Sybase365 simulator , to ease perform the testing

- Add new provider connection named Sybase365 , code name SB1
  . protocol : http 

v2.1.10

- Create RandomPriorityProviderGenerator Class Variable .

- In the ProviderApp support function to generate list of available provider .

v2.1.09

- not show the queue information if found all items are zero .

- during the startup , the each of provider management shall validate connection .

- every provider has a max http error tolerate in the connections level

- simplified provider agent engine , to make easy to create another engine agent .

- add feature to set readtimeout and connectiontimeout in every http provider

  . please add java property below :
    -Dsun.net.client.defaultConnectTimeout=30000 # connection time out ~ 30 second(s)
    it will shown as log below :
    27.04.2009 10.10.50:390 89 DEBUG   {End2endAgentThread-1}End2endAgent [INT_1240798250078] 
      Created the request to remoteUrl http://localhost:9702/ee_one
    27.04.2009 10.11.50:390 116 DEBUG   {End2endAgentThread-1}End2endAgent [INT_1240798250078] 
      Processed the reponse ( 60000 ms ) = +OK01
    
  . please add java property below :
    -Dsun.net.client.defaultReadTimeout=30000     # read time out ~ 30 second(s)
    27.04.2009 10.09.23:765 89 DEBUG   {End2endAgentThread-4}End2endAgent [INT_1240798163468] 
      Created the request to remoteUrl http://localhost:9702/ee_one
    27.04.2009 10.09.54:281 103 WARNING {End2endAgentThread-4}End2endAgent [INT_1240798163468] 
      Found failed to connect to remoteHost - http://localhost:9702/ee_one

  . property connection timeout is the max timeout value ( sec ) from client trying to connect to server
  . property read timeout is the max timeout value ( sec ) from client after succeed connect trying to read response to server
    
v2.1.08

- changed the external messageId to use numeric format only for MB1 ( MBlox ) Provider .

v2.1.07

- use the new dbmanager library v1.1.04

- fixed bugs :
    java.lang.NoSuchMethodError: com.beepcast.dbmanager.Fetch.execute(Lcom/beepcast/dbmanager/Record;Z)Lcom/beepcast/dbmanager/Record;
    	at com.beepcast.api.provider.agent.End2endAgent.getProvider(End2endAgent.java:278)
    	at com.beepcast.api.provider.agent.End2endAgent.sendMessage(End2endAgent.java:352)
    	at com.beepcast.api.provider.agent.End2endAgent.access$7(End2endAgent.java:345)
    	at com.beepcast.api.provider.agent.End2endAgent$End2endAgentThread.run(End2endAgent.java:623)

v2.1.06

- fixed bugs :
    java.lang.NoSuchMethodError: com.beepcast.dbmanager.Fetch.execute(Lcom/beepcast/dbmanager/Record;Z)Lcom/beepcast/dbmanager/Record;
    	at com.beepcast.api.provider.agent.End2endAgent.getProvider(End2endAgent.java:278)
    	at com.beepcast.api.provider.agent.End2endAgent.sendMessage(End2endAgent.java:352)
    	at com.beepcast.api.provider.agent.End2endAgent.access$7(End2endAgent.java:345)
    	at com.beepcast.api.provider.agent.End2endAgent$End2endAgentThread.run(End2endAgent.java:623)

- use a new java library dbmanager-v1.1.03

v2.1.05

- use a new library beepcast_properties-v2.0.00 , so in the configuration level can use alias

- add feature to separate internal and external messageId

  . create index inside the gateway_log : external_message_id 
  . every incoming message from send_buffer with its messageId will trigger as internal messageId
  . before sending the message to provider , the process must insert both internal and external messageId
  . for provider EE1 & MB1 the external will use the same with internal messageId
  . for provider CG4 the external will depend on the provider response
  . as correlation with dr from provider it will compare to external messageId
  . the status will process as original , nothing to change

- change the sql escape 
  
  . SqlUtil.encode() -> StringEscapeUtils.escapeSql()    
  
v2.1.04

- add parameter field named : channelSessionId in the ProviderMessage
- fixed the MTResponse.extractMsg to use List ( changed from array of ProviderMessage )
- forward information from send_buffer.channel_session_id into gateway_log.channel_session_id

v2.1.03

- update the new dbmanager library -v1.1.02
- fixed wrong mapping dn status map , find the fetch mechanism make sure all use a new instance .
- fixed util DateTimeFormat to String to support null input parameter Date

v2.1.02

- removed the PROVIDER_ID const inside every MtAgentWorker Impl object , make it configurable based on conf file
- use java the reflection to create instance of provider as MtAgentWorker
- add end2end provider

v2.1.01

- add sms binary feature for commzgate and mblox
- add latency calculation for DRServlet response
- add priority feature for delivery report , especially for status retryable
- add configurable priority for dn process , additional xml inside the provider.xml

v2.1.00

- add utf-8 for response to provider in DRServlet & MOServlet -> "text/html; charset=utf-8"
- add ignored status at dn_status_map table
ALTER TABLE `beepcast`.`dn_status_map`
  ADD COLUMN `ignored` DECIMAL(1,0) DEFAULT 0 AFTER `submitted` ;
- support unicode message
- there must flag to differentiate between Ascii and Uni Message

v2.0.01

- add retry mechanism from response mt message perspective

- add provider agent capability
- add batch insert for MTResp Worker
- map for unknown external status to NON-ROUTEABLE internal status , and configurable
- fixed mblox agent to supply feature add profile inside the notification request
- fixed mblox agent to supply feature retry based on retry tag inside notification service
- fixed commzgate agent to set destinationNumber of MT Message without plus sign but still use the international format
- disabled agent which is not inactive
- fixed mblox agent to support senderId with diff type : Alpha , Numeric or Shortcode
- add recordId and retry field information in the providerMessage Record
- add send dr retry only for mblox agent , because they provider flag in the http mt response body

v2.0.00

- add sleep in the dr servlet perseptive the size of the queue

- first , insert all dr transaction into the dn_buffer 

- create new table to receive the dn transaction

  DROP TABLE IF EXISTS `beepcast`.`dn_buffer`;
  CREATE TABLE  `beepcast`.`dn_buffer` (
    `dn_id` int(10) unsigned NOT NULL auto_increment,
    `provider_id` varchar(10) NOT NULL,
    `message_id` varchar(30) NOT NULL,
    `external_status` varchar(50) NOT NULL,
    `internal_status` varchar(50) default NULL,
    `description` varchar(50) default NULL,
    `date_inserted` datetime NOT NULL,
    PRIMARY KEY  (`dn_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

- integrate with the api client v2.0.00

  
  
v1.0.04

- integrate with the api client v1.0.05
- set XXXX for the thread name to differentiate
  * ProviderMOWorkerThread-99
  * ProviderDRWorkerThread-99

v1.0.03

- added the new field at gateway_log table named status_date_tm , this field will set every time the status is updated
  sql_command :    
    ALTER TABLE `beepcast`.`gateway_log` ADD COLUMN `status_date_tm` DATETIME AFTER `date_tm`;
    
- added the new field at gateway_log table named external_status , this field will save all the status from provider
  sql command :
    ALTER TABLE `beepcast`.`gateway_log` ADD COLUMN `external_status` VARCHAR(50) AFTER `status`;
    
- separated status into : externalStatus and internalStatus    
    
- added feature to still add status delivery notification if there is no matched in the status dn map
- added delivery status map mechanism
- used the dbmanager cache menchanism
- optimized the lock object when performing dr sql batch
- integrate with the api client v1.0.04

v1.0.02
- integrate with the client api
- put the dr message information into client dr api

v1.0.01
- integrate with module loadmanagement

v1.0.00
- provider api for mo and dr
- every request mo / dr goes to the queue first
- add worker to process mo
- add worker to process dr
- have a speciality for sql statement to use batch
- add all transaction to cdr first for backup ( in case the db is down )
