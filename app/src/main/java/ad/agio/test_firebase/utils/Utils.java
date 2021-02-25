package ad.agio.test_firebase.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Random;

public class Utils {

    static public String randomWord() { // TODO 정말 희박한 가능성이지만 chatId가 중복될 가능성이 있다.
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            builder.append((char)(new Random().nextInt(26) + 'A'));
        }
        return builder.toString();
    }

    static public boolean checkInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
