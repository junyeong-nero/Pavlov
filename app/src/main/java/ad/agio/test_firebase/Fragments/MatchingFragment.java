package ad.agio.test_firebase.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.stream.Collector;
import java.util.stream.Stream;

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

        binding.buttonMatch.setOnClickListener(v -> {
            if (currentUser != null) {
                if(!isMatching) {
                    binding.textIndicator.setText("매칭중..");
                    matchController.startMatching(
                            user -> true,
                            list -> {
                                CharSequence[] items = new CharSequence[list.size()];
                                for (int i = 0; i < items.length; i++)
                                    items[i] = list.get(i).getUserName();

                                new AlertDialog.Builder(requireContext())
                                        .setTitle("매칭성공")
                                        .setItems(items, (dialog, which) -> {
                                            matchController.match(list.get(which));
                                        })
                                        .setNegativeButton("안할래용", (dialog, which) -> {

                                        })
                                        .show();
                            });
                } else {
                    binding.textIndicator.setText("매칭하려면 밑의 버튼을 눌러주세요");
                    matchController.pauseReceiving();
                }
                isMatching = !isMatching;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        matchController.stopReceiving();
        matchController = null;
        userController = null;
    }
}