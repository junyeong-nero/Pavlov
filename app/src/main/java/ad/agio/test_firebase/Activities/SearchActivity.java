package ad.agio.test_firebase.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Function;
import java.util.function.Predicate;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.databinding.ActivitySearchBinding;
import ad.agio.test_firebase.domain.User;

public class SearchActivity extends AppCompatActivity {

    final static public String TAG = "SearchActivity";
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener(v -> {
            finish();
        });

        binding.button.setOnClickListener(v -> {
            // 20 ~ 40대를 위한 lambda expression
            Predicate<User> condition = user -> user.getUserName().contains(binding.editQuery.getText().toString());
            search(condition);
        });
    }

    private DatabaseReference mDatabase;

    public void search(Predicate<User> condition) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users").orderByChild("type");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        // TODO: handle the post
                        if(postSnapshot.getValue(User.class) != null) {
                            User post = postSnapshot.getValue(User.class);
                            if (condition.test(post)) {
                                Log.d(TAG, post.toString());
                                LayoutInflater layoutInflater = getLayoutInflater();
                                View view = layoutInflater.inflate(R.layout.inflate_profile, null);
                                TextView nick = (TextView) view.findViewById(R.id.text_nickname);
                                ImageButton button = (ImageButton) view.findViewById(R.id.button_chat);
                                nick.setText(post.getUserName());

                                FirebaseAuth auth = FirebaseAuth.getInstance();

                                if(auth.getCurrentUser() == null)
                                    throw new IllegalStateException("it is not valid user");

                                button.setOnClickListener(v -> {
                                    Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                                    chat.putExtra("receiver", post.getId());
                                    chat.putExtra("sender", auth.getUid());
                                    startActivity(chat);
                                });
                                binding.textviewLog.addView(view);
                            }
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}