package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.db.UserDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UserHelperTest {

    @Mock
    WalletHelper walletHelper;

    @Mock
    private UserDao userDao;

    @Mock
    private User user;

    @Mock
    private DaoSessionManager daoSessionManager;


    private List<User> users = new ArrayList<>();

    @Mock
    private WalletDao walletDao;

    private List<Wallet> wallets = new ArrayList<>();

    @Mock
    private Wallet wallet;

    @Mock
    private QueryBuilder query;

    private UserHelper userHelper;

    @Before
    public void setUp() {
        wallets.add(wallet);
        users.add(user);
        when(daoSessionManager.getUserDao()).thenReturn(userDao);
        when(daoSessionManager.getWalletDao()).thenReturn(walletDao);
        when(walletDao.loadAll()).thenReturn(wallets);
        when(userDao.queryBuilder()).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(user);
        when(user.getWallets()).thenReturn(wallets);
        when(walletHelper.getWallet()).thenReturn(wallet);
        userHelper = new UserHelper(daoSessionManager, walletHelper);
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

    @Test
    public void create_new_user_test() {
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);

        userHelper.createFirstUser();

        verify(userDao).insert(argumentCaptor.capture());
        User user = argumentCaptor.getValue();
        verify(daoSessionManager).createWallet(user);
    }

    @Test
    public void user_has_completed_training_screens_test() {
        when(user.getCompletedTraining()).thenReturn(true);

        boolean hasCompletedTraining = userHelper.hasCompletedTraining();

        assertThat(hasCompletedTraining, equalTo(true));
    }

    @Test
    public void user_has_NOT_completed_training_screens_test() {
        when(user.getCompletedTraining()).thenReturn(false);

        boolean hasCompletedTraining = userHelper.hasCompletedTraining();

        assertThat(hasCompletedTraining, equalTo(false));
    }

    @Test
    public void user_object_null_while_asking_about_training_screens_test() {
        when(query.unique()).thenReturn(null);

        boolean hasCompletedTraining = userHelper.hasCompletedTraining();

        assertThat(hasCompletedTraining, equalTo(false));
    }
}