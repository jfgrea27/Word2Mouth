package com.imperial.word2mouth.previous.background;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.TopicItem;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterCourseOnline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LearnOnlineNewLecturesSelectionFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Map<String, ArrayList<String>> newLectures = new HashMap<>();

    private ImageButton searchButton;
    private ListView listCourses;
    private int courseNumber = -1;
    private boolean selectedCourse = false;

    private ArrayList<TopicItem> onlineCourses;
    private ArrayAdapterCourseOnline adapter;
    private ArrayList<ListNewLectures> listLectures;


    public LearnOnlineNewLecturesSelectionFragment() {

    }


    private static final String ARG_PARAM1 = "param1";


    public static LearnOnlineNewLecturesSelectionFragment newInstance(ArrayList<ListNewLectures> listLectures) {
        LearnOnlineNewLecturesSelectionFragment fragment = new LearnOnlineNewLecturesSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, listLectures);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (getArguments() != null) {
            listLectures = getArguments().getParcelableArrayList(ARG_PARAM1);
            convertArrayToMap();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_course_online_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetching courses
        configureListView();

        configureSearchButton();

        configureListCourses();
    }

    private void convertArrayToMap() {
        for (ListNewLectures item : listLectures) {
            newLectures.put(item.courseUID, item.lectures);
        }
    }

    private void configureSearchButton() {
        searchButton = getView().findViewById(R.id.search_button);

        searchButton.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseNumber > -1) {
                    Intent intent = new Intent(getActivity(), LearnOnlineNewCourseSummary.class);
                    intent.putExtra("newLectures", newLectures.get(onlineCourses.get(courseNumber).getCourseOnlineIdentification()));
                    intent.putExtra("course", onlineCourses.get(courseNumber));
                    startActivity(intent);
                }
            }
        });
    }



    private void configureListView() {
        listCourses = getView().findViewById(R.id.list_courses_per_teacher);

        retrieveCourses();
    }

    private void retrieveCourses() {

        ArrayList<TopicItem> topicItems = new ArrayList<>();
        int counter = 0;
        int totalNumberOfCourses = newLectures.size();

        for (Map.Entry<String, ArrayList<String>> entry : newLectures.entrySet()) {
            counter++;
            Query query = db.collection("content").whereEqualTo("type", "Course").whereEqualTo("courseUID", entry.getKey());

            int finalCounter = counter;
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    TopicItem topicItem = null;
                    for (DocumentSnapshot course: queryDocumentSnapshots.getDocuments()) {
                        topicItem = new TopicItem((String) course.get("courseName"), (String) course.get("courseUID"), true);
                        topicItem.setAuthorID((String) course.get("authorUID"));
                        topicItem.setLanguage((String) course.get("language"));
                        topicItem.setCategory((String) course.get("category"));
                    }
                    topicItems.add(topicItem);

                    if (finalCounter == totalNumberOfCourses) {
                        onlineCourses = topicItems;
                        updateListView();
                    }
                }
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (onlineCourses.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterCourseOnline(getView().getContext(), R.layout.list_item, onlineCourses);
                adapter.loadThumbnails();
                listCourses.setAdapter(adapter);
            }
        }
    }



    private void configureListCourses() {
        if (listCourses != null) {
            listCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    for (int i = 0; i < adapter.getCount(); i++) {
                        View item = listCourses.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundColor(Color.WHITE);
                        }
                    }

                    if (selectedCourse) {
                        view.setBackgroundColor(Color.WHITE);
                        courseNumber = -1;
                        searchButton.setVisibility(View.INVISIBLE);

                    } else {
                        view.setBackgroundColor(Color.LTGRAY);
                        courseNumber = position;
                        searchButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }



}
