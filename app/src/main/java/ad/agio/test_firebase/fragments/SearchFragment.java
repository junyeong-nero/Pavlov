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
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Optional;
import java.util.function.Predicate;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ProfileActivity;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentSearchBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.RequestCodes;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private void log(String s) {
        Log.e(this.getClass().getSimpleName(), s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Predicate<User> condition = user -> user.getUserName()
                .contains(binding.editQuery.getText().toString());

        binding.button.setOnClickListener(v -> {
            // 20 ~ 40대를 위한 lambda expression
            search(condition);
        });

        binding.editQuery.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                search(condition);
            }
            return false;
        });
    }

    public void search(Predicate<User> condition) {
        binding.textLog.removeAllViews();
        UserController controller = new UserController();
        controller.readAllUsers(user -> {
            if (user != null && condition.test(user)) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.inflate_profile, null);
                TextView nick = view.findViewById(R.id.text_nickname);
                nick.setText(user.getUserName());

                log(user.toString());

                ImageButton button = view.findViewById(R.id.button_chat);
                button.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ProfileActivity.class);
                    Optional<User> opt = Optional.of(user);
                    opt.ifPresent(value -> {
                        intent.putExtra("type", "appoint");
                        intent.putExtra("isReceiving", false); // is not receiving
                        intent.putExtra("user", value.toString());
                        intent.putExtra("chatId", "fake"); // actually it's empty
                    });
                    startActivityForResult(intent, RequestCodes.SEARCH_ACTIVITY);
                });
                binding.textLog.addView(view);
            }
        });
    }
}