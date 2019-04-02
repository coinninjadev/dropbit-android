package com.coinninja.coinkeeper.util.uri.parameter;

public enum BitcoinParameter {
    AMOUNT,
    BIP70URL,
    BIP70URLLONG;

    public String getParameterKey(){
        switch(this){
            case AMOUNT:
                return "amount";
            case BIP70URL:
                return "r";
            case BIP70URLLONG:
                return "request";
        }

        return null;
    }

}
