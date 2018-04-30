package net.oldev.aljotit;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

public class LogToFile implements Closeable {

    public static final String TAG = "LJI-LogToFile";

    @NonNull
    private final Context mCtx;
    private FileWriter mLogWriter;

    /**
     * Requires WRITE_EXTERNAL_STORAGE permission be granted by caller.
     */
    public LogToFile(@NonNull Context ctx) {
        mCtx = ctx;
        initLogWriter();
    }

    public void i(@NonNull String tag, @NonNull String msg) {
        String level = "I";
        String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
                .format(Calendar.getInstance().getTime());

        String logLine = String.format("%s\t%s/%s  %s", timestamp, level, tag, msg);

        println(logLine);
    }

    private void println(@NonNull String logLine) {
        if (mLogWriter == null) {
            return;
        }

        try {
            Log.v(TAG, "[" + logLine + "]");
            // Ensure writing the line is atomic across
            // the entire application process
            synchronized (LogToFile.class) {
                mLogWriter.write(logLine);
                mLogWriter.write("\n");
                mLogWriter.flush();
            }
        } catch (Throwable t) {
            reportInternalError(t, "Error in writing to log file");
        }
    }

    @Override
    public void close() throws IOException {
        mLogWriter.close();
    }

    private void initLogWriter() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            boolean dirCreated = dir.mkdirs();
            File logFile = new File(dir, BuildConfig.APPLICATION_ID + ".log");
            boolean fileCreated = logFile.createNewFile();

            mLogWriter = new FileWriter(logFile, true); // mark writer as append
        } catch (Throwable t) {
            reportInternalError(t, "Error in creating / accessing log file");
        }
    }

    private void reportInternalError(Throwable t, String errMsg) {
        Log.e(TAG, errMsg, t);
        Toast.makeText(mCtx, String.format("Error: %s | %s: %s",
                errMsg, t.getClass().getSimpleName(),
                t.getMessage())
                , Toast.LENGTH_LONG);
    }
}
