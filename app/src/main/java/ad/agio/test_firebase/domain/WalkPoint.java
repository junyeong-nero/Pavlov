package ad.agio.test_firebase.domain;

import com.google.android.libraries.places.api.model.Place;

public class WalkPoint {

    public String name = "장소이름";
    public String id = "장소아이디";
    public double lat = -1;
    public double lng = -1;
    public String address = "주소";

    public WalkPoint() {

    }

    public WalkPoint(Place place) {
        this.lat = place.getLatLng().latitude;
        this.lng = place.getLatLng().longitude;
        this.name = place.getName();
        this.address = place.getAddress();
    }
}
