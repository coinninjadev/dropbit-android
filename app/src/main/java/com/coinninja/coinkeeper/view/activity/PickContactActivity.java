package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Filter;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserQueryTask;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PickContactActivity extends SecuredActivity implements CoinNinjaUserQueryTask.OnCompleteListener, OnItemClickListener {

    protected static int CONTACTS_PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView list;
    protected PickContactRecycleViewAdapter adapter;
    protected SearchView searchView;
    protected AlertDialog loadingDialog;

    @Inject
    PermissionsUtil permissionsUtil;

    @Inject
    LocalContactQueryUtil localContactQueryUtil;

    @Inject
    CoinNinjaUserQueryTask fetchUserTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cn_contacts);
        adapter = new PickContactRecycleViewAdapter(this, this::onWhatIsDropBit);
        list = findViewById(R.id.list_contacts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        list.addItemDecoration(dividerItemDecoration);
        searchView = findViewById(R.id.searchView);
        initSearch(searchView);
    }

    protected void initSearch(SearchView searchView) {
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

    protected void onWhatIsDropBit(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Intents.URI_WHAT_IS_DROPBIT);
        startActivity(intent);
        analytics.trackEvent(Analytics.EVENT_INVITE_WHATIS_DROPBIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fetchUserTask.setOnCompleteListener(null);
    }

    protected void loadContacts(Activity context) {
        if (hasContactsPermission(context)) {
            showLoading();
            fetchContacts();
        } else {
            removeLoading();
            reqeustContactsPermission(context);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchContacts();
        } else {
            setResult(Activity.RESULT_CANCELED, null);
        }
    }

    private void fetchContacts() {
        fetchUserTask.setOnCompleteListener(this);
        fetchUserTask.clone().execute();
    }

    private void reqeustContactsPermission(Activity context) {
        permissionsUtil.requestPermissions(context, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION_REQUEST_CODE);
    }

    private boolean hasContactsPermission(Context context) {
        return permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS);
    }

    private void showContacts() {
        removeLoading();
        list.setAdapter(adapter);
        list.getAdapter().notifyDataSetChanged();
    }

    private void showLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        loadingDialog = AlertDialogBuilder.buildIndefiniteProgress(this);
    }

    protected void removeLoading() {
        if (loadingDialog == null) {
            return;
        }

        loadingDialog.dismiss();
        loadingDialog = null;
    }

    @Override
    public void onComplete(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts) {
        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        showContacts();
    }

    @Override
    public void onItemClick(View view, int position) {
        Contact contact = ((PickContactRecycleViewAdapter) list.getAdapter()).getItemAt(position);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        setResult(RESULT_OK, intent);
        reportAnalytics(contact);
        finish();
    }

    protected boolean onSearchTextChange(String newText) {
        Filter filter = adapter.getFilter();
        if (filter == null) {
            return false;
        }

        filter.filter(newText);
        return true;
    }

    protected void reportAnalytics(Contact contact) {
        if (contact.isVerified()) {
            analytics.trackEvent(Analytics.EVENT_CONTACT_SEND_BTN);
        } else {
            analytics.trackEvent(Analytics.EVENT_DROPBIT_SEND_BTN);
        }
    }
}
