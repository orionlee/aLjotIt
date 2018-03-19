package net.oldev.alsscratchpad;

import net.oldev.alsscratchpad.TimeRange.HhMm;

import org.junit.Test;

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
}
