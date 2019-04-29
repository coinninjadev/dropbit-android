package com.coinninja.coinkeeper.view.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.DropbitIntents;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CoinKeeperSupportAdapter extends RecyclerView.Adapter {

    private final OnItemSelectedListener onItemSelectedListener;
    private final List<String> urls;
    private final List<String> labels;

    public CoinKeeperSupportAdapter(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
        urls = new ArrayList<>(DropbitIntents.SUPPORT_LINKS.values());
        labels = new ArrayList<>(DropbitIntents.SUPPORT_LINKS.keySet());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VHItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_cn_support_item, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((VHItem) holder).bindTo(position, labels.get(position), this::onClickListener);
    }

    private void onClickListener(View view) {
        onItemSelectedListener.onItemSelected(
                Uri.parse(urls.get(
                        Integer.parseInt((String) view.getTag())
                )));
    }

    @Override
    public int getItemCount() {
        return DropbitIntents.SUPPORT_LINKS.size();
    }


    public class VHItem extends RecyclerView.ViewHolder {
        View view;

        public VHItem(View itemView) {
            super(itemView);
            view = itemView;
        }

        public View getView() {
            return view;
        }

        public void bindTo(int position, String label, View.OnClickListener onClickListener) {
            view.setOnClickListener(onClickListener);
            view.setTag(String.valueOf(position));
            ((TextView) view.findViewById(R.id.label)).setText(label);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Uri uri);
    }
}
