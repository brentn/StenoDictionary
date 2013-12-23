package com.brentandjody.stenodictionary;

import android.app.Activity;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Fragment;
import android.text.TextWatcher;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.widget.ProgressBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import java.util.Queue;

public class MainActivity extends Activity implements Dictionary.OnDictionaryLoadedListener, TextWatcher {

    EditText lookup;
    TextView output;
    ProgressBar progressbar;
    Dictionary mDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
        lookup = (EditText) findViewById(R.id.lookup);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        ((StenoApp) getApplication()).setProgressBar(progressbar);
        output = ((TextView) findViewById(R.id.strokes));
        mDictionary = ((StenoApp) getApplication()).getDictionary();
        if (mDictionary.size() < 10) {
            lockInput();
            mDictionary.setOnDictionaryLoadedListener(this);
            loadDictionary();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SelectDictionaryActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDictionaryLoaded() {
        unlockInput();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        clearOutput();
        if (lookup.getText().length() > 0)
            lookup_strokes(lookup.getText().toString());
    }

    private void clearOutput() {
        output.setText("");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private void loadDictionary() {
        String[] dictionaries = ((StenoApp) getApplication()).getDictionaryNames();
        if (dictionaries.length == 0) {
            Intent intent = new Intent(this, SelectDictionaryActivity.class);
            startActivity(intent);
        } else {
            ((StenoApp) getApplication()).loadDictionaries();
        }
    }

    private void lockInput() {
        lookup.setEnabled(false);
        progressbar.setVisibility(View.VISIBLE);
    }

    private void unlockInput() {
        lookup.setEnabled(true);
        progressbar.setVisibility(View.GONE);
    }

    private void lookup_strokes(String text) {
        StringBuilder sb = new StringBuilder();
        Queue<String> result = mDictionary.lookup(text);
        if (result != null) {
            for (String s : result) {
                sb.append(s);
                sb.append(System.getProperty("line.separator"));
            }
            output.setText(sb.toString());
        } else {
            clearOutput();
        }
    }
}
