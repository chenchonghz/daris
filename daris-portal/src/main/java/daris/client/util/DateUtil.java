package daris.client.util;

import java.util.Date;

public class DateUtil {

    @SuppressWarnings("deprecation")
    public static Date clearTime(Date date) {
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        long time = date.getTime();
        return new Date(time - time % 1000);
    }

    public static Date copyDate(Date date, boolean dateOnly) {
        if (dateOnly) {
            return clearTime(date);
        } else {
            return new Date(date.getTime());
        }
    }

}
