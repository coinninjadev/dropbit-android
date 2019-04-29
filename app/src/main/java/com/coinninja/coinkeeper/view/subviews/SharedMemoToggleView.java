package com.coinninja.coinkeeper.view.subviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.memo.MemoCreator;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;

import static com.coinninja.android.helpers.Views.makeViewGone;
import static com.coinninja.android.helpers.Views.makeViewVisible;
import static com.coinninja.android.helpers.Views.withId;

public class SharedMemoToggleView implements ActivityViewType {

    private ActivityNavigationUtil activityNavigationUtil;
    private MemoCreator memoCreator;
    private AppCompatActivity activity;
    private View view;
    private TextView memoView;
    private ImageView shareToggleButton;
    private ImageView unShareToggleButton;
    private boolean isSharing = true;

    @Inject
    SharedMemoToggleView(ActivityNavigationUtil activityNavigationUtil, MemoCreator memoCreator) {
        this.activityNavigationUtil = activityNavigationUtil;
        this.memoCreator = memoCreator;
    }

    @Override
    public void render(AppCompatActivity activity, View rootView) {
        this.activity = activity;
        view = rootView;
        memoView = view.findViewById(R.id.memo_text_view);
        shareToggleButton = view.findViewById(R.id.shared_memo_toggle_button);
        unShareToggleButton = view.findViewById(R.id.unshare_memo_toggle_button);

        withId(view, R.id.shared_memo_tooltip_button)
                .setOnClickListener(v -> activityNavigationUtil.explainSharedMemos(activity));
        withId(view, R.id.memo_text_view)
                .setOnClickListener(v -> onAddMemoClicked((TextView) v));
        withId(view, R.id.memo_background_view).setOnClickListener(v -> toggleSharingMemo());
        showSharedMemoViews();
    }

    @Override
    public void tearDown() {
        withId(view, R.id.shared_memo_tooltip_button).setOnClickListener(null);
        withId(view, R.id.memo_text_view).setOnClickListener(null);
        withId(view, R.id.memo_background_view).setOnClickListener(null);
        activity = null;
        view = null;
        memoView = null;
        shareToggleButton = null;
        unShareToggleButton = null;
    }

    public void setText(String text) {
        memoView.setText(text);
    }

    public void hideSharedMemoViews() {
        isSharing = false;
        makeViewGone(view, R.id.shared_memo_group);
        shareToggleButton.setVisibility(View.GONE);
        unShareToggleButton.setVisibility(View.GONE);
    }

    public void showSharedMemoViews() {
        isSharing = true;
        makeViewVisible(view, R.id.shared_memo_group);
        updateSharingViews();
    }

    public void onMemoCreated(String memo) {
        if (memo == null || memo.isEmpty()) return;
        memoView.setText(memo);
    }

    public boolean isSharing() {
        return isSharing;
    }

    public String getMemo() {
        return memoView.getText().toString();
    }

    void toggleSharingMemo() {
        isSharing = !isSharing;
        updateSharingViews();
    }

    private void updateSharingViews() {
        shareToggleButton.setVisibility(isSharing ? View.VISIBLE : View.GONE);
        unShareToggleButton.setVisibility(isSharing ? View.GONE : View.VISIBLE);
    }

    private void onAddMemoClicked(TextView view) {
        memoCreator.createMemo(activity, this::onMemoCreated, view.getText().toString());
    }
}
