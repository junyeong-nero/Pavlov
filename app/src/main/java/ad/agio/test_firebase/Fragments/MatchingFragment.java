package ad.agio.test_firebase.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import ad.agio.test_firebase.Activities.RegisterActivity;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.DataController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentLoginBinding;
import ad.agio.test_firebase.databinding.FragmentMatchingBinding;
import ad.agio.test_firebase.domain.User;

public class MatchingFragment extends Fragment {

    final static public String TAG = "LoginFragment";

    private FirebaseAuth mAuth;
    private FragmentMatchingBinding binding;

    public MatchingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentMatchingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private User currentUser;
    private boolean isMatching = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserController userController = new UserController();
        MatchController matchController = new MatchController();

        userController.readMe(user -> {
            currentUser = user;
        });

        matchController.setChangeListener(new MatchController.EventListener() {
            @Override
            public void change(ArrayList<User> list) {
                list.forEach(user -> Log.d(TAG, user.getId()));
            }
        });

        binding.buttonMatch.setOnClickListener(v -> {
            if(!isMatching) {
                matchController.addMatcher(currentUser);
                // matchController.findAll(list -> list.forEach(user -> Log.d(TAG, user.getUserName())));
                matchController.startMatching();
            } else {
                matchController.removeMatcher(currentUser);
                matchController.pauseMatching();
            }
            isMatching = !isMatching;
        });
    }
}