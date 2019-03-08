package com.coinninja.coinkeeper.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_ACCOUNT;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_COIN_TYPE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_INDEX;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_ADDRESS_DERIVATION_PATH_PURPOSE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.CHANGE_AMOUNT_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.FEE_AMOUNT_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.PAYMENT_ADDRESS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.REQUESTING_TO_SPEND_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UNSPENT_TOTAL_NUMBER_OF_UTXOS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UNSPENT_TOTAL_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UNSPENT_UTXOS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXOS;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_INDEX;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_ADDRESS_DERIVATION_PATH_PURPOSE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_REPLACEABLE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_SATOSHI_VALUE;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_TRANSACTION_ID;
import static com.coinninja.coinkeeper.model.UnspentTransactionHolder.JSONKeys.UTXO_TRANSACTION_INDEX;

public class UnspentTransactionHolder implements Parcelable {
    private static final int DERIVATION_PURPOSE = 49;
    private static final int DERIVATION_COIN_TYPE = 0;
    private static final int DERIVATION_ACCOUNT = 0;

    public final long satoshisUnspentTotal;
    public final UnspentTransactionOutput[] unspentTransactionOutputs;
    public final long satoshisRequestingToSpend;
    public final long satoshisFeeAmount;
    public final long satoshisChangeAmount;
    public final DerivationPath changePath;
    public final String paymentAddress;

    public UnspentTransactionHolder(long satoshisUnspentTotal,
                                    UnspentTransactionOutput[] unspentTransactionOutputs,
                                    long satoshisRequestingToSpend,
                                    long satoshisFeeAmount,
                                    long satoshisChangeAmount,
                                    DerivationPath changePath,
                                    String paymentAddress) {

        this.satoshisUnspentTotal = satoshisUnspentTotal;
        this.unspentTransactionOutputs = unspentTransactionOutputs;
        this.satoshisRequestingToSpend = satoshisRequestingToSpend;
        this.satoshisFeeAmount = satoshisFeeAmount;
        this.satoshisChangeAmount = satoshisChangeAmount;
        this.changePath = changePath;
        this.paymentAddress = paymentAddress;
    }


    protected UnspentTransactionHolder(Parcel in) {
        satoshisUnspentTotal = in.readLong();
        int count = in.readInt();
        unspentTransactionOutputs = new UnspentTransactionOutput[count];

        for (int i = 0; i < count; i++) {
            unspentTransactionOutputs[i] =
                    new UnspentTransactionOutput(
                            in.readString(),
                            in.readInt(),
                            in.readLong(),
                            new DerivationPath(
                                    in.readInt(),
                                    in.readInt(),
                                    in.readInt(),
                                    in.readInt(),
                                    in.readInt()
                            ),
                            (in.readByte() != 0)
                    );
        }

        satoshisRequestingToSpend = in.readLong();
        satoshisFeeAmount = in.readLong();
        satoshisChangeAmount = in.readLong();
        boolean hasChange = in.readByte() != 0;
        if (hasChange) {
            changePath = new DerivationPath(
                    in.readInt(),
                    in.readInt(),
                    in.readInt(),
                    in.readInt(),
                    in.readInt()
            );
        } else {
            changePath = null;
        }

        paymentAddress = in.readString();
    }

    public static final Creator<UnspentTransactionHolder> CREATOR = new Creator<UnspentTransactionHolder>() {
        @Override
        public UnspentTransactionHolder createFromParcel(Parcel in) {
            return new UnspentTransactionHolder(in);
        }

        @Override
        public UnspentTransactionHolder[] newArray(int size) {
            return new UnspentTransactionHolder[size];
        }
    };

