package com.letinvr.playsdk.util;


//
//                    .::::.
//                  .::::::::.
//                 :::::::::::  FUCK YOU
//             ..:::::::::::'
//           '::::::::::::'
//             .::::::::::
//        '::::::::::::::..
//             ..::::::::::::.
//           ``::::::::::::::::
//            ::::``:::::::::'        .:::.
//           ::::'   ':::::'       .::::::::.
//         .::::'      ::::     .:::::::'::::.
//        .:::'       :::::  .:::::::::' ':::::.
//       .::'        :::::.:::::::::'      ':::::.
//      .::'         ::::::::::::::'         ``::::.
//  ...:::           ::::::::::::'              ``::.
// ```` ':.          ':::::::::'                  ::::..
//                    '.:::::'                    ':'````..

import android.os.Environment;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * You may think you know what the following code does.
 * But you dont. Trust me.
 * Fiddle with it, and youll spend many a sleepless
 * night cursing the moment you thought youd be clever
 * enough to "optimize" the code below.
 * Now close this file and go play with something else.
 * <p>
 * 2018/11/30 13:57
 **/

public class LogToFile {


    static String className;//类名
    static String methodName;//方法名
    static int lineNumber;//行数
    private static final String TAG = "||";
    /**
     * 判断是否可以调试
     *
     * @return
     */
    public static boolean isDebuggable() {

        if (getTextContent("logcontrol.txt").equals("true")) {
            return true;
        } else {
            return false;
        }
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
        Log.e("||", createLog(message));
    }

    public static void i(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.i("||", createLog(message));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.d("||", createLog(message));
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.v("||", createLog(message));
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.w("||", createLog(message));
    }

    public static String getTextContent(String txtName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(UnityPlayer.currentActivity.getResources().getAssets().open(txtName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            Log.e("Srz_error ", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
