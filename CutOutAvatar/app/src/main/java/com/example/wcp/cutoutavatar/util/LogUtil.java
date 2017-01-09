package com.example.wcp.cutoutavatar.util;

import android.util.Log;

import java.util.Locale;

public class LogUtil {
    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "uploadTopImg";
    public static boolean needSaveLog = false;  //保存刷卡每一步的时间

    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, msg);
    }

    public static void i(Class<?> _class, String msg) {
        if (isDebug)
            Log.i(_class.getName(), msg);
    }

    public static void d(Class<?> _class, String msg) {
        if (isDebug)
            Log.i(_class.getName(), msg);
    }

    public static void e(Class<?> _class, String msg) {
        if (isDebug)
            Log.i(_class.getName(), msg);
    }

    public static void v(Class<?> _class, String msg) {
        if (isDebug)
            Log.i(_class.getName(), msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void showlog(String msg) {
        if (isDebug) {
            if (msg == null) {
                Log.i(TAG, "null");
            } else {
                Log.i(TAG, buildMessage("%s", new String[]{msg}));
            }
        }
    }

    public static String buildMessage(String format, Object args[]) {
        String msg = args != null ? String.format(Locale.US, format, args)
                : format;
        StackTraceElement trace[] = (new Throwable()).fillInStackTrace()
                .getStackTrace();
        String caller = "<unknown>";
        for (int i = 2; i < trace.length; i++) {
            Class clazz = trace[i].getClass();
            if (clazz.equals(LogUtil.class))
                continue;
            String callingClass = trace[i].getClassName();

            callingClass = callingClass
                    .substring(callingClass.lastIndexOf('.') + 1);

            caller = (new StringBuilder(String.valueOf(callingClass)))
                    .append(".").append(trace[i].getMethodName()).toString();
            break;
        }

        return String.format(Locale.US, "[%d] %s: %s",
                new Object[]{Long.valueOf(Thread.currentThread().getId()),
                        caller, msg});
    }
}
