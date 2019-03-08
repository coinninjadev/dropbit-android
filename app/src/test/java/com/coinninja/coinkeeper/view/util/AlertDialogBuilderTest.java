package com.coinninja.coinkeeper.view.util;

import android.app.AlertDialog;
import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class AlertDialogBuilderTest {

    @Test
    public void build() {
        Context context = RuntimeEnvironment.application.getApplicationContext();

        String message = "hello";
        AlertDialog.Builder build = AlertDialogBuilder.build(context, message);

        assertNotNull(build);
    }

}