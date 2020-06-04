package com.imperial.slidepassertrial;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CourseTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    SlideInterface slide1 = context.mock(SlideInterface.class, "Slide 1");
    SlideInterface slide2 = context.mock(SlideInterface.class, "Slide 2");

    public final int SLIDE_1_POSIITON = 0;
    public final int SLIDE_2_POSIITON = 1;

    public final String COURSE_NAME_1 = "Test Course 1";

    Course course = new Course(COURSE_NAME_1);

    @Test
    public void canAddSlidesToTheCourse() {
        context.checking(new Expectations() {{
            exactly(1).of(slide1).getSlideNumber(); will(returnValue(SLIDE_1_POSIITON));
        }});
        assertThat(course.size(), is(0));
        course.addSlide(slide1);

        assertThat(course.size(), is(1));
    }

    @Test
    public void itemNotAddedIfThereIsAlreadyAnItemOfThatPositionInTheSlides() {

        context.checking(new Expectations(){{
            exactly(2).of(slide1).getSlideNumber(); will(returnValue(SLIDE_1_POSIITON));
        }});

        course.addSlide(slide1);
        course.addSlide(slide1);
        assertThat(course.size(), is(1));

    }



    @Test
    public void canRetrieveTheRightSlideWhenGivenAPosition() {

        context.checking(new Expectations(){{
            exactly(1).of(slide1).getSlideNumber(); will(returnValue(SLIDE_1_POSIITON));
            exactly(1).of(slide2).getSlideNumber(); will(returnValue(SLIDE_2_POSIITON));

        }});

        course.addSlide(slide1);
        course.addSlide(slide2);

        assertThat(course.retrieveByPosition(0), is(slide1));
        assertThat(course.retrieveByPosition(1), is(slide2));
    }

    @Test
    public void returnsNullWhenOutOfBoundSlidePosition() {

        context.checking(new Expectations(){{
            exactly(1).of(slide1).getSlideNumber(); will(returnValue(SLIDE_1_POSIITON));

        }});

        course.addSlide(slide1);

        assertNull(course.retrieveByPosition(-1));
        assertNull(course.retrieveByPosition(1));

    }
}