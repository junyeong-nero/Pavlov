package ad.agio.test_firebase.domain;

import androidx.annotation.NonNull;

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
        set(cal);
    }

    /**
     * 캘린더 데이터를 이용하여 Time 객체를 초기화합니다.
     * @param calendar Calendar
     */
    public void set(Calendar calendar) {
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.date = calendar.get(Calendar.DATE);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    /**
     * 시간을 더합니다
     * @param field 시간을 더하는 필드
     * @param num 더하는 시간의 크기
     */
    public void add(int field, int num) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        cal.add(field, num);
        set(cal);
    }

    @NonNull
    @Override
    public String toString() {
        return month + "월 " + date + "일 " + hour + "시 " + minute + "분 ";
    }
}
