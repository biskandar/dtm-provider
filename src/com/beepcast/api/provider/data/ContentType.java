package com.beepcast.api.provider.data;

public class ContentType {

  public static final int SMSTEXT = 0;
  public static final int SMSBINARY = 1;
  public static final int SMSUNICODE = 2;
  public static final int MMS = 3;
  public static final int WAPPUSH = 4;
  public static final int QRPNG = 5;
  public static final int QRGIF = 6;
  public static final int QRJPG = 7;
  public static final int WEBHOOK = 8;

  public static String toString( int contentType ) {
    String name = null;
    switch ( contentType ) {
    case SMSTEXT :
      name = "SMS_TEXT";
      break;
    case SMSBINARY :
      name = "SMS_BINARY";
      break;
    case SMSUNICODE :
      name = "SMS_UNICODE";
      break;
    case MMS :
      name = "MMS";
      break;
    case QRPNG :
      name = "QR_PNG";
      break;
    case QRGIF :
      name = "QR_GIF";
      break;
    case QRJPG :
      name = "QR_JPG";
      break;
    case WEBHOOK :
      name = "WEBHOOK";
      break;
    }
    return name;
  }

}
