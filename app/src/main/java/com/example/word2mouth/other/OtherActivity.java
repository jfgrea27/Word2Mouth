package com.example.word2mouth.other;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.word2mouth.R;

public abstract class OtherActivity extends AppCompatActivity {
    private Button backButton;

    protected void configureBackButton() {
        backButton = (Button) findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
