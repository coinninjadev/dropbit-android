package com.coinninja.coinkeeper.view.subviews;

import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.memo.MemoCreator;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SharedMemoToggleViewTest {

    @Mock
    ActivityNavigationUtil activityNavigationUtil;
    @Mock
    MemoCreator memoCreator;
    private SharedMemoToggleView sharedMemoToggleView;
    private TestableActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.memo_container);
        sharedMemoToggleView = new SharedMemoToggleView(activityNavigationUtil, memoCreator);
        sharedMemoToggleView.render(activity, withId(activity, R.id.memo_container));
    }

    @Test
    public void hides_shared_memo_views() {
        sharedMemoToggleView.showSharedMemoViews();
        sharedMemoToggleView.hideSharedMemoViews();

        View sharedMemoGroup = withId(activity, R.id.shared_memo_group);

        assertThat(sharedMemoGroup, isGone());
        assertThat(withId(activity, R.id.unshare_memo_toggle_button), isGone());
        assertThat(withId(activity, R.id.shared_memo_toggle_button), isGone());
        assertFalse(sharedMemoToggleView.isSharing());
    }

    @Test
    public void inits_with_sharing_hidden() {
        View sharedMemoGroup = withId(activity, R.id.shared_memo_group);
        View unShare = withId(activity, R.id.unshare_memo_toggle_button);
        View share = withId(activity, R.id.shared_memo_toggle_button);

        assertThat(sharedMemoGroup, isVisible());
        assertThat(share, isVisible());
        assertThat(unShare, isGone());
        assertTrue(sharedMemoToggleView.isSharing());
    }

    @Test
    public void shows_shared_memo_views() {
        sharedMemoToggleView.hideSharedMemoViews();
        sharedMemoToggleView.showSharedMemoViews();

        View sharedMemoGroup = withId(activity, R.id.shared_memo_group);
        View unShare = withId(activity, R.id.unshare_memo_toggle_button);
        View share = withId(activity, R.id.shared_memo_toggle_button);

        assertThat(sharedMemoGroup, isVisible());
        assertThat(unShare, isGone());
        assertThat(share, isVisible());
        assertTrue(sharedMemoToggleView.isSharing());
    }

    @Test
    public void provides_memo_from_view() {
        TextView memo = withId(activity, R.id.memo_text_view);

        memo.setText("foo");

        assertThat(sharedMemoToggleView.getMemo(), equalTo("foo"));
    }

    @Test
    public void toggles_sharing_when_background_pressed() {
        sharedMemoToggleView.showSharedMemoViews();
        assertTrue(sharedMemoToggleView.isSharing());

        withId(activity, R.id.memo_background_view).performClick();

        assertFalse(sharedMemoToggleView.isSharing());
    }

    @Test
    public void toggles_shared() {
        sharedMemoToggleView.showSharedMemoViews();
        View unShare = withId(activity, R.id.unshare_memo_toggle_button);
        View share = withId(activity, R.id.shared_memo_toggle_button);
        assertTrue(sharedMemoToggleView.isSharing());
        assertThat(unShare, isGone());
        assertThat(share, isVisible());

        sharedMemoToggleView.toggleSharingMemo();
        assertFalse(sharedMemoToggleView.isSharing());
        assertThat(share, isGone());
        assertThat(unShare, isVisible());

        sharedMemoToggleView.toggleSharingMemo();
        assertTrue(sharedMemoToggleView.isSharing());
        assertThat(unShare, isGone());
        assertThat(share, isVisible());
    }

    @Test
    public void tooltip_explains_shared_memos() {
        withId(activity, R.id.shared_memo_tooltip_button).performClick();

        verify(activityNavigationUtil).explainSharedMemos(activity);
    }

    @Test
    public void clicking_on_memo_creates_allows_user_to_input() {
        TextView memoTextView = withId(activity, R.id.memo_text_view);
        memoTextView.setText("memo");

        memoTextView.performClick();

        verify(memoCreator).createMemo(eq(activity), any(MemoCreator.OnMemoCreatedCallback.class), eq("memo"));
    }

    @Test
    public void sets_text_from_memo_creator_when_callback_triggered() {
        String text = "I am a memo";
        sharedMemoToggleView.onMemoCreated(text);

        TextView memoView = withId(activity, R.id.memo_text_view);
        assertThat(memoView, hasText(text));
    }
}