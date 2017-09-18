package tj.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import tj.Util.FilePathUtil;
import tj.com.downloaddemo.MainActivity;
import tj.com.downloaddemo.R;
import tj.listener.DownLoadListener;
import tj.task.DownLoadTask;

/**
 * Created by tjhan on 2017/9/11.
 */

public class DownLoadService extends Service {
    private static final String TAG = "DownLoadService";
    private DownLoadTask downLoadTask;
    private String downloadUrl;

    private DownloadBinder downloadBinder = new DownloadBinder();

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    private DownLoadListener listener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("download success", -1));
            Toast.makeText(DownLoadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownLoadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downLoadTask = null;
            getNotificationManager().notify(1, getNotification("暂停下载", -1));
            Toast.makeText(DownLoadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownLoadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }

    public class DownloadBinder extends Binder {
        private String storagePath = "";

        public void startDownload(String url, String storagePath) {
            if (downLoadTask == null) {
                try {
                    downloadUrl = url;
                    downLoadTask = new DownLoadTask(listener);
                    downLoadTask.execute(downloadUrl);
                    this.storagePath = storagePath;
                    startForeground(1, getNotification("Downloading...", 0));
                    Toast.makeText(DownLoadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.d(TAG, "startDownload: 异常信息：" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        public void pauseDownload() {
            if (downLoadTask != null) {
                downLoadTask.pauseDownLoad();
            }
        }

        public void cancelDownload() {
            if (downLoadTask != null) {
                downLoadTask.cancelDownLoad();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

//                    directory = storagePath;
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownLoadService.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 服务启动啦...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: 服务创建好了");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: 服务销毁了");
        super.onDestroy();
    }
}
