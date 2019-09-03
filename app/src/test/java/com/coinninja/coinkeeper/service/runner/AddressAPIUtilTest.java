package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperClient;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.wallet.data.TestData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;

import static com.coinninja.coinkeeper.service.client.CoinKeeperClient.ADDRESSES_TO_QUERY_AT_A_TIME;
import static com.coinninja.coinkeeper.service.runner.AddressAPIUtil.INITIAL_GAP_LIMIT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class AddressAPIUtilTest {

    private static final int DEFAULT_PAGE = 1;
    @InjectMocks
    private AddressAPIUtil addressAPIUtil;

    @Mock
    private CoinKeeperApiClient apiClient;
    @Mock
    private HDWallet hdWallet;
    private String[] block1;
    private String[] block2;
    private String[] block3;
    private String[] block4;

    @Before
    public void setUp() {
        addressAPIUtil.setLookAhead(AddressAPIUtil.LOOK_AHEAD);
    }

    @Test
    public void calculates_number_of_addresses_to_fetch() {
        // indexes are 0 based so the 27th index really means fetch 28 addresses
        mockIndex22();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        assertThat(addressAPIUtil.getNumberOfAddressesToFetch(), equalTo(27));
    }

    @Test
    public void limits_blocks_by_API_query_limit() {
        mockIndex22();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        verify(hdWallet).fillBlock(HDWallet.EXTERNAL, 0, ADDRESSES_TO_QUERY_AT_A_TIME);
    }

    @Test
    public void only_requests_addresses_that_are_needed__small_index() {
        mockIndex2();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 2);

        verify(hdWallet).fillBlock( HDWallet.EXTERNAL, 0, 7);
    }

    @Test
    public void requests_address_information_from_API() {
        mockIndex22();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        verify(apiClient).queryAddressesFor(block1, DEFAULT_PAGE);
    }

    @Test
    public void requests_all_of_necessary_address_indexes_recursively() {
        mockIndex22();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        verify(hdWallet).fillBlock( HDWallet.EXTERNAL, 0, ADDRESSES_TO_QUERY_AT_A_TIME);
        verify(hdWallet).fillBlock( HDWallet.EXTERNAL, 25, 2);

        verify(apiClient).queryAddressesFor(block1, DEFAULT_PAGE);
        verify(apiClient).queryAddressesFor(block2, DEFAULT_PAGE);
    }


    @Test
    public void will_look_ahead_when_partial_returned__LOOK_AHEAD_ANY() {
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, INITIAL_GAP_LIMIT);
        block2 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 5, 25);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 20)).thenReturn(block1);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 5, 20)).thenReturn(block2);

        when(apiClient.queryAddressesFor(block1, DEFAULT_PAGE)).thenReturn(buildResponse(block1, 5));
        when(apiClient.queryAddressesFor(block2, DEFAULT_PAGE)).thenReturn(buildResponse(block2, 0));

        addressAPIUtil.setLookAhead(INITIAL_GAP_LIMIT);


        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 0);

        verify(hdWallet).fillBlock( HDWallet.EXTERNAL,5, 20);
        verify(apiClient).queryAddressesFor(block2, DEFAULT_PAGE);
    }

    @Test
    public void looks_ahead_when_full_return() {
        mockIndex22AllAddressesUsed();
        mockLookAhead();

        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        verify(hdWallet).fillBlock( HDWallet.EXTERNAL, 27, 5);
        verify(apiClient).queryAddressesFor(block3, DEFAULT_PAGE);
    }

    @Test
    public void sets_deravation_path_on_addresses_returned_from_CN() {
        mockIndex2();

        List<GsonAddress> addresses = addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 2);

        assertThat(addresses.size(), equalTo(2));
        assertThat(addresses.get(0).getDerivationIndex(), equalTo(0));
        assertThat(addresses.get(1).getDerivationIndex(), equalTo(1));
    }

    @Test
    public void findsIndex() {
        mockIndex0();
        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 0);
        assertThat(addressAPIUtil.getLargestIndexConsumed(), equalTo(0));

        mockIndex2();
        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 2);
        assertThat(addressAPIUtil.getLargestIndexConsumed(), equalTo(1));

        mockIndex22();
        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);
        assertThat(addressAPIUtil.getLargestIndexConsumed(), equalTo(24));

        mockIndex22AllAddressesUsed();
        mockLookAhead();
        addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);
        assertThat(addressAPIUtil.getLargestIndexConsumed(), equalTo(26));
    }

    @Test
    public void requests_next_pages_for_fetching_addresses() {
        mockIndex22with3Pages();

        List<GsonAddress> addresses = addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 22);

        verify(apiClient).queryAddressesFor(block1, 1);
        verify(apiClient).queryAddressesFor(block1, 2);
        verify(apiClient).queryAddressesFor(block1, 3);
        verify(apiClient).queryAddressesFor(block2, 1);
        assertThat(addresses.size(), equalTo(202));
    }

    @Test
    public void look_ahead_will_recurse_unitl_empty_block() {
        mockPatchyWallet();

        List<GsonAddress> addresses = addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL,
                0, 0);

        verify(apiClient).queryAddressesFor(block1, 1);
        verify(apiClient).queryAddressesFor(block2, 1);
        verify(apiClient).queryAddressesFor(block3, 1);
        verify(apiClient).queryAddressesFor(block4, 1);
        assertThat(addresses.size(), equalTo(7));
    }

    private void mockPatchyWallet() {
        /*
         * Address lookup 1 [0, 1, 2, 3, 4]
         *    -> Response: addr[0, 1, 2, 3, 4] has Transactions
         * Address lookup 2 [5, 6, 7, 8, 9]
         *    -> Response: addr[3] has Transactions
         * Address lookup 3 [9, 10, 11, 12, 13]
         *    -> Response: addr[2] has Transactions
         * Address lookup 4 [12, 13, 14, 15, 16]
         *    -> Response: no transactions returned
         */
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, 5);
        block2 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 5, 10);
        block3 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 9, 14);
        block4 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 12, 17);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 5)).thenReturn(block1);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 5, 5)).thenReturn(block2);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 9, 5)).thenReturn(block3);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 12, 5)).thenReturn(block4);

        when(apiClient.queryAddressesFor(block1, 1)).thenReturn(buildResponse(block1, block1.length));

        List<GsonAddress> b2data = new ArrayList<>();
        GsonAddress result = new GsonAddress();
        result.setAddress(block2[3]);
        b2data.add(result);
        Response b2Response = Response.success(b2data);
        when(apiClient.queryAddressesFor(block2, 1)).thenReturn(b2Response);

        List<GsonAddress> b3data = new ArrayList<>();
        GsonAddress result3 = new GsonAddress();
        result3.setAddress(block3[2]);
        b3data.add(result3);
        Response b3Response = Response.success(b3data);
        when(apiClient.queryAddressesFor(block3, 1)).thenReturn(b3Response);

        when(apiClient.queryAddressesFor(block4, 1)).thenReturn(buildResponse(block4, 0));
    }

    private void mockIndex22with3Pages() {
        mockLookAhead();
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, ADDRESSES_TO_QUERY_AT_A_TIME);
        block2 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), ADDRESSES_TO_QUERY_AT_A_TIME, 27);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 25)).thenReturn(block1);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 25, 2)).thenReturn(block2);

        when(apiClient.queryAddressesFor(block1, 1)).thenReturn(buildResponse(block1, block1.length,
                true));
        when(apiClient.queryAddressesFor(block1, 2)).thenReturn(buildResponse(block1, block1.length,
                true));
        when(apiClient.queryAddressesFor(block1, 3)).thenReturn(buildResponse(block1, 0));

        when(apiClient.queryAddressesFor(block2, DEFAULT_PAGE)).thenReturn(buildResponse(block2, 2));
    }

    private void mockLookAhead() {
        block3 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 27, 27 + AddressAPIUtil.LOOK_AHEAD);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 27, AddressAPIUtil.LOOK_AHEAD)).thenReturn(block3);
        when(apiClient.queryAddressesFor(block3, DEFAULT_PAGE)).thenReturn(buildResponse(block3, 0));
    }

    private void mockIndex0() {
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, AddressAPIUtil.LOOK_AHEAD);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 5)).thenReturn(block1);

        when(apiClient.queryAddressesFor(block1, DEFAULT_PAGE)).thenReturn(buildResponse(block1, 0));
    }

    private void mockIndex2() {
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, 7);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 7)).thenReturn(block1);

        when(apiClient.queryAddressesFor(block1, DEFAULT_PAGE)).thenReturn(buildResponse(block1, 2));
    }

    private void mockIndex22() {
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, ADDRESSES_TO_QUERY_AT_A_TIME);
        block2 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), ADDRESSES_TO_QUERY_AT_A_TIME, 27);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 0, 25)).thenReturn(block1);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 25, 2)).thenReturn(block2);

        when(apiClient.queryAddressesFor(block1, DEFAULT_PAGE)).thenReturn(buildResponse(block1, block1.length));
        when(apiClient.queryAddressesFor(block2, DEFAULT_PAGE)).thenReturn(buildResponse(block2, 0));
    }

    private void mockIndex22AllAddressesUsed() {
        block1 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, ADDRESSES_TO_QUERY_AT_A_TIME);
        block2 = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), ADDRESSES_TO_QUERY_AT_A_TIME, 27);

        when(hdWallet.fillBlock( HDWallet.EXTERNAL,0, 25)).thenReturn(block1);
        when(hdWallet.fillBlock( HDWallet.EXTERNAL, 25, 2)).thenReturn(block2);

        when(apiClient.queryAddressesFor(block1, DEFAULT_PAGE)).thenReturn(buildResponse(block1, block1.length));
        when(apiClient.queryAddressesFor(block2, DEFAULT_PAGE)).thenReturn(buildResponse(block2, block2.length));
    }

    private Response buildResponse(String[] block, int numAddressesReturned, boolean fullPage) {
        List<GsonAddress> addressses = new ArrayList<>();
        int cycles = fullPage ? CoinKeeperClient.ADDRESSES_RESULT_LIMIT : numAddressesReturned;

        for (int i = 0; i < cycles; i++) {
            GsonAddress address = new GsonAddress();
            address.setAddress(block[i % numAddressesReturned]);
            addressses.add(address);
        }


        return Response.success(addressses);
    }

    private Response buildResponse(String[] block, int numAddressesReturned) {
        List<GsonAddress> addressses = new ArrayList<>();

        for (int i = 0; i < numAddressesReturned; i++) {
            GsonAddress address = new GsonAddress();
            address.setAddress(block[i]);
            addressses.add(address);
        }

        return Response.success(addressses);
    }

}
