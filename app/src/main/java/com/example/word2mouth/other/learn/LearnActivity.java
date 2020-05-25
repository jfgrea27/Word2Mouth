package com.example.word2mouth.other.learn;

import android.os.Bundle;

import com.example.word2mouth.R;
import com.example.word2mouth.other.OtherActivity;

public class LearnActivity extends OtherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        configureBackButton();
    }

}
