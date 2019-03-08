package com.coinninja.coinkeeper.util.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.coinninja.coinkeeper.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BadgeOverlayTest {

    @Mock
    Bitmap bitmap;

    @Mock
    Resources resources;

    @Mock
    Paint paint;

    @Mock
    Canvas canvas;

    BadgeOverlay badgeOverlay;

    @Before
    public void setUp() {
        badgeOverlay = new BadgeOverlay(resources, bitmap);
        badgeOverlay.paint = paint;
        when(resources.getDimension(R.dimen.badge_radius)).thenReturn(13F);
    }

    @After
    public void teardown() {
        //bitmap.recycle();
    }

    @Test
    public void paints_badge_in_upper_right_corner() {
        badgeOverlay.draw(canvas);
        verify(paint).setColor(anyInt());

        verify(canvas).drawCircle(anyFloat(), anyFloat(), eq(13F), eq(paint));
    }


}