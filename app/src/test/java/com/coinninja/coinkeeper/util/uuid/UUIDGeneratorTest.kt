package com.coinninja.coinkeeper.util.uuid

import junit.framework.Assert.assertTrue
import org.junit.Test
import java.util.regex.Matcher
import java.util.regex.Pattern

class UUIDGeneratorTest {
    private val uUIDGenerator = UUIDGenerator()
    @Test
    fun build_UUID() {
        val uuid = uUIDGenerator.generate()
        assertTrue("Does Not Match 4 Spec", uuidMeetsServerSpeci(uuid))
    }

    private fun uuidMeetsServerSpeci(uuid: String): Boolean {
        val pattern: Pattern = Pattern.compile(UUID_V4_PATTERN)
        val matcher: Matcher = pattern.matcher(uuid)
        return matcher.find()
    }

    companion object {
        //this is the regex our server is using to check all incoming UUID's
        //https://git.coinninja.net/backend/btc-api/blob/release/0.1/common/uuid.go
        private const val UUID_V4_PATTERN = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-[8|9|aA|bB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$"
    }
}