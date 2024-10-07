package com.pasc.business.emrgency.location.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.pasc.business.R;
import com.pasc.business.emrgency.location.adapter.AddressAdapter;
import com.pasc.business.emrgency.location.util.DataConversionUtils;
import com.pasc.business.emrgency.routertable.MapJumper;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.SPUtils;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.base.util.ToastUtils;
import com.pasc.lib.nearby.map.base.Locator;
import com.pasc.lib.widget.toolbar.PascToolbar;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by ex-huangzhiyi001 on 18/10/27.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
@Route(path = MapJumper.NEARBY_MAP_LOCATION)
public class LocationActivity extends BaseActivity {

    public static final String SEARCH_INFO = "search_info";
    public static final String LOCATION_INFO = "location_info";
    private PascToolbar toolbar_location;
    private FrameLayout et_search;
    private TextureMapView mapView;
    private Guideline guideline5;
    private RecyclerView recyclerview;
    private ImageView iv_location;
    private View guideline;
    private ImageView iv_center_location;
    private ConstraintLayout container;
    private AMap aMap;

    private ImageButton mCloseImageButton;
    private TextView mRightTextButton;
    private AddressAdapter mAddressAdapter;
    public static final String LOCATION_DATA = "location_data";
    private boolean isSearchData;
    private float zoom = 14f;//地图缩放级别
    private Marker mSelectByListMarker;

    private GeocodeSearch.OnGeocodeSearchListener mOnGeocodeSearchListener = null;
    private ObjectAnimator mTransAnimator;
    private int searchAllPageNum = 1;//Poi搜索最大页数，可应用于上拉加载更多
    private PoiSearch.Query mQuery;
    private PoiSearch mPoiSearch;
    private int searchNowPageNum = 1;
    private ArrayList<PoiItem> mList;
    private Gson gson;
    private int SEARCHREQUESTCODE = 1001;
    private PoiItem userSelectPoiItem;
    private OnPoiSearchListener mOnPoiSearchListener;
    private String city = "深圳市";
    private float latitude = 0.0f;//后面添加 常熟市 的经纬度;
    private float longitude = 0.0f;
    private AMapLocationClient locationClient;
    private AMapLocationListener mAMapLocationListener;
    private AMapLocation location;

