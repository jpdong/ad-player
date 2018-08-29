package com.eeseetech.nagrand.api;

import com.eeseetech.nagrand.entity.MessageResponseData;
import com.eeseetech.nagrand.entity.PlayResponseData;
import com.eeseetech.nagrand.entity.ResponseData;
import com.eeseetech.nagrand.entity.VideoResponseData;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ADSProvider {

    /*@GET("ads/index.php?service=Sync.Video&v=1&t={timestamp}&data={data}&sign={sign}")
    Call<VideoResponseData> videos(@Path("timestamp") String timestamp, @Path("data") String data, @Path("sign") String sign);*/

    @GET("ads/index.php?service=Sync.Video&v=1")
    Call<VideoResponseData> videos(@Query("t") String timestamp, @Query("data") String data, @Query("sign") String sign);

    @GET("ads/index.php?service=Sync.Video&v=1")
    Call<PlayResponseData> playList(@Query("t") String timestamp, @Query("data") String data, @Query("sign") String sign);

    @GET("ads/index.php?service=Log.History&v=1")
    Call<ResponseData> addPlayHistory(@Query("t") String timestamp, @Query("data") String data, @Query("sign") String sign);

    @GET("ads/index.php?service=Sync.Msg&v=1")
    Call<MessageResponseData> messages(@Query("t") String timestamp, @Query("data") String data, @Query("sign") String sign);
}
