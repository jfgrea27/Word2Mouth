package com.imperial.word2mouth.previous.teach.online.lectureData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LectureTrackingData {


    public int getNumberSlides() {
        return numberSlides;
    }

    private int numberSlides;
    private boolean onlyOnceSlideCount = false;
    public ArrayList<TimeStampLectureData> getData() {
        return data;
    }

    private ArrayList<TimeStampLectureData> data = new ArrayList<>();

    public  LectureTrackingData() {}

    public LectureTrackingData(File filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            if (filePath.length() > 0){
                String timeStamp = br.readLine();

                while(timeStamp != null && !timeStamp.isEmpty()) {
                    String timeSpent = br.readLine();
                    String video = br.readLine();
                    String audio = br.readLine();
                    TimeStampLectureData temp = new TimeStampLectureData(timeStamp, timeSpent, video, audio);
                    if (!onlyOnceSlideCount) {
                        numberSlides = temp.numberSlides();
                        onlyOnceSlideCount = true;
                    }
                    data.add(temp);

                    timeStamp = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setData(ArrayList<TimeStampLectureData> data) {
        this.data = data;
    }
}
