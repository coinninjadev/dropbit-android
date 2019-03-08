package com.coinninja.coinkeeper.service.client.model.injection;

import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.VIn;
import com.coinninja.coinkeeper.service.client.model.VOut;

import java.util.List;

public class TransactionDetailInjectModel extends TransactionDetail {
    private String transactionId; //YESS
    private String blockhash; //YESS = ""
    private int height; //YESS = 0
    private int numConfirmations; // YESS = 0
    private long timeMS; //YESS = NOW
    private long blocktimeMS;//YESS = 0
    private List<VIn> vInList;
    private List<VOut> vOutList;

    //private int size; //Maybe If needed
    //private int vsize; //Maybe If needed
    //private long weight; //Maybe If needed

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setBlockhash(String blockhash) {
        this.blockhash = blockhash;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setNumConfirmations(int numConfirmations) {
        this.numConfirmations = numConfirmations;
    }

    public void setTimeMS(long timeMS) {
        this.timeMS = timeMS;
    }

    public void setBlockTimeMS(long blocktimeMS) {
        this.blocktimeMS = blocktimeMS;
    }

    public void setvOutList(List<VOut> vOutList) {
        this.vOutList = vOutList;
    }

    public void setvInList(List<VIn> vInList) {
        this.vInList = vInList;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getBlockhash() {
        return blockhash;
    }

    @Override
    public long getBlocktime() {
        return blocktimeMS;
    }

    @Override
    public long getTime() {
        return timeMS;
    }

    @Override
    public List<VIn> getvInList() {
        return vInList;
    }

    @Override
    public List<VOut> getvOutList() {
        return vOutList;
    }
}

