package com.example.mpowroznik.testapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class ButtonsLearnActivity extends AppCompatActivity {

    private Map<Integer, String> m_learnMap;
    private Map<Integer, String> m_orgMap;
    private List<Integer> mWordsIdx = new ArrayList<>();

    private int mCurIdx;
    private int m_buttonIdx;
    private int m_mapIdx;
    private int m_allWords;
    private int m_correctAnswers;
    private boolean m_bWrongAnswer;
    private boolean m_bRepeatWrong;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCurIdx++;
            learningFunction();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons_learn);

        int listIdx = getIntent().getExtras().getInt("ListIndex");
        String orgLang = getIntent().getExtras().getString("OriginalLanguage");
        if(orgLang.equals("PL")){
            m_orgMap = new TreeMap<>(Storage.getStorage().getPlMap(listIdx));
            m_learnMap = new TreeMap<>(Storage.getStorage().getEngMap(listIdx));
        }
        else {
            m_learnMap = new TreeMap<>(Storage.getStorage().getPlMap(listIdx));
            m_orgMap = new TreeMap<>(Storage.getStorage().getEngMap(listIdx));
        }

        for (int i=0; i<m_learnMap.size(); ++i)
            mWordsIdx.add(i+1);
        Collections.shuffle(mWordsIdx);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        m_bRepeatWrong = prefs.getBoolean("repeat", false);

        m_allWords = m_learnMap.size();
        m_correctAnswers = 0;
        mCurIdx = 0;
        learningFunction();
    }

    public void showStatistic() {
        String msgString = "You have answered correctly " + m_correctAnswers + "/" + m_allWords;
        Message msg = new Message();
        msg.showMessage("Information", msgString, this, true);
    }

    public void learningFunction() {

        if (mWordsIdx.size() <= mCurIdx) {
            showStatistic();
            return;
        }

        Random rand = new Random();
        m_buttonIdx = rand.nextInt(4);

        int idx;
        m_mapIdx = mWordsIdx.get(mCurIdx);

        List<Integer> list = new ArrayList<>();
        list.add(m_mapIdx);
        while(list.size() != 4) {
            idx = rand.nextInt(m_orgMap.size()) + 1;

            boolean bAdd = true;
            for (int i=0;i<list.size();++i) {
                if (list.get(i) == idx) {
                    bAdd = false;
                    break;
                }
            }
            if (bAdd)
                list.add(idx);
        }

        TextView mainText = (TextView) findViewById(R.id.learn_header);
        mainText.setText(m_learnMap.get(m_mapIdx));

        List<Button> buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.learn_button1));
        buttons.add((Button) findViewById(R.id.learn_button2));
        buttons.add((Button) findViewById(R.id.learn_button3));
        buttons.add((Button) findViewById(R.id.learn_button4));
        buttons.get(m_buttonIdx).setText(m_orgMap.get(m_mapIdx));

        int listIdx = 0;
        int mapIdx = 0;
        for(int i=0;i<4;++i) {
            buttons.get(i).setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBlue));

            if (i == m_buttonIdx)
                continue;

            mapIdx = list.get(listIdx++);
            if(mapIdx == m_mapIdx)
                mapIdx = list.get(listIdx++);

            buttons.get(i).setText(m_orgMap.get(mapIdx));

        }

        m_bWrongAnswer = false;
    }

    public void handleAnswer(Button btn, int idx) {
        if (m_buttonIdx == idx) {
            btn.setBackgroundColor(Color.GREEN);
            //SystemClock.sleep(1000);

            if (!m_bWrongAnswer)
                m_correctAnswers++;

            handler.postDelayed(runnable, 100);
        }
        else
        {
            btn.setBackgroundColor(Color.RED);

            if(m_bRepeatWrong) {
                m_allWords++;
                mWordsIdx.add(mWordsIdx.get(mCurIdx));
            }

            m_bWrongAnswer = true;
        }
    }

    public void button1onClick(View view) {
        Button btn = (Button) findViewById(R.id.learn_button1);
        handleAnswer(btn, 0);
    }
    public void button2onClick(View view) {
        Button btn = (Button) findViewById(R.id.learn_button2);
        handleAnswer(btn, 1);
    }
    public void button3onClick(View view) {
        Button btn = (Button) findViewById(R.id.learn_button3);
        handleAnswer(btn, 2);
    }
    public void button4onClick(View view) {
        Button btn = (Button) findViewById(R.id.learn_button4);
        handleAnswer(btn, 3);
    }
}
