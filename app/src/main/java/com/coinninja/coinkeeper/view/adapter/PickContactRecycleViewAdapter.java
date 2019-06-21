package com.coinninja.coinkeeper.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.coinninja.coinkeeper.view.adapter.PickContactRecycleViewAdapter.ViewHolder.CONTACT;
import static com.coinninja.coinkeeper.view.adapter.PickContactRecycleViewAdapter.ViewHolder.UNVERIFIED_HEADER;

public class PickContactRecycleViewAdapter extends RecycleViewAdapterFilterable<PickContactRecycleViewAdapter.ViewHolder> {
    OnItemClickListener clickListener;
    int unVerifiedHeaderIndex = -1;
    View.OnClickListener dropbitClickHandler;
    ContactFilter contactFilter;
    private List<Contact> contacts;

    public PickContactRecycleViewAdapter() {
        contacts = new ArrayList<>();
    }

    public void setOnClickListeners(OnItemClickListener clickListener, View.OnClickListener whatIsDropbitClickListener) {
        dropbitClickHandler = whatIsDropbitClickListener;
        this.clickListener = clickListener;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        setupDataSourceForContacts(contacts);
        contactFilter = new ContactFilter(contacts);

        notifyDataSetChanged();
    }

    public Contact getItemAt(int position) {
        if (position == unVerifiedHeaderIndex) {
            return null;
        }

        boolean hasUnVerifiedHeader = unVerifiedHeaderIndex != -1;
        boolean isBelowUnVerifiedHeader = hasUnVerifiedHeader && position > unVerifiedHeaderIndex;

        int offset = isBelowUnVerifiedHeader ? 1 : 0;

        return contacts.get(position - offset);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder view = null;

        switch (viewType) {
            case UNVERIFIED_HEADER:
                view = new ViewHolderHeader(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_unverified_header, parent, false),
                        viewType, dropbitClickHandler);
                break;

            case CONTACT:
                view = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout, parent, false), clickListener, viewType);
                break;
        }

        return view;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case UNVERIFIED_HEADER:
                return;
        }

        Contact contact = getItemAt(position);
        holder.bindTo(contact, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == unVerifiedHeaderIndex) {
            return UNVERIFIED_HEADER;
        } else {
            return CONTACT;
        }
    }

    @Override
    public int getItemCount() {
        int offsetCount = unVerifiedHeaderIndex == -1 ? 0 : 1;

        return contacts.size() + offsetCount;
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private void setupDataSourceForContacts(List<Contact> contacts) {
        List<Contact> unverifiedContacts = new ArrayList<>();
        List<Contact> verifiedContacts = new ArrayList<>();
        unVerifiedHeaderIndex = 0;

        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            if (contact.isVerified()) {
                verifiedContacts.add(contact);
                unVerifiedHeaderIndex++;
            } else {
                unverifiedContacts.add(contact);
            }
        }

        this.contacts = new ArrayList<>();
        this.contacts.addAll(verifiedContacts);
        this.contacts.addAll(unverifiedContacts);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        static final int CONTACT = 0;
        static final int UNVERIFIED_HEADER = 2;
        public final int viewType;
        private final OnItemClickListener onItemClickListener;
        public View view;
        int position;

        public ViewHolder(View itemView, OnItemClickListener onItemClickListener, int viewType) {
            super(itemView);
            view = itemView;
            this.viewType = viewType;
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(V -> onClick());
        }

        public void bindTo(Contact contact, int position) {
            this.position = position;
            ((TextView) view.findViewById(R.id.contact_name)).setText(contact.getDisplayName());
            ((TextView) view.findViewById(R.id.contact_phone)).setText(contact.getPhoneNumber().toInternationalDisplayText());
            View sendButton = view.findViewById(R.id.contact_action_send);
            View inviteButton = view.findViewById(R.id.contact_action_invite);

            sendButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.GONE);
            sendButton.setOnLongClickListener(null);
            inviteButton.setOnLongClickListener(null);

            Button actionButton;
            if (contact.isVerified()) {
                actionButton = (Button) sendButton;
            } else {
                actionButton = (Button) inviteButton;
            }

            actionButton.setOnClickListener(V -> onClick());
            actionButton.setVisibility(View.VISIBLE);
        }

        private void onClick() {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, position);
            }
        }
    }

    public static class ViewHolderHeader extends ViewHolder {

        public ViewHolderHeader(View itemView, int viewType, View.OnClickListener dropbitClickHandler) {
            super(itemView, null, viewType);
            itemView.findViewById(R.id.what_is_dropbit).setOnClickListener(dropbitClickHandler);
        }

    }

    private class ContactFilter extends Filter {

        private final List<Contact> contacts;

        public ContactFilter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            List<Contact> filteredVerifiedContacts = performContactFiltering(constraint, contacts);

            filterResults.count = filteredVerifiedContacts.size();
            filterResults.values = filteredVerifiedContacts;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            PickContactRecycleViewAdapter.this.contacts = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }

        private List<Contact> performContactFiltering(CharSequence constraint, List<Contact> contacts) {
            if (constraint == null || constraint.length() == 0) {
                return contacts;
            }

            ArrayList<Contact> tempList = new ArrayList<>();
            for (Contact contact : contacts) {
                if (contact.getDisplayName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    tempList.add(contact);
                }
            }
            return tempList;
        }
    }
}