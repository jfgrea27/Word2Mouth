package com.imperial.word2mouth.shared;

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

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.Categories;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ArrayAdapterCourseItemsOffline extends ArrayAdapter<CourseItem> {
    private static ArrayList<CourseItem> courseItems;

    private int layout;
    private Context context;

    private ViewHolder holder = new ViewHolder();

    private Categories categories = new Categories();
    private Languages languages = new Languages();

    public ArrayAdapterCourseItemsOffline(@NonNull Context context, int resource, @NonNull ArrayList<CourseItem> objects) {
        super(context, resource, objects);

        layout = resource;
        courseItems = objects;
        this.context = context;
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
            holder.language = convertView.findViewById(R.id.flag);
            holder.category = convertView.findViewById(R.id.category);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        File thumbnailFile;
        final File audioFile = new File(courseItems.get(position).getAudio().getPath());
        Uri audioUri = null;


        thumbnailFile = new File(courseItems.get(position).getThumbnail().getPath());
        if (thumbnailFile.exists()) {
            holder.thumbnail.setImageURI(Uri.parse(thumbnailFile.getPath()));
        }


        // Title Course
        holder.title.setText(courseItems.get(position).getCourseName());

       // audio
        if (audioFile.exists()) {
            audioUri =  Uri.parse(courseItems.get(position).getAudio().getPath());
        }


        final Uri finalAudioUri = audioUri;

        if (courseItems.get(position).getCategory() != null) {
            holder.category.setImageResource(categories.categoryIconMap.get(courseItems.get(position).getCategory()));
        }

        if (courseItems.get(position).getLanguage() != null) {
            holder.language.setImageResource(languages.languageIconMap.get(courseItems.get(position).getLanguage()));
        }



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
        ImageView language;
        ImageView category;

        ImageButton audio;
        public String getCourseName() {
            return title.getText().toString();
        }
    }


    public class Categories {

        public final ArrayList<String> categories = new ArrayList<>(Arrays.asList("Health", "Mechanical", "Agriculture", "Academic"));




        public final HashMap<String, Integer> categoryIconMap= new HashMap<String, Integer>(){{
            put("Health", R.drawable.category_health);
            put("Mechanical", R.drawable.category_mechanical);
            put("Agriculture", R.drawable.category_agriculture);
            put("Academic", R.drawable.category_academic);
        }};

        public String get(int position) {
            return categories.get(position);
        }

    }



    public class Languages {


        public  ArrayList<String> languages = new ArrayList<>(Arrays.asList("English", "Francais", "Maures", "Swahili"));


        public  HashMap<String, Integer> languageIconMap = new HashMap<String, Integer>(){{
            put("English", R.drawable.flag_uk);
            put("Francais", R.drawable.flag_france);
            put("Maures", R.drawable.flag_burkina);
            put("Swahili", R.drawable.flag_kenya);
        }};

        public  String get(int position) {
            return languages.get(position);
        }

    }

}
