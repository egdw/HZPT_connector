package hdy.im.hzpt;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hdy on 2017/9/28.
 * 请求工具
 */
public class RequestUtils {

    /**
     * 判断当期是否已经登录
     */
    public static boolean isLogin() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://10.50.50.2/")
                .build();
        Call call = mOkHttpClient.newCall(request);
        String body = null;
        try {
            body = call.execute().body().string();
            int timeIndex = body.indexOf("time='") + 6;
            int flowIndex = body.indexOf("flow='") + 6;
            if (body == null || timeIndex == 5 || flowIndex == 5) {
                //说明没有登录
                return false;
            }
            //获取当前已使用时间
            String time = body.substring(timeIndex, timeIndex + body.substring(timeIndex).indexOf("'")).trim();
            //获取到使用的流量数
            String flow = body.substring(flowIndex, flowIndex + body.substring(flowIndex).indexOf("'")).trim();
            if (time == null || flow == null || time.equals("") || flow.equals("")) {
                return false;
            }
            //下面根据学校的算法进行仿照
            long flow0 = Long.valueOf(flow) % 1024;
            long flow1 = Long.valueOf(flow) - flow0;
            flow0 = flow0 * 1000;
            flow0 = flow0 - flow0 % 1024;
            String flow3 = ".";
            if (flow0 / 1024 < 10) {
                flow3 = ".00";
            } else {
                if (flow0 / 1024 < 100) {
                    flow3 = ".0";
                }
            }
            Log.e("已使用时间 Used time : ", time + " Min");
            Log.e("已使用流量 Used flux : ", flow1 / 1024 + flow3 + flow0 / 1024 + " MByte");
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 获取登录后的信息
     *
     * @return
     */
    public static Object[] getLoginInfo() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://10.50.50.2/")
                .build();
        Call call = mOkHttpClient.newCall(request);
        String body = null;
        try {
            body = call.execute().body().string();
            int timeIndex = body.indexOf("time='") + 6;
            int flowIndex = body.indexOf("flow='") + 6;
            if (body == null || timeIndex == 5 || flowIndex == 5) {
                //说明没有登录
                return null;
            }
            //获取当前已使用时间
            String time = body.substring(timeIndex, timeIndex + body.substring(timeIndex).indexOf("'")).trim();
            //获取到使用的流量数
            String flow = body.substring(flowIndex, flowIndex + body.substring(flowIndex).indexOf("'")).trim();
            if (time == null || flow == null || time.equals("") || flow.equals("")) {
                return null;
            }
            //下面根据学校的算法进行仿照
            long flow0 = Long.valueOf(flow) % 1024;
            long flow1 = Long.valueOf(flow) - flow0;
            flow0 = flow0 * 1000;
            flow0 = flow0 - flow0 % 1024;
            String flow3 = ".";
            if (flow0 / 1024 < 10) {
                flow3 = ".00";
            } else {
                if (flow0 / 1024 < 100) {
                    flow3 = ".0";
                }
            }
            Log.e("已使用时间 Used time ", time + " Min");
            Log.e("已使用流量 Used flux ", flow1 / 1024 + flow3 + flow0 / 1024 + " MByte");
            return new Object[]{time, flow1 / 1024 + flow3 + flow0 / 1024};
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 进行登陆的操作
     *
     * @param username
     * @param password
     */
    public static boolean login(String username, String password) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("DDDDD", username);
        builder.add("upass", password);
        builder.add("R1", "0");
        builder.add("R2", "");
        builder.add("R6", "0");
        builder.add("para", "00");
        builder.add("MKKey", "123456");
        FormBody formBody = builder.build();
        Request request = new Request.Builder()
                .url("http://10.50.50.2/a70.htm")
                .post(formBody)
                .build();
        Response response = null;
        try {
            response = mOkHttpClient.newCall(request).execute();
            String s = response.body().string();
            return isLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 登出的操作
     */
    public static boolean logout() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://10.50.50.2/F.htm")
                .build();
        Call call = mOkHttpClient.newCall(request);
        try {
            String body = call.execute().body().string();
            if (body.contains("注销成功")) {
                //说明注销成功了
                //其他的没用信息我就不获取了
                return true;
            } else {
                //说明注销存在问题
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 验证码请求
     */
    public static boolean getVerfiy(String phoneNumber, String mac) {
        String body = "";
        mac = mac.replace(":", "");
        OkHttpClient mOkHttpClient = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();

        builder.add("telephone", phoneNumber);
        builder.add("regURL", "172.18.180.128:8080");
        builder.add("machineno", "DR15NM8800380");
        builder.add("mac", mac);

        FormBody formBody = builder.build();

        Request request = new Request.Builder()
                .url("http://10.50.50.2:801/eportal/controller/Action.php")
                .post(formBody)
                .build();
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            String s = response.body().string();
            if (s == null || s.isEmpty()) {
                return false;
            }
            boolean contains = s.contains("true");
            if (contains) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
