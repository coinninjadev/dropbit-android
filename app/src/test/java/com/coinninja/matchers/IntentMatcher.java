package com.coinninja.matchers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class IntentMatcher extends BaseMatcher {

    private final Intent intent;
    private Intent actual;
    private String describeReason = "";
    private String expected = "";

    public static IntentMatcher equalTo(Intent intent) {
        return new IntentMatcher(intent);
    }

    public IntentMatcher(Intent intent) {
        this.intent = intent;
    }

    @Override
    public boolean matches(Object item) {
        actual = (Intent) item;

        if (intent.getAction() != null && !intent.getAction().equals(actual.getAction())) {
            expected = String.format("Expected action: %s", intent.getAction());
            describeReason = String.format("Received action: %s", actual.getAction());
            return false;
        }

        if (intent.getFlags() != actual.getFlags()) {
            describeReason = String.format("Expected Intent Flags: %s", intent.getFlags());
            expected = String.format("Actual Intent Flags: %s", actual.getFlags());
            return false;
        }

        if (intent.getCategories() != null) {
            expected = "Matcher does not support categories";
            describeReason = "Please Implement";
            return false;
        }

        if (intent.getType() != null && !intent.getType().equals(actual.getType())) {
            expected = String.format("Expected intent.getType() of: %s", intent.getType());
            describeReason = String.format("Expected intent.getType of: %s", actual.getType());
            return false;
        }

        if (intent.getComponent() != null && actual.getComponent() != null) {
            if (!intent.getComponent().getClassName().equals(actual.getComponent().getClassName())) {
                expected = String.format("Expected with component: %s", intent.getComponent().getClassName());
                describeReason = String.format("Actual supplied component: %s", actual.getComponent().getClassName());
                return false;
            }
        } else if (intent.getComponent() != null && actual.getComponent() == null) {
            expected = String.format("Expected with component: %s", intent.getComponent().getClassName());
            describeReason = "Actual is Null";
            return false;
        }

        if (intent.getData() != null && !intent.getDataString().equals(actual.getDataString())) {
            expected = String.format("Expected Data Uri: %s", intent.getDataString());
            describeReason = String.format("Actual Data Uri: %s", actual.getDataString());
            return false;
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                if (actual.hasExtra(key)) {
                    Object obj = extras.get(key);
                    if (obj instanceof String) {
                        if (!((String) obj).equals(actual.getStringExtra(key))) {
                            expected = String.format("Intent with key: \"%s\", with value of: \"%s\"", key, extras.getString(key));
                            describeReason = String.format("has value of: %s", actual.getStringExtra(key));
                            return false;
                        }
                    } else if (obj instanceof Long) {
                        if ((Long) obj != actual.getLongExtra(key, 0L)) {
                            expected = String.format("Intent with key: \"%s\", with value of: \"%s\"", key, extras.getLong(key, 0L));
                            describeReason = String.format("has value of: %s", actual.getLongExtra(key, 0L));
                            return false;
                        }
                    } else if (obj instanceof Intent) {
                        IntentMatcher nestedIntentMatcher = new IntentMatcher((Intent) extras.get(key));
                        if (!nestedIntentMatcher.matches(actual.getExtras().get(key))) {
                            expected = nestedIntentMatcher.expected;
                            describeReason = nestedIntentMatcher.describeReason;
                            return false;
                        }
                    } else if (obj instanceof Parcelable) {
                        if (!extras.getParcelable(key).equals(actual.getParcelableExtra(key))) {
                            expected = "Parcelables Do not Match";
                            describeReason = String.format("Consider overriding %s.equals(obj)", obj.getClass().getSimpleName());
                            return false;
                        }
                    } else {
                        expected = String.format("Matcher does not support type: %s", obj.getClass().getSimpleName());
                        describeReason = "Please Implement";
                        return false;
                    }
                } else {
                    expected = String.format("expected extra with key: \"%s\"", key);
                    describeReason = "was Null";
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendValue(describeReason);
    }
}
