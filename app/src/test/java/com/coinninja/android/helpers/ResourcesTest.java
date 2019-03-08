package com.coinninja.android.helpers;

import android.content.Context;

import com.coinninja.coinkeeper.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.coinninja.android.helpers.Resources.getString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ResourcesTest {
    @Test
    public void gets_string_for_given_resource() {
        Context context = RuntimeEnvironment.application;
        assertThat(getString(context, R.string.send_request), equalTo("Send Request"));
    }

    @Test
    public void gets_string_for_given_resource__with_formatting() {
        Context context = RuntimeEnvironment.application;
        assertThat(getString(context, R.string.hello_world, "World"), equalTo("Hello World"));
    }

}