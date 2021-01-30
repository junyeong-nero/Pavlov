package ad.agio.test_firebase.utils;

import java.util.Random;

public class Utils {
    static public String randomWord() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            builder.append(new Random().nextInt(26) + 'A');
        }
        return builder.toString();
    }
}
