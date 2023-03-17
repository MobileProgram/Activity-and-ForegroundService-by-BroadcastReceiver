package com.mblhcmute.activity_and_foregroundservice_by_broadcastreceiver;

import static com.mblhcmute.activity_and_foregroundservice_by_broadcastreceiver.MyApplication.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyService extends Service {

    private MediaPlayer mediaPlayer;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_CLEAR = 3;
    public static final int ACTION_START = 4;
    private  boolean isPlaying;
    private Song mSong;
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Check","MyService onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if(bundle != null)
        {
            Song song = (Song) bundle.get("object_song");

            if(song != null){
                startMusic(song);
                sendNotification(song);
                mSong = song;
            }

        }
        int actionMusic = intent.getIntExtra("acton_music_service", 0);
        handleActionMusic(actionMusic);
        return START_NOT_STICKY;
    }

    private void startMusic(Song song) {
        if(mediaPlayer == null){
            mediaPlayer = mediaPlayer.create(getApplicationContext(),song.getResource());
        }
        sendActionToActivity(ACTION_START);
        mediaPlayer.start();
        isPlaying = true;
    }
    private void handleActionMusic(int action)
    {
        switch (action) {
            case ACTION_PAUSE:
                pauseMusic();
                break;
            case ACTION_RESUME:
                resumeMusic();
                break;
            case ACTION_CLEAR:
                stopSelf();
                sendActionToActivity(ACTION_CLEAR);
                break;
        }
    }
    private void pauseMusic(){
        if(mediaPlayer !=null && isPlaying){
            mediaPlayer.pause();
            isPlaying = false;
            sendNotification(mSong);
            sendActionToActivity(ACTION_PAUSE);
        }
    }
    private void resumeMusic(){
        if(mediaPlayer !=null && !isPlaying){
            mediaPlayer.start();
            isPlaying = true;
            sendNotification(mSong);
            sendActionToActivity(ACTION_RESUME);
        }
    }


    private void sendNotification(Song song) {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), song.getImage());
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteViews.setTextViewText(R.id.tv_title_song, song.getTitle());
        remoteViews.setTextViewText(R.id.tv_single_song, song.getSinger());
        remoteViews.setImageViewBitmap(R.id.img_song, bitmap);

        remoteViews.setImageViewResource(R.id.img_play_or_pause, R.drawable.play);


        if (isPlaying){
            remoteViews.setOnClickPendingIntent(R.id.img_play_or_pause, getPendingIntent(this, ACTION_PAUSE));
            remoteViews.setImageViewResource(R.id.img_play_or_pause, R.drawable.pause);
        }
        else{
            remoteViews.setOnClickPendingIntent(R.id.img_play_or_pause, getPendingIntent(this, ACTION_RESUME));
            remoteViews.setImageViewResource(R.id.img_play_or_pause, R.drawable.play);
        }
        remoteViews.setOnClickPendingIntent(R.id.img_clear, getPendingIntent(this, ACTION_CLEAR));
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setCustomContentView(remoteViews)
                .build();

        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(Context context, int action){
        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("acton_music", action);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Check","MyService onDestroy");
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void sendActionToActivity(int action){
        Intent intent = new Intent("send_data_to_activity");
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song",mSong);
        bundle.putBoolean("status_player",isPlaying);
        bundle.putInt("action_music", action);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}