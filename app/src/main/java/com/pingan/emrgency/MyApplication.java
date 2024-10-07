package com.pingan.emrgency;

import android.app.Application;

import com.pasc.lib.base.AppProxy;
import com.pasc.lib.router.RouterManager;

/**
 * 功能：
 *
 * @author lichangbao702
 * @email : lichangbao702@pingan.com.cn
 * @date : 2020/2/28
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        AppProxy.getInstance().init(this, false)
                .setIsDebug(BuildConfig.DEBUG)
                .setHost("https://massc-smt-stg.yun.city.pingan.com/")
                .setVersionName(BuildConfig.VERSION_NAME);

        initARouter();
    }



    private void initARouter() {
        RouterManager.initARouter(this, BuildConfig.DEBUG);
        RouterManager.instance().setApiGet(new com.pasc.lib.router.interceptor.ApiGet() {
            @Override
            public boolean isLogin() {
                // 路由拦截是否 已经等；
                return AppProxy.getInstance().getUserManager().isLogin();
            }

            @Override
            public boolean isCertification() {
                // 路由拦截是否 已经 实名认证；
                return AppProxy.getInstance().getUserManager().isCertified();
            }
        });
    }

}
