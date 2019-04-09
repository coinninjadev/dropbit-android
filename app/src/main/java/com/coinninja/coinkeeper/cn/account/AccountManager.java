package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class AccountManager {

    private final AddressCache addressCache;
    private final AddressHelper addressHelper;
    private final HDWallet hdWallet;
    private WalletHelper walletHelper;

    @Inject
    AccountManager(HDWallet hdWallet, WalletHelper walletHelper, AddressCache addressCache, AddressHelper addressHelper) {
        this.hdWallet = hdWallet;
        this.walletHelper = walletHelper;
        this.addressCache = addressCache;
        this.addressHelper = addressHelper;
    }

    public String getNextReceiveAddress() {
        List<Address> addresses = addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL);
        return addresses.size() > 0 ? addresses.get(0).getAddress() : hdWallet.getExternalAddress(0);
    }

    public int getNextReceiveIndex() {
        List<Address> addresses = addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL);
        return addresses.size() > 0 ? addresses.get(0).getIndex() : 0;
    }

    public int getNextChangeIndex() {
        List<Address> addresses = addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL);
        return addresses.size() > 0 ? addresses.get(0).getIndex() : 0;
    }

    //TODO can find the largest consumed from DB and use that as a reference point
    @Deprecated
    public void reportLargestReceiveIndexConsumed(int index) {
        if (walletHelper.getCurrentExternalIndex() <= index) {
            walletHelper.setExternalIndex(index + 1);
        }
    }

    //TODO can find the largest consumed from DB and use that as a reference point
    @Deprecated
    public void reportLargestChangeIndexConsumed(int index) {
        if (walletHelper.getCurrentInternalIndex() <= index) {
            walletHelper.setInternalIndex(index + 1);
        }
    }

    public void cacheAddresses() {
        addressCache.cacheAddressesFor(HDWallet.EXTERNAL);
        addressCache.cacheAddressesFor(HDWallet.INTERNAL);
    }

    public HashMap<String, AddressDTO> unusedAddressesToPubKey(int chainIndex, int blockSize) {
        List<Address> unusedAddresses = addressHelper.getUnusedAddressesFor(chainIndex);
        HashMap<String, AddressDTO> addressToDTO = new HashMap<String, AddressDTO>();

        blockSize = blockSize <= unusedAddresses.size() ? blockSize : unusedAddresses.size();

        for (int i = 0; i < blockSize; i++) {
            Address address = unusedAddresses.get(i);
            String uncompressedPubKey = addressCache.getUncompressedPublicKey(address.getDerivationPath());
            addressToDTO.put(address.getAddress(), new AddressDTO(address, uncompressedPubKey));
        }

        return addressToDTO;
    }

    public int getLargestReportedChangeAddress() {
        return addressHelper.getLargestDerivationIndexReportedFor(HDWallet.INTERNAL);
    }

    public int getLargestReportedReceiveAddress() {
        return addressHelper.getLargestDerivationIndexReportedFor(HDWallet.EXTERNAL);
    }
}
