package com.kabouzeid.gramophone.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class CalendarUtil {
    private static final long MS_PER_MINUTE = 60 * 1000;
    private static final long MS_PER_DAY = 24 * 60 * MS_PER_MINUTE;

    private Calendar calendar;

    public CalendarUtil() {
        this.calendar = Calendar.getInstance();
    }

    /**
     * Returns the time elapsed so far today in seconds.
     *
     * @return Time elapsed today in seconds.
     */
    public long getElapsedToday() {
        // Time elapsed so far today
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * MS_PER_MINUTE;
    }

    /**
     * Returns the time elapsed so far this week in milliseconds.
     *
     * @return Time elapsed this week in milliseconds.
     */
    public long getElapsedWeek() {
        // Today + days passed
        return getElapsedToday() + ((calendar.get(Calendar.DAY_OF_WEEK) - 1 - calendar.getFirstDayOfWeek()) * MS_PER_DAY);
    }

    /**
     * Returns the time elapsed so far this month in milliseconds.
     *
     * @return Time elapsed this month in milliseconds.
     */
    public long getElapsedMonth() {
        // Today + rest of this month
        return getElapsedToday() + ((calendar.get(Calendar.DAY_OF_MONTH) - 1) * MS_PER_DAY);
    }

    /**
     * Returns the time elapsed so far this month and the last numMonths months in milliseconds.
     *
     * @param numMonths Additional number of months prior to the current month to calculate.
     * @return Time elapsed this month and the last numMonths months in milliseconds.
     */
    public long getElapsedMonths(int numMonths) {
        // Today + rest of this month
        long elapsed = getElapsedMonth();

        // Previous numMonths months
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        for (int i = 0; i < numMonths; i++) {
            month--;

            if (month < Calendar.JANUARY) {
                month = Calendar.DECEMBER;
                year--;
            }

            final Calendar monthCal = new GregorianCalendar(year, month, 1);
            final int daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            elapsed += daysInMonth * MS_PER_DAY;
        }

        return elapsed;
    }

    /**
     * Returns the time elapsed so far this year in milliseconds.
     *
     * @return Time elapsed this year in milliseconds.
     */
    public long getElapsedYear() {
        // Today + rest of this month + previous months until January
        long elapsed = getElapsedMonth();

        int month = calendar.get(Calendar.MONTH) - 1;
        while (month > Calendar.JANUARY) {
            final Calendar monthCal = new GregorianCalendar(calendar.get(Calendar.YEAR), month, 1);
            final int daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            elapsed += daysInMonth * MS_PER_DAY;

            month--;
        }

        return elapsed;
    }
}
