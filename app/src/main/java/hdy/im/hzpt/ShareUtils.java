package hdy.im.hzpt;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hdy on 2017/9/28.
 */

public class ShareUtils {
    private Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor edit;

    public ShareUtils(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("data", Context.MODE_MULTI_PROCESS);
        edit = preferences.edit();
    }

    public boolean put(String key, String value) {
        boolean commit = edit.putString(key, value).commit();
        return commit == true ? true : false;
    }

    public String get(String key) {
        return preferences.getString(key, null);
    }
}
