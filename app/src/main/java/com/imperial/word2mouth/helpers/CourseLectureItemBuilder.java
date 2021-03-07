package com.imperial.word2mouth.helpers;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Paths;
import java.util.ArrayList;

public class CourseLectureItemBuilder {


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<CourseItem> getCourseItemFromDirectory(String courseDirectoryString, Context context) throws JSONException {

        ArrayList<CourseItem> courseItems = new ArrayList<>();
        ArrayList<JSONObject> jsonCourses = FileSystemHelper.getCourseJSON(courseDirectoryString, context);

        for (JSONObject jsonCourse : jsonCourses) {

            String course_uuid = jsonCourse.getString("course-uuid");
            String course_name = jsonCourse.getString("course-name");
            String course_language = jsonCourse.getString("course-language");
            String course_category = jsonCourse.getString("course-topic");

            CourseItem courseItem = new CourseItem(course_uuid, course_name, course_language, course_category);
            courseItem.setCourseRootPath(String.valueOf(Paths.get(String.valueOf(context.getExternalFilesDir(null)), FileSystemConstants.offline, course_uuid)));

            courseItem.setCourseLecturePath(Paths.get(courseItem.getCoursePath(), FileSystemConstants.lectures).toString());
            courseItem.setCourseAudioThumbnailPath(Paths.get(courseItem.getCoursePath(), FileSystemConstants.audioThumbnail).toString());
            courseItem.setCourseImageThumbnailPath(Paths.get(courseItem.getCoursePath(), FileSystemConstants.photoThumbnail).toString());
            courseItem.setCourseLecturePath(Paths.get(courseItem.getCoursePath(), FileSystemConstants.lectures).toString());
            courseItems.add(courseItem);
        }
        return courseItems;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<LectureItem> getLectureItemsFromCourseDirectory(CourseItem courseItem, Context context) throws JSONException {
        String lectureDirectoryString = courseItem.getCourseLecturePath();
        ArrayList<LectureItem> lectureItems = new ArrayList<>();
        ArrayList<JSONObject> jsonLectures=  FileSystemHelper.getLectureJSON(lectureDirectoryString, context);

        for (JSONObject jsonLecture : jsonLectures) {

            String lecture_uuid = jsonLecture.getString("lecture-uuid");
            String lecture_name = jsonLecture.getString("lecture-name");

            LectureItem lectureItem = new LectureItem(courseItem, lecture_name);
            lectureItem.setLecturePath(String.valueOf(Paths.get(lectureDirectoryString, lecture_uuid)));

            lectureItem.setSlidePath(Paths.get(lectureDirectoryString, FileSystemConstants.slides).toString());
            lectureItem.setLectureAudioThumbnailPath(Paths.get(lectureDirectoryString, FileSystemConstants.audioThumbnail).toString());
            lectureItem.setLectureImageThumbnailPath(Paths.get(lectureDirectoryString, FileSystemConstants.photoThumbnail).toString());
            lectureItems.add(lectureItem);
        }
        return lectureItems;
    }
}
