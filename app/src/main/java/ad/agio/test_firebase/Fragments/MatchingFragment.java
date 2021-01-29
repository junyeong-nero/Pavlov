package ad.agio.test_firebase.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Optional;

import ad.agio.test_firebase.Activities.ChatActivity;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
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

        matchController.setSendListener(new MatchController.EventListener() {
            @Override
            public void send(ArrayList<User> list) {
                list.forEach(user -> Log.d(TAG, user.getUid()));
                Optional<User> any = list.stream().findAny();
                if (currentUser != null && !any.isPresent()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("그곳에는 아무도 없었다.")
                            .setMessage("죄송해요. 매칭을 하는 사람이 없어요!")
                            .setPositiveButton("미안해요", (dialog, which) -> {
                                Intent chat = new Intent(requireContext(), ChatActivity.class);
                                chat.putExtra("receiver", currentUser.getUid());
                                chat.putExtra("sender", any.get().getUid());
                                startActivity(chat);
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("매칭성공")
                            .setMessage(any.toString())
                            .setPositiveButton("좋아요", (dialog, which) -> {

                            })
                            .show();
                }
            }

        });

        binding.buttonMatch.setOnClickListener(v -> {
            if (currentUser != null) {
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
            }
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