package com.coinninja.coinkeeper.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.text.TextInputNotifierWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class RestoreWalletPageAdapter extends PagerAdapter implements TextInputNotifierWatcher.OnInputEventListener {
    private final int numPages;
    private final View.OnClickListener onPageBackListener;
    private final View.OnClickListener onPageForwardListener;
    private View page;
    private ViewPager parent;
    private List<String> possiblities;
    List<String> words = new ArrayList<>();
    private TextInputNotifierWatcher watcher;

    public RestoreWalletPageAdapter(int numPages,
                                    View.OnClickListener onPageBackListener,
                                    View.OnClickListener onPageForwardListener) {
        this.numPages = numPages;
        this.onPageBackListener = onPageBackListener;
        this.onPageForwardListener = onPageForwardListener;
        watcher = new TextInputNotifierWatcher(this);
    }

    @Override
    public int getCount() {
        return numPages;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        parent = (ViewPager) container;
        View page = LayoutInflater.from(context).inflate(R.layout.restore_wallet_word_page, null);
        possiblities = Arrays.asList(context.getResources().getStringArray(R.array.recovery_words));
        this.page = page;
        page.setTag("TAG-" + String.valueOf(position));
        setup(position);
        container.addView(page);
        return page;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onAfterChanged(String input) {
        page = parent.findViewWithTag("TAG-" + String.valueOf(parent.getCurrentItem()));
        if (input.isEmpty()) {
            resetPage();
        } else {
            showOptionsFor(input.toLowerCase());
        }
    }

    private void setup(int position) {
        setVisibility();
        bindPageMarker(position);
    }

    private void bindPageMarker(int position) {
        String text = getString(R.string.restore_wallet_enter_x_of_12)
                .replace(getString(R.string.word_mask),
                        String.valueOf(position + 1));
        ((TextView) page.findViewById(R.id.page_marker)).setText(text);
    }

    @NonNull
    private String getString(int which) {
        return page.getResources().getString(which);
    }

    private void setVisibility() {
        resetPage();
    }

    private void resetPage() {
        page.findViewById(R.id.page_instructions).setVisibility(View.INVISIBLE);
        page.findViewById(R.id.words).setVisibility(View.INVISIBLE);
        page.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        page.findViewById(R.id.page_marker).setVisibility(View.VISIBLE);
        EditText input = page.findViewById(R.id.word);
        input.setFocusable(true);
        input.requestFocus();
        showKeyboard(input);
        input.removeTextChangedListener(watcher);
        input.setText("");
        input.addTextChangedListener(watcher);
        words.clear();
    }

    private void showOptionsFor(String input) {
        page.findViewById(R.id.page_instructions).setVisibility(View.VISIBLE);
        page.findViewById(R.id.words).setVisibility(View.VISIBLE);
        page.findViewById(R.id.page_marker).setVisibility(View.INVISIBLE);

        if (parent.getCurrentItem() > 0) {
            View back = page.findViewById(R.id.back);
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(onPageBackListener);
        }

        showPossibleWordsFor(input);
    }

    private void showPossibleWordsFor(String input) {
        createIntialWordListFrom(input);
        bindPossibleWords();
    }

    private void bindPossibleWords() {
        ViewGroup parent = page.findViewById(R.id.words);
        for (int i = 0; i < parent.getChildCount(); i++) {
            Button button = (Button) parent.getChildAt(i);
            try {
                button.setText(words.get(i));
                button.setBackground(page.getResources().getDrawable(R.drawable.primary_button));
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(onPageForwardListener);
            } catch (IndexOutOfBoundsException e) {
                button.setVisibility(View.GONE);
                button.setText("");
            }
        }

        if (words.size() == 0) {
            Button button = (Button) parent.getChildAt(0);
            button.setText(page.getResources().getString(R.string.restore_wallet_invalid_word));
            button.setBackground(page.getResources().getDrawable(R.drawable.error_button));
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(null);
        }
    }

    private void createIntialWordListFrom(String input) {
        words.clear();
        for (String word : possiblities) {
            if (null != word && word.startsWith(input)) {
                words.add(word);
            }
        }
    }

    @Override
    public void onInput(int numValues) {
        // n/a
    }

    @Override
    public void onRemove(int numValues) {
        // n/a
    }

    public void resetState() {
        page = parent.findViewWithTag("TAG-" + String.valueOf(parent.getCurrentItem()));
        if (page != null) {
            resetPage();
        }

    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
