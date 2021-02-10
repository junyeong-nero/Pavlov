package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.concurrent.atomic.AtomicReference;

import ad.agio.test_firebase.Fragments.LoginFragment;
import ad.agio.test_firebase.Fragments.OtherProfileFragment;
import ad.agio.test_firebase.Fragments.ProfileFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityProfileBinding;
import ad.agio.test_firebase.domain.User;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AuthController authController;
    private UserController userController;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        authController = new AuthController();
        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSignout.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment(), "LoginFragment");
            authController.signOut();
        });

        Intent intent = getIntent();
        if (intent.hasExtra("isMatching") && intent.hasExtra("user")) {
            Gson gson = new Gson();
            fragmentTransaction.add(R.id.fragment_container, new OtherProfileFragment(
                    intent.getBooleanExtra("isMatching", false),
                    gson.fromJson(intent.getStringExtra("user"), User.class),
                    intent.getStringExtra("chatId"))
            ).commit();
        } else {
            if (authController.isAuth()) { // 로그인되어있는 상태 firestore 부터 정보를 불러오는건 느리다.
                fragmentTransaction
                        .add(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                        .commit();
            } else { // 로그인 fragment 실행
                fragmentTransaction
                        .add(R.id.fragment_container, new LoginFragment(), "LoginFragment")
                        .commit();
            }
        }
    }


}