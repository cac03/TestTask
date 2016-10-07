package com.caco3.testtask.songs;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caco3.testtask.R;
import com.caco3.testtask.songsApi.Song;
import com.caco3.testtask.ui.widget.AutoFitRecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


/**
 * Recycler view adapter for {@link SongsActivity}
 */
public class SongsAdapter extends AutoFitRecyclerView.Adapter<SongsAdapter.ViewHolder> {
    /**
     * Where this adapter was created
     */
    private final Context mContext;

    /**
     * {@link Song} items which this adapter holds
     */
    private final List<Song> mItems = new ArrayList<>();


    /**
     * Color ids to select from to set background color to item
     */
    private static final int[] sColorIds = {
            R.color.deep_orange_light,
            R.color.green_light,
            R.color.indigo_light,
            R.color.yellow_light,
            R.color.deep_purple_light,
            R.color.teal_light,
            R.color.purple_light,
            R.color.brown_light};

    /**
     * Used to compare {@link SongsAdapter#mItems} and list
     * of songs in the {@link SongsAdapter#updateItems(List)} methods.
     */
    private static final Comparator<Song> sSongByIdComparator
            = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            /**
             * Can't delegate it to {@link Long#compare(long, long)}
             * since minApiLevel for this app is 15
             */
            if (o1.getId() - o2.getId() < 0) {
                return -1;
            } else if (o1.getId() - o2.getId() > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public SongsAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View songView = inflater.inflate(R.layout.item_song, parent, false);
        return new ViewHolder(songView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mItems.get(position);
        holder.mAuthorView.setText(song.getAuthor());
        holder.mNameView.setText(song.getName());
        /**
         * set item background color depending on the {@link Song#hashCode()}
         */
        holder.mWrapper.setBackgroundColor(
                ResourcesCompat
                        .getColor(mContext.getResources(), sColorIds[Math.abs(song.hashCode()) % sColorIds.length], null));

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Called when this list of this items must be updated.
     * @param items list of items that must replace list in this adapter
     */
    public void updateItems(List<Song> items){
        if (mItems.isEmpty()){
            // Adapter has no items in the list just add all
            for(int i = 0; i < items.size(); i++){
                mItems.add(items.get(i));
                notifyItemInserted(i);
            }
        } else if (items.isEmpty()){
            // No items in new items list. Remove all from adapter's list
            for(int i = mItems.size() - 1; i >= 0; i--){
                mItems.remove(i);
                notifyItemRemoved(i);
            }
        } else {
            // Remove all items in the adapter's list which aren't present in new items list
            // and add all items which present in the new list but not present in adapter'
            // keeping sort order by song id.

            // something like merge procedure for mergeSort
            // this part does its work for O(n^2)

            int newListIdx = 0;
            int adapterListIdx = 0;
            // Start iterating over list in the adapter and new list
            while (newListIdx < items.size() && adapterListIdx < mItems.size()) {
                if (sSongByIdComparator.compare(mItems.get(adapterListIdx), items.get(newListIdx)) < 0) {
                    // item in adapter's list has id which is not present in the new list.
                    // so it must be removed
                    mItems.remove(adapterListIdx);
                    notifyItemRemoved(adapterListIdx);
                } else if (sSongByIdComparator.compare(mItems.get(adapterListIdx), items.get(newListIdx)) == 0) {
                    // current items in the adapter's list and in the new list
                    // have the same id. Update item (id may stay the same, but other song fields may change)
                    if (!mItems.get(adapterListIdx).equals(items.get(newListIdx))) {
                        mItems.set(adapterListIdx, items.get(newListIdx));
                        notifyItemChanged(adapterListIdx);
                    }
                    // analyse next items
                    newListIdx++;
                    adapterListIdx++;
                } else {
                    // current item in the new list is not present in the adapter's list
                    // so we must add it
                    mItems.add(adapterListIdx, items.get(newListIdx));
                    notifyItemInserted(adapterListIdx);
                    // analyse next positions
                    adapterListIdx++;
                    newListIdx++;
                }
            }

            if (newListIdx < items.size()) {
                // there are left some items in the new list which were not added
                // so we must add them
                while (newListIdx < items.size()) {
                    mItems.add(items.get(newListIdx++));
                    notifyItemInserted(mItems.size() - 1);
                }
            } else if (adapterListIdx < mItems.size()) {
                // there are left some items in the adapter's list
                // which are not present in the new list
                // so we have to delete them
                while (sSongByIdComparator.compare(mItems.get(mItems.size() - 1), items.get(items.size() - 1)) > 0) {
                    int removedItemPosition = mItems.size() - 1;
                    mItems.remove(removedItemPosition);
                    notifyItemRemoved(removedItemPosition);
                }
            }

        }

    }

    /**
     * View holder class
     */
     /* package */static class ViewHolder extends AutoFitRecyclerView.ViewHolder{
        private TextView mNameView;
        private TextView mAuthorView;
        private View mWrapper;
        private ViewHolder(View itemView){
            super(itemView);
            mNameView = (TextView) itemView.findViewById(R.id.song_item_name);
            mAuthorView = (TextView) itemView.findViewById(R.id.song_item_author);
            mWrapper = itemView.findViewById(R.id.item_song_wrapper);
        }
    }
}
