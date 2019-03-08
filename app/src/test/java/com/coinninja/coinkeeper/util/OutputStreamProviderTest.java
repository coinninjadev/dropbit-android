package com.coinninja.coinkeeper.util;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class OutputStreamProviderTest {

    @Test
    public void testReturnsByteArrayOutputStream(){
        ByteArrayOutputStream stream = new OutputStreamProvider().newStream();
        assertNotNull(stream);
    }

}