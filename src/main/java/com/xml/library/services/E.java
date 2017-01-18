package com.xml.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xml.library.utils.LogUtil;

/**
 * Created by xlc on 2016/12/30.
 */
public class E extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, A.class));
    }
}
