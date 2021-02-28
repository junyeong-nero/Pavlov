package ad.agio.test_firebase.domain;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;

public class Time {
    public int year = -1;
    public int month = -1;
    public int date = -1;
    public int hour = -1;
    public int minute = -1;

    public Time() {
        // no argument constructor required
    }

    public Time(Calendar cal) {
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH) + 1;
        this.date = cal.get(Calendar.DATE);
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
    }

    @NonNull
    @Override
    public String toString() {
        return month + "월 " + date + "일 " + hour + "시 " + minute + "분 ";
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        return gson.toJson(this);
    }
}
