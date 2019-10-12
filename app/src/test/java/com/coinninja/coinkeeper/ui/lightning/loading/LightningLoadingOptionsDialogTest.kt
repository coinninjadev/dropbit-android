package com.coinninja.coinkeeper.ui.lightning.loading

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningLoadingOptionsDialogTest {

    private val scenario = ActivityScenario.launch(TestableActivity::class.java)

    @After
    fun teardown() {
        scenario.close()
    }

    private fun showDialog(): LightningLoadingOptionsDialog {
        val dialog = LightningLoadingOptionsDialog()

        scenario.onActivity { activity ->
            dialog.show(activity.supportFragmentManager, LightningLoadingOptionsDialog::class.java.simpleName)
        }

        return dialog
    }

    @Test
    fun cancel_dismisses_dialog() {
        val dialog = showDialog()

        scenario.onActivity { activity ->
            assertThat(activity.supportFragmentManager.findFragmentByTag(LightningLoadingOptionsDialog::class.java.simpleName)).isNotNull()

            dialog.cancelButton!!.performClick()

            assertThat(activity.supportFragmentManager.findFragmentByTag(LightningLoadingOptionsDialog::class.java.simpleName)).isNull()
        }
    }

    @Test
    fun options_rendered() {
        val dialog = showDialog()
        assertThat(dialog.options!!.adapter!!.itemCount).isEqualTo(2)

        val holder = dialog.options!!.adapter!!.onCreateViewHolder(dialog.options!!, 0) as LightningLoadingOptionsDialog.ViewHolder
        dialog.options!!.adapter!!.onBindViewHolder(holder, 0)
        holder.view.let {
            assertThat(it.text).isEqualTo(dialog.getString(R.string.loading_option_load_lightning))
            it.performClick()
            verify(dialog.activityNavigationUtil).showLoadLightningWith(any(), eq(null))
        }

        dialog.options!!.adapter!!.onBindViewHolder(holder, 1)
        holder.view.let {
            assertThat(it.text).isEqualTo(dialog.getString(R.string.loading_option_unload_lightning))
            holder.itemView.performClick()
            verify(dialog.activityNavigationUtil).showWithdrawalLightning(any())
        }
    }
}