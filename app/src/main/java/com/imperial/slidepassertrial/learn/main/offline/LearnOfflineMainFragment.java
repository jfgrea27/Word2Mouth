package com.imperial.slidepassertrial.learn.main.offline;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.shared.ArrayAdapterCourseItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnOfflineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOfflineMainFragment extends Fragment {

    // Button View
    private ImageButton learn;
    private ImageButton share;
    private ImageButton receive;


    // ListView
    private ListView courseList;
    private File directory;
    private File[] fileList;
    private  ArrayList<String> fileNames;
    private ArrayAdapterCourseItems adapter;

    // Model
    private boolean selectedCourse = false;
    private String courseName = null;

    public LearnOfflineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOfflineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LearnOfflineMainFragment newInstance() {
        LearnOfflineMainFragment fragment = new LearnOfflineMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_learn_offline_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureLearnButton();
        configureShareButton();
        configureReceiveButton();
        configureListView();
    }



    private void configureListView() {
        courseList = (ListView) getView().findViewById(R.id.list_view_course_offline);
        configureCourseOnSystem();
        adapter = new ArrayAdapterCourseItems(getView().getContext(), R.layout.list_item, fileNames);
        courseList.setAdapter(adapter);
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    View item = courseList.getChildAt(i);
                    if (item != null) {
                        item.setBackgroundColor(Color.WHITE);
                    }
                }

                if (selectedCourse) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedCourse = false;
                    if (learn != null) {
                        learn.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                    if (share != null) {
                        share.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                    courseName = null;

                } else {
                    selectedCourse = true;
                    view.setBackgroundColor(Color.LTGRAY);
                    if (learn != null) {
                        learn.setColorFilter(null);
                    }
                    if (share != null) {
                        share.setColorFilter(null);
                    }
                    courseName = parent.getAdapter().getItem(position).toString();
                }
            }
        });
    }

    private void configureReceiveButton() {
        receive = getView().findViewById(R.id.receive_button);

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Receive Button", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void configureShareButton() {
        share = getView().findViewById(R.id.share_button);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedCourse) {
                    Toast.makeText(getContext(), "Share Button", Toast.LENGTH_SHORT).show();
                }

            }
        });


        if (share != null) {
            share.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    private void configureLearnButton() {
        learn = getView().findViewById(R.id.learn_button);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                   if (courseName != null) {
                       Intent learnIntent = new Intent(getView().getContext(), SlideLearningActivity.class);
                       learnIntent.putExtra("course directory path", getContext().getExternalFilesDir(null).getPath() + "/" + courseName);
                       startActivity(learnIntent);
                   }
                }
            }
        });


        if (learn != null) {
            learn.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    private void configureCourseOnSystem() {
        directory = new File(getView().getContext().getExternalFilesDir(null).getPath());
        fileList = directory.listFiles();
        fileNames = new ArrayList<>();

        for (int i = 0; i < fileList.length; i++) {
            fileNames.add(fileList[i].getName());
        }
    }




}
