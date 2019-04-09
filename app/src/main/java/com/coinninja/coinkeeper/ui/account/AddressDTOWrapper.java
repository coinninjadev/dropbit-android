package com.coinninja.coinkeeper.ui.account;

import com.coinninja.coinkeeper.model.dto.AddressDTO;

class AddressDTOWrapper implements Comparable<AddressDTOWrapper> {

    private final AddressDTO addressDTO;
    private boolean shouldShowDerivationPath = false;

    public AddressDTOWrapper(AddressDTO addressDTO) {
       this.addressDTO = addressDTO;
    }

    public String getDisplayText(){
        if (shouldShowDerivationPath) {
            return addressDTO.getDerivationPathString();
        } else {
            return addressDTO.getWrappedAddress().getAddress();
        }
    }

    public void toggleShowingDerivationPath() {
        shouldShowDerivationPath = !shouldShowDerivationPath;
    }

    @Override
    public int compareTo(AddressDTOWrapper otherWrapper) {
        if (otherWrapper == null) { return 0; }

        if (this.addressDTO.getWrappedAddress().getIndex() < otherWrapper.addressDTO.getWrappedAddress().getIndex()) {
            return -1;
        } else if (this.addressDTO.getWrappedAddress().getIndex() > otherWrapper.addressDTO.getWrappedAddress().getIndex()) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isDerivationPath() {
        return shouldShowDerivationPath;
    }
}
