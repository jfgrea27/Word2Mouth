package com.example.word2mouth.utilities.video

import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoRule;

class VideoHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    final String OUTPUT_PATH = "/Test"
    VideoHandler videoHandler = new VideoHandler(OUTPUT_PATH);

    @Mock
    InputStream mockInputStream;

    @Test
    public void canCopyAVideoToTheAppropriatePath() {
        File f = new File(OUTPUT_PATH + "/video.mp4");
        when(videoHandler.copyVideo(mockInputStream), verify(f.exists()))

    }

}