    private int i1 = 1;
    private float y2 = 0f;
    private float dy = 0f;
    private float y1 = 0f;
    private boolean isClose = false;
    private ConstraintSet constraintSet = new ConstraintSet();
    private ValueAnimator mClose = ObjectAnimator.ofFloat(0.6f, 0.4f);
    private ValueAnimator mOpen = ObjectAnimator.ofFloat(0.4f, 0.6f);
    private boolean isFromSetting = false;
    private Marker mLocationGpsMarker;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    private String DEFAULT_CITY = "深圳市";
    private float DEFAULT_LATITUDE = 22.533771f;//大概位置
    private float DEFAULT_LONGITUDE = 114.055741f;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
       
    }

    @Override
    protected int layoutResId() {

        return R.layout.emergency_activity_location;
    }
    @Override
    protected void onInit(@Nullable Bundle savedInstanceState) {
        getSpData();
        initView();
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        initMapRecyclerView();
        initData();
        checkLocationPermission();
    }

    private void checkLocationPermission () {
        Locator.prepareLocation(this, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Locator.PrepareStatus>() {
                    @Override
                    public void accept (Locator.PrepareStatus prepareStatus) throws Exception {
                        if (prepareStatus == Locator.PrepareStatus.PERMISSION_GRANTED){
                            startLocation();
                        } else if (prepareStatus == Locator.PrepareStatus.OPEN_GPS_SETTING ||
                                prepareStatus == Locator.PrepareStatus.OPEN_PERMISSION_SETTING){
                            isFromSetting = true;
                        } else if (prepareStatus == Locator.PrepareStatus.CANCEL_DIALOG ||
                                prepareStatus == Locator.PrepareStatus.PERMISSION_NOT_GRANTED){
                            ToastUtils.toastMsg("请检查GPS是否打开");
                            doWhenNoPermission();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept (Throwable throwable) throws Exception {
                        ToastUtils.toastMsg("无法获取定位信息");
                        doWhenNoPermission();
                        Log.e(TAG, "accept: throwable " + throwable.getMessage());
                    }
                });
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption () {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true);//可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setMockEnable(true);//如果您希望位置被模拟，请通过setMockEnable(true);方法开启允许位置模拟
        return mOption;
    }

    /**
     * 初始化定位
     */
    private void initLocation () throws Exception {
        if (null == locationClient){
            //初始化client
            locationClient = new AMapLocationClient(this.getApplicationContext());
            //设置定位参数
            locationClient.setLocationOption(getDefaultOption());
            // 设置定位监听
            locationClient.setLocationListener(mAMapLocationListener);
        }
    }

    /**
     * 开始定位
     */
    private void startLocation () throws Exception {
        initLocation();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
        //showLoading();
    }

    /**
     * 当定位成功需要做的事情
     */
    private void doWhenLocationSuccess () throws AMapException {
        isSearchData = false;
        userSelectPoiItem = DataConversionUtils.changeToPoiItem(location);
        doSearchQuery(true, "", location.getCity(), new LatLonPoint(location.getLatitude(), location.getLongitude()));
        moveMapCamera(location.getLatitude(), location.getLongitude());
        refleshLocationMark(location.getLatitude(), location.getLongitude());
        iv_location.setImageResource(R.drawable.emergency_ic_location_gps_black);
    }

    /**
     * 当定位成功需要做的事情
     */
    private void doWhenNoPermission () throws AMapException {
        isSearchData = false;
//        userSelectPoiItem = DataConversionUtils.changeToPoiItem(location)
        doSearchQuery(true, "商务住宅|交通设施服务|风景名胜|地名地址信息",
                DEFAULT_CITY, new LatLonPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
        moveMapCamera(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        refleshLocationMark(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        iv_location.setImageResource(R.drawable.emergency_ic_location_gps_black);
    }

    /**
     * 刷新地图标志物gps定位位置
     *
     * @param latitude
     * @param longitude
     */
    private void refleshLocationMark (double latitude, double longitude) {
        if (mLocationGpsMarker == null){
            mLocationGpsMarker = aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.emergency_ic_location_blue)))
                    .draggable(true)
                    .anchor(0.5f, 0.8f));

        }
        mLocationGpsMarker.setPosition(new LatLng(latitude, longitude));

    }

    private void initData () {
        mList = new ArrayList<>();

        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);//是否显示地图中放大缩小按钮
        mUiSettings.setLogoBottomMargin(-100);
        mUiSettings.setMyLocationButtonEnabled(false);  // 是否显示默认的定位按钮
        mUiSettings.setScaleControlsEnabled(true);//是否显示缩放级别
        aMap.setMyLocationEnabled(false); // 是否可触发定位并显示定位层

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mTransAnimator = ObjectAnimator.ofFloat(iv_center_location, "translationY", 0f, -80f, 0f);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mTransAnimator.setDuration(800);
        }
        gson = new Gson();

        initListener();
    }

    private void getSpData() {
        try {
            DEFAULT_LATITUDE = (float) SPUtils.getInstance().getParam(SPUtils.SP_KEY_LATITUDE, DEFAULT_LATITUDE);
            DEFAULT_LONGITUDE = (float) SPUtils.getInstance().getParam(SPUtils.SP_KEY_LONGITUDE, DEFAULT_LONGITUDE);
            DEFAULT_CITY= (String) SPUtils.getInstance().getParam(SPUtils.CURRENT_CITY, DEFAULT_CITY);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void initView () {
        this.toolbar_location = (PascToolbar) findViewById(R.id.nearby_title_bar);
        this.et_search = (FrameLayout) findViewById(R.id.et_search);
        this.mapView = (TextureMapView) findViewById(R.id.mapview);
        this.guideline5 = (Guideline) findViewById(R.id.guideline5);
        this.recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        this.iv_location = (ImageView) findViewById(R.id.iv_location);
        this.guideline = (View) findViewById(R.id.guideline);
        this.iv_center_location = (ImageView) findViewById(R.id.iv_center_location);
        this.container = (ConstraintLayout) findViewById(R.id.container);

        toolbar_location.setTitle("位置");
        mCloseImageButton = toolbar_location.addCloseImageButton();
        mRightTextButton = toolbar_location.addRightTextButton("确定");
        mRightTextButton.setTextColor(Color.BLACK);
        toolbar_location.setBackgroundColor(Color.WHITE);
        mCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                onBackPressed();
            }
        });
        mRightTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                PoiItem poiItem = null;
                String title = "";
                String address = "";
                if (null != mList && 0 < mList.size() && null != mAddressAdapter){
                    int position = mAddressAdapter.getSelectPositon();
                    if (position < 0){
                        position = 0;
                    } else if (position > mList.size()){
                        position = mList.size();
                    }
                    poiItem = mList.get(position);
                    String msg = "发送：" + poiItem.getTitle() + "  " + poiItem.getSnippet() + "  " + "纬度："
                            + poiItem.getLatLonPoint().getLatitude() +
                            " 城市: " + poiItem.getCityName() + poiItem.getProvinceName() + " " + "经度：" + poiItem.getLatLonPoint().getLongitude();
                    //Log.e(TAG, "mRightTextButton onClick: " + msg);

                    title = poiItem.getTitle();
                    if (!TextUtils.isEmpty(poiItem.getSnippet()) && poiItem.getSnippet().contains(poiItem.getProvinceName())) {
                        address=poiItem.getSnippet();
                    } else {
                        address= poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet();

                    }

                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("title", title);
                bundle.putString("address", address);
                intent.putExtra(LOCATION_DATA, bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        //mCloseImageButton.setBackgroundColor(Color.TRANSPARENT);
        toolbar_location.enableUnderDivider(false);
    }

    /**
     * 刷新地图标志物选中列表的位置
     *
     * @param latitude
     * @param longitude
     */
    private void refreshSelectByListMark (double latitude, double longitude) {
        if (mSelectByListMarker == null){
            mSelectByListMarker = aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.emergency_ic_location_red)))
                    .draggable(true));
        }
        mSelectByListMarker.setPosition(new LatLng(latitude, longitude));
        if (!mSelectByListMarker.isVisible()){
            mSelectByListMarker.setVisible(true);
        }
    }

    /**
     * 通过经纬度获取当前地址详细信息，逆地址编码
     *
     * @param latitude
     * @param longitude
     */
    private void getAddressInfoByLatLong (double latitude, double longitude) throws AMapException {
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        /*
        point - 要进行逆地理编码的地理坐标点。
        radius - 查找范围。默认值为1000，取值范围1-3000，单位米。
        latLonType - 输入参数坐标类型。包含GPS坐标和高德坐标。 可以参考RegeocodeQuery.setLatLonType(String)
        */
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latitude, longitude), 3000f, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        geocodeSearch.setOnGeocodeSearchListener(mOnGeocodeSearchListener);
    }

    /**
     * 移动动画
     */
    private void startTransAnimator () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (null != mTransAnimator && !mTransAnimator.isRunning()){
                mTransAnimator.start();
            }
        }
    }

    /**
     * 把地图画面移动到定位地点(使用moveCamera方法没有动画效果)
     *
     * @param latitude
     * @param longitude
     */
    private void moveMapCamera (double latitude, double longitude) {
        if (null != aMap){
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        }
    }

    /**
     * 停止定位
     */
    private void stopLocation () {
        if (null != locationClient){
            locationClient.stopLocation();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener () {
        //监测地图画面的移动
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange (CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish (CameraPosition cameraPosition) {
                if (null != cameraPosition && isSearchData){
                    //showLoading();
                    iv_location.setImageResource(R.drawable.emergency_ic_location_gps_black);
                    zoom = cameraPosition.zoom;
                    if (null != mSelectByListMarker){
                        mSelectByListMarker.setVisible(false);
                    }
                    try {
                        getAddressInfoByLatLong(cameraPosition.target.latitude, cameraPosition.target.longitude);
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                    startTransAnimator();
                }
                if (!isSearchData){
                    isSearchData = true;
                }
            }
        });
        //设置触摸地图监听器
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick (LatLng latLng) {
                isSearchData = true;
            }
        });
        //Poi搜索监听器
        mOnPoiSearchListener = new OnPoiSearchListener();

        //逆地址搜索监听器

        mOnGeocodeSearchListener = new GeocodeSearch.OnGeocodeSearchListener() {

            @Override
            public void onRegeocodeSearched (RegeocodeResult regeocodeResult, int i) {
                //dismissLoading();
                if (i == 1000){
                    if (regeocodeResult != null){
                        userSelectPoiItem = DataConversionUtils.changeToPoiItem(regeocodeResult);
                        city = regeocodeResult.getRegeocodeAddress().getCity();
                        latitude = (float) regeocodeResult.getRegeocodeQuery().getPoint().getLatitude();
                        longitude = (float) regeocodeResult.getRegeocodeQuery().getPoint().getLongitude();
                        if (null != mList){
                            mList.clear();
                        }
                        mList.addAll(regeocodeResult.getRegeocodeAddress().getPois());
                        if (regeocodeResult.getRegeocodeAddress().getPois().isEmpty()){
                            ToastUtils.toastMsg("附近无可用地址");
                        }
                        if (null != userSelectPoiItem){
                            mList.add(0, userSelectPoiItem);
                        }
                        mAddressAdapter.setList(mList);
                        recyclerview.smoothScrollToPosition(0);
                    }
                } else{
                    String msg;
                    switch (i){
                    case 1802:
                        msg = "请检查网络状况是否良好";
                        break;
                    case 1804:
                        msg = "请检查网络连接是否畅通";
                        break;
                    case 1806:
                        msg = "请检查网络状况以及网络的稳定性";
                        break;
                    default:
                        msg = "无法获取位置信息";
                        break;
                    }
                    ToastUtils.toastMsg(msg);
                }
            }

            @Override
            public void onGeocodeSearched (GeocodeResult geocodeResult, int i) {

            }
        };

        //gps定位监听器
        mAMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged (AMapLocation loc) {
                //dismissLoading();
                if (null != loc){
                    stopLocation();
                    if (loc.getErrorCode() == 0){//可在其中解析amapLocation获取相应内容。
                        location = loc;
                        city = loc.getCity();
                        latitude = (float) loc.getLatitude();
                        longitude = (float) loc.getLongitude();
                        try {
                            doWhenLocationSuccess();
                        } catch (AMapException e) {
                            e.printStackTrace();
                        }
                    } else{
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + loc.getErrorCode() + ", errInfo:"
                                + loc.getErrorInfo());
//                        ToastUtils.toastMsg("无法获取定位")
                        // ToastUtils.toastMsg("定位失败," + Locator.mapperGaoDeErrorMessage(loc.getErrorCode()));
                    }
                }
            }
        };
        //recycleview列表监听器
        mAddressAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick (BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.linearLayout2){

                    //选定item
                    try{
                        mAddressAdapter.setSelectPosition(position);
                        isSearchData = false;
                        iv_location.setImageResource(R.drawable.emergency_ic_location_gps_black);
                        moveMapCamera(mList.get(position).getLatLonPoint().getLatitude(),
                                mList.get(position).getLatLonPoint().getLongitude());
                        refreshSelectByListMark(mList.get(position).getLatLonPoint().getLatitude(),
                                mList.get(position).getLatLonPoint().getLongitude());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        mAddressAdapter.setEnableLoadMore(true);
        mAddressAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested () {
                try {
                    doSearchQuery(false, "", city, new LatLonPoint(latitude, longitude));
                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }
        }, recyclerview);

        //控件点击监听器
        iv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                iv_location.setImageResource(R.drawable.emergency_ic_location_gps_green);
                if (null != mSelectByListMarker){
                    mSelectByListMarker.setVisible(false);
                }
                checkLocationPermission();
            }
        });
        et_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(LocationActivity.this, PoiSearchActivity.class);
                intent.putExtra(LOCATION_INFO, location);
                startActivityForResult(intent, SEARCHREQUESTCODE);
            }
        });
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled (RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (dy < 0 && isClose && !recyclerview.canScrollVertically(-1)){
                        open();
                    }
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mClose.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate (ValueAnimator animation) {
                    constraintSet.setGuidelinePercent(R.id.guideline5, (Float) animation.getAnimatedValue());
                    constraintSet.applyTo(container);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOpen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate (ValueAnimator animation) {
                    constraintSet.setGuidelinePercent(R.id.guideline5, (Float) animation.getAnimatedValue());
                    constraintSet.applyTo(container);
                }
            });
        }
        constraintSet.clone(container);
        recyclerview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event) {
                switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    y1 = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    y2 = event.getRawY();
                    dy = y2 - y1;
                    if (i1 >= 1){
                        y1 = y2;
                        i1--;
                        return false;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        if (Math.abs(dy) < ViewConfiguration.get(LocationActivity.this).getScaledEdgeSlop()){
                            return false;
                        }
                    }

                    if (dy < 0 && !isClose){
                        close();
                        i1 = 1;
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        if (dy > 0 && isClose && !recyclerview.canScrollVertically(-1)){
                            open();
                            i1 = 1;
                        }
                    }
                    y1 = y2;
                    break;
                case MotionEvent.ACTION_UP:
                    i1 = 1;
                    break;
                default:
                    break;
                }
                return false;
            }
        });
    }

    private void close () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mClose.isRunning() || mOpen.isRunning()){
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mClose.start();
        }
        isClose = true;
    }

    private void open () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mOpen.isRunning() || mClose.isRunning()){
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOpen.start();
        }
        isClose = false;
    }

    private void initMapRecyclerView () {
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        mAddressAdapter = new AddressAdapter(this, new ArrayList<PoiItem>());
        recyclerview.setAdapter(mAddressAdapter);

//        ViewGroup.LayoutParams params =
//                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils
//                        .dp2px(50));
//        View footView = View.inflate(getActivity(), R.layout.item_foot_view, null);
//        footView.setLayoutParams(params);
        //mAddressAdapter.removeAllFooterView();
        //mAddressAdapter.addFooterView(footView);
    }

    @Override
    protected void onStart () {
        super.onStart();
        if (isFromSetting){
            isFromSetting = false;
            checkLocationPermission();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        mapView.onResume();
        StatusBarUtils.setStatusBarColor(this, true);

    }

    @Override
    protected void onPause () {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory () {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        stopLocation();
        mapView.onDestroy();
        if (null != mPoiSearch){
            mPoiSearch = null;
        }
        if (null != gson){
            gson = null;
        }
        if (null != locationClient){
            locationClient.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    /**
     * 开始进行poi搜索
     *
     * @param isReflsh 是否为刷新数据
     * @param keyWord
     * @param city
     * @param lpTemp
     */
    private void doSearchQuery (boolean isReflsh, String keyWord, String city, LatLonPoint lpTemp) throws AMapException {
        mQuery = new PoiSearch.Query(keyWord, "商务住宅|交通设施服务|风景名胜|地名地址信息", city);//第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        mQuery.setPageSize(20);// 设置每页最多返回多少条poiitem
        if (isReflsh){
            searchNowPageNum = 1;
        } else{
            searchNowPageNum++;
        }
        if (searchNowPageNum > searchAllPageNum){
            return;
        }
        mQuery.setPageNum(searchNowPageNum);// 设置查第一页


        mPoiSearch = new PoiSearch(this, mQuery);
        mOnPoiSearchListener.isRefresh(isReflsh);
        mPoiSearch.setOnPoiSearchListener(mOnPoiSearchListener);
        if (lpTemp != null){
            mPoiSearch.setBound(new PoiSearch.SearchBound(lpTemp, 20000, true));  //该范围的中心点-----半径，单位：米-----是否按照距离排序
        }
        mPoiSearch.searchPOIAsyn();// 异步搜索
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data && SEARCHREQUESTCODE == requestCode){
            userSelectPoiItem = data.getParcelableExtra(SEARCH_INFO);
            try{
                if (null != userSelectPoiItem){
                    isSearchData = false;
                    doSearchQuery(true, "", location.getCity(), userSelectPoiItem.getLatLonPoint());
                    moveMapCamera(userSelectPoiItem.getLatLonPoint().getLatitude(), userSelectPoiItem.getLatLonPoint().getLongitude());
                    //refleshMark(userSelectPoiItem.getLatLonPoint().getLatitude(), userSelectPoiItem.getLatLonPoint().getLongitude());
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    //重写Poi搜索监听器，可扩展上拉加载数据，下拉刷新
    class OnPoiSearchListener implements PoiSearch.OnPoiSearchListener {

        private boolean isRefresh;

        void isRefresh (boolean isRefrash) {
            this.isRefresh = isRefrash;
        }

        @Override
        public void onPoiSearched (PoiResult result, int i) {
            if (i == 1000){
                if (result != null && result.getQuery() != null){// 搜索poi的结果
                    searchAllPageNum = result.getPageCount();
                    //if (result.getQuery() == mQuery){// 是否是同一条
                        if (isRefresh && null != mList){
                            mList.clear();
                            if (null != userSelectPoiItem
                                    && !TextUtils.isEmpty(userSelectPoiItem.getSnippet())
                                    && !TextUtils.isEmpty(userSelectPoiItem.getProvinceName())){
                                mList.add(0, userSelectPoiItem);
                            }
                        }
                        mList.addAll(result.getPois());// 取得第一页的poiitem数据，页数从数字0开始
                        if (result.getPois().size() < 20){
                            mAddressAdapter.loadMoreEnd();
                        } else{
                            mAddressAdapter.loadMoreComplete();
                        }
                        if (null != mAddressAdapter){
                            mAddressAdapter.setList(mList);
                            if (isRefresh){
                                recyclerview.smoothScrollToPosition(0);
                            }

                        }
                   // }
                }
            } else{
                if (searchNowPageNum > 1){
                    mAddressAdapter.loadMoreFail();
                }
            }
        }

        @Override
        public void onPoiItemSearched (PoiItem poiItem, int i) {

        }
    }

}
