package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

public class Message {

    public void showMessage(String title, String message, final Activity activity, final boolean bBack) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        // add a button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(bBack == true && activity != null) {
                    Intent resultIntent = new Intent();
                    activity.setResult(Activity.RESULT_OK, resultIntent);
                    activity.finish();
                }
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
