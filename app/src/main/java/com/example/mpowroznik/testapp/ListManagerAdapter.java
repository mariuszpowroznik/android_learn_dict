package com.example.mpowroznik.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ListManagerAdapter extends BaseAdapter {

    private ListManagerActivity mParent;
    private LayoutInflater mInfater;
    private ArrayList<String> mLists;
    private int mSelectedIdx;

    public ListManagerAdapter(ListManagerActivity c, ArrayList<String> lists, int selectedIdx) {
        mParent = c;
        mSelectedIdx = selectedIdx;
        mLists = lists;
        mInfater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getSelected() { return mSelectedIdx; }
    public void setSelected(int idx) { mSelectedIdx = idx; }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public Object getItem(int i) {
        return mLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = mInfater.inflate(R.layout.listmanager_layout, null);
        TextView plTextView = v.findViewById(R.id.listManagerTextView);
        plTextView.setText(mLists.get(i));

        final CheckBox checkBox = v.findViewById(R.id.listManagerCheckBox);
        checkBox.setChecked(mParent.isChecked(i));
        checkBox.setEnabled(true);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParent.setChecked(i, checkBox.isChecked());
            }
        });

        RadioButton radioButton = v.findViewById(R.id.listManagerRadioButton);
        radioButton.setEnabled(Storage.getStorage().getEngMap(i).size() > 4);
        radioButton.setChecked(i == mSelectedIdx);
        radioButton.setTag(i);
        radioButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mSelectedIdx = (Integer)view.getTag();
                notifyDataSetChanged();
            }
        });

        return v;
    }
}
