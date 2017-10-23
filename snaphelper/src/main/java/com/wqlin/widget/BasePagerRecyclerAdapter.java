package com.wqlin.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.rubensousa.gravitysnaphelper.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wqlin on 2017/10/14.
 */

public abstract class BasePagerRecyclerAdapter<K> extends RecyclerView.Adapter {
    private List<K> mData;
    private int childNum;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongClickListener mOnItemChildLongClickListener;

    public BasePagerRecyclerAdapter(int childNum) {
        checkChildNum(childNum);
        this.childNum = childNum;
        mData = new ArrayList<>();
    }

    private ViewHolder onCreateParentViewHolder(Context context, ViewGroup recyclerView) {
        View pView = getParentView(context, recyclerView);
        List<PagerRecyclerView.BaseViewHolder> childViewHolders = new ArrayList<>();
        ViewGroup.LayoutParams parentParams = getParentViewParams(recyclerView, pView);
        if (parentParams != null) {
            pView.setLayoutParams(parentParams);
        }
        ViewGroup parentView = null;
        if (pView instanceof ViewGroup) {
            parentView = (ViewGroup) pView;
        }

        for (int i = 0; i < childNum; i++) {
            PagerRecyclerView.BaseViewHolder baseViewHolder = onCreateChildViewHolder(context,  recyclerView,pView, i);
            childViewHolders.add(baseViewHolder);
            View view = baseViewHolder.convertView;
            if (parentView!=null) {
                addChildView(recyclerView, parentView, view, i);
            }


        }
        return new ViewHolder(pView, childViewHolders);
    }

    public View getParentView(Context context, ViewGroup recyclerView) {
        return createView(context, R.layout.adpter_pager_recycler_item);
    }

    /**
     * 将childView添加到parentView 并设置LayoutParams; 若childView=parentView则不会添加
     *
     * @param parentView
     * @param childView
     */
    public void addChildView(ViewGroup recyclerView, ViewGroup parentView, View childView, int childIndex) {
        if (childView != null && parentView != null&&childView!=parentView) {
            if (parentView instanceof LinearLayout) {
                ViewGroup.LayoutParams layoutParams = getChildViewParams(recyclerView, parentView, childView, childIndex);
                if (layoutParams != null) {
                    parentView.addView(childView, layoutParams);
                } else {
                    parentView.addView(childView);
                }
            }
        }
    }

    /**
     * 获取childView的LayoutParams
     *
     * @param parentView
     * @param childView
     * @param childIndex null则pchildView不设置LayoutParams
     * @return
     */
    public ViewGroup.LayoutParams getChildViewParams(ViewGroup recyclerView, ViewGroup parentView, View childView, int childIndex) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        return layoutParams;
    }

    /**
     * 获取parentView的LayoutParams
     *
     * @param parentView null则parentView不设置LayoutParams
     */
    public ViewGroup.LayoutParams getParentViewParams(ViewGroup recyclerView, View parentView) {
        if (parentView == null)
            return null;
        ViewGroup.LayoutParams layoutParams = parentView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        return layoutParams;
    }

    public void onBindParentViewHodler(ViewHolder viewHolder, int pagePosition) {
        int maxPosition = ((pagePosition + 1) * getChildNum()) - 1;
        int max = getSize() - 1;
        maxPosition = maxPosition > max ? max : maxPosition;
        int minPosition = pagePosition * getChildNum();

        for (int i = 0; i < getChildNum(); i++) {
            int position = minPosition + i;
            PagerRecyclerView.BaseViewHolder childViewHolder = viewHolder.getChildViewHolder(i);
            childViewHolder.setPagePosition(pagePosition);
            childViewHolder.setAdapter(this);
            if (position > maxPosition) {
                onBindNoDataChildView(viewHolder, childViewHolder, pagePosition, i);
            } else {
                onBindChildView(viewHolder, childViewHolder, mData.get(position), position, pagePosition, i);
            }
        }
    }

    /**
     * 建议 若PagerRecyclerView不是ViewPager效果则直接用parentView作为childView
     * @param context
     * @param recyclerView
     * @param parentView
     * @param childIndex
     * @return
     */
    public abstract PagerRecyclerView.BaseViewHolder onCreateChildViewHolder(Context context, ViewGroup recyclerView,View parentView, int childIndex);

    public abstract void onBindChildView(ViewHolder parentViewHolder, PagerRecyclerView.BaseViewHolder childViewHolder, K data, int position, int pagePosition, int childIndex);

    public abstract void onBindNoDataChildView(ViewHolder parentViewHolder, PagerRecyclerView.BaseViewHolder childViewHolder, int pagePosition, int childIndex);

    public void setNewData(List<K> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void addData(@NonNull Collection<? extends K> newData) {
        mData.addAll(newData);
        notifyItemRangeInserted(mData.size() - newData.size(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    public void addData(@IntRange(from = 0) int position, @NonNull K data) {
        mData.add(position, data);
        notifyItemInserted(position);
        compatibilityDataSizeChanged(1);
    }
    public void addData(@NonNull K data) {
        mData.add(data);
        notifyItemInserted(mData.size());
        compatibilityDataSizeChanged(1);
    }

    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mData == null ? 0 : mData.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    public List<K> getData() {
        return mData;
    }

    public View createView(Context context, int res) {
        return LayoutInflater.from(context).inflate(res, null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateParentViewHolder(parent.getContext(), parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pagePosition) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            onBindParentViewHodler(viewHolder, pagePosition);
        }
    }

    public int getPosition(int pagePosition, int childIndex) {
        if (pagePosition >= 0 && pagePosition <= getItemCount() && childIndex >= 0 && childIndex <= getChildNum()) {
            int maxPosition = ((pagePosition + 1) * getChildNum()) - 1;
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
        pagePosition = pagePosition % getChildNum() > 0 ? (pagePosition + 1) : pagePosition;
        return pagePosition;
    }

    private int getSize() {
        if (mData ==null)
            return 0;
        return mData.size();
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (mData != null && (size = mData.size()) > 0) {
            int count = size / getChildNum();
            count = size % getChildNum() > 0 ? (count + 1) : count;
            return count;
        }
        return 0;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        checkChildNum(childNum);
        this.childNum = childNum;
    }

    private void checkChildNum(int childNum) {
        if (childNum <= 0)
            new Throwable("childNum must > 0");
    }
    public boolean isPager(ViewGroup recyclerView) {
        if (recyclerView != null && recyclerView instanceof PagerRecyclerView) {
            PagerRecyclerView pagerRecyclerView = (PagerRecyclerView) recyclerView;
            return pagerRecyclerView.isPager();
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private List<PagerRecyclerView.BaseViewHolder> childViewHolders = new ArrayList<>();

        public ViewHolder(View itemView, List<PagerRecyclerView.BaseViewHolder> childViewHolders) {
            super(itemView);
            this.childViewHolders = childViewHolders;
        }

        public PagerRecyclerView.BaseViewHolder getChildViewHolder(int childIndex) {
            int count = childViewHolders.size();
            if (count == 0)
                return null;
            if (childIndex >= count)
                return null;
            return childViewHolders.get(childIndex);
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
        void onItemChildClick(BasePagerRecyclerAdapter adapter, View view, int position, int pagePosition, int childIndex);
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
        boolean onItemChildLongClick(BasePagerRecyclerAdapter adapter, View view, int position, int pagePosition, int childIndex);
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
