package com.wqlin.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
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

import com.github.rubensousa.gravitysnaphelper.R;
import com.wqlin.snap.GravityPagerSnapHelper;
import com.wqlin.snap.GravitySnapHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wqlin on 2017/10/23. <p>
 * 实现ViewPager的效果 </p>
 * <p>
 *  属性设置见 attrs.xml中的PagerRecyclerView ,item_gravity 默认START
 * </p>
 * <p>
 *   **************
 *  <p>
 *  isPager时ItemDecoration必须使用PageItemDecoration;其实左对齐时 右对齐 中间对齐是把ItemView的左右间隔算成了ItemView的宽度，所以设置左对齐时，
 *  不想左边有间隔请请把ItemView的左间隔设置成0，右对齐时，不想右边有间隔请请把ItemView的右间隔设置成0，中间对齐时，想要居中请请把ItemView的左右间隔设置成相等；
 * </p>
 * Adapter必须使用{@link BasePagerRecyclerAdapter}  <p>
 * {@link #isFlingMorePage} 快速滑动是否翻动多页 <p>
 * {@link #isPager} 是否需要设置成ViewPager效果  不是则为正常RecyclerView   false时会清除所有滑动监听
 */

public class PagerRecyclerView extends RecyclerView {
    private final String TAG = PagerRecyclerView.class.getSimpleName();

    public final static int CENTER = 0;
    public final static int START = 1;
    public final static int END = 2;

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
    private int gravity = CENTER;
    private int snapGravity = Gravity.CENTER;

    private PageItemDecoration mPageItemDecoration;
    private PagerOnScrollListener mPagerOnScrollListener;
    private List<OnPageChangeListener> mOnPageChangeListeners = new ArrayList<>();
    private List<OnDestroyListener> mOnDestroyListeners = new ArrayList<>();

    public PagerRecyclerView(Context context) {
        this(context, null);
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context,attrs,defStyle);
        init();
    }

    private void initAttrs(Context context,AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerRecyclerView, defStyle, 0);
            setGravity(a.getInt(R.styleable.PagerRecyclerView_item_gravity, START));
        }
    }

    private void setGravity(@IntRange(from = 0,to = 2) int gravity) {
        this.gravity = gravity;
        snapGravity = getSnapGravity(gravity);
    }

    private int getSnapGravity(int gravity) {
        int snapGravity = Gravity.CENTER;

        switch (gravity) {
            case START:
                snapGravity = Gravity.START;
                break;
            case CENTER:
                snapGravity = Gravity.CENTER;
                break;
            case END:
                snapGravity = Gravity.END;
                break;
        }
        return snapGravity;
    }

    private void init() {
        if (!isPager)
            return;

        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        setSnapHelper(isFlingMorePage,snapGravity);
        mPagerOnScrollListener = new PagerOnScrollListener();
        addOnScrollListener(mPagerOnScrollListener);
        /*addOnPageChangeListener(new OnPageChangeListener() {
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

            @Override
            public void onDetachedFromRecyclerView() {
                log("onPageScrollStateChanged() "+"onDetachedFromRecyclerView()");
            }
        });*/
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }

    private void setSnapHelper(boolean flingMorePage,int snapGravity) {
        if (!isPager)
            return;
        if (flingMorePage) {
            if (snapGravity != Gravity.START && snapGravity != Gravity.END
                    && snapGravity != Gravity.BOTTOM && snapGravity != Gravity.TOP) {
                new LinearSnapHelper().attachToRecyclerView(this);
            } else {
                new GravitySnapHelper(snapGravity).attachToRecyclerView(this);
            }
        } else {
            if (snapGravity != Gravity.START && snapGravity != Gravity.END
                    && snapGravity != Gravity.BOTTOM && snapGravity != Gravity.TOP) {
                new PagerSnapHelper().attachToRecyclerView(this);
            } else {
                new GravityPagerSnapHelper(snapGravity).attachToRecyclerView(this);
            }
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
    public void setCurrentItem(final int item, boolean smoothScroll) {
        if (!isPager)
            return;
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (smoothScroll) {
                linearLayoutManager.smoothScrollToPosition(PagerRecyclerView.this,null,item);
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        linearLayoutManager.scrollToPositionWithOffset(item,getPageX(item,null,false));
                    }
                }, 0);
            }
        }
    }
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    public void addOnDetachListener(OnDestroyListener listener) {
        if (mOnDestroyListeners == null) {
            mOnDestroyListeners = new ArrayList<>();
        }
        mOnDestroyListeners.add(listener);
    }

    public void removeAllOnPageChangeListener() {
        if (mOnPageChangeListeners != null) {
            for (OnPageChangeListener listener :
                    mOnPageChangeListeners) {
                if (listener != mPagerOnScrollListener)
                    mOnPageChangeListeners.remove(listener);
            }

        }
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
            setSnapHelper(flingMorePage,snapGravity);
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
    private int getPageX(int position,View currentView) {
        return getPageX(position, currentView, true);
    }

    /**
     *
     * @param position
     * @param currentView
     * @param isCalculationDecoration 是否计算Decoration 当使用linearLayoutManager.scrollToPositionWithOffset()时就不要计算
     * @return
     */
    private int getPageX(int position,View currentView,boolean isCalculationDecoration) {
        int pageX =getPaddingLeft();
        Adapter adapter = getAdapter();
        if ( adapter== null) {
            return 0;
        }
        int count = getChildCount();
        if ( count == 0) {
            return 0;
        }

        if (currentView==null)
            currentView = getChildAt(0);
        int deLeft = getDecorationLeft(position);
        int deRight = getDecorationRight(position);
        int childWidth = currentView.getWidth();
        int width = getWidth();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        switch (gravity) {
            case START:
                if (position == getAdapter().getItemCount() - 1) {
                    pageX = width - paddingRight - childWidth -deRight;
                } else {
                    pageX = paddingLeft+deLeft;
                }
                break;
            case END:
                if (position == 0) {
                    pageX = paddingLeft+deLeft;
                } else{
                    pageX = width - paddingRight - childWidth -deRight;
                }
                break;
            case CENTER:
                if (position == 0) {
                    pageX = paddingLeft+deLeft;
                } else if (position == getAdapter().getItemCount() - 1) {
                    pageX = width - paddingRight - childWidth-deRight;
                } else {
                    pageX = ((width-childWidth-deLeft-deRight)/2)+deLeft;
                }
                break;

        }
        if (!isCalculationDecoration) {
            pageX -= deLeft;
        }
        return pageX;
    }

    private int getAllWidth(View view,int position) {
        int deLeft = getDecorationLeft(position);
        int deRight = getDecorationRight(position);
        return view.getWidth() + deLeft + deRight;
    }
    private int getDecorationLeft(int position) {
        if (mPageItemDecoration==null) return 0;

        return mPageItemDecoration.getLeftWidth(position,this);
    }

    private int getDecorationRight(int position) {
        if (mPageItemDecoration==null) return 0;

        return mPageItemDecoration.getRightWidth(position,this);
    }

    public void addItemDecoration(ItemDecoration decor, int index) {
        if (decor==null) return;
        if (isPager) {
            if (decor instanceof PageItemDecoration) {
                if (mPageItemDecoration != null) {
                    removeItemDecoration(mPageItemDecoration);
                }
                mPageItemDecoration = (PageItemDecoration) decor;
            } else {
                throw new ClassCastException("PagerRecyclerView is isPager,decor is not PageItemDecoration");
            }
        }
        super.addItemDecoration(decor,index);
    }
    class PagerOnScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!isPager)
                return;
            if (recyclerView.getAdapter()==null)
                return;

            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                View currentView;
                int position = 0;
                currentView = getChildAt(0);
                int childCount = getChildCount();
                int vWidth0 = 0;
                for (int i = 0; i < childCount; i++) {
                    View childView = getChildAt(i);
                    Rect r = new Rect();
                    childView.getLocalVisibleRect(r); //获取在当前窗口内的绝对坐标
                    /*int visibleLeft = r.left;
                    int visibleRight = r.right;*/
                    if (r.left >= 0) {
                        int vWidth = r.right - r.left;
//                        int tPosition = linearLayoutManager.getPosition(childView);
//                        Log.e("onPageScrolled() recyclerview", "position:" + tPosition + ",vWidth:" + vWidth+",r.right:"+r.right+",r.left:"+r.left);
                        if (vWidth > vWidth0) {
                            vWidth0 = vWidth;
                            position = linearLayoutManager.getPosition(childView);
                            currentView = getChildAt(i);
                        }
                    }

                }

                int pageX=getPageX(position,currentView) ;

                int width = currentView.getWidth();
                int positionOffsetPixels = 0;
                float startX = currentView.getX();
                positionOffsetPixels = (int) (pageX - startX);
                float positionOffset = (positionOffsetPixels*1f) / (width*1f);
                int size=mOnPageChangeListeners.size();
                if (size>0) {
                    if (startX==pageX) {
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
            if (recyclerView.getAdapter()==null)
                return;
            int size=mOnPageChangeListeners.size();
            if (size>0) {
                for (int i = 0; i <size; i++) {
                    mOnPageChangeListeners.get(i).onPageScrollStateChanged(newState);
                }
            }
        }
    }

    @Override
    public void scrollBy(int x, int y) {
//        if (getScrollState() != SCROLL_STATE_DRAGGING) {
////            invokeMethod(this, "setScrollState",new Class[]{int.class} , new Object[]{SCROLL_STATE_DRAGGING});
//            setSuperField(this,"mScrollState",SCROLL_STATE_DRAGGING);
//        }
        super.scrollBy(x, y);
    }

    public void endScrollBy() {
        if (!isPager)
            return;
        LayoutManager layoutManager = this.getLayoutManager();
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
            Rect r = new Rect();
            currentView.getLocalVisibleRect(r); //获取在当前窗口内的绝对坐标
            int visibleLeft = r.left;
            int visibleRight = r.right;
            int width = currentView.getWidth();
            int positionOffsetPixels = 0;
            if (visibleLeft > 0 ) {
                positionOffsetPixels = visibleLeft;
            }
            if (visibleRight < width) {
                positionOffsetPixels = visibleRight - width;
            }
            float positionOffset = (positionOffsetPixels*1f) / (width*1f);
            int netPosition=position ;
            if (positionOffset < -0.5) {
                netPosition = mCurrentPosition - 1;

            } else if (positionOffset > 0.5){
                netPosition = mCurrentPosition + 1;
            }
            netPosition = netPosition < 0 ? 0 : netPosition;
            int count = getAdapter().getItemCount() - 1;
            netPosition = netPosition > count ? count : netPosition;
            setCurrentItem(netPosition,true);

        }
    }
    public static void setSuperField(Object object, String fieldName, Object value) {
        try {
            Class superclass=object.getClass().getSuperclass();
            Field f=superclass.getDeclaredField(fieldName);
            f.setAccessible(true);//为 true 则表示反射的对象在使用时取消 Java 语言访问检查
            f.set(object,value);
        } catch (Exception ep) {
            ep.printStackTrace();
        }

    }
    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */

    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes){
        Method method = null ;

        for(Class<?> clazz = object.getClass(); clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes) ;
                return method ;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }

        return null;
    }
    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected, default)
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @param parameters : 父类中的方法参数
     * @return 父类中方法的执行结果
     */

    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes,
                                      Object[] parameters) {
        //根据 对象、方法名和对应的方法参数 通过反射 调用上面的方法获取 Method 对象
        Method method = getDeclaredMethod(object, methodName, parameterTypes) ;

        //抑制Java对方法进行检查,主要是针对私有方法而言
        method.setAccessible(true) ;

        try {
            if(null != method) {

                //调用object 的 method 所代表的方法，其方法的参数是 parameters
                return method.invoke(object, parameters) ;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void destroy() {
        if (mOnDestroyListeners != null) {
            for (OnDestroyListener listener :
                    mOnDestroyListeners) {
                listener.onDestroy();
            }
        }
        if (mOnPageChangeListeners!=null)
            mOnPageChangeListeners.clear();
        if (mOnDestroyListeners !=null)
            mOnDestroyListeners.clear();

        mPageItemDecoration = null;
    }
    public interface OnPageChangeListener {

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);


    }

    public interface OnDestroyListener {
        void onDestroy();
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
        public BaseViewHolder(final View view, int childIndex) {
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
