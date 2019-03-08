package com.coinninja.coinkeeper.util;

import java.io.ByteArrayOutputStream;

class OutputStreamProvider {
    public ByteArrayOutputStream newStream() {
        return new ByteArrayOutputStream();
    }
}
