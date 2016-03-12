package ru.spbau.anastasia.race.Other;

import junit.framework.TestCase;

public class DataBaseHelperTest extends TestCase {

    public void testSetAndGetBestScore() throws Exception {

        long firstScore = DataBaseHelper.getBestScore();
        assertEquals(firstScore, 0);

        DataBaseHelper.initializeDataBase(null);
        DataBaseHelper.setBestScore(100);

        long secondScore = DataBaseHelper.getBestScore();
        assertEquals(secondScore, 100);
    }
}