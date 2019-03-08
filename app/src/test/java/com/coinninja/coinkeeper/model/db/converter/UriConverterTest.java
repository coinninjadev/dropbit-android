package com.coinninja.coinkeeper.model.db.converter;

import android.net.Uri;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class UriConverterTest {

    private UriConverter uriConverter;

    @Test
    public void convert_a_uri_to_string_test() {
        uriConverter = new UriConverter();
        Uri sampleUri = Uri.parse("https://coinninja.com");

        String uriString = uriConverter.convertToDatabaseValue(sampleUri);

        assertThat(uriString, equalTo("https://coinninja.com"));
    }

    @Test
    public void convert_a_string_to_uri_test() {
        uriConverter = new UriConverter();
        String sampleString = "https://coinninja.com";

        Uri uri = uriConverter.convertToEntityProperty(sampleString);

        assertThat(uri, equalTo(Uri.parse("https://coinninja.com")));
    }

    @Test
    public void convert_a_empty_uri_to_null_string_test() {
        uriConverter = new UriConverter();
        Uri sampleUri = Uri.parse("");

        String uriString = uriConverter.convertToDatabaseValue(sampleUri);

        assertNull(uriString);
    }

    @Test
    public void convert_a_empty_string_to_null_uri_test() {
        uriConverter = new UriConverter();
        String sampleString = "";

        Uri uri = uriConverter.convertToEntityProperty(sampleString);

        assertNull(uri);
    }
}