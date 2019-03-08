package com.coinninja.matchers;

import android.view.View;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ViewMatcher {
    public static final Matcher<View> hasTag(String expectedTag) {
        return new BaseMatcher<View>() {
            private String actual = "";
            private Object tag;

            @Override
            public boolean matches(Object item) {
                View view = (View) item;
                tag = view.getTag();
                if (tag == null) {
                    actual = " Tag was null";
                    return false;
                }

                return expectedTag.equals(tag);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected view to have tag: ").appendValue(expectedTag);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendValue(actual);
            }
        };
    }

    public static final Matcher<View> hasTag(int expectedTag) {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                View view = (View) item;
                Object tag = view.getTag();

                if (tag == null)
                    return false;

                return expectedTag == (int) tag;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected view to have tag: ").appendValue(expectedTag);
            }
        };
    }

    public static final Matcher<View> isGone() {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                View view = (View) item;

                return view.getVisibility() == View.GONE;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected view to be GONE");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                switch (((View) item).getVisibility()) {
                    case View.VISIBLE:
                        description.appendText("was VISIBLE");
                        break;
                    case View.GONE:
                        description.appendText("was GONE");
                        break;
                    case View.INVISIBLE:
                        description.appendText("was INVISIBLE");
                        break;
                }
            }
        };
    }

    public static final Matcher<View> isInvisible() {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                View view = (View) item;

                return view.getVisibility() == View.INVISIBLE;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected view to be INVISIBLE");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                switch (((View) item).getVisibility()) {
                    case View.VISIBLE:
                        description.appendText("was VISIBLE");
                        break;
                    case View.GONE:
                        description.appendText("was GONE");
                        break;
                    case View.INVISIBLE:
                        description.appendText("was INVISIBLE");
                        break;
                }
            }
        };
    }

    public static final Matcher<View> isVisible() {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                View view = (View) item;

                return view.getVisibility() == View.VISIBLE;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected view to be VISIBLE");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                switch (((View) item).getVisibility()) {
                    case View.VISIBLE:
                        description.appendText("was VISIBLE");
                        break;
                    case View.GONE:
                        description.appendText("was GONE");
                        break;
                    case View.INVISIBLE:
                        description.appendText("was INVISIBLE");
                        break;
                }
            }
        };
    }
}
