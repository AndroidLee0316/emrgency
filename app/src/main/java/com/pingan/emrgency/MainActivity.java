package com.pingan.emrgency;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pasc.business.emrgency.EmrgencyActivity;
import com.pasc.business.emrgency.routertable.EmrgencyJumper;
import com.pasc.lib.router.BaseJumper;


/**
 * 主容器
 * Created by duyuan797 on 18/1/15.
 */
public class MainActivity extends Activity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.aaa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(EmrgencyActivity.PARAM_SEND_SMS_PHONE_NUM,"123456789");
                BaseJumper.jumpBundleARouter(EmrgencyJumper.PATH_HOMEPAGE_ONE_WORN, bundle);
            }
        });
    }
}

