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

    var pushTokenVerifiedObserver: PushTokenVerifiedObserver? = null

    val token: String get() = preferencesUtil.getString(PUSH_NOTIFICATION_DEVICE_TOKEN, "")

    val onCompleteListener: (Task<InstanceIdResult>) -> Unit = { task ->
        if (task.isSuccessful) {
            task.result?.let {
                saveToken(it.token)
                pushTokenVerifiedObserver?.onTokenAcquired(it.token)
            }
        }
    }

    fun saveToken(token: String) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_DEVICE_TOKEN, token)
    }

    fun retrieveTokenIfNecessary(observer: PushTokenVerifiedObserver? = null) {
        pushTokenVerifiedObserver = observer
        val token = this.token
        if (token.isEmpty()) {
            fireBaseInstanceId.instanceId.addOnCompleteListener(onCompleteListener)
        } else {
            pushTokenVerifiedObserver?.onTokenAcquired(token)
        }
    }


    companion object {
        internal val PUSH_NOTIFICATION_DEVICE_TOKEN = "push_notification_device_token"
    }
}
