package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Filter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserQueryTask;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PickContactActivityTest {

    public static final String CONTACT_PHONE_STRING = "+13305551111";
    PhoneNumber contactPhoneNumber = new PhoneNumber(CONTACT_PHONE_STRING);
    private PickContactActivity activity;
    private ShadowActivity shadowActivity;
    private TestCoinKeeperApplication application;

    @Mock
    private View.OnClickListener whatIsDB;
    private ShadowApplication shadowApplication;
    private ActivityController<PickContactActivity> activityController;

    @After
    public void tearDown() {
        activity = null;
        shadowActivity = null;
        application = null;
        whatIsDB = null;
        shadowApplication = null;
    }

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        activityController = Robolectric.buildActivity(PickContactActivity.class);
        activity = activityController.get();
        activityController.create();
        shadowActivity = shadowOf(activity);
    }


    private void start() {
        activityController.start().resume().visible();
    }

    @Test
    public void ask_for_permission_if_not_already_granted_test() {
        start();
        verify(application.coinNinjaUserQueryTask, times(0)).execute();
    }

    @Ignore
    @Test
    public void if_already_have_permission_start_loading_contacts_test() {
        when(application.permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)).thenReturn(true);

        start();

        verify(application.coinNinjaUserQueryTask).execute();
    }

    @Test
    public void renders_recycle_view() {
        start();
        assertNotNull(activity.findViewById(R.id.list_contacts));
    }

    @Test
    public void results_with_contact_on_selection() {
        start();
        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact(contactPhoneNumber, "John Doe", true);
        contactList.add(contact);
        RecyclerView recyclerView = activity.findViewById(R.id.list_contacts);
        PickContactRecycleViewAdapter adapter = new PickContactRecycleViewAdapter(activity, whatIsDB);
        recyclerView.setAdapter(adapter);
        adapter.setContacts(contactList, null);

        PickContactRecycleViewAdapter.ViewHolder holder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(holder, 0);
        holder.view.performClick();

        Intent resultIntent = shadowActivity.getResultIntent();
        assertNotNull(resultIntent);
        assertThat(shadowActivity.getResultCode(), equalTo(AppCompatActivity.RESULT_OK));
        assertThat(((Contact) resultIntent.getExtras().getParcelable(DropbitIntents.EXTRA_CONTACT)).getPhoneNumber(), equalTo(contact.getPhoneNumber()));
        assertThat(((Contact) resultIntent.getExtras().getParcelable(DropbitIntents.EXTRA_CONTACT)).getDisplayName(), equalTo(contact.getDisplayName()));
        verify(activity.analytics).trackEvent(Analytics.EVENT_CONTACT_SEND_BTN);
    }

    @Test
    public void send_analytics_when_clicking_CONTACT_SEND_BTN_test() {
        start();
        Contact contact = mock(Contact.class);
        when(contact.isVerified()).thenReturn(true);

        activity.reportAnalytics(contact);

        verify(activity.analytics).trackEvent(Analytics.EVENT_CONTACT_SEND_BTN);
    }

    @Test
    public void send_analytics_when_clicking_DROPBIT_SEND_BTN_test() {
        start();
        Contact contact = mock(Contact.class);
        when(contact.isVerified()).thenReturn(false);

        activity.reportAnalytics(contact);

        verify(activity.analytics).trackEvent(Analytics.EVENT_DROPBIT_SEND_BTN);
    }

    @Test
    public void init_search_is_called_in_onCreate_test() {
        ActivityController<PickContactActivity> activityController = Robolectric.buildActivity(PickContactActivity.class);
        activity = activityController.get();

        assertNull(activity.searchView);

        activityController.create().start().resume();
        assertNotNull(activity.searchView);
    }

    @Test
    public void init_search_view_search_manager_test() {
        SearchView mockSearchView = mock(SearchView.class);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        activity.initSearch(mockSearchView);

        verify(mockSearchView).setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
    }

    @Test
    public void init_search_view_set_on_query_text_listener_test() {
        SearchView mockSearchView = mock(SearchView.class);
        ArgumentCaptor<SearchView.OnQueryTextListener> argumentCaptor = ArgumentCaptor.forClass(SearchView.OnQueryTextListener.class);
        PickContactRecycleViewAdapter mockAdapter = mock(PickContactRecycleViewAdapter.class);
        activity.adapter = mockAdapter;
        Filter mockFilter = mock(Filter.class);
        when(mockAdapter.getFilter()).thenReturn(mockFilter);

        activity.initSearch(mockSearchView);

        verify(mockSearchView).setOnQueryTextListener(argumentCaptor.capture());
    }

    @Test
    public void init_search_view_set_view_elements_test() {
        SearchView mockSearchView = mock(SearchView.class);

        activity.initSearch(mockSearchView);

        verify(mockSearchView).setSubmitButtonEnabled(false);
        verify(mockSearchView).onActionViewExpanded();
        verify(mockSearchView).clearFocus();
    }

    @Test
    public void search_filter_inside_adapter_is_called_when_onQueryTextChange_is_called_test() {
        start();
        SearchView mockSearchView = mock(SearchView.class);
        ArgumentCaptor<SearchView.OnQueryTextListener> argumentCaptor = ArgumentCaptor.forClass(SearchView.OnQueryTextListener.class);
        PickContactRecycleViewAdapter mockAdapter = mock(PickContactRecycleViewAdapter.class);
        activity.adapter = mockAdapter;
        Filter mockFilter = mock(Filter.class);
        when(mockAdapter.getFilter()).thenReturn(mockFilter);


        activity.initSearch(mockSearchView);
        verify(mockSearchView).setOnQueryTextListener(argumentCaptor.capture());
        SearchView.OnQueryTextListener listener = argumentCaptor.getValue();
        listener.onQueryTextChange("Some Change");

        verify(mockFilter).filter("Some Change");
    }

    @Test
    public void search_filter_inside_adapter_is_not_called_when_null_test() {
        SearchView mockSearchView = mock(SearchView.class);
        ArgumentCaptor<SearchView.OnQueryTextListener> argumentCaptor = ArgumentCaptor.forClass(SearchView.OnQueryTextListener.class);
        PickContactRecycleViewAdapter mockAdapter = mock(PickContactRecycleViewAdapter.class);
        activity.adapter = mockAdapter;
        when(mockAdapter.getFilter()).thenReturn(null);


        activity.initSearch(mockSearchView);
        verify(mockSearchView).setOnQueryTextListener(argumentCaptor.capture());
        SearchView.OnQueryTextListener listener = argumentCaptor.getValue();
        boolean consumeChange = listener.onQueryTextChange("Some Change");

        assertThat(consumeChange, equalTo(false));
    }

    @Ignore
    @Test
    public void while_loading_contacts_show_spinner_test() {
        when(application.permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)).thenReturn(true);
        start();
        String sampleLoadingMessage = activity.getResources().getString(R.string.loading_dialog);

        activity.loadContacts(activity);

        AlertDialog alert = (AlertDialog) ShadowDialog.getLatestDialog();
        ShadowAlertDialog shadowAlertDialog = (ShadowAlertDialog) shadowOf(alert);

        assertThat(alert.isShowing(), equalTo(true));
        assertThat(shadowAlertDialog.getMessage(), equalTo(sampleLoadingMessage));
    }

    @Ignore
    @Test
    public void when_done_loading_contacts_remove_spinner_test() {
        start();
        CoinNinjaUserQueryTask.OnCompleteListener onCompleteListener = activity;
        AppCompatActivity context = mock(AppCompatActivity.class);
        when(context.checkPermission(eq(Manifest.permission.READ_CONTACTS), anyInt(), anyInt())).thenReturn(
                PackageManager.PERMISSION_GRANTED);


        activity.loadContacts(context);
        AlertDialog alert = (AlertDialog) ShadowDialog.getLatestDialog();
        assertThat(alert.isShowing(), equalTo(true));
        onCompleteListener.onComplete(null, null);


        assertThat(alert.isShowing(), equalTo(false));
    }

    @Test
    public void make_sure_to_null_the_alert_progress_dialog_when_removing_test() {
        start();
        AlertDialog mockLoadingDialog = mock(AlertDialog.class);
        activity.loadingDialog = mockLoadingDialog;

        activity.removeLoading();

        assertThat(activity.loadingDialog, nullValue());
    }
}
