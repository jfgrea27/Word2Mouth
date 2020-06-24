package com.imperial.slidepassertrial;

import org.junit.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        Set<Integer> test = new LinkedHashSet<>();

        test.add(1);
        test.add(1);

        assertEquals(test.size(), 1);
    }
}