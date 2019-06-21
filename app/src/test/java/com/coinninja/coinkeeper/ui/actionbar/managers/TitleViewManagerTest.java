package com.coinninja.coinkeeper.ui.actionbar.managers;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TitleViewManagerTest {

    @Mock
    ActionBar actionBar;

    @Mock
    TextView titleView;

    TitleViewManager titleViewManager;

    @Before
    public void setUp() throws Exception {
        titleViewManager = new TitleViewManager();
        titleViewManager.setActionBar(actionBar);
        titleViewManager.setTitleView(titleView);
    }

    @After
    public void tearDown() throws Exception {
        titleViewManager = null;
        titleView = null;
        actionBar = null;
    }

    @Test
    public void set_title_to_app_bar() {
        String title = " --- TITLE --";
        when(actionBar.getTitle()).thenReturn(title);

        titleViewManager.renderTitleView();

        verify(titleView).setVisibility(View.VISIBLE);
        verify(titleView).setText(title);
    }

    @Test
    public void set_title_to_app_bar_directly() {
        String title = " --- TITLE --";
        String titleWeDoNotWant = " --- TITLE BAD --";

        titleViewManager.renderTitleView(title);

        verify(titleView).setVisibility(View.VISIBLE);
        verify(titleView, never()).setText(titleWeDoNotWant);
        verify(titleView).setText(title);
    }

    @Test
    public void uppercase_title_text() {
        String title = " --- the title --";
        when(actionBar.getTitle()).thenReturn(title);

        titleViewManager.renderUpperCaseTitleView();

        verify(titleView).setVisibility(View.VISIBLE);
        verify(titleView).setText(" --- THE TITLE --");
    }

    @Test
    public void remove_title_bar_when_text_is_empty() {
        String title = "";
        when(actionBar.getTitle()).thenReturn(title);

        titleViewManager.renderUpperCaseTitleView();

        verify(titleView).setVisibility(View.GONE);
    }

    @Test
    public void clear_action_bar_title_when_getting_title() {
        String title = "";
        when(actionBar.getTitle()).thenReturn(title);

        String actionBarTitle = titleViewManager.getTitle();

        assertThat(title, equalTo(actionBarTitle));
        verify(actionBar).setTitle("");
    }

    @Test
    public void return_false_when_text_is_null() {
        String someTitle = null;

        boolean isTitleValid = titleViewManager.isTitleValid(someTitle);

        assertFalse(isTitleValid);
    }

    @Test
    public void return_false_when_text_is_empty() {
        String someTitle = "";

        boolean isTitleValid = titleViewManager.isTitleValid(someTitle);

        assertFalse(isTitleValid);
    }

    @Test
    public void return_true_when_text_is_has_text() {
        String someTitle = "-- come title";

        boolean isTitleValid = titleViewManager.isTitleValid(someTitle);

        assertTrue(isTitleValid);
    }

    @Test
    public void hide_title_view_on_empty_string() {
        titleViewManager.renderTitleView("");
        verify(titleView).setVisibility(View.GONE);
    }

    @Test
    public void hide_title_view_on_null() {
        titleViewManager.renderTitleView(null);
        verify(titleView).setVisibility(View.GONE);
    }
}