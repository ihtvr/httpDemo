package com.example.httpDemo.http;

import com.example.httpDemo.common.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HttpManager {
    private static final String BASE_URL = "https://www.wanandroid.com";
    private static final int DEFAULT_TIME_OUT = 10;//超时时间5s
    private static final int DEFAULT_READ_TIME_OUT = 10;//读取时间
    private static final int DEFAULT_WRITE_TIME_OUT = 10;//读取时间
    private Retrofit mRetrofit;

    private HttpManager() {
        //OkHttpClient配置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS);
        if (Config.DEBUG)
            addInterceptor(builder);
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    /**
     * 添加各种拦截器
     *
     * @param builder
     */
    private void addInterceptor(OkHttpClient.Builder builder) {
        // 添加日志拦截器
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

    }

    //单例 饿汉模式
    private static class SingletonHolder {
        private static HttpManager httpManager = new HttpManager();
    }

    public static HttpManager getInstance() {
        return SingletonHolder.httpManager;
    }

    //获取Service实例
    public <T> T creat(Class<T> tClass) {
        return mRetrofit.create(tClass);
    }


}
