package com.coinninja.coinkeeper.ui.account;

import android.widget.ListView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.SERVER_ADDRESSES;

public class UserServerAddressesFragment extends BaseBottomDialogFragment {

    private List<AddressDTO> serverAddresses;
    private ListView addressListView;
    private ServerAddressArrayAdapter arrayAdapter;
    private DropbitUriBuilder dropbitUriBuilder = new DropbitUriBuilder();

    public static UserServerAddressesFragment newInstance(List<AddressDTO> serverAddresses) {
        UserServerAddressesFragment fragment = new UserServerAddressesFragment();
        fragment.serverAddresses = serverAddresses;
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<AddressDTOWrapper> wrappers = new ArrayList<>();
        for (AddressDTO addressDTO : serverAddresses) {
            wrappers.add(new AddressDTOWrapper(addressDTO));
            Collections.sort(wrappers);
        }

        addressListView = getView().findViewById(R.id.address_list_view);
        arrayAdapter = new ServerAddressArrayAdapter(getActivity(), wrappers);
        setupListViewDependencies();
        setupOnClickListeners();
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.user_server_addresses;
    }

    private void setupOnClickListeners() {
        getView().findViewById(R.id.tooltip).setOnClickListener(V -> onClickTooltip());
        getView().findViewById(R.id.ic_close).setOnClickListener(V -> onCloseClicked());
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
