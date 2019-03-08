package com.coinninja.coinkeeper.util;

import android.annotation.SuppressLint;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;

@SuppressLint("SimpleDateFormat")
@CoinkeeperApplicationScope
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final SimpleDateFormat dateFormat;
    private final Date date;

    @Inject
    public DateUtil() {
        dateFormat = new SimpleDateFormat(DEFAULT_PATTERN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        date = new Date();
    }

    public long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }

    public String getCurrentTimeFormatted() {
        return format(System.currentTimeMillis());
    }

    public String format(long millis) {
        date.setTime(millis);
        return dateFormat.format(date);
    }


    public static long RFC3339_TO_EPOCH_TIME(String rfcTime) {
        DateFormatUtil dateFormatUtil = new DateFormatUtil(DateFormatUtil.SERVER_DATE_FORMAT, false);
        Long time = dateFormatUtil.parseDateToMilliseconds(rfcTime);
        if (time < 1) {
            DateFormatUtil dateFormatUtil_Backup = new DateFormatUtil(DateFormatUtil.SERVER_DATE_FORMAT_BACKUP, false);
            time = dateFormatUtil_Backup.parseDateToMilliseconds(rfcTime);
        }
        return time;
    }
}
