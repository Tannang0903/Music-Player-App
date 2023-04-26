package com.androidexam.musicplayer;

import static com.androidexam.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.androidexam.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.androidexam.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.androidexam.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.androidexam.musicplayer.DetailSongActivity.listSongs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.androidexam.musicplayer.model.ActionPlaying;
import com.androidexam.musicplayer.model.Song;

import java.util.ArrayList;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer ;
    public ArrayList<Song> songs = new ArrayList<>();
    Uri uri;
    public int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method ");
        return mBinder;
    }

    public class MyBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition != -1) {
            playMedia(myPosition);
        }
        if(actionName != null) {
            switch (actionName) {
                case "playPause":
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    playPauseBtnClicked();
                    break;
                case "next":
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    nextBtnClicked();
                    break;
                case "previous":
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    previousBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        songs = listSongs;
        position = StartPosition;
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(songs != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start() {
        mediaPlayer.start();
    }

    void pause() {
        mediaPlayer.pause();
    }

    void stop() {
        mediaPlayer.stop();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void release(){
        mediaPlayer.release();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(songs.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,
                MODE_PRIVATE)
                .edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, songs.get(position).getArtist());
        editor.putString(SONG_NAME, songs.get(position).getTitle());
        editor.apply();

        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            if(mediaPlayer != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
                onCompleted();
            }
        }
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, DetailSongActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(songs.get(position).getPath());
        Bitmap thump = null;
        if(picture != null) {
            thump = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else {
            thump = BitmapFactory.decodeResource(getResources(), R.drawable.seebar);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thump)
                .setContentTitle(songs.get(position).getTitle())
                .setContentText(songs.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(1,notification);
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    public void playPauseBtnClicked(){
        if(actionPlaying != null) {
            actionPlaying.playPauseClicked();
        }
    }
    public void previousBtnClicked(){
        if(actionPlaying != null) {
            actionPlaying.preBtnClicked();
        }
    }
    public void nextBtnClicked() {
        if(actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }
}
