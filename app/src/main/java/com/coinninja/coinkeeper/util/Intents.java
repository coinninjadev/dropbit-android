package com.coinninja.coinkeeper.util;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.BuildConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class Intents {

    /* CONSTS */
    public static final String DB_NAME = "coin-ninja-db";
    public static final int QR_WIDTH = 300;
    public static final int QR_HEIGHT = 300;
    public static final long PENDING_TRANSITION_LIFE_LIMIT_SECONDS = 180;//180 secs = 3minuets
    public static final double MIN_FEE_FLOOR = 5d;
    public static final long LOCK_DURRATION = 300000L;
    public static final long MAX_DOLLARS_SENT_THROUGH_CONTACTS = 10000L;
    public static final Map<String, String> SUPPORT_LINKS;
    public static final Uri URI_WHERE_TO_BUY = Uri.parse("https://coinninja.com/news/where-can-i-spend-my-bitcoin");
    public static final Uri URI_WHAT_IS_DROPBIT = Uri.parse("https://dropbit.app/dropbit");
    public static final Uri URI_LEARN_ABOUT_BITCOIN = Uri.parse("https://coinninja.com/learnbitcoin");
    public static final Uri URI_WHY_BITCOIN = Uri.parse("https://coinninja.com/whybitcoin");
    public static final Uri URI_RECOVERY_WORDS = Uri.parse("https://coinninja.com/recoverywords");
    public static final Uri URI_SHARED_MEMOS = Uri.parse("https://dropbit.app/tooltips/sharedmemos");

    public static final long THIRTY_SECONDS = 1000 * 30;

    /* PREFERENCES */
    public static final String PREFERENCES_LAST_CN_MESSAGES_TIME = "PREFERENCES_LAST_CN_MESSAGES_TIME";
    public static final long PREFERENCES_LAST_CN_MESSAGES_DEFAULT_TIME = 0l;

    static {
        Map<String, String> aMap = new LinkedHashMap<>();
        aMap.put("FAQs", "https://dropbit.app/faq");
        aMap.put("Contact Us", "https://dropbit.app/faq#contact");
        aMap.put("Terms of Use", "https://dropbit.app/termsofuse");
        aMap.put("Privacy Policy", "https://dropbit.app/privacypolicy");
        SUPPORT_LINKS = aMap;
    }

    /* PATTERNS */
    public static final String BITCOIN_ADDRESS_PATTERN = "((?:bc1|[13])[a-zA-HJ-NP-Z0-9]{25,39}(?![a-zA-HJ-NP-Z0-9]))";
    public static final String BITCOIN_URI_PATTERN = BITCOIN_ADDRESS_PATTERN + "(\\?.*&?(?:amount=)((?:[0-9]{0,8})(?:\\.[0-9]{1,8})?))?";

    /* REQUESTS */
    public static int REQUEST_QR_ACTIVITY_SCAN = 221;
    public static int REQUEST_QR_FRAGMENT_SCAN = 222;
    public static final int REQUEST_PERMISSIONS_CAMERA = 524;


    /* RESULTS */
    public static final int RESULT_SCAN_OK = 221;
    public static final int RESULT_SCAN_ERROR = 400;

    /* ACTIONS */
    public static final String ACTION_DEVERIFY_PHONE_NUMBER_FAILED = BuildConfig.APPLICATION_ID + ".ACTION_DEVERIFY_PHONE_NUMBER_FAILED";
    public static final String ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED = BuildConfig.APPLICATION_ID + ".ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED";
    public static final String ACTION_BTC_PRICE_UPDATE = BuildConfig.APPLICATION_ID + ".ACTION_BTC_PRICE_UPDATE";
    public static final String ACTION_TRANSACTION_FEE_UPDATE = BuildConfig.APPLICATION_ID + ".ACTION_TRANSACTION_FEE_UPDATE";
    public static final String ACTION_WALLET_SYNC_COMPLETE = BuildConfig.APPLICATION_ID + ".ACTION_WALLET_SYNC_COMPLETE";
    public static final String ACTION_PHONE_VERIFICATION__CODE_SENT = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION__CODE_SENT";
    public static final String ACTION_PHONE_VERIFICATION__INVALID_CODE = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION__INVALID_CODE";
    public static final String ACTION_PHONE_VERIFICATION__EXPIRED_CODE = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION__EXPIRED_CODE";
    public static final String ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION_RATE_LIMIT_ERROR";
    public static final String ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR";
    public static final String ACTION_PHONE_VERIFICATION__SUCCESS = BuildConfig.APPLICATION_ID + "ACTION_PHONE_VERIFICATION__SUCCESS";
    public static final String ACTION_ON_WALLET_DELETED = BuildConfig.APPLICATION_ID + ".ACTION_ON_WALLET_DELETED";
    public static final String ACTION_INTERNAL_NOTIFICATION_UPDATE = BuildConfig.APPLICATION_ID + ".ACTION_INTERNAL_NOTIFICATION_UPDATE";
    public static final String ACTION_CANCEL_DROPBIT = BuildConfig.APPLICATION_ID + ".ACTION_CANCEL_DROPBIT";
    public static final String ACTION_DROPBIT__ERROR_RATE_LIMIT = BuildConfig.APPLICATION_ID + "ACTION_DROPBIT__ERROR_RATE_LIMIT";
    public static final String ACTION_DROPBIT__ERROR_UNKNOWN = BuildConfig.APPLICATION_ID + "ACTION_DROPBIT__ERROR_UNKNOWN";
    public static final String ACTION_CN_USER_ACCOUNT_UPDATED = BuildConfig.APPLICATION_ID + "ACTION_CN_USER_ACCOUNT_UPDATED";
    public static final String ACTION_ON_APPLICATION_FOREGROUND_STARTUP = BuildConfig.APPLICATION_ID + ".ACTION_ON_APPLICATION_FOREGROUND_STARTUP";
    public static final String ACTION_SAVE_RECOVERY_WORDS = BuildConfig.APPLICATION_ID + ".ACTION_SAVE_RECOVERY_WORDS";
    public static final String ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS = BuildConfig.APPLICATION_ID + ".ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS";
    public static final String ACTION_ON_SERVICE_CONNECTION_BOUNDED = BuildConfig.APPLICATION_ID + ".ACTION_ON_SERVICE_CONNECTION_BOUNDED";
    public static final String ACTION_VIEW_QR_CODE = BuildConfig.APPLICATION_ID + ".ACTION_VIEW_QR_CODE";
    public static final String ACTION_ON_USER_AUTH_SUCCESSFULLY = BuildConfig.APPLICATION_ID + ".ACTION_ON_USER_AUTH_SUCCESSFULLY";
    public static final String ACTION_WALLET_REGISTRATION_COMPLETE = BuildConfig.APPLICATION_ID + ".ACTION_WALLET_REGISTRATION_COMPLETE";
    public static final String ACTION_ON_APPLICATION_START = BuildConfig.APPLICATION_ID + ".ACTION_ON_APPLICATION_START";
    public static final String ACTION_WALLET_CREATED = BuildConfig.APPLICATION_ID + ".ACTION_WALLET_CREATED";
    public static final String ACTION_TRANSACTION_DATA_CHANGED = BuildConfig.APPLICATION_ID + ".ACTION_TRANSACTION_DATA_CHANGED";
    public static final String ACTION_WALLET_ADDRESS_RETRIEVED = BuildConfig.APPLICATION_ID + ".ACTION_WALLET_ADDRESS_RETRIEVED";


    /* EXTRAS */
    public static final String EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID";
    public static final String EXTRA_TRANSACTION_RECORD_ID = "EXTRA_TRANSACTION_RECORD_ID";
    public static final String EXTRA_CONTACT = "EXTRA_CONTACT";
    public static final String EXTRA_BITCOIN_PRICE = "EXTRA_BITCOIN_PRICE";
    public static final String EXTRA_TRANSACTION_FEE = "EXTRA_TRANSACTION_FEE";
    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";
    public static final String EXTRA_PHONE_NUMBER_CODE = "EXTRA_PHONE_NUMBER_CODE";
    public static final String EXTRA_AUTHORIZED_ACTION_MESSAGE = "EXTRA_AUTHORIZED_ACTION_MESSAGE";
    public static final String EXTRA_CONTACT_INVITE = "EXTRA_CONTACT_INVITE";
    public static final String EXTRA_CONTACT_INVITE_VALUE = "EXTRA_CONTACT_INVITE_VALUE";
    public static final String EXTRA_CONTACT_INVITE_FEE = "EXTRA_CONTACT_INVITE_FEE";
    public static final String EXTRA_CONTACT_INVITE_USD_VALUE = "EXTRA_CONTACT_INVITE_USD_VALUE";
    public static final String EXTRA_RECOVERY_WORDS = "EXTRA_RECOVERY_WORDS";
    public static final String EXTRA_NEXT = "EXTRA_NEXT";
    public static final String EXTRA_NEXT_BUNDLE = "EXTRA_NEXT_BUNDLE";
    public static final String EXTRA_QR_CODE_LOCATION = "EXTRA_QR_CODE_LOCATION";
    public static final String EXTRA_TEMP_QR_SCAN = "EXTRA_TEMP_QR_SCAN";
    public static final String EXTRA_SCANNED_DATA = "EXTRA_SCANNED_DATA";
    public static final String EXTRA_VIEW_STATE = "VIEW_STATE";
    public static final int EXTRA_VIEW = 1;
    public static final int EXTRA_BACKUP = 2;
    public static final int EXTRA_CREATE = 0;
    public static final String EXTRA_INVITATION_ID = "EXTRA_INVITATION_ID";
    public static final String EXTRA_COMPLETED_INVITE_DTO = "EXTRA_COMPLETED_INVITE_DTO";
    public static final String EXTRA_MEMO = "EXTRA_MEMO";
    public static final String EXTRA_BROADCAST_DTO = "EXTRA_BROADCAST_DTO";
    public static final String EXTRA_COMPLETED_BROADCAST_DTO = "EXTRA_COMPLETED_BROADCAST_DTO";
    public static final String EXTRA_INVITE_DTO = "EXTRA_COMPLETED_BROADCAST_DTO";
    public static final String EXTRA_PHONE_NUMBER_HASH = "EXTRA_PHONE_NUMBER_HASH";
    public static final String EXTRA_ADDRESS_LOOKUP_RESULT = "EXTRA_ADDRESS_LOOKUP_RESULT";

    /* CN Rest API KEYS */
    public static final String CN_API_ELASTIC_SEARCH_PLATFORM_ALL = "all";
    public static final String CN_API_ELASTIC_SEARCH_PLATFORM_ANDROID = "android";
    public static final String CN_API_ELASTIC_SEARCH_QUERY = "query";
    public static final String CN_API_ELASTIC_SEARCH_VERSION = "version";
    public static final String CN_API_ELASTIC_SEARCH_PARAMS = "params";
    public static final String CN_API_ELASTIC_SEARCH_SEMVER = "semver";
    public static final String CN_API_ELASTIC_SEARCH_ID = "id";
    public static final String CN_API_ELASTIC_SEARCH_SCRIPT = "script";
    public static final String CN_API_ELASTIC_SEARCH_GREATER_THAN = "gt";
    public static final String CN_API_ELASTIC_SEARCH_PUBLISH_TIME = "published_at";
    public static final String CN_API_ELASTIC_SEARCH_RANGE = "range";
    public static final String CN_API_ELASTIC_SEARCH_PLATFORM = "platform";
    public static final String CN_API_ELASTIC_SEARCH_TERMS = "terms";
    public static final String CN_API_CREATE_DEVICE_APPLICATION_KEY = "application";
    public static final String CN_API_CREATE_DEVICE_PLATFORM_KEY = "platform";
    public static final String CN_API_CREATE_DEVICE_UUID_KEY = "uuid";

    /* CN Rest API VALUES */
    public static final String CN_API_CREATE_DEVICE_APPLICATION_NAME = "DropBit";
    public static final String CN_API_CREATE_DEVICE_PLATFORM_ANDROID = "android";
    public static final String CN_API_CREATE_DEVICE_PLATFORM_IOS = "ios";
}
