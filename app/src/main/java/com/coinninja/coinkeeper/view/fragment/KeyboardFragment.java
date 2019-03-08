package com.coinninja.coinkeeper.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.Keys;

import androidx.annotation.Nullable;

public class KeyboardFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private OnKeyPressListener onKeyListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_keyboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.one).setOnClickListener(this);
        view.findViewById(R.id.two).setOnClickListener(this);
        view.findViewById(R.id.three).setOnClickListener(this);
        view.findViewById(R.id.four).setOnClickListener(this);
        view.findViewById(R.id.five).setOnClickListener(this);
        view.findViewById(R.id.six).setOnClickListener(this);
        view.findViewById(R.id.seven).setOnClickListener(this);
        view.findViewById(R.id.eight).setOnClickListener(this);
        view.findViewById(R.id.nine).setOnClickListener(this);
        view.findViewById(R.id.zero).setOnClickListener(this);
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.back).setOnLongClickListener(this);
        view.findViewById(R.id.dot).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (onKeyListener == null) return;

        switch (view.getId()) {

            case R.id.one:
                onKeyListener.onKeyPress(Keys.ONE);
                break;
            case R.id.two:
                onKeyListener.onKeyPress(Keys.TWO);
                break;
            case R.id.three:
                onKeyListener.onKeyPress(Keys.THREE);
                break;
            case R.id.four:
                onKeyListener.onKeyPress(Keys.FOUR);
                break;
            case R.id.five:
                onKeyListener.onKeyPress(Keys.FIVE);
                break;
            case R.id.six:
                onKeyListener.onKeyPress(Keys.SIX);
                break;
            case R.id.seven:
                onKeyListener.onKeyPress(Keys.SEVEN);
                break;
            case R.id.eight:
                onKeyListener.onKeyPress(Keys.EIGHT);
                break;
            case R.id.nine:
                onKeyListener.onKeyPress(Keys.NINE);
                break;
            case R.id.zero:
                onKeyListener.onKeyPress(Keys.ZERO);
                break;
            case R.id.dot:
                onKeyListener.onKeyPress(Keys.DOT);
                break;
            case R.id.back:
                onKeyListener.onKeyPress(Keys.BACK);
                break;
        }
    }

    public void setOnKeyListener(OnKeyPressListener onKeyListener) {
        this.onKeyListener = onKeyListener;
    }

    public OnKeyPressListener getOnKeyListener() {
        return onKeyListener;
    }

    @Override
    public boolean onLongClick(View view) {
        if (onKeyListener == null) return false;

        if (view.getId() == R.id.back) {
            onKeyListener.onKeyPress(Keys.CLEAR);
            return true;
        }
        return false;
    }


    public interface OnKeyPressListener {
        void onKeyPress(Keys which);
    }
}
