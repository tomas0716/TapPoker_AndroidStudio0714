package com.unity3d.util;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class Util 
{
	private static Util _sharedUtil = null;
	
	private WifiLock wifiLock;
	
	private String signatur0 = "e3488acaff2836f8c5f9570766d88aa9";
	private String signatur1 = "80b01719d81c7621f013208e19b18f89";
	
	public static Util sharedUtil()
	{
		synchronized ( Util.class )
		{
			if ( _sharedUtil == null )
				_sharedUtil = new Util();
		}
		
		return _sharedUtil;
	}
	
	public void init( Activity activity )
	{
		WifiManager wm = (WifiManager) activity.getApplicationContext().getSystemService( Activity.WIFI_SERVICE );
		wifiLock = wm.createWifiLock( getClass().getName() );
		wifiLock.setReferenceCounted( false );
		wifiLock.acquire();

//		showHashKey( activity );
	}
	
	public void onDestroy()
	{
		if ( wifiLock != null )
		{
			wifiLock.release();
			wifiLock = null;
		}
	}
	
	public void onPause()
	{
		if ( wifiLock != null )
		{
			wifiLock.release();
			wifiLock = null;
		}
	}
	
//	public Boolean isRooting()
//	{
//		boolean isRootingFlag = false;
//		String ROOT_PATH = Environment.getExternalStorageDirectory() + "";
//		String ROOTING_PATH_1 = "/system/bin/su";
//		String ROOTING_PATH_2 = "/system/xbin/su";
//		String ROOTING_PATH_3 = "/system/app/SuperUser.apk";
//		String ROOTING_PATH_4 = "/data/data/com.noshufou.android.su";
//		String[] RootFilesPath = new String[]{ ROOT_PATH + ROOTING_PATH_1 , ROOT_PATH + ROOTING_PATH_2 , ROOT_PATH + ROOTING_PATH_3 , ROOT_PATH + ROOTING_PATH_4 };
//		
//		try
//		{
//			Runtime.getRuntime().exec("su");
//			isRootingFlag = true;
//		}
//		catch (Exception e)
//		{
//			isRootingFlag = false;
//		}
//		
//		if ( !isRootingFlag )
//		{
//			isRootingFlag = checkRootingFiles( createFiles( RootFilesPath ) );
//		}
//		
//		return isRootingFlag;
//	}
//	
//	protected File[] createFiles( String[] sfiles )
//	{
//		File[] rootingFiles = new File[sfiles.length];
//		
//		for( int i=0 ; i < sfiles.length; i++ )
//		{
//			rootingFiles[i] = new File(sfiles[i]);
//		}
//		
//		return rootingFiles;
//	}
//	
//	protected Boolean checkRootingFiles( File... file )
//	{
//		boolean result = false;
//		
//		for( File f : file )
//		{
//			if( f != null && f.exists() && f.isFile() )
//			{
//				result = true;
//				break;
//			}
//			else
//			{
//				result = false;
//			}
//		}
//		
//		return result;
//	}
//	
//	public void showHashKey(Context context) 
//	{
//        try 
//        {
//            PackageInfo info = context.getPackageManager().getPackageInfo( "com.freeon.tappoker", PackageManager.GET_SIGNATURES);
//            
//            for ( Signature signature : info.signatures ) 
//            {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
////                DebugLog.log( "KeyHash : " + Base64.encodeToString(md.digest(), Base64.DEFAULT ) );
//            }
//        }
////        Certificate fingerprints:
////        	MD5 : 2C:26:B9:9A:6B:D7:A5:4F:D2:8E:E7:4F:CD:45:B8:5A
////        	SHA1: 18:D6:85:5F:67:B3:4F:24:0D:7C:4C:FD:92:55:18:42:E0:71:15:51
//        catch (NameNotFoundException e) 
//        {
//
//        } 
//        catch (NoSuchAlgorithmException e) 
//        {
//
//        }
//    }
	
	/*
	public void checkSignatur( Context context )
	{
		try 
        {
			PackageManager pm = context.getApplicationContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo( context.getPackageName(), PackageManager.GET_SIGNATURES );
 
            for (int i = 0; i < packageInfo.signatures.length; i++) 
            {
                Signature signature = packageInfo.signatures[i];
                
//                DebugLog.log( "SIGNATURE : " + signature.toCharsString() );
                
                String decrypt = NetworkManager.sharedNetworkManager().GetSeedDecrypt( signature.toCharsString() );
                String md5String = md5( decrypt );
                String md5String2 = md5( signature.toCharsString() );
                
//                DebugLog.log( "decrypt : " + decrypt );
//                DebugLog.log( "md5String : " + md5String );
//                DebugLog.log( "md5String2 : " + md5String2 );
            }
        } 
		catch (NameNotFoundException e) 
		{
            e.printStackTrace();
	    }
	}
	*/
	/*
	private String md5(final String s) 
	{
		final String MD5 = "MD5";
		try
		{
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	
	        // Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        
	        for (byte aMessageDigest : messageDigest) 
	        {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        
	        return hexString.toString();
		}
		catch (NoSuchAlgorithmException e) 
		{
		        e.printStackTrace();
		}
		    return "";
	}
	*/
}
