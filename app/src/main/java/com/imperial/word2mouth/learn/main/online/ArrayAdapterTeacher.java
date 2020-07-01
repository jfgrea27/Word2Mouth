package com.imperial.word2mouth.learn.main.online;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.ArrayAdapterCourseItemsOnline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.teach.offline.upload.database.DataTransferObject;

import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ArrayAdapterTeacher  extends ArrayAdapter<Teacher> {


    private int layout;
    private ArrayList<Teacher> teachers;
    private Context context;

    private ArrayMap<String, Uri> profilePictures = new ArrayMap<>();

    private ViewHolder holder;

    public ArrayAdapterTeacher(@NonNull Context context, int resource, @NonNull ArrayList<Teacher> objects) {
        super(context, resource, objects);


        layout = resource;
        teachers = objects;
        this.context = context;
    }


    public void loadThumbnails() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        profilePictures.clear();

        for (Teacher teacher : teachers) {

            StorageReference imageRef = storageRef.child("/" + teacher.getTeacherName() + "/profilePicture/pp.jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    profilePictures.put(teacher.getTeacherName(), uri);
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
            holder.title = convertView.findViewById(R.id.list_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        // Title Course
        holder.title.setText(teachers.get(position).getTeacherName());

        Uri image = profilePictures.get(teachers.get(position).getTeacherName());

        if (image == null) {
            holder.thumbnail.setImageResource(R.drawable.ic_account);
        } else {
            Glide.with(getContext()).load(image).into(holder.thumbnail);

        }


        return convertView;
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

    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        public String getCourseName() {
            return title.getText().toString();
        }
    }
}
