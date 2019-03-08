package com.coinninja.coinkeeper.view.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static com.coinninja.coinkeeper.view.activity.PickContactRecycleViewAdapter.ViewHolder.CONTACT;
import static com.coinninja.coinkeeper.view.activity.PickContactRecycleViewAdapter.ViewHolder.UNVERIFIED_HEADER;
import static com.coinninja.coinkeeper.view.activity.PickContactRecycleViewAdapter.ViewHolder.VERIFIED_HEADER;

class PickContactRecycleViewAdapter extends Adapter<PickContactRecycleViewAdapter.ViewHolder> implements Filterable {
    final OnItemClickListener clickListener;
    private List<Contact> contacts;

    private int verifiedHeaderIndex = -1;
    private int unVerifiedHeaderIndex = -1;
    private View.OnClickListener dropbitClickHandler;
    private ContactFilter contactFilter;
    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = new PhoneNumberUtil();

    public PickContactRecycleViewAdapter(OnItemClickListener clickListener, View.OnClickListener dropbitClickHandler) {
        this.dropbitClickHandler = dropbitClickHandler;
        contacts = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public Contact getItemAt(int position) {
        if (position == verifiedHeaderIndex) {
            return null;
        }
        if (position == unVerifiedHeaderIndex) {
            return null;
        }

        //do we have an VerifiedHeader on screen?
        boolean hasVerifiedHeader = verifiedHeaderIndex == -1 ? false : true;
        //do we have an UnVerifiedHeader on screen?
        boolean hasUnVerifiedHeader = unVerifiedHeaderIndex == -1 ? false : true;

        //is the requested position below the verified header
        boolean isBelowVerifiedHeader = hasVerifiedHeader ? position > verifiedHeaderIndex : false;
        //is the requested position below the verified header
        boolean isBelowUnVerifiedHeader = hasUnVerifiedHeader ? position > unVerifiedHeaderIndex : false;


        int offset = isBelowVerifiedHeader ? 1 : 0;
        offset += isBelowUnVerifiedHeader ? 1 : 0;

        return contacts.get(position - offset);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == verifiedHeaderIndex) {
            return VERIFIED_HEADER;
        } else if (position == unVerifiedHeaderIndex) {
            return UNVERIFIED_HEADER;
        } else {
            return CONTACT;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder view = null;

        switch (viewType) {
            case VERIFIED_HEADER:
            case UNVERIFIED_HEADER:
                view = new ViewHolderHeader(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_unverified_header, parent, false),
                        viewType, dropbitClickHandler);
                break;

            case CONTACT:
                view = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_contact_item, parent, false), clickListener, viewType);
                break;
        }

        return view;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case VERIFIED_HEADER:
            case UNVERIFIED_HEADER:
                return;
        }

        Contact contact = getItemAt(position);
        holder.bindTo(contact, position);
    }

    @Override
    public int getItemCount() {
        int offsetCount = verifiedHeaderIndex == -1 ? 0 : 1;
        offsetCount += unVerifiedHeaderIndex == -1 ? 0 : 1;

        return contacts.size() + offsetCount;
    }

    public void setContacts(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts) {
        contactFilter = new ContactFilter(verifiedContacts, unVerifiedContacts);
        ArrayList<Contact> allContacts = buildMergedList(verifiedContacts, unVerifiedContacts);

        contacts = allContacts;
        notifyDataSetChanged();
    }

    private ArrayList<Contact> buildMergedList(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts) {
        ArrayList<Contact> allContacts = new ArrayList<>();//reset set to default values first
        verifiedHeaderIndex = -1;//reset set to default values first
        unVerifiedHeaderIndex = -1;//reset set to defaults values first


        if (verifiedContacts != null && !verifiedContacts.isEmpty()) {
            allContacts.addAll(verifiedContacts);
        }

        if (unVerifiedContacts != null && !unVerifiedContacts.isEmpty()) {
            unVerifiedHeaderIndex = concatListAddHeader(allContacts, unVerifiedContacts);
        }
        return allContacts;
    }

    public int concatListAddHeader(ArrayList<Contact> masterList, List<Contact> listToAdd) {
        masterList.addAll(listToAdd);
        return masterList.size() - listToAdd.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        static final int CONTACT = 0;
        static final int VERIFIED_HEADER = 1;
        static final int UNVERIFIED_HEADER = 2;

        View view;
        private final OnItemClickListener onItemClickListener;
        int position;
        public final int viewType;

        public ViewHolder(View itemView, OnItemClickListener onItemClickListener, int viewType) {
            super(itemView);
            view = itemView;
            this.viewType = viewType;
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(V -> onClick(this));
        }

        private void onClick(ViewHolder viewHolder) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, position);
            }
        }

        public void bindTo(Contact contact, int position) {
            this.position = position;
            ((TextView) view.findViewById(R.id.contact_name)).setText(contact.getDisplayName());
            ((TextView) view.findViewById(R.id.contact_phone)).setText(contact.getPhoneNumber().toNationalDisplayText());
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

            actionButton.setOnClickListener(V -> onClick(this));
            actionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    public static class ViewHolderHeader extends ViewHolder {

        public ViewHolderHeader(View itemView, int viewType, View.OnClickListener dropbitClickHandler) {
            super(itemView, null, viewType);
            itemView.findViewById(R.id.what_is_dropbit).setOnClickListener(dropbitClickHandler);
        }

    }

    private class ContactFilter extends Filter {

        private final List<Contact> verifiedContacts;
        private final List<Contact> unVerifiedContacts;

        public ContactFilter(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts) {
            this.verifiedContacts = verifiedContacts;
            this.unVerifiedContacts = unVerifiedContacts;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            List<Contact> filteredVerifiedContacts = performContactFiltering(constraint, verifiedContacts);
            List<Contact> filteredUnVerifiedContacts = performContactFiltering(constraint, unVerifiedContacts);

            ArrayList<Contact> filteredContacts = buildMergedList(filteredVerifiedContacts, filteredUnVerifiedContacts);

            filterResults.count = filteredContacts.size();
            filterResults.values = filteredContacts;

            return filterResults;
        }

        private List<Contact> performContactFiltering(CharSequence constraint, List<Contact> contacts) {
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Contact> tempList = new ArrayList<>();
                for (Contact contact : contacts) {
                    if (contact.getDisplayName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(contact);
                    }
                }
                return tempList;
            } else {
                return contacts;
            }
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contacts = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }
    }
}