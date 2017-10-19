package com.github.rubensousa.recyclerviewsnap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.wqlin.widget.BasePagerRecyclerAdapter;
import com.wqlin.widget.PagerRecyclerView;

/**
 * Created by wqlin on 2017/10/14.
 */

public class LLYPagerAdapter extends BasePagerRecyclerAdapter<PagerRecyclerActivity.ItemEntity> {
    public LLYPagerAdapter(int childNum) {
        super(childNum);
    }

    @Override
    public PagerRecyclerView.BaseViewHolder onCreateChildViewHolder(Context context,ViewGroup recyclerView,View parentView,int childIndex) {
        if (!isPager(recyclerView)) {
            return new PagerRecyclerView.BaseViewHolder(parentView,childIndex);
        }
        return new PagerRecyclerView.BaseViewHolder(createView(context,R.layout.adapter_pager_v),childIndex);
    }

    @Override
    public void onBindChildView(ViewHolder parentViewHolder, PagerRecyclerView.BaseViewHolder childViewHolder, PagerRecyclerActivity.ItemEntity data, int position, int pagePosition, int childIndex) {
        childViewHolder.getConvertView().setVisibility(View.VISIBLE);
        childViewHolder.setText(R.id.tv, data.getText());
        childViewHolder.setImageResource(R.id.iv, data.getImgRes());
    }

    @Override
    public void onBindNoDataChildView(ViewHolder parentViewHolder,PagerRecyclerView.BaseViewHolder childViewHolder,int pagePosition,int childIndex) {
        childViewHolder.getConvertView().setVisibility(View.INVISIBLE);
    }


    @Override
    public View getParentView(Context context, ViewGroup recyclerView) {
        if (!isPager(recyclerView)) {
            return createView(context,R.layout.adapter_pager_v);
        }
        return super.getParentView(context, recyclerView);
    }

    @Override
    public ViewGroup.LayoutParams getParentViewParams(ViewGroup recyclerView,View parentView) {
        if (!isPager(recyclerView)) {
            return null;
        }
        return super.getParentViewParams(recyclerView,parentView);
    }

    @Override
    public ViewGroup.LayoutParams getChildViewParams(ViewGroup recyclerView,ViewGroup parentView, View childView, int childIndex) {
        if (!isPager(recyclerView))
            return null;
        return super.getChildViewParams(recyclerView,parentView, childView, childIndex);
    }
}
