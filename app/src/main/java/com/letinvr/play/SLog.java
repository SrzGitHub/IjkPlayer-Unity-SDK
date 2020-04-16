package com.letinvr.play;

import android.util.Log;


public class SLog {


    //Environment.getExternalStorageDirectory().getAbsolutePath()
    static String className;//类名
    static String methodName;//方法名
    static int lineNumber;//行数
    private static final String TAG = "LogToFile";

    /**
     * 判断是否可以调试
     *
     * @return
     */
    public static boolean isDebuggable() {
        return true;
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Methods：");
        buffer.append(methodName);
        buffer.append(" (").append(className).append(":").append(lineNumber).append(") Msg: ");
        buffer.append(log);
        return buffer.toString();
    }

    /**
     * 获取文件名、方法名、所在行数
     *
     * @param sElements
     */
    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }

    public static void i(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.v(className, createLog(message));
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.w(className, createLog(message));
    }


}