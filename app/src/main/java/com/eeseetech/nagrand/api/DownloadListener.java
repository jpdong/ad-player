package com.eeseetech.nagrand.api;

public interface DownloadListener<T> {
    void process(float process);

    void done(T t);

    void fail(Exception e);
}