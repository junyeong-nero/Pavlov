package ad.agio.test_firebase.controller;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class AuthController {
    private void _log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private final FirebaseAuth mAuth;

    public AuthController() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * 사용자의 UID를 가져옵니다.
     * @return
     */
    public String getUid() {
        _log("getUID");
        if(isAuth())
            return mAuth.getCurrentUser().getUid();
        else
            return "";
    }

    public boolean isAuth() {
        return mAuth.getCurrentUser() != null;
    }
    public void signOut() {
        mAuth.signOut();
    }

    /**
     * 사용자가 로그인 되어있는지 확인합니다.
     */
    public void checkValidUser() {
        _log("checkValidUsers");
        try {
            if (!isAuth())
                throw new IllegalAccessException("checkValidUser: it is not valid user");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
