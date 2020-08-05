package com.imperial.word2mouth.teach.online.lectureData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LectureTrackingData {
    public ArrayList<TimeStampLectureData> getData() {
        return data;
    }

    private ArrayList<TimeStampLectureData> data = new ArrayList<>();

    public LectureTrackingData(File filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            if (filePath.length() > 0){
                String timeStamp = br.readLine();

                while(timeStamp != null && !timeStamp.isEmpty()) {
                    String timeSpent = br.readLine();
                    String video = br.readLine();
                    String audio = br.readLine();
                    data.add(new TimeStampLectureData(timeStamp, timeSpent, video, audio));
                    timeStamp = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
