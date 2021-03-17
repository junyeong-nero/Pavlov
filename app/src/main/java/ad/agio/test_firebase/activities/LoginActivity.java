package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import ad.agio.test_firebase.controller.DataController;
import ad.agio.test_firebase.databinding.ActivityLoginBinding;
import ad.agio.test_firebase.utils.GraphicComponents;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    public void init() {

        binding.buttonLogin.setOnClickListener(v ->
                login(binding.etEmail.getText().toString(),
                        binding.etPw.getText().toString())
        );

        binding.buttonRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        DataController dataController = new DataController(this);
        if (dataController.readData("auto_save").equals("true")) {
            binding.checkbox.setChecked(true);
            binding.etEmail.setText(dataController.readData("email"));
            binding.etPw.setText(dataController.readData("password"));
        }
    }

    /**
     * 로그인
     * @param email 이메일
     * @param password 비밀번호
     */
    private void login(String email, String password) {
        showLoading();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    dismissLoading();
                    if (task.isSuccessful()) {
                        log("signInWithEmail:success");
                        loginSuccess(email, password);
                    } else {
                        // If sign in fails, display a message to the user.
                        log("signInWithEmail:failure");
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 로그인 성공시 호출되는 함수
     * @param email 계정 이메일
     * @param password 계정 비밀번호
     */
    private void loginSuccess(String email, String password) {
        DataController dataController = new DataController(this);

        // email, password auto-save
        if(binding.checkbox.isChecked()) {
            dataController.saveData("email", email);
            dataController.saveData("password", password);
            dataController.saveData("auto_save", "true");
        } else {
            dataController.deleteData("email");
            dataController.deleteData("password");
            dataController.saveData("auto_save", "false");
        }

        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private AlertDialog dialog;

    private void showLoading() {
        GraphicComponents g = new GraphicComponents(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        ProgressBar p = new ProgressBar(this);
        p.setPadding(g.dp(20), g.dp(20), g.dp(20), g.dp(20));
        layout.addView(p, g.dp(100), g.dp(100));

        TextView t = new TextView(this);
        t.setText("요청중에요");
        t.setTextColor(Color.BLACK);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(g.dp(8), g.dp(8), g.dp(8), g.dp(8));
        layout.addView(t, WRAP_CONTENT, g.dp(100));

        dialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
        dialog.show();
    }

    private void dismissLoading() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}