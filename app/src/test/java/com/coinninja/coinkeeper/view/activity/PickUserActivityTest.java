package com.coinninja.coinkeeper.view.activity;

import android.app.SearchManager;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.adapter.util.PickUserViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PickUserActivityTest {

    private ActivityController<PickUserActivity> activityController;

    @After
    public void tearDown() {
        activityController = null;
    }

    @Before
    public void setUp() {
        activityController = Robolectric.buildActivity(PickUserActivity.class);
        activityController.create();
    }

    @Test
    public void renders_recycle_view() {
        start();
        assertNotNull(activityController.get().findViewById(R.id.list_contacts));
    }

    @Test
    public void send_analytics_when_clicking_contact_send_button_test() {
        start();
        Contact contact = mock(Contact.class);
        when(contact.isVerified()).thenReturn(true);

        activityController.get().reportAnalytics(contact);

        verify(activityController.get().analytics).trackEvent(Analytics.EVENT_CONTACT_SEND_BTN);
    }

    @Test
    public void send_analytics_when_clicking_dropbit_send_button_test() {
        start();
        Contact contact = mock(Contact.class);
        when(contact.isVerified()).thenReturn(false);

        activityController.get().reportAnalytics(contact);

        verify(activityController.get().analytics).trackEvent(Analytics.EVENT_DROPBIT_SEND_BTN);
    }

    @Test
    public void init_search_is_called_in_onCreate_test() {
        ActivityController<PickUserActivity> activityController = Robolectric.buildActivity(PickUserActivity.class);

        assertNull(activityController.get().searchView);

        activityController.create().start().resume();
        assertNotNull(activityController.get().searchView);
    }

    @Test
    public void init_search_view_search_manager_test() {
        androidx.appcompat.widget.SearchView mockSearchView = mock(androidx.appcompat.widget.SearchView.class);
        SearchManager searchManager = (SearchManager) activityController.get().getSystemService(Context.SEARCH_SERVICE);

        activityController.get().initSearch(mockSearchView);

        verify(mockSearchView).setSearchableInfo(searchManager.getSearchableInfo(activityController.get().getComponentName()));
    }

    @Test
    public void init_search_view_set_view_elements_test() {
        start();
        androidx.appcompat.widget.SearchView mockSearchView = mock(androidx.appcompat.widget.SearchView.class);
        activityController.get().initSearch(mockSearchView);

        verify(mockSearchView).setSubmitButtonEnabled(false);
        verify(mockSearchView).onActionViewExpanded();
        verify(mockSearchView).clearFocus();
    }

    @Test
    public void search_inside_view_model_is_called_when_onQueryTextChange_is_called_test() {
        start();
        androidx.appcompat.widget.SearchView mockSearchView = mock(androidx.appcompat.widget.SearchView.class);
        PickUserViewModel viewModel = mock(PickUserViewModel.class);
        activityController.get().viewModel = viewModel;
        activityController.get().initSearch(mockSearchView);
        activityController.get().onSearchTextChange("Some Change");

        verify(viewModel).search("Some Change");
    }

    @Test
    public void search_filter_inside_adapter_is_not_called_when_null_test() {
        start();
        activityController.get().initSearch(null);

        assert (activityController.get().onSearchTextChange("Some Change"));
    }

    private void start() {
        activityController.start().resume().visible();
    }
}
