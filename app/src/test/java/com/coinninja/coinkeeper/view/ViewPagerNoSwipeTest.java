package com.coinninja.coinkeeper.view;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import androidx.viewpager.widget.ViewPager;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ViewPagerNoSwipeTest {


    private ViewPager pager;

    @Before
    public void setUp() {
        pager = new ViewPagerNoSwipe(RuntimeEnvironment.application.getApplicationContext(), null);
    }


    @Test
    public void eats_touch() {
        assertFalse(pager.onTouchEvent(mock(MotionEvent.class)));
    }

    @Test
    public void eats_motion() {
        assertFalse(pager.onInterceptTouchEvent(mock(MotionEvent.class)));
    }

    @Test
    public void eats_keys() {
        assertFalse(pager.executeKeyEvent(mock(KeyEvent.class)));
    }

}