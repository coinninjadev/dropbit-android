package com.coinninja.coinkeeper.view.button

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class HelpLinkButtonTest {

    @Test
    fun inits_with_string_attr() {
        val builder = Robolectric.buildAttributeSet()
        val url = "http://example.com"
        builder.addAttribute(R.attr.url, url)

        val button = HelpLinkButton(
                context = ApplicationProvider.getApplicationContext(),
                attrs = builder.build(),
                defStyleAttr = 0
        )

        assertThat(button.uri.toString()).isEqualTo("http://example.com")
    }

    @Test
    fun inits_with_resource_attr() {
        val builder = Robolectric.buildAttributeSet()
        builder.addAttribute(R.attr.url, "@string/upgrade_now_help_url")

        val button = HelpLinkButton(
                context = ApplicationProvider.getApplicationContext(),
                attrs = builder.build(),
                defStyleAttr = 0
        )

        assertThat(button.uri.toString()).isEqualTo("https://dropbit.app/upgrade")
    }

    @Test
    fun clicking_link_navigates_to_uri() {
        val scenario = ActivityScenario.launch(TestableActivity::class.java)
        scenario.onActivity { activity ->
            val builder = Robolectric.buildAttributeSet()
            builder.addAttribute(R.attr.url, "@string/upgrade_now_help_url")
            val button = HelpLinkButton(
                    context = activity,
                    attrs = builder.build(),
                    defStyleAttr = 0
            )

            button.performClick()

            shadowOf(activity).let { shadow ->
                shadow.peekNextStartedActivity()!!.also {
                    assertThat(it).isNotNull()
                    assertThat(it.data).isNotNull()
                    assertThat(it.data!!.toString()).isEqualTo("https://dropbit.app/upgrade")
                }
            }
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_link_does_not_navigate_to_no_where() {
        val scenario = ActivityScenario.launch(TestableActivity::class.java)
        scenario.onActivity { activity ->
            val button = HelpLinkButton(
                    context = activity,
                    defStyleAttr = 0
            )

            button.performClick()

            shadowOf(activity).let { shadow ->
                assertThat(shadow.peekNextStartedActivity()).isNull()
            }
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}