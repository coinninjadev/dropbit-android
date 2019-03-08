package com.coinninja.coinkeeper.res;

import com.coinninja.coinkeeper.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class RecoveryWordListTest {


    private List<String> recoveryWords;

    @Before
    public void setUp() {
        recoveryWords = Arrays.asList(RuntimeEnvironment.application.getResources().getStringArray(R.array.recovery_words));
    }

    @Test
    public void false_is_a_valid_word() {
        assertTrue(recoveryWords.indexOf("false") > 0);
    }

    @Test
    public void true_is_a_valid_word() {
        assertTrue(recoveryWords.indexOf("true") > 0);
    }
}
