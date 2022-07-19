package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ListManagerItemActivity extends AppCompatActivity {

    private EditText mEditTxt;
    private int mIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_manager_item);

        mIdx = -1;
        mEditTxt = findViewById(R.id.listNameEditText);

        if (getIntent().hasExtra("listIndex")) {
            mIdx = getIntent().getExtras().getInt("listIndex");
            String listName = getIntent().getExtras().getString("listName");
            mEditTxt.setText(listName);
        }
    }

    public void buttonCancelOnClick(View view) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    public void buttonOkOnClick(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("listIndex", mIdx);
        resultIntent.putExtra("listName", mEditTxt.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

}
