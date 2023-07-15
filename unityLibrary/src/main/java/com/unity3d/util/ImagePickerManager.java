package com.unity3d.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.unity3d.player.R;

public class ImagePickerManager
{
	public static final byte PICK_FROM_CAMERA = 0;
	public static final byte PICK_FROM_ALBUM = 1;
	public static final byte CROP_FROM_CAMERA = 2;
	public static final byte CROP_FROM_ALBUM = 3;
	
	private static ImagePickerManager _sharedImagePickerManager = null;
	
	Uri mImageCaptureUri;
	Activity activity;
//	PhotoManager photoManager;
	public ImagePickerListener listener = null;
	
	private Uri mTempImageUri;
	
	private ImagePickerManager()
	{
		
	}
	
	public static ImagePickerManager sharedImagePickerManager()
	{
		synchronized ( ImagePickerManager.class )
		{
			if ( _sharedImagePickerManager == null )
				_sharedImagePickerManager = new ImagePickerManager();
		}
		
		return _sharedImagePickerManager;
	}
	
	public void init( Activity activity )
	{
		this.activity = activity;
//		photoManager = PhotoManager.sharedPhotoManager();
	}
	
	public void clear()
	{
		_sharedImagePickerManager = null;
	}
	
	public void showImagePickerView( ImagePickerListener listener )
	{
		this.listener = listener;

		new AlertDialog.Builder(activity)
		.setTitle(" ")
		.setPositiveButton( R.string.Camera, cameraListener)
		.setNeutralButton( R.string.Photo_library, albumListener)
		.setNegativeButton( R.string.Cancel, cancelListener)
		.setOnCancelListener( cancelKeyListener )
		.show();
	}
	
	public void cancelPickerImage()
	{
		if ( listener != null )
		{
			listener.cancelPickerImage();
			listener = null;
		}
	}
	
	DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			String url = "tmp_" + String.valueOf( System.currentTimeMillis() ) + ".jpg";
			mImageCaptureUri = Uri.fromFile( new File( Environment.getExternalStorageDirectory(), url ) );
			
			Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			intent.putExtra( MediaStore.EXTRA_OUTPUT, mImageCaptureUri );
			intent.putExtra( "return-data", true );
			activity.startActivityForResult( intent, PICK_FROM_CAMERA );
		}
	};
	
	DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			Intent intent = new Intent( Intent.ACTION_PICK );
			intent.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE );
			activity.startActivityForResult( intent, PICK_FROM_ALBUM );
		}
	};
	
	DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
			cancelPickerImage();
		}
	};
	
	DialogInterface.OnCancelListener cancelKeyListener = new DialogInterface.OnCancelListener()
	{
		
		@Override
		public void onCancel(DialogInterface dialog) 
		{
			dialog.dismiss();
			cancelPickerImage();
		}
	};
	
	public void cropPhotoAlbum( Intent data )
	{
		mImageCaptureUri = data.getData();
		cropCamera( CROP_FROM_ALBUM );
	}
	
	public void cropCamera( int index )
	{
		
		try
		{
			mTempImageUri = Uri.fromFile( getTempFile() );
			
			Intent intent = new Intent( "com.android.camera.action.CROP" );
	        intent.setDataAndType( mImageCaptureUri, "image/*" );			
//	        intent.putExtra("crop", "true");
	        //indicate aspect of desired crop
//	        intent.putExtra("aspectX", 4);
//	        intent.putExtra("aspectY", 3);
	        //indicate output X and Y
	        intent.putExtra("aspectX", 1);
	        intent.putExtra("aspectY", 1);
	        intent.putExtra("outputX", 400);
	        intent.putExtra("outputY", 400);
	        intent.putExtra("scale", true);
	        intent.putExtra( MediaStore.EXTRA_OUTPUT, mTempImageUri );
	        
//	        File f = new File(Environment.getExternalStorageDirectory(), "/temporary_holder.jpg");
//	            
//	        try {
//	                f.createNewFile();
//	            } 
//	        catch (IOException ex) 
//	        {
//	        	DebugLog.log("io : " + ex.getMessage());  
//	           }

	        activity.startActivityForResult( intent, index );
		}
		catch( ActivityNotFoundException e )
		{
			DebugLog.log( "========= e : "+ e.toString() );
		}
	}
	
	private File getTempFile(){

		File file = new File( Environment.getExternalStorageDirectory(), "/temp_crop.jpg"  );

		try
		{

			file.createNewFile();

		}

		catch( Exception e ){

			DebugLog.log( "fileCreation fail" );

		}

		return file;

		}
	
	public void finishImage( Intent data )
	{
		File tempFile = getTempFile();
		
		if ( tempFile.exists() )
		{
			listener.finishPickerImage( tempFile.getPath() );
		}
		else
		{
			listener.finishPickerImage( "" );
		}
	}

	public Bitmap getContactBitmapFromURI(Context context, Uri uri)
	{
		try
		{

			InputStream input = context.getContentResolver().openInputStream(uri);

			if (input == null)
				return null;

			return BitmapFactory.decodeStream(input);
		}
		catch (FileNotFoundException e)
		{

		}

		return null;

	}

	public String saveBitmapIntoFileImage(Context context, Bitmap finalBitmap)
	{
		String ret = "";
		String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
		String imageFileName = "img_" + timeStamp + "_";
		File storageDir = new File(context.getExternalCacheDir() + "/FREEON/");

		if (!storageDir.exists())
			storageDir.mkdirs();

		try
		{
			File image = File.createTempFile(imageFileName, ".jpg", storageDir);
			//ret = "file:" + image.getAbsolutePath();
			ret = image.getAbsolutePath();

			FileOutputStream out = new FileOutputStream(image);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			DebugLog.log( "Exception : " + e.toString() );
			ret = "";
		}

//        DebugLog.log( "saveBitmapIntoFileImage ret : " + ret );

		return ret;
	}
	
	/*
	public void finishImage( Intent data )
	{
		DebugLog.log( "================ finishImage" );
		
		File tempFile = getTempFile();
		
		if ( tempFile.exists() )
		{
			try 
			{
				FileInputStream fis = new FileInputStream( tempFile );
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				
				for (int readNum; (readNum = fis.read(buf)) != -1;) 
				{
					bos.write( buf, 0, readNum ); //no doubt here is 0
					//Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
		            System.out.println("read " + readNum + " bytes,");
				}
				
				 byte[] bytes = bos.toByteArray();
				 
				 fis.close();
				 
				 listener.finishPickerImage( bytes );
			}
			catch (IOException e) 
			{
				listener.finishPickerImage( null );
			}
			
			
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			        
//			try {
//				
//				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream( tempFile ), null, options);
////				Bitmap resizePhoto = Bitmap.createScaledBitmap( bitmap, 400, 400, true );
////				
////				DebugLog.log( "bitmap.getWidth() : " + bitmap.getWidth() );
////				DebugLog.log( "bitmap.getHeight() : " + bitmap.getHeight() );
////				
////				DebugLog.log( "resizePhoto.getWidth() : " + resizePhoto.getWidth() );
////				DebugLog.log( "resizePhoto.getHeight() : " + resizePhoto.getHeight() );
//				
////				File bitmapFile = new File( photoManager.avatarPath );
////				bitmapFile.createNewFile();
////				
////				OutputStream out = new FileOutputStream( bitmapFile );
//////				resizePhoto.compress( CompressFormat.PNG, 100, out );
////				bitmap.compress( CompressFormat.PNG, 100, out );
////				out.close();
////				
////				File file = new File( mImageCaptureUri.getPath() );
////				
////				if ( file.exists() )
////					file.delete();
////				
////				tempFile.delete();
//
//			}
//			catch (FileNotFoundException e) 
//			{
//				DebugLog.log( "finishImage FileNotFoundException: " + e.toString() );
////				e.printStackTrace();
//			}
//			catch (IOException e) 
//			{
////				e.printStackTrace();
//				DebugLog.log( "finishImage IOException : " + e.toString() );
//			}
			
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			imgBtnUserPhoto.setImageBitmap( photoManager.loadAvatar() );
//			photoBitmap.compress( CompressFormat.PNG, 100, stream );
		}
		else
		{
			listener.finishPickerImage( null );
		}
		
		listener = null;
	}
	*/
}
