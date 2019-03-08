package com.coinninja.coinkeeper.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SeedWordsPagerAdapterTest {
    private String[] seedWords = {"WORD1", "WORD2", "WORD3", "WORD4", "WORD5", "WORD6", "WORD7", "WORD8", "WORD9", "WORD10", "WORD11", "WORD12"};

    private SeedWordsPagerAdapter adapter;

    private int position = 0;

    @Mock
    private ViewGroup rootView;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(rootView.getContext()).thenReturn(RuntimeEnvironment.application.getApplicationContext());
        adapter = new SeedWordsPagerAdapter();
        adapter.setSeedWords(seedWords);
    }

    @After
    public void tearDown() throws Exception {
        seedWords = null;
        adapter = null;
        rootView = null;
    }

    @Test
    public void instantiateItem() {
        View view = (View) adapter.instantiateItem(rootView, position);

        String word = ((TextView) view.findViewById(R.id.seed_word_txt_view)).getText().toString();

        assertThat(word, equalTo(seedWords[position]));
    }

    @Test
    public void destroyItem() {
        View view = (View) adapter.instantiateItem(rootView, position);

        adapter.destroyItem(rootView, position, view);

        verify(rootView).removeView(view);
    }

    @Test
    public void getCount() {
        int wordCount = adapter.getCount();

        assertEquals(wordCount, seedWords.length);
    }

    @Test
    public void isViewFromObject() {
        boolean isViewFromObject;

        isViewFromObject = adapter.isViewFromObject(rootView, rootView);

        assertTrue(isViewFromObject);


        View nonView = mock(View.class);
        isViewFromObject = adapter.isViewFromObject(rootView, nonView);

        assertFalse(isViewFromObject);
    }

}