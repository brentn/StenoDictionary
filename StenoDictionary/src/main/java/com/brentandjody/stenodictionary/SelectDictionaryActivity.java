package com.brentandjody.stenodictionary;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brent on 19/12/13.
 */
public class SelectDictionaryActivity extends ListActivity {

    private static final String TAG = "StenoDictionary";
    private static final int FILE_SELECT_CODE = 2;

    private SharedPreferences prefs;
    private boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.dictionary_list);
        List<String> list = loadDictionaryList();
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        Button addButton = new Button(this);
        addButton.setText(getString(R.string.add_dictionary));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDictionary();
            }
        });
        getListView().addFooterView(addButton);
        setListAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (changed) {
            ((StenoApp) getApplication()).loadDictionaries();
            changed = false;
        }
    }

    @Override
    protected void onListItemClick(ListView listview, View v, int position, long id) {
        super.onListItemClick(listview, v, position, id);
        changed=true;
        AlertDialog.Builder adb=new AlertDialog.Builder(SelectDictionaryActivity.this);
        adb.setTitle("Remove Dictionary?");
        adb.setMessage("Are you sure you want to remove this dictionary?");
        final int positionToRemove = position;
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDictionary(positionToRemove);
            }
        });
        adb.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    changed=true;
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = getPath(this, uri);
                    Log.d(TAG, "File Path: " + path);
                    //update list
                    String file = path.substring(path.lastIndexOf("/")+1);
                    ((ArrayAdapter<String>) getListAdapter()).add(file);
                    //update preference
                    String dictionaries = prefs.getString(StenoApp.KEY_DICTIONARIES, "");
                    prefs.edit().putString(StenoApp.KEY_DICTIONARIES, dictionaries+StenoApp.DELIMITER+path).commit();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private List<String> loadDictionaryList() {
        List<String> result = new ArrayList<String>();
        String dictionaries = prefs.getString(StenoApp.KEY_DICTIONARIES, "");
        for (String dictionary : dictionaries.split(StenoApp.DELIMITER)) {
            if (!dictionary.trim().isEmpty()) {
                result.add(dictionary.substring(dictionary.lastIndexOf("/")+1));
            }
        }
        return result;
    }

    private void removeDictionary(int pos) {
        getListView().removeViews(pos, 1);
        String[] prefs_dicts = prefs.getString(StenoApp.KEY_DICTIONARIES, "").split(StenoApp.DELIMITER);
        prefs_dicts[pos] = "";
        String dicts = "";
        for (String d : prefs_dicts) {
            if (!d.trim().isEmpty()) {
                dicts += StenoApp.DELIMITER + d;
            }
        }
        //dicts now contains a string, prefixed with an extra DELIMITER - use substring(1) to skip
        if (!dicts.isEmpty()) {
            dicts = dicts.substring(1);
        }
        prefs.edit().putString(StenoApp.KEY_DICTIONARIES, dicts).commit();
    }

    private void addDictionary() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select your .json dictionary file"),FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

}
