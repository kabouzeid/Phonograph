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


    /**
     * Purpose: to verify leap year
     * cf. month parameter value is needed to minus 1
     * Input: getDaysInMonth (2024,1) -> days in that month/year
     *        getDaysInMonth (2022,1) -> days in that month/year
     * Expected:
     *      (2024,1) = 29
     *      (2022,1) = 28
     */
    @Test
    public void testLeapYearGetDaysInMonth() {
        int februaryDaysInLeafYear = CalendarUtil.getInstance().getDaysInMonth(2024, 1);
        Assert.assertEquals(29,februaryDaysInLeafYear);

        int februaryDays = CalendarUtil.getInstance().getDaysInMonth(2022, 1);
        Assert.assertEquals(28,februaryDays);
    }
}