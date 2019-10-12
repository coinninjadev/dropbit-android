package com.coinninja.coinkeeper.ui.dropbit.me;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.ui.base.DropbitMeDialogFactory;

import javax.inject.Inject;

public abstract class DropBitMeDialog extends BaseDialogFragment {
    public static String TAG = DropBitMeDialog.class.getName();

    @Inject
    DropbitMeDialogFactory dropbitMeDialogFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_DropBitMe);
    }

    @Nullable
    @Override
    final public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        ViewGroup base = (ViewGroup) inflater.inflate(R.layout.dialog_dropbit_me, container, false);
        ViewGroup content = base.findViewById(R.id.dialog_content);
        inflater.inflate(getContentViewLayoutId(), content, true);
        return base;
    }

    @Override
    public void onResume() {
        super.onResume();
        configureDialog();
    }

    abstract protected int getContentViewLayoutId();

    abstract protected void configurePrimaryCallToAction(Button button);

    protected void configureSecondaryButton(Button button) {
        button.setVisibility(View.GONE);
    }

    protected void position(Window window) {
        FrameLayout content = window.findViewById(android.R.id.content);
        Drawable background = window.getDecorView().getBackground();
        if (content.getBackground() == null && background != null) {
            content.setBackground(background);
            window.getDecorView().setBackground(null);
        }
    }

    protected void renderAccount() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        DropBitMeDialog old = (DropBitMeDialog) fragmentManager.findFragmentByTag(TAG);
        if (old != null) {
            old.dismiss();
        }

        dropbitMeDialogFactory.newInstance().show(fragmentManager, TAG);
    }

    protected boolean shouldShowClose() {
        return true;
    }

    protected String getTitleText() {
        return "";
    }

    private void configureDialog() {
        View view = getView();
        if (view == null) return;

        configurePosition();
        setCancelable(true);
        setupCloseButton(view);
        setupTitle(view);
        configurePrimaryCallToAction(findViewById(R.id.dialog_primary_button));
        configureSecondaryButton(findViewById(R.id.dialog_secondary_button));
    }

    private void setupTitle(View view) {
        String titleText = getTitleText();
        TextView title = findViewById(R.id.dialog_title);
        if (titleText == null || "".equals(titleText)) {
            title.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
        }
    }

    private void setupCloseButton(View view) {
        View closeButton = findViewById(R.id.dialog_close);
        if (shouldShowClose()) {
            closeButton.setOnClickListener(v -> dismiss());
        } else {
            closeButton.setVisibility(View.GONE);
        }
    }

    private void configurePosition() {
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        window.setAttributes(params);
        position(window);
    }
}
