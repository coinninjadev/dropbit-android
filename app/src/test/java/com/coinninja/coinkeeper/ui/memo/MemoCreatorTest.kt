package com.coinninja.coinkeeper.ui.memo

import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R.id
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.matchers.TextViewMatcher.hasText
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class MemoCreatorTest {

    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java)
    private val alertDialog: AlertDialog get() = ShadowDialog.getLatestDialog() as AlertDialog

    @Test
    fun shows_dialog_when_create_called() {
        val memoCreator = MemoCreator()
        val callback: MemoCreator.OnMemoCreatedCallback = mock()
        val scenario = createScenario()

        scenario.onActivity { activity ->
            memoCreator.createMemo(activity, callback)
            val latestAlertDialog = alertDialog
            assertNotNull(latestAlertDialog)
            assertNotNull(latestAlertDialog.findViewById<View?>(id.memo))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun provides_text_to_callback() {
        val memoCreator = MemoCreator()
        val callback: MemoCreator.OnMemoCreatedCallback = mock()
        val scenario = createScenario()

        scenario.onActivity { activity ->
            memoCreator.createMemo(activity, callback)
            val memo: EditText = alertDialog.findViewById(id.memo)!!
            memo.setText("foo my bar")
            alertDialog.findViewById<View>(id.done)!!.performClick()
            verify(callback).onMemoCreated("foo my bar")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun no_callback_for_no_text() {
        val memoCreator = MemoCreator()
        val callback: MemoCreator.OnMemoCreatedCallback = mock()
        val scenario = createScenario()

        scenario.onActivity { activity ->
            memoCreator.createMemo(activity, callback)
            alertDialog.findViewById<EditText?>(id.memo)!!.apply {
                setText("")
            }
            alertDialog.findViewById<View>(id.done)!!.performClick()

            alertDialog.findViewById<EditText?>(id.memo)!!.apply {
                setText("\n")
            }
            alertDialog.findViewById<View>(id.done)!!.performClick()

            alertDialog.findViewById<EditText?>(id.memo)!!.apply {
                setText("       ")
            }
            alertDialog.findViewById<View>(id.done)!!.performClick()

            verify(callback, times(0)).onMemoCreated(ArgumentMatchers.anyString())

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun populates_with_provided_text() {
        val memoCreator = MemoCreator()
        val callback: MemoCreator.OnMemoCreatedCallback = mock()
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val text = "some already entered text"
            memoCreator.createMemo(activity, callback, text)
            alertDialog.findViewById<EditText?>(id.memo)!!.also {
                assertThat(it, hasText(text))
                assertThat(it.selectionEnd, equalTo(text.length))
            }
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

}