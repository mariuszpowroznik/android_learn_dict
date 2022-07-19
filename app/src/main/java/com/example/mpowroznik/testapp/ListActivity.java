package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class ListActivity extends AppCompatActivity {

    private static final int WORD_ACTIVITY_REQUEST_CODE = 0;
    private static final int COPY_ACTIVITY_REQUEST_CODE = 1;
    private int mListIdx;
    private boolean mFlush;
    private ListView myListView;
    private ArrayList<String> plStr;
    private ArrayList<String> enStr;
    private ArrayList<Boolean> mChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar myToolbar = findViewById(R.id.listViewToolbar);
        setSupportActionBar(myToolbar);

        mChecked = new ArrayList<>();
        plStr = new ArrayList<>();
        enStr = new ArrayList<>();
        mFlush = false;
        mListIdx = 0;
        if(getIntent().hasExtra("index")){
            mListIdx = getIntent().getExtras().getInt("index");
        }

        getStringArrays(mListIdx);
        myListView = findViewById(R.id.myListView);

        ListAdapter adapter = new ListAdapter(this, enStr, plStr);
        myListView.setAdapter(adapter);
        myListView.setClickable(true);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent startIntent = new Intent(view.getContext(), WordActivity.class);
                startIntent.putExtra("index", position);
                startIntent.putExtra("plText", plStr.get(position));
                startIntent.putExtra("enText", enStr.get(position));
                startActivityForResult(startIntent, WORD_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    public void setChecked(int i, boolean bCheck) {
        mChecked.set(i, bCheck);
    }

    public boolean isChecked(int i) {
        return mChecked.get(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.add_action){
            Intent startIntent = new Intent(getApplicationContext(), WordActivity.class);
            startIntent.putExtra("index", -1);
            startActivityForResult(startIntent, WORD_ACTIVITY_REQUEST_CODE);
            return true;
        }
        else if(id == R.id.del_action) {
            if(!isSelected()) { return true; }

            ArrayList<Integer> selected = getSelectedItems();
            for(int i = selected.size()-1; i>=0; i--){
                int idx = (int)selected.get(i);
                plStr.remove(idx);
                enStr.remove(idx);
                mChecked.remove(idx);
            }

            myListView.invalidateViews();
            saveStringArrays();
            return true;
        }
        else if(id == R.id.copy_action) {
            if(!isSelected()) { return true; }
            Intent copy = new Intent(getApplicationContext(), CopyActivity.class);
            startActivityForResult(copy, COPY_ACTIVITY_REQUEST_CODE);
            return true;
        }
        else if(id == R.id.select_action) {

            for (int i=0; i<mChecked.size(); ++i) {
                mChecked.set(i,true);
            }
            myListView.invalidateViews();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (WORD_ACTIVITY_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    String plText = data.getStringExtra("plText");
                    String enText = data.getStringExtra("enText");

                    if(!plText.isEmpty() && !enText.isEmpty()) {
                        int idx = data.getExtras().getInt("index");
                        if (idx == -1) { //add element
                            plStr.add(plText);
                            enStr.add(enText);
                            mChecked.add(false);
                        } else { //edit element
                            plStr.set(idx, plText);
                            enStr.set(idx, enText);
                        }
                        myListView.invalidateViews();
                        saveStringArrays();
                    }
                }
                break;
            }
            case (COPY_ACTIVITY_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    Integer pasteListIdx = data.getExtras().getInt("pasteListIdx");
                    if (pasteListIdx == mListIdx) {
                        if (prefs.getBoolean("notifications", false)) {
                            Toast.makeText(getApplicationContext(), "Cannot copy to the same list.", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        Map<Integer, String > enMap = Storage.getStorage().getEngMap(pasteListIdx);
                        Map<Integer, String > plMap = Storage.getStorage().getPlMap(pasteListIdx);

                        ArrayList<Integer> toCopy = getSelectedItems();
                        for (int i: toCopy) {
                            enMap.put(enMap.size()+1, enStr.get(i));
                            plMap.put(plMap.size()+1, plStr.get(i));
                        }

                        myListView.invalidateViews();
                        mFlush = true;

                        if (prefs.getBoolean("notifications", false)) {
                            Toast.makeText(getApplicationContext(), "Copy done.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    protected boolean isSelected() {
        ArrayList<Integer> selected = getSelectedItems();
        if(selected.isEmpty()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("notifications", false)) {
                Toast.makeText(getApplicationContext(), "Nothing is selected.", Toast.LENGTH_SHORT).show();
            }
        }
        return !selected.isEmpty();
    }

    protected ArrayList<Integer> getSelectedItems() {

        ArrayList<Integer> selected= new ArrayList<>();
        for (int i=0; i<mChecked.size(); ++i) {
            if(mChecked.get(i)) {
                selected.add(i);
            }
        }
        return selected;
    }

    protected void saveStringArrays() {
        Map<Integer, String> enMap = new TreeMap<>();
        Map<Integer, String> plMap = new TreeMap<>();

        for (int i = 0; i < enStr.size(); ++i) {
            enMap.put(i + 1, enStr.get(i));
        }

        for (int i = 0; i < plStr.size(); ++i) {
            plMap.put(i + 1, plStr.get(i));
        }

        Storage.getStorage().updateMaps(mListIdx, plMap, enMap);
        mFlush = true;
    }

    protected void getStringArrays(int idx){
        Map<Integer, String > enMap = Storage.getStorage().getEngMap(idx);
        Map<Integer, String > plMap = Storage.getStorage().getPlMap(idx);

        for(Map.Entry<Integer, String> item: enMap.entrySet()) {
            enStr.add(item.getValue());
        }

        for(Map.Entry<Integer, String> item: plMap.entrySet()) {
            plStr.add(item.getValue());
            mChecked.add(false);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("flushStorage", mFlush);
        setResult(RESULT_OK, intent);
        finish();
    }

}
