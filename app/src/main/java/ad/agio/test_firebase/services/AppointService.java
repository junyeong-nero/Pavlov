package ad.agio.test_firebase.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Optional;

import ad.agio.test_firebase.activities.ChatActivity;
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
    public void onCreate() {
        super.onCreate();
        _log("onCreate");
        appointController = new AppointController();

        appointController.appointmentCompleteListener = chat -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };

        appointController.startReceive(list -> {
            if(!list.isEmpty()) {
                // TODO notification -> profileActivity
                Optional<User> user = list.stream().findAny();
                _log(user.toString());

                Intent intent = new Intent(this, ProfileActivity.class);
                user.ifPresent(value -> {
                    intent.putExtra("type", "appoint");
                    intent.putExtra("isReceiving", true); // is receiving
                    intent.putExtra("user", value.toString());
                    intent.putExtra("chatId", value.getChatId());
                });
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
