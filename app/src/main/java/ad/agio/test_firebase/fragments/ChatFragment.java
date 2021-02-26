package ad.agio.test_firebase.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.function.Consumer;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.controller.ChatController;
import ad.agio.test_firebase.databinding.FragmentChatBinding;
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

        cook(rawData, chatId -> {
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
                startActivity(intent);
            });

            View line = new View(requireContext());
            line.setBackgroundColor(Color.BLACK);
            binding.layout.addView(line, g.getScreenWidth(), g.dp(1));

            binding.layout.addView(view);
        });
    }

    private void cook(String rawData, Consumer<String> consumer) {
        log(rawData);
        String[] split = rawData.split("\\|");
        for (String chatId : split) {
            log(chatId);
            if (chatId != null && !chatId.equals("")) {
                consumer.accept(chatId);
            }
        }
    }
}