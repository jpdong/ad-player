package com.eeseetech.nagrand.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseEntity {

    @Expose
    @SerializedName("code")
    protected int code;

    @Expose
    @SerializedName("msg")
    protected String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
