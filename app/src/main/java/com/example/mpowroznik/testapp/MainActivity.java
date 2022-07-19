package com.example.mpowroznik.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

//ToDo: search??

public class MainActivity extends AppCompatActivity {

    private Storage m_storage;
    private boolean m_bFileRead;
    private static final int BUTTON_LEARN_ACTIVITY_REQUEST_CODE = 1;
    private static final int LIST_MANAGER_ACTIVITY_REQUEST_CODE = 2;
    private static final int WRITE_LEARN_ACTIVITY_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //read the file
        if (!m_bFileRead){
            Storage.createStorage(getApplicationContext());
            m_storage = Storage.getStorage();
            if (m_storage.isExternalStorageWritable()) {
                if(m_storage.readStorageFile()) {
                    m_bFileRead = true;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if(prefs.getBoolean("notifications", false)) {
                        Toast.makeText(getApplicationContext(), "Read data file with dictionary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else {
                Message msg = new Message();
                msg.showMessage("Error !", "External storage not available", this, false);
            }
        }

        //Toast.makeText(getApplicationContext(), "Application initialized...", Toast.LENGTH_LONG).show();
    }

    public void buttonSettingsOnClick(View view) {
        Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settings);
    }

    public void buttonDickOnClick(View view) {
        Intent learnIntent = new Intent(getApplicationContext(), ListManagerActivity.class);
        learnIntent.putExtra("selectedListIdx", Storage.getStorage().getSelectedList());
        startActivityForResult(learnIntent, LIST_MANAGER_ACTIVITY_REQUEST_CODE);
    }

    private boolean isActiveSelection() {
        return Storage.getStorage().getSelectedList() >= 0;
    }

    public void buttonPlOnClick(View view) {
        if(isActiveSelection()) {
            Intent learnIntent = new Intent(getApplicationContext(), ButtonsLearnActivity.class);
            learnIntent.putExtra("ListIndex", Storage.getStorage().getSelectedList());
            learnIntent.putExtra("OriginalLanguage", "PL");
            startActivityForResult(learnIntent, BUTTON_LEARN_ACTIVITY_REQUEST_CODE);
        }
        else {
            showNoActiveListMsg();
        }
    }

    public void buttonEnOnClick(View view) {
        if(isActiveSelection()) {
            Intent learnIntent = new Intent(getApplicationContext(), ButtonsLearnActivity.class);
            learnIntent.putExtra("ListIndex", Storage.getStorage().getSelectedList());
            learnIntent.putExtra("OriginalLanguage", "EN");
            startActivityForResult(learnIntent, BUTTON_LEARN_ACTIVITY_REQUEST_CODE);
        }
        else {
            showNoActiveListMsg();
        }
    }

    public void buttonWriteOnClick(View view) {
        if(isActiveSelection()) {
            Intent learnIntent = new Intent(getApplicationContext(), WriteLearnActivity.class);
            learnIntent.putExtra("ListIndex", Storage.getStorage().getSelectedList());
            startActivityForResult(learnIntent, WRITE_LEARN_ACTIVITY_REQUEST_CODE);
        }
        else {
            showNoActiveListMsg();
        }
    }

    public void showNoActiveListMsg() {
        Message msg = new Message();
        msg.showMessage("Error !", "No active selected list." +
                " \nPlease create or select it in Dictionary first.", this, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (BUTTON_LEARN_ACTIVITY_REQUEST_CODE) : {
                //do nothing
                break;
            }
            case (WRITE_LEARN_ACTIVITY_REQUEST_CODE) : {
                //do nothing
                break;
            }
            case (LIST_MANAGER_ACTIVITY_REQUEST_CODE) : {
                if(resultCode == RESULT_OK) {
                    int idx = data.getExtras().getInt("selectedListIdx");
                    Storage.getStorage().setSelectedList(idx);

                    boolean bFlush = data.getExtras().getBoolean("flushStorage");
                    if(bFlush) {
                        Storage.getStorage().flush();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        if(prefs.getBoolean("notifications", false)) {
                            Toast.makeText(getApplicationContext(), "Data storage updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }
}
