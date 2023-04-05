package com.androidexam.musicplayer.model;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

public interface SongsAPI {
    @GET("")
    Single<List<Song>> getSongs();
}
