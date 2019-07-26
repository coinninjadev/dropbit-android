package com.coinninja.coinkeeper.ui.actionbar.managers

import android.view.View
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class TitleViewManagerTest {


    private fun createManager(): TitleViewManager = TitleViewManager().also {
        it.actionBar = mock()
        it.titleView = mock()
    }


    @Test
    fun set_title_to_app_bar() {
        val manager = createManager()
        val title = " --- TITLE --"
        whenever(manager.actionBar!!.title).thenReturn(title)

        manager.renderTitle()

        verify(manager.titleView)!!.visibility = View.VISIBLE
        verify(manager.titleView)!!.text = title
    }

    @Test
    fun set_title_to_app_bar_directly() {
        val manager = createManager()
        val title = " --- TITLE --"
        val titleWeDoNotWant = " --- TITLE BAD --"
        whenever(manager.actionBar!!.title).thenReturn(titleWeDoNotWant)

        manager.title = title
        manager.renderTitle()

        verify(manager.titleView)!!.visibility = View.VISIBLE
        verify(manager.titleView, never())!!.text = titleWeDoNotWant
        verify(manager.titleView)!!.text = title
    }

    @Test
    fun uppercase_title_text() {
        val manager = createManager()
        val title = " --- the title --"
        whenever(manager.actionBar!!.title).thenReturn(title)

        manager.renderTitle()

        verify(manager.titleView!!).visibility = View.VISIBLE
        verify(manager.titleView)!!.text = " --- THE TITLE --"
    }

    @Test
    fun remove_title_bar_when_text_is_empty() {
        val manager = createManager()
        val title = ""
        whenever(manager.actionBar!!.title).thenReturn(title)

        manager.renderTitle()

        verify(manager.titleView)!!.visibility = View.GONE
    }

    @Test
    fun clear_action_bar_title_when_getting_title() {
        val manager = createManager()
        whenever(manager.actionBar!!.title).thenReturn("")

        manager.title = "foo"
        assertThat(manager.title).isEqualTo("foo")
        verify(manager.actionBar!!).title = ""
    }

}