package com.coinninja.coinkeeper.view.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.coinninja.coinkeeper.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class GenericAlertDialog extends DialogFragment {

    private String title;
    private AlertDialog.Builder builder;
    private String message;
    private String positiveLabel;
    private String negativeLabel;
    private DialogInterface.OnClickListener onClickListener;
    private boolean isCancelable = true;
    private boolean isCancelableOnTouchOutside = true;
    private View view;
    private boolean showAsWide = false;
    private AlertDialog alertDialog;

    public static GenericAlertDialog newInstance(String message) {
        return newInstance(null, message, null, null, null, true, true);
    }

    public static GenericAlertDialog newInstance(String title, String message, String positiveLabel, String negativeLabel, DialogInterface.OnClickListener clickListener, boolean isCancelable, boolean isCancelableOnTouchOutside) {

        GenericAlertDialog dialog = new GenericAlertDialog();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveLabel(positiveLabel);
        dialog.setNegativeLabel(negativeLabel);
        dialog.setOnClickListener(clickListener);
        dialog.setIsCancelable(isCancelable);
        dialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside);
        return dialog;
    }

    public static GenericAlertDialog newInstance(View view, boolean isCancelable, boolean isCancelableOnTouchOutside) {

        GenericAlertDialog dialog = new GenericAlertDialog();
        dialog.setView(view);
        dialog.setIsCancelable(isCancelable);
        dialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside);
        return dialog;
    }

    public DialogInterface.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (builder == null) {
            setDialogBuilder(new AlertDialog.Builder(getActivity()));
        }

        if (title != null)
            builder.setTitle(title);

        if (message != null)
            builder.setMessage(message);

        if (positiveLabel != null) {
            builder.setPositiveButton(positiveLabel, onClickListener);
        }

        if (negativeLabel != null) {
            builder.setNegativeButton(negativeLabel, onClickListener);
        }

        if (view != null) {
            builder.setView(view);
        }

        builder.setCancelable(isCancelable);

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside);

        if (showAsWide)
            alertDialog.getContext().setTheme(R.style.WideDialogTheme);

        return alertDialog;
    }

    public void setDialogBuilder(AlertDialog.Builder builder) {
        this.builder = builder;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.positiveLabel = positiveLabel;
    }

    public void setNegativeLabel(String negativeLabel) {
        this.negativeLabel = negativeLabel;
    }

    public void setIsCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    public void setCanceledOnTouchOutside(boolean isCancelableOnTouchOutside) {
        this.isCancelableOnTouchOutside = isCancelableOnTouchOutside;
    }

    public GenericAlertDialog setView(View view) {
        this.view = view;

        return this;
    }

    public void asWide() {
        showAsWide = true;
    }

    public String getMessage() {
        return message;
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    @Nullable
    protected void setTitle(String title) {
        this.title = title;
    }
}
