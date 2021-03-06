package ad.agio.test_firebase.domain;

import androidx.annotation.NonNull;

public class Meeting {
    public WalkPoint place = new WalkPoint();
    public Time time = new Time();

    public Meeting() {
        // no argument constructor required
    }

    @NonNull
    @Override
    public String toString() {
        return "약속장소 : " + place.name + "\n"
                + "약속시간 : " + time.toString();
    }
}
