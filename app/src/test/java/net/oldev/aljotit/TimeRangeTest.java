package net.oldev.aljotit;

import net.oldev.aljotit.TimeRange.HhMm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
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
                                            new HhMm(7, 5));
        String timeRangeStrOut = timeRange.toString();

        assertEquals("average test toString()",
                     "08:33pm - 07:05am", timeRangeStrOut);
    }

    @RunWith(Parameterized.class)
    public static class ContainsTest {

        @Parameters
        public static Iterable<Object[]> data() {
            String timeRangeNightTime = "[22:30,07:15]";
            String timeRangeDaytime = "[09:05,13:35]";
            return Arrays.asList(new Object[][]{
                    {"avg - before midnight pos",
                            23, 10, timeRangeNightTime, true},
                    {"avg - after midnight pos",
                            1, 30, timeRangeNightTime, true},
                    {"avg - begin boundary neg",
                            22, 29, timeRangeNightTime, false},
                    {"boundary - begin pos",
                            22, 30, timeRangeNightTime, true},
                    {"boundary - end pos",
                            7, 14, timeRangeNightTime, true},
                    {"boundary - end neg",
                            7, 15, timeRangeNightTime, false},
                    {"avg - outside range neg",
                            9, 5, timeRangeNightTime, false},
                    {"daytime - pos 1",
                            10, 2, timeRangeDaytime, true, },
                    {"daytime - pos 2",
                            12, 50, timeRangeDaytime, true },
                    {"daytime - neg before",
                            8, 48, timeRangeDaytime, false },
                    {"daytime - neg after",
                            14, 12, timeRangeDaytime, false }
            });
        }

        @Parameter public String msg;
        @Parameter(1) public int hr;
        @Parameter(2) public int minute;
        @Parameter(3) public String timeRangeStr;
        @Parameter(4) public boolean expected;

        @Test
        public void test() throws Exception {
            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, hr);
            time.set(Calendar.MINUTE, minute);

            TimeRange timeRange = TimeRange.parse(timeRangeStr);
            boolean actual = timeRange.contains(time);

            assertEquals(msg + " (time: " + hr + ":" + minute + ")", expected, actual);
        }
    }
}
