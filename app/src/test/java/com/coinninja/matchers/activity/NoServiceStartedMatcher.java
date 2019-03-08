package com.coinninja.matchers.activity;

import android.content.Intent;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.robolectric.shadows.ShadowActivity;

import static org.robolectric.Shadows.shadowOf;

public class NoServiceStartedMatcher<Activity> extends BaseMatcher<Activity> {
    private Intent startedService;

    @Override
    public boolean matches(Object item) {
        ShadowActivity shadowActivity = shadowOf(((android.app.Activity) item));
        startedService = shadowActivity.getNextStartedService();
        return startedService == null;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Expected no Service to be started.");
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (startedService.getAction() != null) {
            description.appendText(String.format("Service with started with Action: %s", startedService.getAction()));
        } else {
            description.appendText(String.format("Service with started with Component: %s", startedService.getComponent().getClassName()));
        }
    }
}
