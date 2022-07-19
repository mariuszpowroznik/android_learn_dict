package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class WordActivity extends AppCompatActivity {

    private EditText plEditText;
    private EditText enEditText;
    int mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        mIndex = -1;
        plEditText = findViewById(R.id.plEditText);
        enEditText = findViewById(R.id.enEditText);

        if(getIntent().hasExtra("index")){
            mIndex = getIntent().getExtras().getInt("index");
            if(mIndex != -1){
                String plText = getIntent().getExtras().getString("plText");
                String enText = getIntent().getExtras().getString("enText");
                plEditText.setText(plText);
                enEditText.setText(enText);
            }
        }
    }

    public void buttonCancelOnClick(View view) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    public void buttonOkOnClick(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("index", mIndex);
        resultIntent.putExtra("plText", plEditText.getText().toString());
        resultIntent.putExtra("enText", enEditText.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
