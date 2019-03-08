package com.coinninja.coinkeeper.view.adapter;

import android.net.Uri;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.adapter.CoinKeeperSupportAdapter.OnItemSelectedListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import androidx.recyclerview.widget.RecyclerView;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CoinKeeperSupportAdapterTest {

    private OnItemSelectedListener onItemSelectedListener;
    private CoinKeeperSupportAdapter adapter;
    private RecyclerView parent;

    @Before
    public void setUp() {
        parent = new RecyclerView(RuntimeEnvironment.application.getBaseContext());
        onItemSelectedListener = mock(OnItemSelectedListener.class);
        adapter = new CoinKeeperSupportAdapter(onItemSelectedListener);
    }

    @Test
    public void clicking_on_view_forwards_uri() {
        ArgumentCaptor<Uri> argumentCaptor = ArgumentCaptor.forClass(Uri.class);
        CoinKeeperSupportAdapter.VHItem holder = (CoinKeeperSupportAdapter.VHItem) adapter.onCreateViewHolder(parent, 1);
        adapter.onBindViewHolder(holder, 1);

        holder.getView().performClick();

        verify(onItemSelectedListener).onItemSelected(argumentCaptor.capture());
        Uri uri = argumentCaptor.getValue();
        assertNotNull(uri);
        assertThat(uri.toString(), equalTo("https://dropbit.app/faq#contact"));
    }

    @Test
    public void binds_to_view() {
        CoinKeeperSupportAdapter.VHItem holder = (CoinKeeperSupportAdapter.VHItem) adapter.onCreateViewHolder(parent, 1);

        adapter.onBindViewHolder(holder, 1);

        assertThat(holder.getView().getTag().toString(),
                equalTo("1"));

        assertThat(((TextView) holder.getView().findViewById(R.id.label)).getText().toString(),
                equalTo("Contact Us"));
    }

    @Test
    public void inflates_correct_layout() {
        CoinKeeperSupportAdapter.VHItem holder = (CoinKeeperSupportAdapter.VHItem) adapter.onCreateViewHolder(parent, 1);
        assertNotNull(holder);
        assertNotNull(holder.getView());
        assertNotNull(holder.getView().findViewById(R.id.list_link_indicator));
        assertNotNull(holder.getView().findViewById(R.id.label));
    }

    @Test
    public void contains_right_number_of_Urls() {
        assertThat(adapter.getItemCount(), equalTo(4));
    }
}