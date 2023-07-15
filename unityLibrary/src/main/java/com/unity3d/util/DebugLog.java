package com.unity3d.util;

import android.util.Log;

public class DebugLog 
{
	private static DebugLog _sharedDebugLog = null;
	static Boolean visible = false;
	
	private DebugLog()
	{
//		visible = true;
	}
	
	public static DebugLog sharedDebugLog()
	{
		synchronized ( DebugLog.class )
		{
			if ( _sharedDebugLog == null )
				_sharedDebugLog = new DebugLog();
		}
		
		return _sharedDebugLog;
	}
	
	public void clear()
	{
		_sharedDebugLog = null;
	}
	
	public static void log( String string )
	{
		if ( visible )
			Log.d( "DebugLog", string );
	}
}
