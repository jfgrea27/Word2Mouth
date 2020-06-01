package com.example.word2mouth.other.teach.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.other.OtherActivity;
import com.example.word2mouth.other.teach.createContent.CourseSlideSummaryActivity;
import com.example.word2mouth.other.teach.createContent.TeachCourseSlidePageActivity;
import com.example.word2mouth.other.teach.createContent.data.CourseContent;

import java.io.File;
import java.util.ArrayList;

import static android.view.View.VISIBLE;

public class TeachMainActivity extends OtherActivity {

    private ListView teachCourses;
    private String selectedCourse;

    private Button createContent;
    private final static int CREATING_COURSE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_main);

        configureBackButton();
        configureCreateContentButton();

        configureListView();
    }



    private void configureCreateContentButton() {
        createContent = (Button) findViewById(R.id.button_create_content);
        createContent.setOnClickListener(new View.OnClickListener() {
            String nameCourse = "";
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeachMainActivity.this);
                builder.setTitle("Course Name");
                final EditText input = new EditText(TeachMainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nameCourse = input.getText().toString();
                        if (nameCourse.isEmpty()) {
                            nameCourse = "Untitled Course";
                        }
                        createDirectoryForContent(nameCourse);
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

            private void createDirectoryForContent(String nameCourse) {
                File directoryCourse = new File(getApplicationContext().getExternalFilesDir(null), "/" + nameCourse);

                int directoryNumber = 0;
                if (!directoryCourse.exists()) {
                    directoryCourse.mkdirs();
                } else {
                    Toast.makeText(getApplicationContext(), "Course with name " + nameCourse
                            + " already exists", Toast.LENGTH_SHORT);
                    while(directoryCourse.exists()) {
                        directoryNumber++;
                        directoryCourse = new File(getApplicationContext().getExternalFilesDir(null), "/" + nameCourse + " (" + directoryNumber + ")");

                    }
                    directoryCourse.mkdirs();
                }
                CourseContent courseContent = new CourseContent(directoryCourse.getPath());
                Intent creatingCourseIntent = new Intent(TeachMainActivity.this, CourseSlideSummaryActivity.class);
                creatingCourseIntent.putExtra("contents", courseContent);
                startActivityForResult(creatingCourseIntent, CREATING_COURSE);
            }

        });

    }


    // View
    private void configureListView() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, getListDirectory());

        teachCourses = findViewById(R.id.created_content);
        teachCourses.setAdapter(adapter);

        teachCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = parent.getAdapter().getItem(position).toString();
                //TODO
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
