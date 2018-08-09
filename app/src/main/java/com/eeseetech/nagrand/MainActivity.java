package com.eeseetech.nagrand;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.eeseetech.nagrand.player.PlayerActivity;
import com.eeseetech.nagrand.service.MultiDisplayService;
import com.eeseetech.nagrand.service.WorkService;
import com.eeseetech.nagrand.view.FadingSeekBar;

import java.lang.ref.WeakReference;


public class MainActivity extends Activity {

    private static final String TAG = "nagrand";

    private TextView tvLoading;
    private TextView tvVersion;
    private ImageView ivShadow;
    private FadingSeekBar sbLoading;
    private StarUpHandler mHandler;
    private Handler mWorkHandler;

    private boolean isEmpty;
    private static final int MSG_UPDATE_PROGRESS = 10;
    private static final int MSG_DB_STATUS = 11;
    private static final int MSG_START_PLAY = 12;

    private WorkService.DownloadCallback mCallback = new WorkService.DownloadCallback(){

        @Override
        public void onStart() {

        }

        @Override
        public void onProcess(float process) {
            Log.d(Global.TAG, "MainActivity/onProcess:" +process);
            Message message = mHandler.obtainMessage(MSG_UPDATE_PROGRESS);
            message.obj = process;
            mHandler.sendMessage(message);
        }

        @Override
        public void onEnd() {
            Log.d(Global.TAG, "MainActivity/onEnd:");
            //startPlayer();
            //unbindService(mServiceConnection);
            if (Global.hasHDMI) {
                startMultiService();
            } else {
                startPlayer();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Global.TAG, "MainActivity/onServiceConnected:");
            WorkService.WorkBinder binder = (WorkService.WorkBinder)service;
            WorkService workService = (WorkService) binder.getService();
            mWorkHandler = workService.getWorkHandler();
            workService.setCallBack(mCallback);
            mWorkHandler.sendEmptyMessage(WorkService.MSG_CHECK_MEDIA);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Global.TAG, "MainActivity/onServiceDisconnected:");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        showLoadingText();
        mHandler = new StarUpHandler(this);
        Intent serviceIntent = new Intent(this, WorkService.class);
        bindService(serviceIntent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Global.TAG, "MainActivity/onDestroy:");
        unbindService(mServiceConnection);
    }

    private void startPlayer() {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMultiService() {
        Log.d(Global.TAG, "MainActivity/startMultiService:");
        Intent intent = new Intent(this, MultiDisplayService.class);
        startService(intent);
        finish();
    }

    private void setLoadingText(String loadingText) {
        tvLoading.setText(loadingText);
    }

    private boolean checkNet() {
        setLoadingText("正在检查网络连接...");
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            if (info.getState() == NetworkInfo.State.CONNECTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WeakReference<MainActivity> reference = new WeakReference<>(MainActivity.this);
                        setLoadingText("正在同步资源...");
                    }
                });
                return true;
            }
        }
        setLoadingText("网络连接断开，请检查网络");
        return false;
    }

    private void showLoadingText() {
        tvLoading = (TextView) findViewById(R.id.tv_loading);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        sbLoading = (FadingSeekBar) findViewById(R.id.sb_loading);
        ivShadow = (ImageView) findViewById(R.id.iv_shadow);

        String coInfo = "上海意视信息科技有限公司 © 2016-2017 | 当前版本：" + Utils.getVersionName(this);
        tvVersion.setText(coInfo);
        if (Global.LOG) {
            tvVersion.append(" beta");
        }
        Animation alphaIn = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        tvLoading.startAnimation(alphaIn);
    }

    private static class StarUpHandler extends Handler {
        private WeakReference<Context> reference;

        public StarUpHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) reference.get();
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    activity.setLoadingText("资源下载中，请保持网络畅通");
                    float progress = (float) msg.obj;
                    activity.sbLoading.setProgress((int) (progress * 100));
                    break;
                case MSG_START_PLAY:
                    activity.startPlayer();
                    break;
                default:
                    break;
            }
        }
    }
}
