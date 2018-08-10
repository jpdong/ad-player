package com.eeseetech.nagrand.api;

import android.support.annotation.Nullable;


import com.eeseetech.nagrand.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    public static final String DOWNLOAD_BASE_URL = "http://download.eeseetech.com";
    public static final String API_BASE_URL = "http://api.eeseetech.com";
    public static final String TEST_API_BASE_URL = "http://test.api.eeseetech.com";
    public static final String DEV_API_BASE_URL = "http://dev.api.eeseetech.com";

    private static OkHttpClient.Builder httpClientBuilder;
    private static Retrofit.Builder builder = new Retrofit.Builder();

    public static <T> T createService(Class<T> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <T> T createService(Class<T> serviceClass, final DownloadListener listener) {
        if (httpClientBuilder == null) {
            httpClientBuilder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }
        if ("develop".equals(BuildConfig.API_TYPE)) {
            builder.baseUrl(DEV_API_BASE_URL);
        } else if ("test".equals(BuildConfig.API_TYPE)) {
            builder.baseUrl(TEST_API_BASE_URL);
        } else {
            builder.baseUrl(API_BASE_URL);
        }
        builder.addConverterFactory(GsonConverterFactory.create());
        OkHttpClient client = httpClientBuilder.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}
