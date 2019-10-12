package com.coinninja.coinkeeper.view.subviews

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedMemoToggleViewTest {
    private fun createScenario(): ActivityScenario<TestableActivity> {
        val scenario = ActivityScenario.launch(TestableActivity::class.java)
        scenario.onActivity { activity ->
            activity.appendLayout(R.layout.memo_container)
        }
        return scenario
    }


    @Test
    fun hides_shared_memo_views() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))

            sharedMemoView.hideSharedMemoViews()

            assertThat(activity.findViewById<View>(R.id.unshare_memo_toggle_button).visibility).isEqualTo(View.GONE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_toggle_button).visibility).isEqualTo(View.GONE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_group).visibility).isEqualTo(View.GONE)
            assertThat(sharedMemoView.isSharing).isFalse()

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)

        scenario.close()
    }

    @Test
    fun inits_with_sharing_hidden() {

        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))


            assertThat(activity.findViewById<View>(R.id.unshare_memo_toggle_button).visibility).isEqualTo(View.GONE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_toggle_button).visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_group).visibility).isEqualTo(View.VISIBLE)
            assertThat(sharedMemoView.isSharing).isTrue()

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)

        scenario.close()
    }

    @Test
    fun shows_shared_memo_views() {

        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))
            sharedMemoView.hideSharedMemoViews()

            sharedMemoView.showSharedMemoViews()

            assertThat(activity.findViewById<View>(R.id.unshare_memo_toggle_button).visibility).isEqualTo(View.GONE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_toggle_button).visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.findViewById<View>(R.id.shared_memo_group).visibility).isEqualTo(View.VISIBLE)
            assertThat(sharedMemoView.isSharing).isTrue()

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun provides_memo_from_view() {

        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))
            sharedMemoView.memoView?.text = "foo"


            assertThat(sharedMemoView.memo).isEqualTo("foo")

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun toggles_sharing_when_background_pressed() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))

            assertThat(sharedMemoView.isSharing).isTrue()

            activity.findViewById<View>(R.id.memo_background_view).performClick()

            assertThat(sharedMemoView.isSharing).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun toggles_shared() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))

            assertThat(sharedMemoView.isSharing).isTrue()

            sharedMemoView.toggleSharingMemo()
            assertThat(sharedMemoView.isSharing).isFalse()

            sharedMemoView.toggleSharingMemo()
            assertThat(sharedMemoView.isSharing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun tooltip_explains_shared_memos() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))

            activity.findViewById<View>(R.id.shared_memo_tooltip_button).performClick()

            verify(sharedMemoView.activityNavigationUtil).explainSharedMemos(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_on_memo_creates_allows_user_to_input() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val sharedMemoView = SharedMemoToggleView(mock(), mock())
            sharedMemoView.render(activity, activity.findViewById(R.id.memo_container))

            sharedMemoView.memoView?.text = "foo"

            activity.findViewById<View>(R.id.memo_text_view).performClick()

            verify(sharedMemoView.memoCreator).createMemo(any(), any(), eq("foo"))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

}