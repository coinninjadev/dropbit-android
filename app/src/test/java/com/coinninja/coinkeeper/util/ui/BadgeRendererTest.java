package com.coinninja.coinkeeper.util.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BadgeRendererTest {

    @Mock
    private Bitmap bitmap;
    @Mock
    private BitmapDrawable drawable;
    @Mock
    private Resources resources;
    @Mock
    private Toolbar toolbar;
    @Mock
    private ImageView imageView;

    private BadgeRenderer badgeRenderer;

    @Before
    public void setUp() {
        badgeRenderer = new BadgeRenderer();
        when(imageView.getResources()).thenReturn(resources);
        when(toolbar.getResources()).thenReturn(resources);
        when(drawable.getBitmap()).thenReturn(bitmap);
    }

    @Test
    public void sets_badger_on_provided_imageview() {
        ArgumentCaptor<BadgeOverlay> argumentCaptor = ArgumentCaptor.forClass(BadgeOverlay.class);
        when(imageView.getDrawable()).thenReturn(drawable);

        badgeRenderer.renderBadge(imageView);

        verify(imageView).setImageDrawable(argumentCaptor.capture());
        BadgeOverlay overlay = argumentCaptor.getValue();
        assertThat(overlay.getResources(), equalTo(resources));
        assertThat(overlay.getBaseBitmap(), equalTo(bitmap));
    }


    @Test
    public void does_not_double_wrap_imageview() {
        when(imageView.getDrawable()).thenReturn(mock(BadgeOverlay.class));

        badgeRenderer.renderBadge(imageView);

        verify(imageView, times(0)).setImageDrawable(any());
    }


    @Test
    public void sets_badger_on_toolbars_navigation_icon() {
        ArgumentCaptor<BadgeOverlay> argumentCaptor = ArgumentCaptor.forClass(BadgeOverlay.class);
        when(toolbar.getNavigationIcon()).thenReturn(drawable);

        badgeRenderer.renderBadge(toolbar);

        verify(toolbar).setNavigationIcon(argumentCaptor.capture());
        BadgeOverlay overlay = argumentCaptor.getValue();
        assertThat(overlay.getResources(), equalTo(resources));
        assertThat(overlay.getBaseBitmap(), equalTo(bitmap));
    }

    @Test
    public void does_not_double_wrap_toolbar_navigation() {
        when(toolbar.getNavigationIcon()).thenReturn(mock(BadgeOverlay.class));

        badgeRenderer.renderBadge(toolbar);

        verify(toolbar, times(0)).setNavigationIcon(any());
    }

}