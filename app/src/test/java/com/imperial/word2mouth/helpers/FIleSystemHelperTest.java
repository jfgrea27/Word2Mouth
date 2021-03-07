package com.imperial.word2mouth.helpers;

import android.content.Context;

import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@PrepareForTest({
        UUID.class,
        CourseItem.class,
        Context.class
})
@RunWith(PowerMockRunner.class)
public class FIleSystemHelperTest {
    @Test
    public void testCreateCourseFileSystem() throws Exception {
        
    }
}
