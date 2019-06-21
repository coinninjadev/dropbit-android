package com.coinninja.coinkeeper.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;
import com.coinninja.coinkeeper.util.image.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import app.dropbit.twitter.model.TwitterUser;

public class PickTwitterRecycleViewAdapter extends RecycleViewAdapterFilterable<PickTwitterRecycleViewAdapter.ViewHolder> {
    private OnItemClickListener clickListener;
    private List<TwitterUser> twitterUsersFollowing;
    private List<TwitterUser> searchTwitterUsers;
    private TwitterFilter twitterFilter;

    public PickTwitterRecycleViewAdapter() {
        twitterUsersFollowing = new ArrayList<>();
        searchTwitterUsers = new ArrayList<>();
    }

    public TwitterUser getItemAt(int position) {
        return twitterUsersFollowing.get(position);
    }

    public void setOnClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.twitter_layout,
                parent, false), clickListener, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TwitterUser twitterUser = getItemAt(position);
        if (twitterUser == null) { return; }
        holder.bindTo(twitterUser, position);
    }

    @Override
    public int getItemCount() {
        if (twitterUsersFollowing == null) { return 0; }
        return twitterUsersFollowing.size();
    }

    public void setTwitterUsers(List<TwitterUser> twitterUsers) {
        twitterFilter = new TwitterFilter(twitterUsers);

        this.twitterUsersFollowing = twitterUsers;
        notifyDataSetChanged();
    }

    public void setSearchTwitterUsers(List<TwitterUser> followingTwitterUsers) {
        twitterFilter = new TwitterFilter(followingTwitterUsers);

        twitterUsersFollowing = followingTwitterUsers;
        notifyDataSetChanged();
    }

    public List<TwitterUser> getTwitterUsers() {
        return twitterUsersFollowing;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        private final OnItemClickListener onItemClickListener;
        int position;
        public final int viewType;
        private CircleTransform circleTransform;

        public ViewHolder(View itemView, OnItemClickListener onItemClickListener, int viewType) {
            super(itemView);
            view = itemView;
            this.viewType = viewType;
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(V -> onClick(this));
            circleTransform = CoinKeeperApplication.appComponent.provideCircleTransform();
        }

        private void onClick(ViewHolder viewHolder) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, position);
            }
        }

        public void bindTo(TwitterUser twitterUser, int position) {
            this.position = position;
            ((TextView) view.findViewById(R.id.twitter_name_text_view)).setText(twitterUser.getName());
            ((TextView) view.findViewById(R.id.twitter_handle_text_view)).setText(twitterUser.displayScreenName());
            view.findViewById(R.id.verified_checkmark).setVisibility(twitterUser.getVerified() ? View.VISIBLE : View.GONE);

            Picasso.get().load(twitterUser.getProfileImage()).transform(circleTransform).into(((ImageView) view.findViewById(R.id.twitter_profile_picture)));
        }
    }

    @Override
    public Filter getFilter() {
        return twitterFilter;
    }

    private class TwitterFilter extends Filter {

        private final List<TwitterUser> twitterUsers;

        public TwitterFilter(List<TwitterUser> verifiedContacts) {
            this.twitterUsers = verifiedContacts;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<TwitterUser> filteredTwitterUsers = filterTwitterUsers(constraint, twitterUsers);
            filterResults.count = filteredTwitterUsers.size();
            filterResults.values = filteredTwitterUsers;

            return filterResults;
        }

        private List<TwitterUser> filterTwitterUsers(CharSequence constraint, List<TwitterUser> followers) {
            if (constraint == null || constraint.length() == 0) { return twitterUsers; }

            ArrayList<TwitterUser> tempList = new ArrayList<>();
            for (TwitterUser user: twitterUsers) {
                if (user.getScreenName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    tempList.add(user);
                }
            }

            return tempList;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            PickTwitterRecycleViewAdapter.this.twitterUsersFollowing = (ArrayList<TwitterUser>) results.values;
            notifyDataSetChanged();
        }
    }
}