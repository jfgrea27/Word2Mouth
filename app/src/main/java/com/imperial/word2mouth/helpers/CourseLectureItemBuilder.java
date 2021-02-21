package com.imperial.word2mouth.helpers;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.imperial.word2mouth.model.CourseItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class CourseLectureItemBuilder {


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<CourseItem> getCourseItemFromDirectory(String parentStringDirectory, Context context) throws JSONException {

        ArrayList<CourseItem> courseItems = new ArrayList<>();
        ArrayList<JSONObject> jsonCourses = FileSystemHelper.getJSON(parentStringDirectory, context);

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

}
