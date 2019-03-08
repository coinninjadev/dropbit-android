package com.coinninja.coinkeeper.dialog;


import android.app.AlertDialog;
import android.content.DialogInterface;

import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericAlertDialogTest {

    private String title = "Test Title";
    private String message = "test message";
    private GenericAlertDialog dialog;
    private String negativeActionLabel = "negative";
    private String positiveActionLabel = "positive";

    private DialogInterface.OnClickListener clickListener;

    @Mock
    AlertDialog.Builder mockBuilder;

    @Mock
    AlertDialog onCreateDialog;


    @Before
    public void setUp() {
        dialog = GenericAlertDialog.newInstance(title, message, positiveActionLabel,
                negativeActionLabel, clickListener, false, false);
        dialog.setDialogBuilder(mockBuilder);
        when(mockBuilder.create()).thenReturn(onCreateDialog);
    }

    @Test
    public void intitsWithTitle() {
        dialog.onCreateDialog(null);

        verify(mockBuilder, times(1)).setTitle(title);
    }

    @Test
    public void initsWithMessage() {
        dialog.onCreateDialog(null);
        verify(mockBuilder, times(1)).setMessage(message);
    }

    @Test
    public void initsWithPositiveAction() {
        dialog.onCreateDialog(null);
        verify(mockBuilder, times(1)).setPositiveButton(positiveActionLabel, clickListener);
    }

    @Test
    public void initsWithNegativeAction() {
        dialog.onCreateDialog(null);
        verify(mockBuilder, times(1)).setNegativeButton(negativeActionLabel, clickListener);
    }

    @Test
    public void initsWithCancelable() {
        dialog.onCreateDialog(null);
        verify(mockBuilder, times(1)).setCancelable(false);
    }

    @Test
    public void initsWithCancelableOnTouchOutside() {
        dialog.onCreateDialog(null);
        verify(onCreateDialog, times(1)).setCanceledOnTouchOutside(false);
    }
}