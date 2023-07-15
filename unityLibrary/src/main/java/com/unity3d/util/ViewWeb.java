package com.unity3d.util;

import com.unity3d.player.R;
import com.unity3d.player.UnityPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ViewWeb extends RelativeLayout
{
	private FrameLayout mainLayout;
	private WebView webView;
//	Util util;
	private String unityScene, url;
	
	public ViewWeb( Context context, AttributeSet attrs ) 
	{
		super( context, attrs );
//		util = Util.sharedUtil();
	}

	public void init( FrameLayout mainLayout )
	{
		this.mainLayout = mainLayout;
		
//		url = "file:///android_asset/tappoker.htm";
//		url = "http://blog.naver.com/mfreeon";
//		url = "http://blog.naver.com/crazymobile";

		webView = (WebView) findViewById( R.id.webView_web );
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setJavaScriptEnabled( true );
		webView.setWebViewClient( new WebViewClientClass() );
	}
	
	public void addView( String unityScene, String url )
	{
		this.url = url;
		this.unityScene = unityScene;
		
		webView.clearHistory();
		webView.loadUrl( url );
		mainLayout.addView( this );
	}
	
	public void removeView()
	{
		webView.loadUrl( "about:blank" );
		mainLayout.removeView( this );
		UnityPlayer.UnitySendMessage( unityScene, "RemovepWebView", "" );
	}
	
    private class WebViewClientClass extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) 
        {
            view.loadUrl(url);
            return true;
        }
    }
    
    public void clearKey()
    {
    	WebBackForwardList webBackForwardList = webView.copyBackForwardList();
    	int currentIndex = webBackForwardList.getCurrentIndex();
    	
    	DebugLog.log( "webView.canGoBack()  : " + webView.canGoBack() );
    	DebugLog.log( "webBackForwardList.getSize() : " + webBackForwardList.getSize() );
    	DebugLog.log( "currentIndex : " + currentIndex);
    	
    	if ( currentIndex == 0 )
    	{
    		removeView();
    		return;
    	}
    	
    	if (  webBackForwardList.getSize() > 0 & currentIndex > 0 )
    	{
    		String backUrl = webBackForwardList.getItemAtIndex( webBackForwardList.getCurrentIndex() - 1 ).getUrl();
    		DebugLog.log( "backUrl : " + backUrl );
    		
    		if ( backUrl.equals( url ) || backUrl.equals( "about:blank" ) )
    		{
    			removeView();
    			return;
    		}
    	}
    	
    	if ( webView.canGoBack() )
    	{
    		webView.goBack();
    	}
    	else
    	{
    		removeView();
    	}
    }
}
