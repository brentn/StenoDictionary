package com.brentandjody.stenodictionary;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brent on 19/12/13.
 */
public class SelectDictionaryActivity extends ListActivity {

    private static final String DICTIONARIES = "dictionaries";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictionary_list);
        List<String> list = loadDictionariyList();
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
        Button addButton = new Button(this);
        addButton.setText(getString(R.string.add_dictionary));
        getListView().addFooterView(addButton);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    private List<String> loadDictionariyList() {
        List<String> result = new ArrayList<String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dictionaries = prefs.getString(DICTIONARIES, "");
        for (String dictionary : dictionaries.split(":")) {
            if (!dictionary.trim().isEmpty()) {
                result.add(dictionary);
            }
        }
        return result;
    }
}
