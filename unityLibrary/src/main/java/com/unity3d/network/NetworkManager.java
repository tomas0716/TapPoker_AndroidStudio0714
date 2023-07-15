package com.unity3d.network;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.unity3d.util.DebugLog;

public class NetworkManager 
{
	private static NetworkManager _sharedNetworkManager = null;
	private HashMap<String, String> localizableMap;

	public CurrencyType currencyType;
	public String thmID, sessionID;
	
	public String appLanguage, countryCode, osVersion, apnsToken, localizable, deviceID;
	public String loginID, loginAccessToken, loginName;
	
	public static NetworkManager sharedNetworkManager()
	{
		synchronized ( NetworkManager.class )
		{
			if ( _sharedNetworkManager == null )
				_sharedNetworkManager = new NetworkManager();
		}
		
		return _sharedNetworkManager;
	}
	
	public void init( Activity activity )
	{
		currencyType = new CurrencyType();
		
		/**
		 * English (United States)
		 * Korean
		 * Chinese (Simplified)
		 * French (France)
		 * German (Germany)
		 * Greek
		 * Italian (Italy)
		 * Japanese
		 * Portuguese (Portugal)
		 * Russian (Russia)
		 * Spanish (Spain)
		 * Thai
		 * Vietnamese
		 * Arabic (Algeria)
		 * Chinese (Traditional)
		 */
		
		localizableMap = new HashMap<String, String>();
		localizableMap.put( "en", "English (United States)" );
		localizableMap.put( "ko", "Korean" );
		localizableMap.put( "zh-Hans", "Chinese (Simplified)" );
		localizableMap.put( "fr", "French (France)" );
		localizableMap.put( "de", "German (Germany)" );
		localizableMap.put( "el", "Greek" );
		localizableMap.put( "it", "Italian (Italy)" );
		localizableMap.put( "ja", "Japanese" );
		localizableMap.put( "pt", "Portuguese (Portugal)" );
		localizableMap.put( "ru", "Russian (Russia)" );
		localizableMap.put( "es", "Spanish (Spain)" );
		localizableMap.put( "th", "Thai" );
		localizableMap.put( "vi", "Vietnamese" );
		localizableMap.put( "ar", "Arabic (Algeria)" );
		localizableMap.put( "zh-Hant", "Chinese (Traditional)" );
				
		//market = "APPLEMARKET";
		//market = "ANDROIDMARKET";
		//market = "ANDROIDMARKET_KR";
		//market = "SAMSUNG";
		//market = "TSTORE";
		
		countryCode = Locale.getDefault().getCountry();
		osVersion = "ANDROID" + android.os.Build.VERSION.RELEASE;
		deviceID = Secure.getString( activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		appLanguage = "en";
		
		String strLanguage = Locale.getDefault().getLanguage();
		String[] countryName = { "de", "el", "en", "es", "fi", "fr", "it", "ja", "ko", "pt", "pt-PT", "ru", "th", "zh", "zh-rcn", "zh-rtw", "vi", "ar_EG", "ar_IL", "ar", "id", "hi", "ms", "nl", "ro" };

		
		for ( String string : countryName )
		{
			if ( strLanguage.equals( string ) )
			{
				appLanguage = strLanguage;
				break;
			}
		}
		
		if ( appLanguage.equals( "zh" ) || appLanguage.equals( "zh-rcn" ) )
			appLanguage = "zh-Hans";
		else if ( appLanguage.equals( "zh-rtw" ) ) 
			appLanguage = "zh-Hant";
		else if ( appLanguage.equals( "ar_EG" ) || appLanguage.equals( "ar_IL" ) )
			appLanguage = "ar";
		
		localizable = "English (United States)";
		String localString = localizableMap.get( appLanguage );
		if ( localString != null )
			localizable = localString;
		
		DebugLog.log( "Locale country : " + countryCode );
		DebugLog.log( "localizable : " + localizable );
		DebugLog.log( "appLanguage : " + appLanguage );
		
		String[] eurCodeArray = { "ES", "PT", "GR", "NL", "BE", "NO", "DE", "LU", "LI", "CH", 
				  				  "AT", "DK", "SK", "IT", "FR", "TR", "HU", "FI", "SE", "PL",
				  				  "HR", "GB", "UA", "RO", "CZ" };
		
		currencyType.type = CurrencyType.kCurrency_USD;
		
		if ( countryCode.equals( "KR" ) )
		{
			currencyType.type = CurrencyType.kCurrency_KRW;
		}
		else if ( countryCode.equals( "CN" ) )
		{
			currencyType.type = CurrencyType.kCurrency_CNY;
		}
		else if ( countryCode.equals( "JP" ) )
		{
			currencyType.type = CurrencyType.kCurrency_JPY;
		}
		else if ( countryCode.equals( "BR" ) )
		{
			currencyType.type = CurrencyType.kCurrency_BRL;
		}
		else if ( countryCode.equals( "RU" ) )
		{
			currencyType.type = CurrencyType.kCurrency_RUB;
		}
		else if ( countryCode.equals( "MY" ) )
		{
			currencyType.type = CurrencyType.kCurrency_MYR;
		}
		else if ( countryCode.equals( "HK" ) )
		{
			currencyType.type = CurrencyType.kCurrency_HKD;
		}
		else if ( countryCode.equals( "TH" ) )
		{
			currencyType.type = CurrencyType.kCurrency_THB;
		}
		else if ( countryCode.equals( "VN" ) )
		{
			currencyType.type = CurrencyType.kCurrency_VND;
		}
		else if ( countryCode.equals( "ID" ) )
		{
			currencyType.type = CurrencyType.kCurrency_IDR;
		}
		else if ( countryCode.equals( "MX" ) )
		{
			currencyType.type = CurrencyType.kCurrency_MXN;
		}
		else if ( countryCode.equals( "AR" ) )
		{
			currencyType.type = CurrencyType.kCurrency_ARS;
		}
		else if ( countryCode.equals( "TW" ) )
		{
			currencyType.type = CurrencyType.kCurrency_TWD;
		}
		else if ( countryCode.equals( "GB" ) )
		{
			currencyType.type = CurrencyType.kCurrency_GBP;
		}
		else if ( countryCode.equals( "NZ" ) )
		{
			currencyType.type = CurrencyType.kCurrency_NDZ;
		}
		else if ( countryCode.equals( "CH" ) )
		{
			currencyType.type = CurrencyType.kCurrency_CHF;
		}
		else if ( countryCode.equals( "NO" ) )
		{
			currencyType.type = CurrencyType.kCurrency_NOK;
		}
		else if ( countryCode.equals( "DK" ) )
		{
			currencyType.type = CurrencyType.kCurrency_DKK;
		}
		else if ( countryCode.equals( "SE" ) )
		{
			currencyType.type = CurrencyType.kCurrency_SEK;
		}
		else if ( countryCode.equals( "AU" ) )
		{
			currencyType.type = CurrencyType.kCurrency_AUS;
		}
		else if ( countryCode.equals( "SG" ) )
		{
			currencyType.type = CurrencyType.kCurrency_SGD;
		}
		else
		{
			for ( String code : eurCodeArray )
			{
				if ( countryCode.equals( code ) )
				{
					currencyType.type = CurrencyType.kCurrency_EUR;
					break;
				}
			}
		}
//			DebugLog.log( "currencyType.toString() : " + String.valueOf( currencyType.type ) );
	}
}
