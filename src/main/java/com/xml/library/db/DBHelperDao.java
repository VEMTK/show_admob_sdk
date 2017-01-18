package com.xml.library.db;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.xml.library.modle.T;
import com.xml.library.services.B;
import com.xml.library.utils.DeviceUtils;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;
import com.xml.library.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by aspsine on 15-4-19.
 */
public class DBHelperDao extends AbstractDao {

    private String TAG = "Adlog";

    private Context mContext;

    /* 缓存配置表 */
    public static final String TBL_SETS = "tbl_sets";

    public static final String TBL_STATUS = "tbl_status";

//    /* banner 缓存表 */
//    public static final String BANNER_TAB = "banner_tab";
//
//    public static final String CLICK_TAB = "click_tab";
//
//    /* 下载包信息表 */
//    public static final String DOWN_TAB = "down_tab";

    /**
     * atype:广告类型 c:弹出次数 inr:时间间隔(分钟) aid:sdk中的appid iid：sdk中的apiid 两个id可以互换
     */
    public static final String TBL_SETS_CREATE = "create table " + TBL_SETS + " (id integer primary key autoincrement,"
            + "atype integer," + "c integer," + "inr integer," + "aid text,iid text)";

    /**
     * dt 每天的日期 it 一天中的时间间隔 ic 每天的次数
     */
    public static final String TBL_STATUS_CREATE = "create table " + TBL_STATUS
            + " (id integer primary key autoincrement," + "dt text," + "it text," + "ic integer" + ")";

//    /***
//     * 缓存服务器banner表
//     */
//    public static final String CREATE_BANNER_TAB = "create table " + BANNER_TAB
//
//            + " (id integer primary key autoincrement," + "offer_id integer," + "banner_type integer," + "imgname text,"
//
//            + "link_url text," + "start_time integer," + "end_time integer," + "display_interval_banner integer,"
//
//            + "display_count integer," + "link_display_interval integer," + "isbanner integer,"
//
//            + "click_count integer,link_title text)";
//
//    /***
//     * offer_id id ;** local_check 本地点击次数
//     */
//    public static final String CREATE_CHECK_TAB = "create table " + CLICK_TAB
//
//            + " (id integer primary key autoincrement," + "offer_id integer," + "local_click integer" + ")";
//
//    /**
//     * pkg_name 包名
//     * <p/>
//     * down_url 下载链接
//     */
//    public static final String CREATE_DOWN_TAB = "create table " + DOWN_TAB + " (id integer primary key autoincrement,"
//
//            + "pkg_name text," + "appalias text," + "appname text," + "down_url text" + ")";


    public DBHelperDao(Context context) {
        super(context);
        this.mContext = context;
    }

    public static void createTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + TBL_SETS);

        db.execSQL(TBL_SETS_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + TBL_STATUS);

        db.execSQL(TBL_STATUS_CREATE);

        //  db.execSQL("DROP TABLE IF EXISTS " + BANNER_TAB);
//
//        db.execSQL(CREATE_BANNER_TAB);
//
//        db.execSQL(
//                "create index if not exists a on banner_tab(offer_id,click_count,isbanner,start_time,end_time,display_interval_banner)");
//
//        db.execSQL("DROP TABLE IF EXISTS " + CLICK_TAB);
//
//        db.execSQL(CREATE_CHECK_TAB);
//
//        db.execSQL("create index if not exists b on click_tab(offer_id,local_click)");
//
//        db.execSQL("DROP TABLE IF EXISTS " + DOWN_TAB);
//
//        db.execSQL(CREATE_DOWN_TAB);

    }

    public static void dropTable(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + TBL_SETS);

        db.execSQL("DROP TABLE IF EXISTS " + TBL_STATUS);

