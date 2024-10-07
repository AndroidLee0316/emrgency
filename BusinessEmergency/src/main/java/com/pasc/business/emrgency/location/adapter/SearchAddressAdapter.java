package com.pasc.business.emrgency.location.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.amap.api.services.core.PoiItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.pasc.business.R;

import java.util.List;


public class SearchAddressAdapter extends BaseQuickAdapter<PoiItem,BaseViewHolder> {
    private Context mContext;
    private List<PoiItem> mList;
    private String userInput = "";
    private int selectPosition = -1;
    private OnItemClickListener mOnItemClickListener;

    public SearchAddressAdapter(Context context, List<PoiItem> list) {
        super(R.layout.emergency_item_address_seach,list);
        this.mContext = context;
        this.mList = list;
    }

    public void setList(List<PoiItem> list, String userInput) {
        this.selectPosition = 0;
        this.userInput = userInput;
        this.mList = list;
        setNewData(list);
        disableLoadMoreIfNotFullPage();
        notifyDataSetChanged();
    }

    public void setSelectPosition(int position) {
        this.selectPosition = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected void convert(BaseViewHolder holder, PoiItem item) {
        int position = holder.getAdapterPosition();
        PoiItem poiItem = mList.get(position);
        holder.itemView.setTag(position);
        if (position == selectPosition) {
            holder.setChecked(R.id.checkBox,true);
        } else {
            holder.setChecked(R.id.checkBox,false);
        }

        String name = poiItem.getTitle();

        if (TextUtils.isEmpty(userInput)) {
            holder.setText(R.id.tv_title,name);
        } else {
            try {
                if (name != null && name.contains(userInput)) {//如果搜索出来的文字跟输入文字有重叠，则改变重叠文字的颜色
                    int index = name.indexOf(userInput);
                    int len = userInput.length();
                    Spanned temp = Html.fromHtml(name.substring(0, index)
                            + "<font color=#19ad19>"
                            + name.substring(index, index + len) + "</font>"
                            + name.substring(index + len, name.length()));
                    holder.setText(R.id.tv_title,temp);
                } else {
                    holder.setText(R.id.tv_title,name);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                holder.setText(R.id.tv_title,name);
            }
        }



        holder.setText(R.id.tv_message,poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                setSelectPosition(position);
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}
