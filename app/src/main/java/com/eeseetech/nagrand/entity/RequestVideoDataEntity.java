package com.eeseetech.nagrand.entity;

public class RequestVideoDataEntity {

    private String tag;
    private int type;
    private String mac;
    private String self;
    private int version;

    public RequestVideoDataEntity(String tag, int type, String mac, String self, int version) {
        this.tag = tag;
        this.type = type;
        this.mac = mac;
        this.self = self;
        this.version = version;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
