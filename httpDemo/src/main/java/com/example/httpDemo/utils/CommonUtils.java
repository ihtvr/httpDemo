package com.example.httpDemo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.httpDemo.app.MyApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;


/**
 * @author quchao
 * @date 2017/11/27
 */

public class CommonUtils {

    /**
     * 检查是否有可用网络
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    /**
     * 简单判断是否json格式
     *
     * @param json
     * @return
     */
    public static boolean isJson(String json) {
        try {
            if (json.startsWith("{")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}
