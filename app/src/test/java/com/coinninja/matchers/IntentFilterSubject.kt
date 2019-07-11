package com.coinninja.matchers

import android.content.IntentFilter
import androidx.core.util.Preconditions.checkNotNull
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

internal class IntentFilterSubject private constructor(metadata: FailureMetadata, private val actual: IntentFilter?) : Subject<IntentFilterSubject, IntentFilter>(metadata, actual) {

    fun containsAction(action: String) {
        checkNotNull(action)
        if (actual == null) {
            failWithActual("expected a intent filter that contains", action)
        } else if (!actual.hasAction(action)) {
            failWithActual("expected to contain", action)
        }
    }

    companion object {

        fun intentFilters(): Subject.Factory<IntentFilterSubject, IntentFilter> {
            return Subject.Factory { metadata, intentFilter -> IntentFilterSubject(metadata, intentFilter) }
        }

        fun assertThatIntentFilter(actual: IntentFilter?): IntentFilterSubject {
            return assertAbout<IntentFilterSubject, IntentFilter>(intentFilters()).that(actual)
        }
    }
}
