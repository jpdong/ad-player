package com.eeseetech.nagrand.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by dong on 2017/5/2.
 */

public class MessageInfo {

    public String position;

    @SerializedName("msg")
    public String message;

    public List<TimelistBean> timelist;
}
