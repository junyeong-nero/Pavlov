package ad.agio.test_firebase.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ad.agio.test_firebase.Activities.ChatActivity;
import ad.agio.test_firebase.Activities.HomeActivity;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentOtherProfileBinding;
import ad.agio.test_firebase.databinding.FragmentProfileBinding;
import ad.agio.test_firebase.domain.User;
import gun0912.tedbottompicker.TedBottomPicker;

public class OtherProfileFragment extends Fragment {

    private void _log(String text) {
        Log.d(OtherProfileFragment.class.getSimpleName(), text);
    }

    private FragmentOtherProfileBinding binding;
    private UserController userController;
    private MatchController matchController;
    private User currentUser;
    private boolean isMatching;

    public OtherProfileFragment(boolean isMatching, User user, String chatId) {
        this.isMatching = isMatching;
        this.currentUser = user;

        matchController = new MatchController();
        matchController.setChatController(chatId);
        matchController.matchListener = chat -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };
    }

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOtherProfileBinding.inflate(inflater, container, false);

        userController = new UserController();
        if(matchController == null)
            matchController = new MatchController();

        drawProfile(currentUser);
        setProfileImage(currentUser);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonMatch.setOnClickListener(v -> {
            if(isMatching) // receiving 하는 중
                matchController.receiveResult(currentUser);
            else // matching 하는 중
                matchController.match(currentUser);
        });

        binding.imageSelect.setOnClickListener(v -> {
            TedBottomPicker.with(getActivity())
                    .show(uri -> {
                        binding.imageProfile.setImageURI(uri);
                        UserController userController = new UserController();
                        userController.updateUser("profile", uri.getPath());
                        _log(uri.getPath());
                    });
        });
    }

    private void drawProfile(User user) {
        binding.layoutUser.removeAllViews();
        try {
            JSONObject obj = new JSONObject(user.toString());
            Iterator iterator = obj.keys();
            while (iterator.hasNext()) {
                String next = iterator.next().toString();
                TextView textView = new TextView(requireContext());
                textView.setText(next);
                binding.layoutUser.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                EditText editText = new EditText(requireContext());
                editText.setText(obj.getString(next));
                binding.layoutUser.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setProfileImage(User user) {
        String profile = user.getProfile(); // 프로필이 있으면 사진 설정.
        if (!profile.equals("")) {
            binding.imageProfile.setImageURI(Uri.parse(profile));
        }
    }
}