package com.caco3.testtask.songs;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.caco3.testtask.provider.SongsDatabase;
import com.caco3.testtask.provider.SongsProvider;
import com.caco3.testtask.songsApi.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.caco3.testtask.util.LogUtils.*;

/**
 * Provides some useful methods for accessing {@link SongsDatabase}
 */

public class SongsHelper {
    private static final String TAG = makeLogTag(SongsHelper.class);

    /**
     * Compares {@link Song}s by {@link Song#getId()}.
     * Used to update db with new list.
     */
    private static final Comparator<Song> sSongByIdComparator
            = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            /**
             * Can't delegate it to {@link Long#compare(long, long)}
             * since minApiLevel for this app is 15
             */
            if (o1.getId() < o2.getId()) {
                return -1;
            } else if (o1.getId() > o2.getId()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

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

    /**
     * Compares songs stored in the {@link SongsDatabase} and passed songs.
     * Removes songs which are present in db and not present in new list.
     * Updates songs which have the same id but different content.
     * Adds songs to the db which are present in new list, but not present in db.
     *
     * All this operations are done using {@link SongsProvider#applyBatch(ArrayList)}
     *
     * @param context to get content resolver
     * @param newSongList to update db
     */
    public static void updateSongsInDb(Context context, List<Song> newSongList){
        /**
         * {@link SongsProvider#applyBatch(ArrayList)} accepts only {@code ArrayList},
         * not {@code List}.
         *
         * Collect operations in this list. It will be done with linear complexity
         */
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        /**
         * Sort new list by {@link Song#getId()}.
         *
         * This list comes already sorted... But right now we can't rely on it
         * If list will not be sorted by {@link Song#getId()} we will have
         * wrong entries in db
         */
        Collections.sort(newSongList, sSongByIdComparator);

        /**
         * Songs from db. They are sorted by {@link Song#getId()}
         */
        List<Song> oldSongList = getSongs(context);
        if (oldSongList.isEmpty()) {
            // there was no songs in db
            // just add all from new songList
            for (Song song : newSongList) {
                batch.add(ContentProviderOperation
                        .newInsert(SongsProvider.CONTENT_URI)
                        .withValues(contentValuesFromSong(song))
                        .build());
            }
        } else if (oldSongList.isEmpty()){
            // no new songs
            // clear table
            batch.add(ContentProviderOperation
                    .newDelete(SongsProvider.CONTENT_URI)
                    .build());
        } else {
            int newListIdx = 0;
            int oldListIdx = 0;
            // Start iterating over new list and old list
            while (newListIdx < newSongList.size() && oldListIdx < oldSongList.size()) {
                if (sSongByIdComparator.compare(oldSongList.get(oldListIdx), newSongList.get(newListIdx)) < 0) {
                    // item in old list has id which is not present in the new list.
                    // so it must be removed
                    batch.add(ContentProviderOperation.newDelete(SongsProvider.CONTENT_URI)
                            .withSelection(SongsDatabase.Songs.KEY_SONG_ID + " = ?",
                                    new String[]{oldSongList.get(oldListIdx).getId() + ""})
                            .build());
                    // go to next
                    oldListIdx++;
                } else if (sSongByIdComparator.compare(oldSongList.get(oldListIdx), newSongList.get(newListIdx)) == 0) {
                    // current item in the old list and in the new list
                    // have the same id.

                    // Are their contents different ?
                    if (!oldSongList.get(oldListIdx).equals(newSongList.get(newListIdx))) {
                        // we must update entry
                        batch.add(ContentProviderOperation.newUpdate(SongsProvider.CONTENT_URI)
                                .withValues(contentValuesFromSong(newSongList.get(newListIdx)))
                                .withSelection(SongsDatabase.Songs.KEY_SONG_ID + " = ?",
                                        new String[]{oldSongList.get(oldListIdx).getId() + ""})
                                .build());
                    }
                    // go to next items
                    newListIdx++;
                    oldListIdx++;
                } else {
                    // current item in the new list is not present in the old list
                    // so we must add it
                    batch.add(ContentProviderOperation
                            .newInsert(SongsProvider.CONTENT_URI)
                            .withValues(contentValuesFromSong(newSongList.get(newListIdx)))
                            .withSelection(SongsDatabase.Songs.KEY_SONG_ID + " = ?",
                                    new String[]{newSongList.get(newListIdx).getId() + ""})
                            .build());
                    // analyse next positions
                    oldListIdx++;
                    newListIdx++;
                }
            }

            if (newListIdx < newSongList.size()) {
                // there are left some items in the new list which were not added
                // so we must add them
                while (newListIdx < newSongList.size()) {
                    batch.add(ContentProviderOperation
                            .newInsert(SongsProvider.CONTENT_URI)
                            .withValues(contentValuesFromSong(newSongList.get(newListIdx)))
                            .build());
                    // go to next
                    newListIdx++;
                }
            } else if (oldListIdx < oldSongList.size()) {
                // there are left some items in the old list
                // which are not present in the new list
                // so we have to delete them
                while (oldListIdx < oldSongList.size()) {
                    batch.add(ContentProviderOperation
                            .newDelete(SongsProvider.CONTENT_URI)
                            .withSelection(SongsDatabase.Songs.KEY_SONG_ID + " = ?",
                                    new String[]{oldSongList.get(oldListIdx).getId() + ""})
                            .build());
                    // go to next
                    oldListIdx++;
                }
            }
        }
        // apply operations
        try {
            LOGI(TAG, "About to apply batch");
            context.getContentResolver().applyBatch(SongsProvider.AUTHORITY, batch);
            LOGI(TAG, "Batch was successfully applied");
        } catch (RemoteException | OperationApplicationException e){
            LOGE(TAG, "Unable to apply batch", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates {@link ContentValues} which is ready to be set in the db
     * @param song to create from
     * @return {@link ContentValues}
     */
    private static ContentValues contentValuesFromSong(Song song){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SongsDatabase.Songs.KEY_AUTHOR, song.getAuthor());
        contentValues.put(SongsDatabase.Songs.KEY_NAME, song.getName());
        contentValues.put(SongsDatabase.Songs.KEY_VERSION, song.getVersion());
        contentValues.put(SongsDatabase.Songs.KEY_SONG_ID, song.getId());

        return contentValues;
    }

    private SongsHelper(){
        throw new AssertionError(SongsHelper.class.getCanonicalName() + " not intended to be instantiated");
    }
}
