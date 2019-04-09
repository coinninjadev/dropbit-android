package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.Wallet;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TargetStatHelperTest {
    @Mock
    private DaoSessionManager daoSessionManager;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private Wallet wallet;
    @Mock
    private DustProtectionPreference dustProtectionPreference;

    @InjectMocks
    private TargetStatHelper targetStatHelper;

    @Before
    public void setUp() throws Exception {
        when(walletHelper.getWallet()).thenReturn(wallet);
        when(wallet.getId()).thenReturn(1l);
    }

    @After
    public void tearDown() {
        walletHelper = null;
        wallet = null;
        targetStatHelper = null;
        daoSessionManager = null;
        dustProtectionPreference = null;
    }

    //todo: this is a concrete test preventing accidental change. It is not a behavior test.
    @Test
    public void inits_query_rightly_with_dust_protection() {
        TargetStat mockTarget = mock(TargetStat.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        mockTargets.add(mockTarget);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);

        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(true);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        targetStatHelper.getSpendableTargets();


        ArgumentCaptor<WhereCondition> argumentCaptor = ArgumentCaptor.forClass(WhereCondition.class);

        verify(qb).where(argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());

        List<WhereCondition> values = argumentCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) values.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId())).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(1)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.WalletId.eq(1l)).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(2)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.AddressId.isNotNull()).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(3)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.Value.gt(9999L)).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(4)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.FundingId.isNull()).op));
    }

    @Test
    public void inits_query_rightly_without_dust_protection() {
        TargetStat mockTarget = mock(TargetStat.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        mockTargets.add(mockTarget);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);

        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(false);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        targetStatHelper.getSpendableTargets();


        ArgumentCaptor<WhereCondition> argumentCaptor = ArgumentCaptor.forClass(WhereCondition.class);

        verify(qb).where(argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture(), argumentCaptor.capture());

        List<WhereCondition> values = argumentCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) values.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId())).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(1)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.WalletId.eq(1l)).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(2)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.AddressId.isNotNull()).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(3)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.Value.gt(0L)).op));
        assertThat(((WhereCondition.PropertyCondition) values.get(4)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.FundingId.isNull()).op));
    }

    @Test
    public void get_spendable_targets_test() {
        TargetStat mockTarget = mock(TargetStat.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        mockTargets.add(mockTarget);


        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        List<TargetStat> targets = targetStatHelper.getSpendableTargets();

        assertThat(targets.size(), equalTo(1));
        assertThat(targets.get(0), equalTo(mockTarget));
    }

}