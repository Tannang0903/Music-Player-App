package com.androidexam.musicplayer.viewmodel;

import com.androidexam.musicplayer.model.Song;
import com.androidexam.musicplayer.model.SongsAPI;

import java.util.List;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SongsAPIService {
    private static final String BASE_URL = "https://mp3.zing.vn/";
    private SongsAPI api;

    public SongsAPIService() {
        api = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(SongsAPI.class);
    }

    public Single<List<Song>> getDogs() {
        return api.getSongs();
    }
}
