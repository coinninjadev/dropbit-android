package com.coinninja.coinkeeper.ui.memo;

import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDialog;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class MemoCreatorTest {

    @Mock
    MemoCreator.OnMemoCreatedCallback callback;

    private TestableActivity activity;
    private MemoCreator memoCreator;
    private String text = "";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestableActivity.class);
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
        assertNotNull(latestAlertDialog.findViewById(R.id.memo));
    }

    @Test
    public void provides_text_to_callback() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = getAlertDialog().findViewById(R.id.memo);
        memo.setText("foo my bar");

        getAlertDialog().findViewById(R.id.done).performClick();

        verify(callback).onMemoCreated("foo my bar");
    }

    @Test
    public void no_callback_for_no_text() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = getAlertDialog().findViewById(R.id.memo);
        memo.setText("");
        getAlertDialog().findViewById(R.id.done).performClick();

        memoCreator.createMemo(activity, callback, text);
        memo = getAlertDialog().findViewById(R.id.memo);
        memo.setText("\n");
        getAlertDialog().findViewById(R.id.done).performClick();

        memoCreator.createMemo(activity, callback, text);
        memo = getAlertDialog().findViewById(R.id.memo);
        memo.setText("       ");
        getAlertDialog().findViewById(R.id.done).performClick();

        verify(callback, times(0)).onMemoCreated(anyString());
    }

    @Test
    public void dismisses_dialog_when_finished() {
        memoCreator.createMemo(activity, callback, text);
        EditText memo = getAlertDialog().findViewById(R.id.memo);
        memo.setText("foo my bar");

        getAlertDialog().findViewById(R.id.done).performClick();

        assertFalse(getAlertDialog().isShowing());
    }

    @Test
    public void populates_with_provided_text() {
        text = "some already entered text";
        memoCreator.createMemo(activity, callback, text);

        EditText memo = getAlertDialog().findViewById(R.id.memo);

        assertThat(memo, hasText(text));
        assertThat(memo.getSelectionEnd(), equalTo(text.length()));
    }

    private AlertDialog getAlertDialog() {
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }
}