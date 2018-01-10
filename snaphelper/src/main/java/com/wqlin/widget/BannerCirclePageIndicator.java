/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wqlin.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.github.rubensousa.gravitysnaphelper.R;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * Draws circles (one for each view). The current view mCurrentPosition is filled and
 * others are only stroked.
 *  <p>
 *    属性设置见 attrs.xml中的CirclePageIndicator
 *  <p>
 *   {@link #setPagerRecyclerView(PagerRecyclerView,int)} {@link #setPagerRecyclerView(PagerRecyclerView,int,int)}-->配置，默认启用{@link #startLoop()}；
 *   <p>
 *   {@link #setCurrentItem(int)}  {@link #setCurrentItem(int, boolean)} -->设置当前选中的item,是否慢速滑动;
 * <p>
 *  {@link #startLoop()}-->开始循环；
 *  <p>
 *   {@link #stopLoop()}-->停止循环；
 *   <p>
 *   {@link #suspendLoop()}-->中断循环（并不会停止，在如果在循环，则中断）；
 *   <p>
 *   {@link #setBannerLoopDuration(long)}-->循环时间；
 * @author wangql
 * @email wangql@leleyuntech.com
 * @date 2017/10/23 15:47
 */
public class BannerCirclePageIndicator extends View implements PagerRecyclerView.OnPageChangeListener,PagerRecyclerView.OnDestroyListener {
    private static final int INVALID_POINTER = -1;

    private float mRadius;
    private float mSpace;
    private final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
    private PagerRecyclerView mPagerRecyclerView;
    private int mCurrentPage;
    private int mCurrentPosition;
    private int mSnapPage;
    private float mPageOffset;
    private int mScrollState;
    private int mOrientation;
    private boolean mCentered;
    private boolean mSnap;
    private List<ItemInfo> mItemInfos = new ArrayList<>();
    private GestureDetectorCompat mGestureDetector;

    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;

    private Handler mHandler;
    private boolean isLoopStop;
    private final int WHAT_BANNER_LOOP = 1;
    private long bannerLoopDuration = 3000;

    private int bannerCount = 1;

    public BannerCirclePageIndicator(Context context) {
        this(context, null);
    }

    public BannerCirclePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.vpiCirclePageIndicatorStyle);
    }

    public BannerCirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color);
        final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
        final int defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation);
        final int defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color);
        final float defaultStrokeWidth = res.getDimension(R.dimen.default_circle_indicator_stroke_width);
        final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);
        final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);
        final boolean defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0);

        mCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered);
        mOrientation = a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation);
        mPaintPageFill.setStyle(Style.FILL);
        mPaintPageFill.setColor(a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor));
        mPaintStroke.setStyle(Style.STROKE);
        mPaintStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor));
        mPaintStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
        mPaintFill.setStyle(Style.FILL);
        mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));
        mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);
        mSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap);
        mSpace = a.getDimension(R.styleable.CirclePageIndicator_space, mRadius);

        Drawable background = a.getDrawable(R.styleable.CirclePageIndicator_android_background);
        if (background != null) {
            setBackgroundDrawable(background);
        }

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

        mGestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector!=null)
            mGestureDetector.onTouchEvent(ev);
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if (mPagerRecyclerView==null ||mPagerRecyclerView.getAdapter().getItemCount()==0) {
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x;
                    if (mPagerRecyclerView != null&&mPagerRecyclerView.getAdapter().getItemCount()>0) {
                        mPagerRecyclerView.scrollBy(-(int) deltaX,0);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    if (mPagerRecyclerView != null &&
                            mPagerRecyclerView.getAdapter().getItemCount() > 0){
                        mPagerRecyclerView.endScrollBy();
                    }
                }
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionX = MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        return true;
//        return mGestureDetector.onTouchEvent(event);
    }

    private int getPosition(float x, float y) {
        int count = mItemInfos.size();
        for (int i = 0; i < count; i++) {
            ItemInfo itemInfo = mItemInfos.get(i);
            float minX = itemInfo.cx - itemInfo.radius;
            float maxX = itemInfo.cx + itemInfo.radius;
            float minY = itemInfo.cy - itemInfo.radius;
            float maxY = itemInfo.cy + itemInfo.radius;
            if (x <= maxX && x >= minX &&
                    y <= maxY && y >= minY) {
                return i;
            }
        }
        return -1;
    }

    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setPageColor(int pageColor) {
        mPaintPageFill.setColor(pageColor);
        invalidate();
    }

    public int getPageColor() {
        return mPaintPageFill.getColor();
    }

    public void setFillColor(int fillColor) {
        mPaintFill.setColor(fillColor);
        invalidate();
    }

    public int getFillColor() {
        return mPaintFill.getColor();
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                mOrientation = orientation;
                requestLayout();
                break;

            default:
                throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setStrokeColor(int strokeColor) {
        mPaintStroke.setColor(strokeColor);
        invalidate();
    }

    public int getStrokeColor() {
        return mPaintStroke.getColor();
    }

    public void setStrokeWidth(float strokeWidth) {
        mPaintStroke.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public float getStrokeWidth() {
        return mPaintStroke.getStrokeWidth();
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setSnap(boolean snap) {
        mSnap = snap;
        invalidate();
    }

    public boolean isSnap() {
        return mSnap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int count;
        count = bannerCount;
        if (count == 0 ||count==1) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        int longSize;
        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;
        if (mOrientation == HORIZONTAL) {
            longSize = getWidth();
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        final float threeRadius = mRadius * 2+mSpace;
        final float shortOffset = shortPaddingBefore + mRadius+mPaintStroke.getStrokeWidth();
        float longOffset = longPaddingBefore + mRadius;
        if (mCentered) {
            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - ((count * threeRadius-mSpace) / 2.0f);
        }

        float dX;
        float dY;

        float pageFillRadius = mRadius;
        if (mPaintStroke.getStrokeWidth() > 0) {
            pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
        }
        if (mItemInfos==null) return;
        mItemInfos.clear();
        //Draw stroked circles
        for (int iLoop = 0; iLoop < count; iLoop++) {
            float drawLong = longOffset + (iLoop * threeRadius);
            if (mOrientation == HORIZONTAL) {
                dX = drawLong;
                dY = shortOffset;
            } else {
                dX = shortOffset;
                dY = drawLong;
            }
            ItemInfo itemInfo = new ItemInfo();
            itemInfo.cx = dX;
            itemInfo.cy = dY;
            itemInfo.radius = mRadius;
            mItemInfos.add(itemInfo);
            // Only paint fill if not completely transparent
            if (mPaintPageFill.getAlpha() > 0) {
                canvas.drawCircle(dX, dY, pageFillRadius, mPaintPageFill);
            }

            // Only paint stroke if a stroke width was non-zero
            if (pageFillRadius != mRadius) {
                canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
            }

        }

        //Draw the filled circle according to the current scroll
        float cx = (mSnap ? mSnapPage : mCurrentPage) * threeRadius;
        if (!mSnap) {
            cx += mPageOffset * threeRadius;
        }
        if (mOrientation == HORIZONTAL) {
            dX = longOffset + cx;
            dY = shortOffset;
        } else {
            dX = shortOffset;
            dY = longOffset + cx;
        }

        float lastCx = mItemInfos.get(mItemInfos.size() - 1).cx;
        float firstCx = mItemInfos.get(0).cx;

        if (dX>lastCx)
            dX = lastCx;
        if (dX<firstCx)
            dX = firstCx;
        canvas.drawCircle(dX, dY, mRadius, mPaintFill);
    }

    public void setPagerRecyclerView(PagerRecyclerView view,@IntRange(from = 1)  final int bannerCount) {
        this.bannerCount = bannerCount;
        if (mPagerRecyclerView == view) {
            return;
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        mPagerRecyclerView = view;
        mPagerRecyclerView.removeOnPageChangeListener(this);
        mPagerRecyclerView.addOnPageChangeListener(this);
        mPagerRecyclerView.addOnDetachListener(this);
        addBanner();
        if (isSupportLoop()&&mCurrentPage == 0) {
            setCurrentItem(bannerCount);
        }
        invalidate();
        startLoop();
    }

    private boolean isSupportLoop() {
        if (bannerCount==1||bannerCount==0)
            return false;
        return true;
    }

    /**
     * 开始循环
     */
    public void startLoop() {
        startLoop(bannerLoopDuration);
    }

    /**
     * 循环时间
     * @param bannerLoopDuration
     */
    public void setBannerLoopDuration(long bannerLoopDuration) {
        this.bannerLoopDuration = bannerLoopDuration;
    }

    /**
     * 开始循环
     */
    public void startLoop(long duration) {
        if (mHandler==null) {
            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what) {
                        case WHAT_BANNER_LOOP:
                            setCurrentItem(mCurrentPosition+1,true);
                            return true;
                    }
                    return false;
                }
            });
        }
        if (!isSupportLoop())
            return;
        isLoopStop = false;
        setBannerLoopDuration(duration);
        nextBanner();
    }

    /**
     * 跳到下一个banner
     */
    private void nextBanner() {
        if (mHandler==null)
            return;
        if (!isSupportLoop())
            return;
        if (isLoopStop)
            return;
        mHandler.removeMessages(WHAT_BANNER_LOOP);
        mHandler.sendEmptyMessageDelayed(WHAT_BANNER_LOOP,bannerLoopDuration);
    }

    /**
     * 停止循环
     */
    public void stopLoop() {
        suspendLoop();
        isLoopStop = true;

    }
    /**
     * 中断循环（并不会停止，在如果在循环，则中断）
     */
    public void suspendLoop() {
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_BANNER_LOOP);
        }
    }

    public void setPagerRecyclerView(PagerRecyclerView view, int initialPosition,@IntRange(from = 1) int bannerCount) {
        setPagerRecyclerView(view,bannerCount);
        setCurrentItem(initialPosition);
    }

    /**
     * @param item 设置当前选中的item
     */
    public void setCurrentItem(int item) {
        setCurrentItem(item,false);
    }

    /**
     *
     * @param item 设置当前选中的item
     * @param smoothScroll 是否慢速滑动
     */
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (mPagerRecyclerView == null) {
            throw new IllegalStateException("PagerRecyclerView has not been bound.");
        }
        if (bannerCount==0||bannerCount==1)
            return;
        if (item == 0) {
            addBanner();
            setCurrentItem(bannerCount);
        } else {
            addBanner(item);
            mPagerRecyclerView.setCurrentItem(item,smoothScroll);
            mCurrentPosition = item;
            mCurrentPage = item%bannerCount;
            invalidate();
        }
    }

    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    public void onDestroy() {
        if (mPagerRecyclerView != null) {
            mPagerRecyclerView.removeAllOnPageChangeListener();
            mPagerRecyclerView = null;
        }

        if (mHandler != null) {
            mHandler.removeMessages(WHAT_BANNER_LOOP);
            mHandler = null;
        }

        if (mItemInfos != null) {
            mItemInfos.clear();
            mItemInfos=null;
        }

        mGestureDetector = null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        suspendLoop();
        mCurrentPage = position%bannerCount;
//        Log.e("onPageScrolled()", "position:" + position + ",mCurrentPage:" + mCurrentPage+",positionOffset:"+positionOffset+",positionOffsetPixels:"+positionOffsetPixels);
        mCurrentPosition = position;
        mPageOffset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(final int position) {
        if (mSnap || (mScrollState == ViewPager.SCROLL_STATE_IDLE)||mPagerRecyclerView!=null) {
            nextBanner();
            mPagerRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addBanner(position);
                    if (position==0)
                        setCurrentItem(bannerCount);
                }
            }, 0);
            mCurrentPage = position%bannerCount;
            mCurrentPosition = position;
            mSnapPage = position%bannerCount;
            mPageOffset = 0f;
            invalidate();
        }

    }

    private boolean isAddBanner(int position) {
        if (!isSupportLoop())
            return false;
        if (mPagerRecyclerView==null)
            return false;
        RecyclerView.Adapter adapter = mPagerRecyclerView.getAdapter();
        if (adapter == null) return false;
        if (position+1==adapter.getItemCount())
            return true;
        return false;
    }

    private void addBanner(int position) {
        if (isAddBanner(position)) {
            addBanner();
        }
    }
    private void addBanner() {
        if (!isSupportLoop())
            return;
        if (mPagerRecyclerView==null)
            return;
        RecyclerView.Adapter adapter = mPagerRecyclerView.getAdapter();
        if (adapter == null) return;
        if (adapter instanceof BasePagerRecyclerAdapter) {
            BasePagerRecyclerAdapter pagerRecyclerAdapter = (BasePagerRecyclerAdapter) adapter;
            List data = pagerRecyclerAdapter.getData();
            pagerRecyclerAdapter.addData(data);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || mPagerRecyclerView == null) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            final int count;
            count = mPagerRecyclerView.getAdapter().getItemCount();

            result = (int) (getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mSpace + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 2*mPaintStroke.getStrokeWidth());
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        mCurrentPosition = savedState.currentPosition;
        mSnapPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
            dest.writeInt(currentPosition);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    class ItemInfo {
        public float cx;
        public float cy;
        public float radius;
        public int position;
    }

    class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            int position = getPosition(x, y);
            if (position >= 0) {
                setCurrentItem(position);
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }
}
