package com.wqlin.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.wqlin.snap.GravityPagerSnapHelper;
import com.wqlin.snap.GravitySnapHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wqlin on 2017/10/14. <p>
 * 实现ViewPager的效果 </p>
 * Adapter必须使用{@link PagerRecyclerAdapter}  <p>
 * {@link #isFlingMorePage} 快速滑动是否翻动多页
 */

public class PagerRecyclerView extends RecyclerView {
    private final String TAG = PagerRecyclerView.class.getSimpleName();
    private boolean isLog = true;

    /**
     * 快速滑动是否翻动多页
     */
    private boolean isFlingMorePage = false;
    private PagerOnScrollListener mPagerOnScrollListener;
    private List<OnPageChangeListener> mOnPageChangeListeners = new ArrayList<>();

    public PagerRecyclerView(Context context) {
        this(context, null);
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        setSnapHelper(isFlingMorePage);
        mPagerOnScrollListener = new PagerOnScrollListener();
        addOnScrollListener(mPagerOnScrollListener);
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                log("onPageScrolled() "+"position:"+position+",positionOffset:"+positionOffset+",positionOffsetPixels:"+positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                log("onPageSelected() "+"position:"+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                log("onPageScrollStateChanged() "+"state:"+state);
            }
        });
    }
    /**
     * 快速滑动是否翻动多页
     */
    public boolean isFlingMorePage() {
        return isFlingMorePage;
    }
    /**
     * 快速滑动是否翻动多页
     */
    public void setFlingMorePage(boolean flingMorePage) {
        if (flingMorePage != isFlingMorePage) {
            setSnapHelper(flingMorePage);
        }
        isFlingMorePage = flingMorePage;
    }

    private void setSnapHelper(boolean flingMorePage) {
        if (flingMorePage) {
            new GravitySnapHelper(Gravity.START).attachToRecyclerView(this);
        } else {
            new GravityPagerSnapHelper(Gravity.START).attachToRecyclerView(this);
        }
    }
    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }
    public void setCurrentItem(int item, boolean smoothScroll) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (smoothScroll) {
                linearLayoutManager.smoothScrollToPosition(PagerRecyclerView.this,null,item);
            } else {
                linearLayoutManager.scrollToPositionWithOffset(item, 0);
            }
        }
    }
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    /**
     * @param adapter {@link PagerRecyclerAdapter}
     */
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof PagerRecyclerAdapter) {
            super.setAdapter(adapter);
        } else {
            new Throwable("adapter instanceof PagerRecyclerAdapter");
        }
    }

    private boolean isFling = false;
    @Override
    public boolean fling(int velocityX, int velocityY) {
        isFling = true;
        return super.fling(velocityX, velocityY);
    }
    private void log(String str) {
        if (isLog) {
            Log.e(TAG, str);
        }
    }
    class PagerOnScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                int position = linearLayoutManager.findFirstVisibleItemPosition();
                View current = linearLayoutManager.findViewByPosition(position);
                /*int[] location = new int[2];
                current.getLocationInWindow(location); //获取在当前窗口内的绝对坐标*/
                Rect r = new Rect();
                current.getLocalVisibleRect(r); //获取在当前窗口内的绝对坐标
                int visibleLeft = r.left;
                int visibleRight = r.right;
                int width = current.getWidth();
                int inVisibleWith = width - (visibleRight - visibleLeft);
                float positionOffset = (inVisibleWith*1f) / (width*1f);
                int positionOffsetPixels = 0;
                if (visibleLeft > 0 ) {
                    positionOffsetPixels = visibleLeft;
                }
                if (visibleRight < width) {
                    positionOffsetPixels = visibleRight - width;
                }
                int size=mOnPageChangeListeners.size();
                if (size>0) {
                    if (visibleLeft == 0) {
                        for (int i = 0; i <size; i++) {
                            mOnPageChangeListeners.get(i).onPageSelected(position);
                        }
                    } else {
                        for (int i = 0; i <size; i++) {
                            mOnPageChangeListeners.get(i).onPageScrolled(position,positionOffset,positionOffsetPixels);
                        }
                    }
                }
                /*Log.e("PagerRecyclerView", "position:" + position + ",location.x:" + location[0] + ",location.y:"
                        + location[1] + ",r.left:" + r.left + ",r.right:" + r.right +",r.top:"+ r.top +",r.bottom:"+ r.bottom);*/
            }
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            /*newState 一共有三种状态
            SCROLL_STATE_IDLE：目前RecyclerView不是滚动，也就是静止
            SCROLL_STATE_DRAGGING：RecyclerView目前被外部输入如用户触摸输入。
            SCROLL_STATE_SETTLING：RecyclerView目前动画虽然不是在最后一个位置外部控制。*/
            int size=mOnPageChangeListeners.size();
            if (size>0) {
                for (int i = 0; i <size; i++) {
                    mOnPageChangeListeners.get(i).onPageScrollStateChanged(newState);
                }
            }
        }
    }
    public interface OnPageChangeListener {

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }
}
