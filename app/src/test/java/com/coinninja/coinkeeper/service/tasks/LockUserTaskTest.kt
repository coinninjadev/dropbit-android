package com.coinninja.coinkeeper.service.tasks

import com.coinninja.coinkeeper.model.helpers.UserHelper
import com.coinninja.coinkeeper.util.DateUtil
import com.coinninja.coinkeeper.util.DropbitIntents

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(MockitoJUnitRunner::class)
class LockUserTaskTest {

    @Mock
    private val dateUtil: DateUtil? = null

    @Mock
    private val user: UserHelper? = null

    private var lockUserTask: LockUserTask? = null


    @Before
    fun setUp() {
        `when`(dateUtil!!.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_IN_MILLIS)
        lockUserTask = LockUserTask(user, dateUtil)
    }

    @Test
    fun locks_user_out_of_wallet_for_five_minutes() {
        lockUserTask!!.doInBackground()

        verify<UserHelper>(user).lockOutUntil(CURRENT_TIME_IN_MILLIS + DropbitIntents.LOCK_DURRATION)

    }

    companion object {

        private val CURRENT_TIME_IN_MILLIS = 1533238943110L
    }
}