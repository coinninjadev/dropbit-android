package com.coinninja.matchers;

import android.widget.TextView;

import com.coinninja.matchers.text.HasTextMatcher;

import org.hamcrest.Matcher;

public class TextViewMatcher {
    public static Matcher<TextView> hasText(String text) {
        return new HasTextMatcher<>(text);
    }

}
