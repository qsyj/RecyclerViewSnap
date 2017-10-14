package com.wqlin.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wqlin on 2017/10/14.
 */

public abstract class PagerRecyclerAdapter<K> extends RecyclerView.Adapter {
    private List<K> datas;
    private int childNum;

    public PagerRecyclerAdapter(int childNum) {
        if (childNum <= 0)
            new Throwable("childNum must > 0");
        this.childNum = childNum;
        datas = new ArrayList<>();
    }
    public abstract View getParentView(Context context);

    public abstract View getChildView(View parentView,int pagePosition,int childIndex);
    public abstract void onBindChildView(View parentView,View childView,K data,int position,int pagePosition,int childIndex);
    public abstract void onBindNoDataChildView(View parentView,View childView,int pagePosition,int childIndex);

    public void setDatas(List<K> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    public View createView(Context context, int res) {
        return LayoutInflater.from(context).inflate(res, null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View parentView = getParentView(parent.getContext());
        ViewGroup.LayoutParams layoutParams = parentView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        parentView.setLayoutParams(layoutParams);
        return new ViewHolder(parentView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pagePosition) {
        int maxPosition = ((pagePosition+1) * childNum)-1;
        int max=getSize()-1;
        maxPosition = maxPosition >max  ? max : maxPosition;
        int minPosition = pagePosition * childNum;
        for (int i = 0; i < childNum; i++) {
            int position = minPosition + i;
            View childView=getChildView(holder.itemView,pagePosition,i);
            if (position > maxPosition) {
                onBindNoDataChildView(holder.itemView, childView, pagePosition, i);
            } else {
                onBindChildView(holder.itemView, childView,datas.get(position),position,pagePosition,i);
            }
        }

    }

    private int getPagePosition(int position) {
        int pagePosition = 0;
        pagePosition = position / childNum;
        pagePosition=pagePosition % childNum > 0 ? (pagePosition + 1) : pagePosition;
        return pagePosition;
    }

    private int getSize() {
        return datas.size();
    }
    @Override
    public int getItemCount() {
        int size = 0;
        if (datas != null && (size = datas.size()) > 0) {
            int count = size / childNum;
            count = size % childNum > 0 ? (count + 1) : count;
            return count;
        }
        return 0;
    }

    public int getChildNum() {
        return childNum;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
