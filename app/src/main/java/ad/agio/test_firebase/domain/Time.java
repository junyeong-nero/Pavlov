package ad.agio.test_firebase.domain;

import java.util.Calendar;

public class Time {
    public int year;
    public int month;
    public int date;
    public int hour;
    public int minute;

    public Time(Calendar cal) {
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH);
        this.date = cal.get(Calendar.DATE);
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
    }
}
