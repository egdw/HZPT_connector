package hdy.im.hzpt;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Created by hdy on 2017/9/29.
 */

public class ConnectorService extends Service {
    private TelephonyManager manager;
    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private TimerTask task;
    private final Timer timer = new Timer();
    private ShareUtils utils;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        utils = new ShareUtils(this);
        openSmsReciver();
//        startTimer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean verfiy = RequestUtils.getVerfiy(utils.get("account"), getLocalMacAddressFromWifiInfo());
                Log.e("verfiy", verfiy + "");
            }
        }).start();

    }

//    /**
//     * 开启定时任务
//     */
//    public void startTimer() {
//        task = new TimerTask() {
//            @Override
//            public void run() {
//                handler.sendEmptyMessage(0x1);
//            }
//
//        };
//        //五分钟自动上传一次
//        timer.schedule(task, 2000, 1000 * 20);
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 打开短信监听器
     */
    private void openSmsReciver() {
        Uri uri = Uri.parse("content://sms");
        getContentResolver().registerContentObserver(uri, true,
                new MyObserver(new Handler()));
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFirst = false;

    private class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ContentResolver cr = getContentResolver();
            //定义一个接收短信的集合
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uri = Uri.parse(String.valueOf(Telephony.Sms.CONTENT_URI));
            }
            Cursor cursor = cr.query(uri, null, null, null, null);
            boolean b = cursor.moveToNext();
            if (b) {
                String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                Long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                String person = cursor.getString(cursor.getColumnIndex(Telephony.Sms.PERSON));
                int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                if (body != null && body.contains("【杭电大】")) {
                    //说明是验证码
                    final String code = body.substring(body.indexOf("为:") + 2).trim();
                    isFirst = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean login = RequestUtils.login(utils.get("account"), code);
                            //这里还要加一个判断就是.
                            //如果用户是在否访客模式时间登陆的
                            //这个时候获取到验证码但是肯定是无法登陆的.这里要另外加一个判断.
                            System.out.println("获取到的验证码:" + code);
                            if (login) {
                                utils.put("isClient", "true");
                                utils.put("success", "true");
                                utils.put("code_get", "false");
                            } else {
                                if (code != null && code.length() == 6) {
                                    //说明接收到了验证码.但是无法登陆
                                    utils.put("code_get", "true");
                                } else {
                                    utils.put("code_get", "false");
                                }
                                utils.put("success", "");
                            }
                            ConnectorService.this.stopSelf();
                        }
                    }).start();

                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

        }
    };

    /**
     * 获取当前的mac地址
     *
     * @return
     */
    public String getLocalMacAddressFromWifiInfo() {
        WifiManager wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
}
