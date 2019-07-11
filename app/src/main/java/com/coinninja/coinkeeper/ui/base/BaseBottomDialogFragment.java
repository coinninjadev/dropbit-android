package com.coinninja.coinkeeper.ui.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coinninja.coinkeeper.R;

public abstract class BaseBottomDialogFragment extends BaseDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().
                inflate(getContentViewLayoutId(), container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        configureDialog();
    }

    abstract protected int getContentViewLayoutId();

    protected void configureDialog() {
        Window window = getDialog().getWindow();
        LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.softInputMode = LayoutParams.SOFT_INPUT_ADJUST_PAN | LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        window.setAttributes(params);
        updateBackgroundIfNecessary(window);

    }

    private void updateBackgroundIfNecessary(Window window) {
        window.getDecorView().setPadding(0, Math.round(getResources().getDimension(R.dimen.vertical_margin_xxlarge)), 0, 0);
        FrameLayout content = window.findViewById(android.R.id.content);
        Drawable background = window.getDecorView().getBackground();
        if (content.getBackground() == null && background != null) {
            content.setBackground(background);
            window.getDecorView().setBackground(null);
        }
    }
}
