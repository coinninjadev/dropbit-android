package com.coinninja.coinkeeper.util.uuid;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;

public class UUIDGeneratorTest {

    //this is the regex our server is using to check all incoming UUID's
    //https://git.coinninja.net/backend/btc-api/blob/release/0.1/common/uuid.go
    private static final String UUID_V4_PATTERN = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-[8|9|aA|bB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";

    private UUIDGenerator uUIDGenerator = new UUIDGenerator();

    @Test
    public void build_UUID() {
        String uuid = uUIDGenerator.generate();

        assertTrue("Does Not Match 4 Spec", uuidMeetsServerSpeci(uuid));
    }

    private boolean uuidMeetsServerSpeci(String uuid) {
        Pattern pattern = Pattern.compile(UUID_V4_PATTERN);
        Matcher matcher = pattern.matcher(uuid);
        return matcher.find();
    }

}