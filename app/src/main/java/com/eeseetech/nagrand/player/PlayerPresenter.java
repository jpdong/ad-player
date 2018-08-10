package com.eeseetech.nagrand.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraDevice;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.common.SocketController;
import com.eeseetech.nagrand.data.VideoRepository;
import com.eeseetech.nagrand.view.MediaView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;


/**
 * Created by dong on 2017/4/12.
 */

public class PlayerPresenter {

    private static final String TAG = "nagrand";
    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private VideoRepository mVideoRepository;

    private Handler mHandler;
    private MediaView mMediaView;
    private SocketController mSocketController;
    private boolean prePlayerState = false;

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (null != info) {
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (Global.LOG) {
                            Log.d(TAG, "onReceive:Conn connect");
                        }
                        if (Global.hasXServer) {
                            mSocketController.checkSocketState();
                        }
                    } else {
                        if (Global.LOG) {
                            Log.d(TAG, "Conn disconnect");
                        }
                        if (Global.hasXServer) {
                            mSocketController.sendAppDisconnect();
                        }
                    }

                }
            }
        }
    };

    private SocketController.StateCallback mStateCallback = new SocketController.StateCallback() {
        @Override
        public void onLoadFile(String fileName) {
            Log.d(Global.TAG, "PlayerPresenter/onLoadFile:" + fileName);
            if (mMediaView != null) {
                mMediaView.prepareFile(fileName);
            }
        }

        @Override
        public void onStartPlay(long delay) {
            Log.d(Global.TAG, "PlayerPresenter/onStartPlay:" + delay);
            if (mMediaView != null) {
                mMediaView.playDelay(delay);
            }
        }
    };

    public PlayerPresenter(MediaView mediaView) {
        mMediaView = mediaView;
        mVideoRepository = VideoRepository.getInstance(mMediaView.getContext());
        if (Global.hasXServer) {
            mSocketController = new SocketController(mStateCallback);
        }
    }

    public void deleteFile(String s) {
        mVideoRepository.deleteFileInDB(s);
    }

    public void start() {
        HandlerThread thread = new HandlerThread("PlayPresenter Thread");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        registerNetworkReceiver();
        if (Global.hasXServer) {
            mSocketController.setCallback(mStateCallback);
            mSocketController.sendAppConnect();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sendScheduleSync();
            }
        });
    }

    public void stop() {
        if (Global.hasXServer) {
            mSocketController.setCallback(null);
            mSocketController.sendAppDisconnect();
        }
        unregisterNetworkReceiver();
        mHandler.getLooper().quit();
    }

    private void sendScheduleSync() {
        Log.d(Global.TAG, "PlayerPresenter/sendScheduleSync: schedule task start");
        mVideoRepository.updateFiles();
        List<String> list = mVideoRepository.getPlayList();
        if (Global.hasXServer) {
            checkSocketState();
            mSocketController.syncTime();
            uploadPlayList(list);
        } else {
            mMediaView.replaceList(list);
        }
        mMediaView.replaceList(list);
        mVideoRepository.checkCachedHistory();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendScheduleSync();
            }
        }, 60 * 1000);
    }

    private void registerNetworkReceiver() {
        if (mMediaView != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(CONNECTIVITY_CHANGE);
            mMediaView.getContext().registerReceiver(mNetworkReceiver, filter);
        }
    }

    private void unregisterNetworkReceiver() {
        if (mMediaView != null) {
            mMediaView.getContext().unregisterReceiver(mNetworkReceiver);
        }
    }

    private void checkSocketState() {
        mSocketController.checkSocketState();
    }

    private void uploadPlayList(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (mMediaView != null) {
            prePlayerState = mMediaView.playerState();
        }
        JSONObject params = new JSONObject();
        JSONArray array = new JSONArray();
        for (String name : list) {
            array.put(name);
        }
        try {
            params.put("playlist", array);
            params.put("ptag", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadPlayList: " + e.toString());
        }
        mSocketController.emit("app:playlist", params, new Ack() {
            @Override
            public void call(Object... args) {
                if (Global.LOG) {
                    Log.d(Global.TAG, "PlayerPresenter/uploadPlayList:" + args[0]);
                }
                checkPlayState();
            }
        });
    }

    private void checkPlayState() {
        if (mMediaView != null && !mMediaView.playerState() && !prePlayerState) {
            sendPlayerStatus("idle");
        }
    }

    public void addPlayHistory(final String fileName, final String timeStamp) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoRepository.addPlayHistory(fileName, timeStamp);
            }
        });
    }

    public void sendPlayerStatus(String status) {
        if (Global.hasXServer) {
            JSONObject data = new JSONObject();
            try {
                data.put("state", status);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "sendPlayerStatus: " + e.toString());
            }
            mSocketController.emit("app:go", data);
            if (Global.LOG) {
                Log.d(TAG, "VideoPresenter/sendPlayerStatus:" + status);
            }
        }
    }
}
