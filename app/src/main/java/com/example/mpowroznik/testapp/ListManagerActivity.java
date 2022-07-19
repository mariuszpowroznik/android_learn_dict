package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListManagerActivity extends AppCompatActivity {

    private static final int LIST_VIEW_REQUEST_CODE = 0;
    private static final int LIST_CREATE_REQUEST_CODE = 1;
    private static final int LIST_EDIT_REQUEST_CODE = 2;
    private ListView mListView;
    private ArrayList<String> mLists;
    private ArrayList<Boolean> mChecked;
    private int mSelectedIdx;
    private boolean mFlush;
    private ListManagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmanager);

        mSelectedIdx = 0;
        mFlush = false;

        Toolbar myToolbar = findViewById(R.id.listManagerViewToolbar);
        setSupportActionBar(myToolbar);

        mChecked = Storage.getStorage().getChecked();
        mLists = Storage.getStorage().getLists();
        mListView = findViewById(R.id.listManagerListView);
        if(getIntent().hasExtra("selectedListIdx")) {
            mSelectedIdx = getIntent().getExtras().getInt("selectedListIdx");
        }

        mAdapter = new ListManagerAdapter(this, mLists, mSelectedIdx);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mSelectedIdx = mAdapter.getSelected();
                mFlush = true;
            }
        });

        mListView.setAdapter(mAdapter);

        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent startIntent = new Intent(view.getContext(), ListActivity.class);
                startIntent.putExtra("index", position);
                startActivityForResult(startIntent, LIST_VIEW_REQUEST_CODE );
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
        getMenuInflater().inflate(R.menu.listmanager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.add_action){
            Intent startIntent = new Intent(getApplicationContext(), ListManagerItemActivity.class);
            startActivityForResult(startIntent, LIST_CREATE_REQUEST_CODE);
            return true;
        }
        else if(id == R.id.edit_action) {
            Intent startIntent = new Intent(getApplicationContext(), ListManagerItemActivity.class);
            startIntent.putExtra("listIndex", mAdapter.getSelected());
            startIntent.putExtra("listName", mLists.get(mAdapter.getSelected()));
            startActivityForResult(startIntent, LIST_EDIT_REQUEST_CODE);
            return true;
        }
        else if(id == R.id.del_action) {
            ArrayList<Integer> toDel= new ArrayList<>();
            for (int i=0; i<mChecked.size(); ++i) {
                if(mChecked.get(i)) {
                    toDel.add(i);
                }
            }

            if(toDel.isEmpty()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean("notifications", false)) {
                    Toast.makeText(getApplicationContext(), "Nothing is selected.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            for(int i = toDel.size()-1; i>=0; i--){
                int idx = (int)toDel.get(i);
                Storage.getStorage().remList(idx);

                if (mSelectedIdx == idx) {
                    mSelectedIdx = 0;
                } else if(mSelectedIdx > idx) {
                    mSelectedIdx--;
                }
            }

            if (mLists.isEmpty()) {
                mSelectedIdx = -1;
            }

            mFlush = true;
            Storage.getStorage().setSelectedList(mSelectedIdx);
            mAdapter.setSelected(mSelectedIdx);
            mListView.invalidateViews();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (LIST_VIEW_REQUEST_CODE) : {
                mListView.invalidateViews();
                mFlush = data.getExtras().getBoolean("flushStorage");
                break;
            }
            case (LIST_CREATE_REQUEST_CODE) :
            case (LIST_EDIT_REQUEST_CODE) :
                if (resultCode == Activity.RESULT_OK) {
                    String listName = data.getStringExtra("listName");

                    if(!listName.isEmpty()) {
                        int idx = data.getExtras().getInt("listIndex");
                        if (idx != -1) {
                            mLists.set(idx, listName);
                        } else {
                            mLists.add(listName);
                            mChecked.add(false);
                            Storage.getStorage().addNewList();
                        }
                        mListView.invalidateViews();
                        Storage.getStorage().flush();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra ("selectedListIdx", mAdapter.getSelected());
        intent.putExtra("flushStorage", mFlush);
        setResult(RESULT_OK, intent);
        finish();
    }
}
