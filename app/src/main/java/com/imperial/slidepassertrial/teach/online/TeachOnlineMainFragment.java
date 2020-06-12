package com.imperial.slidepassertrial.teach.online;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.imperial.slidepassertrial.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeachOnlineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeachOnlineMainFragment extends Fragment {

    private ImageButton upload;
    private WebView onlineCoursesView;

    public TeachOnlineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeachOnlineMainFragment newInstance(String param1, String param2) {
        TeachOnlineMainFragment fragment = new TeachOnlineMainFragment();
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
        return inflater.inflate(R.layout.fragment_teach_online_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureUploadButton();
        configureWebView();
    }

    private void configureWebView() {
        onlineCoursesView = (WebView) getView().findViewById(R.id.online_courses_web);

        onlineCoursesView.getSettings().setLoadsImagesAutomatically(true);
        onlineCoursesView.setWebViewClient(new WebViewClient());
        onlineCoursesView.getSettings().setJavaScriptEnabled(true);
        onlineCoursesView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        onlineCoursesView.loadUrl("https://www.google.com/");

    }



    private void configureUploadButton() {
        upload = getView().findViewById(R.id.upload_button);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Upload Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

}