package com.kabouzeid.gramophone.util;

import static org.junit.Assert.*;

import com.kabouzeid.gramophone.dialogs.DialogFactory;

import org.junit.Assert;
import org.junit.Test;

public class CalendarUtilTest {

    /**
     * Purpose: to confirm Singleton Pattern
     * Input: getInstance calendarUtil1:()->CalendarUtil, calendarUtil2:()->CalendarUtil
     * Expected:
     *      calendarUtil1 = calendarUtil2
     */
    @Test
    public void calendarUtilTestGetInstance() {
        System.out.println("test start");
        CalendarUtil calendarUtil1 = CalendarUtil.getInstance();
        CalendarUtil calendarUtil2 = CalendarUtil.getInstance();
        Assert.assertEquals(calendarUtil1,calendarUtil2);
    }

    @Test
    public void getElapsedToday() {
    }

    @Test
    public void getElapsedDays() {
    }

    @Test
    public void getElapsedWeek() {
    }

    @Test
    public void getElapsedMonth() {
    }

    @Test
    public void getElapsedMonths() {
    }

    @Test
    public void getElapsedYear() {
    }
}