package com.eeseetech.nagrand.data;

import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.api.DownloadListener;
import com.eeseetech.nagrand.api.Provider;
import com.eeseetech.nagrand.entity.MessageInfo;
import com.eeseetech.nagrand.entity.Result;
import com.eeseetech.nagrand.entity.VideoInfo;

import java.util.List;
import java.util.Map;

import static com.eeseetech.nagrand.entity.Result.NO_CHANGE;
import static com.eeseetech.nagrand.entity.Result.SUCCESS;


public class RemoteRepository {

    private static RemoteRepository mInstance;
    private Provider mProvider;
    private String mGateAddress = "000000000000";
    private String mMacAddress = "000000000000";
    private String mFileRequestTag = "0";
    private String mPlayListRequestTag = "0";
    private String mMessageRequestTag = "0";

    public static synchronized RemoteRepository getInstance() {
        if (mInstance == null) {
            mInstance = new RemoteRepository();
        }
        return mInstance;
    }

    private RemoteRepository() {
        mProvider = new Provider();
        //mGateAddress = Utils.getGateAddress();
        //mMacAddress = Utils.getMacAddress(context);
        Log.d(Global.TAG, "RemoteRepository/RemoteRepository:mac:" + mMacAddress);
    }

    public void setMacAddress(String gateAddress, String macAddress) {
        mGateAddress = gateAddress;
        mMacAddress = macAddress;
    }

    public void downloadMedia(String fileName, final DownloadListener downloadListener) {
        mProvider.downloadVideo(fileName, downloadListener);
    }

    public Map<String, VideoInfo> getRemoteFileList() {
        Result<Map<String, VideoInfo>> result = mProvider.videos(mFileRequestTag, mGateAddress, mMacAddress);
        if (result != null) {
            if (result.code == SUCCESS) {
                Log.d(Global.TAG, "RemoteRepository/getRemoteFileList:" + result.data.size());
                mFileRequestTag = result.tag;
                return result.data;
            } else if (result.code == NO_CHANGE){
                Log.d(Global.TAG, "RemoteRepository/getRemoteFileList:NO_CHANGE");
            } else {
                Log.d(Global.TAG, "RemoteRepository/getRemoteFileList:FAIL");
            }
        } else {
            Log.d(Global.TAG, "RemoteRepository/getRemoteFileList:NULL");
        }
        return null;
    }

    public List<String> getRemotePlayList() {
        Log.d(Global.TAG, "RemoteRepository/getRemotePlayList:");
        Result<List<String>> result = mProvider.playList(mPlayListRequestTag, mGateAddress, mMacAddress);
        if (result != null) {
            if (result.code == SUCCESS) {
                Log.d(Global.TAG, "RemoteRepository/getRemotePlayList:" + result.data.size());
                mPlayListRequestTag = result.tag;
                return result.data;
            } else if (result.code == NO_CHANGE){
                Log.d(Global.TAG, "RemoteRepository/getRemotePlayList:NO_CHANGE");
            } else {
                Log.d(Global.TAG, "RemoteRepository/getRemotePlayList:FAIL");
            }
        } else {
            Log.d(Global.TAG, "RemoteRepository/getRemotePlayList:NULL");
        }
        return null;
    }


    public boolean uploadPlayHistory(String fileName, String timeStamp) {
        String playTime = timeStamp.substring(0, timeStamp.length() - 3);
        String timeMillis = String.valueOf(System.currentTimeMillis());
        final String currentTime = timeMillis.substring(0, timeMillis.length() - 3);
        long delay = Long.valueOf(currentTime) - Long.valueOf(playTime);
        Result<Boolean> result = mProvider.addPlayHistory(delay, mGateAddress, mMacAddress, fileName);
        if (result != null && result.code == SUCCESS) {
            return true;
        } else {
            return false;
        }
    }

    public List<MessageInfo> getRemoteMessages() {
        Log.d(Global.TAG, "RemoteRepository/getRemoteMessages:");
        Result<List<MessageInfo>> result = mProvider.messages(mMessageRequestTag, mGateAddress);
        if (result != null && result.code == SUCCESS) {
            if (result.code == SUCCESS) {
                Log.d(Global.TAG, "RemoteRepository/getRemoteMessages:" + result.data.size());
                mMessageRequestTag = result.tag;
                return result.data;
            } else if (result.code == NO_CHANGE){
                Log.d(Global.TAG, "RemoteRepository/getRemoteMessages:NO_CHANGE");
            } else {
                Log.d(Global.TAG, "RemoteRepository/getRemoteMessages:FAIL");
            }
        } else {
            Log.d(Global.TAG, "RemoteRepository/getRemoteMessages:NULL");

        }
        return null;
    }
}
