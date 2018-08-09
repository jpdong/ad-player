package com.eeseetech.nagrand.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseData {

    @Expose
    @SerializedName("code")
    public int code;

    @Expose
    @SerializedName("msg")
    public String msg;

}
