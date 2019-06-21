package app.dropbit.commons.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HmacSHA1SignerTest {

    @Test
    fun `signs data`() {
        assertThat(HmacSHA1Signer().signForDigest("data", "key").toHexString(),
                equalTo("104152c5bfdca07bc633eebd46199f0255c9f49d"))
    }

    @Test
    fun `sign twitter example`() {
        val oAuthTokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"
        val secret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"
        val content = "POST&https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521"

        val signingKey = "$secret&$oAuthTokenSecret"
        val expectedSigningKey = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"
        assertThat(signingKey, equalTo(expectedSigningKey))

        assertThat(HmacSHA1Signer().signForDigest(content, signingKey).toHexString().toUpperCase(),
                equalTo("842B5299887E88760212A056AC4EC2EE1626B549"))
        assertThat(HmacSHA1Signer().signAndEncodeDigest(content, signingKey), equalTo("hCtSmYh-iHYCEqBWrE7C7hYmtUk="))
    }
}


