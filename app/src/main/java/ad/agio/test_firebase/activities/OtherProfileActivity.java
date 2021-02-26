package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivityOtherProfileBinding;
import ad.agio.test_firebase.domain.User;

import static ad.agio.test_firebase.activities.HomeActivity.appointController;
import static ad.agio.test_firebase.activities.HomeActivity.matchController;
import static ad.agio.test_firebase.activities.HomeActivity.userController;

public class OtherProfileActivity extends AppCompatActivity {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private User otherUser;
    private String chatId, type;
    private boolean isReceiving;

    private ActivityOtherProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        this.isReceiving = intent.getBooleanExtra("isReceiving", false);
        this.type = intent.getStringExtra("type");
        this.otherUser = new Gson().fromJson(intent.getStringExtra("user"), User.class);
        this.chatId = intent.getStringExtra("chatId");

        if(intent.hasExtra("uid")) {
            userController.readUser(intent.getStringExtra("uid"), user -> {
                this.otherUser = user;
                drawProfile(user);
                setProfileImage(user);
            });
        }

        binding.buttonBack.setOnClickListener(v -> {
            if (type.equals("match")) {
                // rejectResult 에서 failureListener 호출 -> finish
                // request 하는 사람이면, 그냥 finish
                if (isReceiving) {
                    matchController.reject(otherUser);
                } else {
                    finish();
                }
            } else if (type.equals("appoint")) {
                // 거절 위와 마찬가지로 failureListener 호출 -> finish
                // request 하는 사람이면, 그냥 finish
                if (isReceiving) {
                    appointController.reject(chatId);
                } else {
                    finish();
                }
            } else if (type.equals("none")) {
                finish();
            }
        });

        init();
    }

    private void init() {
        appointController.setContext(this);
        matchController.setContext(this);

        appointController.failureListener = none -> finish();
        appointController.successListener = chat -> {
            Intent intent = new Intent(appointController.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
            finish();
        };

        if(isReceiving)
            matchController.setChatController(chatId);

        matchController.failureListener = none -> finish();
        matchController.successListener = chat -> {
            Intent intent = new Intent(matchController.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        };

        switch (type) {
            case "match":
                buttonMatch();
                if (!isReceiving) // 매칭 요청을 하는 경우, 약속 요청도 할 수 있도록
                    buttonAppointment();
                break;

            case "appoint":
                buttonAppointment();
                break;

            case "none":
                // 매칭, 약속 불가능
                binding.buttonAppointment.setVisibility(View.GONE);
                binding.buttonMatch.setVisibility(View.GONE);
                break;
        }
    }

    private void buttonMatch() {
        binding.buttonMatch.setText("매칭");
        binding.buttonMatch.setBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPrimary));
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
                ContextCompat.getColor(this, R.color.colorPrimaryVariant));
        binding.buttonAppointment.setOnClickListener(v -> {
            if(isReceiving) {
                appointController.appoint(chatId);
            } else {
                appointController.request(otherUser);
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
                TextView textView = new TextView(this);
                textView.setText(next);
                binding.layoutUser.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                EditText editText = new EditText(this);
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
            Uri parse = Uri.parse(profile);
            if (new File(parse.getPath()).exists()) {
                log("using file");
                binding.imageProfile.setImageURI(Uri.parse(profile));
            } else {
                userController.readProfileImage(user.getUid(), bytes -> {
                    // binding.imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    Glide.with(this)
                            .load(bytes)
                            .into(binding.imageProfile);
                });
            }
        }
    }
}