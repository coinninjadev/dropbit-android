package com.coinninja.coinkeeper.ui.account;

import com.coinninja.coinkeeper.model.dto.AddressDTO;

class AddressDTOWrapper implements Comparable<AddressDTOWrapper> {

    private final AddressDTO addressDTO;
    private boolean shouldShowDerivationPath = false;

    public AddressDTOWrapper(AddressDTO addressDTO) {
        this.addressDTO = addressDTO;
    }

    public String getDisplayText() {
        if (shouldShowDerivationPath) {
            return addressDTO.getDerivationPath();
        } else {
            return addressDTO.getAddress();
        }
    }

    public void toggleShowingDerivationPath() {
        shouldShowDerivationPath = !shouldShowDerivationPath;
    }

    @Override
    public int compareTo(AddressDTOWrapper otherWrapper) {
        if (otherWrapper == null) {
            return 0;
        }

        return Integer.compare(addressDTO.getIndex(), otherWrapper.getIndex());
    }

    private int getIndex() {
        return addressDTO.getIndex();
    }

    public boolean isDerivationPath() {
        return shouldShowDerivationPath;
    }
}
