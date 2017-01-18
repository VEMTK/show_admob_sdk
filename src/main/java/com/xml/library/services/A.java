package com.xml.library.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.xml.library.ad.Ab;
import com.xml.library.ad.Ad;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.N;
import com.xml.library.modle.T;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.OkHttpTool;
import com.xml.library.utils.RUtil;
import com.xml.library.utils.SharedUtil;
import com.xml.library.utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xlc on 2016/12/29.
 */
public class A extends Service {

    private static final String TAG = "Adlog";

    private final int notid = TAG.hashCode();

    public int getType() {
        return type;
    }

    private int type = 0;

    private N n = null;

    private B b = null;

    private Ab abHander = null;

    private Notification notification;

    private String pk_name = null;

    private boolean flash_status = false;

    private SharedUtil sharedUtil = null;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = null;

    private int[] weightArrays, admobArrays;

    private boolean showNotification = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.create_device_id(getApplicationContext());

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);

        n = new N(this);

        b = new B(this);
//
        abHander = new Ab(this);

        pk_name = this.getPackageName();

        sharedUtil = SharedUtil.getInstance(this);

        showNotification();

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new MyRunnable(), 0, 1000, TimeUnit.MILLISECONDS);


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//
        F f = new F(this, type);

        f.executeOnExecutor(OkHttpTool.executorService);

        init_proportion();

        canNotTop();

        check_notification_by_blacklist();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (n != null) {
            n.unregisterReceiver();
            n.unRegisteredContentObserver();
        }
    }

    /**
     * 获取不到最上层 banner的定时显示
     **/
    private void setAlarmTime(Context context, String str_type, long timeInMillis) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(str_type);

        PendingIntent sender = PendingIntent.getBroadcast(

                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, sender);

    }

    public void canNotTop() {

        if (type != B.NONE) return;

        showAd(B.ADMOB, 0);
    }

    class MyRunnable implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (b != null && weightArrays != null) {

                type = b.onStartCommand();

                LogUtil.info("top", "type:" + type);

                if (type != B.NONE) {

                    showAd(type, 1);
                }
            }
        }
    }

    /***
     * 显示广告 0:banner 1:SCREEN 2:ADMOB大廣告平台
     *
     * @param type
     */
    private void showAd(final int type, final int banner) {

        OkHttpTool.executorService.execute(new Runnable() {
            @Override
            public void run() {
                LogUtil.info("top", "显示广告操作");
                if (Utils.check_black_list(getApplicationContext()) == -1) {
                    if (type != B.CLEAR && type != B.SAME_CN) {
                        LogUtil.info("Adlog", "黑名单或网络异常不展示广告");
                        Log.i("Alog", "is blacklist");
                    }
                    return;
                }
                switch (type) {

                    case B.ADMOB:

                        if (!check_ad_conditions()) break;

                        if (RUtil.get_proportion(admobArrays) == 0) {
                            Log.i("Alog", "in AdScr");
                            Ad.startAdmobActivity(getApplicationContext());
                        } else {
                            Log.i("Alog", "in Ad banner");
                            if (banner == 0)
                                setAlarmTime(getApplicationContext(), N.ACTION_ALART_ADMOBBANER + pk_name, 10000);
                            else
                                Ab.sendMsg(abHander, banner, B.ADMOB_BANNER);
                        }
                        break;
                    case B.ADMOB_BANNER:
                        Log.i("Alog", "in Ad banner");
                        Ab.sendMsg(abHander, banner, B.ADMOB_BANNER);
                        break;
                    case B.CLEAR:// 9
                        Ab.sendMsg(abHander, banner, B.CLEAR);
                        break;
                }
            }
        });
    }

    /***
     * 判断显示广告条件
     *
     * @return
     */
    private boolean check_ad_conditions() {

        if (admobArrays == null) return false;

        T t0 = DataBaseManager.getInstance(getApplicationContext()).get_setData(0);

        if (t0 == null) return false;

        boolean res = DataBaseManager.getInstance(getApplicationContext()).check_status(t0.getIn(), t0.getC());

        boolean check_time = Utils.checkAdtime_in_2min(getApplicationContext());

        return res && check_time;
    }

    /***
     * 初始化配置比例
     */
    public void init_proportion() {

        // TODO Auto-generated method stub
        T t = DataBaseManager.getInstance(this).get_setData(1);

        LogUtil.info("Adlog", "服务器中配置的比例：" + t.getAid().trim());

        Log.i("Alog", "service proportion：" + t.getAid().trim());

        String msg = t.getAid().trim();

        if (TextUtils.isEmpty(msg)) {

            msg = "900550|22";
        }
        String randMsg = msg.substring(0, msg.indexOf("|"));

        String sendTime = msg.substring(msg.indexOf("|") + 1);

        Log.i("Adlog", "proportion:" + randMsg);

        Log.i("Adlog", "sendTime_proportion:" + sendTime);

        weightArrays = new int[3];

        admobArrays = new int[3];

        String all = randMsg.substring(0, 3);

        String admob = randMsg.substring(3, randMsg.length());

        for (int i = 0; i < weightArrays.length; i++) {

            weightArrays[i] = Integer.parseInt(all.substring(i, i + 1));

            admobArrays[i] = Integer.parseInt(admob.substring(i, i + 1));
        }
    }

    private void showNotification() {

        if (Utils.check_black_list(this) != -1) {

            notification = n.buildNotification(notid, pk_name);

            if (notification != null) {

                showNotification = true;

                startForeground(notid, notification);
                LogUtil.info("Adlog", "显示通知栏");
                Log.i("Alog", "show notification");
                //  showNotification = true;
            }
        }
    }

    private void check_notification_by_blacklist() {

        if (notification != null) {

            if (Utils.check_black_list(getApplicationContext()) == -1) {

                LogUtil.info("Adlog", "黑名单清除 通知栏");

                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                nm.cancel(notid);

                stopForeground(true);

                StatService.onEvent(getApplicationContext(), "cancel_notification", "cancel_notification_s", 1);

                showNotification = false;

            } else {

                if (!showNotification) {

                    LogUtil.info("Adlog", "不是黑名单显示 通知栏");

                    startForeground(notid, notification);

                    showNotification = true;
                }
            }

        }
    }


}
