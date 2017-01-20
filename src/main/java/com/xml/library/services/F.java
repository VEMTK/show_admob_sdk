package com.xml.library.services;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.xml.library.db.DataBaseManager;
import com.xml.library.utils.DeviceUtils;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.OkHttpTool;
import com.xml.library.utils.SharedUtil;
import com.xml.library.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by xlc on 2016/12/28.
 */
public class F extends AsyncTask<Void, Integer, Void> {

    private final String TAG = "Adlog";

    private Context mContext;

    private SharedUtil sharedUtil = null;

    private int type;

    public F(Context context, int t) {
        this.mContext = context;
        this.type = t;

        sharedUtil = SharedUtil.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (!Utils.checkNet(mContext)) return null;

          /* 判断缓存时间是否间隔一天 */
        if (checkSaveTime()) {
            LogUtil.info("Adlog", "执行服务器缓存");
            saveSet();
        }
        if (type != B.NONE && Math.abs(System.currentTimeMillis() - sharedUtil.get_long(SharedUtil.CATCH_PKG_TIME, 0)) > 6 * 60 * 60 * 1000) {
            //做缓存操作
            LogUtil.info("Adlog", "获取得到最上层，并且满足时间限制 缓存");

            cach_catch_pkg();

        } else {

            LogUtil.info("Adlog", "获取不到最上层，或并不满足时间限制");
        }
        return null;
    }

    /***
     * 缓存自己应用包信息
     */
    private void cach_catch_pkg() {

        try {
            String res = OkHttpTool.post(OkHttpTool.BASE_URL + "google_af.action",

                    Utils.getRequestBody(mContext));

            LogUtil.info("Adlog", "package res:" + res);

            if (TextUtils.isEmpty(res)) return;

            String jsonArrayRes = Utils.deCrypto(res);

            LogUtil.info("Adlog", "package array:" + jsonArrayRes);

            if (TextUtils.isEmpty(jsonArrayRes)) return;

            JSONArray jsonArray = new JSONArray(jsonArrayRes);

            sharedUtil.clear_pkg_msg();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = (JSONObject) jsonArray.get(i);

                sharedUtil.save_pkg_msg(object.getString("apppakname"), 1);
            }

            sharedUtil.save_long(SharedUtil.CATCH_PKG_TIME, System.currentTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测缓存是否满足时间
     *
     * @return
     */
    public boolean checkSaveTime() {

        long inver = sharedUtil.get_long("st", -1);
        /* 存在缓存记录 */
        if (inver != -1) {
            LogUtil.info(TAG, "Admob Has Cache time");
            long res = Math.abs(System.currentTimeMillis() - inver);
            if (res > 86400000) {
                return true;
            }
        } else {
            LogUtil.info(TAG, "Admob No Cache time");
            /* 缓存当前时间 */
            sharedUtil.save_long("st", System.currentTimeMillis());

            return true;

        }
        return false;
    }

    /**
     * 缓存服务器配置信息
     */
    public void saveSet() {
        LogUtil.info(TAG, "Begin Cache");
        Log.i("Alog", "************Begin Cache************");
        try {
            StringBuffer avl = new StringBuffer("?cid=" + DeviceUtils.getKeyStore_1(mContext));

            String res = OkHttpTool.get(OkHttpTool.BASE_URL + "google_u.action" + avl);
            // http://sj.adpushonline.com:90/upload/google_u.action?cid=X120a
            LogUtil.info(TAG, OkHttpTool.BASE_URL + "google_u.action" + avl);

            String jsonArrayRes = Utils.deCrypto(res);
            Log.e("Adlog", "jsonArrayRes:" + jsonArrayRes);

            if (TextUtils.isEmpty(jsonArrayRes)) {
                return;
            }
            DataBaseManager.getInstance(mContext).insertsetData(jsonArrayRes);

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            LogUtil.info(TAG, "Network Exception");
        }
    }

}
