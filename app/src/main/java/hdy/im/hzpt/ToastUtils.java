package hdy.im.hzpt;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by hdy on 2017/9/28.
 */

public class ToastUtils {

    private Context context;
    private final Toast toast;

    public ToastUtils(Context context) {
        this.context = context;
        toast = new Toast(context);
        toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public void toast(String message) {
        toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void longToast(String message) {
        toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
