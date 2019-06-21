package com.coinninja.android.helpers;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ResourcesTest {
    @Test
    public void gets_string_for_given_resource() {
        assertThat(Resources.INSTANCE.getString(ApplicationProvider.getApplicationContext(),
                R.string.send_request), equalTo("Send Request"));
    }

    @Test
    public void gets_string_for_given_resource__with_formatting() {
        assertThat(Resources.INSTANCE.getString(ApplicationProvider.getApplicationContext(),
                R.string.hello_world, "World"), equalTo("Hello World"));
    }

}