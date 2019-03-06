package com.example.httpDemo.http;

import com.example.httpDemo.model.AppApis;
import com.example.httpDemo.common.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HttpManager {
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
//        builder.cache(new Cache(new File(Environment.getExternalStorageDirectory() + "/RxJavaDemo"),1024*1024*10));
        //cookie持久化管理
//        builder.cookieJar(new PersistentCookieJar(new SetCookieCache(),new SharedPrefsCookiePersistor(App.getInstance())));
        if (Config.DEBUG)
            addInterceptor(builder);


        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(AppApis.BASE_URL)
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


//        HttpHeaderInterceptor httpHeaderInterceptor = new HttpHeaderInterceptor.Builder().build();
        //日志拦截
        builder.addInterceptor(loggingInterceptor);
        //头部参数拦截
//        builder.addInterceptor(httpHeaderInterceptor);
//        //缓存拦截
//        builder.addInterceptor(new HttpCacheInterceptor());
//        //请求参数拦截
//        builder.addInterceptor(new CommonParamsInterceptor());
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
