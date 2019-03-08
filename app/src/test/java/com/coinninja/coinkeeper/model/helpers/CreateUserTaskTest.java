package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserTaskTest {

    @Mock
    UserHelper userHelper;

    String uuid = "-- uuid--";

    private CreateUserTask task;

    @Before
    public void setUp() {
        task = new CreateUserTask(uuid, userHelper);
    }

    @Test
    public void only_creates_user_when_user_does_not_exist() {
        when(userHelper.getUser()).thenReturn(null);

        task.doInBackground();

        verify(userHelper).createFirstUser(uuid);
    }

    @Test
    public void does_not_create_user_when_user_is_present() {
        when(userHelper.getUser()).thenReturn(new User());

        task.doInBackground();

        verify(userHelper, times(0)).createFirstUser(anyString());
    }

    @Test
    public void notifies_callback_that_created_complete() {
        CreateUserTask.OnUserCreatedListener onUserCreatedListener =
                mock(CreateUserTask.OnUserCreatedListener.class);
        task.setOnUserCreatedListener(onUserCreatedListener);

        task.onPostExecute(userHelper);

        verify(onUserCreatedListener).onCreatedSuccessfully();
    }

}