//        db.execSQL("DROP TABLE IF EXISTS " + BANNER_TAB);
//
//        db.execSQL("DROP TABLE IF EXISTS " + CLICK_TAB);
//
//        db.execSQL("DROP TABLE IF EXISTS " + DOWN_TAB);
    }


    public void saveSet(String jsonArrayRes) {

        SQLiteDatabase sd = getWritableDatabase();
        try {
            LogUtil.info(TAG, "begin");

            sd.delete(TBL_SETS, " 1=1", null);

            LogUtil.info(TAG, "Cache_res:" + jsonArrayRes);

            JSONArray jsonArray = new JSONArray(TextUtils.isEmpty(jsonArrayRes) ? "" : jsonArrayRes);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                ContentValues contentValues = new ContentValues();

                contentValues.put("atype", jsonObject.getInt("atype"));

                contentValues.put("c", jsonObject.getInt("c"));

                contentValues.put("inr", jsonObject.getInt("inr"));

                contentValues.put("aid", jsonObject.getString("aid"));

                contentValues.put("iid", jsonObject.getString("iid"));

                sd.insert(TBL_SETS, null, contentValues);

            }
            SPreferencesUtil.getInstance(mContext).save_long("st", System.currentTimeMillis());// 缓存数据完毕，更新时间

            LogUtil.info(TAG, "Synchronous correct");

        } catch (JSONException e) {

            LogUtil.info(TAG, e.getMessage());

        }
    }

    public T get_set_data(int atype) {
        SQLiteDatabase sd = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sd.query(TBL_SETS, new String[]{"c", "atype", "inr", "aid", "iid"},
                    " atype=" + atype, null, null, null, null);

            if (cursor.moveToNext()) {
                T t = new T();
                if (atype == 1) {

                    String aid = cursor.getString(cursor.getColumnIndex("aid"));

                    if (Pattern.compile("[0-9|]*").matcher(cursor.getString(cursor.getColumnIndex("aid"))).matches()
                            && aid != null) {
                        t.setAid(aid);
                    } else {
                        t.setAid("532343|22");
                    }

                } else {
                    t.setAid(cursor.getString(cursor.getColumnIndex("aid")));
                }

                t.setAtype(cursor.getInt(cursor.getColumnIndex("atype")));

                t.setC(cursor.getInt(cursor.getColumnIndex("c")));

                t.setIid(cursor.getString(cursor.getColumnIndex("iid")));
                /* 转换为Long 时间戳 */
                t.setIn(cursor.getInt(cursor.getColumnIndex("inr")) * 60 * 1000);

                return t;

            } else {

                if (atype == 1) {
                    T t = new T();
                    t.setAtype(1);
                    t.setC(10);
                    t.setIn(2);
                    t.setAid("532343|22");
                    return t;
                }
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void update_counts() {

        SQLiteDatabase sd = getWritableDatabase();

        Cursor cursor = null;

        try {

            cursor = sd.query(TBL_STATUS, new String[]{"dt", "it", "ic"}, "1=1", null, null, null, null,
                    "1");
            if (cursor.moveToNext()) {

                int sc = cursor.getInt(cursor.getColumnIndex("ic"));

                ContentValues contentValues = new ContentValues();

                // 次数加一
                contentValues.put("ic", sc + 1);

                Log.i("Alog", "Sh_counts:" + (sc + 1));

                LogUtil.info(B.TAG, "==显示次数==" + (sc + 1));

                // 每日的时间间隔
                contentValues.put("it", System.currentTimeMillis() + "");

                sd.update(TBL_STATUS, contentValues, "1=1", null);

            } else {

                Log.i("Alog", "Sh_counts: 1");

                LogUtil.info("Adlog", "第一次展示广告 次数：1");

                ContentValues contentValues1 = new ContentValues();

                // 保存当天日期信息
                contentValues1.put("dt", System.currentTimeMillis() + "");

                contentValues1.put("it", System.currentTimeMillis() + "");

                // 修改为1
                contentValues1.put("ic", 1);

                sd.insert(TBL_STATUS, null, contentValues1);
            }

        } finally {

            if (cursor != null) {

                cursor.close();

            }

        }
    }

    public boolean check_show_counts(long it, int ic) {

        if (!Utils.checkNet(mContext)) {

            LogUtil.info("Adlog","网络异常");

            return false;
        }

        SQLiteDatabase sd = getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String now = sdf.format(System.currentTimeMillis());

        Cursor cursor = null;

        try {

            // 查询今天限制------
            cursor = sd.query(TBL_STATUS, new String[]{"dt", "it", "ic"}, "1=1", null, null, null, null,
                    "1");

            if (!cursor.moveToNext()) {

                LogUtil.info(TAG, "No restriction return true");

                Log.i("Alog", "No restriction return true");

                /* 初始化一条 */
                //update_counts();

                return true;
            }

            String saveTimeString = cursor.getString(cursor.getColumnIndex("dt"));

            long saveTime = 0;

            if (!TextUtils.isEmpty(saveTimeString)) {

                saveTime = Long.valueOf(saveTimeString);

            }
            LogUtil.info(TAG, "Mobile Date：" + now);

            LogUtil.info(TAG, "Db Save Date：" + sdf.format(saveTime));

            int sic = cursor.getInt(cursor.getColumnIndex("ic"));

            if (now.equals(sdf.format(saveTime))) {

                LogUtil.info(TAG, "Verification Begin");

                String strit = cursor.getString(cursor.getColumnIndex("it"));

                long lit = 0;

                if (!TextUtils.isEmpty(strit)) {

                    lit = Long.valueOf(strit);
                }

                Log.i(TAG, "check_show_counts: ic" + ic + "  sic:" + sic);

                if (sic >= ic) {

                    Log.i("Alog", "is top of show counts");

                }
                if (Math.abs(System.currentTimeMillis() - lit) > it && sic < ic) {

                    LogUtil.info(TAG, "Verification:True");

                    Log.i("Alog", "Verification:True");

                    return true;

                } else {

                    LogUtil.info(TAG, "Verification:False");

                    Log.i("Alog", "Verification:False");
                                                  /* true test */
                    return false;
                }

            } else {

                StringBuffer strb = new StringBuffer();

                strb.append("?cid=" + DeviceUtils.getKeyStore_1(mContext));

                strb.append("&a=" + DeviceUtils.getIMEI_1(mContext));

                strb.append("&b=" + DeviceUtils.getIMSI_1(mContext));

                strb.append("&c=" + sic);

                AccountManager accountManager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);

                Account[] accounts = accountManager.getAccountsByType("com.google");

                if (accounts.length > 0) {

                    strb.append("&d=" + accounts[0].name);

                } else {
                    strb.append("&d=-1");
                }
                try {
                    String google_id = null;

                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);

                    if (adInfo != null) {

                        google_id = adInfo.getId();
                    }
                    strb.append("&e=").append(google_id);

                } catch (IOException e) {
                    Log.i("Adlog", "error:" + e.getMessage());
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.i("Adlog", "error:" + e.getMessage());
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    Log.i("Adlog", "error:" + e.getMessage());
                    e.printStackTrace();
                }
                LogUtil.info(TAG, "上传服务器 删除数据库表 操作：" + sic);

                LogUtil.info(TAG, HttpUtil.BASE_URL + "google_v.action");

                try {
                  HttpUtil.getRequest(HttpUtil.BASE_URL + "google_v.action" + strb);

                } catch (Exception e) {
                    Log.e("Adlog", "Exception:" + e.getMessage());
                    e.printStackTrace();
                }
                sd.delete(TBL_STATUS, "1=1", null);

                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
