package com.wqlin.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wqlin.snap.GravityPagerSnapHelper;
import com.wqlin.snap.GravitySnapHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wqlin on 2017/10/14. <p>
 * 实现ViewPager的效果 </p>
 * Adapter必须使用{@link BasePagerRecyclerAdapter}  <p>
 * {@link #isFlingMorePage} 快速滑动是否翻动多页
 */

public class PagerRecyclerView extends RecyclerView {
    private final String TAG = PagerRecyclerView.class.getSimpleName();
    private boolean isLog = false;
    private int mCurrentPosition = -1;
    /**
     * 是否需要设置成ViewPager效果  不是则为正常RecyclerView <P>
     * false时会清除所有滑动监听
     */
    private boolean isPager=true;
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
        if (!isPager)
            return;

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

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }

    private void setSnapHelper(boolean flingMorePage) {
        if (!isPager)
            return;
        if (flingMorePage) {
            new GravitySnapHelper(Gravity.START).attachToRecyclerView(this);
        } else {
            new GravityPagerSnapHelper(Gravity.START).attachToRecyclerView(this);
        }
    }
    private void destroyCallbacks() {
        this.clearOnScrollListeners();
        this.setOnFlingListener(null);
    }
    public void setCurrentItem(int item) {
        if (!isPager)
            return;
        setCurrentItem(item, false);
    }
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (!isPager)
            return;
        if (item != mCurrentPosition) {
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
    }
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            if (listener != null) {
                mOnPageChangeListeners.remove(listener);
            }
        }
    }
    /**
     * @param adapter {@link BasePagerRecyclerAdapter}
     */
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof BasePagerRecyclerAdapter) {
            super.setAdapter(adapter);
        } else {
            new Throwable("adapter instanceof BasePagerRecyclerAdapter");
        }
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
        if (!isPager)
            return;
        if (flingMorePage != isFlingMorePage) {
            setSnapHelper(flingMorePage);
        }
        isFlingMorePage = flingMorePage;
    }

    /**
     * 是否需要设置成ViewPager效果  不是则为正常RecyclerView
     * @return
     */
    public boolean isPager() {
        return isPager;
    }
    /**
     * 是否需要设置成ViewPager效果  不是则为正常RecyclerView
     * @return false时会清除所有滑动监听
     */
    public void setPager(boolean pager) {
        if (isPager != pager) {
            isPager = pager;
            if (!pager) {
                destroyCallbacks();
            } else {
                init();
            }
            return;
        }
        isPager = pager;
    }

    private void log(String str) {
        if (isLog) {
            Log.e(TAG, str);
        }
    }
    class PagerOnScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!isPager)
                return;
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                View currentView;
                int position;
                if (mCurrentPosition >= firstPosition && mCurrentPosition <= lastPosition) {
                    currentView = linearLayoutManager.findViewByPosition(mCurrentPosition);
                    position = mCurrentPosition;
                } else {
                    currentView = linearLayoutManager.findViewByPosition(firstPosition);
                    position = firstPosition;
                }
                /*int[] location = new int[2];
                current.getLocationInWindow(location); //获取在当前窗口内的绝对坐标*/
                Rect r = new Rect();
                currentView.getLocalVisibleRect(r); //获取在当前窗口内的绝对坐标
                int visibleLeft = r.left;
                int visibleRight = r.right;
                int width = currentView.getWidth();
                int inVisibleWith = width - (visibleRight - visibleLeft);
                int positionOffsetPixels = 0;
                if (visibleLeft > 0 ) {
                    positionOffsetPixels = visibleLeft;
                }
                if (visibleRight < width) {
                    positionOffsetPixels = visibleRight - width;
                }
                float positionOffset = (positionOffsetPixels*1f) / (width*1f);
                int size=mOnPageChangeListeners.size();
                if (size>0) {
                    if (visibleLeft == 0&&visibleRight==width) {
                        mCurrentPosition = position;
                        for (int i = 0; i <size; i++) {
                            mOnPageChangeListeners.get(i).onPageSelected(mCurrentPosition);
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
            if (!isPager)
                return;
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
    public static class BaseViewHolder{
        /**
         * Views indexed with their IDs
         */
        private final SparseArray<View> views;
        private final HashSet<Integer> nestViews;
        private final LinkedHashSet<Integer> childClickViewIds;
        private final LinkedHashSet<Integer> itemChildLongClickViewIds;

        /**
         * use itemView instead
         */
        public View convertView;
        private BasePagerRecyclerAdapter adapter;

        private int pagePosition = -1;
        private int childIndex = -1;
        public BaseViewHolder(final View view,int childIndex) {
            this.views = new SparseArray<>();
            this.childClickViewIds = new LinkedHashSet<>();
            this.itemChildLongClickViewIds = new LinkedHashSet<>();
            this.nestViews = new HashSet<>();
            convertView = view;
            this.childIndex = childIndex;
        }
        /**
         * Sets the adapter of a adapter view.
         *
         * @param adapter The adapter;
         * @return The BaseViewHolder for chaining.
         */
        protected BaseViewHolder setAdapter(BasePagerRecyclerAdapter adapter) {
            this.adapter = adapter;
            return this;
        }
        /**
         * use itemView instead
         *
         * @return the ViewHolder root view
         */
        public View getConvertView() {

            return convertView;
        }
        public Set<Integer> getNestViews() {
            return nestViews;
        }

        @SuppressWarnings("unchecked")
        public <T extends View> T getView(@IdRes int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = convertView.findViewById(viewId);
                views.put(viewId, view);
            }
            return (T) view;
        }
        /**
         * Will set the text of a TextView.
         *
         * @param viewId The view id.
         * @param value  The text to put in the text view.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setText(@IdRes int viewId, CharSequence value) {
            TextView view = getView(viewId);
            view.setText(value);
            return this;
        }

        public BaseViewHolder setText(@IdRes int viewId, @StringRes int strId) {
            TextView view = getView(viewId);
            view.setText(strId);
            return this;
        }

        /**
         * Will set the image of an ImageView from a resource id.
         *
         * @param viewId     The view id.
         * @param imageResId The image resource id.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setImageResource(@IdRes int viewId, @DrawableRes int imageResId) {
            ImageView view = getView(viewId);
            view.setImageResource(imageResId);
            return this;
        }

        /**
         * Will set background color of a view.
         *
         * @param viewId The view id.
         * @param color  A color, not a resource id.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setBackgroundColor(@IdRes int viewId, @ColorInt int color) {
            View view = getView(viewId);
            view.setBackgroundColor(color);
            return this;
        }

        /**
         * Will set background of a view.
         *
         * @param viewId        The view id.
         * @param backgroundRes A resource to use as a background.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setBackgroundRes(@IdRes int viewId, @DrawableRes int backgroundRes) {
            View view = getView(viewId);
            view.setBackgroundResource(backgroundRes);
            return this;
        }

        /**
         * Will set text color of a TextView.
         *
         * @param viewId    The view id.
         * @param textColor The text color (not a resource id).
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setTextColor(@IdRes int viewId, @ColorInt int textColor) {
            TextView view = getView(viewId);
            view.setTextColor(textColor);
            return this;
        }


        /**
         * Will set the image of an ImageView from a drawable.
         *
         * @param viewId   The view id.
         * @param drawable The image drawable.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setImageDrawable(@IdRes int viewId, Drawable drawable) {
            ImageView view = getView(viewId);
            view.setImageDrawable(drawable);
            return this;
        }

        /**
         * Add an action to set the image of an image view. Can be called multiple times.
         */
        public BaseViewHolder setImageBitmap(@IdRes int viewId, Bitmap bitmap) {
            ImageView view = getView(viewId);
            view.setImageBitmap(bitmap);
            return this;
        }

        /**
         * Add an action to set the alpha of a view. Can be called multiple times.
         * Alpha between 0-1.
         */
        public BaseViewHolder setAlpha(@IdRes int viewId, float value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getView(viewId).setAlpha(value);
            } else {
                // Pre-honeycomb hack to set Alpha value
                AlphaAnimation alpha = new AlphaAnimation(value, value);
                alpha.setDuration(0);
                alpha.setFillAfter(true);
                getView(viewId).startAnimation(alpha);
            }
            return this;
        }

        /**
         * Set a view visibility to VISIBLE (true) or GONE (false).
         *
         * @param viewId  The view id.
         * @param visible True for VISIBLE, false for GONE.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setGone(@IdRes int viewId, boolean visible) {
            View view = getView(viewId);
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
            return this;
        }

        /**
         * Set a view visibility to VISIBLE (true) or INVISIBLE (false).
         *
         * @param viewId  The view id.
         * @param visible True for VISIBLE, false for INVISIBLE.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setVisible(@IdRes int viewId, boolean visible) {
            View view = getView(viewId);
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            return this;
        }

        /**
         * Add links into a TextView.
         *
         * @param viewId The id of the TextView to linkify.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder linkify(@IdRes int viewId) {
            TextView view = getView(viewId);
            Linkify.addLinks(view, Linkify.ALL);
            return this;
        }

        /**
         * Apply the typeface to the given viewId, and enable subpixel rendering.
         */
        public BaseViewHolder setTypeface(@IdRes int viewId, Typeface typeface) {
            TextView view = getView(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            return this;
        }

        /**
         * Apply the typeface to all the given viewIds, and enable subpixel rendering.
         */
        public BaseViewHolder setTypeface(Typeface typeface, int... viewIds) {
            for (int viewId : viewIds) {
                TextView view = getView(viewId);
                view.setTypeface(typeface);
                view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
            return this;
        }

        /**
         * Sets the progress of a ProgressBar.
         *
         * @param viewId   The view id.
         * @param progress The progress.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setProgress(@IdRes int viewId, int progress) {
            ProgressBar view = getView(viewId);
            view.setProgress(progress);
            return this;
        }

        /**
         * Sets the progress and max of a ProgressBar.
         *
         * @param viewId   The view id.
         * @param progress The progress.
         * @param max      The max value of a ProgressBar.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setProgress(@IdRes int viewId, int progress, int max) {
            ProgressBar view = getView(viewId);
            view.setMax(max);
            view.setProgress(progress);
            return this;
        }

        /**
         * Sets the range of a ProgressBar to 0...max.
         *
         * @param viewId The view id.
         * @param max    The max value of a ProgressBar.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setMax(@IdRes int viewId, int max) {
            ProgressBar view = getView(viewId);
            view.setMax(max);
            return this;
        }

        /**
         * Sets the rating (the number of stars filled) of a RatingBar.
         *
         * @param viewId The view id.
         * @param rating The rating.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setRating(@IdRes int viewId, float rating) {
            RatingBar view = getView(viewId);
            view.setRating(rating);
            return this;
        }

        /**
         * Sets the rating (the number of stars filled) and max of a RatingBar.
         *
         * @param viewId The view id.
         * @param rating The rating.
         * @param max    The range of the RatingBar to 0...max.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setRating(@IdRes int viewId, float rating, int max) {
            RatingBar view = getView(viewId);
            view.setMax(max);
            view.setRating(rating);
            return this;
        }

        /**
         * Sets the on click listener of the view.
         *
         * @param viewId   The view id.
         * @param listener The on click listener;
         * @return The BaseViewHolder for chaining.
         */
        @Deprecated
        public BaseViewHolder setOnClickListener(@IdRes int viewId, View.OnClickListener listener) {
            View view = getView(viewId);
            view.setOnClickListener(listener);
            return this;
        }

        /**
         * add childView id
         *
         * @param viewId add the child view id   can support childview click
         * @return if you use adapter bind listener
         * @link {(adapter.setOnItemChildClickListener(listener))}
         * <p>
         * or if you can use  recyclerView.addOnItemTouch(listerer)  wo also support this menthod
         */
        @SuppressWarnings("unchecked")
        public BaseViewHolder addOnClickListener(@IdRes final int viewId) {
            childClickViewIds.add(viewId);
            final View view = getView(viewId);
            if (view != null) {
                if (!view.isClickable()) {
                    view.setClickable(true);
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapter.getOnItemChildClickListener() != null) {
                            adapter.getOnItemChildClickListener().onItemChildClick(adapter, v, getPosition(),getPagePosition(),getChildIndex());
                        }
                    }
                });
            }

            return this;
        }


        /**
         * set nestview id
         *
         * @param viewId add the child view id   can support childview click
         * @return
         */
        public BaseViewHolder setNestView(@IdRes int viewId) {
            addOnClickListener(viewId);
            addOnLongClickListener(viewId);
            nestViews.add(viewId);
            return this;
        }

        /**
         * add long click view id
         *
         * @param viewId
         * @return if you use adapter bind listener
         * @link {(adapter.setOnItemChildLongClickListener(listener))}
         * <p>
         * or if you can use  recyclerView.addOnItemTouch(listerer)  wo also support this menthod
         */
        @SuppressWarnings("unchecked")
        public BaseViewHolder addOnLongClickListener(@IdRes final int viewId) {
            itemChildLongClickViewIds.add(viewId);
            final View view = getView(viewId);
            if (view != null) {
                if (!view.isLongClickable()) {
                    view.setLongClickable(true);
                }
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return adapter.getOnItemChildLongClickListener() != null &&
                                adapter.getOnItemChildLongClickListener().onItemChildLongClick(adapter, v, getPosition(),getPagePosition(),getChildIndex());
                    }
                });
            }
            return this;
        }


        /**
         * Sets the on touch listener of the view.
         *
         * @param viewId   The view id.
         * @param listener The on touch listener;
         * @return The BaseViewHolder for chaining.
         */
        @Deprecated
        public BaseViewHolder setOnTouchListener(@IdRes int viewId, View.OnTouchListener listener) {
            View view = getView(viewId);
            view.setOnTouchListener(listener);
            return this;
        }

        /**
         * Sets the on long click listener of the view.
         *
         * @param viewId   The view id.
         * @param listener The on long click listener;
         * @return The BaseViewHolder for chaining.
         * Please use {@link #addOnLongClickListener(int)} (adapter.setOnItemChildLongClickListener(listener))}
         */
        @Deprecated
        public BaseViewHolder setOnLongClickListener(@IdRes int viewId, View.OnLongClickListener listener) {
            View view = getView(viewId);
            view.setOnLongClickListener(listener);
            return this;
        }

        /**
         * Sets the listview or gridview's item click listener of the view
         *
         * @param viewId   The view id.
         * @param listener The item on click listener;
         * @return The BaseViewHolder for chaining.
         * Please use {@link #addOnClickListener(int)} (int)} (adapter.setOnItemChildClickListener(listener))}
         */
        @Deprecated
        public BaseViewHolder setOnItemClickListener(@IdRes int viewId, AdapterView.OnItemClickListener listener) {
            AdapterView view = getView(viewId);
            view.setOnItemClickListener(listener);
            return this;
        }

        /**
         * Sets the listview or gridview's item long click listener of the view
         *
         * @param viewId   The view id.
         * @param listener The item long click listener;
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setOnItemLongClickListener(@IdRes int viewId, AdapterView.OnItemLongClickListener listener) {
            AdapterView view = getView(viewId);
            view.setOnItemLongClickListener(listener);
            return this;
        }

        /**
         * Sets the listview or gridview's item selected click listener of the view
         *
         * @param viewId   The view id.
         * @param listener The item selected click listener;
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setOnItemSelectedClickListener(@IdRes int viewId, AdapterView.OnItemSelectedListener listener) {
            AdapterView view = getView(viewId);
            view.setOnItemSelectedListener(listener);
            return this;
        }

        /**
         * Sets the on checked change listener of the view.
         *
         * @param viewId   The view id.
         * @param listener The checked change listener of compound button.
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setOnCheckedChangeListener(@IdRes int viewId, CompoundButton.OnCheckedChangeListener listener) {
            CompoundButton view = getView(viewId);
            view.setOnCheckedChangeListener(listener);
            return this;
        }

        /**
         * Sets the tag of the view.
         *
         * @param viewId The view id.
         * @param tag    The tag;
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setTag(@IdRes int viewId, Object tag) {
            View view = getView(viewId);
            view.setTag(tag);
            return this;
        }

        /**
         * Sets the tag of the view.
         *
         * @param viewId The view id.
         * @param key    The key of tag;
         * @param tag    The tag;
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setTag(@IdRes int viewId, int key, Object tag) {
            View view = getView(viewId);
            view.setTag(key, tag);
            return this;
        }

        /**
         * Sets the checked status of a checkable.
         *
         * @param viewId  The view id.
         * @param checked The checked status;
         * @return The BaseViewHolder for chaining.
         */
        public BaseViewHolder setChecked(@IdRes int viewId, boolean checked) {
            View view = getView(viewId);
            // View unable cast to Checkable
            if (view instanceof Checkable) {
                ((Checkable) view).setChecked(checked);
            }
            return this;
        }

        public void setPagePosition(int pagePosition) {
            this.pagePosition = pagePosition;
        }

        public  int getPosition(){
            return adapter.getPosition(pagePosition,childIndex);
        }

        public int getPagePosition() {
            return pagePosition;
        }

        public int getChildIndex() {
            return childIndex;
        }
    }
}
