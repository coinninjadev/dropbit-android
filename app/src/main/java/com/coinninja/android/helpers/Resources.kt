package com.coinninja.android.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources

object Resources {
    fun getString(context: Context, resourceId: Int): String {
        return context.resources.getString(resourceId)
    }

    fun getString(context: Context, resourceId: Int, vararg formats: String): String {
        return context.resources.getString(resourceId, *formats as Array<Any>)
    }

    fun getDrawable(context: Context, resourceId: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(context, resourceId)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        return drawable
    }

    fun getColor(context: Context, resourceId: Int): Int {
        return context.resources.getColor(resourceId)
    }

    fun scaleValue(context: Context, unit: Int, value: Float): Float {
        return TypedValue.applyDimension(unit, value, context.resources.displayMetrics)
    }
}
