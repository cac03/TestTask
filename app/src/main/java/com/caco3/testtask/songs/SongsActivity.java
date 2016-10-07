package com.caco3.testtask.songs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.caco3.testtask.R;
import com.caco3.testtask.service.PollSongsService;
import com.caco3.testtask.songsApi.Song;
import com.caco3.testtask.ui.widget.AutoFitRecyclerView;
import com.caco3.testtask.util.NetworkUtils;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static com.caco3.testtask.util.LogUtils.*;

public class SongsActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = makeLogTag(SongsActivity.class);

    /**
     * Allow user to trigger manual update
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // UI
    /**
     * Represents list of {@link Song} items
     */
    private RecyclerView mSongsAutoFitRecyclerView;

    /**
     * Adapter for {@link SongsActivity#mSongsAutoFitRecyclerView}
     */
    private SongsAdapter mSongsAdapter;

    /**
     * Keep track of running service to prevent starting another
     * while one is already running
     */
    private Intent mPollDataIntent = null;

    /**
     * Receives result intents from {@link PollSongsService}.
     * And performs ui operations depending on result returned
     * by the service
     */
    private final BroadcastReceiver mOnDataPollCompletedBr
            = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LOGI(TAG, "Received intent with action " + action);
            // check result
            if (PollSongsService.isResultOk(intent)){
                mSongsAdapter.updateItems(SongsHelper.getSongs(SongsActivity.this));
            } else if (PollSongsService.isNetworkErrorOccurred(intent)){
                Toast.makeText(SongsActivity.this,
                        getString(R.string.unable_to_retrieve_songs_no_network), Toast.LENGTH_SHORT)
                        .show();
            } else if (PollSongsService.isJsonParseErrorOccurred(intent)){
                Toast.makeText(SongsActivity.this, getString(R.string.unable_to_parse_server_response), Toast.LENGTH_SHORT).show();
            } else if (PollSongsService.isUnknownErrorOccurred(intent)){
                Toast.makeText(SongsActivity.this, getString(R.string.unknown_error_occurred), Toast.LENGTH_SHORT).show();
            }
            // Stop refreshing
            mSwipeRefreshLayout.setRefreshing(false);

            mPollDataIntent = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.songs_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setupSongsRecyclerView();

        registerBroadcastReceivers();
    }

    /**
     * Called when user triggers manual update using {@link SwipeRefreshLayout}
     */
    @Override
    public void onRefresh(){
        if (mPollDataIntent != null){
            /**
             * we're already running one
             */
            return;
        }
        if (NetworkUtils.isNetworkAvailable(this)) {
            mPollDataIntent = new Intent(this, PollSongsService.class);
            startService(mPollDataIntent);
        } else {
            Toast.makeText(this, getString(R.string.unable_to_retrieve_songs_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy(){
        unregisterBroadcastReceivers();
        super.onDestroy();
    }

    /**
     * Asynchronously performs retrieving {@link Song}s from {@link com.caco3.testtask.provider.SongsDatabase}
     * and updates {@link #mSongsAutoFitRecyclerView} with retrieved items
     */
    private class PopulateSongsRecyclerView extends AsyncTask<Void, Void, List<Song>>{
        @Override
        protected List<Song> doInBackground(Void... params){
            return SongsHelper.getSongs(SongsActivity.this);
        }

        @Override
        protected void onPostExecute(List<Song> result){
            mSongsAdapter.updateItems(result);
        }
    }

    /**
     * Registers {@link #mOnDataPollCompletedBr}
     */
    private void registerBroadcastReceivers(){
        LocalBroadcastManager broadcastManager
                = LocalBroadcastManager.getInstance(this);

        broadcastManager.registerReceiver(mOnDataPollCompletedBr,
                new IntentFilter(PollSongsService.ACTION));
    }

    /**
     * Registers {@link #mOnDataPollCompletedBr}
     */
    private void unregisterBroadcastReceivers(){
        LocalBroadcastManager broadcastManager
                = LocalBroadcastManager.getInstance(this);

        broadcastManager.unregisterReceiver(mOnDataPollCompletedBr);
    }

    /**
     * Setups {@link #mSongsAutoFitRecyclerView} and populates
     * it with {@link Song} items stored in the {@link com.caco3.testtask.provider.SongsDatabase}
     */
    private void setupSongsRecyclerView(){
        mSongsAutoFitRecyclerView = (AutoFitRecyclerView) findViewById(R.id.songs_recycler_view);
        mSongsAutoFitRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mSongsAdapter = new SongsAdapter(this);
        mSongsAutoFitRecyclerView.setAdapter(mSongsAdapter);
        // add margins
        mSongsAutoFitRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int margin = (int)getResources().getDimension(R.dimen.song_item_margin);
                outRect.set(margin,
                        margin,
                        margin,
                        margin);
            }
        });
        new PopulateSongsRecyclerView().execute();
    }
}
