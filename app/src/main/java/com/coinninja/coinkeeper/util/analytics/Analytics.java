package com.coinninja.coinkeeper.util.analytics;

import android.app.Activity;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public interface Analytics {
    // EVENTS

    // -- BROADCASTING
    String EVENT_BROADCAST_STARTED = "BroadcastStart";
    String EVENT_BROADCAST_COMPLETE = "BroadcastSuccess";
    String EVENT_BROADCAST_FAILED = "BroadcastFailure";
    String EVENT_CONTACT_SEND = "ContactSend";
    String EVENT_DROPBIT_SEND = "DropBitSend";
    String EVENT_BROADCAST_TO_ADDRESS = "AddressPayment";
    // -- BROADCASTING -- Pending
    String EVENT_PENDING_TRANSACTION_FAILED = "TxVerificationFailure";
    String EVENT_PENDING_DROPBIT_SEND_FAILED = "DropBitSendFailure  ";
    String EVENT_PENDING_DROPBIT_RECEIVE_FAILED = "DropBitReceiveFailure";
    // -- PHONE VERIFICATION
    String EVENT_PHONE_VERIFICATION_SUCCESSFUL = "PhoneVerified";
    String EVENT_PHONE_VERIFICATION_SKIPPED = "SkipPhoneVerification";
    String EVENT_PHONE_AUTO_DEVERIFIED = "PhoneAutoDeverified";
    // -- WALLET
    String EVENT_WALLET_DELETE = "DeleteWallet"; //- user deletes wallet
    String EVENT_WALLET_RESTORE = "RestoreWallet";
    String EVENT_WALLET_BACKUP_SUCCESSFUL = "WordsBackedUp";
    String EVENT_WALLET_CREATE = "CreateWallet"; //- user creates a wallet
    // CONTACTS PICK SCREEN
    String EVENT_INVITE_WHATIS_DROPBIT = "WhatIsDropBit"; //- user taps what is dropbit link
    String EVENT_DROPBIT_SEND_BTN = "DropBitPressed"; //- user taps dropbit send
    String EVENT_CONTACT_SEND_BTN = "ContactPressed"; //- user taps contact send    String EVENT_DROPBIT_SEND_BTN = "DropBitSend"; //- user taps dropbit send
    // TRANSACTION
    String EVENT_DROPBIT_INITIATED = "DropBitInitiated";
    String EVENT_DROPBIT_INITIATION_FAILED = "DropBitInitiationFailed";
    String EVENT_DROPBIT_ADDRESS_PROVIDED = "DropBitAddressProvided";
    String EVENT_DROPBIT_COMPLETED = "DropBitCompleted";
    String EVENT_TRANSACTION_RETRY = "RetryFailedPayment";
    // -- Button Pressed --
    String EVENT_BUTTON_SUFFIX = "Btn";
    // -- Button Pressed -- Payment Bar
    String EVENT_BUTTON_BALANCE_HISTORY = "BalanceHistory"; // - User taps History button
    String EVENT_BUTTON_REQUEST = "Request"; // - User taps request button on payment bar
    String EVENT_BUTTON_SCAN_QR = "ScanQR"; //- User taps Scan QR button on payment bar
    String EVENT_BUTTON_PAY = "Pay"; //- User taps pay button on payment bar
    // -- Button Pressed -- DrawerBar
    String EVENT_BUTTON_HISTORY = "History"; // - User taps History button
    String EVENT_BUTTON_SETTINGS = "Settings"; // - User taps settings button
    String EVENT_BUTTON_SPEND = "Spend"; // - User taps spend button
    String EVENT_BUTTON_SUPPORT = "Support"; // - User taps support button
    // -- Button Pressed -- Request fragment
    String EVENT_BUTTON_SEND_REQUEST = "SendRequest"; // - User taps send request button of QR code
    // -- Button Pressed -- Pay fragment
    String EVENT_PAY_SCREEN_LOADED = "PayScreenLoaded";
    String EVENT_BUTTON_CONTACTS = "Contacts"; // - user taps contacts button
    String EVENT_BUTTON_SCAN = "Scan"; // - user taps scan button on the pay dialog screen
    String EVENT_BUTTON_PASTE = "Paste"; // - user taps paste button
    String EVENT_CONFIRM_SCREEN_LOADED = "ConfirmScreenLoaded";
    // -- Button Pressed -- History
    String EVENT_BUTTON_SHARE_TRANS_ID = "ShareTransID"; // - user taps share transaction ID button
    //json keys
    String EVENT_BROADCAST_JSON_KEY_LIB_CODE = "LibCode";
    String EVENT_BROADCAST_JSON_KEY_LIB_MSG = "LibMsg";
    String EVENT_BROADCAST_JSON_KEY_BLOCK_CODE = "BlockCode";
    String EVENT_BROADCAST_JSON_KEY_BLOCK_MSG = "BlockMsg";
    String EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL = "Message";
    String EVENT_MEMO_JSON_KEY_DID_SHARE = "SharingEnabled";
    //check-in
    String EVENT_BROADCAST_CHECK_IN_FAIL = "CheckInFail";
    //SHARED MEMOS
    String EVENT_SENT_SHARED_PAYLOAD = "SharedPayloadSent";

    //BACKUP / Recovery Words
    String EVENT_VIEW_RECOVERY_WORDS = "ViewWords";

    //Transaction history empty state
    String EVENT_GET_BITCOIN = "GetBitcoin";
    String EVENT_LEARN_BITCOIN = "LearnBitcoin";
    String EVENT_SPEND_BITCOIN = "SpendBitcoin";

    //Buying bitcoin
    String EVENT_BUY_BITCOIN_CREDIT_CARD = "BuyBitcoinWithCreditCard";
    String EVENT_BUY_BITCOIN_GIFT_CARD = "BuyBitcoinWithGiftCard";
    String EVENT_BUY_BITCOIN_AT_ATM = "BuyBitcoinAtATM";

    //Spending bitcoin
    String EVENT_SPEND_GIFT_CARDS = "SpendOnGiftCards";
    String EVENT_SPEND_AROUND_ME = "SpendOnAroundMe";
    String EVENT_SPEND_ONLINE = "SpendOnOnline";

    //not implemented yet
    String EVENT_CANCEL_DROPBIT_PRESSED = "CancelDropBit";

    // Application
    String EVENT_APP_OPEN = "AppOpen";

    // Sharing
    String EVENT_SHARE_VIA_TWITTER = "ShareViaTwitter";
    String EVENT_SHARE_NEXT_TIME = "ShareNextTime";
    String EVENT_NEVER_SHARE = "ShareNever";

    // PROPERTIES
    String PROPERTY_HAS_WALLET = "Has Wallet";
    String PROPERTY_HAS_WALLET_BACKUP = "Has Backup";
    String PROPERTY_PHONE_VERIFIED = "Phone Verified";
    String PROPERTY_HAS_BTC_BALANCE = "Has BTC Balance";
    String PROPERTY_HAS_SENT_DROPBIT = "Has Sent DropBit";
    String PROPERTY_HAS_SENT_ADDRESS = "Has Sent";
    String PROPERTY_HAS_RECEIVED_DROPBIT = "Has Received DropBit";
    String PROPERTY_HAS_RECEIVED_ADDRESS = "Has Received";

    Analytics start();

    void flush();

    @NonNull
    void onActivityStop(Activity activity);

    @NonNull
    void trackFragmentStop(Fragment fragment);

    @NonNull
    void trackEvent(String event);

    @NonNull
    void trackEvent(String event, JSONObject properties);

    @NonNull
    void trackButtonEvent(String event);

    @NonNull
    void setUserProperty(String propertyHasWallet, boolean value);
}
