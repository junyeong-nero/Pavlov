package ad.agio.test_firebase.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import ad.agio.test_firebase.R;
import ad.agio.test_firebase.activities.ChatActivity;
import ad.agio.test_firebase.activities.OtherProfileActivity;
import ad.agio.test_firebase.controller.AppointController;
import ad.agio.test_firebase.controller.DataController;
import ad.agio.test_firebase.domain.User;

public class AppointWorker extends Worker {

    private void log(String text) {
        Log.e(this.getClass().getSimpleName(), text);
    }

    private final Context context;
    private AppointController appointController;
    private DataController dataController;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";


    public AppointWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.dataController = new DataController(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        prepare();
        for (int i = 0; i < 15 * 6; i++) {
            try {
                receive();
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

    public void prepare() {
        dataController.deleteData("appoint_uid");
        appointController = new AppointController();
        appointController.setContext(context);
        appointController.setEmpty();
        appointController.failureListener = none -> {
            log("finish");
            dataController.deleteData("appoint_uid");
        };
        appointController.successListener = chat -> {
            Intent intent = new Intent(appointController.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            dataController.deleteData("appoint_uid");
            context.startActivity(intent);
        };
    }

    @Override
    public void onStopped() {
        super.onStopped();
        WorkRequest wr =
                new PeriodicWorkRequest.Builder(AppointWorker.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager
                .getInstance(context)
                .enqueue(wr);
    }

    public void receive() {
        appointController.readReceive(list -> {
            if(!list.isEmpty()) {
                Optional<User> user = list.stream().findAny();
                Intent intent = new Intent(appointController.getContext(), OtherProfileActivity.class);
                user.ifPresent(value -> {
                    String temp = dataController.readData("appoint_uid");
                    if(temp.equals(value.getUid()))
                        return;

                    dataController.saveData("appoint_uid", value.getUid());
                    intent.putExtra("type", "appoint");
                    intent.putExtra("isReceiving", true); // is receiving
                    intent.putExtra("user", value.toString());
                    intent.putExtra("userName", value.getUserName());
                    intent.putExtra("chatId", value.getChatId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    notification(intent);
                });
            }
        });
    }

    public void notification(Intent intent) {
        log("notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(intent.getStringExtra("userName") + "님의 약속요청이 도착했어요!")
                .setContentText("클릭해서 확인해보세요.")
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "pavlov";
            String description = "pavlov";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1158, builder.build()); // 고유숫자로 노티피케이션 동작시킴
    }
}
