package com.coinninja.coinkeeper.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

public class DateFormatUtil {

    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    public static final String SERVER_DATE_FORMAT_BACKUP = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String DEFAULT_DATE_FORMAT = "MMMM d, yyyy hh:mma";
    private final Calendar calendar;
    private final DateFormatSymbols symbols;
    private final SimpleDateFormat formatter;

    @Inject
    public DateFormatUtil() {
        this(DEFAULT_DATE_FORMAT, true);
    }

    public DateFormatUtil(String format, boolean local) {
        calendar = Calendar.getInstance();
        symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        if (local) {
            formatter = new SimpleDateFormat(format, Locale.getDefault());
        } else {
            formatter = new SimpleDateFormat(format);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        formatter.setDateFormatSymbols(symbols);
    }


    public String formatTime(long millis) {
        calendar.setTimeInMillis(millis);
        return formatter.format(calendar.getTime());
    }

    public Date parseDate(String serverTime) {
        try {
            Date date = formatter.parse(serverTime);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public Long parseDateToMilliseconds(String serverTime) {
        Date date = parseDate(serverTime);
        if (date != null) {
            return date.getTime();
        }

        return 0l;
    }
}
