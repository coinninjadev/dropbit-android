package com.coinninja.coinkeeper.ui.memo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.android.helpers.Input;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

public class MemoCreator {
    private OnMemoCreatedCallback callback;
    private GenericAlertDialog genericAlertDialog;
    private View view;

    @Inject
    MemoCreator() {
    }

    public void createMemo(@NonNull AppCompatActivity activity, @NonNull OnMemoCreatedCallback callback, String text) {
        this.callback = callback;
        view = LayoutInflater.from(activity).inflate(R.layout.dialog_create_memo, null);
        genericAlertDialog = GenericAlertDialog.newInstance(view, false, false);
        genericAlertDialog.asWide();
        genericAlertDialog.show(activity.getSupportFragmentManager(), MemoCreator.class.getSimpleName());
        EditText memoView = view.findViewById(R.id.memo);
        memoView.setText(text);
        memoView.setSelection(text.length());
        view.findViewById(R.id.done).setOnClickListener(v -> onDonePressed());
        memoView.postDelayed(() -> {
            Input.INSTANCE.showKeyboard(memoView);
        }, 200);

    }

    private void onDonePressed() {
        TextView memo = view.findViewById(R.id.memo);
        String text = memo.getText().toString().trim();

        if (!"".equals(text))
            callback.onMemoCreated(text);

        genericAlertDialog.dismiss();
        genericAlertDialog = null;
        view = null;
        callback = null;
    }


    public interface OnMemoCreatedCallback {
        void onMemoCreated(String memo);
    }
}
