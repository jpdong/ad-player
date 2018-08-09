package com.eeseetech.nagrand.view;

import android.content.Context;

import java.util.List;

public interface MediaView {

    Context getContext();

    void replaceList(List<String> playList);

    void prepareFile(String fileName);

    void playDelay(long delay);

    boolean playerState();
}
