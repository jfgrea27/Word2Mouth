package com.imperial.slidepassertrial;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button createButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureCreateButton();
    }

    private void configureCreateButton() {
        createButton = findViewById(R.id.button_create);

        createButton.setOnClickListener(new View.OnClickListener() {
            String courseName = "";
            File courseDirectory;

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Course Name");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        courseName = input.getText().toString();
                        if (courseName.isEmpty()) {
                            courseName = "Untitled Course";
                        }
                       courseDirectory = DirectoryHandler.createDirectoryForCourseAndReturnIt(courseName, getApplicationContext());

                        intentToCreateCourseAndStartActivity();
                    }

                    private void intentToCreateCourseAndStartActivity() {
                        Intent createIntent = new Intent(MainActivity.this, SlideFlickingActivity.class);
                        createIntent.putExtra("course name", courseDirectory.getName());
                        createIntent.putExtra("course directory path", courseDirectory.getPath());
                        startActivity(createIntent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();



            }
        });

    }
}