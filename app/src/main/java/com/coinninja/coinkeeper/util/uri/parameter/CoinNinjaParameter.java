package com.coinninja.coinkeeper.util.uri.parameter;

public enum CoinNinjaParameter {
    LATITUDE,
    TYPE,
    LONGITUDE;

    public String getParameterKey(){
        switch(this){
            case LATITUDE:
                return "lat";
            case TYPE:
                return "type";
            case LONGITUDE:
                return "long";
        }

        return null;
    }
}
