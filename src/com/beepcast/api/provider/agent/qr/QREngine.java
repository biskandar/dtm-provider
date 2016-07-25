package com.beepcast.api.provider.agent.qr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.data.ContentType;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class QREngine {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "QREngine" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String imageFileLocation;
  private String imageFileFormat;
  private int imageFileSizeWidth;
  private int imageFileSizeHeight;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public QREngine( Map mapConfMessageParams ) {
    init( mapConfMessageParams );
    DLog.debug( lctx , "Created qr engine : imageFileLocation = "
        + imageFileLocation + " , imageFileFormat = " + imageFileFormat
        + " , imageFileSizeWidth = " + imageFileSizeWidth
        + " , imageFileSizeHeight = " + imageFileSizeHeight );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean createQrImageFile( String messageId , int eventId ,
      int contentType , String imageFileName , int imageFileSizeWidth ,
      int imageFileSizeHeight , String qrText ) {
    boolean created = false;

    // clean params
    messageId = ( ( messageId == null ) || ( messageId.equals( "" ) ) ) ? "000"
        : messageId;
    eventId = ( eventId < 0 ) ? 0 : eventId;
    imageFileName = ( ( imageFileName == null ) || ( imageFileName.equals( "" ) ) ) ? ""
        : imageFileName;
    imageFileSizeWidth = ( imageFileSizeWidth < 1 ) ? this.imageFileSizeWidth
        : imageFileSizeWidth;
    imageFileSizeHeight = ( imageFileSizeHeight < 1 ) ? this.imageFileSizeHeight
        : imageFileSizeHeight;
    qrText = ( qrText == null ) || ( qrText.equals( "" ) ) ? "" : qrText;

    // header log
    String headerLog = "[QRMessage-" + messageId + "] ";

    // log it
    DLog.debug( lctx , headerLog + "Creating qr image file : eventId = "
        + eventId + " , imageFileName = " + imageFileName
        + " , imageFileSizeWidth = " + imageFileSizeWidth
        + " , imageFileSizeHeight = " + imageFileSizeHeight + " , qrText = "
        + StringEscapeUtils.escapeJava( qrText ) );

    // resolve image type
    ImageType imageType = resolveImageType( headerLog , contentType ,
        imageFileName );
    if ( imageType == null ) {
      DLog.warning( lctx , headerLog + "Failed to create qr image file "
          + ", found failed to resolve image type : imageFileName = "
          + imageFileName );
      return created;
    }

    // resolve full path image file name
    String fullPathImageFileName = resolveFullPathImageFileName( headerLog ,
        eventId , messageId , imageFileName , imageType );
    if ( ( fullPathImageFileName == null )
        || ( fullPathImageFileName.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to create qr image file "
          + ", found failed to resolve full path image file name "
          + ": imageFileName = " + imageFileName );
      return created;
    }

    // prepare stream variables to store into file
    File fileQrImage = null;
    FileOutputStream fosQrImage = null;
    ByteArrayOutputStream baosQrImage = null;
    try {

      // trap delta time
      long deltaTime = System.currentTimeMillis();

      // create full path image file
      fileQrImage = new File( fullPathImageFileName );

      // create parent folder if not exist
      fileQrImage.getParentFile().mkdirs();

      // create output stream to image file
      fosQrImage = new FileOutputStream( fileQrImage );

      // create qr image stream
      baosQrImage = QRCode.from( qrText ).to( imageType )
          .withSize( imageFileSizeWidth , imageFileSizeHeight ).stream();

      // write stream to file
      baosQrImage.writeTo( fosQrImage );

      // return as true
      created = true;

      // trap delta time
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log it
      DLog.debug( lctx , headerLog + "Created qr image file ( " + deltaTime
          + " ms ) : name = " + fileQrImage.getAbsolutePath() + " , size = "
          + fileQrImage.length() + " bytes" );

    } catch ( Exception e ) {

      DLog.warning( lctx , headerLog + "Failed to create qr image , " + e );

    } finally {

      // close qr stream
      try {
        if ( baosQrImage != null ) {
          baosQrImage.close();
        }
      } catch ( Exception e ) {
      }

      // close file stream
      try {
        if ( fosQrImage != null ) {
          fosQrImage.close();
        }
      } catch ( Exception e ) {
      }

    }

    return created;
  }

  public boolean copyQrImageFiles( String messageId , int eventId ,
      String savedFolder ) {
    boolean copied = false;

    // clean params
    messageId = ( ( messageId == null ) || ( messageId.equals( "" ) ) ) ? "000"
        : messageId;
    eventId = ( eventId < 0 ) ? 0 : eventId;

    // header log
    String headerLog = "[QRMessage-" + messageId + "] ";

    // resolved saved folder
    if ( ( savedFolder == null ) || ( savedFolder.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to copy qr image files "
          + ", found invalid saved folder = " + savedFolder );
      return copied;
    }

    try {

      // trap delta time
      long deltaTime = System.currentTimeMillis();

      // setup parent path image file
      String parentPathImageFile = resolveParentPathImageFile( headerLog ,
          eventId , messageId );
      if ( ( parentPathImageFile == null )
          || ( parentPathImageFile.equals( "" ) ) ) {
        DLog.warning( lctx , headerLog + "Failed to copy qr image files "
            + ", found blank parent path image file" );
        return copied;
      }

      // setup origin folder
      File originalFolder = new File( parentPathImageFile );

      // setup destination folder
      File destinationFolder = new File( savedFolder );

      // create destination folder if not exist
      destinationFolder.mkdirs();

      // copy all files from origin to destination folder
      FileUtils.copyDirectory( originalFolder , destinationFolder );

      // result as copied
      copied = true;

      // trap delta time
      deltaTime = System.currentTimeMillis() - deltaTime;

      // log it
      DLog.debug( lctx , headerLog + "Copied file(s) from folder ( "
          + deltaTime + " ms ) : " + originalFolder + " -> "
          + destinationFolder );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to copy qr image files , " + e );
    }

    return copied;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ImageType resolveImageType( String headerLog , int contentType ,
      String imageFileName ) {
    ImageType imageType = null;

    // resolve based on content type ( most priority )
    switch ( contentType ) {
    case ContentType.QRPNG :
      imageType = ImageType.PNG;
      break;
    case ContentType.QRGIF :
      imageType = ImageType.GIF;
      break;
    case ContentType.QRJPG :
      imageType = ImageType.JPG;
      break;
    }
    if ( imageType != null ) {
      return imageType;
    }

    // resolve based on file extension( lower priority )
    if ( imageFileName != null ) {
      String imageFileNameLowerCase = imageFileName.toLowerCase();
      if ( imageFileNameLowerCase.endsWith( ".png" ) ) {
        imageType = ImageType.PNG;
      }
      if ( imageFileNameLowerCase.endsWith( ".gif" ) ) {
        imageType = ImageType.GIF;
      }
      if ( imageFileNameLowerCase.endsWith( ".jpg" ) ) {
        imageType = ImageType.JPG;
      }
    }
    if ( imageType != null ) {
      return imageType;
    }

    // resolve based on configuration ( lowest priority )
    if ( imageFileFormat != null ) {
      if ( imageFileFormat.equalsIgnoreCase( "png" ) ) {
        imageType = ImageType.PNG;
      }
      if ( imageFileFormat.equalsIgnoreCase( "gif" ) ) {
        imageType = ImageType.GIF;
      }
      if ( imageFileFormat.equalsIgnoreCase( "jpg" ) ) {
        imageType = ImageType.JPG;
      }
    }
    if ( imageType != null ) {
      return imageType;
    }

    DLog.warning( lctx , headerLog + "Failed to resolve image type based on "
        + ": contentType , extensionFile , and configuration" );

    return imageType;
  }

  private String resolveFullPathImageFileName( String headerLog , int eventId ,
      String messageId , String imageFileName , ImageType imageType ) {
    String fullPathImageFileName = null;

    if ( ( imageFileName == null ) || ( imageFileName.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve full path image "
          + "file name , found blank qr image file name" );
      return fullPathImageFileName;
    }

    StringBuffer sb = new StringBuffer();

    // setup parent path image file
    String parentPathImageFile = resolveParentPathImageFile( headerLog ,
        eventId , messageId );
    if ( ( parentPathImageFile == null ) || ( parentPathImageFile.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve full path image "
          + "file name , found blank parent path image file" );
      return fullPathImageFileName;
    }
    sb.append( parentPathImageFile );

    // setup file name
    if ( !imageFileName.startsWith( "/" ) ) {
      imageFileName = "/".concat( imageFileName );
    }
    sb.append( imageFileName );

    // setup extension file
    String imageFileNameLowerCase = imageFileName.toLowerCase();
    if ( ( imageType.equals( ImageType.PNG ) )
        && ( !imageFileNameLowerCase.endsWith( ".png" ) ) ) {
      sb.append( ".png" );
    }
    if ( ( imageType.equals( ImageType.GIF ) )
        && ( !imageFileNameLowerCase.endsWith( ".gif" ) ) ) {
      sb.append( ".gif" );
    }
    if ( ( imageType.equals( ImageType.JPG ) )
        && ( !imageFileNameLowerCase.endsWith( ".jpg" ) ) ) {
      sb.append( ".jpg" );
    }

    // return as full string
    fullPathImageFileName = sb.toString();
    return fullPathImageFileName;
  }

  private String resolveParentPathImageFile( String headerLog , int eventId ,
      String messageId ) {
    String parentPathImageFile = null;

    if ( ( imageFileLocation == null ) || ( imageFileLocation.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve full path image "
          + "file name , found blank qr image file location " );
      return parentPathImageFile;
    }

    StringBuffer sb = new StringBuffer();

    // setup qr image file base location
    if ( imageFileLocation.endsWith( "/" ) ) {
      imageFileLocation = imageFileLocation.substring( 0 ,
          imageFileLocation.length() - 1 );
    }
    sb.append( imageFileLocation );

    // setup folder event
    sb.append( "/event-" );
    sb.append( eventId );

    // setup folder message id
    sb.append( "/message-" );
    sb.append( messageId );

    // return as full string
    parentPathImageFile = sb.toString();
    return parentPathImageFile;
  }

  public void init( Map mapConfMessageParams ) {

    imageFileLocation = ".";
    imageFileFormat = "png";
    imageFileSizeWidth = imageFileSizeHeight = 128;

    if ( mapConfMessageParams == null ) {
      return;
    }

    String str = null;

    str = (String) mapConfMessageParams.get( "imageFileLocation" );
    if ( ( str != null ) && ( !str.equals( "" ) ) ) {
      imageFileLocation = str;
    }

    str = (String) mapConfMessageParams.get( "imageFileFormat" );
    if ( ( str != null ) && ( !str.equals( "" ) ) ) {
      imageFileFormat = str;
    }

    str = (String) mapConfMessageParams.get( "imageFileSizeWidth" );
    if ( ( str != null ) && ( !str.equals( "" ) ) ) {
      try {
        imageFileSizeWidth = Integer.parseInt( str );
      } catch ( NumberFormatException e ) {
      }
    }

    str = (String) mapConfMessageParams.get( "imageFileSizeHeight" );
    if ( ( str != null ) && ( !str.equals( "" ) ) ) {
      try {
        imageFileSizeHeight = Integer.parseInt( str );
      } catch ( NumberFormatException e ) {
      }
    }

  }

}
