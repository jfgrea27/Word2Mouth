package com.example.word2mouth.mainActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.other.learn.LearnCourseSlideActivity;
import com.example.word2mouth.other.teach.createContent.TeachCourseSlidePageActivity;
import com.example.word2mouth.other.teach.main.TeachMainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity {

    String[] mobileArray = {"Test", "Test", "Test", "Test","Test"};

    private Button buttonToLearn;
    private ListView learnContent;
    private boolean contentSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        goToLearnButton();
        configureListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.teach) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(new Intent(MainActivity.this, TeachMainActivity.class));;
                    return true;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToLearnButton() {
        buttonToLearn = (Button) findViewById(R.id.button_learn);
            buttonToLearn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, LearnCourseSlideActivity.class));
                    //TODO
//                    if (contentSelected) {
//                        contentSelected  = false;
//                        startActivity(new Intent(MainActivity.this, LearnCourseMainPageActivity.class));
//                    }
                }
            });
    }

    private void configureListView() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        learnContent = findViewById(R.id.learn_content);
        learnContent.setAdapter(adapter);

        learnContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contentSelected = true;
            }
        });
    }


}
