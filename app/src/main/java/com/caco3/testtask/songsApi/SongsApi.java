package com.caco3.testtask.songsApi;

import java.util.List;


public interface SongsApi {
    /**
     * Retrieves list of songs from the server
     * @return list of songs
     * @throws SongsApiException if there was network problems during request
     * or server returned invalid json
     * {@see {@link SongsApiException }}
     */
    List<Song> getSongs() throws SongsApiException;
}
