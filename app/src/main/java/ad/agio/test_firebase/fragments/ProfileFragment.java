package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.Iterator;

import ad.agio.test_firebase.activities.LoginActivity;
import ad.agio.test_firebase.activities.NeighborActivity;
import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.SearchPlaceActivity;
import ad.agio.test_firebase.databinding.FragmentProfileBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.domain.WalkPoint;
import ad.agio.test_firebase.utils.Codes;
import gun0912.tedbottompicker.TedBottomPicker;

import static ad.agio.test_firebase.activities.HomeActivity.authController;
import static ad.agio.test_firebase.activities.HomeActivity.currentUser;
import static ad.agio.test_firebase.activities.HomeActivity.userController;


public class ProfileFragment extends Fragment {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private FragmentProfileBinding binding;

    // onCreate -> onCreateView -> onViewCreated -> onStart -> onResume

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        if(currentUser != null) {
            drawProfile(currentUser);
            setProfileImage(currentUser);
        } else {
            userController.readMe(me -> {
                currentUser = me;
                drawProfile(currentUser);
                setProfileImage(currentUser);
            });
        }

        binding.buttonNeighbor.setOnClickListener(v -> startActivityForResult(
                new Intent(requireContext(), NeighborActivity.class),
                Codes.NEIGHBOR));

        binding.buttonPlace.setOnClickListener(v -> startActivityForResult(
                new Intent(requireContext(), SearchPlaceActivity.class),
                Codes.SEARCH));

        binding.imageSelect.setOnClickListener(v ->
                TedBottomPicker.with(getActivity())
                        .show(uri -> {
                            binding.imageProfile.setImageURI(uri);
                            userController.updateUser("profile", uri.getPath());
                            currentUser.setProfile(uri.getPath());
                            try {
                                userController.writeProfileImage(uri.getPath());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            log(uri.getPath());
                        })
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void drawProfile(User user) {

        if(!isAdded())
            return;

        binding.textName.setText(user.getUserName());
        binding.textNeighbor.setText(user.getNeighbor());
        drawPlace(user);
        drawTags(user);
    }

    private void drawTags(User user) {
        binding.layoutTags.removeAllViews();
        for (String tag : user.getTags()) {
            View view = getLayoutInflater().inflate(R.layout.inflater_tag, binding.layoutTags, false);
            TextView textView = view.findViewById(R.id.text);
            textView.setText(tag);
            binding.layoutTags.addView(view);
        }
    }

    private void drawPlace(User user) {
        binding.layoutPlace.removeAllViews();
        for (WalkPoint wp : user.getWalkPoints()) {
            log(wp.name);
            View view = getLayoutInflater().inflate(R.layout.inflater_place, null);
            TextView text = view.findViewById(R.id.text);
            text.setText(wp.name);

            ImageButton button = view.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                userController.removeWalkPoint(wp);
                drawPlace(currentUser);
            });

            binding.layoutPlace.addView(view);
        }
    }

    private void setProfileImage(User user) {

        if(!isAdded())
            return;

        userController.readProfileImage(user.getUid(), bytes -> {
            // binding.imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            if(isAdded())
                Glide.with(this)
                        .load(bytes)
                        .into(binding.imageProfile);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");

        switch (resultCode) {

            case Codes.LOGOUT:
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
                break;

            case Codes.NEIGHBOR:
                log("NEIGHBOR_ACTIVITY");

                if (data != null && data.hasExtra("neighbor")) {
                    String neighbor = data.getStringExtra("neighbor");
                    userController.updateUser("neighbor", neighbor);
                    currentUser.setNeighbor(neighbor);
                }

                drawProfile(currentUser);
                setProfileImage(currentUser);
                break;

//            case Codes.MENU:
//                if(authController.isAuth()) {
//                    requireActivity().getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, new LoginFragment()).commit();
//                }
//                break;

            case Codes.SEARCH_PLACE:
                log("SEARCH_PLACE");
                if (data != null && data.hasExtra("walk_point")) {
                    WalkPoint wp = new Gson().fromJson(data.getStringExtra("walk_point"),
                            WalkPoint.class);

                    userController.addWalkPoint(wp);
                    log(currentUser.toString());
                }

                drawProfile(currentUser);
                setProfileImage(currentUser);
                break;


            default:
                log("default!");
        }
    }
}