package com.brentandjody.stenodictionary;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

/**
 * Created by brentn on 22/12/13.
 */
public class StenoApp extends android.app.Application {
    public static final String KEY_DICTIONARY_SIZE = "dictionary_size";
    public static final String KEY_DICTIONARIES = "dictionaries";
    public static final String DELIMITER = ":";

    private Dictionary mDictionary;
    private SharedPreferences prefs;
    private ProgressBar progressbar;

    @Override
    public void onCreate() {
        super.onCreate();
        mDictionary = new Dictionary(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        progressbar = new ProgressBar(getApplicationContext());
    }

    public void setProgressBar(ProgressBar pb) {
        progressbar = pb;
    }

    public String[] getDictionaryNames() {
        String dicts = prefs.getString(KEY_DICTIONARIES, "");
        if (dicts.isEmpty()) {
            return new String[0];
        } else {
            return dicts.split(DELIMITER);
        }
    }

    public Dictionary getDictionary() {
        return mDictionary;
    }

    public void loadDictionaries() {
        int size = prefs.getInt(KEY_DICTIONARY_SIZE, 100000);
        mDictionary.load(getDictionaryNames(), progressbar, size);
    }
}