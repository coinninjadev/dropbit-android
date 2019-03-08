package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.AnalyticUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BaseDialogFragmentTest {
    private AnalyticUtil analyticUtil;
    private FragmentController<BaseDialogFragment> fragmentController;
    private BaseDialogFragment fragment;

    @Before
    public void setUp() {
        analyticUtil = mock(AnalyticUtil.class);
        fragmentController = Robolectric.buildFragment(BaseDialogFragment.class);
        fragment = fragmentController.get();
        fragmentController.create();
        fragment.analytics = analyticUtil;
        fragmentController.start().resume().visible();
    }

    @Test
    public void notifies_analytics_that_fragment_stopped() {
        fragmentController.pause().stop().destroy();

        verify(analyticUtil).trackFragmentStop(fragment);
    }
}