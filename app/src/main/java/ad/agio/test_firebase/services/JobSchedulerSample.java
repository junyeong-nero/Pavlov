package ad.agio.test_firebase.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

class JobSchedulerSample {
    private static final int JOB_ID_UPDATE = 0x1000;

//    static void setUpdateJob(Context context) {
//        // 대용량 데이터를 업데이트하기 위한 적정 조건 설정
//        JobInfo job =
//                new JobInfo.Builder(
//                        // Job에 설정할 Id 값
//                        JOB_ID_UPDATE,
//                        // 조건 만족 시 UpdateDataByWiFiService가 실행
//                        new ComponentName(this, SyncJobService.class)
//                )
//                        // WiFi 등의 비과금 네트워크를 사용 중이며
//                        .setRequiredNetworksCapabilities(JobInfo.NETWORK_TYPE_ANY)
//                        .build();
//
//        // JobScheduler 서비스
//        JobService mJobService = (JobService) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//        // Job을 등록한다.
//        mJobService.scheduleJob(job);
//    }
}