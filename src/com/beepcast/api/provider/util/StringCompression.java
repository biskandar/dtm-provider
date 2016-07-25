package com.beepcast.api.provider.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompression {

  private static final int BUFFER_SIZE = 1024;

  public static byte[] compress( String plainText , String encoding )
      throws IOException {
    byte[] compressBytes = null;
    if ( plainText == null ) {
      return compressBytes;
    }
    BufferedInputStream bis = new BufferedInputStream(
        new ByteArrayInputStream( plainText.getBytes( encoding ) ) );
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream( baos );
    byte[] buffer = new byte[BUFFER_SIZE];
    int len;
    while ( ( len = bis.read( buffer , 0 , BUFFER_SIZE ) ) != -1 ) {
      gzip.write( buffer , 0 , len );
    }
    gzip.finish();
    gzip.close();
    compressBytes = baos.toByteArray();
    bis.close();
    return compressBytes;
  }

  public static String uncompress( byte[] compressBytes , String encoding )
      throws IOException {
    String plainText = null;
    if ( compressBytes == null ) {
      return plainText;
    }
    GZIPInputStream gzip = new GZIPInputStream( new ByteArrayInputStream(
        compressBytes ) );
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    int len;
    while ( ( len = gzip.read( buffer , 0 , BUFFER_SIZE ) ) != -1 ) {
      baos.write( buffer , 0 , len );
    }
    baos.close();
    plainText = new String( baos.toByteArray() , encoding );
    gzip.close();
    return plainText;
  }

}
