package com.github.rubensousa.recyclerviewsnap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wqlin.widget.CirclePageIndicator;
import com.wqlin.widget.PagerRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PagerRecyclerActivity extends AppCompatActivity {

    private PagerRecyclerView mPagerRecyclerView;
    private CirclePageIndicator mCirclePageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_recycler);
        mPagerRecyclerView = (PagerRecyclerView) findViewById(R.id.pagerRecyclerView);
        mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
        LLYPagerAdapter adapter=new LLYPagerAdapter(3);
        List<String> list=new ArrayList<String>();

        for (int i = 0; i < 11; i++) {
            list.add(i+"");
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
}
