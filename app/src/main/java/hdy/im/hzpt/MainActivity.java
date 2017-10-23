package hdy.im.hzpt;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button accountButton;
    private Button clientButton;
    private ToastUtils utils;
    private ShareUtils shareUtils;
    private Button flowButton;
    private Button timeButton;
    private Button mainButton;
    private boolean exit = false;
    private Button loginButton;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new ToastUtils(this);
        shareUtils = new ShareUtils(this);
        init();
    }

    private void requirePermission() {
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    0x1);
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, 2);
            }
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE}, 2);
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 2);
            }
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 2);
            }
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            }
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
            return;
        }
    }

    private void init() {
        flowButton = (Button) this.findViewById(R.id.flow_button);
        timeButton = (Button) this.findViewById(R.id.time_button);
        mainButton = (Button) this.findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);
        requirePermission();
        initData();
    }

    @Override
    protected void onResume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Object[] info = RequestUtils.getLoginInfo();
                System.out.println(info);
                Message message =
                        MainActivityHandler.obtainMessage();
                message.what = 0;
                message.obj = info;
                MainActivityHandler.sendMessage(message);
            }
        }).start();
        super.onResume();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (osVersion > 22) {
            return;
        }
        //判断网络是否连接为hzpt
        boolean wifi = isWifi();
        //当前不为wifi
        if (!wifi) {
            utils.toast("请先确定连接上了HZPT的wifi!");
        }
    }

    /**
     * 判断当前是否为wifi
     *
     * @return
     */
    private boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            //当前为wifi
            boolean wifiScan = false;
            //用来判断HZPT的wifi是否存在
            WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
            List<ScanResult> results = wifi_service.getScanResults();
            if (results.size() == 0) {
                //说明无法获取或数据太弱了.
                return true;
            }
            for (ScanResult scanResult : results) {
                Log.e("sadasd", ("\n设备名：" + scanResult.SSID + " 信号强度：" + scanResult.level + "/n :" + WifiManager.calculateSignalLevel(scanResult.level, 4)));
                if (scanResult.SSID.equals("HZPT")) {
                    //如果和hzpt相似的wifi.说明附近有hzpt的wifi
                    wifiScan = true;
                    break;
                }
            }
            //获取搜索的数据
            if (wifiScan) {
                //如果wifi存在.则判断当前的wifi信号是否为HZPT.如果当前不为HZPT的话则自动的进行连接
                WifiInfo info = wifi_service.getConnectionInfo();
                //获取当前的wifi信息.判断是否为hzpt信号
                String ssid = info.getSSID().replace("\"", "").trim();
                if (ssid.equals("HZPT")) {
                    return true;
                    //说明当前连接的是hzpt的wifi
                } else {
                    CreateWifiInfo("HZPT", null, 1);
                }
                return false;
            } else {
                return false;
            }
        }
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_button:
                oncreateAccountView();
                break;
            default:
                break;
        }
    }

    /**
     * 创建用户登录界面
     */
    private void oncreateAccountView() {
        requirePermission();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.connect_layout, null);
        final EditText account = (EditText) view.findViewById(R.id.editText);
        final EditText password = (EditText) view.findViewById(R.id.editText2);
        if (shareUtils.get("account") != null) {
            account.setText(shareUtils.get("account"));
        }
        if (shareUtils.get("password") != null) {
            password.setText(shareUtils.get("password"));
        }
        loginButton = (Button) view.findViewById(R.id.acoount_login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWifi()) {
                    utils.toast("请先连接HZPT的wifi!");
                    return;
                }
                if (account.getText() == null || account.getText().toString().equals("")) {
                    //说明为空
                    utils.toast("账户/手机号不能输入为空");
                    return;
                } else {
                    loginButton.setEnabled(false);
                    shareUtils.put("account", account.getText().toString());
                    shareUtils.put("password", password.getText().toString());
                    Pattern compile = Pattern.compile("^1[3|4|5|7|8][0-9]{9}$");
                    Matcher matcher = compile.matcher(account.getText().toString());
                    boolean b = matcher.find();
                    if (!b) {
                        if (password.getText() == null || password.getText().toString().equals("")) {
                            utils.toast("账户登录密码不能输入为空!");
                        }
                        //说明是账户登录
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean login = RequestUtils.login(account.getText().toString(), password.getText().toString());
                                Object[] info = RequestUtils.getLoginInfo();
                                Message message =
                                        MainActivityHandler.obtainMessage();
                                message.what = 0;
                                message.obj = info;
                                MainActivityHandler.sendMessage(message);
                            }
                        }).start();
                    } else {
                        //说明是手机登录
                        loginButton.setEnabled(false);
                        Intent intent = new Intent(MainActivity.this, ConnectorService.class);
                        utils.toast("正在等待接受验证码~请稍后.");
                        startService(intent);
                        Message message = new Message();
                        message.what = 1;
                        MainActivityHandler.sendMessageDelayed(message, 8000);
                    }

                }

            }
        });
        builder.setView(view);
        builder.create();
        dialog = builder.show();
    }

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


    public android.os.Handler MainActivityHandler = new android.os.Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case 0:
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                    }
                    Object[] objs = (Object[]) msg.obj;
                    if (objs == null) {
                        //说明登录失败
                        utils.toast("当前没有登录!请连接HZPT后点击一键登录!");
                        mainButton.setText("一键登录~");
                        mainButton.setOnClickListener(MainActivity.this);
                    } else {
                        utils.toast("您可以放心使用wifi了!");
                        flowButton.setText("已使用流量: " + objs[1] + "  MByte");
                        timeButton.setText("已使用时间: " + objs[0] + "  Min");
                        mainButton.setText("登录成功~");
                        mainButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                utils.toast("您已经登录成功了~如果需要修改请长按此按钮~");
                            }
                        });
                        mainButton.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                oncreateAccountView();
                                return false;
                            }
                        });
                    }
                    //代表页面刷新数据
                    break;
                case 1:
                    if (loginButton != null) {
                        loginButton.setEnabled(true);
                    }
                    //接受验证码成功或失败.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean login = RequestUtils.isLogin();
                            Message message = new Message();
                            message.what = 2;
                            if (login) {
                                message.obj = true;
                            } else {
                                Calendar cal = Calendar.getInstance();
                                int hour = cal.get(Calendar.HOUR);// 小时
                                if (hour >= 17 || hour < 6) {
                                    Message obtainMessage = MainActivity.this.MainActivityHandler.obtainMessage();
                                    obtainMessage.what = 3;
                                    obtainMessage.obj = "当前时间访客模式无法登陆~谢谢~账户登陆无限制~";
                                    MainActivity.this.MainActivityHandler.sendMessage(obtainMessage);
                                    return;
                                }
                                //再从数据库进行读取一次,以防万一
                                ContentResolver contentResolver = getContentResolver();
                                Uri uri = Uri.parse("content://sms/");
                                Cursor query = contentResolver.query(uri, new String[]{"address",
                                        "person", "body", "date", "type"}, null, null, null);
                                if (query != null && query.moveToFirst()) {
                                    String address = query.getString(0);
                                    String person = query.getString(1);
                                    String body = query.getString(2);
                                    String date = query.getString(3);
                                    String type = query.getString(4);
                                    if (body != null && body.contains("【杭电大】")) {
                                        //说明是验证码
                                        String code = body.substring(body.indexOf("为:") + 2).trim();
                                        boolean b = RequestUtils.login(shareUtils.get("account"), code);
                                        message.obj = b;
                                    }
                                } else {
                                    message.obj = false;
                                }
                            }
                            MainActivityHandler.sendMessage(message);
                        }
                    }).start();
                    break;
                case 2:
                    boolean flag = false;
                    try {
                        flag = (boolean) msg.obj;
                    } catch (Exception e) {
                        //如果检测到异常说明用户有异常操作问题
                        utils.toast(e.getMessage());
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    if (flag) {
                        utils.toast("登录成功~");
                        onResume();
                    } else {
                        String codeGet = shareUtils.get("code_get");
                        if ("true".equals(codeGet)) {
                            //说明验证码实际上得到的.但是无法登陆成功.
                            Message obtainMessage = MainActivity.this.MainActivityHandler.obtainMessage();
                            obtainMessage.what = 3;
                            obtainMessage.obj = "您的手机不支持自动输入验证码.请手动输入验证码~";
                            MainActivity.this.MainActivityHandler.sendMessage(obtainMessage);
                        } else {
                            utils.toast("登录失败~(是否给与权限?是否接收到验证码?是否已经超出上网时间?)");
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("如果接收到验证码~请输入");
                        final EditText text = new EditText(MainActivity.this);
                        builder.setView(text);
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (text.getText().toString().equals("")) {
                                    utils.toast("请输入验证码!");
                                }
                                text.setEnabled(false);
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
                                    @Override
                                    public Boolean call() throws Exception {
                                        boolean b = RequestUtils.login(shareUtils.get("account"), text.getText().toString());
                                        return b;
                                    }
                                });
                                utils.toast("尝试登录中..请稍后");
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (submit.isDone()) {
                                    try {
                                        if (submit.get()) {
                                            utils.toast("登录成功~");
                                            onResume();
                                        } else {
                                            utils.toast("登录失败~请稍后重试");
                                        }
                                    } catch (Exception e) {
                                        utils.toast(e.getMessage());
                                    }
                                }
                            }
                        });
                        builder.setNegativeButton("没有收到", null);
                        builder.show();
                    }
                    //用于接受验证码线程刷新
                    break;
                case 3:
                    String str = (String) msg.obj;
                    utils.toast(str);
                    break;
                default:
                    break;
            }
        }
    };


    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS 没有密码的情况
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP WEP加密的情况
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA WPA加密的情况
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    //这个方法的作用在于如果按照网上介绍的方法成功加入指定的wifi后，都会在终端的wifi列表中新添加一个以该指定ssid的网络，所以每运行一次程序，
    //列表中就会多一个相同名字的ssid。而该方法就是检查wifi列表中是否有以输入参数为名的wifi热点，如果存在，则在CreateWifiInfo方法开始
    //配置wifi网络之前将其移除，以避免ssid的重复：
    private WifiConfiguration IsExsits(String SSID) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
}
