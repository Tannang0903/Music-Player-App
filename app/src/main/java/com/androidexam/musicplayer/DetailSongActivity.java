package com.androidexam.musicplayer;

//import static com.androidexam.musicplayer.viewmodel.AlbumDetailsAdapter.albumFiles;
import static com.androidexam.musicplayer.MainActivity.repeatBoolean;
import static com.androidexam.musicplayer.MainActivity.shuffleBoolean;
import static com.androidexam.musicplayer.MainActivity.song;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidexam.musicplayer.model.Song;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class DetailSongActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    TextView durationPlayed, durationTotal, songName, songArtist;
    ImageView nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, coverArt;
    FloatingActionButton playPause;
    SeekBar seekBar;
    int position = -1;
    static Uri uri;
    static ArrayList<Song> listSongs = new ArrayList<>();
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_song);
        initViews();
        getIntenMethod();
        songName.setText(listSongs.get(position).getTitle());
        songArtist.setText(listSongs.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mediaPlayer != null && b){
                    mediaPlayer.seekTo(i * 1000);
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
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
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
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
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

    private void playPauseClicked() {
        if(mediaPlayer.isPlaying()){
            playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
        } else {
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
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

    private void nextBtnClicked() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            // else position will be position ...
            uri = Uri.parse((listSongs.get(position).getPath()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
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

    private void preBtnClicked() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() -1) : (position -1 ));
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() -1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() -1) : (position -1 ));
            }
            uri = Uri.parse((listSongs.get(position).getTitle()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            songArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            DetailSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
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
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        } else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
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
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if(mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}