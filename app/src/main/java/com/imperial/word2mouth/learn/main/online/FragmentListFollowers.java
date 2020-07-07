package com.imperial.word2mouth.learn.main.online;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.imperial.word2mouth.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentListFollowers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentListFollowers extends Fragment {


    private ListView listFollowers;
    private SearchView searchFollowers;

    public FragmentListFollowers() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentListFollowers.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentListFollowers newInstance() {
        FragmentListFollowers fragment = new FragmentListFollowers();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_followers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpUI();

        setUpSearchView();

        setUpListFollowers();
    }

    private void setUpListFollowers() {

    }

    private void setUpSearchView() {

    }

    private void setUpUI() {
        listFollowers = getView().findViewById(R.id.list_following);
        searchFollowers = getView().findViewById(R.id.search_following);
    }






}