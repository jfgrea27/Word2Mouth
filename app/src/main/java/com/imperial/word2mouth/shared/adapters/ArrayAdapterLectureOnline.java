package com.imperial.word2mouth.shared.adapters;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.teach.online.TeachOnlineCourseSummary;

import java.io.File;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ArrayAdapterLectureOnline extends ArrayAdapter<LectureItem> {


    private final ArrayList<LectureItem> lectureItems;
    private final Context context;
    private final int layout;

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private ArrayMap<String, Uri> soundThumbnails = new ArrayMap<>();
    private ArrayMap<String, Uri> photoThumbnails = new ArrayMap<>();

    private ViewHolder holder;


    public ArrayAdapterLectureOnline(@NonNull Context context, int resource, @NonNull ArrayList<LectureItem> objects) {
        super(context, resource, objects);

        layout = resource;
        lectureItems = objects;
        this.context = context;
    }



    public void loadThumbnails() {

        StorageReference lectureRef = FirebaseStorage.getInstance().getReference("/content/");

        soundThumbnails.clear();
        photoThumbnails.clear();

        for (LectureItem lecture : lectureItems) {

            StorageReference imageRef = lectureRef.child(lecture.getLectureIdentification() + "/Photo Thumbnail");
            StorageReference soundRef = lectureRef.child(lecture.getLectureIdentification() + "/Sound Thumbnail");

            soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    soundThumbnails.put(lecture.getLectureIdentification(), uri);
                    notifyDataSetInvalidated();
                    notifyDataSetChanged();
                }
            });

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    photoThumbnails.put(lecture.getLectureIdentification(), uri);
                    notifyDataSetInvalidated();
                    notifyDataSetChanged();
                }
            });

        }
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

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

        /// Title
        holder.title.setText(lectureItems.get(position).getLectureName());


        // Thumbnail
        holder.thumbnail.setImageURI(photoThumbnails.get(lectureItems.get(position).getLectureIdentification()));


        // Audio
        holder.audioUri = soundThumbnails.get(lectureItems.get(position).getLectureIdentification());


        final Uri finalAudioUri = holder.audioUri;
        final ViewHolder finalHolder = holder;
        holder.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MediaPlayer player;
                if (finalAudioUri != null) {
                    finalHolder.audio.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(getContext(), finalAudioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                finalHolder.audio.setColorFilter(null);
                            }
                        });
                    }

                } else {
                    Toast.makeText(getContext(), "No audio File", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return convertView;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        Uri audioUri;

        ImageButton audio;
        public String getCourseName() {
            return title.getText().toString();
        }
    }



}
