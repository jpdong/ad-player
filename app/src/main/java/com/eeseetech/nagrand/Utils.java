package com.eeseetech.nagrand;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import com.eeseetech.nagrand.data.TimelistBean;
import com.eeseetech.nagrand.data.VideoInfo;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Utils {
    public static boolean checkVideoFile(String name) {
        if (!name.contains(".")) {
            return false;
        }
        if (".mp4".equals(name.substring(name.indexOf("."), name.length()))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkFileMD5(VideoInfo videoInfo, File file) {
        String name = file.getName();
        if (videoInfo.getMd5sum() != null) {
            try {
                if (videoInfo.getMd5sum().equals(Utils.fileMD5(file))) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Global.TAG, "Utils/checkFileMD5:" + e.toString());
            }
        } else {
            return true;
        }

        return false;
    }

    public static String fileMD5(File inputFile) throws IOException {
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;
            messageDigest = digestInputStream.getMessageDigest();
            byte[] resultByteArray = messageDigest.digest();
            return byteArrayToHex(resultByteArray).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    public static String getMD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            return getHashString(digest);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }
        return builder.toString();
    }

    public static String urlSign(String data, String service, String timeStamp, int version) {
        String signTemp = "eeseeTech"
                + "data" + data
                + "service" + service
                + "t" + timeStamp
                + "v" + version;
        return getMD5(signTemp);
    }

    public static boolean checkTime(List<TimelistBean> timelist) {
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        int current = transTimeToNum(currentTime);
        for (TimelistBean time : timelist) {
            int start = transTimeToNum(time.getStart());
            int end = transTimeToNum(time.getEnd());
            if (end == 0) {
                end = 2400;
            }
            if (current > start && current < end) {
                return true;
            }
        }
        return false;
    }

    private static int transTimeToNum(String s) {
        String[] strings = s.split(":");
        return Integer.valueOf(strings[0]) * 100 + Integer.valueOf(strings[1]);
    }


    public static String getMacAddress(Context context) {
        String macAddress = null;
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
            if (macAddress == null) {
                macAddress = "000000000000";
            }
            macAddress = macAddress.replace(":", "");
            CrashReport.putUserData(context, "mac", macAddress);
            CrashReport.putUserData(context, "versionCode", getVersionCode(context) + "");
        }
        return macAddress;
    }

    private static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        if (null != wifiManager) {
            info = wifiManager.getConnectionInfo();
        }
        return info;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getRootDirectory() {
        //File sdroot = new File("/mnt/external_sd/");
        if (hasSDCard()) {
            Log.d(Global.TAG, "Utils/getRootDirectory:hasSDCard");
            return "/mnt/external_sd/";
        } else {
            Log.d(Global.TAG, "Utils/getRootDirectory: no SD card ");
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }

    }

    public static boolean hasSDCard() {
        File mediaDirectory = new File("/mnt/external_sd/EeseeMedia");
        if (mediaDirectory.exists()) {
            return true;
        } else {
            return mediaDirectory.mkdir();
        }
    }

    public static File getOldVideoDirectory(Context context) {
        if (hasSDCard()) {
            return new File("/mnt/external_sd/nagrand");
        } else {
            return context.getExternalFilesDir("");
        }
    }

    public static String getGatewayIpAddress(Context context) {
        String IpAddress = null;
        WifiInfo wifiInfo = getWifiInfo(context);
        if (wifiInfo != null) {
            IpAddress = intToGatewayAddress(wifiInfo.getIpAddress());
        }
        return IpAddress;
    }

    private static String intToGatewayAddress(long ipInt) {
        StringBuffer sb = new StringBuffer();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".1");
        return sb.toString();
    }
}
