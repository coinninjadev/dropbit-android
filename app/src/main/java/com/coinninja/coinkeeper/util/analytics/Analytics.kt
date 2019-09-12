package com.coinninja.coinkeeper.util.analytics

import android.app.Activity
import androidx.fragment.app.Fragment

import org.json.JSONObject

interface Analytics {

    fun start(): Analytics

    fun flush()

    fun onActivityStop(activity: Activity)

    fun trackFragmentStop(fragment: Fragment)

    fun trackEvent(event: String)

    fun trackEvent(event: String, properties: JSONObject)

    fun trackButtonEvent(event: String)

    fun setUserProperty(propertyName: String, value: Boolean)
    fun setUserProperty(propertyName: String, value: String)
    fun setUserProperty(propertyName: String, value: Long)

    companion object {
        const val OS = "Android"

        // EVENTS

        // -- BROADCASTING
        const val EVENT_BROADCAST_STARTED = "BroadcastStart"
        const val EVENT_BROADCAST_COMPLETE = "BroadcastSuccess"
        const val EVENT_BROADCAST_FAILED = "BroadcastFailure"
        const val EVENT_CONTACT_SEND = "ContactSend"
        const val EVENT_DROPBIT_SEND = "DropBitSend"
        const val EVENT_BROADCAST_TO_ADDRESS = "AddressPayment"
        // -- BROADCASTING -- Pending
        const val EVENT_PENDING_TRANSACTION_FAILED = "TxVerificationFailure"
        const val EVENT_PENDING_DROPBIT_SEND_FAILED = "DropBitSendFailure  "
        const val EVENT_PENDING_DROPBIT_RECEIVE_FAILED = "DropBitReceiveFailure"
        // -- PHONE VERIFICATION
        const val EVENT_PHONE_VERIFICATION_SUCCESSFUL = "PhoneVerified"
        const val EVENT_PHONE_VERIFICATION_SKIPPED = "SkipPhoneVerification"
        const val EVENT_PHONE_AUTO_DEVERIFIED = "PhoneAutoDeverified"
        // -- WALLET
        const val EVENT_WALLET_DELETE = "DeleteWallet" //- user deletes wallet
        const val EVENT_WALLET_RESTORE = "RestoreWallet"
        const val EVENT_WALLET_BACKUP_SUCCESSFUL = "WordsBackedUp"
        const val EVENT_WALLET_CREATE = "CreateWallet" //- user creates a wallet
        // CONTACTS PICK SCREEN
        const val EVENT_INVITE_WHATIS_DROPBIT = "WhatIsDropBit" //- user taps what is dropbit link
        const val EVENT_DROPBIT_SEND_BTN = "DropBitPressed" //- user taps dropbit send
        const val EVENT_CONTACT_SEND_BTN = "ContactPressed" //- user taps contact send    String EVENT_DROPBIT_SEND_BTN = "DropBitSend"; //- user taps dropbit send
        // TRANSACTION
        const val EVENT_DROPBIT_INITIATED = "DropBitInitiated"
        const val EVENT_DROPBIT_INITIATION_FAILED = "DropBitInitiationFailed"
        const val EVENT_DROPBIT_ADDRESS_PROVIDED = "DropBitAddressProvided"
        const val EVENT_DROPBIT_COMPLETED = "DropBitCompleted"
        const val EVENT_TRANSACTION_RETRY = "RetryFailedPayment"
        // -- Button Pressed --
        const val EVENT_BUTTON_SUFFIX = "Btn"
        // -- Button Pressed -- Payment Bar
        const val EVENT_BUTTON_BALANCE_HISTORY = "BalanceHistory" // - User taps History button
        const val EVENT_BUTTON_REQUEST = "Request" // - User taps request button on payment bar
        const val EVENT_BUTTON_SCAN_QR = "ScanQR" //- User taps Scan QR button on payment bar
        const val EVENT_BUTTON_PAY = "Pay" //- User taps pay button on payment bar
        // -- Button Pressed -- DrawerBar
        const val EVENT_BUTTON_HISTORY = "History" // - User taps History button
        const val EVENT_BUTTON_SETTINGS = "Settings" // - User taps settings button
        const val EVENT_BUTTON_SPEND = "Spend" // - User taps spend button
        const val EVENT_BUTTON_SUPPORT = "Support" // - User taps support button
        // -- Button Pressed -- Request fragment
        const val EVENT_BUTTON_SEND_REQUEST = "SendRequest" // - User taps send request button of QR code
        // -- Button Pressed -- Pay fragment
        const val EVENT_PAY_SCREEN_LOADED = "PayScreenLoaded"
        const val EVENT_BUTTON_CONTACTS = "Contacts" // - user taps contacts button
        const val EVENT_BUTTON_SCAN = "Scan" // - user taps scan button on the pay dialog screen
        const val EVENT_BUTTON_PASTE = "Paste" // - user taps paste button
        const val EVENT_CONFIRM_SCREEN_LOADED = "ConfirmScreenLoaded"
        // -- Button Pressed -- History
        const val EVENT_BUTTON_SHARE_TRANS_ID = "ShareTransID" // - user taps share transaction ID button
        //json keys
        const val EVENT_BROADCAST_JSON_KEY_BLOCK_STREAM_CODE = "BlockStreamCode"
        const val EVENT_BROADCAST_JSON_KEY_BLOCK_STREAM_MSG = "BlockStreamMessage"
        const val EVENT_BROADCAST_JSON_KEY_LIB_CODE = "LibCode"
        const val EVENT_BROADCAST_JSON_KEY_LIB_MSG = "LibMsg"
        const val EVENT_BROADCAST_JSON_KEY_BLOCK_CODE = "BlockChainCode"
        const val EVENT_BROADCAST_JSON_KEY_BLOCK_MSG = "BlockChainMsg"
        const val EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL = "Message"
        const val EVENT_MEMO_JSON_KEY_DID_SHARE = "SharingEnabled"
        //check-in
        const val EVENT_BROADCAST_CHECK_IN_FAIL = "CheckInFail"
        //SHARED MEMOS
        const val EVENT_SENT_SHARED_PAYLOAD = "SharedPayloadSent"

        //BACKUP / Recovery Words
        const val EVENT_VIEW_RECOVERY_WORDS = "ViewWords"

        //Transaction history empty state
        const val EVENT_GET_BITCOIN = "GetBitcoin"
        const val EVENT_LEARN_BITCOIN = "LearnBitcoin"
        const val EVENT_SPEND_BITCOIN = "SpendBitcoin"

        //Buying bitcoin
        const val EVENT_BUY_BITCOIN_CREDIT_CARD = "BuyBitcoinWithCreditCard"
        const val EVENT_BUY_BITCOIN_GIFT_CARD = "BuyBitcoinWithGiftCard"
        const val EVENT_BUY_BITCOIN_AT_ATM = "BuyBitcoinAtATM"

        //Spending bitcoin
        const val EVENT_SPEND_GIFT_CARDS = "SpendOnGiftCards"
        const val EVENT_SPEND_AROUND_ME = "SpendOnAroundMe"
        const val EVENT_SPEND_ONLINE = "SpendOnOnline"

        //not implemented yet
        const val EVENT_CANCEL_DROPBIT_PRESSED = "CancelDropBit"

        // Application
        const val EVENT_APP_OPEN = "AppOpen"

        // Sharing
        const val EVENT_SHARE_VIA_TWITTER = "ShareViaTwitter"
        const val EVENT_SHARE_NEXT_TIME = "ShareNextTime"
        const val EVENT_NEVER_SHARE = "ShareNever"

        // Dropbit Me
        const val EVENT_DROPBIT_ME_DISABLED = "DropBitMeDisabled"
        const val EVENT_DROPBIT_ME_ENABLED = "DropBitMeReenabled"

        // TWITTER
        const val EVENT_TWEET_VIA_DROPBIT = "SendTweetViaDropBit"
        const val EVENT_TWEET_MANUALLY = "SendTweetViaDropBit"
        const val EVENT_TWITTER_SEND_SUCCESSFUL = "TwitterSendComplete"
        const val EVENT_TWITTER_VERIFIED = "TwitterVerified"

        // CHARTS / NEWS
        const val EVENT_CHARTS_OPENED = "ChartsOpened"
        const val EVENT_NEWS_ARTICLE_OPENED = "NewsArticleOpened"

        // PROPERTIES
        const val PROPERTY_HAS_WALLET = "Has Wallet"
        const val PROPERTY_HAS_WALLET_BACKUP = "Backed Up"
        const val PROPERTY_PHONE_VERIFIED = "Phone Verified"
        const val PROPERTY_HAS_BTC_BALANCE = "Has BTC Balance"
        const val PROPERTY_RELATIVE_WALLET_RANGE = "Relative Wallet Range"
        const val PROPERTY_HAS_SENT_DROPBIT = "Has Sent DropBit"
        const val PROPERTY_HAS_SENT_ADDRESS = "Has Sent"
        const val PROPERTY_HAS_RECEIVED_DROPBIT = "Has Received DropBit"
        const val PROPERTY_HAS_RECEIVED_ADDRESS = "Has Received"
        const val PROPERTY_HAS_DROPBIT_ME_ENABLED = "DropBitMe Enabled"
        const val PROPERTY_TWITTER_VERIFIED = "Twitter Verified"
        const val PROPERTY_WALLET_VERSION = "WalletVersion"
        const val PROPERTY_PLATFORM = "platform"
    }
}
