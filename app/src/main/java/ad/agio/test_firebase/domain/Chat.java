package ad.agio.test_firebase.domain;

public class Chat {
    private String text = "";
    private String senderId;
    private String receiverId;
    private String chatId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
//    public String getChatId() {
//        return senderId + "|" + receiverId;
//    }
//
//    public String getReverseChatId() {
//        return receiverId + "|" + senderId;
//    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
