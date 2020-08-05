package com.imperial.word2mouth.teach.online.courseData;

import com.imperial.word2mouth.teach.online.lectureData.LectureTrackingData;

import java.util.ArrayList;

public class CourseTrackingData {

    public ArrayList<LectureTrackingData> getLectureGeneralData() {
        return lectureGeneralData;
    }

    public void setLectureGeneralData(ArrayList<LectureTrackingData> lectureGeneralData) {
        this.lectureGeneralData = lectureGeneralData;
    }

    private ArrayList<LectureTrackingData> lectureGeneralData = new ArrayList();


}
