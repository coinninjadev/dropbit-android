package com.coinninja.coinkeeper.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class Shuffler {

    @Inject
    public Shuffler() {
    }

    public void shuffle(List items) {
        Collections.shuffle(items);
    }

    public int pick(int numChoices) {
        return new Random().nextInt(numChoices);

    }

}
