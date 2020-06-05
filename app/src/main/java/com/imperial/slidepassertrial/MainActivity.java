package com.imperial.slidepassertrial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    //private final static int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 0;

//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT:
//                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Can Access Storeage", Toast.LENGTH_SHORT).show();
//                }
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        }
//    }
//
//    private void checkReadExternalStoragePermission() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
//            PackageManager.PERMISSION_DENIED) {
//                // code
//            }
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this, "App needs storage persmission for content creation", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_RESULT);
//            }
//            //code
//        }
//    }


}