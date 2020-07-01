package com.imperial.word2mouth.learn.main.online;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imperial.word2mouth.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseOnlineSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseOnlineSelection extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String teacherName;

    public CourseOnlineSelection() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CourseOnlineSelection.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseOnlineSelection newInstance(String param1) {
        CourseOnlineSelection fragment = new CourseOnlineSelection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacherName = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_online_selection, container, false);
    }
}