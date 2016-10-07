package com.caco3.testtask.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Arrays;

import static com.caco3.testtask.util.LogUtils.*;

public class SongsProvider extends ContentProvider {
    private static final String TAG = makeLogTag(SongsProvider.class);

    private static final String SONG_TYPE = "com.caco3.testtask.datatype.song";

    private static final String AUTHORITY = "com.caco3.testtask.songs";

    private static final String BASE_PATH = "songs";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final int URI_SONGS = 0x0;


    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, URI_SONGS);
    }

    private SongsDatabase mSongsDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    @Override
    public boolean onCreate() {
        LOGI(TAG, "onCreate()");
        mSongsDatabase = new SongsDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LOGD(TAG, "Query: " + uri
                + " projection: " + Arrays.toString(projection)
                + " selection: " + selection
                + " args: " + Arrays.toString(selectionArgs)
                + " sortOrder: " + sortOrder);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SongsDatabase.Songs.TABLE_NAME);
        switch (sUriMatcher.match(uri)){
            case URI_SONGS:
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        mSQLiteDatabase = mSongsDatabase.getWritableDatabase();
        return mSQLiteDatabase.query(SongsDatabase.Songs.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, // groupBy
                null, // having
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)){
            case URI_SONGS:
                return SONG_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGI(TAG, "insert: " + uri + " cv = " + values);
        mSQLiteDatabase = mSongsDatabase.getWritableDatabase();
        long rowId;
        switch(sUriMatcher.match(uri)){
            case URI_SONGS:
                rowId = mSQLiteDatabase.insert(SongsDatabase.Songs.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        return ContentUris.withAppendedId(uri, rowId);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LOGD(TAG, "Delete: " + uri + " selection: " + selection + " args: " + Arrays.toString(selectionArgs));
        mSQLiteDatabase = mSongsDatabase.getWritableDatabase();
        int affectedRows;
        switch (sUriMatcher.match(uri)){
            case URI_SONGS:
                affectedRows = mSQLiteDatabase.delete(SongsDatabase.Songs.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        LOGD(TAG, "Update: " + uri + " selection: " + selection + " args: " + Arrays.toString(selectionArgs));
        mSQLiteDatabase = mSongsDatabase.getWritableDatabase();
        int affectedRows;
        switch (sUriMatcher.match(uri)){
            case URI_SONGS:
                affectedRows = mSQLiteDatabase.update(SongsDatabase.Songs.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        return affectedRows;
    }
}
