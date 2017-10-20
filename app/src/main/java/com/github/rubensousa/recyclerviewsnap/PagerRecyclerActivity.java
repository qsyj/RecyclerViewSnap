package com.github.rubensousa.recyclerviewsnap;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.wqlin.widget.CirclePageIndicator;
import com.wqlin.widget.PagerRecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PagerRecyclerActivity extends AppCompatActivity {

    private PagerRecyclerView mPagerRecyclerView;
    private CirclePageIndicator mCirclePageIndicator;
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
    private LLYPagerAdapter mAdapter;
    private MyRunnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_recycler);
        mPagerRecyclerView = (PagerRecyclerView) findViewById(R.id.pagerRecyclerView);
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
        },0,5000);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mPagerRecyclerView.setCurrentItem(2);
                mPagerRecyclerView.setFlingMorePage(!mPagerRecyclerView.isFlingMorePage());
            }
        }, 5000);*/

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
        mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);

        mAdapter = new LLYPagerAdapter(getChildNum());
        mList = new ArrayList<ItemEntity>();

        for (int i = 0; i < 11; i++) {
            mList.add(new ItemEntity("child" + i,res[i]));
        };
        mAdapter.setDatas(mList);
        mPagerRecyclerView.setAdapter(mAdapter);
        mCirclePageIndicator.setPagerRecyclerView(mPagerRecyclerView);
        return true;
    }

    private int getChildNum() {
        int childNum = 3;
        if (!isPager)
            childNum = 1;
        return childNum;
    }
    private void switchPager() {
        mAdapter.setDatas(null);
        mPagerRecyclerView.getRecycledViewPool().clear();
        mPagerRecyclerView.removeAllViews();
        if (!isPager) {
            mPagerRecyclerView.setLayoutManager(new LinearLayoutManager(PagerRecyclerActivity.this));
        }
        mAdapter.setChildNum(getChildNum());
        mAdapter.setDatas(mList);
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
