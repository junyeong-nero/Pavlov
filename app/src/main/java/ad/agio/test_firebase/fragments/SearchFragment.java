package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.OtherProfileActivity;
import ad.agio.test_firebase.databinding.FragmentSearchBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Codes;

import static ad.agio.test_firebase.activities.HomeActivity.userController;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private void log(String s) {
        Log.e(this.getClass().getSimpleName(), s);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Predicate<User> condition = user -> user.getUserName()
                .contains(binding.editQuery.getText().toString());
        // 쿼리를 포함하고 있는 사람을 뽑는다.

        binding.button.setOnClickListener(v -> search(condition));
        binding.editQuery.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                search(condition);
                return true;
            }
            return false;
        });
    }

    public void search(Predicate<User> condition) {
        log("search");
        binding.textLog.removeAllViews();
        userController.readAllUsers(user -> {
            if (user != null && condition.test(user)) {
                log(user.getUserName());

                View view = getLayoutInflater().inflate(R.layout.inflater_profile,
                        binding.textLog, false);
                Button button = view.findViewById(R.id.text_nickname);
                button.setText(user.getUserName());
                button.setOnClickListener(v -> {
                    Optional<User> opt = Optional.of(user);
                    opt.ifPresent(value -> {
                        Intent intent = new Intent(requireContext(), OtherProfileActivity.class);
                        intent.putExtra("type", "appoint");
                        intent.putExtra("isReceiving", false); // is not receiving
                        intent.putExtra("user", value.toString());
                        intent.putExtra("chatId", "fake"); // actually it's empty
                        startActivityForResult(intent, Codes.OTHER_PROFILE);
                    });
                });
                binding.textLog.addView(view);
            }
        });
    }
}