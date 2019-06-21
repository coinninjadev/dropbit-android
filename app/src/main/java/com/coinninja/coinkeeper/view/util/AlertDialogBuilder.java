package com.coinninja.coinkeeper.view.util;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AlertDialogBuilder {
    public static AlertDialog.Builder build(Context context, String msg) {
        return build(context, null, msg);
    }

    public static AlertDialog.Builder build(Context context, String title, String msg) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        if (title != null && !title.isEmpty()) {
            builder.setTitle(title);
        }
        builder.setPositiveButton(android.R.string.ok, null);
        return builder;
    }

    public static AlertDialog buildIndefiniteProgress(AppCompatActivity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage(activity.getResources().getString(R.string.loading_dialog));

        AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        alertDialog.setOnDismissListener(new LoadingAnimation(alertDialog));
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private static class LoadingAnimation implements DialogInterface.OnDismissListener {
        private static final int MAX_DOT_COUNT = 6;
        private static final long ANIMATION_DELAY_MS = 100;
        private AlertDialog alertDialog;

        private TextView messageView;
        private int loopCount = 0;
        private final Runnable animationRunner = () -> {
            if (loopCount > MAX_DOT_COUNT) {
                initDots();
            } else {
                increaseDots();
            }

            loopAnimation();
        };

        public LoadingAnimation(AlertDialog alertDialog) {
            this.alertDialog = alertDialog;
        }

        private void initDots() {
            messageView = alertDialog.findViewById(android.R.id.message);
            loopCount = 0;
            String currentMessage = messageView.getText().toString();

            while (currentMessage.contains(".")) {
                currentMessage = currentMessage.replace(".", "");
            }

            messageView.setText(currentMessage);
        }

        private void increaseDots() {
            loopCount++;
            String currentMessage = messageView.getText().toString();

            currentMessage = currentMessage + ".";
            messageView.setText(currentMessage);
        }

        private void loopAnimation() {
            messageView.removeCallbacks(animationRunner);
            messageView.postDelayed(animationRunner, ANIMATION_DELAY_MS);
        }

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            if (messageView == null) { return; }
            messageView.removeCallbacks(animationRunner);
        }
    }
}
