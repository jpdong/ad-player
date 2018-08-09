package com.eeseetech.nagrand.data;

import android.content.SyncRequest;
import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.Utils;
import com.eeseetech.nagrand.api.DownloadListener;
import com.eeseetech.nagrand.service.WorkService;

import java.io.File;
import java.util.Queue;

public class VideoDownloadTask {

    private Queue<VideoInfo> mFileNeedDownload;
    private float[] mDownloadProgress;
    private int mDownloadSize = 0;
    private int mDownloadIndex = 0;
    private RemoteRepository mRemoteRepository;
    private VideoDataBase mDataBase;
    private WorkService.DownloadCallback mCallback;

    public VideoDownloadTask(VideoDataBase dataBase,Queue<VideoInfo> queue) {
        this.mFileNeedDownload = queue;
        this.mRemoteRepository = RemoteRepository.getInstance();
        this.mDataBase = dataBase;
    }

    public void start(WorkService.DownloadCallback callback) {
        this.mCallback = callback;
        mDownloadSize = mFileNeedDownload.size();
        mDownloadProgress = new float[mDownloadSize];
        for (int i = 0; i < 2; i++) {
            downloadNext();
        }
    }

    public void downloadNext() {
        if (mFileNeedDownload == null || mFileNeedDownload.size() == 0) {
            return;
        }
        VideoInfo videoInfo = mFileNeedDownload.poll();
        downloadVideo(videoInfo);
    }

    public void downloadVideo(final VideoInfo videoInfo) {
        final int cuttentIndex = mDownloadIndex++;
        final int[] a = new int[]{0};
        mRemoteRepository.downloadMedia(videoInfo.getFilename(),
                 new DownloadListener<File>() {
            @Override
            public void process(float process) {
                //Log.d(Global.TAG, "VideoDownloadTask/process:");
                handleProcess(process, a, cuttentIndex);
            }

            @Override
            public void done(File file) {
                if (file.exists()) {
                    if (Utils.checkFileMD5(videoInfo,file)) {
                        if (mDataBase.saveVideoInfo(videoInfo)) {
                            Log.d(Global.TAG, "VideoDownloadTask/downloadVideo:" + videoInfo.getFilename() + " success");
                        } else {
                            Log.d(Global.TAG, "VideoDownloadTask/downloadVideo:" + videoInfo.getFilename() + " fail");
                        }
                    }
                }
                downloadNext();
            }

            @Override
            public void fail(Exception e) {
                Log.e(Global.TAG, "VideoDownloadTask/fail:" + e.toString());
                mDownloadSize--;
                downloadNext();
            }
        });
    }

    private void handleProcess(float progress, int[] a, int cuttentIndex) {
        if (a[0] != (int) Math.floor(progress * 10) || a[0] == 10) {
            a[0] = (int) Math.floor(progress * 10);
            if (cuttentIndex < mDownloadProgress.length) {
                mDownloadProgress[cuttentIndex] = progress;
                calculateDownloadPercent();
            }
        }
    }

    private void calculateDownloadPercent() {
        float totalProgress = 0;
        for (float f : mDownloadProgress) {
            totalProgress += f;
        }
        float percent = totalProgress / mDownloadSize;
        if (mCallback != null) {
            mCallback.onProcess(percent);
        }
        if (totalProgress >= mDownloadSize) {
            mDownloadProgress = new float[0];
            mDownloadIndex = 0;
            if (mCallback != null) {
                mCallback.onEnd();
                mCallback = null;
            }
        }
    }
}
