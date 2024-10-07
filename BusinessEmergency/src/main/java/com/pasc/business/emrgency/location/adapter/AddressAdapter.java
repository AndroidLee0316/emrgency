package com.pasc.business.emrgency.location.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.services.core.PoiItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.pasc.business.R;

import java.util.List;


public class AddressAdapter extends BaseQuickAdapter<PoiItem,BaseViewHolder> {
    private Context mContext;
    private List<PoiItem> mList;
    private int selectPosition = -1;

    public interface OnItemClickLisenter {
        void onItemClick(int position);
    }
    public AddressAdapter(Context context, List<PoiItem> list) {
        super(R.layout.emergency_item_address_info,list);
        this.mContext = context;
        this.mList = list;
    }

    public void setList(List<PoiItem> list) {
        this.mList = list;
        selectPosition = 0;
        setNewData(list);
        disableLoadMoreIfNotFullPage();
    }

    public void setSelectPosition(int position) {
        this.selectPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectPositon(){
        return selectPosition;
    }


    @Override
    protected void convert(BaseViewHolder holder, PoiItem poiItem) {
        holder.itemView.setTag(holder.getAdapterPosition());
        if (holder.getAdapterPosition() == selectPosition) {
            holder.setImageResource(R.id.checkBox, R.drawable.emergency_check_box_checked_green);
        } else {
            holder.setImageResource(R.id.checkBox, R.drawable.emergency_transparent_drawable);
        }
        holder.setText(R.id.tv_title,poiItem.getTitle());
        String address;
        if (!TextUtils.isEmpty(poiItem.getSnippet()) && poiItem.getSnippet().contains(poiItem.getProvinceName())) {
            address=poiItem.getSnippet();
        } else {
            address= poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet();

        }
        holder.setText(R.id.tv_message, address);
        holder.addOnClickListener(R.id.linearLayout2);
    }



}
