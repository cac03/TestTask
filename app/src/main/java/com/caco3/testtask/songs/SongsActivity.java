package com.caco3.testtask.songs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.caco3.testtask.R;
import com.caco3.testtask.service.PollSongsService;
import com.caco3.testtask.songsApi.Song;
import com.caco3.testtask.ui.BaseActivity;
import com.caco3.testtask.ui.widget.AutoFitRecyclerView;
import com.caco3.testtask.util.NetworkUtils;
import com.caco3.testtask.util.UiUtils;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static com.caco3.testtask.util.LogUtils.*;

public class SongsActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = makeLogTag(SongsActivity.class);

    /**
     * Key for boolean in {@link Bundle} filling when {@link #onSaveInstanceState(Bundle)}
     * called and received when {@link #onCreate(Bundle)} called
     *
     * Represents whether the {@link #mSwipeRefreshLayout} was
     * refreshing when {@link #onSaveInstanceState(Bundle)} was called.
     *
     * Helps to restore {@link #mSwipeRefreshLayout} state when {@link #onCreate(Bundle)}
     * called
     */
    private static final String STATE_SWIPE_REFRESH_LAYOUT_REFRESHING
            = "swrl_refreshing";

    /**
     * Allows user to trigger manual update
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
     * Keep track of running task updating {@link #mSongsAutoFitRecyclerView}
     * so we can cancel it. When this activity is about to be destroyed
     */
    private UpdateSongsViewTask mUpdateSongsViewTask = null;

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
                mUpdateSongsViewTask = new UpdateSongsViewTask(true /* stop refreshing */);
                mUpdateSongsViewTask.execute();
            } else {
                // Updating failed
                // Refreshing animation will not be cleared.
                // So we must clear it here
                mSwipeRefreshLayout.setRefreshing(false);
                if (PollSongsService.isNetworkErrorOccurred(intent)) {
                    Toast.makeText(SongsActivity.this,
                            getString(R.string.unable_to_retrieve_songs_no_network), Toast.LENGTH_SHORT)
                            .show();
                } else if (PollSongsService.isJsonParseErrorOccurred(intent)) {
                    Toast.makeText(SongsActivity.this, getString(R.string.unable_to_parse_server_response), Toast.LENGTH_SHORT).show();
                } else if (PollSongsService.isUnknownErrorOccurred(intent)) {
                    Toast.makeText(SongsActivity.this, getString(R.string.unknown_error_occurred), Toast.LENGTH_SHORT).show();
                }
            }

            mPollDataIntent = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.songs_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // restore state
        if (savedInstanceState != null){
            // orientation changed e.g.
            if (savedInstanceState.getBoolean(STATE_SWIPE_REFRESH_LAYOUT_REFRESHING)){
                // app was refreshing data when orientation was changed.
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }
        setupSongsRecyclerView();
        /**
         * Apply color scheme for {@link #mSwipeRefreshLayout}
         */
        UiUtils.setColorsForSwipeRefreshLayout(mSwipeRefreshLayout);

        registerBroadcastReceivers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /**
         * Save {@link #mSwipeRefreshLayout} refreshing state
         */
        outState.putBoolean(STATE_SWIPE_REFRESH_LAYOUT_REFRESHING, mSwipeRefreshLayout.isRefreshing());
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
            // stop refreshing
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy(){
        unregisterBroadcastReceivers();
        if (mUpdateSongsViewTask != null){
            LOGD(TAG, "UpdateSongsViewTask canceled");
            mUpdateSongsViewTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Asynchronously performs retrieving {@link Song}s from {@link com.caco3.testtask.provider.SongsDatabase}
     * and updates {@link #mSongsAutoFitRecyclerView} with retrieved items.
     *
     * We execute this in following cases:
     * 1) We need to populate our {@link #mSongsAutoFitRecyclerView} when this activity is created.
     * 2) We need to update view, when user triggered manual update using {@link #mSwipeRefreshLayout}
     *
     * Also we must control refreshing animation on {@link #mSwipeRefreshLayout}.
     * It seems that we can just call setRefreshing(false) onPostExecute.
     * But there arises a problem...
     * When device orientation changed activity gets recreated. And if the app hasn't finished
     * data updating, instance of this task created in {@link #setupSongsRecyclerView()}
     * will clear refreshing animation.
     * So we have to really know whether we have to call setRefreshing(false) onPostExecute.
     * This problem solved by constructor with boolean parameter. If we really must call
     * setRefreshing(false) instance must be created with 'true' parameter and 'false'
     * otherwise.
     */
    private class UpdateSongsViewTask extends AsyncTask<Void, Void, List<Song>>{
        private final boolean mNeedToStopRefreshingOnPostExecute;

        /**
         * @param needToStopRefreshingOnPostExecute if true, task will call setRefreshing(false)
         *                                          in onPostExecute()
         */
        UpdateSongsViewTask(boolean needToStopRefreshingOnPostExecute){
            this.mNeedToStopRefreshingOnPostExecute = needToStopRefreshingOnPostExecute;
        }


        @Override
        protected List<Song> doInBackground(Void... params){
            return SongsHelper.getSongs(SongsActivity.this);
        }

        @Override
        protected void onPostExecute(List<Song> result){
            mSongsAdapter.updateItems(result);

            /**
             * If item was inserted at 0 position. RecyclerView
             * will scroll to second row...
             * {@see https://code.google.com/p/android/issues/detail?id=174227}
             *
             * Fix it...
             */
            GridLayoutManager gridLayoutManager
                    = (GridLayoutManager)mSongsAutoFitRecyclerView.getLayoutManager();
            int itemsPerRow = gridLayoutManager.getSpanCount();
            int firstCompletelyVisibleItem = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
            if (firstCompletelyVisibleItem != -1
                    && firstCompletelyVisibleItem < itemsPerRow){
                mSongsAutoFitRecyclerView.scrollToPosition(0);
            }

            // Stop refreshing if needed
            if (mNeedToStopRefreshingOnPostExecute) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            mUpdateSongsViewTask = null;
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
     * Unregisters {@link #mOnDataPollCompletedBr}
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
        mUpdateSongsViewTask = new UpdateSongsViewTask(false /* mustn't stop refreshing */);
        mUpdateSongsViewTask.execute();
    }
}
