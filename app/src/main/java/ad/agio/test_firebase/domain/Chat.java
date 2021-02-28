package ad.agio.test_firebase.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ad.agio.test_firebase.controller.AuthController;

public class Chat {
    public String text = "";
    public String chatId = "";
    public String chatName = "";
    public String textChange = "";
    public String result = "";
    public Meeting meeting = null;
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

    public void readUserBy(Predicate<User> condition, Consumer<ArrayList<User>> consumer) {
        ArrayList<User> list = new ArrayList<>();
        this.users.forEach((key, user) -> {
            if (condition.test(user))
                list.add(user);
        });
        consumer.accept(list);
    }

    public void readAllUsers(Consumer<ArrayList<User>> consumer) {
        readUserBy(user -> true, consumer);
    }

    public void readOtherUsers(Consumer<ArrayList<User>> consumer) {
        AuthController auth = new AuthController();
        readUserBy(user -> !user.getUid().equals(auth.getUid()), consumer);
    }

    public void readMe(Consumer<ArrayList<User>> consumer) {
        AuthController auth = new AuthController();
        readUserBy(user -> user.getUid().equals(auth.getUid()), consumer);
    }
}
