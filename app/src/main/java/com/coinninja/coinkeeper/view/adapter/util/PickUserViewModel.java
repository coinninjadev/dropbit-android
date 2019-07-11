package com.coinninja.coinkeeper.view.adapter.util;

import android.view.View;
import android.widget.Filter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserViewModel;
import com.coinninja.coinkeeper.service.tasks.TwitterUserViewModel;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;
import com.coinninja.coinkeeper.view.adapter.PickContactRecycleViewAdapter;
import com.coinninja.coinkeeper.view.adapter.PickTwitterRecycleViewAdapter;
import com.coinninja.coinkeeper.view.adapter.RecycleViewAdapterFilterable;

import javax.inject.Inject;

public class PickUserViewModel extends ViewModel {

    private CoinNinjaUserViewModel coinNinjaUserViewModel;
    private TwitterUserViewModel twitterUserViewModel;
    private PickContactRecycleViewAdapter contactAdapter;
    private PickTwitterRecycleViewAdapter twitterAdapter;

    private UserType primaryType = UserType.PHONE_NUMBER;

    public MutableLiveData<Boolean> dataLoaded;

    @Inject
    PickUserViewModel(CoinNinjaUserViewModel coinNinjaUserViewModel, TwitterUserViewModel twitterUserViewModel) {
        contactAdapter = new PickContactRecycleViewAdapter();
        twitterAdapter = new PickTwitterRecycleViewAdapter();
        dataLoaded = new MutableLiveData<>();
        dataLoaded.setValue(false);
        this.twitterUserViewModel = twitterUserViewModel;
        this.coinNinjaUserViewModel = coinNinjaUserViewModel;
    }

    public void setupOnClickListeners(OnItemClickListener adapterClickListener, View.OnClickListener onWhatIsDropbitClicked) {
        contactAdapter.setOnClickListeners(adapterClickListener, onWhatIsDropbitClicked);
        twitterAdapter.setOnClickListener(adapterClickListener);
    }

    public void start(AppCompatActivity activity) {
        coinNinjaUserViewModel.getContacts().observe(activity, contacts -> {
            contactAdapter.setContacts(contacts);
            if (twitterAdapter.getTwitterUsers() != null) {
                dataLoaded.setValue(true);
            }
        });

        twitterUserViewModel.getFollowingTwitterUsers().observe(activity, twitterUsers -> {
            twitterAdapter.setTwitterUsers(twitterUsers);
            if (contactAdapter.getContacts() != null) {
                dataLoaded.setValue(true);
            }
        });

        twitterUserViewModel.getSearchTwitterUsers().observe(activity, twitterUsers -> {
            twitterAdapter.setSearchTwitterUsers(twitterUsers);
            if (contactAdapter.getContacts() != null) {
                dataLoaded.setValue(true);
            }
        });

        loadData();
    }

    public boolean isDataSetEmpty() {
        switch (primaryType) {
            case TWITTER:
                if (twitterAdapter.getTwitterUsers() == null) {
                    return true;
                }
                return twitterAdapter.getTwitterUsers().isEmpty();
            case PHONE_NUMBER:
                if (contactAdapter.getContacts() == null) {
                    return true;
                }
                return contactAdapter.getContacts().isEmpty();
        }

        return true;
    }

    private void loadData() {
        coinNinjaUserViewModel.load();
        twitterUserViewModel.load();
    }

    @Nullable
    public Object getItem(int position) {
        switch (primaryType) {
            case TWITTER:
                return twitterAdapter.getItemAt(position);
            case PHONE_NUMBER:
                return contactAdapter.getItemAt(position);
        }

        return null;
    }

    public void loadContactsManually() {
        coinNinjaUserViewModel.load();
    }

    public RecyclerView configure(RecyclerView recyclerView, UserType type) {
        primaryType = type;
        recyclerView.setAdapter(getPrimaryAdapter());
        recyclerView.getAdapter().notifyDataSetChanged();
        return recyclerView;
    }

    private RecycleViewAdapterFilterable getPrimaryAdapter() {
        return getAdapter(primaryType);
    }

    private RecycleViewAdapterFilterable getAdapter(UserType type) {
        switch (type) {
            case TWITTER:
                return twitterAdapter;
            case PHONE_NUMBER:
                return contactAdapter;
        }

        return null;
    }

    public boolean search(String search) {
        switch (primaryType) {
            case TWITTER:
                if (search.length() > 2) {
                    twitterUserViewModel.search(search);
                } else if (search.length() == 0) {
                    twitterUserViewModel.setDefaultListToFollowing();
                }
                break;
            case PHONE_NUMBER:
                Filter filter = getPrimaryAdapter().getFilter();
                if (filter == null) {
                    return false;
                }

                filter.filter(search);
                break;
        }

        return true;
    }

    public void manuallyLoadTwitter() {
        twitterUserViewModel.load();
    }

    public enum UserType {
        TWITTER, PHONE_NUMBER
    }
}
