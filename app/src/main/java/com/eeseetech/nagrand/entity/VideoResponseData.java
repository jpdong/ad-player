package com.eeseetech.nagrand.entity;

import com.eeseetech.nagrand.data.VideoInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoResponseData extends ResponseData{

    @SerializedName("data")
    public DataBean data;

    public static class DataBean {

        public int retcode;
        public String tag;
        public List<VideoInfo> fileList;

        @Override
        public String toString() {
            return "retcode:" + retcode + ",tag:" + tag + ",fileList:" + fileList.toString();
        }
    }

    @Override
    public String toString() {
        return "code:" + code + ",message:" + msg + ",data:" + data.toString();
    }
}
