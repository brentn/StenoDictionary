package com.brentandjody.stenodictionary;

import android.app.Activity;
import android.text.Editable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.text.TextWatcher;
import android.content.Intent;
import android.graphics.Color;
import android.widget.ProgressBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.Queue;

public class MainActivity extends Activity implements Dictionary.OnDictionaryLoadedListener, TextWatcher {

    private static final int MAX_POSSIBILITIES = 10;
    private static final int SELECT_DICTIONARY_ACTIVITY = 3;
    private static StenoApp App;

    private EditText lookup;
    private TextView output;
    private LinearLayout possibilities;
    private Dictionary mDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        App = ((StenoApp) getApplication());
        lookup = (EditText) findViewById(R.id.lookup);
        lookup.addTextChangedListener(this);
        output = ((TextView) findViewById(R.id.strokes));
        possibilities = ((LinearLayout) findViewById(R.id.possibilities));
        loadDictionary();
        ImageButton clear_button = (ImageButton) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookup.setText("");
                clearPossibilities();
            }
        });
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
            startActivityForResult(intent, SELECT_DICTIONARY_ACTIVITY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_DICTIONARY_ACTIVITY:
                loadDictionary();
                break;
        }
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
        clearView(output);
        if (lookup.getText().length() > 0)
            lookup_strokes(lookup.getText().toString());
    }

    private void clearView(TextView v) {
        v.setText("");
    }

    private void clearPossibilities() {
        possibilities.removeAllViews();
    }

    private void lockInput() {
        lookup.setEnabled(false);
        findViewById(R.id.overlay).setVisibility(View.VISIBLE);
    }

    private void unlockInput() {
        lookup.setEnabled(true);
        findViewById(R.id.overlay).setVisibility(View.GONE);
    }

    private void loadDictionary() {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressbar);
        LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);
        mDictionary = App.getDictionary(this, lookup, overlay, progressbar);
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
            clearView(output);
        }
        result = mDictionary.possibilities(text, MAX_POSSIBILITIES);
        clearPossibilities();
        if (result != null && result.size()>0) {
            for (String s : result) {
                if (!s.equals(text)) { //ignore the word itself
                    final String choice = s;
                    TextView tv = new TextView(this);
                    tv.setText(choice);
                    tv.setTextColor(Color.parseColor("#FF99CC00"));
                    tv.setTextSize(25);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            lookup.setText(choice);
                        }
                    });
                    possibilities.addView(tv);
                }
            }
        }
    }
}
