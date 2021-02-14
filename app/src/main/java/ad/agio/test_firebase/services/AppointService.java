package ad.agio.test_firebase.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Optional;

import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.HomeActivity;
import ad.agio.test_firebase.activities.ProfileActivity;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.domain.User;

public class AppointService extends Service {

    private void _log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private AppointController appointController;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _log("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appointController = new AppointController();

        appointController.appointmentListener = chat -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };

        appointController.makeReceiver(list -> {
            if(!list.isEmpty()) {
                // TODO notification -> profileActivity
                Optional<User> user = list.stream().findAny();

                // boolean : isMatching
                // JSON String : user
                // String : chatId

                // TODO 여기는 매칭 동의화면으로 연결되어야 한다.

                Intent intent = new Intent(this, ProfileActivity.class);
                user.ifPresent(value -> {
                    intent.putExtra("type", "appoint");
                    intent.putExtra("isReceiving", false); // is not receiving
                    intent.putExtra("user", value.toString());
                    intent.putExtra("chatId", value.getChatId());
                });
                startActivity(intent);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
