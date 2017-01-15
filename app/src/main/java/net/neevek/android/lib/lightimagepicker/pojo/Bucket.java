package net.neevek.android.lib.lightimagepicker.pojo;

/**
 * Lilith Games
 * Created by JiaminXie on 13/01/2017.
 */

public class Bucket {
    public String bucketId;
    public String bucketName;
    public String lastImagePath;
    public int fileCount;

    public Bucket(String bucketId, String bucketName, String lastImagePath, int fileCount) {
        this.bucketId = bucketId;
        this.bucketName = bucketName;
        this.lastImagePath = lastImagePath;
        this.fileCount = fileCount;
    }

    @Override
    public String toString() {
        return "(" + bucketId + " - " + bucketName + ", " + lastImagePath + ", " + fileCount + ")";
    }
}
