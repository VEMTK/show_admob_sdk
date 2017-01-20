package com.xml.library.ad;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.baidu.mobstat.StatService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;
import com.xml.library.services.B;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
/**
 * Created by xlc on 2017/1/20.
 */
public class GameAd extends Activity {

    private InterstitialAd mInterstitial;

    public static final String TAG = "Adlog";

    private boolean interstitial_show = false;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void showAdmob(Context context) {

        Intent in = new Intent(context, GameAd.class);

        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(in);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ProgressBar(getApplicationContext()));
        /* 检查网络设置 */
        // 执行代码,将广告类型传入
        new Asyn().executeOnExecutor(HttpUtil.executorService);
    }
    public void showAdmob(String adid) {
        try {
            mInterstitial = new InterstitialAd(this);
            mInterstitial.setAdUnitId(adid);
            AdRequest builder = new AdRequest.Builder().build();
            mInterstitial.loadAd(builder);
            /**统计请求次数**/
            StatService.onEvent(getApplicationContext(), "game_interstitial", "request_admob_interstitial", 1);
            /**开始统计请求时长**/
            StatService.onEventStart(getApplicationContext(), "game_interstitial", "request_admob_interstitial");

            mInterstitial.setAdListener(new AdListener() {
                @Override
                public void onAdLeftApplication() {
                    // TODO Auto-generated method stub
                    super.onAdLeftApplication();
                    // check save time
                    StatService.onEvent(getApplicationContext(), "game_interstitial", "click_admob_interstitial", 1);

                    finish();
                }
                @Override
                public void onAdLoaded() {
                    /*****展示成工统计******/
                    StatService.onEvent(getApplicationContext(), "game_interstitial", "request_admob_interstitial_success", 1);
                    /*****请求结束****/
                    StatService.onEventEnd(getApplicationContext(), "game_interstitial", "request_admob_interstitial");

                    if (!isFinishing() && !interstitial_show) {

                        showInterstitial();

                        interstitial_show = true;

                        LogUtil.info(TAG, "Admob show");

                        Log.i("Alog", "AdSrc_Show");

                        /****开始统计展示时长****/
                        StatService.onEventStart(GameAd.this, "game_interstitial", "show_admob_interstitial");

                    }
                    super.onAdLoaded();

                }

                @Override
                public void onAdClosed() {
                    // TODO Auto-generated method stub
                    super.onAdClosed();
                    if (mInterstitial != null) {

                        mInterstitial = null;
                    }
                    /******关闭插屏事件统计*******/
                    StatService.onEvent(getApplicationContext(), "game_interstitial", "close_admob_interstitial", 1);

                    finish();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);

                    LogUtil.info(B.TAG, "Admob onAdFailedToLoad:" + errorCode);

                    Log.i("Alog", "adMsc_fail:" + errorCode);

                    /******请求失败******/
                    StatService.onEvent(getApplicationContext(), "game_interstitial", "request_admob_interstitial_fail", 1);

                    /*****请求结束****/
                    StatService.onEventEnd(getApplicationContext(), "game_interstitial", "request_admob_interstitial");

                    finish();

                }
                @Override
                public void onAdOpened() {
                    // TODO Auto-generated method stub
                    super.onAdOpened();

                }

            });

        } catch (Exception e) {

            Log.i("Alog", "" + e.getMessage());

            finish();
        }
    }

    /**
     * @author
     */
    class Asyn extends AsyncTask<Void, Integer, T> {

        @Override
        protected T doInBackground(Void... params) {

            return DataBaseManager.getInstance(getApplicationContext()).get_setData(0);
        }

        @Override
        protected void onPostExecute(T t) {
            // TODO Auto-generated method stub
            super.onPostExecute(t);

            LogUtil.info(B.TAG, "onPostExecute>>t：" + t);

            if (t == null) {
                return;
            }
            LogUtil.info(B.TAG, "onPostExecute>>t.getAtype：" + t.getAtype());

            LogUtil.info(B.TAG, "onPostExecute>>t.getAid：" + t.getAid());

            showAdmob(t.getAid());
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        LogUtil.info("Adlog", "onDestroy");

        if (mInterstitial != null) {

            mInterstitial = null;
        }
        if (interstitial_show) {

            StatService.onEventEnd(GameAd.this, "game_interstitial", "show_admob_interstitial");
        }
    }

    public void showInterstitial() {

        if (mInterstitial != null && mInterstitial.isLoaded()) {

            mInterstitial.show();
        }
    }
}