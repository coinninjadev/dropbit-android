package com.coinninja.matchers;

import com.coinninja.coinkeeper.view.ConfirmationsView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ConfirmationViewMatcher {

    public static final Matcher<ConfirmationsView> configuredForDropbit() {
        return new BaseMatcher<ConfirmationsView>() {
            @Override
            public boolean matches(Object item) {
                ConfirmationsView confirmationsView = (ConfirmationsView) item;
                return confirmationsView.getConfiguration() == ConfirmationsView.CONFIGURATION_DROPBIT;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected configuration: ").appendValue("CONFIGURATION_DROPBIT");
            }
        };
    }

    public static final Matcher<ConfirmationsView> configuredForTransaction() {
        return new BaseMatcher<ConfirmationsView>() {
            @Override
            public boolean matches(Object item) {
                ConfirmationsView confirmationsView = (ConfirmationsView) item;
                return confirmationsView.getConfiguration() == ConfirmationsView.CONFIGURATION_TRANSACTION;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected configuration: ").appendValue("CONFIGURATION_TRANSACTION");
            }
        };
    }

    public static final Matcher<ConfirmationsView> stageIs(int stage) {
        return new BaseMatcher<ConfirmationsView>() {
            @Override
            public boolean matches(Object item) {
                ConfirmationsView confirmationsView = (ConfirmationsView) item;
                return confirmationsView.getStage() == stage;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected stage: ").appendValue(stage);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(((ConfirmationsView) item).getStage());
            }
        };
    }
}
