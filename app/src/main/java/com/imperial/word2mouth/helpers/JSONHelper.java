package com.imperial.word2mouth.helpers;

import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {


    public static String prepareMetaDataCourseCreation(CourseItem courseItem) {
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

        return jsonCourseMetaData.toString();
    }

    public static String prepareMetaDataLectureCreation(LectureItem lectureItem) {
        JSONObject jsonLectureMetaData = new JSONObject();

        try {
            jsonLectureMetaData.put("course-uuid", lectureItem.getCourseItem().getUuidCourse().toString());
            jsonLectureMetaData.put("lecture-uuid", lectureItem.getUuidLecture().toString());
            // TODO add firebase data here
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonLectureMetaData.toString();
    }

    public static JSONObject getJSONFromString(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
