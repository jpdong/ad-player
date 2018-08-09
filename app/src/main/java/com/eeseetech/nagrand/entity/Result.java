package com.eeseetech.nagrand.entity;

public class Result<T> {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int NO_CHANGE = 2;

    public int code = 0;
    public T data;
    public String msg;
    public String tag;

}
