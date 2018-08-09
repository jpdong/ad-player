package com.eeseetech.nagrand.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.api.DownloadListener;
import com.eeseetech.nagrand.data.RemoteRepository;
import com.eeseetech.nagrand.data.VideoDataBase;
import com.eeseetech.nagrand.data.VideoDownloadTask;
import com.eeseetech.nagrand.data.VideoInfo;
import com.eeseetech.nagrand.data.VideoRepository;

import java.util.Queue;

public class WorkService extends Service {

    public static final int MSG_CHECK_MEDIA = 100;

    private Handler mHandler;
    private DownloadCallback mCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Global.TAG, "WorkService/onCreate:");
        HandlerThread handlerThread = new HandlerThread("WorkServiceThread");
        handlerThread.start();
        mHandler = new WorkHandler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WorkBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Global.TAG, "WorkService/onDestroy:");
        mCallback = null;
        mHandler.getLooper().quit();
    }

    public void setCallBack(DownloadCallback callBack) {
        mCallback = callBack;
    }

    public Handler getWorkHandler() {
        return mHandler;
    }

    private void checkMedia() {
        Log.d(Global.TAG, "WorkService/checkMedia:");
        VideoRepository repository = VideoRepository.getInstance(this);
        Queue<VideoInfo> downloadQueue = repository.getDownloadFiles();
        //Log.d(Global.TAG, "WorkService/checkMedia:media dir:" + Global.LOCAL_MEDIA_DIR);
        if (downloadQueue.size() == 0) {
            mCallback.onEnd();
            return;
        }
        VideoDownloadTask downloadTask = new VideoDownloadTask(new VideoDataBase(this), downloadQueue);
        downloadTask.start(mCallback);
    }

    public interface DownloadCallback{
        void onStart();
        void onProcess(float process);
        void onEnd();
    }

    public class WorkBinder extends Binder {

        public Service getService() {
            return WorkService.this;
        }
    }

    class WorkHandler extends Handler {

        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHECK_MEDIA:
                    checkMedia();
                    break;
            }
        }
    }
}
