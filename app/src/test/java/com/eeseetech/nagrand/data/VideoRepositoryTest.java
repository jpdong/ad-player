package com.eeseetech.nagrand.data;

import android.content.Context;
import android.util.Log;

import com.eeseetech.nagrand.Utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VideoRepositoryTest {

    VideoRepository videoRepository;
    Map<String, VideoInfo> current;
    Map<String, VideoInfo> server;
    List<String> remotePlayList;
    List<VideoInfo> localData;
    List<String> cacheList;
    VideoDataBase dataBase;
    RemoteRepository remoteRepository;

    @Before
    public void setup() {
        Context context = mock(Context.class);
        videoRepository = VideoRepository.getInstance(context);
        dataBase = mock(VideoDataBase.class);
        when(dataBase.updateVideoInfo(any(VideoInfo.class))).thenReturn(true);
        remoteRepository = mock(RemoteRepository.class);
        videoRepository.setDataSource(dataBase,remoteRepository);
        current = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            current.put(String.format("%d.mp4",i),new VideoInfo(""+i,String.format("%d.mp4",i)));
        }
        server = new HashMap<>();
        for (int i = 5; i < 15; i++) {
            server.put(String.format("%d.mp4",i),new VideoInfo(""+i,String.format("%d.mp4",i)));
        }
        remotePlayList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            remotePlayList.add("" + i);
        }
        when(remoteRepository.getRemotePlayList(anyString())).thenReturn(remotePlayList);
        localData = new ArrayList<>();
        for (int i = 0; i <10; i++) {
            localData.add(new VideoInfo(""+i,String.format("%d.mp4",i)));
        }
        when(dataBase.getVideoInfo()).thenReturn(localData);
        cacheList = new ArrayList<>();
        when(dataBase.getCachePlayList()).thenReturn("1,3,5,7,");
    }

    @Test
    public void getInstance() {
    }

    @Test
    public void updateFiles() {
    }

    @Test
    public void getDownloadFiles() {
    }

    @Test
    public void insertDataToDB() {
    }

    @Test
    public void getLocalFileList() {
    }

    @Test
    public void getPlayList() {
        List<String> list = videoRepository.getPlayList();
        Log.d("getPlayList", "list size = " + list.size());
        assertTrue(list.size() == 10);
        remotePlayList = null;
        when(remoteRepository.getRemotePlayList(anyString())).thenReturn(remotePlayList);
        List<String> list1 = videoRepository.getPlayList();
        Log.d("getPlayList", "list size = " + list1.size());
        assertTrue(list1.size() == 4);
    }

    @Test
    public void syncFileList() {
        Queue<VideoInfo> queue = videoRepository.syncFileList(current, server);
        Log.d("syncFileList", "queue size = "+ queue.size());
        assertTrue(queue.size() == 5);
    }

    @Test
    public void loadListFromDB() {
    }

    @Test
    public void deleteFileInDB() {
    }

    @Test
    public void getFileList() {
    }

    @Test
    public void getPlayIdList() {
    }

    @Test
    public void addPlayHistory() {
    }

    @Test
    public void checkCachedHistory() {
    }
}