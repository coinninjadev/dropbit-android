package com.coinninja.matchers;

import android.app.Activity;
import android.content.Intent;

import com.coinninja.matchers.activity.ActivityWItoIntentStarted;
import com.coinninja.matchers.activity.NoServiceStartedMatcher;
import com.coinninja.matchers.activity.ServiceWithIntentStartedMatcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import androidx.annotation.NonNull;

public class ActivityMatchers {
    public static final Matcher<Activity> hasViewWithId(int resourceId) {
        return new BaseMatcher<Activity>() {
            @Override
            public boolean matches(Object item) {
                return ((Activity) item).findViewById(resourceId) != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should have view with id of:").appendValue(resourceId);

            }
        };
    }

    public static Matcher<Activity> noServiceStarted() {
        return new NoServiceStartedMatcher<Activity>();
    }

    public static final Matcher<Activity> serviceWithIntentStarted(@NonNull Intent intent) {
        return new ServiceWithIntentStartedMatcher<Activity>(intent);
    }


    public static final Matcher<Activity> activityWithIntentStarted(@NonNull Intent intent) {
        return new ActivityWItoIntentStarted<Activity>(intent);
    }


}

