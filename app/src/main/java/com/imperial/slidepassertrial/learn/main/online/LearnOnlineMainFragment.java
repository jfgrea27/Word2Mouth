package com.imperial.slidepassertrial.learn.main.online;

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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnOnlineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOnlineMainFragment extends Fragment {

    private ImageButton download;
    private ListView courseList;

    public LearnOnlineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LearnOnlineMainFragment newInstance(String param1, String param2) {
        LearnOnlineMainFragment fragment = new LearnOnlineMainFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_learn_online_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureDownloadButton();
        configureListView();
    }


    // TODO
    // DATA FOR THE COURSE LIST
    // IMPROVE THE ROWS OF THE COURSE SELECTION
    private ArrayList<String> data = new ArrayList<>();

    private void configureListView() {
        courseList = (ListView) getView().findViewById(R.id.list_view_course_offline);
        generateListContent();
        courseList.setAdapter(new ArrayAdapterCourseItems(getView().getContext(), R.layout.list_item, data));
        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getView().getContext(), "Text about the course" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void configureDownloadButton() {
        download = getView().findViewById(R.id.download_button);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Download Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateListContent() {
        for(int i = 0; i < 55; i++) {
            data.add("This is row number " + i);
        }
    }


}