package com.imperial.word2mouth.learn.main.online.teacher;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.teacher.Teacher;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileReaderHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)

public class ArrayAdapterTeacher  extends ArrayAdapter<Teacher> {


    private int layout;
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private ArrayList<Teacher> queryTeachers;

    private ArrayList<String> followingEmailTeachers = new ArrayList<>();

    private Context context;

    private ArrayMap<String, Uri> profilePictures = new ArrayMap<>();

    private ViewHolder holder;

    public ArrayAdapterTeacher(@NonNull Context context, int resource, @NonNull ArrayList<Teacher> objects) {
        super(context, resource, objects);


        layout = resource;
        queryTeachers = objects;
        this.context = context;
        teachers.addAll(queryTeachers);

    }



    public void loadThumbnails() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("/users");
        profilePictures.clear();

        for (Teacher teacher : teachers) {


            StorageReference imageRef = storageRef.child(teacher.getUID() + "/profilePicture/pp.jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    profilePictures.put(teacher.getTeacherEmail(), uri);
                    notifyDataSetInvalidated();
                    notifyDataSetChanged();
                }
            });
        }

    }


    private boolean isFollowing(String email) {
        return followingEmailTeachers.contains(email);
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ViewHolder();

            holder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            holder.title = convertView.findViewById(R.id.name_Teacher);
            holder.addButton = convertView.findViewById(R.id.follow_button);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        // Title Course
        holder.title.setText(setUserNameOnly(queryTeachers.get(position).getTeacherEmail()));

        Uri image = profilePictures.get(queryTeachers.get(position).getTeacherEmail());

        if (image == null) {
            holder.thumbnail.setImageResource(R.drawable.ic_account);
        } else {
            Glide.with(getContext()).load(image).into(holder.thumbnail);

        }



        if (isFollowing(queryTeachers.get(position).getTeacherEmail())) {
            holder.addButton.setImageResource(R.drawable.ic_unfollow);
        } else {
            holder.addButton.setImageResource(R.drawable.ic_follow);
        }

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing(queryTeachers.get(position).getTeacherEmail())) {
                    unsubscribeTeacher(context, queryTeachers.get(position).getTeacherEmail());
                    holder.addButton.setImageResource(R.drawable.ic_follow);
                } else {
                    try {
                        subscribeTeacher(queryTeachers.get(position).getTeacherEmail());
                        holder.addButton.setImageResource(R.drawable.ic_unfollow);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        return convertView;
    }

    private void unsubscribeTeacher(Context c, String teacherName) {
        File followingFile = new File(context.getExternalFilesDir(null) + DirectoryConstants.cache + DirectoryConstants.following);

        try {
            FileReaderHelper.removesAnyLineMatchingPatternInFile(c, followingFile, teacherName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void subscribeTeacher(String teacherName) throws IOException {
        File followingFile = new File(context.getExternalFilesDir(null) + DirectoryConstants.cache + DirectoryConstants.following);
        if (!followingFile.exists()) {
            try {
                followingFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fw = new FileWriter(followingFile, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(teacherName);
        bw.newLine();
        bw.close();
    }

    private String setUserNameOnly(String email) {
        String temp = email;
        int iend = temp.indexOf("@");
        if (iend != -1) {
            temp = temp.substring(0, iend);
        }
        return temp;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public void filter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        queryTeachers.clear();
        if (query.length() == 0) {
            queryTeachers.addAll(teachers);
        } else {
            for (Teacher wp : teachers) {
                if (wp.getUserName().toLowerCase(Locale.getDefault()).contains(query)) {
                    queryTeachers.add(wp);
                }
            }
        }
        notifyDataSetChanged();

    }

    public void setFollowingTeachersArray(ArrayList<String> followingEmailTeachers) {
        this.followingEmailTeachers = followingEmailTeachers;
    }

    public Uri getThumbnail(String name) {
        return profilePictures.get(name);
    }


    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        ImageButton addButton;
        public String getCourseName() {
            return title.getText().toString();
        }
    }

}
