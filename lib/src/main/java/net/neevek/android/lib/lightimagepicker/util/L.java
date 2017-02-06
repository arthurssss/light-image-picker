package net.neevek.android.lib.lightimagepicker.util;

import android.util.Log;

import net.neevek.android.lib.lightimagepicker.BuildConfig;

/**
 * Created with IntelliJ IDEA.
 * User: xiejm
 * Date: 7/25/13
 * Time: 6:32 PM
 */
public class L {
    private static void log(int type, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        String className = stackTrace.getClassName();
        String tag = className.substring(className.lastIndexOf('.') + 1) + "." + stackTrace.getMethodName() + "#" + stackTrace.getLineNumber();

        switch (type) {
            case Log.DEBUG:
                Log.d(tag, message);
                break;

            case Log.INFO:
                Log.i(tag, message);
                break;

            case Log.WARN:
                Log.w(tag, message);
                break;

            case Log.ERROR:
                Log.e(tag, message);
                break;

            case Log.VERBOSE:
                Log.v(tag, message);
                break;
        }
    }


    public static void d(String fmt, Object ... args) {
        if (BuildConfig.DEBUG) {
            if (args == null || args.length == 0) {
                log(Log.DEBUG, fmt);
            } else {
                log(Log.DEBUG, String.format(fmt, args));
            }
        }
    }

    public static void i(String fmt, Object ... args) {
        if (BuildConfig.DEBUG) {
            if (args == null || args.length == 0) {
                log(Log.INFO, fmt);
            } else {
                log(Log.INFO, String.format(fmt, args));
            }
        }
    }

    public static void w(String fmt, Object ... args) {
        if (BuildConfig.DEBUG) {
            if (args == null || args.length == 0) {
                log(Log.WARN, fmt);
            } else {
                log(Log.WARN, String.format(fmt, args));
            }
        }
    }

    public static void e(String fmt, Object ... args) {
        if (BuildConfig.DEBUG) {
            if (args == null || args.length == 0) {
                log(Log.ERROR, fmt);
            } else {
                log(Log.ERROR, String.format(fmt, args));
            }
        }
    }

    public static void v(String fmt, Object ... args) {
        if (BuildConfig.DEBUG) {
            if (args == null || args.length == 0) {
                log(Log.VERBOSE, fmt);
            } else {
                log(Log.VERBOSE, String.format(fmt, args));
            }
        }
    }
}

