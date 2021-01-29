package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;

import ad.agio.test_firebase.Fragments.LoginFragment;
import ad.agio.test_firebase.Fragments.MatchingFragment;
import ad.agio.test_firebase.Fragments.ProfileFragment;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityMatchBinding;

public class MatchActivity extends AppCompatActivity {

    private ActivityMatchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AuthController controller = new AuthController();
        controller.checkValidUser();

        binding.buttonBack.setOnClickListener(v -> finish());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new MatchingFragment()).commit();
    }
}