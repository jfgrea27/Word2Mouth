package com.imperial.slidepassertrial.shared;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
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

import com.imperial.slidepassertrial.R;

import java.io.File;
import java.util.ArrayList;

public class ArrayAdapterCourseItems extends ArrayAdapter<CourseItem> {
    private static ArrayList<CourseItem> courseItems;

    private int layout;
    private Context context;
    public ArrayAdapterCourseItems(@NonNull Context context, int resource, @NonNull ArrayList<CourseItem> objects) {
        super(context, resource, objects);

        layout = resource;
        courseItems = objects;
        this.context = context;
    }



    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ArrayAdapterCourseItems.ViewHolder mainViewHolder = null;

        File thumbnailFile;
        final File audioFile;
        Uri audioUri = null;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            ArrayAdapterCourseItems.ViewHolder viewhHolder = new ArrayAdapterCourseItems.ViewHolder();

            viewhHolder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            thumbnailFile = new File(courseItems.get(position).getThumbnail().getPath());
            if (thumbnailFile.exists()) {
                viewhHolder.thumbnail.setImageURI(Uri.parse(thumbnailFile.getPath()));
            }


            viewhHolder.title = convertView.findViewById(R.id.list_item_text);
            viewhHolder.title.setText(courseItems.get(position).getCourseName());

            viewhHolder.audio = convertView.findViewById(R.id.list_audio_button);
            audioFile = new File(courseItems.get(position).getAudio().getPath());
            if (audioFile.exists()) {
                audioUri =  Uri.parse(courseItems.get(position).getAudio().getPath());
            }


            convertView.setTag(viewhHolder);
        }

        mainViewHolder = (ArrayAdapterCourseItems.ViewHolder) convertView.getTag();

        final Uri finalAudioUri = audioUri;
        final ViewHolder finalMainViewHolder = mainViewHolder;
        mainViewHolder.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MediaPlayer player;
                if (finalAudioUri != null) {
                    finalMainViewHolder.audio.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(getContext(), finalAudioUri);
                    player.start();

                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            finalMainViewHolder.audio.setColorFilter(null);
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "No audio File", Toast.LENGTH_SHORT).show();
                }


            }


        });

        return convertView;
    }

    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        ImageButton audio;

    }

}
