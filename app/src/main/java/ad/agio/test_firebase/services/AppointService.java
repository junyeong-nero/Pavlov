package ad.agio.test_firebase.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Optional;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.OtherProfileActivity;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.domain.User;

public class AppointService extends Service {

    private void _log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }
    private AppointController appointController;

    @Override
    public int onStartCommand(Intent _intent, int flags, int startId) {
        _log("onStartCommand");

        appointController = new AppointController();
        appointController.setContext(this);
        appointController.successListener = chat -> {
            Intent intent = new Intent(appointController.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            startActivity(intent);
        };

        appointController.startReceive(list -> {
            if(!list.isEmpty()) {
                Optional<User> user = list.stream().findAny();
                _log(user.toString());

                Intent intent = new Intent(appointController.getContext(), OtherProfileActivity.class);
                user.ifPresent(value -> {
                    intent.putExtra("type", "appoint");
                    intent.putExtra("isReceiving", true); // is receiving
                    intent.putExtra("user", value.toString());
                    intent.putExtra("userName", value.getUserName());
                    intent.putExtra("chatId", value.getChatId());
                });
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notification(intent);
            }
        });
        return super.onStartCommand(_intent, flags, startId);
    }

    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public void notification(Intent intent) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(intent.getStringExtra("userName") + "님의 약속요청이 도착했어요!")
                .setContentText("클릭해서 확인해보세요.")
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
