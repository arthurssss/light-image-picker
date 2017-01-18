package net.neevek.android.lib.lightimagepicker.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Lilith Games
 * Created by JiaminXie on 13/01/2017.
 */

public class LocalMediaResource implements Parcelable {
    public int mediaType; // supports IMAGE and VIDEO
    public long id;
    public String path;
    public int size;
    public long datetimeAdded;

    public LocalMediaResource(int mediaType, long id, String path, int size, long datetimeAdded) {
        this.mediaType = mediaType;
        this.id = id;
        this.size = size;
        this.path = path;
        this.datetimeAdded = datetimeAdded;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalMediaResource &&
                (this.id == ((LocalMediaResource)obj).id ||
                (this.path.equals(((LocalMediaResource)obj).path)));

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "[" + mediaType + ", " + id + ", " + size + ", " + datetimeAdded + ", " + path + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mediaType);
        dest.writeLong(id);
        dest.writeString(path);
        dest.writeInt(size);
        dest.writeLong(datetimeAdded);
    }

    public static final Parcelable.Creator<LocalMediaResource> CREATOR
            = new Parcelable.Creator<LocalMediaResource>() {
        @Override
        public LocalMediaResource createFromParcel(Parcel source) {
            LocalMediaResource resource = new LocalMediaResource(
                    source.readInt(),
                    source.readLong(),
                    source.readString(),
                    source.readInt(),
                    source.readLong()
            );
            return resource;
        }

        @Override
        public LocalMediaResource[] newArray(int size) {
            return new LocalMediaResource[size];
        }
    };
}
