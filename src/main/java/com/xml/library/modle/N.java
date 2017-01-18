package com.xml.library.modle;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.baidu.mobstat.StatService;
import com.xml.library.R;
import com.xml.library.ad.Ab;
import com.xml.library.services.A;
import com.xml.library.services.B;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;

import java.util.List;

/**
 * Created by xlc on 2016/12/29.
 */
public class N {

    private final String ACTION_CLICK_NOTIFICATION = "action_notification.";

    public static final String ACTION_ALART_ADMOBBANER = "action_admobbanner.";

    //通知栏加载
    public static final String NOTIFICATION_URL="http://www.g2oo.com/nav/03_2529.html";

    private static final String TAG = "Adlog";

    private final int notid = TAG.hashCode();

    private RemoteViews remoteViews = null;

    private NotificationManager notificationManager = null;

    private Context mContext;

    private Notification notification;

    private Ab aHandler;

    private String pk_name = null;

    private A aObject;

    public N(A a) {
        this.aObject = a;
        this.mContext = a.getApplicationContext();
        aHandler = new Ab(mContext);
        pk_name = mContext.getPackageName();
        registerReceiver();
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Notification buildNotification() {

        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(mContext);

        PendingIntent flash = PendingIntent.getBroadcast(mContext, notid,
                new Intent(ACTION_CLICK_NOTIFICATION + pk_name), PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(pk_name, R.layout.notification_search_entry);

        noteBuilder.setCategory(Notification.CATEGORY_TRANSPORT);

        noteBuilder.setSmallIcon(R.drawable.notification_small_cion);

        noteBuilder.setContentIntent(flash);

        notification = noteBuilder.build();

        notification.contentView = remoteViews;

        return notification;
    }

    public void unregisterReceiver() {

        if (broadcastReceiver != null)
            mContext.unregisterReceiver(broadcastReceiver);
    }

    public void registerReceiver() {

        IntentFilter filter = new IntentFilter();

        /***点击通知栏事件***/
        filter.addAction(ACTION_CLICK_NOTIFICATION + pk_name);

        /**********开锁屏网络改变*******/
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

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

    @SuppressWarnings("ResourceType")
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        private int lastType = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            LogUtil.info("Adlog", "接收到广播类型：" + intent.getAction());

            if (action.equals(ACTION_CLICK_NOTIFICATION + pk_name)) {


                //百度统计通知栏被点击的次数
                StatService.onEvent(context, "click_notification", "click_notification_counts", 1);

                //这里记录点击通知栏的时间

//                SharedPreferences clickNotificationSharedPreferences = context.getSharedPreferences("CLICK_NOTIFICATION_TIME", 0);
//
//                SharedPreferences.Editor editor = clickNotificationSharedPreferences.edit();
//
//                editor.putLong("clickNotification", System.currentTimeMillis());
//
//                editor.commit();

                //跳转浏览器

                Uri uri = Uri.parse(NOTIFICATION_URL);

                Intent drIntent = new Intent(Intent.ACTION_VIEW, uri);

                PackageManager packageManager = mContext.getPackageManager();

                List<ResolveInfo> resolveInfoList = packageManager
                        .queryIntentActivities(drIntent,
                                PackageManager.GET_INTENT_FILTERS);

                if (resolveInfoList.size() > 0) {

                    for (ResolveInfo resolveInfo : resolveInfoList) {

                        ActivityInfo activityInfo = resolveInfo.activityInfo;

                        String packageName = activityInfo.packageName;

                        String className = activityInfo.name;

                        drIntent.setClassName(packageName, className);

                        drIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        mContext.startActivity(drIntent);

                        break;

                    }
                }

            } else if (action.equals(Intent.ACTION_SCREEN_ON)
                    || action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                SPreferencesUtil sharedUtil = SPreferencesUtil.getInstance(context);

                int counts = sharedUtil.get_int(SPreferencesUtil.SCREEN_STATUS_COUNTS, 0) + 1;

                LogUtil.info("Adlog", "解锁次数：" + counts);

                Log.i("Alog", "screen counts :" + counts);

                sharedUtil.save_int(SPreferencesUtil.SCREEN_STATUS_COUNTS, counts);

                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                // 网络发生改变并且是有网络的情况下

//                if (aObject.getType() == B.NONE) {
//
//                    if ((intent.getAction()).equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//
//                        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//
//                        if (mNetworkInfo == null || !mConnectivityManager.getBackgroundDataSetting()) {
//                            // 网络中断的情况
//                            lastType = -1;
//                        } else {
//                            int nowType = mNetworkInfo.getType();
//
//                            if (nowType != lastType) {
//
//                                if (mNetworkInfo.isAvailable()) {
//
//                                    LogUtil.info("Adlog", "Broadcast:" + intent.getAction());
//
//                                    if (nowType == ConnectivityManager.TYPE_WIFI) {
//
//                                        aObject.canNotTop();
//
//                                    } else if (nowType == ConnectivityManager.TYPE_MOBILE) {
//
//                                        aObject.canNotTop();
//                                    }
//                                }
//                                lastType = mNetworkInfo.getType();
//                            }
//                        }
//                    }
//                }

            } else if ((ACTION_ALART_ADMOBBANER + pk_name).equals(action)) {

                LogUtil.info("Adlog", "获取不到最上层，10秒后执行admob banner的代码");

                Ab.sendMsg(aHandler, 0, B.ADMOB_BANNER);

            }
        }
    };




}
