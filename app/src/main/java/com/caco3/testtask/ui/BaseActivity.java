package com.caco3.testtask.ui;

import android.support.v7.app.AppCompatActivity;

import static com.caco3.testtask.util.LogUtils.*;

/**
 * Represents base activity which must be extended by all activities in this app.
 * Created for possible future extensions.
 * E.g. if we will need to add ActionBar for all activities we can do it here
 * and all activities will inherit it
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = makeLogTag(BaseActivity.class);
}
