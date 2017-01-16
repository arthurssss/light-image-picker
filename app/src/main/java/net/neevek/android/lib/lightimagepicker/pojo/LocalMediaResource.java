package net.neevek.android.lib.lightimagepicker.pojo;

/**
 * Lilith Games
 * Created by JiaminXie on 13/01/2017.
 */

public class LocalMediaResource {
    public int mediaType; // supports IMAGE and VIDEO
    public String path;
    public int size;
    public long datetimeAdded;

    public LocalMediaResource(int mediaType, String path, int size, long datetimeAdded) {
        this.mediaType = mediaType;
        this.size = size;
        this.path = path;
        this.datetimeAdded = datetimeAdded;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalMediaResource &&
                this.path.equals(((LocalMediaResource)obj).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "[" + mediaType + ", " + size + ", " + datetimeAdded + ", " + path + "]";
    }
}
