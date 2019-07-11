package com.coinninja.matchers;

import android.content.IntentFilter;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Iterator;

public class IntentFilterMatchers {

    public static final Matcher<IntentFilter> containsAction(String action) {
        return new BaseMatcher<IntentFilter>() {
            @Override
            public boolean matches(Object item) {
                if (action == null || action.isEmpty())
                    throw new NullPointerException("Provided Action Must Not Be Null");

                Iterator<String> stringIterator = ((IntentFilter) item).actionsIterator();
                while (stringIterator.hasNext())
                    if (action.equals(stringIterator.next()))
                        return true;

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected to contain the action: ").appendValue(action);

            }
        };
    }
}
