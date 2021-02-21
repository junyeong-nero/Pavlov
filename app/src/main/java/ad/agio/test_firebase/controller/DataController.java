package ad.agio.test_firebase.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataController {
    final static public String TAG = "DataController";
    public void LOGGING(String text) {
        Log.d(TAG, text);
    }

    private Context ctx;
    private SharedPreferences preferences;

    public DataController(Context context) {
        this.ctx = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void saveData(String tag, String data) {
        preferences.edit().putString(tag, data).apply();
    }

    public String readData(String tag) {
        return preferences.getString(tag, "");
    }
    public void deleteData(String tag) {
        preferences.edit().remove(tag).apply();
    }
}
