package ad.agio.test_firebase.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ad.agio.test_firebase.controller.UserController;
import ad.agio.test_firebase.databinding.ActivityRegisterBinding;
import ad.agio.test_firebase.domain.User;
import ad.agio.test_firebase.utils.GraphicComponents;
import ad.agio.test_firebase.utils.Codes;

public class RegisterActivity extends AppCompatActivity {

    private void log(String t) {
        Log.e(this.getClass().getSimpleName(), t);
    }

    private ActivityRegisterBinding binding;
    private GraphicComponents gc;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        gc = new GraphicComponents(this);
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });


        // TODO 본인인증?
        ArrayList<Question> list = new ArrayList<>();
        list.add(new Question("사용자 정보 입력", Question.Type.no));
        list.add(new Question("사용하실 닉네임을 입력해주세요", Question.Type.user_name));
        list.add(new Question("여성이신가요 남성이신가요?", Question.Type.sex));
        list.add(new Question("전화번호가 궁금해요!", Question.Type.telephone));
        list.add(new Question("회원가입을 위한 이메일 주소를 입력해주세요", Question.Type.email));
        list.add(new Question("회원가입을 위한 비밀번호를 입력해주세요", Question.Type.password));
        list.add(new Question("지역인증 부탁드려요", Question.Type.neighbor_validation));

        startSurvey(list);
//        binding.buttonRegister.setOnClickListener(v -> {
//            mUser = new User();
//            mUser.setEmail(binding.etEmail.getText().toString().trim());
//            mUser.setAge(Integer.parseInt(binding.etAge.getText().toString().trim()));
//            mUser.setUserName(binding.etUserName.getText().toString().trim());
//
//            createAuth(mUser.getEmail(), binding.etPw.getText().toString().trim());
//        });
    }

    public void startSurvey(ArrayList<Question> questions) {
        binding.progressbar.setMax(questions.size());

        surveyResult = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++) {
            surveyResult.add("");
        }

        if(!questions.isEmpty())
            showSurvey(questions, 0);
    }

    private ArrayList<String> surveyResult;
    private String result;

    private void finishSurvey() {
        log("finishSurvey");
        cook();
        createAuth(surveyResult.get(Question.Type.email), surveyResult.get(Question.Type.password));
    }

    private void cook() {
        if(surveyResult != null && !surveyResult.isEmpty()) {
            currentUser = new User(); // TODO 더 많은 정보를 기입해야 함.
            currentUser.setUserName(surveyResult.get(Question.Type.user_name));
            currentUser.setSex(surveyResult.get(1));
            currentUser.setEmail(surveyResult.get(3));
        }
    }

    private Button neighbor;

    private void showSurvey(ArrayList<Question> questions, int que) {
        log(surveyResult.toString());

        if(questions.size() == que){
            finishSurvey();
            return;
        }

        Question question = questions.get(que);
        EditText editText = new EditText(this);
        editText.setGravity(Gravity.CENTER);

        String s = ""; // 이전에 입력해놓은 결과.

        if(question.type < surveyResult.size()) {
            s = surveyResult.get(question.type);
            editText.setText(s);
        }

        result = "";
        binding.textTitle.setText(question.text);
        binding.progressbar.setProgress(que);
        binding.layoutContent.removeAllViews();
        neighbor = null;

        switch (question.type) {
            case Question.Type.number:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                binding.layoutContent.addView(editText, gc.dp(150), gc.dp(56));
                break;

            case Question.Type.user_name:
            case Question.Type.password:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                binding.layoutContent.addView(editText, gc.dp(150), gc.dp(56));
                break;

            case Question.Type.sex:
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);

                String[] tt = {"male", "female", "none"};
                result = tt[0];
                for (String t : tt) {
                    Button button = new Button(this);
                    button.setText(t);
                    button.setOnClickListener(v -> {
                        result = t;
                    });
                    layout.addView(button);
                }

                binding.layoutContent.addView(layout);
                break;

            case Question.Type.telephone:
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                binding.layoutContent.addView(editText, gc.dp(150), gc.dp(56));
                break;

            case Question.Type.email:
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                binding.layoutContent.addView(editText, gc.dp(150), gc.dp(56));
                break;

            case Question.Type.neighbor_validation:
                neighbor = new Button(this);
                if(question.type < surveyResult.size()) {
                    if (s.equals("")) {
                        neighbor.setText("인증하기");
                        startActivityForResult(new Intent(this, NeighborActivity.class),
                                Codes.NEIGHBOR);
                    } else {
                        neighbor.setText(s);
                    }
                }
                neighbor.setOnClickListener(v -> {
                    startActivityForResult(new Intent(this, NeighborActivity.class),
                            Codes.NEIGHBOR);
                });
                binding.layoutContent.addView(neighbor);
                break;
        }

        binding.buttonPrevious.setOnClickListener(v -> {
            if (result.equals("")) {
                result = editText.getText().toString();
            }
            if(question.type != Question.Type.no)
                surveyResult.set(question.type, (result));
            showSurvey(questions, que - 1);
        });

        binding.buttonNext.setOnClickListener(v -> {
            if (result.equals("")) {
                result = editText.getText().toString();
            }
            if(question.type != Question.Type.no)
                surveyResult.set(question.type, (result));
            showSurvey(questions, que + 1);
        });
    }

    private static class Question {
        public String text;
        public int type;

        public Question(String text, int type) {
            this.text = text;
            this.type = type;
        }

        public static class Type {
            static final int no = 999;
            static final int number = 0;
            static final int user_name = 1;
            static final int sex = 2;
            static final int telephone = 3;
            static final int email = 4;
            static final int neighbor_validation = 5;
            static final int password = 6;
        }
    }


    private void createAuth(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        log("createUserWithEmail:success");
                        Snackbar.make(binding.layoutContent, "회원가입에 성공했습니다.", 500)
                                .show();
                        UserController userController = new UserController();
                        currentUser.setUid(mAuth.getCurrentUser().getUid()); // UID!
                        userController.writeNewUser(currentUser);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        log( "createUserWithEmail:failure" + task.getException());
                        Snackbar.make(binding.layoutContent, "회원가입에 실패했습니다.", 500)
                                .show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Codes.NEIGHBOR) {
            log("NEIGHBOR_ACTIVITY");
            result = data.getStringExtra("neighbor");
            surveyResult.set(Question.Type.neighbor_validation, result);
            if (neighbor != null) {
                neighbor.setText(result);
            }
        }
    }

    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            err = true;
        }
        return err;
    }
}