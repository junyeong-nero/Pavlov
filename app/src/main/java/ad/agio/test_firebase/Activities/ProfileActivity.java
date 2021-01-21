package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import ad.agio.test_firebase.Fragments.LoginFragment;
import ad.agio.test_firebase.Fragments.ProfileFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(mAuth.getCurrentUser() != null) { // 로그인되어있는 상태
            ProfileFragment fragment = new ProfileFragment();
            fragmentTransaction.add(R.id.fragment_container, fragment).commit();
        } else { // 로그인 fragment 실행
            LoginFragment fragment = new LoginFragment();
            fragmentTransaction
                    .add(R.id.fragment_container, fragment).commit();
        }
    }
}