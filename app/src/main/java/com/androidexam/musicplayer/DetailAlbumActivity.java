package com.androidexam.musicplayer;

import static com.androidexam.musicplayer.MainActivity.song;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.androidexam.musicplayer.model.Song;
import com.androidexam.musicplayer.viewmodel.AlbumDetailsAdapter;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class DetailAlbumActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<Song> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < song.size(); i++) {
            if(albumName.equals(song.get(i).getAlbum())) {
                albumSongs.add(j,song.get(i));
                j++;
            }
        }
        byte[] image = new byte[0];
        try {
            image = getAlbumArt(albumSongs.get(0).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(image != null) {
            Glide.with(this).load(image).into(albumPhoto);
        }else {
            Glide.with(this).load(R.drawable.messi).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(albumSongs.size() < 1)) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this,albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}