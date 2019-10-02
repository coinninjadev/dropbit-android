package com.coinninja.coinkeeper.view.subviews

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedMemoViewTest {

    private fun createView(): View = LayoutInflater.from(ApplicationProvider.getApplicationContext())
            .inflate(R.layout.shared_memo_container, null)

    @Test
    fun test_configure_for_sharing__renders_with_shared_icon() {
        val view = createView()
        SharedMemoView().render(view, true, "some memo", "Jon Doe")
        assertThat(view.findViewById<TextView>(R.id.shared_memo_status_text_view).text.toString()).isEqualTo("Will be seen by Jon Doe")
        assertThat(view.findViewById<TextView>(R.id.shared_memo_text_view).text.toString()).isEqualTo("some memo")
        assertThat(view.findViewById<ImageView>(R.id.shared_status_image_view).tag).isEqualTo(R.drawable.ic_shared_user)
        assertThat(view.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun test_configure_for_sharing__renders_with_out_shared_icon() {
        val view = createView()
        SharedMemoView().render(view, false, "some memo", "Jon Doe")
        assertThat(view.findViewById<TextView>(R.id.shared_memo_status_text_view).text.toString()).isEqualTo("Will be seen by only you.")
        assertThat(view.findViewById<TextView>(R.id.shared_memo_text_view).text.toString()).isEqualTo("some memo")
        assertThat(view.findViewById<ImageView>(R.id.shared_status_image_view).tag).isEqualTo(R.drawable.ic_single_user)
        assertThat(view.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun hidden_when_empty_memo() {
        val view = createView()
        SharedMemoView().render(view, false, "", "Jon Doe")
        assertThat(view.visibility).isEqualTo(View.GONE)
    }
}