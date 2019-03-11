package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import java.util.List;

class PhoneNumberInputViewCountryListAdapter extends ArrayAdapter<CountryCodeLocale> {

    private static class ViewHolder {
        TextView emoji;
        TextView displayName;
        TextView countryCode;
    }

    public PhoneNumberInputViewCountryListAdapter(Context context, List<CountryCodeLocale> countryCodeLocales) {
        super(context, R.layout.phone_number_input_widget___item_country, countryCodeLocales);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CountryCodeLocale countryCodeLocale = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.phone_number_input_widget___item_country, parent, false);
            viewHolder.emoji = convertView.findViewById(R.id.phone_number_widget_country_selector_flag);
            viewHolder.displayName = convertView.findViewById(R.id.phone_number_widget_country_selector_country_name);
            viewHolder.countryCode = convertView.findViewById(R.id.phone_number_widget_country_selector_country_code);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.emoji.setText(countryCodeLocale.getEmoji());
        viewHolder.displayName.setText(countryCodeLocale.getDisplayName());
        viewHolder.countryCode.setText(countryCodeLocale.getDisplayCountryCode());

        return convertView;
    }

}
