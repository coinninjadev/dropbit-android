package com.coinninja.coinkeeper.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RestoreWalletPageAdapterTest {

    private static final String[] a_words = {"abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire", "across", "act", "action", "actor", "actress", "actual", "adapt", "add", "addict", "address", "adjust", "admit", "adult", "advance", "advice", "aerobic", "affair", "afford", "afraid", "again", "age", "agent", "agree", "ahead", "aim", "air", "airport", "aisle", "alarm", "album", "alcohol", "alert", "alien", "all", "alley", "allow", "almost", "alone", "alpha", "already", "also", "alter", "always", "amateur", "amazing", "among", "amount", "amused", "analyst", "anchor", "ancient", "anger", "angle", "angry", "animal", "ankle", "announce", "annual", "another", "answer", "antenna", "antique", "anxiety", "any", "apart", "apology", "appear", "apple", "approve", "april", "arch", "arctic", "area", "arena", "argue", "arm", "armed", "armor", "army", "around", "arrange", "arrest", "arrive", "arrow", "art", "artefact", "artist", "artwork", "ask", "aspect", "assault", "asset", "assist", "assume", "asthma", "athlete", "atom", "attack", "attend", "attitude", "attract", "auction", "audit", "august", "aunt", "author", "auto", "autumn", "average", "avocado", "avoid", "awake", "aware", "away", "awesome", "awful", "awkward", "axis"};
    private static final String[] ac_words = {"access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire", "across", "act", "action", "actor", "actress", "actual"};
    private static final String[] acc_words = {"access", "accident", "account", "accuse"};
    private static final String[] acce_words = {"access"};

    private ViewPager viewPager;
    private View.OnClickListener onPageBackListener;
    private View.OnClickListener onPageForwardListener;
    private RestoreWalletPageAdapter adapter;

    @Before
    public void setUp() {
        onPageBackListener = mock(View.OnClickListener.class);
        onPageForwardListener = mock(View.OnClickListener.class);
        Context context = RuntimeEnvironment.application.getApplicationContext();
        viewPager = new ViewPager(context);
        adapter = new RestoreWalletPageAdapter(12, onPageBackListener, onPageForwardListener);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    @Test
    public void selecting_recovery_word_advances_forward() {
        View page = (View) adapter.instantiateItem(viewPager, 0);
        ((EditText) page.findViewById(R.id.word)).setText("acce");

        View button = ((ViewGroup) page.findViewById(R.id.words)).getChildAt(0);
        button.performClick();

        verify(onPageForwardListener).onClick(button);
    }

    @Test
    public void invalid_word_shows_error_message() {
        View page = (View) adapter.instantiateItem(viewPager, 0);
        ((EditText) page.findViewById(R.id.word)).setText("accen");

        Button button = (Button) ((ViewGroup) page.findViewById(R.id.words)).getChildAt(0);
        button.performClick();

        assertThat(button.getVisibility(), equalTo(View.VISIBLE));
        assertThat(button.getText(),
                equalTo(viewPager.getResources().getText(R.string.restore_wallet_invalid_word)));
        verify(onPageForwardListener, times(0)).onClick(button);
    }

    @Test
    public void text_input_shows_recovery_word_options() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        ((EditText) page.findViewById(R.id.word)).setText("a");

        ViewGroup parent = page.findViewById(R.id.words);
        assertThat(((Button) parent.getChildAt(0)).getText().toString(),
                equalTo(a_words[0]));

        assertThat(((Button) parent.getChildAt(1)).getText().toString(),
                equalTo(a_words[1]));

        assertThat(((Button) parent.getChildAt(2)).getText().toString(),
                equalTo(a_words[2]));

        assertThat(((Button) parent.getChildAt(3)).getText().toString(),
                equalTo(a_words[3]));
    }

    @Test
    public void filters_words_by_users_input() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        ((EditText) page.findViewById(R.id.word)).setText("a");
        assertThat(adapter.words, equalTo(Arrays.asList(a_words)));

        ((EditText) page.findViewById(R.id.word)).setText("ac");
        assertThat(adapter.words, equalTo(Arrays.asList(ac_words)));

        ((EditText) page.findViewById(R.id.word)).setText("acc");
        assertThat(adapter.words, equalTo(Arrays.asList(acc_words)));

        ((EditText) page.findViewById(R.id.word)).setText("acce");
        assertThat(adapter.words, equalTo(Arrays.asList(acce_words)));
    }

    @Test
    public void shows_options_without_words() {
        View page = (View) adapter.instantiateItem(viewPager, 0);
        ((EditText) page.findViewById(R.id.word)).setText("acce");

        ((EditText) page.findViewById(R.id.word)).setText("acc");

        ViewGroup parent = page.findViewById(R.id.words);
        assertThat(parent.getChildAt(1).getVisibility(), equalTo(View.VISIBLE));
        assertThat(parent.getChildAt(2).getVisibility(), equalTo(View.VISIBLE));
        assertThat(parent.getChildAt(3).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void hides_options_without_words() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        ((EditText) page.findViewById(R.id.word)).setText("acce");

        ViewGroup parent = page.findViewById(R.id.words);
        assertThat(parent.getChildAt(1).getVisibility(), equalTo(View.GONE));
        assertThat(parent.getChildAt(2).getVisibility(), equalTo(View.GONE));
        assertThat(parent.getChildAt(3).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void first_page_does_not_have_back_listener() {
        View page = (View) adapter.instantiateItem(viewPager, 0);
        ((EditText) page.findViewById(R.id.word)).setText("a");

        View back = page.findViewById(R.id.back);
        assertThat(back.getVisibility(), equalTo(View.INVISIBLE));

        back.performClick();

        verify(onPageBackListener, times(0)).onClick(back);
    }

    @Test
    public void back_selection_calls_on_back_listener() {
        viewPager.setCurrentItem(1);
        View page = (View) adapter.instantiateItem(viewPager, 1);
        ((EditText) page.findViewById(R.id.word)).setText("a");

        View back = page.findViewById(R.id.back);
        assertThat(back.getVisibility(), equalTo(View.VISIBLE));

        back.performClick();

        verify(onPageBackListener).onClick(back);
    }

    @Test
    public void text_input_hides_page_indicator() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        ((EditText) page.findViewById(R.id.word)).setText("a");

        assertThat(page.findViewById(R.id.page_marker).getVisibility(), equalTo(View.INVISIBLE));
    }

    @Test
    public void resets_state_when_text_empty() {
        View page = (View) adapter.instantiateItem(viewPager, 0);
        ((EditText) page.findViewById(R.id.word)).setText("a");

        ((EditText) page.findViewById(R.id.word)).setText("");

        assertThat(page.findViewById(R.id.page_instructions).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.back).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.words).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.page_marker).getVisibility(), equalTo(View.VISIBLE));
        assertThat(adapter.words.size(), equalTo(0));
    }

    @Test
    public void text_input_shows_button_click_instructions() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        ((EditText) page.findViewById(R.id.word)).setText("a");

        assertThat(page.findViewById(R.id.page_instructions).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void shows_page_marker() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        TextView pageMarker = page.findViewById(R.id.page_marker);
        assertThat(pageMarker.getText().toString(), equalTo("word 1 of 12"));
        assertThat(pageMarker.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void inflates_page_from_layout() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        assertThat(page.findViewById(R.id.page_instructions).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.back).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.words).getVisibility(), equalTo(View.INVISIBLE));
        assertThat(page.findViewById(R.id.page_marker).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void count_returns_number_of_words() {
        assertThat(adapter.getCount(), equalTo(12));

    }

    @Test
    public void set_focus_on_recovery_word_entry() {
        View page = (View) adapter.instantiateItem(viewPager, 0);

        assertTrue(page.findViewById(R.id.word).isFocused());
    }

}