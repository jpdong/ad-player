package com.eeseetech.nagrand;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by XL on 2017/4/14.
 */

public class Global {

    public static final String TAG = "nagrand";
    public static final boolean LOG = true;
    public static long timeGap = 0;
    public static boolean hasXServer = false;
    public static boolean hasHDMI = false;
    public static final String LOCAL_MEDIA_DIR = Utils.getRootDirectory() + "/EeseeMedia/video/";
    public static final String REMOTE_MEDIA_SITE = "http://download.eeseetech.com/media/video/";
    public static boolean appConnected =false;
    public static String gateMacAddress = "000000000000";
    public static String versionName = BuildConfig.VERSION_NAME;
    public static int versionCode = BuildConfig.VERSION_CODE;
}
