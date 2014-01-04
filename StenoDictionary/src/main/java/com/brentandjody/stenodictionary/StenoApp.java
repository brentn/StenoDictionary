package com.brentandjody.stenodictionary;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.EditText;

/**
 * Created by brentn on 22/12/13.
 */
public class StenoApp extends android.app.Application {
    public static final String KEY_DICTIONARY_SIZE = "dictionary_size";
    public static final String KEY_DICTIONARIES = "dictionaries";
    public static final String DELIMITER = ":";

    private Dictionary mDictionary;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mDictionary = new Dictionary(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public String[] getDictionaryNames() {
        String dicts = prefs.getString(KEY_DICTIONARIES, "");
        if (dicts.isEmpty()) {
            return new String[0];
        } else {
            return dicts.split(DELIMITER);
        }
    }

    public Dictionary getDictionary(Dictionary.OnDictionaryLoadedListener listener, EditText input, LinearLayout overlay, ProgressBar progressbar) {
        if (! isDictionaryLoaded()) {
            if (overlay != null) overlay.setVisibility(android.view.View.VISIBLE);
            if (input != null) input.setEnabled(false);
            if (progressbar == null) progressbar = new ProgressBar(getApplicationContext());
            mDictionary.setOnDictionaryLoadedListener(listener);
            loadDictionary(progressbar);
        }
        return mDictionary;
    }

    public void unloadDictionary() {
        mDictionary = new Dictionary(getApplicationContext());
    }

    public void loadDictionary(ProgressBar progressbar) {
        int size = prefs.getInt(KEY_DICTIONARY_SIZE, 100000);
        mDictionary.load(getDictionaryNames(), getAssets(), progressbar, size);
    }

    private boolean isDictionaryLoaded() {
        return mDictionary.size() > 10;
    }
}