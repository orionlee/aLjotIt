package net.oldev.aljotit;

import java.util.Calendar;

public class TimeRange {

    public static class HhMm {
        public final int hh;
        public final int mm;

        public HhMm(int hh, int mm) {
            this.hh = hh;
            this.mm = mm;
        }

        private String lpadZero(String s) {
            switch (s.length()) {
                case 0:
                return "00";
                case 1:
                    return "0" + s;
                default:
                    return s;
            }
        }

        /**
         * @return a human friendly representation of the time
         */
        @Override
        public String toString() {
            String ampm = ( hh >= 12 ? "pm" : "am" );
            String hhStr = ( hh > 12 ?
                    lpadZero(String.valueOf(hh - 12)) :
                    lpadZero(String.valueOf(hh)) );
            String mmStr = lpadZero(String.valueOf(mm));

            return hhStr + ":" + mmStr + ampm;
        }

        /**
         *
         * @return a string representation appropriate for persistence usage.
         * @see #parse(String) to restore it from a string
         */
        public String toPersistString() {
            return String.valueOf(hh)+":"+String.valueOf(mm);
        }

        public static HhMm parse(String timePersistString) {
            String[] pieces=timePersistString.split(":");
            int hour = Integer.parseInt(pieces[0]);
            int minute = Integer.parseInt(pieces[1]);
            return new HhMm(hour, minute);

        }

    }

    public final HhMm begin;
    public final HhMm end;

    public TimeRange(HhMm begin, HhMm end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     *
     * @return true if the supplied time (HH:MM portion) falls within this range object,
     * false otherwise. The range includes #begin, but exclude #end
     */
    public boolean contains(Calendar time) {
        int hr = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        if (isAcrossMidnight()) {
            return (hr > begin.hh ||
                    (hr == begin.hh && minute >= begin.mm)) ||
                    (hr < end.hh ||
                            (hr == end.hh && minute < end.mm));
        } else {
            return (hr > begin.hh ||
                    (hr == begin.hh && minute >= begin.mm)) &&
                    (hr < end.hh ||
                            (hr == end.hh && minute < end.mm));
        }
    }

    // determine if the range is across midnight.
    // Used in #contains calculation
    private boolean isAcrossMidnight() {
        return begin.hh > end.hh ||
                (begin.hh == end.hh && begin.mm < end.mm);
    }

    /**
     * @return a human friendly representation of the time range.
     */
    @Override
    public String toString() {
        return begin.toString() + " - " + end.toString();
    }

    /**
     *
     * @return a string representation appropriate for persistence usage.
     * @see #parse(String) to restore it from a string
     */
    public String toPersistString() {
        return "[" + begin.toPersistString() + "," + end.toPersistString() + "]";
    }

    public static TimeRange parse(String timeRangePersistString) {
        String timeRangeNoBracket = timeRangePersistString.substring(1, timeRangePersistString.length() - 1);
        String[] pieces=timeRangeNoBracket.split(",");
        HhMm b = HhMm.parse(pieces[0]);
        HhMm e = HhMm.parse(pieces[1]);
        return new TimeRange(b, e);
    }
}
