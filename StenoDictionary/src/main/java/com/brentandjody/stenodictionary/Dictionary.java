package com.brentandjody.stenodictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by brent on 09/12/13.
 * implements a reverse-lookup steno dictionary, with strokes sorted by stroke-length
 */
public class Dictionary {

    private static final String[] DICTIONARY_TYPES = {".json"};
    private static final String TAG = "StenoDictionary";


    private TST<Queue<String>> mDictionary = new TST<Queue<String>>();
    private SharedPreferences prefs;

    public Dictionary(Context c) {
        prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    private OnDictionaryLoadedListener onDictionaryLoadedListener;
    public interface OnDictionaryLoadedListener {
        public void onDictionaryLoaded();
    }
    public void setOnDictionaryLoadedListener(OnDictionaryLoadedListener listener) {
        onDictionaryLoadedListener = listener;
    }

    public void load(String[] filenames, AssetManager assetManager, ProgressBar progressBar, int size) {
        Log.d(TAG, "loading dictionary");
        for (String filename : filenames) {
            if (filename.contains(".")) {
                String extension = filename.substring(filename.lastIndexOf("."));
                if (Arrays.asList(DICTIONARY_TYPES).contains(extension)) {
                    try {
                        File file = new File(filename);
                        if (!file.exists()) {
                            throw new IOException("Dictionary file could not be found.");
                        }
                    } catch (IOException e) {
                        System.err.println("Dictionary File: "+filename+" could not be found");
                    }
                } else {
                    throw new IllegalArgumentException(extension + " is not an accepted dictionary format.");
                }
            }
        }
        new JsonLoader(assetManager, progressBar, size).execute(filenames);
    }

    public Queue<String> lookup(String english) {
        return mDictionary.get(english);
    }

    public Queue<String> possibilities(String partial_word, int limit) {
        if (partial_word.length()<1) return null;
        Queue<String> result = new LinkedList<String>();
        for (String possibility : mDictionary.prefixMatch(partial_word)) {
            result.add(possibility);
            if (result.size() >= limit) break;
        }
        if (result.size() < limit) {
            for (String possibility : mDictionary.prefixMatch("{"+partial_word)) {
                result.add(possibility);
                if (result.size() >= limit) break;
            }
        }
        return result;
    }

    public int size() { return mDictionary.size(); }

    public void unload() {
        mDictionary = null;
        mDictionary = new TST<Queue<String>>();
    }

    private class JsonLoader extends AsyncTask<String, Integer, Long> {
        private int loaded;
        private int total_size;
        private ProgressBar progressBar;
        private int update_interval;
        private AssetManager assetManager;

        public JsonLoader(AssetManager am, ProgressBar progress, int size) {
            assetManager = am;
            progressBar = progress;
            total_size = size;
            update_interval = total_size/100;
            if (update_interval==0)
                update_interval = 1;
        }

        protected Long doInBackground(String... filenames) {
            loaded = 0;
            progressBar.setProgress(0);
            boolean simple = (filenames.length<=1); // if there is only 1 dictionary, load in 1 pass
            if (simple) {
                Log.d(TAG, "Loading in simple mode");
            }
            String line, stroke, english;
            String[] fields;
            unload();
            //if no personal dictionaries are defined, load the default
            if (filenames.length==0) {
                try {
                    InputStream filestream = assetManager.open("dict.json");
                    InputStreamReader reader = new InputStreamReader(filestream);
                    BufferedReader lines = new BufferedReader(reader);
                    while ((line = lines.readLine()) != null) {
                        fields = line.split("\"");
                        if ((fields.length > 3) && (fields[3].length() > 0)) {
                            stroke = fields[1];
                            english = fields[3];
                            addToDictionary(stroke, english);
                            incrementSize();
                        }
                    }
                    lines.close();
                    reader.close();
                    filestream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading default dictionary asset");
                }
            } else {
                TST<String> forwardLookup = new TST<String>();
                for (String filename : filenames) {
                    if (!filename.isEmpty()) {
                        try {
                            File file = new File(filename);
                            FileReader reader = new FileReader(file);
                            BufferedReader lines = new BufferedReader(reader);
                            while ((line = lines.readLine()) != null) {
                                fields = line.split("\"");
                                if ((fields.length > 3) && (fields[3].length() > 0)) {
                                    stroke = fields[1];
                                    english = fields[3];
                                    if (simple) {
                                        addToDictionary(stroke, english);
                                    } else {
                                        forwardLookup.put(stroke, english);
                                    }
                                    incrementSize();
                                }
                            }
                            lines.close();
                            reader.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Dictionary File: " + filename + " could not be found");
                        }
                    }
                }
                if (!simple) {
                    // Build reverse lookup
                    for (String s : forwardLookup.keys()) {
                        english = forwardLookup.get(s);
                        addToDictionary(english, s);
                    }
                    forwardLookup = null; // garbage collect
                }
            }
            return (long) loaded;
        }

        private void addToDictionary(String stroke, String english) {
            StrokeComparator compareByStrokeLength = new StrokeComparator();
            Queue<String> strokes = mDictionary.get(english);
            if (strokes == null)
                strokes = new PriorityQueue<String>(3, compareByStrokeLength);
            strokes.add(stroke);
            mDictionary.put(english, strokes);
            incrementSize();
        }

        private void incrementSize() {
            loaded++;
            if (loaded%update_interval==0)
                onProgressUpdate(loaded);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(total_size);
            progressBar.setProgress(0);
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            int size = safeLongToInt(result);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(StenoApp.KEY_DICTIONARY_SIZE, size);
            editor.commit();
            if (onDictionaryLoadedListener != null)
                onDictionaryLoadedListener.onDictionaryLoaded();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

    }

    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    private class StrokeComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            if (a==null || b==null) return 0;
            int aStrokes = countStrokes(a);
            int bStrokes = countStrokes(b);
            //first compare number of strokes
            if (aStrokes < bStrokes) return -1;
            if (aStrokes > bStrokes) return 1;
            //then compare complexity of strokes
            if (a.length() < b.length()) return -1;
            if (a.length() > b.length()) return 1;
            //otherwise consider them equal
            return 0;
        }

        private int countStrokes(String s) {
            return (s.length()-s.replace("/","").length());
        }
    }
}
