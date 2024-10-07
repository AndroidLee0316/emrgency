package com.pasc.business.emrgency.location.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.pasc.business.R;
import com.pasc.business.emrgency.location.adapter.SearchAddressAdapter;
import com.pasc.lib.base.activity.BaseActivity;
import com.pasc.lib.base.util.StatusBarUtils;
import com.pasc.lib.base.util.ToastUtils;
import com.pasc.lib.widget.toolbar.ClearEditText;

import java.util.ArrayList;

/**
 * Created by ex-huangzhiyi001 on 18/10/29.
 */
public class PoiSearchActivity extends BaseActivity {
    private static final String TAG = "PoiSearchActivity";

    private View top_view;
    private ClearEditText et_search;
    private TextView iv_back;
    private LinearLayout linearLayout;
    private RecyclerView recyclerview;
    private SwipeRefreshLayout refresh_layout;
    private TextView tv_result;
    private ArrayList<PoiItem> mList;
    private SearchAddressAdapter mSearchAddressAdapter;
    private Gson gson;
    private PoiSearch.OnPoiSearchListener mOnPoiSearchListener;
    private String tempMsg;
    private PoiSearch.Query mQuery;
    private boolean isRefresh;
    private PoiSearch mPoiSearch;
    private boolean isTriggerSearch;
    private AMapLocation location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }

    @Override
    protected int layoutResId() {
        return R.layout.emergency_activity_poisearch;
    }

    @Override
    protected void onInit(@Nullable Bundle bundle) {
        initView();
        initDatas();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            initListener();
        }
    }

    private void initView() {
//        this.top_view = (View) findViewById(R.id.top_view);
        this.et_search = (ClearEditText) findViewById(R.id.et_search);
        this.iv_back = (TextView) findViewById(R.id.iv_back);
        this.linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        this.recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        this.refresh_layout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        this.tv_result = (TextView) findViewById(R.id.tv_result);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (null != editable) {
                    if (TextUtils.isEmpty(editable)) {//没有输入则清空搜索记录
                        mList.clear();
                        mSearchAddressAdapter.setList(mList, "");
                    } else {
                        startSearch(editable.toString(), true);
                    }
                }
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String info = et_search.getText().toString().trim();
                    if (TextUtils.isEmpty(info)) {
                        ToastUtils.toastMsg("请输入搜索地点");
                    } else {
                        refresh_layout.setRefreshing(true);
                        isTriggerSearch = true;
                        startSearch(info, true);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        mSearchAddressAdapter.setOnItemClickListener(new SearchAddressAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                PoiItem poiItem = mList.get(position);
                if (null != poiItem) {//获取信息并回传上一页面
                    Intent intent = new Intent();
                    intent.putExtra(LocationActivity.SEARCH_INFO, poiItem);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        mSearchAddressAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                startSearch(tempMsg, false);
            }
        }, recyclerview);

        mOnPoiSearchListener = new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult result, int i) {
                tempMsg = et_search.getText().toString().trim();
                refresh_layout.setRefreshing(false);
                if (i == 1000) {
                    refresh_layout.setVisibility(View.VISIBLE);
                    tv_result.setVisibility(View.GONE);
                    mSearchAddressAdapter.loadMoreComplete();
                    if (result != null && result.getQuery() != null) {// 搜索poi的结果
                        //if (mQuery != null && result.getQuery() == mQuery){// 是否是同一条
                        if (isRefresh) {
                            mList.clear();
                        }
                        mList.addAll(result.getPois());// 取得第一页的poiitem数据，页数从数字0开始
                        if (result.getPois().size() < 20) {
                            mSearchAddressAdapter.loadMoreEnd();
                        }
                        if (isTriggerSearch && result.getPois().isEmpty()) {
                            refresh_layout.setVisibility(View.GONE);
                            tv_result.setVisibility(View.VISIBLE);
                        }
                        if (null != mSearchAddressAdapter) {
                            mSearchAddressAdapter.setList(mList, et_search.getText().toString().trim());
                            if (isRefresh) {
                                recyclerview.smoothScrollToPosition(0);
                            }
                        }

                        //}
                    }

                } else if (isTriggerSearch) {
                    refresh_layout.setVisibility(View.GONE);
                    tv_result.setVisibility(View.VISIBLE);
                }
                isTriggerSearch = false;
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        };

    }

    private int pageNumber = 1;

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keyWord, String city, LatLonPoint lpTemp, boolean isRefresh) throws AMapException {
        mQuery = new PoiSearch.Query(keyWord, "商务住宅|交通设施服务|风景名胜|地名地址信息", city);//第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        mQuery.setPageSize(20); // 设置每页最多返回多少条poiitem
        if (isRefresh) {
            mQuery.setPageNum(1); // 设置查第一页
        } else {
            ++pageNumber;
            mQuery.setPageNum(pageNumber);
        }

        this.isRefresh = isRefresh;

        mPoiSearch = new PoiSearch(this, mQuery);
        mPoiSearch.setOnPoiSearchListener(mOnPoiSearchListener);
        mPoiSearch.searchPOIAsyn();// 异步搜索
    }

    private void initDatas() {
        mList = new ArrayList<>();
        mSearchAddressAdapter = new SearchAddressAdapter(this, mList);
        mSearchAddressAdapter.setEnableLoadMore(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(mSearchAddressAdapter);
        refresh_layout.setEnabled(false);
        gson = new Gson();
    }

    private void startSearch(String msg, boolean isRefresh) {
        if (null != location) {
            try {
                doSearchQuery(msg, location.getCity(), new LatLonPoint(location.getLatitude(), location.getLongitude()), isRefresh);
            } catch (AMapException e) {
                e.printStackTrace();
            }
        } else {
            try {
                doSearchQuery(msg, "", null, isRefresh);
            } catch (AMapException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == location) {
            location = getIntent().getParcelableExtra(LocationActivity.LOCATION_INFO);
        }
        StatusBarUtils.setStatusBarColor(this, true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        et_search.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
                    inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(et_search, 0);
                }

            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPoiSearch) {
            mPoiSearch = null;
        }
    }

}
