package com.example.word2mouth.mainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.util.ArrayList;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity {

    private Button buttonToLearn;
    private ListView learnContent;
    private Button shareButton;
    private String selectedCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        goToLearnButton();
        configureSHareButtong();
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

    private static final int CREATE_FILE = 10;

    private void configureSHareButtong() {
        shareButton = findViewById(R.id.button_share);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void goToLearnButton() {
        buttonToLearn = (Button) findViewById(R.id.button_learn);
        buttonToLearn.setVisibility(View.INVISIBLE);

            buttonToLearn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent learnIntent = new Intent(MainActivity.this, LearnCourseSlideActivity.class);
                    learnIntent.putExtra("course", selectedCourse);
                    startActivity(learnIntent);
                }
            });
    }

    private void configureListView() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, getListDirectory());

        learnContent = findViewById(R.id.learn_content);
        learnContent.setAdapter(adapter);

        learnContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = parent.getAdapter().getItem(position).toString();
                buttonToLearn.setVisibility(VISIBLE);
            }
        });
    }

    private ArrayList<String> getListDirectory() {
         File[] files  = getApplicationContext().getExternalFilesDir(null).listFiles();
         ArrayList<String> directoryNames = new ArrayList<>();
         for (File file : files) {
             directoryNames.add(file.getName());
         }
         return directoryNames;
    }


}
