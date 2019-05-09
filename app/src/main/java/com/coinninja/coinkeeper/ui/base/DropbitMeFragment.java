package com.coinninja.coinkeeper.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.dropbit.me.verify.VerifyDropBitMeDialog;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;
import static java.util.Arrays.asList;

public class DropbitMeFragment extends BaseFragment {

    private final String[] visibleWithList = {
            TransactionHistoryActivity.class.getName(),
            TransactionDetailsActivity.class.getName(),
    };

    @Inject
    DropbitMeConfiguration dropbitMeConfiguration;
    @Inject
    DropbitMeDialogFactory dropbitMeDialogFactory;

    private ImageView button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dropbit_me, container, false);
        button = withId(view, R.id.dropbit_me_button);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!asList(visibleWithList).contains(getActivity().getClass().getName())) return;
        setup();
    }

    @Override
    public void onPause() {
        super.onPause();
        button.setOnClickListener(null);
    }

    private void setup() {
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> onDropbitClicked());
        dropbitMeConfiguration.setOnViewDropBitMeViewRequestedObserver(this::showIfRequested);
        showIfRequested();
    }

    private void showIfRequested() {
        if (dropbitMeConfiguration.shouldShowWhenPossible()) {
            showDropbitMeDialog();
        }

        dropbitMeConfiguration.acknowledge();
    }

    private void showDropbitMeDialog() {
        dropbitMeDialogFactory.newInstance().show(getActivity()
                .getSupportFragmentManager(), DropBitMeDialog.TAG);
    }

    private void onDropbitClicked() {
        showDropbitMeDialog();
    }

}
