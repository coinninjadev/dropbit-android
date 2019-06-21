package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Test
    public void convertsToUnspentTransactionOutput() {
        TargetStat targetStat = mock(TargetStat.class);
        when(targetStat.toUnspentTranasactionOutput()).thenCallRealMethod();
        when(targetStat.getValue()).thenReturn(1000L);
        when(targetStat.getPosition()).thenReturn(1);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(transactionSummary.getTxid()).thenReturn("--txid--");
        when(transactionSummary.isReplaceable()).thenReturn(true);
        Address address = new Address();
        address.setChangeIndex(1);
        address.setIndex(50);
        when(targetStat.getTransaction()).thenReturn(transactionSummary);
        when(targetStat.getAddress()).thenReturn(address);

        UnspentTransactionOutput unspentTransactionOutput = targetStat.toUnspentTranasactionOutput();

        assertThat(unspentTransactionOutput.getAmount(), equalTo(1000L));
        assertThat(unspentTransactionOutput.getIndex(), equalTo(1));
        assertThat(unspentTransactionOutput.isReplaceable(), equalTo(true));
        assertThat(unspentTransactionOutput.getTxId(), equalTo("--txid--"));
        assertThat(unspentTransactionOutput.getPath().getPurpose(), equalTo(49));
        assertThat(unspentTransactionOutput.getPath().getCoinType(), equalTo(0));
        assertThat(unspentTransactionOutput.getPath().getAccount(), equalTo(0));
        assertThat(unspentTransactionOutput.getPath().getChange(), equalTo(1));
        assertThat(unspentTransactionOutput.getPath().getIndex(), equalTo(50));
    }

    //todo: this is a concrete test preventing accidental change. It is not a behavior test.
    @Test
    public void inits_query_rightly_with_dust_protection() {
        Join<TargetStat, TransactionSummary> join = mock(Join.class);
        TargetStat mockTarget = mock(TargetStat.class);
        Address address = mock(Address.class);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(address.getChangeIndex()).thenReturn(HDWallet.EXTERNAL);
        when(transactionSummary.getNumConfirmations()).thenReturn(3);
        when(mockTarget.getAddress()).thenReturn(address);
        when(mockTarget.getTransaction()).thenReturn(transactionSummary);
        List<TargetStat> mockTargets = new ArrayList<>();
        mockTargets.add(mockTarget);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);

        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(true);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary.class)).thenReturn(join);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        targetStatHelper.getSpendableTargets();


        ArgumentCaptor<WhereCondition> whereConditionArgumentCaptor = ArgumentCaptor.forClass(WhereCondition.class);
        ArgumentCaptor<WhereCondition> joinCaptor = ArgumentCaptor.forClass(WhereCondition.class);

        verify(qb).where(whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture());
        verify(join).where(joinCaptor.capture());

        List<WhereCondition> joinValues = joinCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) joinValues.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.getId(),
                        MemPoolState.DOUBLE_SPEND.getId(),
                        MemPoolState.ORPHANED.getId()
                )).op));

        List<WhereCondition> whereValues = whereConditionArgumentCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId())).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(1)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.WalletId.eq(1l)).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(2)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.FundingId.isNull()).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(3)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.Value.gt(9999L)).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(4)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.AddressId.isNotNull()).op));
    }

    @Test
    public void inits_query_rightly_without_dust_protection() {
        TargetStat mockTarget = mock(TargetStat.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        Join<TargetStat, TransactionSummary> join = mock(Join.class);
        Address address = mock(Address.class);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(address.getChangeIndex()).thenReturn(HDWallet.EXTERNAL);
        when(transactionSummary.getNumConfirmations()).thenReturn(3);
        when(mockTarget.getAddress()).thenReturn(address);
        when(mockTarget.getTransaction()).thenReturn(transactionSummary);
        mockTargets.add(mockTarget);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);

        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(false);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary.class)).thenReturn(join);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        targetStatHelper.getSpendableTargets();


        ArgumentCaptor<WhereCondition> whereConditionArgumentCaptor = ArgumentCaptor.forClass(WhereCondition.class);
        ArgumentCaptor<WhereCondition> joinCaptor = ArgumentCaptor.forClass(WhereCondition.class);

        verify(qb).where(whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture());
        verify(join).where(joinCaptor.capture());


        List<WhereCondition> joinValues = joinCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) joinValues.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.getId(),
                        MemPoolState.DOUBLE_SPEND.getId(),
                        MemPoolState.ORPHANED.getId()
                )).op));

        List<WhereCondition> whereValues = whereConditionArgumentCaptor.getAllValues();
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(0)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId())).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(1)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.WalletId.eq(1l)).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(2)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.FundingId.isNull()).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(3)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.Value.gt(9999L)).op));
        assertThat(((WhereCondition.PropertyCondition) whereValues.get(4)).op,
                equalTo(((WhereCondition.PropertyCondition) TargetStatDao.Properties.AddressId.isNotNull()).op));
    }

    @Test
    public void get_spendable_targets_test() {
        Join<TargetStat, TransactionSummary> join = mock(Join.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        TargetStat targetStat = mock(TargetStat.class);
        Address address = mock(Address.class);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(address.getChangeIndex()).thenReturn(HDWallet.EXTERNAL);
        when(transactionSummary.getNumConfirmations()).thenReturn(3);
        when(targetStat.getAddress()).thenReturn(address);
        when(targetStat.getTransaction()).thenReturn(transactionSummary);
        mockTargets.add(targetStat);


        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary.class)).thenReturn(join);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);


        List<TargetStat> targets = targetStatHelper.getSpendableTargets();

        assertThat(targets.size(), equalTo(1));
        assertThat(targets.get(0), equalTo(targetStat));
    }

    @Test
    public void get_spendable_targets_test_remove_external_without_enough_confirmations() {
        Join<TargetStat, TransactionSummary> join = mock(Join.class);
        List<TargetStat> mockTargets = new ArrayList<>();
        TargetStat targetStat = mock(TargetStat.class);
        Address address = mock(Address.class);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(address.getChangeIndex()).thenReturn(HDWallet.EXTERNAL);
        when(transactionSummary.getNumConfirmations()).thenReturn(0);
        when(targetStat.getAddress()).thenReturn(address);
        when(targetStat.getTransaction()).thenReturn(transactionSummary);
        mockTargets.add(targetStat);


        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        TargetStatDao mockTargetStatDao = mock(TargetStatDao.class);
        when(daoSessionManager.getTargetStatDao()).thenReturn(mockTargetStatDao);
        when(mockTargetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any(), any(), any(), any())).thenReturn(qb);
        when(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary.class)).thenReturn(join);
        when(qb.orderAsc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(mockTargets);

        List<TargetStat> targets = targetStatHelper.getSpendableTargets();

        assertThat(targets.size(), equalTo(0));
    }

}