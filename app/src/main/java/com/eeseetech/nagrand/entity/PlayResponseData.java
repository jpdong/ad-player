package com.eeseetech.nagrand.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlayResponseData extends ResponseData{

    @SerializedName("data")
    public PlayListData data;

    public static class PlayListData {
        public int retcode;
        public String tag;
        public List<String> snapshot;
    }
}
