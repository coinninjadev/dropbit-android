package com.coinninja.coinkeeper.ui.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import java.util.ArrayList;

public class ServerAddressArrayAdapter extends BaseAdapter {

    private ArrayList<AddressDTOWrapper> addresses;
    private LayoutInflater inflater;

    ServerAddressArrayAdapter(Context context, ArrayList<AddressDTOWrapper> serverAddresses) {
        addresses = serverAddresses;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public AddressDTOWrapper getItem(int position) {
        return addresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.server_address_array_adapter, null);
        TextView addressTextView = view.findViewById(R.id.address_text_view);
        addressTextView.setText(addresses.get(position).getDisplayText());

        if (addresses.get(position).isDerivationPath()) {
            addressTextView.setTextAppearance(addressTextView.getContext(), R.style.TextAppearance_Address_Cache_DerivativePath);
        } else {
            addressTextView.setTextAppearance(addressTextView.getContext(), R.style.TextAppearance_Address_Cache);
        }

        return view;
    }

}
