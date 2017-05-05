package com.example.libruary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by zhjh on 2017/5/5.
 */


public class FriendlyViewpager extends ViewPager {
    private Handler handler = new Handler(Looper.getMainLooper());
private int count;

    public FriendlyViewpager(Context context) {
        super(context);
    }

    public FriendlyViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
//        count = adapter==null?0:adapter.getCount();
//        if (count>1){
//            autoScroll();
//        }
        autoScroll();
    }

    /**
     * start auto scroll job
     */
    private void autoScroll() {

    }

    /**
     * interrupt the auto scroll
     */
    private void interruptScroll(){

    }

}
