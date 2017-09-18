package tj.task;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tj.Util.FilePathUtil;
import tj.com.downloaddemo.MainActivity;
import tj.listener.DownLoadListener;

/**
 * Created by tjhan on 2017/9/11.
 */

public class DownLoadTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "DownLoadTask";
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;
    private DownLoadListener downloadListener;

    public DownLoadTask(DownLoadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    /*
    * 暂停下载
    * */
    public void pauseDownLoad() {
        this.isPaused = true;
    }

    /*
    * 取消下载
    * */
    public void cancelDownLoad() {
        this.isCanceled = true;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream inputStream = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0;
            String downLoadUrl = params[0];
            String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

            Log.d(TAG, "doInBackground: 下载路径："+directory);

            file = new File(directory + fileName);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downLoadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downLoadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                inputStream = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = inputStream.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
//                        Log.d(TAG, "doInBackground: 已下载长度："+downloadedLength+" - 下载中长度："+total+" - 总长度："+contentLength);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        Log.d(TAG, "doInBackground: 上次下载的长度："+lastProgress);
        if (progress > lastProgress) {
            Log.d(TAG, "doInBackground: 下载的长度："+progress);
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    /*
    * 获取下载内容的长度
    * */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
