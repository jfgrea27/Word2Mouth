package com.imperial.word2mouth.helpers;

import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {


    public static JSONObject prepareMetadataCourse(CourseItem courseItem) {
        JSONObject jsonCourseMetaData = new JSONObject();

        try {
            jsonCourseMetaData.put("course-uuid", courseItem.getUuidCourse().toString());
            jsonCourseMetaData.put("course-name", courseItem.getCourseName());
            jsonCourseMetaData.put("course-language", courseItem.getCourseLanguage());
            jsonCourseMetaData.put("course-topic", courseItem.getCourseTopic());
            // TODO add Firebase data here


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonCourseMetaData;
    }

    public static JSONObject prepareMetaDataLectureCreation(LectureItem lectureItem) {
        JSONObject jsonLectureMetaData = new JSONObject();

        try {
            jsonLectureMetaData.put("course-uuid", lectureItem.getCourseItem().getUuidCourse().toString());
            jsonLectureMetaData.put("lecture-uuid", lectureItem.getUuidLecture().toString());
            // TODO add firebase data here
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonLectureMetaData;
    }

    public static JSONObject getJSONFromString(String jsonString) throws JSONException {
        JSONObject jsonObject = null;
        jsonObject = new JSONObject(jsonString);

        return jsonObject;
    }
}
