package ad.agio.test_firebase.domain;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    public String text = "";
    public String chatId = "";
    public String chatName = "";
    public String match = "";
    public HashMap<String, User> users = new HashMap<>();

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
