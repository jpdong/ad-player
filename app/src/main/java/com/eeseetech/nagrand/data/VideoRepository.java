package com.eeseetech.nagrand.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.Utils;
import com.eeseetech.nagrand.entity.PlayHistoryData;
import com.eeseetech.nagrand.entity.RequestVideoDataEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/6.
 */

public class VideoRepository {

    private static final String TAG = "nagrand";

    private static VideoRepository mInstance;
    private String mNetMacAddress;
    private String mSelfMacAddress;

    private String mFileRequestTag = "0";
    private String mListRequestTag = "0";

    private Queue<VideoInfo> mFileNeedDownload;

    private VideoDataBase mVideoDataBase;
    private Context mContext;
    private RemoteRepository mRemoteRepository;

    public static synchronized VideoRepository getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VideoRepository(context.getApplicationContext());
        }
        return mInstance;
    }

    private VideoRepository(Context context) {
        mContext = context;
        setDataSource(new VideoDataBase(mContext),RemoteRepository.getInstance());
        mFileNeedDownload = new LinkedList<>();
        mNetMacAddress = "000000000000";
        mSelfMacAddress = Utils.getMacAddress(context);
        Log.d(Global.TAG, "VideoRepository/VideoRepository:macAddress : " + mSelfMacAddress);
        mRemoteRepository.setMacAddress(mNetMacAddress,mSelfMacAddress);
    }

    public void setDataSource(VideoDataBase dataBase,RemoteRepository remoteRepository) {
        mVideoDataBase = dataBase;
        mRemoteRepository = remoteRepository;
    }

    public void updateFiles() {
        Queue<VideoInfo> downloadQueue = getDownloadFiles();
        Log.d(Global.TAG, "WorkService/checkMedia:media dir:" + Global.LOCAL_MEDIA_DIR);
        if (downloadQueue.size() == 0) {
            return;
        }
        VideoDownloadTask downloadTask = new VideoDownloadTask(mVideoDataBase, downloadQueue);
        downloadTask.start(null);
    }

    public Queue<VideoInfo> getDownloadFiles() {
        Queue<VideoInfo> downloadFiles = new LinkedList<>();
        File file = new File(Global.LOCAL_MEDIA_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        Map<String,VideoInfo> localData = loadListFromDB();
        Map<String,VideoInfo> remoteData = getRemoteFileList();
        Log.d(Global.TAG, "VideoRepository/getDownloadFiles:local:" + localData + ",remotedata:" + remoteData);
        if (remoteData != null && localData != null) {
            downloadFiles = syncFileList(localData,remoteData);
        } else if (remoteData != null) {
            for (VideoInfo videoInfo : remoteData.values()) {
                downloadFiles.offer(videoInfo);
            }
        }
        return downloadFiles;
    }

    private Map<String, VideoInfo> getRemoteFileList() {
        //RequestVideoDataEntity requestEntity = new RequestVideoDataEntity(mFileRequestTag, FILE_LIST_TYPE, mNetMacAddress, mSelfMacAddress, mRequestVersion);
        return mRemoteRepository.getRemoteFileList(mFileRequestTag);
    }

    public void insertDataToDB(final List<String> fileNameList) {
        for (final String fileName : fileNameList) {
            if(mVideoDataBase.saveVideoInfo(fileName)) {
                Log.d(TAG, "VideoRepository/insertDataToDB:" + fileName + " success");
            } else {
                Log.d(TAG, "VideoRepository/insertDataToDB:" + fileName + " fail");
            }
        }
    }

    public boolean getLocalFileList() {
        Log.d(Global.TAG, "VideoRepository/getLocalFileList:");
        List<String> fileNameList = new ArrayList<>();
        File files = new File(Global.LOCAL_MEDIA_DIR);
        File[] videoFiles = files.listFiles();
        if (videoFiles == null || videoFiles.length == 0) {
            return false;
        }
        for (File file : videoFiles) {
            if (Utils.checkVideoFile(file.getName())) {
                fileNameList.add(file.getName());
            }
        }
        insertDataToDB(fileNameList);
        return true;
    }

    private List<String> getCurrentPlayList(List<String> playIdList, Map<String, VideoInfo> fileMap) {
        Log.d(Global.TAG, "VideoRepository/getCurrentPlayList:");
        List<String> currentList = new ArrayList<>();
        Iterator<Map.Entry<String, VideoInfo>> entryIterator = fileMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, VideoInfo> entry = entryIterator.next();
            String name = entry.getKey();
            VideoInfo videoInfo = entry.getValue();
            if (playIdList.contains(videoInfo.getId())) {
                if (videoInfo.getTimelist() != null && videoInfo.getTimelist().size() != 0) {
                    if (Utils.checkTime(videoInfo.getTimelist())) {
                        currentList.add(name);
                    }
                } else {
                    currentList.add(name);
                }
            }
        }
        return currentList;
    }

    public List<String> getPlayList() {
        Log.d(Global.TAG, "VideoRepository/getPlayList:");
        List<String> idList = getPlayIdList();
        if (idList == null || idList.size() == 0) {
            idList = new ArrayList<>();
            String playlist = mVideoDataBase.getCachePlayList();
            if (!"".equals(playlist)) {
                String[] ids = playlist.split(",");
                for (String id : ids) {
                    if (id != null) {
                        idList.add(id);
                    }
                }
            }
        } else {
            mVideoDataBase.storePlayList(idList);
        }
        Log.d(Global.TAG, "VideoRepository/getPlayList:id list size : " + idList.size());
        Map<String,VideoInfo> map = getFileList();
        Log.d(Global.TAG, "VideoRepository/getPlayList:files size : " + map.size());
        return getCurrentPlayList(idList, map);

    }

    public Queue<VideoInfo> syncFileList(Map<String, VideoInfo> localData, Map<String, VideoInfo> remoteData) {
        final Map<String, VideoInfo> currentFileList = localData;
        final Map<String, VideoInfo> serverFileList = remoteData;
        List<String> fileNeedDeleted = new ArrayList<>();
        Iterator<Map.Entry<String, VideoInfo>> iterator = serverFileList.entrySet().iterator();
        Map.Entry<String, VideoInfo> entry;
        VideoInfo videoInfo;
        while (iterator.hasNext()) {
            entry = iterator.next();
            final String name = entry.getKey();
            videoInfo = entry.getValue();
            if (currentFileList.containsKey(name)) {
                if (videoInfo == null) {
                    videoInfo = new VideoInfo(name);
                }
                videoInfo.setDeletedTag(false);
                currentFileList.put(name, videoInfo);
                mVideoDataBase.updateVideoInfo(videoInfo);
            } else {
                mFileNeedDownload.offer(videoInfo);
            }
        }
        Iterator<Map.Entry<String, VideoInfo>> currentIterator = currentFileList.entrySet().iterator();
        Map.Entry<String, VideoInfo> currentEntry;
        String currentName;
        VideoInfo currentVideoInfo;
        while (currentIterator.hasNext()) {
            currentEntry = currentIterator.next();
            currentName = currentEntry.getKey();
            currentVideoInfo = currentEntry.getValue();
            if (currentVideoInfo.getDeletedTag()) {
                fileNeedDeleted.add(currentName);
            }
        }
        for (String fileName : fileNeedDeleted) {
            deleteVideo(fileName);
        }
        return mFileNeedDownload;
    }

    private void deleteVideo(final String fileName) {
        File file = new File(Global.LOCAL_MEDIA_DIR + fileName);
        if (file.delete()) {
            mVideoDataBase.deleteVideoInfo(fileName);
        } else {
            if (Global.LOG) {
                Log.d(TAG, "fail deleteVideo:" + fileName);
            }
        }
    }

    public Map<String,VideoInfo> loadListFromDB() {
        Log.d(Global.TAG, "VideoRepository/loadListFromDB:");
        List<VideoInfo> result = mVideoDataBase.getVideoInfo();
        Log.d(Global.TAG, "VideoRepository/loadListFromDB:file size = " + result.size());
        if (result == null || result.size() == 0) {
            if (getLocalFileList()) {
                return loadListFromDB();
            }else {
                return null;
            }
        }
        Map<String, VideoInfo> currentFileList = new HashMap<>();
        for (VideoInfo videoInfo : result) {
            currentFileList.put(videoInfo.getFilename(), videoInfo);
        }
        return currentFileList;
    }

    public void deleteFileInDB(final String s) {
        mVideoDataBase.deleteVideoInfo(s);

    }

    public Map<String, VideoInfo> getFileList() {
        Map<String,VideoInfo> map = getRemoteFileList();
        if (map == null || map.size() == 0) {
            return loadListFromDB();
        }
        return map;
    }

    public List<String> getPlayIdList() {
        return mRemoteRepository.getRemotePlayList(mListRequestTag);
    }

    public void addPlayHistory(String fileName, String timeStamp) {
        if (mRemoteRepository.uploadPlayHistory(fileName, timeStamp)) {
            Log.d(Global.TAG, "VideoRepository/addPlayHistory:" + fileName + " success");
            mVideoDataBase.deletePlayHistory(fileName);
        } else {
            mVideoDataBase.addPlayHistory(fileName, timeStamp);
        }
    }

    public void checkCachedHistory() {
        List<PlayHistoryData> result = mVideoDataBase.getPlayHistory();
        if (result != null && result.size() != 0) {
            if (result.size() > 3) {
                for (int i = 0; i < 3; i++) {
                    PlayHistoryData playHistory = result.get(i);
                    addPlayHistory(playHistory.name, playHistory.time);
                }
            } else {
                for (int i = 0; i < result.size(); i++) {
                    PlayHistoryData playHistory = result.get(i);
                    addPlayHistory(playHistory.name, playHistory.time);
                }
            }
        }
    }
}
