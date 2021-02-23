package ad.agio.test_firebase.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.ProfileActivity;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.MatchController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentOtherProfileBinding;
import ad.agio.test_firebase.domain.User;

public class OtherProfileFragment extends Fragment {

    private void _log(String text) {
        Log.d(this.getClass().getSimpleName(), text);
    }

    private FragmentOtherProfileBinding binding;
    private MatchController matchController;
    private AppointController appointController;
    private User otherUser;
    private String chatId, type;
    private boolean isReceiving;

    public OtherProfileFragment(String type, boolean isReceiving, User user, String chatId) {
        this.isReceiving = isReceiving;
        this.type = type;
        this.otherUser = user;
        this.chatId = chatId;

        appointController = new AppointController();
        appointController.appointmentCompleteListener = chat -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
            requireActivity().finish();
        };

        matchController = new MatchController();
        if(isReceiving)
            matchController.setChatController(chatId);
        matchController.matchCompleteListener = chat -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
            requireActivity().finish();
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOtherProfileBinding.inflate(inflater, container, false);

        if(matchController == null)
            matchController = new MatchController();

        drawProfile(otherUser);
        setProfileImage(otherUser);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (type.equals("match")) {
            _log("onViewCreated:match");
            buttonMatch();
            if(!isReceiving) // 매칭 요청을 하는 경우, 약속 요청도 할 수 있도록
                buttonAppointment();
        } else if (type.equals("appoint")) {
            _log("onViewCreated:appoint");
            buttonAppointment();
        }

        binding.imageSelect.setOnClickListener(v -> {
            // can't change profile image
        });
    }

    private void buttonMatch() {
        binding.buttonMatch.setText("매칭");
        binding.buttonMatch.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primary));
        binding.buttonMatch.setOnClickListener(v -> {
            if(isReceiving)
                matchController.receiveResult(otherUser);
            else
                matchController.request(otherUser);
        });
    }

    private void buttonAppointment() {
        binding.buttonAppointment.setText("약속");
        binding.buttonAppointment.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.quantum_bluegrey700));
        binding.buttonAppointment.setOnClickListener(v -> {
            if(isReceiving) {
                appointController.appoint(chatId);
            } else {
                appointController.request(otherUser);
                requireActivity().finish();
            }
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