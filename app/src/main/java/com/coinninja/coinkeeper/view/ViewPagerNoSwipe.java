package com.coinninja.coinkeeper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;


public class ViewPagerNoSwipe extends ViewPager {

    public ViewPagerNoSwipe(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        return false;
    }

}
