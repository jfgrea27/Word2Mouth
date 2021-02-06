package com.imperial.word2mouth.previous.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.imperial.word2mouth.previous.main.online.teacher.Teacher;

import java.util.ArrayList;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)

public class ArrayAdapterTeacher  extends ArrayAdapter<Teacher> {


    private int layout;
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private ArrayList<Teacher> queryTeachers;

    private Context context;

    private ArrayMap<String, Uri> profilePictures = new ArrayMap<>();

    private ViewHolder holder;

    public ArrayAdapterTeacher(@NonNull Context context, int resource, @NonNull ArrayList<Teacher> objects) {
        super(context, resource, objects);


        layout = resource;
        queryTeachers = objects;
        this.context = context;

        teachers.clear();
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




    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ViewHolder();

            holder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            holder.title = convertView.findViewById(R.id.name_Teacher);

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


        return convertView;
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

    public Uri getThumbnail(String name) {
        return profilePictures.get(name);
    }


    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        public String getCourseName() {
            return title.getText().toString();
        }
    }

}
