package ad.agio.test_firebase.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import ad.agio.test_firebase.Activities.RegisterActivity;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.DataController;
import ad.agio.test_firebase.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private void _log(String text) {
        Log.d("LoginFragment", text);
    }

    private FirebaseAuth mAuth;
    private FragmentLoginBinding binding;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(v ->
            loginAuth(binding.etEmail.getText().toString(),
                    binding.etPw.getText().toString())
        );

        binding.buttonRegister.setOnClickListener(v ->
                startActivity(new Intent(getContext(), RegisterActivity.class))
        );

        DataController dataController = new DataController(getContext());
        if (dataController.readData("auto_save").equals("true")) {
            binding.checkbox.setChecked(true);
            binding.etEmail.setText(dataController.readData("email"));
            binding.etPw.setText(dataController.readData("password"));
        }
    }

    private void loginAuth(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        _log("signInWithEmail:success");
                        loginSuccess(email, password);
                    } else {
                        // If sign in fails, display a message to the user.
                        _log("signInWithEmail:failure");
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loginSuccess(String email, String password) {
        DataController dataController = new DataController(getContext());

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

        // menu button visible
        requireActivity().findViewById(R.id.button_menu).setVisibility(View.VISIBLE);

        // Fragment change
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_enter,
                R.anim.fragment_fade_exit);
        fragmentTransaction.replace(R.id.fragment_container,
                new ProfileFragment(), "ProfileFragment").commit();
    }
}