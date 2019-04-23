package com.coinninja.matchers.activity;

import android.content.Intent;

import com.coinninja.matchers.IntentMatcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.robolectric.shadows.ShadowActivity;

import static org.robolectric.Shadows.shadowOf;

public class ActivityWItoIntentStarted<Activity> extends BaseMatcher<Activity> {
    private final Intent intent;
    private android.app.Activity activity;
    private String expected = "";
    private String describeReason = "";
    private IntentMatcher intentMatcher;

    public ActivityWItoIntentStarted(Intent intent) {
        this.intent = intent;
    }

    @Override
    public boolean matches(Object item) {
        activity = ((android.app.Activity) item);
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedActivity = shadowActivity.getNextStartedActivity();

        if (startedActivity == null) {
            describeReason = "No activity was started";
            if (intent.getComponent() != null)
                expected = String.format("expected activity with component name of: %s", intent.getComponent().getClassName());

            if (intent.getAction() != null)
                expected = String.format("expected activity with action: %s", intent.getAction());

            return false;
        }

        intentMatcher = new IntentMatcher(intent);
        return intentMatcher.matches(startedActivity);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected);
        if (intentMatcher != null) {
            intentMatcher.describeTo(description);
        }
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (!describeReason.isEmpty())
            description.appendValue(describeReason);

        if (intentMatcher != null) {
            intentMatcher.describeMismatch(item, description);
        }
    }
}
