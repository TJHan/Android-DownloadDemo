package tj.listener;

/**
 * Created by tjhan on 2017/9/11.
 * 下载回调接口，用于对下载过程中的各种状态进行监听和回调
 */

public interface DownLoadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
