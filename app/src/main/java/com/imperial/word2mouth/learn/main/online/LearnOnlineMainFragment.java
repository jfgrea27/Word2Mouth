package com.imperial.word2mouth.learn.main.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.LearnActivityMain;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnOnlineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOnlineMainFragment extends Fragment {

    public static final int FINGER_QUERY = 10;
    public static final int SPEAK_QUERY = 11;


    // Permissions
    private final int INTERNET_PERMISSION = 1;
    private final int READ_WRITE_PERMISSION = 2;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;


    private TextView title;

    private ImageButton speak;

    private ImageButton finger;

    public LearnOnlineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment SearchChooserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LearnOnlineMainFragment newInstance() {
        LearnOnlineMainFragment fragment = new LearnOnlineMainFragment();
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
        return inflater.inflate(R.layout.fragment_search_chooser, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            setUpUI();
            configureOnClicks();

        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(getView().getContext(), "Please allow access to Internet Access", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        } else {
            hasInternetAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(getView().getContext(), "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else {
            hasReadWriteStorageAccess = true;
        }
    }

    private boolean hasNecessaryPermissions() {
        return hasReadWriteStorageAccess && hasInternetAccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == INTERNET_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.INTERNET) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasInternetAccess = true;
            }
        }
        if (requestCode == READ_WRITE_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasReadWriteStorageAccess = true;
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void setUpUI() {
        finger = getView().findViewById(R.id.finger_chooser);
        speak = getView().findViewById(R.id.speak_chooser);
        title = getView().findViewById(R.id.title);
    }

    private void configureOnClicks() {
        finger.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                LearnSearchFingerCourseFragment frag;
                frag = LearnSearchFingerCourseFragment.newInstance();
                manager.beginTransaction().replace( R.id.fragment_chooser_query, frag).addToBackStack(null).commit();
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                LearnSearchSpeakCourseFragment frag;
                frag = LearnSearchSpeakCourseFragment.newInstance();
                manager.beginTransaction().replace( R.id.fragment_chooser_query, frag).addToBackStack(null).commit();
            }
        });


        ///////////////////////////////////////////////////////////////////////////////////////////
        // Speak
        speak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.speakSearch));
                return true;
            }
        });
        finger.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.fingerQuery));
                return true;
            }
        });

        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.query));
                return true;
            }
        });
    }


}