package com.unity3d.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.IOException;

/**
 * Created by freeon on 2017. 1. 9..
 */

public class AudioVoiceManager
{
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private Activity activity;

    public AudioVoiceManager( Activity activity)
    {
        this.activity = activity;
    }

    public String checkRecode()
    {
        String ret = "0";

        if (ActivityCompat.checkSelfPermission( activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ret = "1";
            ActivityCompat.requestPermissions( activity, new String[]{Manifest.permission.RECORD_AUDIO},0);
        }

        return ret;
    }

    public void startRecord()
    {
        if (mediaRecorder != null)
            stopRecord();

        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.setVolume(0, 0);

        String filename = getTempFile().toString();
        DebugLog.log("filename : " + filename);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(filename);

        try
        {
            mediaRecorder.prepare();
            mediaRecorder.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            DebugLog.log("startvoicerecord err : " + e.toString());
        }
    }

    public void stopRecord()
    {
        if( mediaRecorder != null )
        {
            try
            {
                mediaRecorder.stop();
            }
            catch(RuntimeException e)
            {
                //mediaRecorder.delete();
            }finally {
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }

        if ( mediaPlayer != null && mediaPlayer.isPlaying() )
            mediaPlayer.setVolume( 1, 1 );
    }

    public String getRecordFile()
    {
        return getTempFile().toString();
    }

    public String playVoice( String msg )
    {
        DebugLog.log( "========== playVoice : " + msg );

        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume( 1, 1 );
        mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );

        try
        {
//            String filename = getTempFile().getPath();
            String filename = msg;
            mediaPlayer.setDataSource( filename );
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            mediaPlayer.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

//        float time = (float) (mediaPlayer.getDuration() * 0.001);
        int time = (int) (mediaPlayer.getDuration() * 0.001);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                DebugLog.log( "==================== onCompletion" );
                UnityPlayer.UnitySendMessage( "ViewRecordVoice(panel_9)",  "FinishPlayVoice", "" );
            }
        });

        DebugLog.log( "time : " + time );

        mediaPlayer.start();

        return String.valueOf( time );
    }

    public void stopVoice()
    {
        if ( mediaPlayer == null )
            return;

        if ( mediaPlayer.isPlaying() )
            mediaPlayer.stop();

        mediaPlayer.release();
        mediaPlayer = null;
    }

    private File getTempFile(){

//        File file = new File( Environment.getExternalStorageDirectory(), "/voice.amr"  );
        File file = new File( activity.getExternalCacheDir(), "/voice.amr"  );

        try
        {
            file.createNewFile();
        }

        catch( Exception e )
        {
            DebugLog.log( "fileCreation fail" );
        }

        return file;
    }
}
