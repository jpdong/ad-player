package com.eeseetech.nagrand;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v7.view.menu.MenuItemImpl;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Bugly.init(this, "b5babd628e", false);
        if (LeakCanary.isInAnalyzerProcess(getApplicationContext())) {
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        Beta.installTinker();
    }

    public static App getInstance() {
        return mInstance;
    }
}
