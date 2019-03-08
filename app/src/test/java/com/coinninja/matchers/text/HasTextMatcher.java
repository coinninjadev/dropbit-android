package com.coinninja.matchers.text;

import android.widget.TextView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class HasTextMatcher<T> extends BaseMatcher {
    private final String text;

    public HasTextMatcher(String text) {
        this.text = text;
    }

    @Override
    public boolean matches(Object item) {
        return text.equals(((TextView) item).getText().toString());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("\"" + text + "\"");
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("was ").appendValue(((TextView) item).getText().toString());
    }

}