    public static DerivationPath BUILD_DERIVATION(int changeDerivPath, int walletAddressIndex) {
        return new DerivationPath(DERIVATION_PURPOSE, DERIVATION_COIN_TYPE, DERIVATION_ACCOUNT, changeDerivPath, walletAddressIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(satoshisUnspentTotal);

        int length = unspentTransactionOutputs.length;
        parcel.writeInt(length);
        for (int i = 0; i < length; i++) {
            parcel.writeString(unspentTransactionOutputs[i].getTxId());
            parcel.writeInt(unspentTransactionOutputs[i].getIndex());
            parcel.writeLong(unspentTransactionOutputs[i].getAmount());
            parcel.writeInt(unspentTransactionOutputs[i].getPath().getPurpose());
            parcel.writeInt(unspentTransactionOutputs[i].getPath().getCoinType());
            parcel.writeInt(unspentTransactionOutputs[i].getPath().getAccount());
            parcel.writeInt(unspentTransactionOutputs[i].getPath().getChange());
            parcel.writeInt(unspentTransactionOutputs[i].getPath().getIndex());
            parcel.writeByte((byte) (unspentTransactionOutputs[i].isReplaceable() ? 1 : 0));
        }

        parcel.writeLong(satoshisRequestingToSpend);
        parcel.writeLong(satoshisFeeAmount);
        parcel.writeLong(satoshisChangeAmount);
        boolean hasChange = (changePath != null);
        //todo: utility to read and write booleans to remove duplication
        parcel.writeByte((byte) (hasChange ? 1 : 0));
        if (hasChange) {
            parcel.writeInt(changePath.getPurpose());
            parcel.writeInt(changePath.getCoinType());
            parcel.writeInt(changePath.getAccount());
            parcel.writeInt(changePath.getChange());
            parcel.writeInt(changePath.getIndex());
        }
        parcel.writeString(paymentAddress);
    }

    public TransactionData toTransactionData() {
        return new TransactionData(
                unspentTransactionOutputs,
                satoshisRequestingToSpend,
                satoshisFeeAmount,
                satoshisChangeAmount,
                changePath,
                paymentAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnspentTransactionHolder that = (UnspentTransactionHolder) o;
        return satoshisUnspentTotal == that.satoshisUnspentTotal &&
                satoshisRequestingToSpend == that.satoshisRequestingToSpend &&
                satoshisFeeAmount == that.satoshisFeeAmount &&
                satoshisChangeAmount == that.satoshisChangeAmount &&
                Arrays.equals(unspentTransactionOutputs, that.unspentTransactionOutputs) &&
                Objects.equals(changePath, that.changePath) &&
                Objects.equals(paymentAddress, that.paymentAddress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(satoshisUnspentTotal, satoshisRequestingToSpend, satoshisFeeAmount, satoshisChangeAmount, changePath, paymentAddress);
        result = 31 * result + Arrays.hashCode(unspentTransactionOutputs);
        return result;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UNSPENT_TOTAL_SATOSHI_VALUE, satoshisUnspentTotal);

            int length = unspentTransactionOutputs.length;
            jsonObject.put(UNSPENT_TOTAL_NUMBER_OF_UTXOS, length);
            JSONArray arrayofUtxos = new JSONArray();

            for (int i = 0; i < length; i++) {
                JSONObject jsonUtxo = new JSONObject();

                jsonUtxo.put(UTXOS, i);
                jsonUtxo.put(UTXO_TRANSACTION_ID, unspentTransactionOutputs[i].getTxId());
                jsonUtxo.put(UTXO_TRANSACTION_INDEX, unspentTransactionOutputs[i].getIndex());
                jsonUtxo.put(UTXO_SATOSHI_VALUE, unspentTransactionOutputs[i].getAmount());
                jsonUtxo.put(UTXO_ADDRESS_DERIVATION_PATH_PURPOSE, unspentTransactionOutputs[i].getPath().getPurpose());
                jsonUtxo.put(UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE, unspentTransactionOutputs[i].getPath().getCoinType());
                jsonUtxo.put(UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT, unspentTransactionOutputs[i].getPath().getAccount());
                jsonUtxo.put(UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL, unspentTransactionOutputs[i].getPath().getChange());
                jsonUtxo.put(UTXO_ADDRESS_DERIVATION_PATH_INDEX, unspentTransactionOutputs[i].getPath().getIndex());
                jsonUtxo.put(UTXO_REPLACEABLE, unspentTransactionOutputs[i].isReplaceable());


                arrayofUtxos.put(jsonUtxo);
            }

            jsonObject.put(UNSPENT_UTXOS, arrayofUtxos);
            jsonObject.put(REQUESTING_TO_SPEND_SATOSHI_VALUE, satoshisRequestingToSpend);
            jsonObject.put(FEE_AMOUNT_SATOSHI_VALUE, satoshisFeeAmount);
            jsonObject.put(CHANGE_AMOUNT_SATOSHI_VALUE, satoshisChangeAmount);
            boolean hasChange = (changePath != null);
            if (hasChange) {
                jsonObject.put(CHANGE_ADDRESS_DERIVATION_PATH_PURPOSE, changePath.getPurpose());
                jsonObject.put(CHANGE_ADDRESS_DERIVATION_PATH_COIN_TYPE, changePath.getCoinType());
                jsonObject.put(CHANGE_ADDRESS_DERIVATION_PATH_ACCOUNT, changePath.getAccount());
                jsonObject.put(CHANGE_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL, changePath.getChange());
                jsonObject.put(CHANGE_ADDRESS_DERIVATION_PATH_INDEX, changePath.getIndex());
            }
            jsonObject.put(PAYMENT_ADDRESS, paymentAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    //TODO: is a static class with static keys that we statically import in 1 prod class a good idea?

    public static class JSONKeys {
        public static final String UNSPENT_TOTAL_SATOSHI_VALUE = "UnspentTotal Satoshi Value";
        public static final String UNSPENT_TOTAL_NUMBER_OF_UTXOS = "UnspentTotal Number of UTXOs";
        public static final String UTXOS = "Utxo #";
        public static final String UTXO_TRANSACTION_ID = "Utxo Transaction ID";
        public static final String UTXO_REPLACEABLE = "Utxo Replaceable";
        public static final String UTXO_TRANSACTION_INDEX = "Utxo Transaction INDEX";
        public static final String UTXO_SATOSHI_VALUE = "Utxo Satoshi Value";
        public static final String UTXO_ADDRESS_DERIVATION_PATH_PURPOSE = "Utxo Address Derivation Path: Purpose";
        public static final String UTXO_ADDRESS_DERIVATION_PATH_COIN_TYPE = "Utxo Address Derivation Path: Coin Type";
        public static final String UTXO_ADDRESS_DERIVATION_PATH_ACCOUNT = "Utxo Address Derivation Path: Account";
        public static final String UTXO_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL = "Utxo Address Derivation Path: Internal / External";
        public static final String UTXO_ADDRESS_DERIVATION_PATH_INDEX = "Utxo Address Derivation Path: Index";
        public static final String UNSPENT_UTXOS = "Unspent UTXOs";
        public static final String REQUESTING_TO_SPEND_SATOSHI_VALUE = "Requesting To Spend Satoshi Value";
        public static final String FEE_AMOUNT_SATOSHI_VALUE = "Fee Amount Satoshi Value";
        public static final String CHANGE_AMOUNT_SATOSHI_VALUE = "Change Amount Satoshi Value";
        public static final String CHANGE_ADDRESS_DERIVATION_PATH_PURPOSE = "Change Address Derivation Path: Purpose";
        public static final String CHANGE_ADDRESS_DERIVATION_PATH_COIN_TYPE = "Change Address Derivation Path: Coin Type";
        public static final String CHANGE_ADDRESS_DERIVATION_PATH_ACCOUNT = "Change Address Derivation Path: Account";
        public static final String CHANGE_ADDRESS_DERIVATION_PATH_INTERNAL_EXTERNAL = "Change Address Derivation Path: Internal / External";
        public static final String CHANGE_ADDRESS_DERIVATION_PATH_INDEX = "Change Address Derivation Path: Index";
        public static final String PAYMENT_ADDRESS = "Payment Address";
    }
}
