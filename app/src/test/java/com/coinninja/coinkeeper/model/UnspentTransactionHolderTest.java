package com.coinninja.coinkeeper.model;

import android.os.Parcel;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_ACCOUNT;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_COIN_TYPE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_INDEX;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_PURPOSE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_AMOUNT_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.FEE_AMOUNT_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.PAYMENT_ADDRESS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.REQUESTING_TO_SPEND_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UNSPENT_TOTAL_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UNSPENT_UTXOS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_INDEX;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_PURPOSE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_REPLACEABLE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_TRANSACTION_ID;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_TRANSACTION_INDEX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class UnspentTransactionHolderTest {

    private DerivationPath sampleSpendableDerivationPath1;
    private DerivationPath sampleSpendableDerivationPath2;
    private UnspentTransactionOutput sampleUnspentTransactionOutput1;
    private UnspentTransactionOutput sampleUnspentTransactionOutput2;

    private long sampleSatoshisUnspentTotal;
    private UnspentTransactionOutput[] sampleUnspentTransOuts;
    private long sampleSatoshisRequestingToSpend;
    private long sampleSatoshisFeeAmount;
    private long sampleSatoshisChangeAmount;
    private DerivationPath sampleChangePath;
    private String samplePaymentAddress;


    @Before
    public void setUp() throws Exception {

        sampleSpendableDerivationPath1 = sampleDerivationPath1();
        sampleSpendableDerivationPath2 = sampleDerivationPath2();
        sampleUnspentTransactionOutput1 = sampleUnspentTransactionOutput1(sampleSpendableDerivationPath1);
        sampleUnspentTransactionOutput2 = sampleUnspentTransactionOutput2(sampleSpendableDerivationPath2);

        sampleSatoshisUnspentTotal = 4568l;
        sampleUnspentTransOuts = new UnspentTransactionOutput[]{sampleUnspentTransactionOutput1, sampleUnspentTransactionOutput2};
        sampleSatoshisRequestingToSpend = 947284l;
        sampleSatoshisFeeAmount = 1120l;
        sampleSatoshisChangeAmount = 33212l;
        sampleChangePath = sampleChangeDerivationPath();
        samplePaymentAddress = "3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS";
    }

    @Test
    public void parcelable_write_read_no_change() {
        long expectedSatoshisUnspentTotal = sampleSatoshisUnspentTotal;
        int expectedUnspentTransOutsLength = 2;
        long expectedSatoshisRequestingToSpend = 947284l;
        long expectedSatoshisFeeAmount = 1120l;

        String expectedPaymentAddress = "3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS";

        int expectedIndex1 = 66;
        String expectedTXid1 = "xlksdhjf98lsknu9sok3i20iks0ih";
        long expectedAmount1 = 372874l;
        Integer expectedPath1_purpose = 55;
        Integer expectedPath1_coinType = 67;
        Integer expectedPath1_account = 56543;
        Integer expectedPath1_change = 98324;
        Integer expectedPath1_index = 87621;

        int expectedIndex2 = 86;
        String expectedTXid2 = "fj89ejs0js0idsesr5sdssrf";
        long expectedAmount2 = 55222223L;
        Integer expectedPath2_purpose = 3;
        Integer expectedPath2_coinType = 12;
        Integer expectedPath2_account = 444;
        Integer expectedPath2_change = 989;
        Integer expectedPath2_index = 332311;

        Parcel parcel = Parcel.obtain();
        UnspentTransactionHolder expectedUnspentTrans =
                new UnspentTransactionHolder(
                        sampleSatoshisUnspentTotal,
                        sampleUnspentTransOuts,
                        sampleSatoshisRequestingToSpend,
                        sampleSatoshisFeeAmount,
                        0,
                        null,
                        samplePaymentAddress);

        expectedUnspentTrans.writeToParcel(parcel, -1);
        parcel.setDataPosition(0);
        UnspentTransactionHolder repackaged = UnspentTransactionHolder.CREATOR.createFromParcel(parcel);

        assertThat(expectedSatoshisUnspentTotal, equalTo(repackaged.satoshisUnspentTotal));
        UnspentTransactionOutput[] repackagedOuts = repackaged.unspentTransactionOutputs;
        assertThat(expectedUnspentTransOutsLength, equalTo(repackagedOuts.length));
        assertThat(expectedSatoshisRequestingToSpend, equalTo(repackaged.satoshisRequestingToSpend));
        assertThat(expectedSatoshisFeeAmount, equalTo(repackaged.satoshisFeeAmount));
        assertThat(0L, equalTo(repackaged.satoshisChangeAmount));
        assertNull(repackaged.changePath);

        assertThat(expectedPaymentAddress, equalTo(repackaged.paymentAddress));

        UnspentTransactionOutput repackagedOut1 = repackagedOuts[0];
        DerivationPath repackagedOut1DerivationPath = repackagedOut1.getPath();

        assertThat(repackagedOut1.getIndex(), equalTo(expectedIndex1));
        assertThat(repackagedOut1.getAmount(), equalTo(expectedAmount1));
        assertThat(repackagedOut1.getTxId(), equalTo(expectedTXid1));
        assertThat(repackagedOut1DerivationPath.getIndex(), equalTo(expectedPath1_index));
        assertThat(repackagedOut1DerivationPath.getAccount(), equalTo(expectedPath1_account));
        assertThat(repackagedOut1DerivationPath.getChange(), equalTo(expectedPath1_change));
        assertThat(repackagedOut1DerivationPath.getCoinType(), equalTo(expectedPath1_coinType));
        assertThat(repackagedOut1DerivationPath.getPurpose(), equalTo(expectedPath1_purpose));
        assertFalse(repackagedOut1.isReplaceable());

        UnspentTransactionOutput repackagedOut2 = repackagedOuts[1];
        DerivationPath repackagedOut2DerivationPath = repackagedOut2.getPath();

        assertThat(repackagedOut2.getIndex(), equalTo(expectedIndex2));
        assertThat(repackagedOut2.getAmount(), equalTo(expectedAmount2));
        assertThat(repackagedOut2.getTxId(), equalTo(expectedTXid2));
        assertThat(repackagedOut2DerivationPath.getIndex(), equalTo(expectedPath2_index));
        assertThat(repackagedOut2DerivationPath.getAccount(), equalTo(expectedPath2_account));
        assertThat(repackagedOut2DerivationPath.getChange(), equalTo(expectedPath2_change));
        assertThat(repackagedOut2DerivationPath.getCoinType(), equalTo(expectedPath2_coinType));
        assertThat(repackagedOut2DerivationPath.getPurpose(), equalTo(expectedPath2_purpose));
        assertTrue(repackagedOut2.isReplaceable());

    }

    @Test
    public void parcelable_write_read_Test_with_2_spendable_utxos_1_change_address() {
        long expectedSatoshisUnspentTotal = sampleSatoshisUnspentTotal;
        int expectedUnspentTransOutsLength = 2;
        long expectedSatoshisRequestingToSpend = 947284l;
        long expectedSatoshisFeeAmount = 1120l;
        long expectedSatoshisChangeAmount = 33212l;
        Integer expectedChangePath_purpose = 3;
        Integer expectedChangePath_coinType = 22;
        Integer expectedChangePath_account = 67142;
        Integer expectedChangePath_change = 1021;
        Integer expectedChangePath_index = 50482;
        String expectedPaymentAddress = "3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS";

        int expectedIndex1 = 66;
        String expectedTXid1 = "xlksdhjf98lsknu9sok3i20iks0ih";
        long expectedAmount1 = 372874l;
        Integer expectedPath1_purpose = 55;
        Integer expectedPath1_coinType = 67;
        Integer expectedPath1_account = 56543;
        Integer expectedPath1_change = 98324;
        Integer expectedPath1_index = 87621;

        int expectedIndex2 = 86;
        String expectedTXid2 = "fj89ejs0js0idsesr5sdssrf";
        long expectedAmount2 = 55222223L;
        Integer expectedPath2_purpose = 3;
        Integer expectedPath2_coinType = 12;
        Integer expectedPath2_account = 444;
        Integer expectedPath2_change = 989;
        Integer expectedPath2_index = 332311;

        Parcel parcel = Parcel.obtain();
        UnspentTransactionHolder expectedUnspentTrans =
                new UnspentTransactionHolder(
                        sampleSatoshisUnspentTotal,
                        sampleUnspentTransOuts,
                        sampleSatoshisRequestingToSpend,
                        sampleSatoshisFeeAmount,
                        sampleSatoshisChangeAmount,
                        sampleChangePath,
                        samplePaymentAddress);

        expectedUnspentTrans.writeToParcel(parcel, -1);
        parcel.setDataPosition(0);
        UnspentTransactionHolder repackaged = UnspentTransactionHolder.CREATOR.createFromParcel(parcel);

        assertThat(expectedSatoshisUnspentTotal, equalTo(repackaged.satoshisUnspentTotal));
        UnspentTransactionOutput[] repackagedOuts = repackaged.unspentTransactionOutputs;
        assertThat(expectedUnspentTransOutsLength, equalTo(repackagedOuts.length));
        assertThat(expectedSatoshisRequestingToSpend, equalTo(repackaged.satoshisRequestingToSpend));
        assertThat(expectedSatoshisFeeAmount, equalTo(repackaged.satoshisFeeAmount));
        assertThat(expectedSatoshisChangeAmount, equalTo(repackaged.satoshisChangeAmount));
        DerivationPath repackagedChangePath = repackaged.changePath;
        assertThat(repackagedChangePath.getIndex(), equalTo(expectedChangePath_index));
        assertThat(repackagedChangePath.getAccount(), equalTo(expectedChangePath_account));
        assertThat(repackagedChangePath.getChange(), equalTo(expectedChangePath_change));
        assertThat(repackagedChangePath.getCoinType(), equalTo(expectedChangePath_coinType));
        assertThat(repackagedChangePath.getPurpose(), equalTo(expectedChangePath_purpose));
        assertThat(expectedPaymentAddress, equalTo(repackaged.paymentAddress));


        UnspentTransactionOutput repackagedOut1 = repackagedOuts[0];
        DerivationPath repackagedOut1DerivationPath = repackagedOut1.getPath();

        assertThat(repackagedOut1.getIndex(), equalTo(expectedIndex1));
        assertThat(repackagedOut1.getAmount(), equalTo(expectedAmount1));
        assertThat(repackagedOut1.getTxId(), equalTo(expectedTXid1));
        assertThat(repackagedOut1DerivationPath.getIndex(), equalTo(expectedPath1_index));
        assertThat(repackagedOut1DerivationPath.getAccount(), equalTo(expectedPath1_account));
        assertThat(repackagedOut1DerivationPath.getChange(), equalTo(expectedPath1_change));
        assertThat(repackagedOut1DerivationPath.getCoinType(), equalTo(expectedPath1_coinType));
        assertThat(repackagedOut1DerivationPath.getPurpose(), equalTo(expectedPath1_purpose));


        UnspentTransactionOutput repackagedOut2 = repackagedOuts[1];
        DerivationPath repackagedOut2DerivationPath = repackagedOut2.getPath();

        assertThat(repackagedOut2.getIndex(), equalTo(expectedIndex2));
        assertThat(repackagedOut2.getAmount(), equalTo(expectedAmount2));
        assertThat(repackagedOut2.getTxId(), equalTo(expectedTXid2));
        assertThat(repackagedOut2DerivationPath.getIndex(), equalTo(expectedPath2_index));
        assertThat(repackagedOut2DerivationPath.getAccount(), equalTo(expectedPath2_account));
        assertThat(repackagedOut2DerivationPath.getChange(), equalTo(expectedPath2_change));
        assertThat(repackagedOut2DerivationPath.getCoinType(), equalTo(expectedPath2_coinType));
        assertThat(repackagedOut2DerivationPath.getPurpose(), equalTo(expectedPath2_purpose));
    }

    @Test
    public void to_JSON_test_with_2_spendable_utxos_1_change_address() throws JSONException {
        long expectedSatoshisUnspentTotal = sampleSatoshisUnspentTotal;
        int expectedUnspentTransOutsLength = 2;
        long expectedSatoshisRequestingToSpend = 947284l;
        long expectedSatoshisFeeAmount = 1120l;
        long expectedSatoshisChangeAmount = 33212l;
        Integer expectedChangePath_purpose = 3;
        Integer expectedChangePath_coinType = 22;
        Integer expectedChangePath_account = 67142;
        Integer expectedChangePath_change = 1021;
        Integer expectedChangePath_index = 50482;
        String expectedPaymentAddress = "3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS";

        int expectedIndex1 = 66;
        String expectedTXid1 = "xlksdhjf98lsknu9sok3i20iks0ih";
        long expectedAmount1 = 372874l;
        Integer expectedPath1_purpose = 55;
        Integer expectedPath1_coinType = 67;
        Integer expectedPath1_account = 56543;
        Integer expectedPath1_change = 98324;
        Integer expectedPath1_index = 87621;

        int expectedIndex2 = 86;
        String expectedTXid2 = "fj89ejs0js0idsesr5sdssrf";
        long expectedAmount2 = 55222223L;
        Integer expectedPath2_purpose = 3;
        Integer expectedPath2_coinType = 12;
        Integer expectedPath2_account = 444;
        Integer expectedPath2_change = 989;
        Integer expectedPath2_index = 332311;

        UnspentTransactionHolder expectedUnspentTrans =
                new UnspentTransactionHolder(
                        sampleSatoshisUnspentTotal,
                        sampleUnspentTransOuts,
                        sampleSatoshisRequestingToSpend,
                        sampleSatoshisFeeAmount,
                        sampleSatoshisChangeAmount,
                        sampleChangePath,
                        samplePaymentAddress);


        JSONObject jsonObject = expectedUnspentTrans.toJSONObject();

        assertThat(expectedSatoshisUnspentTotal, equalTo(jsonObject.get(UNSPENT_TOTAL_SATOSHI_VALUE)));

        assertThat(expectedSatoshisRequestingToSpend, equalTo(jsonObject.get(REQUESTING_TO_SPEND_SATOSHI_VALUE)));
        assertThat(expectedSatoshisFeeAmount, equalTo(jsonObject.get(FEE_AMOUNT_SATOSHI_VALUE)));
        assertThat(expectedSatoshisChangeAmount, equalTo(jsonObject.get(CHANGE_AMOUNT_SATOSHI_VALUE)));
        assertThat(jsonObject.get(CHANGE_ADDRESS_DERIVATION_PATH_INDEX), equalTo(expectedChangePath_index));
        assertThat(jsonObject.get(CHANGE_ADDRESS_DERIVATION_PATH_ACCOUNT), equalTo(expectedChangePath_account));
        assertThat(jsonObject.get(CHANGE_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL), equalTo(expectedChangePath_change));
        assertThat(jsonObject.get(CHANGE_ADDRESS_DERIVATION_PATH_COIN_TYPE), equalTo(expectedChangePath_coinType));
        assertThat(jsonObject.get(CHANGE_ADDRESS_DERIVATION_PATH_PURPOSE), equalTo(expectedChangePath_purpose));
        assertThat(expectedPaymentAddress, equalTo(jsonObject.get(PAYMENT_ADDRESS)));


        JSONArray repackagedOuts = (JSONArray) jsonObject.get(UNSPENT_UTXOS);
        assertThat(expectedUnspentTransOutsLength, equalTo(repackagedOuts.length()));
        JSONObject repackagedOut1 = repackagedOuts.getJSONObject(0);

        assertThat(repackagedOut1.get(UTXO_TRANSACTION_INDEX), equalTo(expectedIndex1));
        assertThat(repackagedOut1.get(UTXO_SATOSHI_VALUE), equalTo(expectedAmount1));
        assertThat(repackagedOut1.get(UTXO_TRANSACTION_ID), equalTo(expectedTXid1));
        assertThat(repackagedOut1.get(UTXO_ADDRESS_DERIVATION_PATH_INDEX), equalTo(expectedPath1_index));
        assertThat(repackagedOut1.get(UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT), equalTo(expectedPath1_account));
        assertThat(repackagedOut1.get(UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL), equalTo(expectedPath1_change));
        assertThat(repackagedOut1.get(UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE), equalTo(expectedPath1_coinType));
        assertThat(repackagedOut1.get(UTXO_ADDRESS_DERIVATION_PATH_PURPOSE), equalTo(expectedPath1_purpose));
        assertFalse((Boolean) repackagedOut1.get(UTXO_REPLACEABLE));


        JSONObject repackagedOut2 = repackagedOuts.getJSONObject(1);

        assertThat(repackagedOut2.get(UTXO_TRANSACTION_INDEX), equalTo(expectedIndex2));
        assertThat(repackagedOut2.get(UTXO_SATOSHI_VALUE), equalTo(expectedAmount2));
        assertThat(repackagedOut2.get(UTXO_TRANSACTION_ID), equalTo(expectedTXid2));
        assertThat(repackagedOut2.get(UTXO_ADDRESS_DERIVATION_PATH_INDEX), equalTo(expectedPath2_index));
        assertThat(repackagedOut2.get(UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT), equalTo(expectedPath2_account));
        assertThat(repackagedOut2.get(UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL), equalTo(expectedPath2_change));
        assertThat(repackagedOut2.get(UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE), equalTo(expectedPath2_coinType));
        assertThat(repackagedOut2.get(UTXO_ADDRESS_DERIVATION_PATH_PURPOSE), equalTo(expectedPath2_purpose));
        assertTrue((Boolean) repackagedOut2.get(UTXO_REPLACEABLE));
    }

    private UnspentTransactionOutput sampleUnspentTransactionOutput1(DerivationPath derivationPath) {
        String txId = "xlksdhjf98lsknu9sok3i20iks0ih";
        int index = 66;
        long amount = 372874l;

        return new UnspentTransactionOutput(txId, index, amount, derivationPath, false);
    }

    private UnspentTransactionOutput sampleUnspentTransactionOutput2(DerivationPath derivationPath) {
        String txId = "fj89ejs0js0idsesr5sdssrf";
        int index = 86;
        long amount = 55222223L;

        return new UnspentTransactionOutput(txId, index, amount, derivationPath, true);
    }

    private DerivationPath sampleDerivationPath1() {
        Integer purpose = 55;
        Integer coinType = 67;
        Integer account = 56543;
        Integer change = 98324;
        Integer index = 87621;

        return new DerivationPath(purpose, coinType, account, change, index);
    }

    private DerivationPath sampleDerivationPath2() {
        Integer purpose = 3;
        Integer coinType = 12;
        Integer account = 444;
        Integer change = 989;
        Integer index = 332311;

        return new DerivationPath(purpose, coinType, account, change, index);
    }

    private DerivationPath sampleChangeDerivationPath() {
        Integer purpose = 3;
        Integer coinType = 22;
        Integer account = 67142;
        Integer change = 1021;
        Integer index = 50482;

        return new DerivationPath(purpose, coinType, account, change, index);
    }
}