package net.oldev.aljotit;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;


/**
 * Generic class to represent a time range [begin,end] preference
 *
 * Adapted from:
 *   https://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen
 */
public class TimeRangePreference extends DialogPreference {
    private TimeRange lastTimeRange = null;

    private TimePicker picker=null;
    private TimePicker pickerEnd=null;

    private final @LayoutRes int pickerLayoutId;
    private final @IdRes int pickerBeginId;
    private final @IdRes int pickerEndId;

    public TimeRangePreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        // PENDING: parametrize the IDs rather than hardcoding it.
        pickerLayoutId = R.layout.timerange_picker_fragment;
        pickerBeginId = R.id.timePickerBegin;
        pickerEndId = R.id.timePickerEnd;

        setDialogLayoutResource(pickerLayoutId);
    }

    /* /// replaced by setDialogLayoutResource() in constructor.
    @Override
    protected View onCreateDialogView() {
        picker=new TimePicker(getContext()); /// just to avoid null pointer
        return(picker);

    }
    */


    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        // LinearLayout hack to approximate auto orientation
        int orientToSet = ( (v.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) ?
                LinearLayout.HORIZONTAL : LinearLayout.VERTICAL );
        ((LinearLayout)v).setOrientation(orientToSet);

        picker = v.findViewById(pickerBeginId);
        pickerEnd = v.findViewById(pickerEndId);

        if (lastTimeRange != null) {
            picker.setCurrentHour(lastTimeRange.begin.hh);
            picker.setCurrentMinute(lastTimeRange.begin.mm);

            pickerEnd.setCurrentHour(lastTimeRange.end.hh);
            pickerEnd.setCurrentMinute(lastTimeRange.end.mm);
        }
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            TimeRange range = timePickersToTimeRange();
            String rangeStr = range.toPersistString();
            if (callChangeListener(rangeStr)) {
                persistString(rangeStr);
                lastTimeRange = range;
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String rangeStr;

        if (restoreValue) {
            if (defaultValue==null) {
                rangeStr=getPersistedString("[00:00,00:00]");
            }
            else {
                rangeStr=getPersistedString(defaultValue.toString());
            }
        }
        else {
            rangeStr=defaultValue.toString();
        }

        lastTimeRange = TimeRange.parse(rangeStr);
    }

    @NonNull
    private TimeRange timePickersToTimeRange() {
        TimeRange.HhMm begin = new TimeRange.HhMm(picker.getCurrentHour(), picker.getCurrentMinute());
        TimeRange.HhMm end  = new TimeRange.HhMm(pickerEnd.getCurrentHour(), pickerEnd.getCurrentMinute());
        return new TimeRange(begin, end);
    }
}

