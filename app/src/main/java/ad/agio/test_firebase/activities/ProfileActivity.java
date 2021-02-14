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

//        binding.buttonMenu.setOnLongClickListener(v -> {
//            finish();
//            authController.signOut();
//            return true;
//        });

        if(!authController.isAuth())
            binding.buttonMenu.setVisibility(View.GONE);

        Intent intent = getIntent();
        // boolean : isReceiving
        // JSON String : user
        // String : chatId

        if(intent.hasExtra("type")) {
            String type = intent.getStringExtra("type");
            assert type != null;
            switch (type) {
                case "home":
                    fragmentTransaction.add(R.id.fragment_container,
                            new OtherProfileFragment(
                                    intent.getBooleanExtra("isMatching", false),
                                    new Gson().fromJson(intent.getStringExtra("user"), User.class),
                                    intent.getStringExtra("chatId")
                            ),
                            "OtherProfileFragmentHome"
                    ).commit();
                    break;

                case "search":
                    fragmentTransaction.add(R.id.fragment_container,
                            new OtherProfileFragment(
                                    intent.getBooleanExtra("isMatching", false),
                                    new Gson().fromJson(intent.getStringExtra("user"), User.class),
                                    intent.getStringExtra("chatId")
                            ),
                            "OtherProfileFragmentSearch"
                    ).commit();
                    break;

                case "appoint": // TODO 수락하는 fragment 기능 추가해야됨.
                    fragmentTransaction.add(R.id.fragment_container,
                            new OtherProfileFragment(
                                    intent.getBooleanExtra("isMatching", true),
                                    new Gson().fromJson(intent.getStringExtra("user"), User.class),
                                    intent.getStringExtra("chatId")
                            ),
                            "OtherProfileFragmentAppoint"
                    ).commit();
                    break;
            }
        } else {
            showMyProfile();
        }
    }

    private void showMyProfile() {
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