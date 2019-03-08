package com.coinninja.coinkeeper.service.tasks;

import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.Intents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockUserTaskTest {


    public static final long CURRENT_TIME_IN_MILLIS = 1533238943110L;


    @Mock
    private DateUtil dateUtil;

    @Mock
    private UserHelper user;

    private LockUserTask lockUserTask;


    @Before
    public void setUp() {
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_IN_MILLIS);
        lockUserTask = new LockUserTask(user, dateUtil);
    }

    @Test
    public void locks_user_out_of_wallet_for_five_minutes() {
        lockUserTask.doInBackground();

        verify(user).lockOutUntil(CURRENT_TIME_IN_MILLIS + Intents.LOCK_DURRATION);

    }
}