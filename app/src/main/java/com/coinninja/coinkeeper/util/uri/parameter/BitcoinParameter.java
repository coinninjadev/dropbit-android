package com.coinninja.coinkeeper.util.uri.parameter;

public enum BitcoinParameter {
    AMOUNT,
    BIP70URL;

    public String getParameterKey(){
        switch(this){
            case AMOUNT:
                return "amount";
            case BIP70URL:
                return "r";
        }

        return null;
    }
}
