package com.letinvr.playsdk;

import com.unity3d.player.UnityPlayer;

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
 * 2019/06/21 17:28 星期五
 **/
public class API {


    public static final String BIG_BUCK_BUNNY = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
    public static final String LT_PLAYER = "http://192.168.1.208/1.mp4";
    public static final String PLAY_LIST = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";
    public static final String CCTV_1 = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";
    public static final String CCTV_3 = "http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8";
    public static final String CCTV_5 = "http://ivi.bupt.edu.cn/hls/cctv5hd.m3u8";
    public static final String CCTV_5N = "http://ivi.bupt.edu.cn/hls/cctv5phd.m3u8";
    public static final String CCTV_6 = "http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8";
    public static final String PLAY_IOS = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
//    public static final String PLAY_LIST ="http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";
//    public static final String PLAY_LIST ="http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";

    public static final String PLAY_DETAILS = "com.lt.send_PLAY_DETAILS_MSG";
    public static final String PLAY_KEY = "playMsg";

    public static void senUnityMessage(String msg){
        UnityPlayer.UnitySendMessage("PlayTool","PlayeMessageCallback",msg);
    }





}
