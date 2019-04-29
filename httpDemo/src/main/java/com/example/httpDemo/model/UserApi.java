package com.example.httpDemo.model;

import com.example.httpDemo.bean.BaseResponse;
import com.example.httpDemo.bean.login.LoginData;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserApi {

    @POST("user/login")
    @FormUrlEncoded
    Observable<BaseResponse<LoginData>> userLogin(@Field("userName") String userName, @Field("password") String password);
}
