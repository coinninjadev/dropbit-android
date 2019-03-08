package com.coinninja.coinkeeper.view.edittext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Vibrator;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class PinEditText extends AppCompatEditText implements View.OnClickListener {
    private DrawPinEditText drawPinEditText;
    private DigitHandlerEditText digitHandler;

    private OnClickListener externalOnClickListener;
    private OnSixDigitsEnteredListener onSixDigitsEntered;

    int[][] states = new int[][]{
            new int[]{android.R.attr.state_selected}, // selected
            new int[]{android.R.attr.state_focused}, // focused
            new int[]{-android.R.attr.state_focused}, // unfocused
    };

    int[] colors = new int[]{
            Color.GREEN,
            Color.BLACK,
            Color.GRAY
    };
    private OnDismissRequestListener onDismissRequestListener;


    public PinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPinEditText = new DrawPinEditText(states, colors);
        digitHandler = new DigitHandlerEditText(this);

        initView(context, attrs);
    }

    public PinEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        drawPinEditText = new DrawPinEditText(states, colors);
        digitHandler = new DigitHandlerEditText(this);

        initView(context, attrs);
    }

    protected void initView(Context context, AttributeSet attrs) {
        float currentDensity = context.getResources().getDisplayMetrics().density;
        drawPinEditText.initLines(currentDensity, getPaint());

        if (!isInEditMode()) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorControlActivated,
                    outValue, true);
            final int colorActivated = outValue.data;
            colors[0] = colorActivated;

            context.getTheme().resolveAttribute(R.attr.colorPrimaryDark,
                    outValue, true);
            final int colorDark = outValue.data;
            colors[1] = colorDark;

            context.getTheme().resolveAttribute(R.attr.colorControlHighlight,
                    outValue, true);
            final int colorHighlight = outValue.data;
            colors[2] = colorHighlight;
        }

        setBackgroundResource(0);
        drawPinEditText.initSpacing(currentDensity);


        //Disable copy paste
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        // When tapped, move cursor to end of text.
        super.setOnClickListener(this);
        super.addTextChangedListener(digitHandler);
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawPinEditText.draw(canvas, this, getText());
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        externalOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        setSelection(getText().length());
        if (externalOnClickListener != null) {
            externalOnClickListener.onClick(v);
        }
    }

    protected void onSixDigits() {
        if (onSixDigitsEntered != null) {
            onSixDigitsEntered.onSixDigits();
        }
    }

    public void setOnSixDigitsEnteredListener(PinEditText.OnSixDigitsEnteredListener onSixDigitsEntered) {
        this.onSixDigitsEntered = onSixDigitsEntered;
    }

    public int[] getPin() {
        Editable input = getText();
        String inputStr = input.toString();
        return digitHandler.getPinWithIntegrityCheck(inputStr);
    }

    public void clearPin() {
        setText("");
        invalidate();
    }

    public void onError() {
        long[] pattern = {0, 100, 0, 100, 0, 100, 0, 100};
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);

        TranslateAnimation shake = new TranslateAnimation(0, 12, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(5));
        clearPin();
        startAnimation(shake);
    }

    public interface OnSixDigitsEnteredListener {
        void onSixDigits();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (onDismissRequestListener != null) {
                onDismissRequestListener.onDismissRequest();
                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    public void setOnDismissRequestListener(OnDismissRequestListener onDismissRequestListener) {
        this.onDismissRequestListener = onDismissRequestListener;
    }

    public interface OnDismissRequestListener {
        void onDismissRequest();
    }
}
