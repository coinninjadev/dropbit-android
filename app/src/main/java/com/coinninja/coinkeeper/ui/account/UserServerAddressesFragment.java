package com.coinninja.coinkeeper.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.SERVER_ADDRESSES;

public class UserServerAddressesFragment extends BaseDialogFragment {

    private List<AddressDTO> serverAddresses;
    private ListView addressListView;
    private ServerAddressArrayAdapter arrayAdapter;
    private DropbitUriBuilder dropbitUriBuilder = new DropbitUriBuilder();
    private View view;

    public static UserServerAddressesFragment newInstance(List<AddressDTO> serverAddresses) {
        UserServerAddressesFragment fragment = new UserServerAddressesFragment();
        fragment.serverAddresses = serverAddresses;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_server_addresses, container, false);
        view = root;
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        ArrayList<AddressDTOWrapper> wrappers = new ArrayList<>();
        for (AddressDTO addressDTO : serverAddresses) {
            wrappers.add(new AddressDTOWrapper(addressDTO));
            Collections.sort(wrappers);
        }

        addressListView = view.findViewById(R.id.address_list_view);
        arrayAdapter = new ServerAddressArrayAdapter(getActivity(), wrappers);
        setupListViewDependencies();
        setupOnClickListeners();
    }

    private void setupOnClickListeners() {
        view.findViewById(R.id.tooltip).setOnClickListener(V -> onClickTooltip());
        view.findViewById(R.id.ic_close).setOnClickListener(V -> onCloseClicked());
    }

    private void onCloseClicked() {
        dismiss();
    }

    private void setupListViewDependencies() {
        addressListView.setAdapter(arrayAdapter);
        addressListView.setOnItemClickListener((parent, view, position, id) -> {
            AddressDTOWrapper wrapper = arrayAdapter.getItem(position);
            wrapper.toggleShowingDerivationPath();
            arrayAdapter.notifyDataSetChanged();
        });
    }

    private void onClickTooltip() {
        UriUtil.openUrl(dropbitUriBuilder.build(SERVER_ADDRESSES), getActivity());
    }
}
