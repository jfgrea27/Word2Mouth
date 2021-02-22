package com.imperial.word2mouth;

import com.google.gson.JsonParseException;
import com.imperial.word2mouth.helpers.JSONHelper;
import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


@PrepareForTest({
        UUID.class,
        CourseItem.class,
        LectureItem.class
})
@RunWith(PowerMockRunner.class)
public class JSONHelperTest {

    @Test
    public void testPreparesMetadataCourse() throws JSONException {
        UUID courseUUID = UUID.randomUUID();
        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(courseUUID);
        CourseItem sampleCourseItem = new CourseItem(
                 "Course Name",
                "Language",
                "Category");

        JSONObject result = JSONHelper.prepareMetadataCourse(sampleCourseItem);

        assertEquals(result.get("course-uuid"), courseUUID.toString());
        assertEquals(result.get("course-name"), "Course Name");
        assertEquals(result.get("course-language"), "Language");
        assertEquals(result.get("course-topic"), "Category");
        // TODO complete these as more json fields get filled.
    }


    @Test
    public void testPrepareMetadataLecture() throws JSONException {
        UUID courseUUID = UUID.randomUUID();
        UUID lectureUUID = UUID.randomUUID();
        CourseItem MOCK_COURSE_ITEM = Mockito.mock(CourseItem.class);
        PowerMockito.when(MOCK_COURSE_ITEM.getUuidCourse()).thenReturn(courseUUID);
        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(lectureUUID);
        LectureItem sampleLectureItem = new LectureItem(MOCK_COURSE_ITEM, "Lecture Name");

        JSONObject result = JSONHelper.prepareMetaDataLectureCreation(sampleLectureItem);

        assertEquals(result.get("course-uuid"), courseUUID.toString());
        assertEquals(result.get("lecture-uuid"), lectureUUID.toString());

        // TODO complete these as more json fields get filled.
    }

    @Test(expected = JSONException.class)
    public void testGetJSONFromString() throws JSONException {
        String WELL_FORMED_JSON = "{\"key\": \"value\"}";
        String BADLY_FORMED_JSON = "key:\"value\"}";
        String EMPTY_STRING = "{}";

        assertEquals(JSONHelper.getJSONFromString(WELL_FORMED_JSON).get("key"), "value");
        assertEquals(JSONHelper.getJSONFromString(EMPTY_STRING).length(), 0);
        JSONHelper.getJSONFromString(BADLY_FORMED_JSON);

    }



}
