package ad.agio.test_firebase.domain;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;

@IgnoreExtraProperties
public class User {

    // 유저
    private String userName = "홍길동";
    private String sex = "male";
    private String neighbor = "사월동";
    private String email = "";
    private String uid = "";
    private String profile = "";
    private String type = "public";
    private String chatId = "";
    private String arrayChatId = "";
    private float age = 0F;
    private float manner = 0F;
    private boolean isMatching = false;
    private int status = Status.ON;

    // 산책장소를 저장
    private ArrayList<WalkPoint> walkPoints = new ArrayList<>();

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    private ArrayList<String> tags = new ArrayList<>();

    public void addWalkPoint(WalkPoint walkPoint) {
        walkPoints.add(walkPoint);
    }

    public void removeWalkPoint(WalkPoint walkPoint) {
        walkPoints.remove(walkPoint);
    }

    // 강아지
    private String dog_name = "바둑이";
    private String dog_breed = "진돗개";
    private String dog_sex = "남자";
    private String dog_serial = "없음";
    private float dog_age = 7F;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<WalkPoint> getWalkPoints() {
        return walkPoints;
    }

    public void setWalkPoints(ArrayList<WalkPoint> walkPoints) {
        this.walkPoints = walkPoints;
    }

    public String getDog_serial() {
        return dog_serial;
    }

    public void setDog_serial(String dog_serial) {
        this.dog_serial = dog_serial;
    }

    public String getArrayChatId() {
        return arrayChatId;
    }

    public void setArrayChatId(String arrayChatId) {
        this.arrayChatId = arrayChatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(String neighbor) {
        this.neighbor = neighbor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDog_name() {
        return dog_name;
    }

    public void setDog_name(String dog_name) {
        this.dog_name = dog_name;
    }

    public String getDog_breed() {
        return dog_breed;
    }

    public void setDog_breed(String dog_breed) {
        this.dog_breed = dog_breed;
    }

    public String getDog_sex() {
        return dog_sex;
    }

    public void setDog_sex(String dog_sex) {
        this.dog_sex = dog_sex;
    }

    public float getDog_age() {
        return dog_age;
    }

    public void setDog_age(float dog_age) {
        this.dog_age = dog_age;
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }

    public float getManner() {
        return manner;
    }

    public void setManner(float manner) {
        this.manner = manner;
    }

    public boolean isMatching() {
        return isMatching;
    }

    public void setMatching(boolean matching) {
        isMatching = matching;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}