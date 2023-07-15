package com.unity3d.player;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.unity3d.network.LiappManager;
import com.unity3d.network.NetworkManager;
import com.unity3d.util.AudioVoiceManager;
import com.unity3d.util.DebugLog;
import com.unity3d.util.ImagePickerListener;
import com.unity3d.util.ImagePickerManager;
import com.unity3d.util.InAppManager;
import com.unity3d.util.Util;
import com.unity3d.util.ViewWeb;

import java.io.File;
import java.util.Arrays;

public class UnityPlayerActivity extends Activity implements IUnityPlayerLifecycleEvents, ImagePickerListener, GoogleApiClient.OnConnectionFailedListener
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

    // Override this in your custom UnityPlayerActivity to tweak the command line arguments passed to the Unity Android Player
    // The command line arguments are passed as a string, separated by spaces
    // UnityPlayerActivity calls this from 'onCreate'
    // Supported: -force-gles20, -force-gles30, -force-gles31, -force-gles31aep, -force-gles32, -force-gles, -force-vulkan
    // See https://docs.unity3d.com/Manual/CommandLineArguments.html
    // @param cmdLine the current command line arguments, may be null
    // @return the modified command line string or null

    private static final int RC_SIGN_IN = 10;

    private NetworkManager networkManager;
    private Util util;
    private ImagePickerManager imagePickerManager;
    private InAppManager inappManager;

    private FrameLayout mainLayout;
    private ViewWeb viewWeb;

    private String strUnityScene, photoUploadScene;

    /* facebook	 */
//	private Boolean bConnectFacebook = false;
    private CallbackManager callbackManager;

    /* google */
    private GoogleApiClient mGoogleApiClient;
    private Tracker tracker;

    /* voiceRecord */
    private AudioVoiceManager audioVoiceManager;


    protected String updateUnityCommandLineArguments(String cmdLine)
    {
        return cmdLine;
    }

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);

        mUnityPlayer = new UnityPlayer(this, this);
        setContentView( R.layout.tappokerse );
        mUnityPlayer.requestFocus();

        mainLayout = (FrameLayout) findViewById( R.id.mainLayout );

        FrameLayout unityLayout = (FrameLayout) findViewById( R.id.unityLayout );
        unityLayout.addView( mUnityPlayer.getView() );

        DebugLog.sharedDebugLog();
        util = Util.sharedUtil();

        networkManager = NetworkManager.sharedNetworkManager();
        networkManager.init( this );

        imagePickerManager = ImagePickerManager.sharedImagePickerManager();
        imagePickerManager.init( this );

        inappManager = inappManager.sharedInAppManager();
        inappManager.init( this );

        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        viewWeb = (ViewWeb) inflater.inflate( R.layout.view_web, null );
        viewWeb.init( mainLayout );

        /* Liapp */
        LiappManager.sharedLiappManager().init( this );
        LiappManager.sharedLiappManager().onCreate();


        /* google */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestEmail()
                .requestIdToken( "197604352475-fv4uoaonspufdkp5jie5uuvvco8bbnht.apps.googleusercontent.com" )
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener( this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        FirebaseApp.initializeApp( this );

//        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>()
//        {
//            @Override
//            public void onSuccess(InstanceIdResult instanceIdResult)
//            {
//                String deviceToken = instanceIdResult.getToken();
//                networkManager.apnsToken = deviceToken;
//
//                // Do whatever you want with your token now
//                // i.e. store it on SharedPreferences or DB
//                // or directly send it to server
//            }
//        });

//		DebugLog.log( "checkPlayServices : " + checkPlayServices() );

//        String token = FirebaseInstanceId.getInstance().getToken();

//        if ( token != null && token.length() > 0 )
//            networkManager.apnsToken = "";

        DebugLog.log( "networkManager.apnsToken : " + networkManager.apnsToken );

        GoogleAnalytics analytics = GoogleAnalytics.getInstance( this );
        tracker = analytics.newTracker( getString( R.string.ga_trackingId ) );
        tracker.enableExceptionReporting( true );
        tracker.enableAdvertisingIdCollection( true );
        tracker.enableAutoActivityTracking( true );

        /* FaceBook */
//		FacebookSdk.sdkInitialize( getApplicationContext() );
        callbackManager = CallbackManager.Factory.create();

        /* voice */
        audioVoiceManager = new AudioVoiceManager( this );

        LoginManager.getInstance().registerCallback( callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess( LoginResult loginResult )
            {
                DebugLog.log( "LoginManager onSuccess" );
                DebugLog.log( "loginResult.getAccessToken().getToken() : " + loginResult.getAccessToken().getToken() );
                DebugLog.log( "loginResult.getAccessToken().getUserId() : " + loginResult.getAccessToken().getUserId() );

                networkManager.loginID = loginResult.getAccessToken().getUserId();
                networkManager.loginName = "GUEST";
                networkManager.loginAccessToken = loginResult.getAccessToken().getToken();

                UnityPlayer.UnitySendMessage(strUnityScene, "LoginFacebook", "true");
            }

            @Override
            public void onCancel()
            {
                DebugLog.log( "LoginManager onCancel" );

                UnityPlayer.UnitySendMessage(strUnityScene, "LoginFacebook", "false");
            }

            @Override
            public void onError( FacebookException exception )
            {
                DebugLog.log( "LoginManager onError" );

                UnityPlayer.UnitySendMessage(strUnityScene, "LoginFacebook", "false");
            }
        });

        /* unity ads */
