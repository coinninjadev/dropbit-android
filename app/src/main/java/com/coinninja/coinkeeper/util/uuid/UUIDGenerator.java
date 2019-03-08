package com.coinninja.coinkeeper.util.uuid;

import java.util.UUID;

import javax.inject.Inject;

public class UUIDGenerator {

    @Inject
    UUIDGenerator() {
    }

    public String generate() {
        return UUID.randomUUID().toString();
    }

}
