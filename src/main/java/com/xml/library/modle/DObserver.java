package com.xml.library.modle;

import android.database.ContentObserver;
import android.os.Handler;

import com.xml.library.services.A;

/**
 * Created by xlc on 2016/12/29.
 */
public class DObserver extends ContentObserver {

    private N aObject;

    public DObserver(N a, Handler handler) {
        super(handler);
        this.aObject=a;
    }
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        aObject.dObserverChange();
    }
}
