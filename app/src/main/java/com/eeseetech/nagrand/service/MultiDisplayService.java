package com.eeseetech.nagrand.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;
import com.eeseetech.nagrand.player.ViewController;
import com.eeseetech.nagrand.view.MultiWindowManager;

public class MultiDisplayService extends Service {

    //private static final String TAG = "hdmidemo";

    private static final String HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    private static final String ZJS_ZWT_SHOW = "ZJS_ZWT_SHOW";
    private static final String ZJS_ZWT_GONE = "ZJS_ZWT_GONE";

    private boolean isShowing = false;

    private View mView;
    private ViewController mViewController;
    private Handler mHandler;

    private BroadcastReceiver mHDMIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /*if ("android.intent.action.HDMI_PLUGGED".equals(action)) {
                boolean state = intent.getBooleanExtra("state", false);
                Log.d(Global.TAG, "MultiDisplayService/onReceive: hdmi connect " + state);
            }*/
            Log.d(Global.TAG, "MultiDisplayService/onReceive:" + action);
            switch (action) {
                case HDMI_PLUGGED:
                    boolean state = intent.getBooleanExtra("state", false);
                    Log.d(Global.TAG, "MultiDisplayService/onReceive: hdmi connect " + state);
                    if (state) {
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                showMultiView();
                            }
                        }, 10 * 1000);
                    } else {
                        hideMultiView();
                    }
                    break;
                case ZJS_ZWT_SHOW:
                    Log.d(Global.TAG, "MultiDisplayService/onReceive:ZJS_ZWY_SHOW");
                    showMultiView();
                    break;
                case ZJS_ZWT_GONE:
                    Log.d(Global.TAG, "MultiDisplayService/onReceive:ZJS_ZWY_GONE");
                    hideMultiView();
                    break;
            }
        }
    };

    private void hideMultiView() {
        if (isShowing) {
            MultiWindowManager.removeMultiWindow(MultiDisplayService.this, mView);
            MultiWindowManager.clearCache();
            mViewController.stop();
            isShowing = false;
        }
    }

    private void showMultiView() {
        if (!isShowing) {
            if (MultiWindowManager.createMultiWindow(MultiDisplayService.this, mView)) {
                mViewController.start();
                isShowing = true;
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        showMultiView();
                    }
                }, 10 * 1000);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Global.TAG, "MultiDisplayService/onCreate:");
        mHandler = new Handler();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HDMI_PLUGGED);
        intentFilter.addAction(ZJS_ZWT_SHOW);
        intentFilter.addAction(ZJS_ZWT_GONE);
        registerReceiver(mHDMIReceiver, intentFilter);
        initMultiDisplay();
    }

    private void initMultiDisplay() {
        /*DisplayManager dm = (DisplayManager) getApplicationContext().getSystemService(DISPLAY_SERVICE);
        if (dm != null) {
            Display dispArray[] = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
            Log.d(Global.TAG, "onCreate: display num:" + dispArray.length);
            if (dispArray.length > 0) {
                Display display = dispArray[0];
                Log.e(Global.TAG, "Service using display:" + display.getName());
                Context displayContext = getApplicationContext().createDisplayContext(display);
                WindowManager wm = (WindowManager) displayContext.getSystemService(WINDOW_SERVICE);
                mView = LayoutInflater.from(displayContext).inflate(R.layout.view_hdmi, null);
                ViewController viewController = new ViewController(mView, this);
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.RGBA_8888);
                wm.addView(mView, params);
                viewController.start();
            }
        }*/
        Context displayContext = MultiWindowManager.getDisplayContext(this);
        mView = LayoutInflater.from(displayContext).inflate(R.layout.view_hdmi, null);
        MultiWindowManager.createMultiWindow(this, mView);
        isShowing = true;
        mViewController = new ViewController(mView, this);
        mViewController.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Global.TAG, "MultiDisplayService/onStartCommand:");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewController.stop();
        isShowing = false;
        unregisterReceiver(mHDMIReceiver);
    }
}
