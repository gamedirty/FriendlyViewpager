package com.example.libruary;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * viewpager's ontouchevent can be detected by outside object by set an OntouchWatcher
 * <p>
 * Created by zhjh on 2017/5/4.
 */

public class ObservaleViewpager extends ViewPager {
    public ObservaleViewpager(Context context) {
        super(context);
    }

    public ObservaleViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ontouchWatcher != null) ontouchWatcher.onTouch(ev);
        return super.dispatchTouchEvent(ev);
    }

    interface OntouchWatcher {
        void onTouch(MotionEvent event);
    }

    private OntouchWatcher ontouchWatcher;

    public void setOntouchWatcher(OntouchWatcher ontouchWatcher) {
        this.ontouchWatcher = ontouchWatcher;
    }
}
