package com.pasc.business.emrgency.config;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.pasc.business.emrgency.bean.EmergencyConfigBean;
import com.pasc.business.emrgency.utils.AssetsUtil;
import com.pasc.lib.log.PascLog;

/**
 * Created by lanshaomin
 * Date: 2019/7/5 下午2:45
 * Desc:
 */
public class EmergencyManager {
    private static volatile EmergencyManager instance;
    private EmergencyConfigBean.EmergencyBean emergencyBean;

    private EmergencyManager() {

    }

    public static EmergencyManager getInstance() {
        if (instance == null) {
            synchronized (EmergencyManager.class) {
                if (instance == null) {
                    instance = new EmergencyManager();
                }
            }
        }
        return instance;
    }

    public void initConfig(Context context, String jsonPath) {
        if (TextUtils.isEmpty(jsonPath)) {
            throw new NullPointerException("请传入正确的serviceConfigPath");
        }
        try {
            EmergencyConfigBean emergencyConfigBean = new Gson().fromJson(AssetsUtil.parseFromAssets(context, jsonPath), EmergencyConfigBean.class);
            if (emergencyConfigBean != null) {
                emergencyBean = emergencyConfigBean.emergencyBean;
            }

        } catch (Exception e) {
            PascLog.v("EmergencyUrlDispatcher", e.getMessage());
        }
    }

    public boolean isEnable() {
        return emergencyBean == null || emergencyBean.enable;
    }

    public boolean showCallThePolice() {
        return emergencyBean == null || emergencyBean.callThePolice;
    }

    public boolean showCallThePoliceBySms() {
        return emergencyBean == null || emergencyBean.callThePoliceBySms;
    }
}