//        UnityAds.initialize( this, "84382", this );

    }

    // When Unity player unloaded move task to background
    @Override public void onUnityPlayerUnloaded() {
        moveTaskToBack(true);
    }

    // Callback before Unity player process is killed
    @Override public void onUnityPlayerQuitted() {
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
        mUnityPlayer.newIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    // If the activity is in multi window mode or resizing the activity is allowed we will use
    // onStart/onStop (the visibility callbacks) to determine when to pause/resume.
    // Otherwise it will be done in onPause/onResume as Unity has done historically to preserve
    // existing behavior.
    @Override protected void onStop()
    {
        super.onStop();

        if (!MultiWindowSupport.getAllowResizableWindow(this))
            return;

        mUnityPlayer.pause();
    }

    @Override protected void onStart()
    {
        super.onStart();

        if (!MultiWindowSupport.getAllowResizableWindow(this))
            return;

        mUnityPlayer.resume();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();

        MultiWindowSupport.saveMultiWindowMode(this);

        if (MultiWindowSupport.getAllowResizableWindow(this))
            return;

        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();

        if (MultiWindowSupport.getAllowResizableWindow(this) && !MultiWindowSupport.isMultiWindowModeChangedToTrue(this))
            return;

        mUnityPlayer.resume();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        DebugLog.log( "===========requestCode : "+ requestCode + " == resultCode : " + resultCode );

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult( requestCode, resultCode, data );

        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            DebugLog.log( "result.isSuccess() : " + result.isSuccess() );

            if ( result.isSuccess() )
            {
                GoogleSignInAccount acct = result.getSignInAccount();
                // Get account information
                String mloginID = acct.getId();
                String mFullName = acct.getDisplayName();
                String mEmail = acct.getEmail();
                String token = acct.getIdToken();

                networkManager.loginID = mloginID;
                networkManager.loginName = mFullName;
                networkManager.loginAccessToken = token;

                DebugLog.log( "========== mloginID : " + mloginID );
                DebugLog.log( "========== mFullName : " + mFullName );
                DebugLog.log( "========== mEmail : " + mEmail );
                DebugLog.log( "========== token : " + token );

                successLoginGoogle();
            }
            else
            {
                failLoginGoogle();
            }

            return;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
//                DebugLog.log( "RESULT_OK");
                Uri resultUri = result.getUri();
                Bitmap img = imagePickerManager.getContactBitmapFromURI( this,resultUri );

                if ( img == null )
                {
                    finishPickerImage( "" );
                }
                else
                {
                    String ret = imagePickerManager.saveBitmapIntoFileImage(this, img);
                    finishPickerImage( ret );
                }


            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
//                Exception error = result.getError();
                finishPickerImage( "" );
            }
            else
            {
                finishPickerImage( "" );
            }
        }

        /*
        if ( imagePickerManager.listener == null )
            return;

        if ( resultCode != RESULT_OK )
        {
            imagePickerManager.cancelPickerImage();
            return;
        }

        switch ( requestCode )
        {
            case ImagePickerManager.CROP_FROM_CAMERA:

                imagePickerManager.finishImage( data );

                break;

            case ImagePickerManager.CROP_FROM_ALBUM:

                imagePickerManager.finishImage( data );

                break;

            case ImagePickerManager.PICK_FROM_ALBUM:

                imagePickerManager.cropPhotoAlbum( data );

                break;

            case ImagePickerManager.PICK_FROM_CAMERA:

                imagePickerManager.cropCamera( ImagePickerManager.CROP_FROM_CAMERA);

                break;
        }
         */
    }



    /* unity ads */
