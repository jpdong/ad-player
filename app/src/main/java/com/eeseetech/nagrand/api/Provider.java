package com.eeseetech.nagrand.api;

import android.support.annotation.Nullable;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.Utils;
import com.eeseetech.nagrand.entity.VideoInfo;
import com.eeseetech.nagrand.entity.HistoryRequestData;
import com.eeseetech.nagrand.entity.PlayResponseData;
import com.eeseetech.nagrand.entity.ResponseData;
import com.eeseetech.nagrand.entity.Result;
import com.eeseetech.nagrand.entity.VideoRequestData;
import com.eeseetech.nagrand.entity.VideoResponseData;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Response;

import static com.eeseetech.nagrand.entity.Result.FAIL;
import static com.eeseetech.nagrand.entity.Result.NO_CHANGE;
import static com.eeseetech.nagrand.entity.Result.SUCCESS;

public class Provider {

    public Provider() {

    }

    public Result<Map<String, VideoInfo>> videos(String tag, String gateAddress, String macAddress) {
        VideoRequestData requestData = new VideoRequestData(tag, 0, gateAddress, macAddress, 2);
        String data = new Gson().toJson(requestData);
        ADSProvider adsProvider = ServiceGenerator.createService(ADSProvider.class);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String sign = Utils.urlSign(data, "Sync.Video", timeStamp, 1);
        Call<VideoResponseData> call = adsProvider.videos(timeStamp, data, sign);
        Response<VideoResponseData> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {
            VideoResponseData responseData = response.body();
            Result<Map<String, VideoInfo>> result = new Result<>();
            if (responseData.code == 0) {
                result.code = SUCCESS;
                VideoResponseData.DataBean dataBean = responseData.data;
                if (dataBean.retcode == 0) {
                    result.code = NO_CHANGE;
                } else {
                    Map<String, VideoInfo> map = new HashMap<String, VideoInfo>();
                    for (VideoInfo videoInfo : dataBean.fileList) {
                        map.put(videoInfo.getFilename(), videoInfo);
                    }
                    result.data = map;
                    result.tag = dataBean.tag;
                }
            } else {
                result.code = FAIL;
                result.msg = responseData.msg;
            }
            return result;
        } else {
            return null;
        }
    }

    public Result<List<String>> playList(String tag, String gateAddress, String macAddress) {
        VideoRequestData requestData = new VideoRequestData(tag, 1, gateAddress, macAddress, 2);
        String data = new Gson().toJson(requestData);
        ADSProvider adsProvider = ServiceGenerator.createService(ADSProvider.class);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String sign = Utils.urlSign(data, "Sync.Video", timeStamp, 1);
        Call<PlayResponseData> call = adsProvider.playList(timeStamp, data, sign);
        Response<PlayResponseData> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {
            PlayResponseData responseData = response.body();
            Result<List<String>> result = new Result<>();
            if (responseData.code == 0) {
                result.code = SUCCESS;
                PlayResponseData.PlayListData dataBean = responseData.data;
                if (dataBean.retcode == 0) {
                    result.code = NO_CHANGE;
                } else {
                    result.data = dataBean.snapshot;
                    result.tag = dataBean.tag;
                }
            } else {
                result.code = FAIL;
                result.msg = responseData.msg;
            }
            return result;
        } else {
            return null;
        }
    }

    public Result<Boolean> addPlayHistory(long delay, String gateAddress, String macAddress, String fileName) {
        HistoryRequestData requestData = new HistoryRequestData(delay, gateAddress, macAddress, fileName);
        String data = new Gson().toJson(requestData);
        ADSProvider adsProvider = ServiceGenerator.createService(ADSProvider.class);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String sign = Utils.urlSign(data, "Log.History", timeStamp, 1);
        Call<ResponseData> call = adsProvider.addPlayHistory(timeStamp, data, sign);
        Response<ResponseData> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {
            ResponseData responseData = response.body();
            Result<Boolean> result = new Result<>();
            if (responseData.code == 0) {
                result.code = SUCCESS;
                result.data = true;
            } else {
                result.code = FAIL;
                result.data = false;
                result.msg = responseData.msg;
            }
            return result;
        } else {
            return null;
        }
    }

    public void downloadVideo(String fileName, final DownloadListener<File> downloadListener) {
        final File destFile = new File(Global.LOCAL_MEDIA_DIR + fileName);
        if (destFile.exists()) {
            destFile.delete();
        }
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            downloadListener.fail(e);
            e.printStackTrace();
            return;
        }
        final ProgressResponse.ProgressListener listener = new ProgressResponse.ProgressListener() {
            @Override
            public void progress(long bytesRead, long contentLength) {
                //Log.d(Global.TAG, "Provider/progress:");
                float progress = bytesRead * 1f / contentLength;
                downloadListener.process(progress);
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Response response = chain.proceed(chain.request());
                        return response.newBuilder().body(new ProgressResponse(response.body(), listener)).build();
                    }
                }).build();
        Request request = new Request.Builder()
                .url(Global.REMOTE_MEDIA_SITE + fileName)
                //.url("https://images.pexels.com/photos/958363/pexels-photo-958363.jpeg?dl&fit=crop&crop=entropy&w=1280&h=853")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                downloadListener.fail(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                //Log.d(Global.TAG, "RemoteRepository/onResponse1:" + response.body().contentLength());
                //Log.d(Global.TAG, "RemoteRepository/onResponse2:" + response.body().string());
                InputStream inputStream = response.body().byteStream();
                FileOutputStream outputStream = new FileOutputStream(destFile);
                byte[] bytes = new byte[1024];
                int length = -1;
                while ((length = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, length);
                }
                //Log.d(Global.TAG, "RemoteRepository/onResponse3:" + destFile.length());
                downloadListener.done(destFile);
            }
        });
    }

    static class ProgressResponse extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressResponse.ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponse(ResponseBody responseBody, ProgressResponse.ProgressListener listener) {
            this.responseBody = responseBody;
            this.progressListener = listener;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    if (bytesRead != -1) {
                        totalBytesRead += bytesRead;
                        progressListener.progress(totalBytesRead, responseBody.contentLength());
                    }
                    return bytesRead;
                }
            };
        }

        interface ProgressListener {
            void progress(long bytesRead, long contentLength);
        }
    }
}
