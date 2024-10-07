package com.pasc.business.emrgency;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.android.flexbox.FlexboxLayout;
import com.pasc.business.R;
import com.pasc.business.emrgency.config.EmergencyManager;
import com.pasc.business.emrgency.routertable.EmrgencyJumper;
import com.pasc.business.emrgency.routertable.MapJumper;
import com.pasc.lib.base.AppProxy;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.lbs.LbsManager;
import com.pasc.lib.lbs.location.LocationException;
import com.pasc.lib.lbs.location.PascLocationListener;
import com.pasc.lib.lbs.location.bean.PascLocationData;
import com.pasc.lib.widget.toolbar.PascToolbar;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 功能：
 * <p>
 * created by zoujianbo345
 * data : 2018/10/26
 */
@Route(path = EmrgencyJumper.PATH_HOMEPAGE_ONE_WORN)
public class EmrgencyActivity extends BaseActivity implements View.OnClickListener {
    public static final String[] PERMISSIONS_LOCATION = {"android.permission.ACCESS_COARSE_LOCATION"};
    public PascToolbar toolbar;
    private String address;
    private String type;
    private PascLocationListener pascLocationListener;

    FlexboxLayout container_type;
    TextView tv_emer_address;
    TextView tv_emer_aio;
    CheckBox lastBox;
    Button btnEmerPhone;
    Button btnEmerSms;

    /**
     * 短信报警号码可从外面通过bundle传入
     */
    public static final String PARAM_SEND_SMS_PHONE_NUM = "sendSmsPhoneNum";
    /**
     * 短信报警号码
     */
    private String sendSmsPhoneNum = "12110775";

    @Override
    protected void onInit(@Nullable Bundle bundle) {
        if (!EmergencyManager.getInstance().isEnable()) {
            finish();
            return;
        }
        StatusBarUtils.setStatusBarColor(this, true);

        if (getIntent() != null && getIntent().hasExtra(PARAM_SEND_SMS_PHONE_NUM)){
            String phoneNUm = getIntent().getStringExtra(PARAM_SEND_SMS_PHONE_NUM);
            if (!TextUtils.isEmpty(phoneNUm)){
                sendSmsPhoneNum = phoneNUm;
            }
        }
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarUtils.setStatusBarColor(this, true);
    }


    @Override
    protected int layoutResId() {
        return R.layout.emergency_activity_emrgency;
    }


    private void initView() {
        btnEmerSms = findViewById(R.id.bt_emer_sms);
        btnEmerPhone = findViewById(R.id.bt_emer_phone);
        btnEmerSms.setOnClickListener(this);
        btnEmerPhone.setOnClickListener(this);
        findViewById(R.id.ll_location).setOnClickListener(this);
        findViewById(R.id.rl_emer_getlocation).setOnClickListener(this);
        toolbar = findViewById(R.id.toolbar);
        container_type = findViewById(R.id.container_type);
        tv_emer_address = findViewById(R.id.tv_emer_address);
        tv_emer_aio = findViewById(R.id.tv_emer_aio);
        if (toolbar != null) {
            toolbar.enableUnderDivider(true);
            toolbar.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        btnEmerPhone.setVisibility(EmergencyManager.getInstance().showCallThePolice() ? View.VISIBLE : View.GONE);
        btnEmerSms.setVisibility(EmergencyManager.getInstance().showCallThePoliceBySms() ? View.VISIBLE : View.GONE);
    }

    private void initData() {

        pascLocationListener = getPascLocationListener();
        LbsManager.getInstance().doLocation(0, pascLocationListener);

        for (int i = 0; i < container_type.getChildCount(); i++) {
            View view1 = container_type.getChildAt(i);
            if (view1 instanceof CheckBox) {
                final CheckBox checkBox = (CheckBox) view1;
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            if (lastBox != null) {
                                lastBox.setChecked(false);
                            }
                            type = checkBox.getText().toString();
                            lastBox = checkBox;
                        } else {
                            type = "";
                            lastBox = null;
                        }
                    }
                });
            }
        }
    }

    @NonNull
    private PascLocationListener getPascLocationListener() {
        return new PascLocationListener() {
            @Override
            public void onLocationSuccess(PascLocationData pascLocationData) {
                if (pascLocationData != null){
                    setLocation(pascLocationData.getAoiName() + "\n" + pascLocationData.getAddress());
                }

                LbsManager.getInstance().stopLocation(0, pascLocationListener);

            }

            @Override
            public void onLocationFailure(LocationException e) {
                LbsManager.getInstance().stopLocation(0, pascLocationListener);

            }
        };
    }

    /**
     * 根据lbs获取数据 设置定位地址
     *
     * @param address
     */
    private void setLocation(String locationAddress) {
        address = locationAddress;
        tv_emer_aio.post(new Runnable() {
            @Override
            public void run() {
                tv_emer_aio.setVisibility(View.GONE);
                tv_emer_address.setVisibility(View.VISIBLE);
                tv_emer_address.setText(address);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_emer_phone) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "110"));//跳转到拨号界面，同时传递电话号码
            startActivity(dialIntent);
        } else if (view.getId() == R.id.bt_emer_sms) {

            StringBuilder builder = new StringBuilder();
            builder.append("时间").append(new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒",
                    Locale.CHINA).format(new Date()));
            if (!TextUtils.isEmpty(address)) {
                builder.append(" 地点：" + address);
            }
            if (!TextUtils.isEmpty(type)) {
                builder.append(" 类型：" + type);
            }
            try {
                String userName = AppProxy.getInstance().getUserManager().getUserName();
                if (!TextUtils.isEmpty(userName)) {
                    builder.append(" 报警人：" + userName);
                }
                String idCard = AppProxy.getInstance().getUserManager().getUserInfo().getIdCard();
                if (!TextUtils.isEmpty(idCard)) {
                    builder.append(" 身份证号：" + idCard);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            sendSMS(sendSmsPhoneNum, builder.toString());
        } else if (view.getId() == R.id.ll_location) {
            MapJumper.jumpLocationActivity(EmrgencyActivity.this, 100, null);
        }
    }

    /**
     * 调起系统发短信功能
     *
     * @param phoneNumber
     * @param message
     */
    private void sendSMS(String phoneNumber, String message) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle bundleExtra = data.getBundleExtra("location_data");
            if (bundleExtra != null) {
                final String titleResult = bundleExtra.getString("title", "");
                final String addressResult = bundleExtra.getString("address", "");
                String totolAddress = titleResult + "\n" + addressResult;
                if (!TextUtils.isEmpty(titleResult) || !TextUtils.isEmpty(addressResult)) {
                    setLocation(totolAddress);
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LbsManager.getInstance().stopLocation(0, pascLocationListener);
    }
}