/*
    @Override
    public void onUnityAdsReady (String placementId) {
        // Implement functionality for an ad being ready to show.
    }

    @Override
    public void onUnityAdsStart (String placementId)
    {
        // Implement functionality for a user starting to watch an ad.
    }

        @Override
    public void onUnityAdsFinish (String placementId, UnityAds.FinishState finishState)
    {
//		DebugLog.log( "onUnityAdsFinish : " + placementId );
//		DebugLog.log( "finishState : " + finishState );
//        DebugLog.log( "retUnityScene : " + retUnityScene );

        if ( finishState == UnityAds.FinishState.COMPLETED )
        {
            UnityPlayer.UnitySendMessage( "MainMenuScene", "FinishUnityAds", "true" );
        }
    }

    @Override
    public void onUnityAdsError (UnityAds.UnityAdsError error, String message)
    {
        UnityPlayer.UnitySendMessage( "MainMenuScene", "FinishUnityAds", "false");
    }

*/



    @Override
    public void finishPickerImage(String fname)
    {
        UnityPlayer.UnitySendMessage( photoUploadScene, "FinishPickerImage", fname );
    }

    @Override
    public void cancelPickerImage()
    {
        UnityPlayer.UnitySendMessage( photoUploadScene, "FinishPickerImage", "" );
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult )
    {
        // TODO Auto-generated method stub
        DebugLog.log( "========== onConnectionFailed ============== " );

        if ( !connectionResult.hasResolution() )
        {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            googleAPI.getErrorDialog( this, connectionResult.getErrorCode(), 0 ).show();;
        }

        failLoginGoogle();
    }

    /* unity */
    public String getLocalizableString( String msg )
    {
        return networkManager.localizable;
    }

    public String getCurrencyType( String msg )
    {
        return String.valueOf( networkManager.currencyType.type );
    }

    public String getAppLanguage( String msg )
    {
        return networkManager.appLanguage;
    }

    public String getCountry( String msg )
    {
        return networkManager.countryCode;
    }

    public String getOSVersion( String msg )
    {
        return networkManager.osVersion;
    }

    public String getDeviceID( String msg )
    {
        DebugLog.log( "networkManager.deviceID : " + networkManager.deviceID );
        return networkManager.deviceID;
    }

    public String getApnsToken( String msg )
    {
        return networkManager.apnsToken;
    }

    public String[] getInAppPrice( String msg )
    {
        DebugLog.log( "price.size : " + inappManager.inAppPrice.size() );

        String[] price = new String[inappManager.inAppPrice.size()];

        for( int i = 0; i < price.length; i++ )
        {
            price[i] = inappManager.inAppPrice.get(i);
            DebugLog.log( "price["+i+"] : " + price[i]);
        }

        return price;
    }

    public void showImagePicker( String msg )
    {
        photoUploadScene = msg;

        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                showImagePicker();
            }
        });
    }

    public void connectFacebook( String msg )
    {
        strUnityScene = msg;

        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                loginFacebook();
            }
        });
    }

    public void connectGoogle( String msg )
    {
        strUnityScene = msg;
        loginGoogle();
    }

    public void signout( String msg )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                if ( accessToken != null )
                    LoginManager.getInstance().logOut();

                DebugLog.log( "mGoogleApiClient.isConnected() : " + mGoogleApiClient.isConnected() );

                if ( mGoogleApiClient.isConnected() )
                    mGoogleApiClient.disconnect();
            }
        });
    }

    public void initChargeCode( String msg )
    {
        inappManager.inAppCodeArray.clear();
        inappManager.moneyMap.clear();
    }

    public void setChargeCode( String code, String price )
    {
        DebugLog.log( "setChargeCode code : " + code + "  setChargeCode price : " + price );
        inappManager.inAppCodeArray.add( code );
        inappManager.moneyMap.put( code, price );

    }

    public void executePurchase( String msg )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                inappManager.executePurchase();
            }
        });
    }

    public void launchPurchase( String msg )
    {
        inappManager.launchPurchase( msg, this );
    }

    public void showHelpView( String msg )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                viewWeb.addView( "MainMenuScene", "http://www.tappoker.net/" );
            }
        });
    }

    public void clearKey( String msg )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                viewWeb.clearKey();
            }
        });
    }

    public String getLoginID( String msg )
    {
        return networkManager.loginID;
    }

    public String getLoginAccessToken( String msg )
    {
        return networkManager.loginAccessToken;
    }

	/*
	public String getLoginName( String msg )
	{
		return networkManager.loginName;
	}
	*/

    public void googleAnalytics( String msg )
    {
        DebugLog.log( "googleAnalytics : " + msg );
        tracker.setScreenName( msg );
        tracker.send( new HitBuilders.ScreenViewBuilder().build() );
    }

    public void sendEmail( String msg )
    {
        Uri uri = Uri.fromFile( new File( getApplicationContext().getExternalFilesDir( null ), "/Screenshot.png" ) );
        String[] addr = { "teamwithplay@gmail.com" };

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND );
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, addr );
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"REPORT ABUSE");
        emailIntent.setType( "application/image" );
        //storage/emulated/0/Android/data/com.freeon.sevenpokeronline/files
