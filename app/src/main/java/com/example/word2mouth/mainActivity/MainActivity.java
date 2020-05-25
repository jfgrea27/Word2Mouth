package com.example.word2mouth.mainActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.word2mouth.other.learn.LearnActivity;
import com.example.word2mouth.R;
import com.example.word2mouth.other.teach.TeachActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonToLearn;
    private Button buttonToTeach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        goToLearnButton();
        goToTeachButton();
    }

    private void goToTeachButton() {
        buttonToTeach = (Button) findViewById(R.id.button_teach);
        buttonToTeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TeachActivity.class));
            }
        });
    }

    private void goToLearnButton() {
        buttonToLearn = (Button) findViewById(R.id.button_learn);
        buttonToLearn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LearnActivity.class));

            }
        });
    }

}
