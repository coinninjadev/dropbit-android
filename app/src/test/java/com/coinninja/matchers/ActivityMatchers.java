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
import androidx.appcompat.app.AppCompatActivity;

public class ActivityMatchers {
    public static final Matcher<AppCompatActivity> hasViewWithId(int resourceId) {
        return new BaseMatcher<AppCompatActivity>() {
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

    public static Matcher<AppCompatActivity> noServiceStarted() {
        return new NoServiceStartedMatcher<AppCompatActivity>();
    }

    public static final Matcher<AppCompatActivity> serviceWithIntentStarted(@NonNull Intent intent) {
        return new ServiceWithIntentStartedMatcher<AppCompatActivity>(intent);
    }


    public static final Matcher<AppCompatActivity> activityWithIntentStarted(@NonNull Intent intent) {
        return new ActivityWItoIntentStarted<AppCompatActivity>(intent);
    }


}

