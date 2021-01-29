package ad.agio.test_firebase.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityRegisterBinding;
import ad.agio.test_firebase.domain.User;

public class RegisterActivity extends AppCompatActivity {

    final static public String TAG = "RegisterActivity";

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });

        binding.buttonRegister.setOnClickListener(v -> {
            mUser = new User();
            mUser.setEmail(binding.etEmail.getText().toString().trim());
            mUser.setAge(Integer.parseInt(binding.etAge.getText().toString().trim()));
            mUser.setUserName(binding.etUserName.getText().toString().trim());

            createAuth(mUser.getEmail(), binding.etPw.getText().toString().trim());
        });
    }

    private void createAuth(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        Toast.makeText(getApplicationContext(), "Authentication success!",
                                Toast.LENGTH_SHORT).show();
                        UserController userController = new UserController();
                        mUser.setId(mAuth.getCurrentUser().getUid()); // UID!
                        userController.writeNewUser(mUser);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}