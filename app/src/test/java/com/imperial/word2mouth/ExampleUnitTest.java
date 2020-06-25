package com.imperial.word2mouth;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        ArrayList<Integer> array = new ArrayList<>();

        array.add(0);
        array.add(1);
        array.add(2);

        array.remove(1);

        assertThat(array.get(0), is(0));
        assertThat(array.get(1), is(2));


    }
}