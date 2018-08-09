package com.eeseetech.nagrand.entity;

import android.util.Log;

import com.eeseetech.nagrand.BuildConfig;
import com.eeseetech.nagrand.Utils;


/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/7.
 */

public class RequestInfo {
    private final String TAG = "RequestInfo";
    private String API = "http://api.eeseetech.com/";
    private String site;
    private String service;
    private String version;
    private String timestamp;
    private String data;
    private String sign;

    public RequestInfo(String service, String data) {
        this.service = service;
        this.data = data;
    }

    public RequestInfo(String service, String version, String data) {
        this.service = service;
        this.version = version;
        this.data = data;
    }

    public RequestInfo(String site, String service, String version, String data) {
        this.site = site;
        this.service = service;
        this.version = version;
        this.data = data;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String signTemp) {
        this.sign = Utils.getMD5(signTemp);
    }

    public String BuildUrl() {
        if ("version_develop".equals(BuildConfig.FLAVOR)) {
            API = "http://dev.api.eeseetech.com/";
        } else if ("version_test".equals(BuildConfig.FLAVOR)) {
            API = "http://test.api.eeseetech.com/";
        }
        timestamp = String.valueOf(System.currentTimeMillis());
        String signTemp = "eeseeTech"
                + "data" + data
                + "service" + service
                + "t" + timestamp
                + "v" + version;
        Log.d(TAG, "BuildUrl: " + signTemp);
        setSign(signTemp);
        return API + site + "/index.php?service=" + service
                + "&v=" + version
                + "&t=" + timestamp
                + "&data=" + data
                + "&sign=" + sign;
    }
}
