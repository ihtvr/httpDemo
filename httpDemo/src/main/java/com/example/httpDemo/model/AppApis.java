package com.example.httpDemo.model;

import com.example.httpDemo.bean.BaseResponse;
import com.example.httpDemo.bean.collect.FeedArticleListData;
import com.example.httpDemo.bean.login.LoginData;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AppApis  {
    String BASE_URL  ="http://www.wanandroid.com";

    @GET("article/list/{num}/json")
    Observable<BaseResponse<FeedArticleListData>> getFeedArticleList(@Path("num") int num);

    @POST("user/login")
    @FormUrlEncoded
    Observable<BaseResponse<LoginData>> userLogin(@Field("userName") String userName,@Field("password") String password);

}
