package com.xml.library.modle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.baidu.mobstat.StatService;
import com.xml.library.R;
import com.xml.library.ad.Ab;
import com.xml.library.clean.ShortCutActivity;
import com.xml.library.services.A;
import com.xml.library.services.B;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.NotUtil;
import com.xml.library.utils.SharedUtil;

/**
 * Created by xlc on 2016/12/29.
 */
public class N {

    public static final String ACTION_RELEASE_FLASH = "action.release_flash.";

    public static final String ACTION_FLASH = "action.flash.";

    public static final String ACTION_SCREEN_LIGHT = "action.screen_light.";

    public static final String ACTION_VOLUME = "action.volume.";

    public static final String ACTION_WIFI = "action.wifi.";

    public static final String ACTION_MOBLILE = "action_mobile.";

    public static final String ACTION_ALART_ADMOBBANER = "action_admobbanner.";

    private RemoteViews remoteViews = null;

    private NotificationManager notificationManager = null;

    private Context mContext;

    private Notification notification;

    private int notid;

    private Ab aHandler;

    private String pk_name = null;

    private boolean flash_status = false;

    private DObserver dataObserver;

    private LObserver lObserver;

    private A aObject;

    public N(A a) {
        this.aObject = a;
        this.mContext = a.getApplicationContext();
        aHandler = new Ab(mContext);
        pk_name = mContext.getPackageName();
        registered_ContentObserver();
        registerReceiver();
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void setNotificationResource(int id, int res_id) {
        if (remoteViews == null) return;
        remoteViews.setImageViewResource(id, res_id);
    }

    public void notifyNotification() {
        notificationManager.notify(notid, notification);
    }

    public Notification buildNotification(int notid, String pk_name) {

        this.notid = notid;

        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(mContext);

        PendingIntent flash = PendingIntent.getBroadcast(mContext, notid,
                new Intent(ACTION_FLASH + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent wifi = PendingIntent.getBroadcast(mContext, notid,
                new Intent(ACTION_WIFI + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent screen_light = PendingIntent.getBroadcast(mContext, notid,
                new Intent(ACTION_SCREEN_LIGHT + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);

        /*******跳转网络设置********/
        PendingIntent moblie = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
            ComponentName clean = new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
            intent.setComponent(clean);
            moblie = PendingIntent.getActivity(mContext, notid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            moblie = PendingIntent.getBroadcast(mContext, notid, new Intent(ACTION_MOBLILE + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent intent = new Intent(mContext, ShortCutActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent clean = PendingIntent.getActivity(mContext, notid,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent volume = PendingIntent.getBroadcast(mContext, notid,
                new Intent(ACTION_VOLUME + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(pk_name, R.layout.tool_notification_layout);

        if (NotUtil.getInstance(mContext).isWifi_status()) {

            setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_open);

        } else {
            setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_close_black);
        }
        if (NotUtil.getInstance(mContext).isMoblie_status()) {
            setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_open);
        } else {
            setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_close_black);
        }
        int ringerMode = NotUtil.getInstance(mContext).getVolumeType();

        set_ringerMode(ringerMode);

        set_screen_brightness();

        remoteViews.setImageViewResource(R.id.notification_flash, R.drawable.notify_child_flash_closed);

        remoteViews.setOnClickPendingIntent(R.id.notification_volume, volume);

        remoteViews.setOnClickPendingIntent(R.id.notification_flash, flash);

        remoteViews.setOnClickPendingIntent(R.id.notification_wifi, wifi);

        remoteViews.setOnClickPendingIntent(R.id.notification_light, screen_light);

        remoteViews.setOnClickPendingIntent(R.id.notification_mobile, moblie);

        remoteViews.setOnClickPendingIntent(R.id.notification_clean, clean);

        noteBuilder.setCategory(Notification.CATEGORY_TRANSPORT);

        noteBuilder.setSmallIcon(R.drawable.notify_child_flash_closed);

        notification = noteBuilder.build();

        notification.contentView = remoteViews;

        return notification;
    }

    public void set_screen_brightness() {
        int screen_brightness = NotUtil.getInstance(mContext).init_light();
        switch (screen_brightness) {
            case 0:
                setNotificationResource(R.id.notification_light, R.drawable.notify_child_light_auto);
                break;
            case 1:
                setNotificationResource(R.id.notification_light, R.drawable.notify_child_light_25);
                break;
            case 2:
                setNotificationResource(R.id.notification_light, R.drawable.notify_child_light_50);
                break;
            case 3:
                setNotificationResource(R.id.notification_light, R.drawable.notify_child_light_75);
                break;
            case 4:
                setNotificationResource(R.id.notification_light, R.drawable.notify_child_light_100);
                break;
        }
    }

    public void set_ringerMode(int ringerMode) {
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_NORMAL:
                setNotificationResource(R.id.notification_volume, R.drawable.notify_child_ringer_status2);
                //normal
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                setNotificationResource(R.id.notification_volume, R.drawable.notify_child_ringer_status3);
                //vibrate
                break;
            case AudioManager.RINGER_MODE_SILENT:
                setNotificationResource(R.id.notification_volume, R.drawable.notify_child_ringer_status4_black);
                //silent
                break;
        }
    }

    public void unregisterReceiver() {

        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
    }

    public void registerReceiver() {

        IntentFilter filter = new IntentFilter();
        /***点击通知栏事件***/
        filter.addAction(ACTION_FLASH + mContext.getPackageName());//手电
        filter.addAction(ACTION_WIFI + pk_name);//wifi
        filter.addAction(ACTION_MOBLILE + pk_name);//gprs
        filter.addAction(ACTION_SCREEN_LIGHT + pk_name);//亮度调节
        filter.addAction(ACTION_VOLUME + pk_name);//声音模式切换
        filter.addAction(ACTION_RELEASE_FLASH + pk_name);//释放flash
        /**********开锁屏网络改变*******/
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//wifi开关
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);//情景模式
        /**安装广播**/
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Intent.ACTION_PACKAGE_ADDED);//
        filter1.addDataScheme("package");
        /****闹钟定时*****/
        filter.addAction(ACTION_ALART_ADMOBBANER + pk_name);// ad banner
        filter.setPriority(Integer.MAX_VALUE);
        mContext.registerReceiver(broadcastReceiver, filter);
        mContext.registerReceiver(broadcastReceiver, filter1);
    }

    private void baidu_statistical_notification(String tag) {
        StatService.onEvent(mContext, "notification", tag, 1);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        private int lastType = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            LogUtil.info("Adlog", "接收到广播类型：" + intent.getAction());

            if (action.contains(pk_name)) {

                if (notification == null) return;

                if (action.equals(N.ACTION_RELEASE_FLASH + pk_name)) {
                    setNotificationResource(R.id.notification_flash, R.drawable.notify_child_flash_closed);
                    flash_status = false;
                    NotUtil.getInstance(context).close_flash();
                    notifyNotification();
                } else if (action.equals(N.ACTION_FLASH + pk_name)) {
                    baidu_statistical_notification("flash_open_or_close");
                    if (flash_status) {
                        flash_status = false;
                        NotUtil.getInstance(context).close_flash();
                        setNotificationResource(R.id.notification_flash, R.drawable.notify_child_flash_closed);
                    } else {
                        if (NotUtil.getInstance(context).openLight()) {
                            flash_status = true;
                            setNotificationResource(R.id.notification_flash, R.drawable.notify_child_flash_open);
                        }
                    }
                    notifyNotification();

                } else if (action.equals(N.ACTION_SCREEN_LIGHT + pk_name)) {
                    baidu_statistical_notification("screen_light_Adjust");
                    NotUtil.getInstance(context).setScreenBritness();
                    set_screen_brightness();
                    notifyNotification();

                } else if (action.equals(N.ACTION_VOLUME + pk_name)) {

                    int volueType = NotUtil.getInstance(context).setVoluneType();

                    baidu_statistical_notification("mobile_ringerMode");
                    set_ringerMode(volueType);
                    notifyNotification();

                } else if (action.equals(N.ACTION_MOBLILE + pk_name)) {

                    baidu_statistical_notification("gprs_open_or_close");

                    if (NotUtil.getInstance(context).isMoblie_status()) {
                        NotUtil.getInstance(context).setMoblie_status(false, true);
                        setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_close_black);
                    } else {
                        NotUtil.getInstance(context).setMoblie_status(true, true);
                        setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_open);
                    }
                    notifyNotification();

                } else if (action.equals(N.ACTION_WIFI + pk_name)) {

                    baidu_statistical_notification("wifi_open_or_close");

                    if (NotUtil.getInstance(context).isWifi_status()) {
                        NotUtil.getInstance(context).setWifi_status(false, true);
                        setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_close_black);
                    } else {
                        NotUtil.getInstance(context).setWifi_status(true, true);
                        setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_open);
                    }
                    notifyNotification();
                }
            } else if (action.equals(Intent.ACTION_SCREEN_ON)
                    || action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                SharedUtil sharedUtil = SharedUtil.getInstance(context);

                int counts = sharedUtil.get_int(SharedUtil.SCREEN_STATUS_COUNTS, 0) + 1;

                LogUtil.info("Adlog", "解锁次数：" + counts);

                Log.i("Alog", "screen counts :" + counts);

                sharedUtil.save_int(SharedUtil.SCREEN_STATUS_COUNTS, counts);

                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                // 网络发生改变并且是有网络的情况下

                if (aObject.getType() == B.NONE) {

                    if ((intent.getAction()).equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                        if (mNetworkInfo == null || !mConnectivityManager.getBackgroundDataSetting()) {
                            // 网络中断的情况
                            lastType = -1;
                        } else {
                            int nowType = mNetworkInfo.getType();

                            if (nowType != lastType) {

                                if (mNetworkInfo.isAvailable()) {

                                    LogUtil.info("Adlog", "Broadcast:" + intent.getAction());

                                    if (nowType == ConnectivityManager.TYPE_WIFI) {

                                        aObject.canNotTop();

                                    } else if (nowType == ConnectivityManager.TYPE_MOBILE) {

                                        aObject.canNotTop();
                                    }
                                }
                                lastType = mNetworkInfo.getType();
                            }
                        }
                    }
                }

            } else if ((ACTION_ALART_ADMOBBANER + pk_name).equals(action)) {

                LogUtil.info("Adlog", "获取不到最上层，10秒后执行admob banner的代码");

                Ab.sendMsg(aHandler, 0, B.ADMOB_BANNER);

            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                if (notification == null) return;
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    LogUtil.info("Adlog", "wifi isConnected " + isConnected);
                    if (isConnected) {
                        NotUtil.getInstance(context).setWifi_status(true, false);
                        setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_open);
                    } else {
                        NotUtil.getInstance(context).setWifi_status(false, false);
                        setNotificationResource(R.id.notification_wifi, R.drawable.notify_child_wifi_close_black);
                    }
                    notifyNotification();
                }
            } else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                if (notification == null) return;
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                final int ringerMode = am.getRingerMode();
                set_ringerMode(ringerMode);
                NotUtil.getInstance(context).setVolumeType(ringerMode);
                notifyNotification();
            }

        }
    };

    public void dObserverChange() {
        if (notification == null) return;
        if (NotUtil.getInstance(mContext).getMobileDataState(null)) {
            NotUtil.getInstance(mContext).setMoblie_status(true, false);
            setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_open);
        } else {
            NotUtil.getInstance(mContext).setMoblie_status(false, false);
            setNotificationResource(R.id.notification_mobile, R.drawable.notify_child_gprs_close_black);
        }
        notifyNotification();
    }
    public void lObserverChange() {
        if (notification == null) return;
        set_screen_brightness();
        notifyNotification();
    }
    private void registered_ContentObserver() {
        dataObserver = new DObserver(this,
                new Handler());
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor("mobile_data"), false, dataObserver);
        lObserver = new LObserver(this, new Handler());
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true, lObserver);
    }

    public void unRegisteredContentObserver() {
        if (dataObserver != null)
            mContext.getContentResolver().unregisterContentObserver(dataObserver);
        if (lObserver != null)
            mContext.getContentResolver().unregisterContentObserver(lObserver);
    }

}
