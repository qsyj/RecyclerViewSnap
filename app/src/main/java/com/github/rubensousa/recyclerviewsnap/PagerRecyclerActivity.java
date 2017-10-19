package com.github.rubensousa.recyclerviewsnap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.wqlin.widget.CirclePageIndicator;
import com.wqlin.widget.PagerRecyclerView;

import java.util.ArrayList;
import java.util.List;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_recycler);
        mPagerRecyclerView = (PagerRecyclerView) findViewById(R.id.pagerRecyclerView);
        //是否设置成ViewPager效果
        mPagerRecyclerView.setPager(isPager);
        if (!isPager) {
            mPagerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
        int childNum = 3;
        if (!isPager)
            childNum = 1;
        LLYPagerAdapter adapter=new LLYPagerAdapter(childNum);
        List<ItemEntity> list=new ArrayList<ItemEntity>();

        for (int i = 0; i < 11; i++) {
            list.add(new ItemEntity("child" + i,res[i]));
        }
        adapter.setDatas(list);
        mPagerRecyclerView.setAdapter(adapter);
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //                mPagerRecyclerView.setCurrentItem(2);
//                mPagerRecyclerView.setFlingMorePage(!mPagerRecyclerView.isFlingMorePage());
//                timer.cancel();
            }
        },0,15000);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mPagerRecyclerView.setCurrentItem(2);
                mPagerRecyclerView.setFlingMorePage(!mPagerRecyclerView.isFlingMorePage());
            }
        }, 5000);*/
        mCirclePageIndicator.setPagerRecyclerView(mPagerRecyclerView);
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
