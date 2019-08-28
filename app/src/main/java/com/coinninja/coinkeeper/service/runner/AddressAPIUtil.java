package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperClient;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

import static com.coinninja.coinkeeper.service.client.CoinKeeperClient.ADDRESSES_RESULT_LIMIT;
import static com.coinninja.coinkeeper.service.client.CoinKeeperClient.ADDRESSES_TO_QUERY_AT_A_TIME;

public class AddressAPIUtil {
    public static final String TAG = AddressAPIUtil.class.getSimpleName();

    public static final int INITIAL_GAP_LIMIT = 20;
    public static final int LOOK_AHEAD = 5;
    private CoinKeeperApiClient apiClient;

    private HDWallet hdWallet;
    private int largestIndexConsumed;
    private int lookAhead;
    private int numAddressesToFetch;
    private int numAddressesFetched;
    private int changeIndex;

    @Inject
    AddressAPIUtil(CoinKeeperApiClient apiClient) {
        this.apiClient = apiClient;
        lookAhead = LOOK_AHEAD;
    }

    public void setLookAhead(int lookAhead) {
        this.lookAhead = lookAhead;
    }

    public List<GsonAddress> fetchAddresses(HDWallet hdWallet, int changeIndex, int start, int currentIndex) {
        this.hdWallet = hdWallet;
        this.changeIndex = changeIndex;
        largestIndexConsumed = 0;
        numAddressesToFetch = currentIndex == 0 ? lookAhead : currentIndex + lookAhead;
        numAddressesFetched = 0;
        List<GsonAddress> addresses = fetchAddresses(start);
        if (shouldSeekAhead()) {
            addresses.addAll(seekAhead());
        } else if (shouldLookAheadForInitialSync(changeIndex, start, currentIndex, addresses)) {
            addresses.addAll(seekAhead());
        }
        return addresses;
    }


    private int calcBufferSize() {
        return (calcNumRemaining() > ADDRESSES_TO_QUERY_AT_A_TIME) ? ADDRESSES_TO_QUERY_AT_A_TIME : calcNumRemaining();
    }

    private int calcNumRemaining() {
        return numAddressesToFetch - numAddressesFetched;
    }

    private List<GsonAddress> fetchAddresses(int startIndex) {
        String[] requestedAddresses = getBlockToRequest(startIndex);
        List<GsonAddress> addresses = fetchAddresses(requestedAddresses, 1);
        numAddressesFetched += requestedAddresses.length;
        setDerivationIndexes(startIndex, requestedAddresses, addresses);

        if (calcNumRemaining() > 0) {
            addresses.addAll(fetchAddresses(startIndex + requestedAddresses.length));
        }

        return addresses;
    }

    private List<GsonAddress> fetchAddresses(String[] requestedAddresses, int page) {
        List<GsonAddress> addresses;
        addresses = queryForAddresses(requestedAddresses, page);
        if (addresses.size() >= ADDRESSES_RESULT_LIMIT) {
            addresses.addAll(fetchAddresses(requestedAddresses, page + 1));
        }
        return addresses;
    }

    private List<GsonAddress> queryForAddresses(String[] requestedAddresses, int page) {
        List<GsonAddress> addresses;
        Response response = apiClient.queryAddressesFor(requestedAddresses, page);
        if (response.isSuccessful()) {
            addresses = (List<GsonAddress>) response.body();
        } else {
            addresses = new ArrayList<>();

            Log.d(TAG, "|---- Get Batch Addresses");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            try {
                Log.d(TAG, "|--------- message: " + response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return addresses;
    }

    private void setDerivationIndexes(int startIndex, String[] requestedAddresses, List<GsonAddress> addresses) {
        List<String> paths = Arrays.asList(requestedAddresses);
        for (GsonAddress address : addresses) {
            int derivationIndex = startIndex + paths.indexOf(address.getAddress());
            address.setDerivationIndex(derivationIndex);
            trackDerivationIndex(derivationIndex);
        }
    }

    private void trackDerivationIndex(int derivationIndex) {
        if (derivationIndex > largestIndexConsumed) {
            largestIndexConsumed = derivationIndex;
        }
    }

    private String[] getBlockToRequest(int startIndex) {
        return hdWallet.fillBlock(changeIndex,
                startIndex,
                calcBufferSize());
    }

    /**
     * @return largest address index that has transactions during execution
     */
    public int getLargestIndexConsumed() {
        return largestIndexConsumed;
    }

    public int getNumberOfAddressesToFetch() {
        return numAddressesToFetch;
    }

    private List<GsonAddress> seekAhead() {
        numAddressesToFetch = lookAhead;
        numAddressesFetched = 0;
        List<GsonAddress> addresses = fetchAddresses(largestIndexConsumed + 1);
        if (addresses.size() > 0) {
            addresses.addAll(seekAhead());
        }
        return addresses;
    }

    private boolean shouldSeekAhead() {
        return numAddressesToFetch - 1 == largestIndexConsumed;
    }

    private boolean shouldLookAheadForInitialSync(int changeIndex, int start, int currentIndex, List<GsonAddress> addresses) {
        return INITIAL_GAP_LIMIT == lookAhead && changeIndex == 0 && start == 0 && currentIndex == 0 && !addresses.isEmpty();
    }
}

