package com.coinninja.coinkeeper.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;

import androidx.annotation.Nullable;

public class FingerprintAuthDialog extends BaseDialogFragment {

    public static final int DELAY_MILLIS_ERROR = 1600;
    public static final int DELAY_MILLIS_SUCCESS = 1300;
    private FingerprintAuthUIPresentor fingerprintAuthUIPresentor;
    private View view;
    private Runnable successRunnable = new Runnable() {

        @Override
        public void run() {
            fingerprintAuthUIPresentor.onSuccessfulTransition();
        }
    };
    private int layoutId;

    public static FingerprintAuthDialog newInstance(FingerprintAuthUIPresentor presentor, int layoutId) {
        FingerprintAuthDialog dialog = new FingerprintAuthDialog();
        dialog.setAuthUIPresentor(presentor);
        dialog.setLayoutId(layoutId);
        return dialog;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public void setAuthUIPresentor(FingerprintAuthUIPresentor presentor) {
        fingerprintAuthUIPresentor = presentor;
    }

    Runnable resetViewRunnable = new Runnable() {
        @Override
        public void run() {
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_fingerprint_round);
            ((TextView) view.findViewById(R.id.instructions)).setText(view.getResources().getString(R.string.touch_sensor));
            ((TextView) view.findViewById(R.id.instructions)).setTextColor(view.getResources().getColor(R.color.fingerprint_instruction));
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(layoutId, container, false);
        view.findViewById(R.id.negative_button).setOnClickListener(v -> onAuthCancel());
        setCancelable(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fingerprintAuthUIPresentor.setDialog(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintAuthUIPresentor.startListeningForTouch();
    }

    @Override
    public void onPause() {
        super.onPause();
        fingerprintAuthUIPresentor.stopListeningForTouch();
    }

    public void onFailure() {
        ImageView image = view.findViewById(R.id.icon);
        image.setImageResource(R.mipmap.ic_error_round);
        ((TextView) view.findViewById(R.id.instructions)).setText(view.getResources().getString(R.string.fingerprint_error));
        ((TextView) view.findViewById(R.id.instructions)).setTextColor(view.getResources().getColor(R.color.fingerprint_error));
        image.postDelayed(resetViewRunnable, DELAY_MILLIS_ERROR);
    }

    public void onError(int messageId, CharSequence message) {
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_error_round);
        ((TextView) view.findViewById(R.id.instructions)).setText(message.toString());
        ((TextView) view.findViewById(R.id.instructions)).setTextColor(view.getResources().getColor(R.color.fingerprint_error));
        icon.postDelayed(resetViewRunnable, DELAY_MILLIS_ERROR);
    }

    public void onHelp(int messageId, CharSequence message) {
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_fingerprint_round);
        ((TextView) view.findViewById(R.id.instructions)).setText(message.toString());
        ((TextView) view.findViewById(R.id.instructions)).setTextColor(view.getResources().getColor(R.color.fingerprint_instruction));
    }

    public void onSucces() {
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageResource(R.mipmap.ic_success_round);
        ((TextView) view.findViewById(R.id.instructions)).setText(view.getResources().getString(R.string.fingerprint_success));
        ((TextView) view.findViewById(R.id.instructions)).setTextColor(view.getResources().getColor(R.color.fingerprint_success));
        icon.postDelayed(successRunnable, DELAY_MILLIS_SUCCESS);
    }

    protected void onAuthCancel() {
        fingerprintAuthUIPresentor.onAuthCancel();
    }

    public interface FingerprintAuthUIPresentor {
        void onAuthCancel();

        void onSuccessfulTransition();

        void startListeningForTouch();

        void stopListeningForTouch();

        void setDialog(FingerprintAuthDialog dialog);
    }
}
