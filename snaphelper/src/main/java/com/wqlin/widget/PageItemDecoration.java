package com.wqlin.widget;

import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author wangql
 * @email wangql@leleyuntech.com
 * @date 2017/11/30 13:35
 */
public abstract class PageItemDecoration extends RecyclerView.ItemDecoration{
    private int mOrientation=-1;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager==null) return;
        if (!(layoutManager instanceof  LinearLayoutManager)) return;
        mOrientation = ((LinearLayoutManager) layoutManager).getOrientation();
        if (mOrientation!=LinearLayoutManager.HORIZONTAL) return;
        int position=parent.getChildAdapterPosition(view);
        setOutRect(position,outRect,parent);
    }

    protected void setOutRect(int position,Rect outRect, RecyclerView recyclerView) {
        if (isLeft(position,recyclerView)) {
            outRect.left = getLeftWidth(position,recyclerView);
        }
        if (isRight(position,recyclerView)) {
            outRect.right = getRightWidth(position,recyclerView);
        }
    }

    public int getLeftWidth(int position, RecyclerView recyclerView) {
        if (!isLeft(position,recyclerView)) return 0;
        Drawable left=getLeftDrawable(position,recyclerView);
        if (left==null)
            return 0;
        return left.getIntrinsicWidth();
    }

    public int getRightWidth(int position, RecyclerView recyclerView) {
        if (!isRight(position,recyclerView)) return 0;
        Drawable right=getRightDrawable(position,recyclerView);
        if (right==null)
            return 0;
        return right.getIntrinsicWidth();
    }

    protected void draw(int position,View child,Canvas canvas, RecyclerView parent, RecyclerView.State state) {

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int parentTop = parent.getPaddingTop();
        int parentBottom = parent.getHeight() - parent.getPaddingBottom();
        if (isLeft(position,parent)) {
            int right = child.getLeft() - params.leftMargin;
            int left = right - getLeftWidth(position,parent);
            Drawable drawable = getLeftDrawable(position, parent);
            if (drawable != null) {
                drawable.setBounds(left, parentTop , right, parentBottom);
                drawable.draw(canvas);
            }
        }

        if (isRight(position,parent)) {
            int left = child.getRight() + params.rightMargin;
            int right = left + getRightWidth(position,parent);
            Drawable drawable = getRightDrawable(position, parent);
            if (drawable != null) {
                drawable.setBounds(left, parentTop , right, parentBottom);
                drawable.draw(canvas);
            }
        }
    }
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation!=LinearLayoutManager.HORIZONTAL)return;
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position=parent.getChildAdapterPosition(child);
            draw(position,child,c,parent,state);
        }
    }

    private boolean isLeft(int position,RecyclerView recyclerView){
        Drawable drawable = getLeftDrawable(position, recyclerView);
        if (drawable==null)
            return false;
        return true;
    }

    private  boolean isRight(int position,RecyclerView recyclerView) {
        Drawable drawable = getRightDrawable(position, recyclerView);
        if (drawable==null)
            return false;
        return true;
    }

    /**
     * 设置成单列
     * @param position
     * @return
     */
    public abstract Drawable getLeftDrawable(int position,RecyclerView recyclerView);
    /**
     * 设置成单列
     * @param position
     * @return
     */
    public abstract Drawable getRightDrawable(int position,RecyclerView recyclerView);

}
