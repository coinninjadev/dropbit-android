package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.adapter.util.PickUserViewModel;
import com.coinninja.coinkeeper.view.widget.ContactsEmptyStateView;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import app.dropbit.twitter.model.TwitterUser;
import app.dropbit.twitter.ui.login.TwitterLoginActivity;

import static com.coinninja.coinkeeper.view.adapter.util.PickUserViewModel.UserType.PHONE_NUMBER;
import static com.coinninja.coinkeeper.view.adapter.util.PickUserViewModel.UserType.TWITTER;

public class PickUserActivity extends SecuredActivity implements OnItemClickListener {

    protected static int CONTACTS_PERMISSION_REQUEST_CODE = 1001;
    protected SearchView searchView;
    @Inject
    PermissionsUtil permissionsUtil;
    @Inject
    PickUserViewModel viewModel;
    @Inject
    DropbitAccountHelper dropbitAccountHelper;
    @Inject
    TwitterVerificationController twitterVerificationController;
    @Inject
    DropbitMeConfiguration dropbitMeConfiguration;
    private RecyclerView recyclerView;

    public void onWhatIsDropBit() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(DropbitIntents.URI_WHAT_IS_DROPBIT);
        startActivity(intent);
        analytics.trackEvent(Analytics.Companion.EVENT_INVITE_WHATIS_DROPBIT);
    }

    public void initSearch(SearchView searchView) {
        if (searchView == null) {
            return;
        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return onSearchTextChange(newText);
            }
        });

        searchView.setSubmitButtonEnabled(false);
        searchView.onActionViewExpanded();
        searchView.clearFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadContactsManually();
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Object object = viewModel.getItem(position);
        Intent intent = new Intent();

        if (object instanceof TwitterUser) {
            intent.putExtra(DropbitIntents.EXTRA_IDENTITY, new Identity((TwitterUser) object));
        } else if (object instanceof Contact) {
            Contact contact = (Contact) object;
            intent.putExtra(DropbitIntents.EXTRA_IDENTITY, new Identity(contact));
            reportAnalytics(contact);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_user);
        setupDataAdapter();
        setupList();
        setupTabListener();
        setupOnClickListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        twitterVerificationController.onStopped();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TwitterVerificationController.Companion.getTWITTER_LOGIN_REQUEST_CODE() && resultCode == Activity.RESULT_OK) {
            twitterVerificationController.onTwitterAuthorized(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUI();
        twitterVerificationController.onStarted(this, () -> {
            dropbitMeConfiguration.setIsNewlyVerified();
            viewModel.manuallyLoadTwitter();
        });
    }


    protected boolean onSearchTextChange(String newText) {
        if (searchView == null) {
            return false;
        }
        return viewModel.search(newText);
    }

    protected void reportAnalytics(Contact contact) {
        if (contact.isVerified()) {
            analytics.trackEvent(Analytics.Companion.EVENT_CONTACT_SEND_BTN);
        } else {
            analytics.trackEvent(Analytics.Companion.EVENT_DROPBIT_SEND_BTN);
        }
    }

    private void setupOnClickListeners() {
        ((ContactsEmptyStateView) findViewById(R.id.contacts_empty_state_view)).setAllowAccessOnClickListener(() -> requestContactsPermission());
    }

    private void setupUI() {
        requestContactAccessIfNecessary();
        showLoading();
        viewModel.setupOnClickListeners(this, v -> onWhatIsDropBit());
        viewModel.dataLoaded.observe(this, success -> setupUIForUpdatedDataSource());
        viewModel.start(this);
    }

    private void requestContactAccessIfNecessary() {
        if (hasContactsPermission()) {
            return;
        }
        requestContactsPermission();
    }

    private void setupTabListener() {
        TabLayout tabLayout = findViewById(R.id.verification_tab_layout);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                startTwitterActivityIfNecessary();
                setupUIForDataSourceSwitch();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            private void startTwitterActivityIfNecessary() {
                switch (getCurrentUserTypeConfiguration()) {
                    case TWITTER:
                        if (!dropbitAccountHelper.isTwitterVerified()) {
                            startTwitterAuthenticationActivity();
                        }
                        break;
                    default:
                        return;
                }
            }
        });
    }

    private void startTwitterAuthenticationActivity() {
        startActivityForResult(new Intent(this, TwitterLoginActivity.class), TwitterVerificationController.Companion.getTWITTER_LOGIN_REQUEST_CODE());
    }

    private void setupDataAdapter() {
        Intent intent = getIntent();
        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case DropbitIntents.ACTION_CONTACTS_SELECTION:
                TabLayout.Tab contactsTab = ((TabLayout) findViewById(R.id.verification_tab_layout)).getTabAt(0);
                if (contactsTab != null) {
                    contactsTab.select();
                }
                break;
            case DropbitIntents.ACTION_TWITTER_SELECTION:
                TabLayout.Tab twitterTab = ((TabLayout) findViewById(R.id.verification_tab_layout)).getTabAt(1);
                if (twitterTab != null) {
                    twitterTab.select();
                }
                break;
        }
    }

    private void setupList() {
        recyclerView = findViewById(R.id.list_contacts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        searchView = findViewById(R.id.search_view);
        initSearch(searchView);
    }

    private void requestContactsPermission() {
        permissionsUtil.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION_REQUEST_CODE);
    }

    private boolean hasContactsPermission() {
        return permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS);
    }

    private void setupUIForDataSourceSwitch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setQuery("", false);
        setupUIForUpdatedDataSource();
    }

    private void setupUIForUpdatedDataSource() {
        removeLoading();
        viewModel.configure(recyclerView, getCurrentUserTypeConfiguration());
        updateEmptyDataSetUIIfNecessary();
    }

    private void updateEmptyDataSetUIIfNecessary() {
        if (!viewModel.isDataSetEmpty()) {
            findViewById(R.id.contacts_empty_state_view).setVisibility(View.GONE);
            findViewById(R.id.twitter_empty_state_view).setVisibility(View.GONE);
            return;
        }

        int visibility = viewModel.isDataSetEmpty() ? View.VISIBLE : View.GONE;

        switch (getCurrentUserTypeConfiguration()) {
            case TWITTER:
                findViewById(R.id.twitter_empty_state_view).setVisibility(visibility);
                findViewById(R.id.contacts_empty_state_view).setVisibility(View.GONE);
                break;
            case PHONE_NUMBER:
                ContactsEmptyStateView contactsEmptyStateView = findViewById(R.id.contacts_empty_state_view);
                contactsEmptyStateView.setVisibility(visibility);
                if (hasContactsPermission()) {
                    contactsEmptyStateView.findViewById(R.id.allow_contact_access_button).setVisibility(View.GONE);
                } else {
                    contactsEmptyStateView.findViewById(R.id.allow_contact_access_button).setVisibility(View.VISIBLE);
                }
                findViewById(R.id.twitter_empty_state_view).setVisibility(View.GONE);
                break;
        }
    }

    private PickUserViewModel.UserType getCurrentUserTypeConfiguration() {
        TabLayout tabLayout = findViewById(R.id.verification_tab_layout);
        return tabLayout.getSelectedTabPosition() == 0 ? PHONE_NUMBER : TWITTER;
    }

}
