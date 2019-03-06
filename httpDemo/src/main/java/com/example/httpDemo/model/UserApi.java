package com.example.httpDemo.model;

import com.example.httpDemo.bean.BaseResponse;
import com.example.httpDemo.bean.collect.FeedArticleListData;
import com.example.httpDemo.bean.login.LoginData;
import com.example.httpDemo.http.HttpManager;

import io.reactivex.Observable;

public class UserApi {

    //单例 饿汉模式
    private static class SingletonHolder {
        private static UserApi userApi = new UserApi();
    }

    public static UserApi getInstance() {
        return UserApi.SingletonHolder.userApi;
    }

    private AppApis appApis;
    public UserApi(){
        appApis = HttpManager.getInstance().creat(AppApis.class);
    }


    public Observable<BaseResponse<LoginData>> userLogin(String username, String password) {
        return appApis.userLogin(username,password);
    }
}
