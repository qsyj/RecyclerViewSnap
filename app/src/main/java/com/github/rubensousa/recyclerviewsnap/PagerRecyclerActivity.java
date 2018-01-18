package com.github.rubensousa.recyclerviewsnap;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wqlin.widget.BannerCirclePageIndicator;
import com.wqlin.widget.BasePagerRecyclerAdapter;
import com.wqlin.widget.CirclePageIndicator;
import com.wqlin.widget.PageItemDecoration;
import com.wqlin.widget.PagerRecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PagerRecyclerActivity extends AppCompatActivity {

    private PagerRecyclerView mPagerRecyclerView;
    private BannerCirclePageIndicator mCirclePageIndicator;
    private boolean isPager=true;
    private int[] res = new int[]{R.drawable.ic_docs_48dp,
            R.drawable.ic_drive_48dp,
            R.drawable.ic_gmail_48dp,
            R.drawable.ic_google_48dp,
            R.drawable.ic_hangouts_48dp,
            R.drawable.ic_keep_48dp,
            R.drawable.ic_messenger_48dp,
            R.drawable.ic_inbox_48dp,
            R.drawable.ic_photos_48dp,
            R.drawable.ic_sheets_48dp,
            R.drawable.ic_docs_48dp,
            R.drawable.ic_drive_48dp,
            R.drawable.ic_gmail_48dp,
            R.drawable.ic_google_48dp,
            R.drawable.ic_hangouts_48dp,
            R.drawable.ic_keep_48dp,
            R.drawable.ic_messenger_48dp,
            R.drawable.ic_inbox_48dp,
            R.drawable.ic_photos_48dp,
            R.drawable.ic_sheets_48dp,
            R.drawable.ic_docs_48dp,
            R.drawable.ic_drive_48dp,
            R.drawable.ic_gmail_48dp,
            R.drawable.ic_google_48dp,
            R.drawable.ic_hangouts_48dp,
            R.drawable.ic_keep_48dp,
            R.drawable.ic_messenger_48dp,
            R.drawable.ic_inbox_48dp,
            R.drawable.ic_photos_48dp,
            R.drawable.ic_sheets_48dp,
            R.drawable.ic_docs_48dp,
            R.drawable.ic_drive_48dp,
            R.drawable.ic_gmail_48dp,
            R.drawable.ic_google_48dp,
            R.drawable.ic_hangouts_48dp,
            R.drawable.ic_keep_48dp,
            R.drawable.ic_messenger_48dp,
            R.drawable.ic_inbox_48dp,
            R.drawable.ic_photos_48dp,
            R.drawable.ic_sheets_48dp};
    private ArrayList<ItemEntity> mList;
    private BasePagerRecyclerAdapter mAdapter;
    private MyRunnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_recycler);
        mPagerRecyclerView = findViewById(R.id.pagerRecyclerView);
        PageItemDecoration pageItemDecoration=new PageItemDecoration() {
            Drawable mDrawable = null;
            @Override
            public Drawable getLeftDrawable(int position, RecyclerView recyclerView) {
                if (mDrawable == null) {
                    mDrawable = getResources().getDrawable(R.drawable.divider_hor_1dp_f6);
                }
                return mDrawable;
            }

            @Override
            public Drawable getRightDrawable(int position, RecyclerView recyclerView) {
                if (mDrawable == null) {
                    mDrawable = getResources().getDrawable(R.drawable.divider_hor_1dp_f6);
                }
                return mDrawable;
            }
        };
        mRunnable = new MyRunnable();
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //                mPagerRecyclerView.setCurrentItem(2);
//                mPagerRecyclerView.setFlingMorePage(!mPagerRecyclerView.isFlingMorePage());
//                timer.cancel();
                new Handler(getMainLooper()).postDelayed(mRunnable, 0);

            }
        },0,15000000);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mPagerRecyclerView.setCurrentItem(2);
                mPagerRecyclerView.setFlingMorePage(!mPagerRecyclerView.isFlingMorePage());
            }
        }, 5000);*/
        mPagerRecyclerView.addItemDecoration(pageItemDecoration);//decoration
    }
    class MyRunnable implements Runnable{

        @Override
        public void run() {
            //是否设置成ViewPager效果
            mPagerRecyclerView.setPager(isPager);
            if (!init()) {
                switchPager();
            }
            isPager = !isPager;
        }
    }
    private boolean init() {
        if (mCirclePageIndicator!=null)
            return false;
        if (!isPager) {
            mPagerRecyclerView.setLayoutManager(new LinearLayoutManager(PagerRecyclerActivity.this));
        }
        mCirclePageIndicator = (BannerCirclePageIndicator) findViewById(R.id.page_indicator);

        mAdapter = new LLYPagerAdapter(getChildNum());
        mList = new ArrayList<ItemEntity>();

        for (int i = 0; i < 12; i++) {//12
            mList.add(new ItemEntity("child" + i,res[i]));
        };
        mAdapter.setNewData(mList);
        mPagerRecyclerView.setAdapter(mAdapter);
        mCirclePageIndicator.setPagerRecyclerView(mPagerRecyclerView,4);
//        mCirclePageIndicator.setPagerRecyclerView(mPagerRecyclerView);
        return true;
    }

    private int getChildNum() {
        int childNum = 3;
        if (!isPager)
            childNum = 1;
        return childNum;
    }
    private void switchPager() {
        mAdapter.setNewData(null);
        mPagerRecyclerView.getRecycledViewPool().clear();
        mPagerRecyclerView.removeAllViews();
        if (!isPager) {
            mPagerRecyclerView.setLayoutManager(new LinearLayoutManager(PagerRecyclerActivity.this));
        }
        mAdapter.setChildNum(getChildNum());
        mAdapter.setNewData(mList);
    }
    public static class ItemEntity{
        private String text;
        private int imgRes;

        public ItemEntity(String text, int imgRes) {
            this.text = text;
            this.imgRes = imgRes;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getImgRes() {
            return imgRes;
        }

        public void setImgRes(int imgRes) {
            this.imgRes = imgRes;
        }
    }
}
