package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.databinding.FragmentChatBinding;
import ad.agio.test_firebase.utils.Codes;
import ad.agio.test_firebase.utils.GraphicComponents;

import static ad.agio.test_firebase.activities.HomeActivity.userController;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private void log(String t) {
        Log.e(this.getClass().getSimpleName(), t);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        userController.readMe(me -> draw(me.getArrayChatId()));

        return binding.getRoot();
    }

    private void draw(String rawData) {

        if(!isAdded()) // check fragment is attaching
            return;

        GraphicComponents g = new GraphicComponents(requireContext());
        binding.layout.removeAllViews();

        userController.readChat(chatId -> {
            log(chatId);
            View view = getLayoutInflater().inflate(R.layout.inflater_chat_thumb, null);
            TextView t1 = view.findViewById(R.id.title);
            TextView t2 = view.findViewById(R.id.subtitle);

            ChatController chatController = new ChatController(chatId);
            chatController.readChat(chat -> {
                String[] split = chat.textChange.split("\\|");
                t1.setText(chat.chatName);
                t2.setText(split[split.length - 1]);
            });

            Button button = view.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("chatId", chatId);
                startActivityForResult(intent, Codes.CHAT);
            });

            View line = new View(requireContext());
            line.setBackgroundColor(Color.BLACK);
            binding.layout.addView(line, g.getScreenWidth(), g.dp(1));

            binding.layout.addView(view);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("onActivityResult");
        if (requestCode == Codes.CHAT) {
            userController.readMe(me -> draw(me.getArrayChatId()));
        }
    }
}