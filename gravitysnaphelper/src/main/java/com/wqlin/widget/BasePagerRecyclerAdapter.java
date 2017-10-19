package com.wqlin.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wqlin on 2017/10/14.
 */

public abstract class BasePagerRecyclerAdapter<K> extends RecyclerView.Adapter {
    private List<K> datas;
    private int childNum;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongClickListener mOnItemChildLongClickListener;

    public BasePagerRecyclerAdapter(int childNum) {
        if (childNum <= 0)
            new Throwable("childNum must > 0");
        this.childNum = childNum;
        datas = new ArrayList<>();
    }
    public abstract View getParentView(Context context);

    public abstract PagerRecyclerView.BaseViewHolder getChildView(ViewHolder parentViewHolder,int pagePosition,int childIndex);
    public abstract void onBindChildView(ViewHolder parentViewHolder, PagerRecyclerView.BaseViewHolder childViewHolder, K data, int position, int pagePosition, int childIndex);
    public abstract void onBindNoDataChildView(ViewHolder parentViewHolder,PagerRecyclerView.BaseViewHolder childViewHolder,int pagePosition,int childIndex);

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
        int maxPosition = ((pagePosition+1) * getChildNum())-1;
        int max=getSize()-1;
        maxPosition = maxPosition >max  ? max : maxPosition;
        int minPosition = pagePosition * getChildNum();
        for (int i = 0; i < getChildNum(); i++) {
            int position = minPosition + i;
            PagerRecyclerView.BaseViewHolder childViewHolder = getChildView((ViewHolder) holder,pagePosition, i);
            childViewHolder.setAdapter(this);
            if (position > maxPosition) {
                onBindNoDataChildView((ViewHolder) holder, childViewHolder, pagePosition, i);
            } else {
                onBindChildView((ViewHolder) holder, childViewHolder,datas.get(position),position,pagePosition,i);
            }
        }

    }

    public int getPosition(int pagePosition, int childIndex) {
        if (pagePosition >= 0&&pagePosition<=getItemCount() && childIndex >= 0&&childIndex<=getChildNum()) {
            int maxPosition = ((pagePosition+1) * getChildNum())-1;
            int position = pagePosition * getChildNum() + childIndex;
            if (position <= maxPosition) {
                return position;
            }
        }
        return -1;
    }

    private int getPagePosition(int position) {
        int pagePosition = 0;
        pagePosition = position / getChildNum();
        pagePosition=pagePosition % getChildNum() > 0 ? (pagePosition + 1) : pagePosition;
        return pagePosition;
    }

    private int getSize() {
        return datas.size();
    }
    @Override
    public int getItemCount() {
        int size = 0;
        if (datas != null && (size = datas.size()) > 0) {
            int count = size / getChildNum();
            count = size % getChildNum() > 0 ? (count + 1) : count;
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


    /**
     * Interface definition for a callback to be invoked when an itemchild in this
     * view has been clicked
     */
    public interface OnItemChildClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param view     The view whihin the ItemView that was clicked
         * @param position The position of the view int the adapter
         */
        void onItemChildClick(BasePagerRecyclerAdapter adapter, View view, int position,int pagePosition,int childIndex);
    }


    /**
     * Interface definition for a callback to be invoked when an childView in this
     * view has been clicked and held.
     */
    public interface OnItemChildLongClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param view     The childView whihin the itemView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemChildLongClick(BasePagerRecyclerAdapter adapter, View view, int position,int pagePosition,int childIndex);
    }

    /**
     * Register a callback to be invoked when an itemchild in View has
     * been  clicked
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        mOnItemChildClickListener = listener;
    }
    /**
     * Register a callback to be invoked when an itemchild  in this View has
     * been long clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener listener) {
        mOnItemChildLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an itemchild in this RecyclerView has
     * been clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildClickListener getOnItemChildClickListener() {
        return mOnItemChildClickListener;
    }

    /**
     * @return The callback to be invoked with an itemChild in this RecyclerView has
     * been long clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildLongClickListener getOnItemChildLongClickListener() {
        return mOnItemChildLongClickListener;
    }
}
