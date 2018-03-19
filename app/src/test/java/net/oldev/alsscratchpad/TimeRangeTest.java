package net.oldev.alsscratchpad;

import net.oldev.alsscratchpad.TimeRange.HhMm;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class TimeRangeTest {
    @Test
    public void parseAndToPersistString_avg() throws Exception {
        String timeRangeStrIn = "[23:12,11:58]";

        TimeRange timeRange = TimeRange.parse(timeRangeStrIn);

        String timeRangeStrOut = timeRange.toPersistString();

        assertEquals("hh parse spot test", 23, timeRange.begin.hh);
        assertEquals("mm parse spot test", 58, timeRange.end.mm);
        assertEquals("average test parse() and toPersistString()",
                     timeRangeStrIn, timeRangeStrOut);
    }

    @Test
    public void toString_avg() throws Exception {
        TimeRange timeRange = new TimeRange(new HhMm(20, 33),
                                            new HhMm(07, 05));
        String timeRangeStrOut = timeRange.toString();

        assertEquals("average test toString()",
                     "08:33pm - 07:05am", timeRangeStrOut);
    }

    @Test
    public void contains_variation() throws Exception {
        String timeRangeTypical = "[22:30,07:15]";
        doContainsTest("avg - before midnight pos",
                       23, 10, timeRangeTypical, true);
        doContainsTest("avg - after midnight pos",
                       1, 30, timeRangeTypical, true);
        doContainsTest("avg - begin boundary neg",
                       22, 29, timeRangeTypical, false);
        doContainsTest("boundary - begin pos",
                       22, 30, timeRangeTypical, true);
        doContainsTest("boundary - end pos",
                       7, 14, timeRangeTypical, true);
        doContainsTest("boundary - end neg",
                       7, 15, timeRangeTypical, false);
        doContainsTest("avg - outside range neg",
                       9, 5, timeRangeTypical, false);
    }

    private void doContainsTest(String msg, int hr, int minute, String timeRangeStr, boolean expected)
            throws Exception {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hr);
        time.set(Calendar.MINUTE, minute);

        TimeRange timeRange = TimeRange.parse(timeRangeStr);
        boolean actual = timeRange.contains(time);

        assertEquals(msg + " (time: " + hr + ":" + minute + ")", expected, actual);
    }
}
