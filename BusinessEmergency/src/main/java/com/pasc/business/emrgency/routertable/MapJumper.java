package com.pasc.business.emrgency.routertable;

import android.app.Activity;
import android.os.Bundle;

import com.pasc.lib.router.BaseJumper;

/**
 * 地图
 * Created by ex-huangzhiyi001 on 18/9/21.
 */
public class MapJumper extends BaseJumper {

    /**
     * 附近主页
     */
    public static final String NEARBY_MAP_LOCATION = "/emrgency/map/location";

    /**
     * 跳转到附近
     */

    public static void jumpLocationActivity (Activity activity, int requestCode, Bundle bundle) {
        jumpBundleARouter(NEARBY_MAP_LOCATION, activity,requestCode, bundle);
    }
}
