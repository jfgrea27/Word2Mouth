package com.example.word2mouth.other.teach.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.other.OtherActivity;
import com.example.word2mouth.other.teach.createContent.TeachCourseSlidePageActivity;

import java.io.File;

public class TeachMainActivity extends OtherActivity {

    // Array of strings...
    String[] mobileArray = {"Test", "Test", "Test", "Test","Test"};

    private EditText folderName;

    private Button createContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_main);

        configureBackButton();
        configureCreateContentButton();

        configureListView();
    }

    private void configureListView() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        ListView listView = (ListView) findViewById(R.id.created_content);
        listView.setAdapter(adapter);
    }


    private void configureCreateContentButton() {
        createContent = (Button) findViewById(R.id.button_create_content);
        createContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
//                File folder = getApplicationContext().getDir(folderName.getText().toString(), Context.MODE_PRIVATE);
//                if (!folder.exists()) {
//                    folder.mkdir();
//                    Toast.makeText(TeachMainActivity.this, "Folder at" + getFilesDir() + "/" + folderName, Toast.LENGTH_LONG).show();
//                }
                startActivity(new Intent(TeachMainActivity.this, TeachCourseSlidePageActivity.class));
            }
        });
    }


}
