package com.caco3.testtask.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.caco3.testtask.songs.SongsHelper;
import com.caco3.testtask.songsApi.Song;
import com.caco3.testtask.songsApi.SongsApi;
import com.caco3.testtask.songsApi.SongsApiException;
import com.caco3.testtask.songsApi.SongsApiFactory;
import com.caco3.testtask.util.NetworkUtils;

import java.util.List;

import static com.caco3.testtask.util.LogUtils.*;

public class PollSongsService extends IntentService {
    private static final String TAG = makeLogTag(PollSongsService.class);

    /**
     * Used to construct {@link Intent} to trigger BroadcastReceivers
     * subscribed for this action
     */
    public static final String ACTION = PollSongsService.class.getCanonicalName() + ".PollDataCompleted";

    /**
     * Extra key which must be put to Intent triggering broadcast receivers to tell
     * the client result of running this service
     */
    private static final String EXTRA_RESULT = "result";
    /**
     * Value for {@link PollSongsService#EXTRA_RESULT}
     * representing ok result
     */
    private static final int EXTRA_RESULT_OK = 0x0;
    /**
     * Value for {@link PollSongsService#EXTRA_RESULT}
     * representing network error
     */
    private static final int EXTRA_NETWORK_ISSUES = 0x1;
    /**
     * Value for {@link PollSongsService#EXTRA_RESULT}
     * representing parse error
     */
    private static final int EXTRA_PARSE_ERROR = 0x2;
    /**
     * Value for {@link PollSongsService#EXTRA_RESULT}
     * representing unknown error
     */
    private static final int EXTRA_UNKNOWN_ERROR = 0x3;
    /**
     * Value for {@link PollSongsService#EXTRA_RESULT}
     * representing error caused by network unavailability
     */
    private static final int EXTRA_NO_NETWORK = 0x4;

    public PollSongsService(){
        super(TAG); // Worker thread name
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LOGI(TAG, "Running");
        Intent resultIntent = new Intent(ACTION);
        if (NetworkUtils.isNetworkAvailable(this)){
            SongsApi songsApi
                    = SongsApiFactory.getApi();

            try {
                List<Song> songs = songsApi.getSongs();
                // Update songs in db
                SongsHelper.updateSongsInDb(this, songs);

                resultIntent.putExtra(EXTRA_RESULT, EXTRA_RESULT_OK);
            } catch (SongsApiException e){
                if (e.isCausedByNetworkIssue()){
                    LOGE(TAG, "Unable to poll songs. Network issues", e);
                    resultIntent.putExtra(EXTRA_RESULT, EXTRA_NETWORK_ISSUES);
                } else if (e.isCausedByJsonParseErrors()){
                    LOGE(TAG, "Unable to parse json returned by the server", e);
                    resultIntent.putExtra(EXTRA_RESULT, EXTRA_PARSE_ERROR);
                } else {
                    // wtf?
                    resultIntent.putExtra(EXTRA_RESULT, EXTRA_UNKNOWN_ERROR);
                }
            }
        } else {
            LOGI(TAG, "No network, exiting");
            resultIntent.putExtra(EXTRA_RESULT, EXTRA_NO_NETWORK);
        }

        // Trigger broadcast receivers
        LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(resultIntent);
    }

    /**
     * Static method which helps to determine whether the result of
     * running this service was ok
     * @param resultIntent created by this service
     * @return true if result was ok, false otherwise
     */
    public static boolean isResultOk(Intent resultIntent){
        return resultIntent.getExtras().getInt(EXTRA_RESULT) == EXTRA_RESULT_OK;
    }

    /**
     * Static method which helps to determine whether the result of
     * running this service was not ok and error was caused by network issues
     * @param resultIntent created by this service
     * @return true if result is not ok and error is caused by network issue, false otherwise
     */
    public static boolean isNetworkErrorOccurred(Intent resultIntent){
        return resultIntent.getExtras().getInt(EXTRA_RESULT) == EXTRA_NETWORK_ISSUES;
    }

    /**
     * Static method which helps to determine whether the result of
     * running this service was not ok and error was caused by parsing json issues
     * @param resultIntent created by this service
     * @return true if result is not ok and error is caused by json parse issue, false otherwise
     */
    public static boolean isJsonParseErrorOccurred(Intent resultIntent){
        return resultIntent.getExtras().getInt(EXTRA_RESULT) == EXTRA_PARSE_ERROR;
    }

    /**
     * Static method which helps to determine whether the result of
     * running this service was not ok and error was caused by network unavailability
     * @param resultIntent created by this service
     * @return true if result is not ok and error is caused by network unavailability, false otherwise
     */
    public static boolean isThereWasNoNetwork(Intent resultIntent){
        return resultIntent.getExtras().getInt(EXTRA_RESULT) == EXTRA_NO_NETWORK;
    }

    /**
     * Static method which helps to determine whether the result of
     * running this service was not ok and error is unknown
     * @param resultIntent created by this service
     * @return true if result is not ok and error is unknown, false otherwise
     */
    public static boolean isUnknownErrorOccurred(Intent resultIntent){
        return resultIntent.getExtras().getInt(EXTRA_RESULT) == EXTRA_UNKNOWN_ERROR;
    }
}
