package com.caco3.testtask.songsApi;

/**
 * Controls creations of {@link SongsApi} instances.
 * Made for possible further extensions, but now it
 * just holds {@link SongsApiImpl} instance and
 * returns it by demand
 */
public final class SongsApiFactory {
    private static final SongsApi instance = new SongsApiImpl();
    public static SongsApi getApi(){
        return instance;
    }
}
