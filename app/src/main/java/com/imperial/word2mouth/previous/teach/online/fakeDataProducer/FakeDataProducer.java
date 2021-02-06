package com.imperial.word2mouth.previous.teach.online.fakeDataProducer;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.imperial.word2mouth.previous.shared.DirectoryConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

public class FakeDataProducer {

    private final Activity activity;
    private final int numberSlides;

    public FakeDataProducer(Activity activity, int numberSlides) {
        this.activity= activity;
        this.numberSlides = numberSlides;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String randomTime() {
        int max = 100000;
        String result = new String();
        for (int i = 0; i < numberSlides; i++) {

            result += ThreadLocalRandom.current().nextLong(max);
            result += ",";
        }
        int last = result.length() -1;
        result = result.substring(0, last);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String views() {
        int max = 25;
        String result = new String();
        for (int i = 0; i < numberSlides; i++) {
            result += ThreadLocalRandom.current().nextLong(max);
            result += ",";
        }
        int last = result.length() -1;
        result = result.substring(0, last);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addDataToFile(String version) {
        File f = new File(activity.getExternalFilesDir(null) + DirectoryConstants.lecturerTracking + version + ".txt");

        if (f.exists()) {
            // Populate
            LocalDate day = LocalDate.now().minusDays(30);

            // Store LectureTracker Inside file
            try {

                BufferedWriter out = new BufferedWriter(
                        new FileWriter(f, true));

                for (int i = 0; i < 30; i++) {

                    Instant instant = day.atStartOfDay(ZoneId.systemDefault()).toInstant();

                    out.write(String.valueOf(instant.toEpochMilli()));
                    out.write("\n");
                    out.write(randomTime());
                    out.write("\n");
                    out.write(views());
                    out.write("\n");
                    out.write(views());
                    out.write("\n");
                    day = day.plusDays(1);
                }
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


}
