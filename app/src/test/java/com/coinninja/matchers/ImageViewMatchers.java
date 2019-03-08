package com.coinninja.matchers;

import android.widget.ImageView;

import com.coinninja.matchers.images.HasContentDescriptionMatcher;

import org.hamcrest.Matcher;

public class ImageViewMatchers {
    public static final Matcher<ImageView> hasContentDescription(String contentDescription) {
        return new HasContentDescriptionMatcher<>(contentDescription);
    }

}
