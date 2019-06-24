package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import javax.inject.Inject


@Mockable
class PushNotificationTokenManager @Inject internal constructor(
        internal val fireBaseInstanceId: FirebaseInstanceId,
        internal val preferencesUtil: PreferencesUtil) {

    val token: String?
        get() = preferencesUtil.getString(PUSH_NOTIFICATION_DEVICE_TOKEN, "")

    val onCompleteListener: (Task<InstanceIdResult>) -> Unit = { task ->
        if (task.isSuccessful) {
            task.result?.let {
                saveToken(it.token)
            }
        }
    }

    fun saveToken(token: String) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_DEVICE_TOKEN, token)
    }

    fun retrieveTokenIfNecessary() {
        if (token.isNullOrEmpty()) {
            fireBaseInstanceId.instanceId.addOnCompleteListener(onCompleteListener)
        }
    }

    companion object {
        internal val PUSH_NOTIFICATION_DEVICE_TOKEN = "push_notification_device_token"
    }
}
