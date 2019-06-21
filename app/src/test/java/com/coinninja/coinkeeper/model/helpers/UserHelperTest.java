package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.db.UserDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UserHelperTest {

    @Mock
    private UserDao userDao;

    @Mock
    private User user;

    @Mock
    private DaoSessionManager daoSessionManager;

    @Mock
    private QueryBuilder query;

    private UserHelper userHelper;

    @Before
    public void setUp() {
        when(daoSessionManager.getUserDao()).thenReturn(userDao);
        when(userDao.queryBuilder()).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(user);
        userHelper = new UserHelper(daoSessionManager);
    }

    @Test
    public void helperSavesPinOnUser() {
        String pin = "my secret pin";

        userHelper.savePin(pin);

        verify(user, times(1)).setPin(pin);
        verify(user).update();
    }

    @Test
    public void canRetrivePinFromUser() {
        String pin = "my secret pin";
        User user = new User();
        user.setPin(pin);
        when(query.unique()).thenReturn(user);

        userHelper.getPin();

        assertThat(userHelper.getPin(), equalTo(pin));
    }
}