//		DebugLog.log( "uri : " + uri );

        emailIntent.putExtra( Intent.EXTRA_STREAM, uri );
        startActivity( emailIntent );
    }

    public void savePhotoLibrary( String msg )
    {
        ContentValues values = new ContentValues();

        values.put( MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put( MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put( MediaStore.MediaColumns.DATA, getApplicationContext().getExternalFilesDir( null ) + "/Screenshot.png" );

        getApplicationContext().getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );
    }

    private File getTempFile(){

        File file = new File( Environment.getExternalStorageDirectory(), "/test.amr"  );

        try
        {

            file.createNewFile();

        }

        catch( Exception e ){

            DebugLog.log( "fileCreation fail" );

        }

        return file;
    }

    public String checkRecordVoice( String msg )
    {
        String ret = audioVoiceManager.checkRecode();

        return ret;
    }

    public void startRecordVoice( String msg )
    {
        DebugLog.log( "========== startvoicerecord ============" );
//		audioVoiceManager.startRecord();

        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                audioVoiceManager.startRecord();
            }
        });
    }

    public void stopRecordVoice( String msg )
    {
        DebugLog.log( "========== stopVoiceRecord ============" );
//		audioVoiceManager.stopRecord();

        runOnUiThread( new Runnable() {
            @Override
            public void run()
            {
                audioVoiceManager.stopRecord();
            }
        });
    }

    public String playRecordVoice( String msg )
    {
        return audioVoiceManager.playVoice( msg );
    }

    public void stopPlayRecordVoice( String msg )
    {
        audioVoiceManager.stopVoice();
    }

    public String getVoiceRecordFile( String msg )
    {
        return audioVoiceManager.getRecordFile();
    }

    String retUnityScene;
    public void getLiappAuthkey( String msg )
    {
        retUnityScene = msg;

        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                String liappAuthkey = LiappManager.sharedLiappManager().checkA1();
                resultLiappAuthKey( liappAuthkey );
            }
        });
    }

    public void showUnityADS( String msg )
    {
        retUnityScene = msg;
//		DebugLog.log( "UnityAds.isReady() : " + UnityAds.isReady() );
/*
        if ( UnityAds.isReady() )
        {
            UnityAds.show( this );
        }
        else
        {
            UnityPlayer.UnitySendMessage( "MainMenuScene", "FinishUnityAds", "false");
        }
        */
    }

    void resultLiappAuthKey( String authKey )
    {
        DebugLog.log( "liappAuthkey : " + authKey + "   retUnityScene : " + retUnityScene );
        UnityPlayer.UnitySendMessage( retUnityScene, "ResultLiappAuthkey", authKey );
    }

    ///////////////////////////
    private void showImagePicker()
    {
//        imagePickerManager.showImagePickerView( this );
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private void loginFacebook()
    {
//		if ( bConnectFacebook )
//			return;
//
//		bConnectFacebook = true;

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if ( accessToken != null )
        {
            DebugLog.log( "loginFacebook 1" );
            DebugLog.log( "accessToken.getToken() : " + accessToken.getToken() );
            DebugLog.log( "accessToken.getUserId() : " + accessToken.getUserId() );

            networkManager.loginID = accessToken.getUserId();
            networkManager.loginName = "GUEST";
            networkManager.loginAccessToken = accessToken.getToken();

            UnityPlayer.UnitySendMessage(strUnityScene, "LoginFacebook", "true");
        }
        else
        {
            DebugLog.log( "loginFacebook 2" );
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }
    }

    private void loginGoogle()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleApiClient );
        startActivityForResult( signInIntent, RC_SIGN_IN );
    }

    private void successLoginGoogle()
    {
        UnityPlayer.UnitySendMessage( strUnityScene, "LoginGoogle", "true" );
    }

    private void failLoginGoogle()
    {
        UnityPlayer.UnitySendMessage( strUnityScene, "LoginGoogle", "false" );
    }



}
