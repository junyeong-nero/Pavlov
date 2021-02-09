package ad.agio.test_firebase.controller;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class AuthController {
    public static String TAG = "AuthController";
    private final FirebaseAuth mAuth;

    public AuthController() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public String getUid() {
        checkValidUser();
        return mAuth.getCurrentUser().getUid();
    }

    public boolean isAuth() {
        return mAuth.getCurrentUser() != null;
    }
    public void signOut() {
        mAuth.signOut();
    }

    public void checkValidUser() {
        try {
            if (!isAuth())
                throw new IllegalAccessException("checkValidUser: it is not valid user");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void LOGGING(String content) {
        Log.d(TAG, content);
    }
}
