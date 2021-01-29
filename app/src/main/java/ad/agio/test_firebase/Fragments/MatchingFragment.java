package ad.agio.test_firebase.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.Optional;

import ad.agio.test_firebase.Activities.ChatActivity;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentMatchingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private User currentUser;
    private boolean isMatching = false;
    private UserController userController;
    private MatchController matchController;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userController = new UserController();
        matchController = new MatchController();

        userController.readMe(user -> currentUser = user);

        matchController.setChangeListener(new MatchController.EventListener() {
            @Override
            public void change(ArrayList<User> list) {
                list.forEach(user -> Log.d(TAG, user.getId()));
                User any = list.stream().findAny().get();
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("매칭성공")
                        .setMessage(any.toString())
                        .setPositiveButton("좋아요", (dialog, which) -> {
                            Intent chat = new Intent(requireContext(), ChatActivity.class);
                            chat.putExtra("receiver", currentUser.getId());
                            chat.putExtra("sender", any.getId());
                            startActivity(chat);
                        })
                        .show();
            }

        });

        binding.buttonMatch.setOnClickListener(v -> {
            if(!isMatching) {
                binding.textIndicator.setText("매칭중..");
                matchController.addMatcher(currentUser);
                // matchController.findAll(list -> list.forEach(user -> Log.d(TAG, user.getUserName())));
                matchController.startMatching();
            } else {
                binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                matchController.removeMatcher(currentUser);
                matchController.pauseMatching();
            }
            isMatching = !isMatching;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        matchController.pauseMatching();
        matchController = null;
        userController = null;
    }
}