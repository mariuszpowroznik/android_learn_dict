package com.example.mpowroznik.testapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class WriteLearnActivity extends AppCompatActivity {

    Map<Integer, String> mPlMap;
    Map<Integer, String> mEnMap;

    private int mLevel = 0;
    private int mCursor = 0;
    private ArrayList<Integer> mSpaces;
    private List<Character> mLetters = new ArrayList<>();
    private List<Integer> mWordsIdx = new ArrayList<>();
    private Set<Character> mLettersSet = new HashSet<>();
    private ArrayList<Character> mLearnWord;
    private ArrayList<Character> mOrgWord;

    private int mRepeatCnt = 0;
    private int mCurIdx = 0;
    private int mCorrectCnt = 0;
    boolean mCorrect = true;
    boolean mRepeatWrong;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            restoreColor();
        }
    };
    private Runnable runNextWord = new Runnable() {
        @Override
        public void run() {
            learningAction(); };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_learn);

        int listIdx = getIntent().getExtras().getInt("ListIndex");
        mPlMap = new TreeMap<>(Storage.getStorage().getPlMap(listIdx));
        mEnMap = new TreeMap<>(Storage.getStorage().getEngMap(listIdx));

        mSpaces = new ArrayList<>();
        mLearnWord = new ArrayList<>();
        mOrgWord = new ArrayList<>();

        for (int i=0; i<mPlMap.size(); ++i)
            mWordsIdx.add(i+1);
        Collections.shuffle(mWordsIdx);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mRepeatWrong = prefs.getBoolean("repeat", false);

        mCurIdx = 0;
        mRepeatCnt = 0;
        mCorrectCnt = 0;
        mLevel = getLevel();
        learningAction();
    }

    protected void nextLetter() {
        mCursor = -1;
        for (int i = 0; i < mLearnWord.size(); ++i) {
            if (mLearnWord.get(i) == '_') {
                mCursor = i;
                break;
            }
        }

        printWord();

        if (mCursor == -1) {
            if(mCorrect)
                mCorrectCnt++;
            mCurIdx++;
            handler.postDelayed(runNextWord, 300);
        }
        else {
            //check buttons
            if (!mLettersSet.contains(mOrgWord.get(mCursor)))
                setButtons();
        }
    }

    protected void learningAction() {

        if(mCurIdx < mWordsIdx.size()) {
            String enWord = mEnMap.get(mWordsIdx.get(mCurIdx));
            String plWord = mPlMap.get(mWordsIdx.get(mCurIdx));
            TextView constText = findViewById(R.id.plTextView);
            constText.setText(plWord);

            mCorrect = true;
            prepareWord(enWord);
            printWord();
            setButtons();
        }
        else {
            showStatistic();
        }
    }

    protected void setButtons() {
        mLetters.clear();
        mLettersSet.clear();

        for (int i = mCursor; i < mLearnWord.size(); ++i) {
            if (mLearnWord.get(i) == '_' && !mLettersSet.contains(mOrgWord.get(i))) {
                mLetters.add(mOrgWord.get(i));
                mLettersSet.add(mOrgWord.get(i));
            }
            if (mLetters.size() == 5)
                break;
        }

        String alphabet = "qwertyuiopasdfghjklzxcvbnm";
        Random rand = new Random();

        while (mLetters.size() < 10) {
            Character c = alphabet.charAt(rand.nextInt(alphabet.length()));
            if (!mLettersSet.contains(c)) {
                mLettersSet.add(c);
                mLetters.add(c);
            }
        }

        int i = 0;
        Collections.shuffle(mLetters);
        Button b = findViewById(R.id.button1);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button2);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button3);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button4);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button5);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button6);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button7);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button8);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button9);
        b.setText(mLetters.get(i++).toString());
        b = findViewById(R.id.button10);
        b.setText(mLetters.get(i++).toString());
    }

    protected void restoreColor() {
        Button b = findViewById(R.id.button1);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button2);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button3);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button4);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button5);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button6);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button7);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button8);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button9);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
        b = findViewById(R.id.button10);
        b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));
    }

    protected void printWord() {
        String str = new String();
        boolean bCursor = false;
        int arrayIdx = 0;
        int nextSpaceIdx = -1;
        if (!mSpaces.isEmpty())
            nextSpaceIdx = mSpaces.get(arrayIdx++);

        for (int i = 0; i < mLearnWord.size(); ++i) {
            if (i == nextSpaceIdx) {
                str += " ";
                if (mSpaces.size() > arrayIdx)
                    nextSpaceIdx = mSpaces.get(arrayIdx++);
            }

            if (!bCursor && mLearnWord.get(i) == '_') {
                bCursor = true;
                mCursor = i;
            }

            str += "<u>";
            str += mLearnWord.get(i);
            str += "</u>";
        }

        TextView writeText = findViewById(R.id.writeTextView);
        writeText.setText(Html.fromHtml(str));
    }

    protected void prepareWord(String org) {
        mSpaces.clear();
        mLearnWord.clear();
        mOrgWord.clear();

        int numLetters = 0;
        for (int i = 0; i < org.length(); ++i) {
            if (org.charAt(i) != ' ') {
                numLetters++;
                mLearnWord.add(org.charAt(i));
                mOrgWord.add(org.charAt(i));
            } else
                mSpaces.add(numLetters);
        }

        int lettersToFind = Math.round((mLevel * numLetters) / (float) 100.0);
        if (lettersToFind <= 0)
            lettersToFind = 1;

        Random rand = new Random();
        Integer idx;
        Set<Integer> idxToFind = new TreeSet<>();
        while (idxToFind.size() != lettersToFind) {
            idx = rand.nextInt(numLetters);
            idxToFind.add(idx);
        }

        for (Integer spaceIdx : idxToFind) {
            mLearnWord.set(spaceIdx, '_');
        }
    }

    protected int getLevel() {
        SharedPreferences Preference = PreferenceManager.getDefaultSharedPreferences(this);
        String level = Preference.getString("level_preference", "-1");

        int ret = 0; //percentage of letters to find
        switch (level) {
            case "1": //Easy
                ret = 20;
                break;
            case "2":
                ret = 50;
                break;
            case "3":
                ret = 80;
                break;
            case "4": //Expert
            default:
                ret = 100;
                break;
        }

        return ret;
    }

    public void buttonHelpOnClick(View view) {
        if(mCursor != -1) {
            mCorrect = false;
            mLearnWord.set(mCursor, mOrgWord.get(mCursor));
            nextLetter();
        }
    }

    public void buttonOnClick(View view) {
        Character c;
        int id = view.getId();
        Button b = findViewById(id);
        String str = b.getText().toString();
        c = str.toLowerCase().charAt(0);

        if (mCursor != -1) {
            if (mOrgWord.get(mCursor) == c) {
                mLearnWord.set(mCursor, c);
                b.setBackgroundColor(Color.GREEN);
                handler.postDelayed(runnable, 100);
                nextLetter();
            } else {
                if(mCorrect && mRepeatWrong) {
                    mWordsIdx.add(mWordsIdx.get(mCurIdx));
                    mRepeatCnt++;
                }
                mCorrect = false;

                b.setBackgroundColor(Color.RED);
                handler.postDelayed(runnable, 100);
            }
        }
    }

    public void showStatistic() {
        String msgString = new String();
        msgString = "You have answered correctly " + mCorrectCnt + "/" + (mPlMap.size()+mRepeatCnt);
        Message msg = new Message();
        msg.showMessage("Information", msgString, this, true);
    }
}
