package com.caco3.testtask.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.caco3.testtask.util.LogUtils.*;

public class SongsDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(SongsDatabase.class);

    private static final int DB_VERSION = 3;

    private static final String DB_NAME = "songs.db";

    /**
     * Contains constants for 'songs' table
     */
    public static class Songs{
        public static final String TABLE_NAME = "songs";
        public static final String KEY__ID = "_id";
        public static final String KEY_NAME = "name";
        public static final String KEY_AUTHOR = "author";
        public static final String KEY_VERSION = "version";
        public static final String KEY_SONG_ID = "song_id";
    }

    public SongsDatabase(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LOGI(TAG, "onCreate()");
        db.execSQL("CREATE TABLE " + Songs.TABLE_NAME + "("
                + Songs.KEY__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Songs.KEY_AUTHOR + " TEXT, "
                + Songs.KEY_NAME + " TEXT, "
                + Songs.KEY_VERSION + " INTEGER, "
                + Songs.KEY_SONG_ID + " INTEGER UNIQUE"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGI(TAG, "onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS " + Songs.TABLE_NAME);
        onCreate(db);
    }
}
