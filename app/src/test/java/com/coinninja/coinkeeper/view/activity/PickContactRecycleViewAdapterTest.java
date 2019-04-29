package com.coinninja.coinkeeper.view.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.ui.util.OnItemClickListener;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PickContactRecycleViewAdapterTest {

    public static final String DISPLAY_NAME = "John Doe";
    public static final String PHONE_NUMBER = "+13305551111";
    private List<Contact> verifiedContacts;
    private List<Contact> unVerifiedContacts;
    private View.OnClickListener whatIsDB = mock(View.OnClickListener.class);

    private PickContactRecycleViewAdapter adapter;
    private PickContactActivity activity;
    private ViewGroup parent;
    private PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();
    PhoneNumber phoneNumber1 = new PhoneNumber("+1111111111");
    PhoneNumber phoneNumber2 = new PhoneNumber("+2222222222");
    PhoneNumber contactPhoneNumber = new PhoneNumber(PHONE_NUMBER);

    OnItemClickListener onItemClickListener;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(PickContactActivity.class).create().get();
        parent = activity.findViewById(R.id.list_contacts);
        verifiedContacts = new ArrayList<>();
        Contact contact = new Contact(contactPhoneNumber, DISPLAY_NAME, true);
        verifiedContacts.add(contact);
        onItemClickListener = mock(OnItemClickListener.class);
        adapter = new PickContactRecycleViewAdapter(onItemClickListener, whatIsDB);
    }

    @Test
    public void forwards_click_for_what_is_bitcoin() {
        unVerifiedContacts = build_5_contacts(false);
        adapter.setContacts(verifiedContacts, unVerifiedContacts);

        PickContactRecycleViewAdapter.ViewHolder holder =
                adapter.onCreateViewHolder(parent, PickContactRecycleViewAdapter.ViewHolder.UNVERIFIED_HEADER);

        holder.itemView.findViewById(R.id.what_is_dropbit).performClick();

        verify(whatIsDB).onClick(any(View.class));
    }

    @Test
    public void forwards_clicks_up() {
        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        PickContactRecycleViewAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        holder.view.performClick();

        verify(onItemClickListener).onItemClick(holder.view, 0);
    }

    @Test
    public void create_view_holder() {
        String expectedPhoneDisplay = "+1 330-555-1111";
        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        PickContactRecycleViewAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertThat(((TextView) holder.view.findViewById(R.id.contact_phone)).getText(), equalTo(expectedPhoneDisplay));
        assertThat(((TextView) holder.view.findViewById(R.id.contact_name)).getText(), equalTo(DISPLAY_NAME));
    }

    @Test
    public void setting_verified_contacts_updates_item_count() {
        adapter.setContacts(verifiedContacts, unVerifiedContacts);

        assertThat(adapter.getItemCount(), equalTo(1));
    }

    @Test
    public void identify_correct_head_for_concat_2_list() {
        ArrayList<Contact> masterList = build_100_contacts(true);
        List<Contact> unVerifiedContacts = build_5_contacts(false);


        int indexOFSecondListFirstItemInMasterList = adapter.concatListAddHeader(masterList, unVerifiedContacts);

        Contact firstItemInSecondList = unVerifiedContacts.get(0);

        assertThat(indexOFSecondListFirstItemInMasterList, equalTo(masterList.indexOf(firstItemInSecondList)));
    }

    @Test
    public void filter_contacts_with_using_full_first_and_last_name_test() {
        String sampleUserSearchQuery = "Jeff Ubuntu";
        int expectedNumberOfFilteredContacts = 1;
        int expectedNumberOfListHeadersDividers = 0;
        int expectedItemCount = expectedNumberOfFilteredContacts + expectedNumberOfListHeadersDividers;
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();

        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();
        searchFilter.filter(sampleUserSearchQuery);

        assertThat(adapter.getItemCount(), equalTo(expectedItemCount));
        assertThat(adapter.getItemAt(0).getDisplayName(), equalTo("Jeff Ubuntu"));
    }

    @Test
    public void filter_contacts_with_using_full_first_name_test() {
        String sampleUserSearchQuery = "Mike";
        int expectedNumberOfFilteredContacts = 1;
        int expectedNumberOfListHeadersDividers = 0;
        int expectedItemCount = expectedNumberOfFilteredContacts + expectedNumberOfListHeadersDividers;
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();

        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();
        searchFilter.filter(sampleUserSearchQuery);

        assertThat(adapter.getItemCount(), equalTo(expectedItemCount));
        assertThat(adapter.getItemAt(0).getDisplayName(), equalTo("Mike MacBook"));
    }

    @Test
    public void filter_contacts_with_using_full_last_name_test() {
        String sampleUserSearchQuery = "Smith";
        int expectedNumberOfFilteredContacts = 1;
        int expectedNumberOfListHeadersDividers = 1;
        int expectedItemCount = expectedNumberOfFilteredContacts + expectedNumberOfListHeadersDividers;
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();

        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();
        searchFilter.filter(sampleUserSearchQuery);

        assertThat(adapter.getItemCount(), equalTo(expectedItemCount));
        assertThat(adapter.getItemAt(0), equalTo(null));//divider position
        assertThat(adapter.getItemAt(1).getDisplayName(), equalTo("Amanda Smith"));
    }

    @Test
    public void not_filter_contacts_when_using_types_nothing_test() {
        String sampleUserSearchQuery = "";
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();
        int expectedNumberOfFilteredContacts = verifiedContacts.size() + unVerifiedContacts.size();
        int expectedNumberOfListHeadersDividers = 1;
        int expectedItemCount = expectedNumberOfFilteredContacts + expectedNumberOfListHeadersDividers;


        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();
        searchFilter.filter(sampleUserSearchQuery);

        assertThat(adapter.getItemCount(), equalTo(expectedItemCount));
    }

    @Test
    public void filter_contacts_with_the_letters_j_test() {
        String sampleUserSearchQuery = "j";
        int expectedNumberOfFilteredContacts = 3;
        int expectedNumberOfListHeadersDividers = 1;
        int expectedItemCount = expectedNumberOfFilteredContacts + expectedNumberOfListHeadersDividers;
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();

        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();
        searchFilter.filter(sampleUserSearchQuery);

        assertThat(adapter.getItemCount(), equalTo(expectedItemCount));
        assertThat(adapter.getItemAt(0).getDisplayName(), equalTo("Jeff Ubuntu"));
        assertThat(adapter.getItemAt(1).getDisplayName(), equalTo("Jim Think"));
        assertThat(adapter.getItemAt(2), equalTo(null));//divider position
        assertThat(adapter.getItemAt(3).getDisplayName(), equalTo("Jane Doe"));
    }

    @Test
    public void live_typing_text_list_gets_smaller_test() {
        String sampleUserSearchQuery = "Jane Doeeeee";
        List<Contact> verifiedContacts = build_searchableVerifiedContactsContacts();
        List<Contact> unVerifiedContacts = build_searchableUnVerifiedContactsContacts();

        adapter.setContacts(verifiedContacts, unVerifiedContacts);
        Filter searchFilter = adapter.getFilter();

        String liveType = "";
        for (int i = 0; i < sampleUserSearchQuery.toCharArray().length; i++) {
            searchFilter.filter(liveType);
            switch (i) {
                case 0:
                    assertThat(liveType, equalTo(""));
                    assertThat(adapter.getItemCount(), equalTo(9));
                    break;
                case 1:
                    assertThat(liveType, equalTo("J"));
                    assertThat(adapter.getItemCount(), equalTo(4));
                    break;
                case 2:
                    assertThat(liveType, equalTo("Ja"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 3:
                    assertThat(liveType, equalTo("Jan"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 4:
                    assertThat(liveType, equalTo("Jane"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 5:
                    assertThat(liveType, equalTo("Jane "));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 6:
                    assertThat(liveType, equalTo("Jane D"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 7:
                    assertThat(liveType, equalTo("Jane Do"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 8:
                    assertThat(liveType, equalTo("Jane Doe"));
                    assertThat(adapter.getItemCount(), equalTo(2));
                    break;
                case 9:
                    assertThat(liveType, equalTo("Jane Doee"));
                    assertThat(adapter.getItemCount(), equalTo(0));
                    break;
                case 10:
                    assertThat(liveType, equalTo("Jane Doeee"));
                    assertThat(adapter.getItemCount(), equalTo(0));
                    break;
                case 11:
                    assertThat(liveType, equalTo("Jane Doeeee"));
                    assertThat(adapter.getItemCount(), equalTo(0));
                    break;
            }
            char chr = sampleUserSearchQuery.toCharArray()[i];
            liveType += chr;
        }

    }

    private List<Contact> build_searchableVerifiedContactsContacts() {

        List<Contact> verifiedContacts = new ArrayList<>();
        Contact contact1Verified = new Contact(phoneNumber1, "Jeff Ubuntu", true);
        Contact contact2Verified = new Contact(phoneNumber1, "Jim Think", true);
        Contact contact3Verified = new Contact(phoneNumber1, "Tom Pad", true);
        Contact contact4Verified = new Contact(phoneNumber1, "Mike MacBook", true);
        verifiedContacts.add(contact1Verified);
        verifiedContacts.add(contact2Verified);
        verifiedContacts.add(contact3Verified);
        verifiedContacts.add(contact4Verified);
        return verifiedContacts;
    }

    private List<Contact> build_searchableUnVerifiedContactsContacts() {
        List<Contact> unVerifiedContacts = new ArrayList<>();
        Contact contact1UnVerified = new Contact(phoneNumber2, "Bob Him", false);
        Contact contact2UnVerified = new Contact(phoneNumber2, "Jane Doe", false);
        Contact contact3UnVerified = new Contact(phoneNumber2, "Amanda Smith", false);
        Contact contact4UnVerified = new Contact(phoneNumber2, "Artificially Flavored", false);
        unVerifiedContacts.add(contact1UnVerified);
        unVerifiedContacts.add(contact2UnVerified);
        unVerifiedContacts.add(contact3UnVerified);
        unVerifiedContacts.add(contact4UnVerified);
        return unVerifiedContacts;
    }

    @Test
    public void add_two_list_with_one_header_check_every_item() {
        List<Contact> verifiedContacts = build_5_contacts(true);
        List<Contact> unVerifiedContacts = build_5_contacts(false);

        adapter.setContacts(verifiedContacts, unVerifiedContacts);

        for (int i = 0; i < verifiedContacts.size(); i++) {
            Contact contactInAdapter = adapter.getItemAt(i);
            Contact verifiedContact = verifiedContacts.get(i);

            assertThat(contactInAdapter, equalTo(verifiedContact));
        }

        int expectedPositionOfUnVerifiedHeader = 5;
        int bothListSize = verifiedContacts.size() + unVerifiedContacts.size();
        for (int i = expectedPositionOfUnVerifiedHeader; i < bothListSize; i++) {
            Contact contactInAdapter = adapter.getItemAt(i);
            if (i == expectedPositionOfUnVerifiedHeader) {
                assertThat(contactInAdapter, nullValue());
                continue;
            }

            Contact unVerifiedContact = unVerifiedContacts.get(i - (expectedPositionOfUnVerifiedHeader + 1));
            assertThat(contactInAdapter, equalTo(unVerifiedContact));
        }

    }

    private ArrayList<Contact> build_100_contacts(boolean isVerified) {
        return (ArrayList<Contact>) build_contacts(100, isVerified);
    }

    private List<Contact> build_5_contacts(boolean isVerified) {
        return build_contacts(5, isVerified);
    }

    private List<Contact> build_contacts(int howManyToBuild, boolean isVerified) {
        List<Contact> contacts = new ArrayList<>();

        for (int i = 0; i < howManyToBuild; i++) {
            String displayName = "My Name " + i + " " + isVerified;
            String phoneNumber = "+1216" + i + i + i + "4561";
            Contact contact = new Contact(new PhoneNumber(phoneNumber), displayName, isVerified);
            contacts.add(contact);
        }
        return contacts;
    }
}