package com.xml.library.utils;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xlc on 2016/12/29.
 */
public class SharedUtil {

    private static final String LIBRARY_XML = "library_xml";
    //缓存包时间
    public static final String CATCH_PKG_TIME = "catch_pkg_time";
    //进入admob广告时间
    public static final String IN_ADMOB_TIME = "in_admob_time";

    public static  final String CREATE_DEVICE_ID="create_device_id";

    public  static final String APP_MD5="app_md5";
    //开屏次数
    public static final String SCREEN_STATUS_COUNTS="screen_status_counts";
    //联网时间
    public static final String CONNECT_NET_TIME="connect_net_time";

    private static SharedUtil instance = null;

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private SharedPreferences sharedPreferences = null;

    private SharedPreferences.Editor editor = null;

    private Context mContext;

    public static SharedUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedUtil(context);
        }
        return instance;
    }

    private SharedUtil(Context context) {
        this.mContext = context;
        sharedPreferences = context.getSharedPreferences(LIBRARY_XML, 0);

    }
    public void save_int(String tag, int velues) {

        editor = sharedPreferences.edit();

        editor.putInt(tag, velues);

        editor.apply();
    }

    public void save_long(String tag, long velues) {

        editor = sharedPreferences.edit();

        editor.putLong(tag, velues);

        editor.apply();
    }

    public void save_string(String tag, String velues) {

        editor = sharedPreferences.edit();

        editor.putString(tag, velues);

        editor.apply();
    }

    public int get_int(String tag, int default_velues) {

        return sharedPreferences.getInt(tag, default_velues);
    }

    public long get_long(String tag, long default_velues) {

        return sharedPreferences.getLong(tag, default_velues);
    }

    public String get_string(String tag,String default_values) {

        return sharedPreferences.getString(tag, default_values);
    }


}
