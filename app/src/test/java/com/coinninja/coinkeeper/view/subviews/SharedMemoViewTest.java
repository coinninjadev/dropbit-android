package com.coinninja.coinkeeper.view.subviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SharedMemoViewTest {

    private View view;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        view = LayoutInflater.from(context).inflate(R.layout.shared_memo_container, null);

    }

    @Test
    public void test_configure_for_sharing__renders_shared_icon() {
        SharedMemoView sharedMemoView = new SharedMemoView(view, true, "some memo", "Jon Doe");

        ImageView imageView = withId(view, R.id.shared_status_image_view);
        assertThat(imageView, hasTag(R.drawable.ic_shared_user));
    }

    @Test
    public void sets_memo_status_text() {
        SharedMemoView sharedMemoView = new SharedMemoView(view, true, "some memo", "Jon Doe");

        TextView textView = withId(view, R.id.shared_memo_status_text_view);

        assertThat(textView, hasText(Resources.getString(context, R.string.shared_memo, "Jon Doe")));
    }
}