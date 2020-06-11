package com.imperial.slidepassertrial.teach.offline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.shared.ArrayAdapterCourseItems;
import com.imperial.slidepassertrial.shared.CourseItem;
import com.imperial.slidepassertrial.shared.FileReader;
import com.imperial.slidepassertrial.teach.offline.create.TeachCourseCreationSummaryActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeachOfflineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeachOfflineMainFragment extends Fragment {

    // View
    private ImageButton create = null;
    private ImageButton edit = null;
    private ListView courseList;


    // Model
    private boolean selectedCourse = false;

    private ArrayList<CourseItem> localCourses = null;


    public TeachOfflineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TeachOfflineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeachOfflineMainFragment newInstance(String param1, String param2) {
        TeachOfflineMainFragment fragment = new TeachOfflineMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_teach_offline_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureCreateButton();
        configureEditButton();
        configureListView();
    }


    private void configureListView() {
        courseList = (ListView) getView().findViewById(R.id.list_view_course_offline);

        localCourses = retrieveLocalCourses();

        courseList.setAdapter(new ArrayAdapterCourseItems(getContext(), R.layout.list_item, localCourses));
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getView().getContext(), "Text about the course" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureEditButton() {
        edit = getView().findViewById(R.id.edit_button);

        if (edit!= null) {
            edit.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                    // TODO
                }
            }
        });
    }

    private void configureCreateButton() {

        create = getView().findViewById(R.id.create_button);

        create.setOnClickListener(new View.OnClickListener() {
            String courseName = "";
            File courseDirectory;
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
                builder.setTitle("Course Name");
                final EditText input = new EditText(getView().getContext());
                input.setHint("Type Course Name");
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
                        courseDirectory = FileHandler.createDirectoryForCourseAndReturnIt(courseName, getView().getContext());
                        intentToCreateCourseAndStartActivity();
                    }

                    private void intentToCreateCourseAndStartActivity() {
                        Intent createIntent = new Intent(getView().getContext(), TeachCourseCreationSummaryActivity.class);
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

    private ArrayList<CourseItem> retrieveLocalCourses() {
        ArrayList<CourseItem> courseItems = new ArrayList<>();

        File directory = getView().getContext().getExternalFilesDir(null);

        File[] courses = directory.listFiles();

        for (File f : courses) {
            String courseName = FileReader.readTextFromFile(f.getPath()+ "/meta/title.txt");

            CourseItem courseItem= new CourseItem(courseName, f.getPath());
            courseItems.add(courseItem);
        }
        return courseItems;
    }
}