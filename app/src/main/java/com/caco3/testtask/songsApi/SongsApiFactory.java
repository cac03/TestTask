package com.caco3.testtask.songsApi;


public final class SongsApiFactory {
    private static final SongsApi instance = new SongsApiImpl();
    public static SongsApi getApi(){
        return instance;
    }
}
