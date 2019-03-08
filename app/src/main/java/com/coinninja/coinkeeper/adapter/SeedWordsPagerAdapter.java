package com.coinninja.coinkeeper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import java.util.Locale;

import javax.inject.Inject;

import androidx.viewpager.widget.PagerAdapter;

public class SeedWordsPagerAdapter extends PagerAdapter {
    String[] seedWords;

    @Inject
    public SeedWordsPagerAdapter() {

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View rootView = LayoutInflater.from(container.getContext()).inflate(R.layout.pageradapter_item_recovery_words, null);

        TextView wordView = rootView.findViewById(R.id.seed_word_txt_view);

        wordView.setText(seedWords[position].toUpperCase(Locale.ENGLISH));
        container.addView(rootView);
        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object rootView) {
        container.removeView((View) rootView);
    }

    @Override
    public int getCount() {
        return seedWords.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object rootView) {
        return view == rootView;
    }


    public void setSeedWords(String[] seedWords) {
        this.seedWords = seedWords;
    }
}

