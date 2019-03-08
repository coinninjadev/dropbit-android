package com.coinninja.coinkeeper.view.subviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import static com.coinninja.android.helpers.Views.withId;

public class SharedMemoView extends AbstractBasicViewType {

    private TextView memoView;
    private TextView sharedMemoStatusTextView;
    private ImageView sharedStatusImageView;

    private boolean isSharing;
    private String memoText;
    private String displayText;

    public SharedMemoView(View view, boolean isSharing, String memoText, String displayText) {
        super(view);
        this.isSharing = isSharing;
        this.memoText = memoText;
        this.displayText = displayText;
        setupSharingUI();
    }

    private void setupSharingUI() {
        if (isSharing) {
            sharedStatusImageView.setImageResource(R.drawable.ic_shared_user);
            sharedStatusImageView.setTag(R.drawable.ic_shared_user);
            sharedMemoStatusTextView.setText(view.getResources().getString(R.string.shared_memo, displayText));
        } else {
            sharedStatusImageView.setImageResource(R.drawable.ic_single_user);
            sharedMemoStatusTextView.setText(R.string.only_me_memo);
        }

        memoView.setText(memoText);
    }

    @Override
    public void render() {
        memoView = withId(view, R.id.shared_memo_text_view);
        sharedMemoStatusTextView = withId(view, R.id.shared_memo_status_text_view);
        sharedStatusImageView = withId(view, R.id.shared_status_image_view);
    }

    public String getMemoText() {
        return memoView.getText().toString();
    }

    public void hide() {
        view.setVisibility(View.GONE);
    }
}
