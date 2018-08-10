package com.eeseetech.nagrand.entity;

import java.util.List;

/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/6.
 */

public class VideoInfo {

    private String id;

    private String filename;

    private boolean deletedTag = true;

    private String md5sum;

    private List<TimelistBean> timelist;

    public VideoInfo(String filename) {
        this.filename = filename;
    }

    public VideoInfo(String id, String filename, List<TimelistBean> timelist) {
        this.id = id;
        this.filename = filename;
        this.timelist = timelist;
    }

    public VideoInfo(String id, String name) {
        this.id = id;
        this.filename = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public boolean getDeletedTag() {
        return deletedTag;
    }

    public void setDeletedTag(boolean deletedTag) {
        this.deletedTag = deletedTag;
    }

    public List<TimelistBean> getTimelist() {
        return timelist;
    }

    public String getMd5sum() {
        return md5sum;
    }
}
