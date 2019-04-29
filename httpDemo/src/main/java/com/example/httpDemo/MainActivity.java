package com.example.httpDemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.httpDemo.bean.login.LoginData;
import com.example.httpDemo.http.HttpManager;
import com.example.httpDemo.model.UserApi;
import com.example.mvpdemo.R;
import com.example.httpDemo.utils.LogHelper;
import com.example.httpDemo.utils.RxUtils;
import com.example.httpDemo.wdiget.BaseObserver;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @BindView(R.id.btn_click)
    Button btn_click;
    @BindView(R.id.tv_des)
    TextView tv_des;


    @OnClick(R.id.btn_click)
    public void onViewClick(View view) {
        HttpManager.getInstance().creat(UserApi.class).userLogin("123", "saa")
                .compose(RxUtils.rxObSchedulerHelper())
                .compose(RxUtils.handleResult())
                .subscribeWith(new BaseObserver<LoginData>(context) {
                    @Override
                    public void onNext(LoginData loginData) {
                        LogHelper.d("Success");
                    }
                });


    }
}
