package com.coinninja.coinkeeper.util;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContactResolverUtilTest {

    public static final String PHONE_STRING = "+13305551111";
    @Mock
    PermissionsUtil permissionsUtil;

    @Mock
    ContentResolver contentResolver;

    @Mock
    Cursor cursor;

    @Test
    public void returns_list_of_contacts() {
        mockOutDB();
        when(permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)).thenReturn(true);
        LocalContactQueryUtil util = new LocalContactQueryUtil(contentResolver, permissionsUtil);

        List<Contact> contacts = util.getContacts();

        assertThat(contacts.size(), equalTo(1));
        assertThat(contacts.get(0).getPhoneNumber(), equalTo(new PhoneNumber(PHONE_STRING)));
        assertThat(contacts.get(0).getDisplayName(), equalTo("John Doe"));
    }

    private void mockOutDB() {
        when(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(false);
        when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)).thenReturn(1);
        when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)).thenReturn(2);
        when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).thenReturn(3);
        when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).thenReturn(4);
        when(cursor.getInt(1)).thenReturn(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        when(cursor.getString(2)).thenReturn(PHONE_STRING);
        when(cursor.getString(3)).thenReturn("(330) 555-1111");
        when(cursor.getString(4)).thenReturn("John Doe");
    }
}