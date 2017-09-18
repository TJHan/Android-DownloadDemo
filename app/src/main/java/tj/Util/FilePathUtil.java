package tj.Util;

import android.os.Environment;

/**
 * Created by tjhan on 2017/9/15.
 */

public class FilePathUtil {

    public static String GetAppCacheFilePath()
    {
        return Environment.getDownloadCacheDirectory().getPath();
    }

    public static String GetExternalDownloadFilePath()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    }

    public static String GetExternalMusicFilePath()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
    }
    public static String GetExternalPictureFilePath()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
    }
}
