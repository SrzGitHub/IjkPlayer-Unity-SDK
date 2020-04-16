package com.letinvr.playsdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

/**
 * //                    .::::.
 * //                  .::::::::.
 * //                 :::::::::::  FUCK YOU
 * //             ..:::::::::::'
 * //           '::::::::::::'
 * //             .::::::::::
 * //        '::::::::::::::..
 * //             ..::::::::::::.
 * //           ``::::::::::::::::
 * //            ::::``:::::::::'        .:::.
 * //           ::::'   ':::::'       .::::::::.
 * //         .::::'      ::::     .:::::::'::::.
 * //        .:::'       :::::  .:::::::::' ':::::.
 * //       .::'        :::::.:::::::::'      ':::::.
 * //      .::'         ::::::::::::::'         ``::::.
 * //  ...:::           ::::::::::::'              ``::.
 * // ```` ':.          ':::::::::'                  ::::..
 * //                    '.:::::'                    ':'````..
 * ===========================================================
 * You may think you know what the following code does.
 * But you dont. Trust me.
 * Fiddle with it, and youll spend many a sleepless
 * night cursing the moment you thought youd be clever
 * enough to "optimize" the code below.
 * Now close this file and go play with something else.
 * <p>
 * 2019/07/01 15:32 星期一
 **/
public class PowerManagerUtil {


    private static PowerManager pm;
    private static PowerManager.WakeLock wl;

    @SuppressLint("InvalidWakeLockTag")
    public static   void initPowerManager(Context context){

        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
    }

    public static void theScreenIsAlwaysOn(){
        if (pm==null&&wl==null){
            return;
        }
        wl.acquire();
    }
    public static void release(){
        if (pm==null||wl==null){
            return;
        }
        wl.release();
    }
}
