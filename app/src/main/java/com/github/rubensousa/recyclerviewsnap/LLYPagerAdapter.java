package com.github.rubensousa.recyclerviewsnap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wqlin.widget.BasePagerRecyclerAdapter;
import com.wqlin.widget.PagerRecyclerView;

/**
 * Created by wqlin on 2017/10/14.
 */

public class LLYPagerAdapter extends BasePagerRecyclerAdapter<String> {
    public LLYPagerAdapter(int childNum) {
        super(childNum);
    }

    @Override
    public View getParentView(Context context) {
        return createView(context,R.layout.adapter_pager_match);
    }


    @Override
    public PagerRecyclerView.BaseViewHolder getChildView(ViewHolder parentViewHolder,int pagePosition,int childIndex) {
        switch (childIndex) {
            case 0:
                return new PagerRecyclerView.BaseViewHolder(parentViewHolder.itemView.findViewById(R.id.ll_child0),pagePosition,childIndex);
            case 1:
                return new PagerRecyclerView.BaseViewHolder(parentViewHolder.itemView.findViewById(R.id.ll_child1),pagePosition,childIndex);
            case 2:
                return new PagerRecyclerView.BaseViewHolder(parentViewHolder.itemView.findViewById(R.id.ll_child2),pagePosition,childIndex);
        }
        return null;
    }

    @Override
    public void onBindChildView(ViewHolder parentViewHolder, PagerRecyclerView.BaseViewHolder childViewHolder, String data, int position, int pagePosition, int childIndex) {
        childViewHolder.getConvertView().setVisibility(View.VISIBLE);
        ViewGroup viewGroup = (ViewGroup) childViewHolder.getConvertView();
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View c = viewGroup.getChildAt(i);
            if (c instanceof TextView) {
                ((TextView)c).setText("child"+data);
            }
        }
    }

    @Override
    public void onBindNoDataChildView(ViewHolder parentViewHolder,PagerRecyclerView.BaseViewHolder childViewHolder,int pagePosition,int childIndex) {
        childViewHolder.getConvertView().setVisibility(View.INVISIBLE);
    }
}
