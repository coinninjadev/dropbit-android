package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.Libbitcoin;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class SeedWordGenerator {

    @Inject
    public SeedWordGenerator() {
    }

    public String[] generate() {
        return new Libbitcoin().getSeedWords();
    }

}
