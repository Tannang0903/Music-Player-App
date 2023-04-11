package com.androidexam.musicplayer;

//import static com.androidexam.musicplayer.viewmodel.AlbumDetailsAdapter.albumFiles;
import static com.androidexam.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.androidexam.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.androidexam.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.androidexam.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.androidexam.musicplayer.MainActivity.repeatBoolean;
import static com.androidexam.musicplayer.MainActivity.shuffleBoolean;
import static com.androidexam.musicplayer.MainActivity.song;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidexam.musicplayer.model.Song;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class DetailSongActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection{
    private TextView durationPlayed, durationTotal, songName, songArtist;
    private ImageView nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, coverArt;
    private FloatingActionButton playPause;
    private SeekBar seekBar;
    private int position = -1;
    public static Uri uri;
    public static ArrayList<Song> listSongs = new ArrayList<>();
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setFullScreen();
        setContentView(R.layout.activity_detail_song);
        getSupportActionBar().hide();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
        initViews();
        getIntenMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService != null && b){
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        DetailSongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int mCurrentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_24);
                }
                else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on_24);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
                }
                else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_on_24);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private int getRandom(int position) {
        Random random = new Random();
        return random.nextInt(position + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseClicked() {
        if(musicService.isPlaying()){
            playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
        } else {
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            showNotification(R.drawable.ic_baseline_pause_24);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
    }

    public void nextBtnClicked() {
        if(musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse((listSongs.get(position).getPath()));
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_pause_24);
            playPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preBtnClicked();
                    }
                });
            }
        };
    }

    public void preBtnClicked() {
        if(musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() -1) : (position -1 ));
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_pause_24);
            playPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() -1) : (position -1 ));
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private String formattedTime(int mCurrentPosition){
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1){
            return totalNew;
        } else {
            return totalout;
        }
    }

    private void getIntenMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if(sender != null && sender.equals("albumDetails")) {
            listSongs = song;
        }else {
            listSongs = song;
        }
        if(listSongs != null){
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.ic_baseline_pause_24);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void initViews(){
        songName = findViewById(R.id.song_name);
        songArtist = findViewById(R.id.artist);
        playPause = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        durationPlayed = findViewById(R.id.durantionPlayed);
        durationTotal = findViewById(R.id.durantionTotal);
        coverArt = findViewById(R.id.cover_art);
        prevBtn = findViewById(R.id.prev_btn);
        nextBtn = findViewById(R.id.next_btn);
    }

    private void metaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotall = Integer.parseInt(listSongs.get(position).getDuration());
        durationTotal.setText(formattedTime(durationTotall));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if(art != null){
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, coverArt, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gredient = findViewById(R.id.imageViewGredient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setImageResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gredient.setBackground((gradientDrawable));
                        GradientDrawable gradientDrawableBg = new GradientDrawable( GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        songName.setTextColor(swatch.getTitleTextColor());
                        songArtist.setTextColor(swatch.getBodyTextColor());
                    }
                    else {
                        ImageView gredient = findViewById(R.id.imageViewGredient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setImageResource(R.drawable.gredient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gredient.setBackground((gradientDrawable));
                        GradientDrawable gradientDrawableBg = new GradientDrawable( GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        songName.setTextColor(Color.WHITE);
                        songArtist.setTextColor(Color.DKGRAY);
                    }

                }
            });
        } else {
            ImageAnimation(this, coverArt, bitmap);
            ImageView gredient = findViewById(R.id.imageViewGredient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gredient.setImageResource(R.drawable.gredient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            songName.setTextColor(Color.WHITE);
            songArtist.setTextColor(Color.DKGRAY);
        }
    }

    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap) {
        Animation animationOut= AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animationIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animationIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animationOut);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)service;
        musicService = myBinder.getService();
        Toast.makeText(this, "Connected" + musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        songArtist.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
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
        picture = getAlbumArt(song.get(position).getPath());
        Bitmap thump = null;
        if(picture != null) {
            thump = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else {
            thump = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thump).
                setContentTitle(song.get(position).getTitle())
                .setContentText(song.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);


    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}