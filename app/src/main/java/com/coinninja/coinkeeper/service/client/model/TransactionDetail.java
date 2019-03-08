package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionDetail {
    @SerializedName("txid")
    private String transactionId;
    private String hash;
    private int size;
    private int vsize;
    private long weight;
    private int version;
    private long locktime;
    @SerializedName("coinbase")
    private boolean isCoinbase;
    @SerializedName("txinwitness")
    private String[] witnesses;
    private String blockhash;
    private int height;
    private int blockheight;
    private long time;
    private long blocktime;
    @SerializedName("vin")
    private List<VIn> vInList;
    @SerializedName("vout")
    private List<VOut> vOutList;

    @SerializedName("received_time")
    private long receivedTime;


    public String getTransactionId() {
        return transactionId;
    }

    public String getHash() {
        return hash;
    }

    public int getSize() {
        return size;
    }

    public int getVsize() {
        return vsize;
    }

    public long getWeight() {
        return weight;
    }

    public int getVersion() {
        return version;
    }

    public long getLocktime() {
        return locktime;
    }

    public int getHeight() {
        return height;
    }

    public int getBlockheight() {
        return blockheight;
    }

    public boolean getIsCoinbase() {
        return isCoinbase;
    }

    public String getBlockhash() {
        return blockhash;
    }

    public long getBlocktime() {
        // convert to millis
        return blocktime * 1000;
    }

    public long getReceivedTime() {
        // convert to millis
        return receivedTime * 1000;
    }

    public String[] getWitnesses() {
        return witnesses;
    }

    public long getTime() {
        //convert to millis
        return time * 1000;
    }

    public List<VIn> getvInList() {
        return vInList;
    }

    public List<VOut> getvOutList() {
        return vOutList;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setReceiveTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

}
