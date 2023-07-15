package com.unity3d.network;

import android.app.Activity;
import android.widget.Toast;

import com.lockincomp.liappagent.LiappAgent;
import com.unity3d.util.DebugLog;

public class LiappManager
{
	private static LiappManager _sharedLiappManager = null;
	private Activity activity;
	
	private int nRet = 0;
	private boolean bIsStarted = false;
	
	public static LiappManager sharedLiappManager()
	{
		synchronized ( LiappManager.class )
		{
			if ( _sharedLiappManager == null )
				_sharedLiappManager = new LiappManager();
		}
		
		return _sharedLiappManager;
	}
	
	public void init( Activity activity )
	{
		this.activity = activity;
	}
	
	private void checkLA()
	{
		if ( bIsStarted == false )
		{
			nRet = LiappAgent.LA1();
		}
		else
		{
			nRet = LiappAgent.LA2();
		}

		if ( LiappAgent.LIAPP_EXCEPTION == nRet )
		{
			Toast.makeText( activity, LiappAgent.GetMessage(), Toast.LENGTH_LONG ).show();
		}
		else if( LiappAgent.LIAPP_DETECTED == nRet )
		{
			Toast.makeText( activity, LiappAgent.GetMessage(), Toast.LENGTH_LONG ).show();
		}
		else
		{
//			android.util.Log.i( "Liapp", "Liapp Auth : " + LiappAgent.A1() );
			bIsStarted = true;
		}

		DebugLog.log( "liapp create : " + nRet );

		if ( LiappAgent.LIAPP_SUCCESS != nRet )
			activity.finish();
	}
	
	public void onCreate()
	{
		checkLA();
	}
	
	public void onRestart()
	{
		int liRet = LiappAgent.LA2();

		DebugLog.log( "Liapp LA2 : " + liRet );

		if ( liRet == LiappAgent.LIAPP_EXCEPTION )
		{
			DebugLog.log( "LIAPP_EXCEPTION LiappAgent.GetMessage() : " + LiappAgent.GetMessage() );
			activity.finish();
		}
		else if ( liRet == LiappAgent.LIAPP_DETECTED )
		{
			DebugLog.log( "LIAPP_DETECTED LiappAgent.GetMessage() : " + LiappAgent.GetMessage() );
			activity.finish();
		}
	}

	public String checkA1()
	{
		checkLA();
		//String authkey = "" + LiappAgent.A1();
		String authkey = "" + LiappAgent.GA( NetworkManager.sharedNetworkManager().loginID );
		DebugLog.log( "Liapp A1 : " + authkey );
		return authkey;
//		return "";
	}
}
