package org.abazilev.task;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class HashTest {


    private OpenAddressHashMap map;

    @Before
    public void init() {
        map = new OpenAddressHashMap();
    }

    @Test
    public void testSimpleMap() {
        //prepare for test
        //fill the data
        map.put(1, 10l);
        map.put(2, 20l);
        map.put(-2, 30l);
        //make test and check results
        assertThat(map.get(1), is(10l));
        assertThat(map.get(2), is(20l));
        assertThat(map.size(), is(3));
        assertThat(map.containsKey(-2), is(true));
        assertThat(map.containsValue(20l), is(true));
    }

    @Test
    public void testWitchClear() {
        //prepare for test
        //fill the data
        map.put(1, 10l);
        //make test and check results
        assertThat(map.get(1), is(10l));
        assertThat(map.size(), is(1));
        //one more test
        map.clear();
        assertThat(map.size(), is(0));
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue(10l));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAll() {
        //prepare data
        Map<Integer, Long> longMap = new HashMap<>();
        longMap.put(1, 15l);
        longMap.put(2, 25l);
        //test
        map.putAll(longMap);
        //check results
        assertFalse(map.isEmpty());
        assertThat(map.size(), is(2));
        assertThat(map.get(2), is(25l));
        assertThat(map.get(1), is(15l));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue(25l));
        assertFalse(map.containsKey(4));
        assertFalse(map.containsValue(40l));
    }

    @Test
    public void testWithRecreate() {
        //prepare test data and make a test
        for(int i = 0;i < 50;i++) {
            map.put(i, i*10l);
        }
        //check results
        assertThat(map.size(), is(50));
        assertTrue(map.containsKey(20));
        assertTrue(map.containsKey(21));
        assertFalse(map.containsKey(60));
        assertTrue(map.containsValue(200l));
        assertTrue(map.containsValue(210l));
    }

    @Test
    public void testWithRemove() {
        //prepare test data
        map.put(1, 10l);
        map.put(2, 20l);
        map.put(-2, 30l);

        assertThat(map.get(1), is(10l));
        assertThat(map.get(2), is(20l));
        assertThat(map.size(), is(3));
        //make test
        assertThat(map.remove(-2), is(30l));
        assertTrue(map.containsKey(2));
        assertThat(map.size(), is(2));
        assertThat(map.get(1), is(10l));
        assertThat(map.get(2), is(20l));
        assertFalse(map.containsKey(-2));
        assertFalse(map.containsValue(30l));
    }

    @Test
    public void testPutExisted() {
        //prepare test data
        map.put(1, 10l);
        map.put(2, 20l);
        //check
        assertThat(map.size(), is(2));
        assertTrue(map.containsValue(20l));
        //make test and check
        map.put(2, 30l);
        assertThat(map.size(), is(2));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue(30l));
        assertFalse(map.containsValue(20l));
    }

    @Test
    public void stressTest() {
        //prepare test data
        for(int i = 0;i < 10;i++) {
            map.put(i, i*10l);
        }
        for(int i = 0;i < 10;i++) {
            map.put(-i, i*100l);
        }
        //check results
        assertThat(map.size(), is(19));
        assertFalse(map.isEmpty());
        for(int i = 0;i < 10;i++) {
            assertTrue(map.containsKey(i));
            assertTrue(map.containsKey(-i));
            assertThat(map.get(i), is(i*10l));
        }
    }

}