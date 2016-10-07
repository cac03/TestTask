package com.caco3.testtask.util;


import android.support.v4.widget.SwipeRefreshLayout;

import com.caco3.testtask.R;

/**
 * Provides some useful ui util methods
 */
public class UiUtils {

    private UiUtils(){
        throw new AssertionError(UiUtils.class.getCanonicalName() + " not intended to be instantiated");
    }

    /**
     * Sets green-yellow-red-purple color scheme for provided
     * {@link SwipeRefreshLayout}
     * @param swipeRefreshLayout to set to
     */
    public static void setColorsForSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout){
        swipeRefreshLayout.setColorSchemeResources(
                R.color.green_light,
                R.color.yellow_light,
                R.color.deep_orange_light,
                R.color.deep_purple_light);
    }
}
