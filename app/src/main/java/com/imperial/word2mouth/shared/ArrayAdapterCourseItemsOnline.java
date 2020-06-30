package com.imperial.word2mouth.shared;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.teach.offline.upload.database.DataTransferObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ArrayAdapterCourseItemsOnline  extends ArrayAdapter<CourseItem> {

    private static ArrayList<CourseItem> courseItems;

    private int layout;
    private Context context;

    private ViewHolder holder = new ViewHolder();

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private ArrayMap<String, Uri> soundThumbnails = new ArrayMap<>();
    private ArrayMap<String, Uri> photoThumbnails = new ArrayMap<>();

    public ArrayAdapterCourseItemsOnline(@NonNull Context context, int resource, @NonNull ArrayList<CourseItem> objects) {
        super(context, resource, objects);

        layout = resource;
        courseItems = objects;
        this.context = context;
    }


    public void loadThumbnails() {

        StorageReference teacherRef = FirebaseStorage.getInstance().getReference(DataTransferObject.userNameRetrieving(user.getEmail()));

        soundThumbnails.clear();
        photoThumbnails.clear();

        for (CourseItem course : courseItems) {
            String courseName = course.getCourseName();
            String courseIdentification = course.getCourseOnlineIdentification();

            String courseAddress = courseName + courseIdentification;

            StorageReference imageRef = teacherRef.child(courseAddress + "/Photo Thumbnail");
            StorageReference soundRef = teacherRef.child(courseAddress + "/Sound Thumbnail");

            soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    soundThumbnails.put(courseIdentification, uri);
                    notifyDataSetInvalidated();
                    notifyDataSetChanged();
                }
            });

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    photoThumbnails.put(courseIdentification, uri);
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

            holder.audio = convertView.findViewById(R.id.list_audio_button);
            holder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            holder.title = convertView.findViewById(R.id.list_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        // Retrieve image

        Glide.with(getContext()).load(photoThumbnails.get(courseItems.get(position).getCourseOnlineIdentification())).into(holder.thumbnail);



        holder.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer player;
                Uri audioUri = soundThumbnails.get(courseItems.get(position).getCourseOnlineIdentification());
                if (audioUri != null) {
                    holder.audio.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(getContext(), audioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                holder.audio.setColorFilter(null);
                            }
                        });
                    }

                } else {
                    Toast.makeText(getContext(), "No audio File", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Title Course
        holder.title.setText(courseItems.get(position).getCourseName());


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

        ImageButton audio;
        public String getCourseName() {
            return title.getText().toString();
        }
    }
}
