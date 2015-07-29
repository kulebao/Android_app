package com.cocobabys.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.ActionInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;

public class ActionListAdapter extends BaseAdapter{
    private Context          context;
    private List<ActionInfo> list;

    public ActionListAdapter(Context context, List<ActionInfo> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final ViewHolder holder;

        if(convertView == null){
            convertView = LayoutInflater.from(this.context).inflate(R.layout.action_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.image);
            holder.titleView = (TextView)convertView.findViewById(R.id.title);
            holder.originalpriceView = (TextView)convertView.findViewById(R.id.originalprice);
            holder.discountpriceView = (TextView)convertView.findViewById(R.id.discountprice);
            holder.addressView = (TextView)convertView.findViewById(R.id.address);
            setDataToViews(position, holder);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder)convertView.getTag();
            setDataToViews(position, holder);
        }

        return convertView;
    }

    private void setDataToViews(final int position, ViewHolder flagholder){
        ActionInfo item = getItem(position);

        if(!TextUtils.isEmpty(item.getLogo())){
            // imageLoader.displayImage(item.getLogo(), flagholder.imageView);
            ImageUtils.displayEx(item.getLogo(), flagholder.imageView, ConstantValue.ACTION_PIC_MAX_WIDTH,
                                 ConstantValue.ACTION_PIC_MAX_HEIGHT);
        }

        flagholder.titleView.setText(item.getTitle());

        flagholder.originalpriceView.setText(String.format(Utils.getResString(R.string.original_price),
                                                           Utils.doubleToString(item.getPrice().getOrigin()) + ""));

        flagholder.originalpriceView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); // 中划线

        flagholder.originalpriceView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰

        flagholder.discountpriceView.setText(String.format(Utils.getResString(R.string.format_price),
                                                           Utils.doubleToString(item.getPrice().getDiscounted()) + ""));

        flagholder.addressView.setText(item.getAddress());
    }

    @Override
    public final int getCount(){
        return list.size();
    }

    @Override
    public final ActionInfo getItem(int position){
        return list.get(position);
    }

    @Override
    public final long getItemId(int position){
        return position;
    }

    public void clearData(){
        list.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder{
        ImageView imageView;
        TextView  titleView;
        TextView  originalpriceView;
        TextView  discountpriceView;
        TextView  addressView;
    }
}
