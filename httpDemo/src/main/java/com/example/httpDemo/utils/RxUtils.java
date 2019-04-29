package com.example.httpDemo.utils;

import com.example.httpDemo.bean.BaseResponse;
import com.example.httpDemo.http.exception.CommonException;

import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {

    /**
     * 统一线程处理
     *
     * @param <T> 指定的泛型类型
     * @return FlowableTransformer
     */
    public static <T> FlowableTransformer<T, T> rxFlSchedulerHelper() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 统一线程处理
     *
     * @param <T> 指定的泛型类型
     * @return FlowableTransformer
     */
    public static <T> ObservableTransformer<T, T> rxObSchedulerHelper() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 接口调用结果判断
     * @param <T> 指定的泛型类型
     * @return
     */
    public static <T> ObservableTransformer<BaseResponse<T>, T> handleResult() {
        return upstream -> upstream.flatMap((Function<BaseResponse<T>, Observable<T>>) baseResponse -> {
            if(baseResponse.getErrorCode() == BaseResponse.SUCCESS
                    && baseResponse.getData() !=null
                    && CommonUtils.isNetworkConnected()){
                return createData(baseResponse.getData());
            }else {
                return Observable.error(new CommonException());
            }

        });

    }

    /**
     * 分发结果数据
     * @param t
     * @param <T>
     * @return
     */
    private static <T> Observable<T> createData(final T t) {
        return Observable.create(emitter -> {
            try {
                emitter.onNext(t);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }

        });
    }

}
