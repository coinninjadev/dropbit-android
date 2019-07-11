package com.coinninja.coinkeeper.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import androidx.core.view.GravityCompat

@SuppressLint("ViewConstructor")
open class DrawerLayout(context: Context, var isSwipeOpenEnabled: Boolean = true) : androidx.drawerlayout.widget.DrawerLayout(context) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isSwipeOpenEnabled && !isDrawerVisible(GravityCompat.START)) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isSwipeOpenEnabled && !isDrawerVisible(GravityCompat.START)) {
            return false
        }
        return super.onTouchEvent(ev)
    }
}