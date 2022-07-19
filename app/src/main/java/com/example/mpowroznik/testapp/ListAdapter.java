package com.example.mpowroznik.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    ListActivity mParent;
    LayoutInflater mInfater;
    ArrayList<String> enStr;
    ArrayList<String> plStr;

    public ListAdapter(ListActivity c, ArrayList<String> en, ArrayList<String> pl) {
        mParent = c;
        enStr = en;
        plStr = pl;
        mInfater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return plStr.size();
    }

    @Override
    public Object getItem(int i) {
        return plStr.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = mInfater.inflate(R.layout.list_layout, null);
        TextView plTextView = (TextView) v.findViewById(R.id.plTextView);
        TextView enTextView = (TextView) v.findViewById(R.id.enTextView);
        plTextView.setText(plStr.get(i));
        enTextView.setText(enStr.get(i));

        final CheckBox checkBox = (CheckBox) v.findViewById(R.id.selectedCheckBox);
        checkBox.setChecked(mParent.isChecked(i));
        //checkBox.setEnabled(true);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParent.setChecked(i, checkBox.isChecked());
            }
        });

        return v;
    }
}
