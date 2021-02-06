package com.imperial.word2mouth.previous.teach.online.lectureData;

import java.util.ArrayList;

public class TimeStampLectureData {

    private final String timeStamp;
    private int numberSlides;

    public String getTimeStamp() {
        return timeStamp;
    }

    public ArrayList<Long> getTimeSpent() {
        return timeSpent;
    }

    public ArrayList<Long> getVideo() {
        return video;
    }

    public ArrayList<Long> getAudio() {
        return audio;
    }

    private ArrayList<Long> timeSpent;
    private ArrayList<Long> video;
    private ArrayList<Long> audio;


    public TimeStampLectureData(String timeStamp, String timeSpent, String video, String audio) {
        this.timeStamp = timeStamp;

        this.timeSpent = addFromString(timeSpent);
        this.video = addFromString(video);
        this.audio = addFromString(audio);
    }

    private ArrayList<Long> addFromString(String string) {
        String str[] = string.split(",");
        numberSlides = str.length;
        ArrayList<Long> result = new ArrayList<>();
        for (String s : str) {
            result.add(Long.parseLong(s));
        }
        return result;
    }

    public int numberSlides() {
        return numberSlides;
    }

    public int size() {
        return timeSpent.size();
    }
}
