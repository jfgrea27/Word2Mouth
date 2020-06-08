package com.imperial.slidepassertrial.learn.main.offline;

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
 * Use the {@link LearnOfflineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOfflineMainFragment extends Fragment {

    private ImageButton learn;
    private ImageButton share;
    private ImageButton receive;
    private ListView courseList;

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

    private void configureReceiveButton() {
        learn = getView().findViewById(R.id.learn_button);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Learn Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureShareButton() {
        share = getView().findViewById(R.id.share_button);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Share Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureLearnButton() {
        receive = getView().findViewById(R.id.receive_button);
        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Receive Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateListContent() {
        for(int i = 0; i < 55; i++) {
            data.add("This is row number " + i);
        }
    }




}
