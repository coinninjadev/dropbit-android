package com.coinninja.coinkeeper.ui.memo;

import android.app.AlertDialog;
import android.widget.EditText;

import com.coinninja.coinkeeper.DumbActivity;
import com.coinninja.coinkeeper.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class MemoCreatorTest {

    @Mock
    MemoCreator.OnMemoCreatedCallback callback;

    private DumbActivity activity;
    private MemoCreator memoCreator;
    String text = "";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(DumbActivity.class);
        memoCreator = new MemoCreator();
    }

    @After
    public void tearDown() {
        activity = null;
        callback = null;
        memoCreator = null;
    }

    @Test
    public void shows_dialog_when_create_called() {
        memoCreator.createMemo(activity, callback, text);
        AlertDialog latestAlertDialog = getAlertDialog();
        assertNotNull(latestAlertDialog);
        assertNotNull(withId(latestAlertDialog, R.id.memo));
    }

    @Test
    public void provides_text_to_callback() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = withId(getAlertDialog(), R.id.memo);
        memo.setText("foo my bar");

        withId(getAlertDialog(), R.id.done).performClick();

        verify(callback).onMemoCreated("foo my bar");
    }

    @Test
    public void no_callback_for_no_text() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = withId(getAlertDialog(), R.id.memo);
        memo.setText("");
        withId(getAlertDialog(), R.id.done).performClick();

        memoCreator.createMemo(activity, callback, text);
        memo = withId(getAlertDialog(), R.id.memo);
        memo.setText("\n");
        withId(getAlertDialog(), R.id.done).performClick();

        memoCreator.createMemo(activity, callback, text);
        memo = withId(getAlertDialog(), R.id.memo);
        memo.setText("       ");
        withId(getAlertDialog(), R.id.done).performClick();

        verify(callback, times(0)).onMemoCreated(anyString());
    }

    @Test
    public void dismisses_dialog_when_finished() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = withId(getAlertDialog(), R.id.memo);
        memo.setText("foo my bar");

        withId(getAlertDialog(), R.id.done).performClick();

        assertFalse(getAlertDialog().isShowing());
    }

    @Test
    public void populates_with_provided_text() {
        text = "some already entered text";
        memoCreator.createMemo(activity, callback, text);

        EditText memo = withId(getAlertDialog(), R.id.memo);

        assertThat(memo, hasText(text));
        assertThat(memo.getSelectionEnd(), equalTo(text.length()));
    }

    private AlertDialog getAlertDialog() {
        return ShadowAlertDialog.getLatestAlertDialog();
    }
}