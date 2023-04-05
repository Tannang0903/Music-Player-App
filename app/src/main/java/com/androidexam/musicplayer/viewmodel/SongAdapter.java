package com.androidexam.musicplayer.viewmodel;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidexam.musicplayer.DetailSongActivity;
import com.androidexam.musicplayer.R;
import com.androidexam.musicplayer.model.Song;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> implements Filterable {
    private Context mContext;
    private ArrayList<Song> mSong;
    private ArrayList<Song> mSongCopy;


    public SongAdapter(Context mContext, ArrayList<Song> mSong) {
        this.mContext = mContext;
        this.mSong = mSong;
        this.mSongCopy = mSong;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_song_item, parent);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mSong.get(position).getTitle());
        byte[] image = new byte[0];
        try {
            image = getAlbumArt(mSong.get(position).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.album_art);
        } else {
            Glide.with(mContext).load(R.drawable.gredient_bg).into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DetailSongActivity.class);
                intent.putExtra("position", holder.getAdapterPosition());
                mContext.startActivity(intent);
            }
        });
        holder.menu_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.more, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item -> {
                    switch (item.getItemId()) {
                        case R.id.delete:
                            Toast.makeText(mContext, "Delete Clicked!", Toast.LENGTH_SHORT).show();
                            deleleFile(position, v);
                            break;
                    }
                    return true;
                }));
            }
        });
    }

    private void deleleFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mSong.get(position).getId()));
        File file = new File(mSong.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted){
            mContext.getContentResolver().delete(contentUri, null, null);
            mSong.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mSong.size());
            Snackbar.make(view, "File Deleted : ", Snackbar.LENGTH_LONG)
                    .show();
        }
        else {
            Snackbar.make(view, "Can't be Deleted : ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return mSong.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        ImageView album_art;
        ImageView menu_more;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.song_file_name);
            album_art = itemView.findViewById(R.id.song_image);
            menu_more = itemView.findViewById(R.id.menuMore);
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
