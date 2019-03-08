package com.coinninja.matchers.images;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class HasContentDescriptionMatcher<ImageView> extends BaseMatcher<ImageView> {

    private final String contentDescription;

    public HasContentDescriptionMatcher(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    @Override
    public boolean matches(Object item) {
        return contentDescription.equals(((android.widget.ImageView) item).getContentDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected view to have content description: ").appendValue(contentDescription);
    }
}
