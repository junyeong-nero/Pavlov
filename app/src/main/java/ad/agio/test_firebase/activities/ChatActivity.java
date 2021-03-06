package ad.agio.test_firebase.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.databinding.ActivityChatBinding;
import ad.agio.test_firebase.domain.Chat;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.Codes;
import ad.agio.test_firebase.utils.GraphicComponents;

import static ad.agio.test_firebase.activities.HomeActivity.currentUser;
import static ad.agio.test_firebase.activities.HomeActivity.userController;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatController chatController;
    private Chat mChat;
    private String textChange = "";
    private void log(String t) {
        Log.e(this.getClass().getSimpleName(), t);
    }

    private LinearLayout userLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        GraphicComponents g = new GraphicComponents(this);
        setContentView(binding.getRoot());

        userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.VERTICAL);
        userLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        userLayout.setPadding(g.dp(4), g.dp(4), g.dp(4), g.dp(4));

        TextView textView = new TextView(this);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.medium));
        textView.setText("사용자");
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(g.dp(4), 0, 0, 0);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        userLayout.addView(textView, LinearLayout.LayoutParams.MATCH_PARENT, g.dp(24));

        View line = new View(this);
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.colorOnPrimary));
        userLayout.addView(line, LinearLayout.LayoutParams.MATCH_PARENT, g.dp(1));

        View headerView = binding.navView.getHeaderView(0);
        LinearLayout b = headerView.findViewById(R.id.layout);
        b.addView(userLayout, LinearLayout.LayoutParams.MATCH_PARENT,
                g.getScreenHeight() - g.dp(38));

        Intent intent = getIntent();
        String chatId = intent.getStringExtra("chatId");

        // 채팅방 나가기
        headerView.findViewById(R.id.button_out).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle("정말 채팅방을 나가시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        chatController.removeUser(currentUser.getUid());
                        userController.removeChat(chatId);
                        finish();
                    })
                    .setNegativeButton("아니요", null)
                    .show());

        // menu
        binding.buttonMenu.setOnClickListener(v ->
                binding.drawerLayout.openDrawer(GravityCompat.END));

        chatController = new ChatController(chatId);
        chatController.readText(this::drawAll);
        chatController.readChat(chat -> {
            mChat = chat;
            textChange = chat.textChange;

            mChat.readAllUsers(users -> {
                for (User user : users) {
                    @SuppressLint("InflateParams") View view =
                            getLayoutInflater().inflate(R.layout.inflater_profile, null);
                    Button nick = view.findViewById(R.id.text_nickname);
                    nick.setText(user.getUserName());
                    nick.setOnClickListener(v -> startOtherProfileActivity(user.getUid()));

                    userLayout.addView(view);
                }
            });

            binding.buttonMeeting.setText(mChat.meeting.toString());
            binding.buttonMeeting.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                        .setTitle("약속정보")
                        .setMessage(mChat.meeting.toString())
                        .show()
            );
            binding.toolbarTitle.setText(mChat.chatName);

            // 채팅방 정보를 읽고 이후에 채팅과 연결.
            chatController.addTextChangeListener(text -> {
                log(text);
                if (!text.equals(textChange)) {
                    drawChange(text);
                    textChange = text;
                }
            });
        });

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.editText.setSingleLine();
        binding.editText.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                send();
                return true;
            }
            return false;
        });

        binding.buttonSend.setOnClickListener(v -> send());
    }

    private void drawAll(String text) {
        String[] split = text.split("\n");
        for (String line : split) {
            drawLine(line);
        }
        binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN)); // 가장 아래로 스크롤 내리기
    }

    private void startOtherProfileActivity(String uid) {
        Intent _intent = new Intent(ChatActivity.this,
                OtherProfileActivity.class);
        _intent.putExtra("type", "none");
        _intent.putExtra("isReceiving", false); // is not receiving
        _intent.putExtra("user", "");
        _intent.putExtra("uid", uid);
        _intent.putExtra("chatId", "fake"); // actually it's empty
        startActivityForResult(_intent, Codes.OTHER_PROFILE);
    }

    private void drawChange(String text) {
        // 변화된 마지막 줄 결과만 그린다.
        String[] split = text.split("\n");
        drawLine(split[split.length - 1]);
    }

    private void drawLine(String line) {
        HashMap<String, String> map = cook(line);
        View view;

        String uid = map.get("uid");
        String name = map.get("name");
        String date = map.get("date");
        String content = map.get("content");

        // null check
        List<String> list = Arrays.asList(uid, name, date, content);
        if(!list.stream().allMatch(text -> text != null && !text.equals("")))
            return;

        assert uid != null;
        if (uid.equals(currentUser.getUid())) {
            // 내가 쓴 채팅
            view = getLayoutInflater().inflate(R.layout.inflater_my_chat,
                    binding.chatLayout, false);
        } else {
            // 다른 사람이 쓴 채팅
            view = getLayoutInflater().inflate(R.layout.inflater_other_chat,
                    binding.chatLayout, false);
        }

        assert name != null;
        Button button = view.findViewById(R.id.button_name);
        button.setText(name.substring(0, 1));
        button.setOnClickListener(v -> startOtherProfileActivity(uid));

        assert date != null;
        button.setOnLongClickListener(v -> {
            Snackbar.make(v, date, 500).show();
            return true;
        });

        TextView textView = view.findViewById(R.id.text_content);
        textView.setText(content);

        binding.chatLayout.addView(view);
    }

    private HashMap<String, String> cook(String temp) {

        HashMap<String, String> map = new HashMap<>();
        String[] data = temp.split("\\|");
        map.put("name", "");
        map.put("uid", "");
        map.put("date", "");
        map.put("content", "제목없음");

        if(data.length >= 1)
            map.put("name", data[0]);
        if(data.length >= 2)
            map.put("uid", data[1]);
        if(data.length >= 3)
            map.put("date", data[2]);
        if(data.length >= 4)
            map.put("content", data[3]);

        return map;
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        chatController.removeConfirmListener();
        chatController.removeTextListener();
        chatController.removeTextChangeListener();
    }

    public void send() {
        assert chatController != null;
        chatController.sendText(currentUser, binding.editText.getText().toString());

        binding.editText.setText("");
        binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN)); // 가장 아래로 스크롤 내리기
    }
}