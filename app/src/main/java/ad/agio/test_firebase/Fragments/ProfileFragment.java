package ad.agio.test_firebase.Fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.AuthController;
import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.FragmentProfileBinding;
import ad.agio.test_firebase.domain.User;
import gun0912.tedbottompicker.TedBottomPicker;

public class ProfileFragment extends Fragment {

    private void _log(String text) {
        Log.d("ProfileFragment", text);
    }

    private FragmentProfileBinding binding;
    private UserController userController;
    private User currentUser;

    // onCreate -> onCreateView -> onViewCreated -> onStart -> onResume

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        if (new AuthController().isAuth())
            userController = new UserController();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userController.readUser(user -> {
            currentUser = user;
            _log(user.toString());
            drawProfile(user);
            setProfileImage(user);
        });

        binding.buttonNeighbor.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NeighborAuthFragment(), "NeighborAuthFragment")
                    .commit();
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
        if(!profile.equals("")) {
            binding.imageProfile.setImageURI(Uri.parse(profile));
        }
    }
}