package com.example.httpDemo.wdiget;

import android.content.Context;

import com.example.httpDemo.utils.LogHelper;

import io.reactivex.observers.ResourceObserver;

public abstract class BaseObserver<T> extends ResourceObserver<T> {

    private Context context;
    private String errorStr;

    public BaseObserver(Context context){
        this.context = context;
    }

    public BaseObserver(Context context,String errorStr){
        this.context = context;
        this.errorStr = errorStr;

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
