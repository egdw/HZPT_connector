package hdy.im.hzpt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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

import java.util.List;
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
        if (osVersion > 22) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                        0x1);
            }
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
        if (wifi) {
            //当前为wifi
            boolean wifiScan = false;
            //用来判断HZPT的wifi是否存在
            WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
            List<ScanResult> results = wifi_service.getScanResults();
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
                String ssid = info.getSSID();
                if (ssid != null && ssid.equals("HZPT")) {
                    //说明当前连接的是hzpt的wifi
                    //主页刷新
                    utils.toast("您已经连接上了HZPT的wifi!请点击一键连接进行登陆!");
                } else {
                    //说明当前不是hzpt的wifi
                    //进行程序的自动连接
//                    List<WifiConfiguration> networks = wifi_service.getConfiguredNetworks();
                    //暂时...先不写
                    utils.toast("请先连接HZPT的wifi!");
                }
            } else {
                utils.toast("当前没有找到HZPT的wifi热点信号!");
            }
        } else {
            //当前不为wifi
            utils.toast("请先打开wifi!");
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
            return true;
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
        System.out.println("account:" + account.getText().toString());
        System.out.println("password:" + password.getText().toString());
        loginButton = (Button) view.findViewById(R.id.acoount_login_btn);
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (osVersion > 22) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_SMS}, 2);
                }
            }
        }
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
                                System.out.println("当前账号:" + account.getText().toString());
                                System.out.println("当前密码:" + password.getText().toString());
                                boolean login = RequestUtils.login(account.getText().toString(), password.getText().toString());
                                System.out.println("是否登录:" + login);
                                Object[] info = RequestUtils.getLoginInfo();
                                System.out.println(info);
                                Message message =
                                        MainActivityHandler.obtainMessage();
                                message.what = 0;
                                message.obj = info;
                                MainActivityHandler.sendMessage(message);
                            }
                        }).start();
                    } else {
                        //说明是手机登录
                        Intent intent = new Intent(MainActivity.this, ConnectorService.class);
                        utils.toast("正在等待接受验证码~请稍后.");
                        startService(intent);
                        loginButton.setEnabled(false);
                        Message message = new Message();
                        message.what = 1;
                        MainActivityHandler.sendMessageDelayed(message, 7000);
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
                    loginButton.setEnabled(true);
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
                                message.obj = false;
                            }
                            MainActivityHandler.sendMessage(message);
                        }
                    }).start();
                    break;
                case 2:
                    boolean flag = (boolean) msg.obj;
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    if (flag) {
                        utils.toast("登录成功~");
                        onResume();
                    } else {
                        utils.toast("登录失败~(是否给与权限?是否接收到验证码?是否已经超出上网时间?)");
                    }
                    //用于接受验证码线程刷新
                    break;
                case 3:

                    break;
                default:
                    break;
            }
        }
    };

}
