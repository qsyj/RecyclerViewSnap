package com.github.rubensousa.recyclerviewsnap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wqlin.widget.PagerRecyclerAdapter;

/**
 * Created by wqlin on 2017/10/14.
 */

public class LLYPagerAdapter extends PagerRecyclerAdapter<String> {
    public LLYPagerAdapter(int childNum) {
        super(childNum);
    }

    @Override
    public View getParentView(Context context) {
        return createView(context,R.layout.adapter_pager_match);
    }


    @Override
    public View getChildView(View parentView, int pagePosition, int childIndex) {
        switch (childIndex) {
            case 0:
                return parentView.findViewById(R.id.ll_child0);
            case 1:
                return parentView.findViewById(R.id.ll_child1);
            case 2:
                return parentView.findViewById(R.id.ll_child2);
        }
        return null;
    }

    @Override
    public void onBindChildView(View parentView, View childView, String data, int position, int pagePosition, int childIndex) {
        childView.setVisibility(View.VISIBLE);
        ViewGroup viewGroup = (ViewGroup) childView;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View c = viewGroup.getChildAt(i);
            if (c instanceof TextView) {
                ((TextView)c).setText("child"+data);
            }
        }
    }

    @Override
    public void onBindNoDataChildView(View parentView, View childView, int pagePosition, int childIndex) {
        childView.setVisibility(View.INVISIBLE);
    }
}
