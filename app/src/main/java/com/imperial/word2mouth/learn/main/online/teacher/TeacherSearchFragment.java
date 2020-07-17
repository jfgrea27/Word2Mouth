package com.imperial.word2mouth.learn.main.online.teacher;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.LearnSearchFingerCourseFragment;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterTeacher;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherSearchFragment extends Fragment  {


    private ListView listTeachers;
    private SearchView searchTeacher;

    private LearnSearchFingerCourseFragment frag;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Teacher> registeredTeachers = new ArrayList<>();
    private ArrayAdapterTeacher adapter;

    public TeacherSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentSearchTeacher.
     */
    public static TeacherSearchFragment newInstance() {
        TeacherSearchFragment fragment = new TeacherSearchFragment();
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
        return inflater.inflate(R.layout.fragment_search_teacher, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpUI();

        setUpSearchView();

        setUpTeacherList();

        retrieveAllTeachers();
    }

    private void setUpUI() {
        listTeachers = getView().findViewById(R.id.list_teachers);
        searchTeacher = getView().findViewById(R.id.search_teacher);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////




    private void retrieveAllTeachers() {
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                getTeacherList(queryDocumentSnapshots.getDocuments());
                updateListTeachers();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getView().getContext(), "Could not retrieve the teachers", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void getTeacherList(List<DocumentSnapshot> teachers) {
        for (DocumentSnapshot t : teachers) {
            Teacher teacher = new Teacher((String) t.get("email"), (String) t.get("name"), (String) t.get("UID"));
            registeredTeachers.add(teacher);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListTeachers() {
        if (registeredTeachers.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterTeacher(getView().getContext(), R.layout.list_teacher, registeredTeachers);
                adapter.loadThumbnails();
                listTeachers.setAdapter(adapter);
            }
        }
    }


    private void setUpTeacherList() {
        listTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (frag != null) {
                    frag.setTeacherName(registeredTeachers.get(position).getUserName(), registeredTeachers.get(position).getUID());
                    frag.setTeacherUri(adapter.getThumbnail(registeredTeachers.get(position).getTeacherEmail()));
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setUpSearchView() {

        searchTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTeacher.setIconified(false);
            }
        });

        searchTeacher .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                if (adapter != null) {
                    adapter.filter(text);
                }
                return false;
            }
        });
    }

    public void setFragment(LearnSearchFingerCourseFragment learnSearchFingerCourseFragment) {
        this.frag = learnSearchFingerCourseFragment;
    }
}