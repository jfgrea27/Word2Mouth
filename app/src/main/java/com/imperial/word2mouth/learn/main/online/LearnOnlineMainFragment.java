package com.imperial.word2mouth.learn.main.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnOnlineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOnlineMainFragment extends Fragment {

    // Permissions
    private final int INTERNET_PERMISSION = 1;
    private final int READ_WRITE_PERMISSION = 2;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;


    private ListView listTeachers = null;
    private FirebaseDatabase database = null;
    private ArrayList<Teacher> teachers = new ArrayList<>();

    private SearchView searchView = null;

    private ArrayAdapterTeacher adapter;
    private HashMap<String, Teacher> teachersHashMap = new HashMap<>();

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
    public static LearnOnlineMainFragment newInstance() {
        LearnOnlineMainFragment fragment = new LearnOnlineMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_learn_online_main, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            configureTeacherListView();
//            configureSearchView();

        }
    }

    private void configureSearchView() {
        searchView = getView().findViewById(R.id.searchView);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                adapter.filter(text);
                return false;
            }
        });
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


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI

    // ListView Teachers
    private void configureTeacherListView() {
        listTeachers = getView().findViewById(R.id.list_teachers);

        database = FirebaseDatabase.getInstance();

        DatabaseReference teachersRef = database.getReference("users");
        teachersRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    teachers = getTeachers((Map<String, String>) snapshot.getValue());

                    updateListView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getView().getContext(), "Could Not retrieve teachers", Toast.LENGTH_SHORT).show();
            }
        });


        listTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FragmentManager manager = getActivity().getSupportFragmentManager();
                CourseOnlineSelectionFragment frag = CourseOnlineSelectionFragment.newInstance(teachers.get(position).getTeacherName());
                manager.beginTransaction().replace(R.id.frag_courses_per_teacher, frag).addToBackStack("Course per Teacher").commit();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (teachers.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterTeacher(getView().getContext(), R.layout.list_teacher, teachers);
                adapter.loadThumbnails(teachersHashMap);
                listTeachers.setAdapter(adapter);
            }
        }
    }

    private ArrayList<Teacher> getTeachers(Map<String, String> teachers) {
        ArrayList<Teacher> teacherArrayList = new ArrayList<>();


        for (Map.Entry<String, String> entry : teachers.entrySet()) {
            teachersHashMap.put(entry.getKey(), new Teacher(entry.getValue()));
            teacherArrayList.add(new Teacher(entry.getValue()));
        }
        return teacherArrayList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


}