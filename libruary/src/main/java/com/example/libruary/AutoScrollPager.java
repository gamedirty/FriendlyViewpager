package com.example.libruary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.zhaocai.mall.android305.R;
import com.zhaocai.mall.android305.entity.Configurable;
import com.zhaocai.mall.android305.entity.RenderConfig;
import com.zhaocai.mall.android305.presenter.BaseApplication;
import com.zhaocai.util.info.android.Logger;
import com.zhaocai.zchat.utils.ViewUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ivey on 2016/6/29.
 */
public abstract class AutoScrollPager<T> extends FrameLayout {

    private static final int DURATION_FADE = 500;
    protected ObservaleViewpager mVBanner;
    private BannerAdapter mBannerAdapter;
    protected PagerIndicator mVIndicator;
    private View mVRadian;

    private ViewPager.SimpleOnPageChangeListener onPageChangeListener;

    private List<T> mDatas = new ArrayList<T>();
    protected Configurable mConfiguration;
    private boolean flagTouching;//标记是否正在触摸
    private MultiTransformer transformer;

    public AutoScrollPager(Context context) {
        super(context);
        init();
    }

    public AutoScrollPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoScrollPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_mall_header, this, true);

        mVBanner = (ObservaleViewpager) findViewById(R.id.banner);
        mVIndicator = (PagerIndicator) findViewById(R.id.pager_indicator);
        mVRadian = findViewById(R.id.bottom_radian);
        mVBanner.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mVIndicator.setCurPoint(position % mDatas.size());
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(getRealPosition(position));
                }

            }

        });
    }

    /**
     * 效果：自动轮播的时候渐变显示，手指滑动时候平移
     * <p>
     * 设置transformer 两种状态平移和渐变
     * 轮播timer设置flag 标记是否轮播
     * 设置vp触摸监听
     *
     * 默认trasformer状态是fade
     * 手指触摸时候停止自动轮播，轮播结束切换为平移模式，轮播开始前切换为fade模式
     */

    /**
     * 供子类控制fade是否起作用
     */
    public void enableFade() {
        transformer = new MultiTransformer();
        setViewPagerScrollSpeed(mVBanner, DURATION_FADE);
        mVBanner.setPageTransformer(true, transformer);
        mVBanner.setOntouchWatcher(new ObservaleViewpager.OntouchWatcher() {
            boolean hasSet;

            @Override
            public void onTouch(MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        setFlagTouching(true);
                        if (transformer != null) {
                            transformer.setCanFade(false);
                            if (!hasSet) {
                                int nowPosition = mVBanner.getCurrentItem();
                                nowPosition += mDatas.size()*2;//Viewpager缓存了前后共三个item 为了防止transformer由fade切换后缓存
                                // item显示异常，位移两个周期（周期长度为2时候 前一个仍为上次滑动缓存item，故统一偏移两倍周期）
                                mVBanner.setCurrentItem(nowPosition, false);
                                hasSet = true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        setFlagTouching(false);
                        hasSet = false;
                        break;
                }
            }
        });

        mVBanner.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Logger.i("zhjh", "onPageSelected");
                //onpageselected 在setcurent的时候立即调用的，故而延迟transformer模式切换
                if (transformer != null) {
                    BaseApplication.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            transformer.setCanFade(false);
                        }
                    }, DURATION_FADE + 10);
                }
            }
        });
    }

    public void setConfiguration(Configurable config) {
        mConfiguration = config;
    }

    protected boolean isShowBmp() {
        if (mConfiguration != null && mConfiguration instanceof RenderConfig) {
            return ((RenderConfig) mConfiguration).isShowBmp();
        }

        return true;
    }

    public void setBottomRadian(Drawable drawable) {
        mVRadian.setBackgroundDrawable(drawable);
        ViewUtil.setVisibility(View.VISIBLE, mVRadian);
    }

    /**
     * 控制是否展示指示点
     *
     * @param show
     */
    public void showIndicator(final boolean show) {
        final int visibility = show ? View.VISIBLE : View.INVISIBLE;
        ViewUtil.setVisibility(visibility, mVIndicator);
    }

    public void setData(List<T> datas) {
        mDatas.clear();
        if (mDatas != null) mDatas.addAll(datas);

        mVIndicator.setAllPoints(mDatas.size());
        synchronized (Object.class) {
            if (mBannerAdapter == null) {
                mBannerAdapter = new BannerAdapter();
                mVBanner.setAdapter(mBannerAdapter);
                mVBanner.setCurrentItem(30 - 30 % mDatas.size());
                autoSlideStart();
            } else {
                mBannerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void autoSlideStop() {
        if (mBannerAdapter != null) mBannerAdapter.cancelAutoSlide();
    }

    public void autoSlideStart() {
        if (mBannerAdapter != null) mBannerAdapter.startAutoSlide();
    }

    public ViewPager.SimpleOnPageChangeListener getOnPageChangeListener() {
        return onPageChangeListener;
    }

    public void setOnPageChangeListener(ViewPager.SimpleOnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    private class BannerAdapter extends PagerAdapter {

        private static final String TAG = "BannerAdapter";

        private Timer timer;
        private TimerTask timerTask;

        private static final int PERIOD = 5 * 1000;

        private BannerAdapter() {
        }

        private void startAutoSlide() {
            if (timer != null) timer.cancel();
            timer = null;
            timerTask = null;
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (transformer != null)
                        transformer.setCanFade(true);
                    BaseApplication.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (getCount() == 0 || mDatas.size() <= 1) {
                                return;
                            }
                            if (flagTouching) return;

                            int nowPosition = mVBanner.getCurrentItem();
                            nowPosition++;
                            mVBanner.setCurrentItem(nowPosition, true);
                        }
                    });
                }
            };
            timer.schedule(timerTask, PERIOD, PERIOD);
        }

        private void cancelAutoSlide() {
            if (timer != null) timer.cancel();
            timer = null;
            timerTask = null;
        }

        @Override
        public int getCount() {
            if (mDatas == null || mDatas.isEmpty())
                return 0;
            return Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % mDatas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final T data = mDatas.get(position % mDatas.size());
            View view = getItemView(data);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    protected void setFlagTouching(boolean touching) {
        this.flagTouching = touching;
        if (touching) mBannerAdapter.cancelAutoSlide();
        else {
            mBannerAdapter.startAutoSlide();
        }
    }

    protected abstract View getItemView(T data);

    private int getRealPosition(int position) {
        return position % mDatas.size();
    }



    /**
     * 设置viewpager切换速率
     * @param viewPager
     * @param speed
     */
    private void setViewPagerScrollSpeed(ViewPager viewPager, int speed) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller viewPagerScroller = new FixedSpeedScroller(viewPager.getContext(), new AccelerateDecelerateInterpolator());
            field.set(viewPager, viewPagerScroller);
            viewPagerScroller.setmDuration(speed);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }




}