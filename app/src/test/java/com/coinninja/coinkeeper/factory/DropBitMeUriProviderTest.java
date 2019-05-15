package com.coinninja.coinkeeper.factory;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DropBitMeUriProviderTest {

    @Test
    public void provides_uri() {
        assertThat(new DropBitMeUriProvider(true).provideUri().toString(), equalTo("https://dropbit.me"));
        assertThat(new DropBitMeUriProvider(false).provideUri().toString(), equalTo("https://test.dropbit.me"));
    }

}