package com.pasc.business.emrgency.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lanshaomin
 * Date: 2019/7/5 下午2:40
 * Desc:配置信息
 */
public class EmergencyConfigBean {
    @SerializedName("emergency")
    public EmergencyBean emergencyBean;

    public static class EmergencyBean {
        @SerializedName("name")
        public String name;
        @SerializedName("enable")
        public boolean enable;
        @SerializedName("callThePolice")
        public boolean callThePolice;
        @SerializedName("callThePoliceBySms")
        public boolean callThePoliceBySms;
    }
}
