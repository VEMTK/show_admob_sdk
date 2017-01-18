package com.xml.library.utils;

/**
 * Created by yindezhi on 16/12/26.
 */


import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 处理联网交互信息
 */
public class OkHttpTool {

    public static final String BASE_URL = A.gd();

    public static ExecutorService executorService = Executors.newScheduledThreadPool(20);

    /**
     * POST 请求
     *
     * @return
     */
    public static String post(String url, RequestBody requestBody) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {

            Log.e("Adlog","error:"+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    /**
     * get请求
     * @param url
     * @return
     */
    public static String get(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
