package com.caco3.testtask.songs;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.caco3.testtask.provider.SongsDatabase;
import com.caco3.testtask.provider.SongsProvider;
import com.caco3.testtask.songsApi.Song;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides some useful methods for accessing {@link SongsDatabase}
 */

public class SongsHelper {
    /**
     * Retrieves all songs saved in the {@link SongsDatabase}
     * and collects them into List
     * @param context get content resolver
     * @return List of songs stored in the {@link SongsDatabase}
     */
    public static List<Song> getSongs(Context context){
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        List<Song> result = new ArrayList<>();

        try{
            cursor = contentResolver.query(SongsProvider.CONTENT_URI,
                    null,
                    null,
                    null,
                    SongsDatabase.Songs.KEY_SONG_ID + " ASC" /* sort order */);
            if (cursor != null && cursor.moveToFirst()){
                int nameIdx = cursor.getColumnIndex(SongsDatabase.Songs.KEY_NAME);
                int authorIdx = cursor.getColumnIndex(SongsDatabase.Songs.KEY_AUTHOR);
                int idIdx = cursor.getColumnIndex(SongsDatabase.Songs.KEY_SONG_ID);
                int versionIdx = cursor.getColumnIndex(SongsDatabase.Songs.KEY_VERSION);

                do {
                    Song song = new Song(cursor.getString(authorIdx),
                            cursor.getString(nameIdx),
                            cursor.getLong(idIdx),
                            cursor.getInt(versionIdx));
                    result.add(song);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }

        return result;
    }

    private SongsHelper(){
        throw new AssertionError(SongsHelper.class.getCanonicalName() + " not intended to be instantiated");
    }
}
