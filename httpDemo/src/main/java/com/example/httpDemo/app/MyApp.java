package com.example.httpDemo.app;

import android.app.Application;

import com.example.mvpdemo.R;
import com.example.httpDemo.common.Config;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class MyApp extends Application {

    private static MyApp instance;

    public static synchronized MyApp getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initLogger();
    }

    private void initLogger() {
        //DEBUG版本才打控制台log
        if (Config.DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().
                    tag(getString(R.string.app_name)).build()));
        }
    }
}
