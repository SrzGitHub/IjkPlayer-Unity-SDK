package com.letinvr.play.base;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.letinvr.play.AndroidAPI;
import com.letinvr.play.R;
import com.letinvr.play.network.NetWorkChangReceiver;

import butterknife.ButterKnife;


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

/***********************************************************
 *                                                         *
 * You may think you know what the following code does.    *
 * But you dont. Trust me.                                 *
 * Fiddle with it, and youll spend many a sleepless        *
 * night cursing the moment you thought youd be clever     *
 * enough to "optimize" the code below.                    *
 * Now close this file and go play with something else.    *
 *                                                         *
 ***********************************************************/

public abstract class BaseActivity extends AppCompatActivity {
    private AlertDialog alerDialog;
    private NetWorkChangReceiver netWorkChangReceiver;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AndroidAPI.ACTION_LETIN_WIFI_OFF.equals(action)) {
                showUpdateDialog();


            } else if (AndroidAPI.ACTION_LETIN_WIFI_NO.equals(action)) {
                if (alerDialog!=null)
                alerDialog.dismiss();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            StatusBarUtil.setStatusBarColor(this, R.color.transparent);
        }

        getReceWindow();//透明白色字体
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        initView();
        iniBroadcast();


    }


    private void iniBroadcast() {
        netWorkChangReceiver = new NetWorkChangReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter1.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter1.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(AndroidAPI.ACTION_LETIN_WIFI_OFF);
        filter.addAction(AndroidAPI.ACTION_LETIN_WIFI_NO);
        registerReceiver(broadcastReceiver, filter);
    }

    public void showUpdateDialog() {
        alerDialog = new AlertDialog.Builder(this).create();
        alerDialog.setCancelable(false);
        alerDialog.setTitle("友情提示");
        alerDialog.setMessage("\n\t\t\t网络已断开，请确认网络后连接重试！！！\n\t\t\t提示框会在连接网络后退出");
        alerDialog.setIcon(R.drawable.img_qrcode);
        alerDialog.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alerDialog.show();

    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    private void getReceWindow() {
        //实现状态栏图标和文字颜色为浅色
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, 0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkChangReceiver);
        unregisterReceiver(broadcastReceiver);
    }
}
