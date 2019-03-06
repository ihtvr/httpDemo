package com.example.httpDemo.model;

import com.example.httpDemo.bean.BaseResponse;
import com.example.httpDemo.http.HttpManager;
import com.example.httpDemo.bean.collect.FeedArticleListData;

import io.reactivex.Observable;


public class ArticleApi {


    //单例 饿汉模式
    private static class SingletonHolder {
        private static ArticleApi articleApi = new ArticleApi();
    }

    public static ArticleApi getInstance() {
        return SingletonHolder.articleApi;
    }

    private AppApis appApis;
    public ArticleApi(){
        appApis = HttpManager.getInstance().creat(AppApis.class);
    }


    public Observable<BaseResponse<FeedArticleListData>> getFeedArticleList(int num) {
        return appApis.getFeedArticleList(num);
    }
}
