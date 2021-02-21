package ad.agio.test_firebase.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;

import ad.agio.test_firebase.Fragments.LoginFragment;
import ad.agio.test_firebase.Fragments.OtherProfileFragment;
import ad.agio.test_firebase.Fragments.ProfileFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.databinding.ActivityProfileBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.RequestCodes;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AuthController authController;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        authController = new AuthController();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonMenu.setOnClickListener(v ->
            startActivityForResult(new Intent(ProfileActivity.this, MenuActivity.class),
                    RequestCodes.MENU_ACTIVITY)
        );

        if(!authController.isAuth())
            binding.buttonMenu.setVisibility(View.GONE);

        Intent intent = getIntent();
        // boolean : isReceiving
        // JSON String : user
        // String : chatId

        // type 이 있다면 다른 사람의 프로필을 띄운다.
        if(intent.hasExtra("type")) {
            String type = intent.getStringExtra("type");
            assert type != null;
            fragmentTransaction.add(R.id.fragment_container,
                    new OtherProfileFragment(
                            type,
                            intent.getBooleanExtra("isReceiving", false),
                            new Gson().fromJson(intent.getStringExtra("user"), User.class),
                            intent.getStringExtra("chatId")
                    ),
                    "OtherProfileFragment"
            ).commit();
        } else {
            // type 이 없다면 자신의 프로필을 띄운다
            showMyProfile();
        }
    }

    private void showMyProfile() {
        if (authController.isAuth()) { // 로그인되어있는 상태 Firestore 부터 정보를 불러오는건 느리다.
            fragmentTransaction
                    .add(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                    .commit();
        } else { // 로그인 fragment 실행
            fragmentTransaction
                    .add(R.id.fragment_container, new LoginFragment(), "LoginFragment")
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.MENU_ACTIVITY:
                finish();
                break;
        }
    }
}