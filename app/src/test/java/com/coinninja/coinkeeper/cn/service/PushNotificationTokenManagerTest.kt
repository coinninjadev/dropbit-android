package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class PushNotificationTokenManagerTest {

    private fun createPushNotificationTokenManager(): PushNotificationTokenManager {
        val tokenManager = PushNotificationTokenManager(mock(FirebaseInstanceId::class.java),
                mock(PreferencesUtil::class.java))
        val task: Task<InstanceIdResult> = mock(Task::class.java) as Task<InstanceIdResult>
        whenever(tokenManager.fireBaseInstanceId.instanceId).thenReturn(task)
        return tokenManager
    }

    @Test
    fun saves_token_locally() {
        val token = "--token--"
        val tokenManager = createPushNotificationTokenManager()

        tokenManager.saveToken(token)

        verify(tokenManager.preferencesUtil).savePreference(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, token)
    }

    @Test
    fun provides_access_to_push_token() {
        val token = "--token--"
        val tokenManager = createPushNotificationTokenManager()

        whenever(tokenManager.preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, ""))
                .thenReturn(token)

        assertThat(tokenManager.token, equalTo(token))
    }

    @Test
    fun `retrieves current token from FireBase when one is not saved`() {
        val tokenManager = createPushNotificationTokenManager()
        whenever(tokenManager.preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, ""))
                .thenReturn("")

        tokenManager.retrieveTokenIfNecessary()

        verify(tokenManager.fireBaseInstanceId.instanceId).addOnCompleteListener(any())
    }

    @Test
    fun `does not retrieve current token from FireBase when one is saved`() {
        val tokenManager = createPushNotificationTokenManager()
        whenever(tokenManager.preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, ""))
                .thenReturn("--token--")

        tokenManager.retrieveTokenIfNecessary()

        verify(tokenManager.fireBaseInstanceId.instanceId, times(0)).addOnCompleteListener(any())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `on complete listener saves token when task is successful`() {
        val tokenManager = createPushNotificationTokenManager()
        val task: Task<InstanceIdResult> = mock(Task::class.java) as Task<InstanceIdResult>
        val instanceResult = mock(InstanceIdResult::class.java)
        whenever(task.result).thenReturn(instanceResult)
        whenever(task.isSuccessful).thenReturn(true)
        whenever(instanceResult.token).thenReturn("--token--")

        tokenManager.onCompleteListener(task)

        verify(tokenManager.preferencesUtil).savePreference(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, "--token--")
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `notifies observer that token was saved`() {
        val tokenManager = createPushNotificationTokenManager()
        val pushTokenVerifiedObserver = mock<PushTokenVerifiedObserver>()
        whenever(tokenManager.preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, ""))
                .thenReturn("")
        val task: Task<InstanceIdResult> = mock(Task::class.java) as Task<InstanceIdResult>
        val instanceResult = mock(InstanceIdResult::class.java)
        whenever(task.result).thenReturn(instanceResult)
        whenever(task.isSuccessful).thenReturn(true)
        whenever(instanceResult.token).thenReturn("--token--")

        tokenManager.retrieveTokenIfNecessary(observer = pushTokenVerifiedObserver)
        tokenManager.onCompleteListener(task)

        verify(pushTokenVerifiedObserver).onTokenAcquired("--token--")
    }

    @Test
    fun `notifies observer that token was saved when cached in preferences`() {
        val tokenManager = createPushNotificationTokenManager()
        val pushTokenVerifiedObserver = mock<PushTokenVerifiedObserver>()
        whenever(tokenManager.preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, ""))
                .thenReturn("--token--")

        tokenManager.retrieveTokenIfNecessary(observer = pushTokenVerifiedObserver)

        verify(tokenManager.fireBaseInstanceId.instanceId, times(0)).addOnCompleteListener(any())
        verify(pushTokenVerifiedObserver).onTokenAcquired("--token--")
    }
}