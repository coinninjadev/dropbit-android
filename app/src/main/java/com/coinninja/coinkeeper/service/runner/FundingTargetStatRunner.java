package com.coinninja.coinkeeper.service.runner;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;

public class FundingTargetStatRunner extends AsyncTask<Long, Integer, FundingUTXOs> implements FundingUTXOs.ProgressListener {
    private final FundingRunnable fundingRunnable;

    private FundingTargetStatListener listener;


    public FundingTargetStatRunner(FundingRunnable fundingRunnable) {
        this.fundingRunnable = fundingRunnable;
    }

    @Override
    protected FundingUTXOs doInBackground(Long... satoshisRequestingSpendArray) {
        return fundingRunnable.fundRun(satoshisRequestingSpendArray[0], this);
    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        listener.onFundingProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(FundingUTXOs fundingUTXOs) {
        FundingRunnable.FundedHolder fundedHolder = fundingRunnable.evaluateFundingUTXOs(fundingUTXOs);

        if (fundedHolder.getUnspentTransactionHolder() == null) {
            listener.onFundingError(fundedHolder.getErrorReason(), fundedHolder.getSatoshisFee());
        } else {
            listener.onFundingSuccessful(fundedHolder.getUnspentTransactionHolder(), fundedHolder.getSatoshisFee());
        }
    }

    public void setListener(FundingTargetStatListener listener) {
        this.listener = listener;
    }


    @Override
    public void onProgressUpdate(int progress) {
        publishProgress(progress);
        fakeSleep(100);
    }

    public interface FundingTargetStatListener {

        void onFundingSuccessful(UnspentTransactionHolder unspentTransactionHolder, long satoshisFee);

        void onFundingProgress(int progress);

        void onFundingError(String errorMessage, long satoshisFee);

    }

    private void fakeSleep(long timeMS) {
        try {
            Thread.sleep(timeMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

