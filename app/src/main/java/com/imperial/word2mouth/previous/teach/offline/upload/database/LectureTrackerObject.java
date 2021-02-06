package com.imperial.word2mouth.previous.teach.offline.upload.database;

import java.util.ArrayList;
import java.util.Collections;

public class LectureTrackerObject {
    public final String version;

    public ArrayList<Long> time;
    public ArrayList<Integer> video;
    public ArrayList<Integer> audio;


    public LectureTrackerObject(String version, int i) {
        this.version = version;
        time = new ArrayList<>(Collections.nCopies(i, Long.parseLong(String.valueOf(0))));
        video = new ArrayList<>(Collections.nCopies(i, 0));
        audio = new ArrayList<>(Collections.nCopies(i, 0));
    }
}
