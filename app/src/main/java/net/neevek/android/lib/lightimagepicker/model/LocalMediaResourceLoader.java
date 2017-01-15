package net.neevek.android.lib.lightimagepicker.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import net.neevek.android.lib.lightimagepicker.pojo.Bucket;
import net.neevek.android.lib.lightimagepicker.pojo.LocalMediaResource;
import net.neevek.android.lib.lightimagepicker.util.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lilith Games
 * Created by JiaminXie on 13/01/2017.
 */

public class LocalMediaResourceLoader {
    public static List<LocalMediaResource> loadImagesAndVideos(Context context) {
        String[] projection = {
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );

        List<LocalMediaResource> resourceList = new ArrayList<LocalMediaResource>();
        Cursor cursor = null;
        try {
            cursor = cursorLoader.loadInBackground();
            while (cursor.moveToNext()) {
                LocalMediaResource resource = new LocalMediaResource(
                        cursor.getInt(0),       // media type
                        cursor.getString(1),    // path
                        cursor.getInt(2),      // size
                        cursor.getLong(3)       // date added
                );
                resourceList.add(resource);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return resourceList;
    }

    public static List<Bucket> getImageBuckets(Context context) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATA,
                "COUNT(*) AS file_count"
        };
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        return getBuckets(context, mediaUri, projection);
    }

    public static List<Bucket> getVideoBuckets(Context context) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATA,
                "COUNT(*) AS file_count"
        };
        Uri mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        return getBuckets(context, mediaUri, projection);
    }

    @NonNull
    private static List<Bucket> getBuckets(Context context, Uri mediaUri, String[] projection) {
        String bucketGroupBy = "1) GROUP BY (1";
        String bucketOrderBy = "MAX(datetaken) DESC";
        Cursor cursor = context.getContentResolver().query(
                mediaUri,
                projection,
                bucketGroupBy,
                null,
                bucketOrderBy
        );

        if (cursor != null) {
            List<Bucket> bucketList = new ArrayList<Bucket>(cursor.getCount());
            try {
                while (cursor.moveToNext()) {
                    Bucket bucket = new Bucket(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getInt(3)
                    );
                    bucketList.add(bucket);
                }
            } finally {
                cursor.close();
            }
            return bucketList;
        }

        return Collections.EMPTY_LIST;
    }

    public static List<LocalMediaResource> getImagesByBucketId(Context context, String bucketId) {
        L.d(">>>>>>>>>>>> loading images by bucketId: %s", bucketId);
        String[] projection = {
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
        };
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Images.ImageColumns.BUCKET_ID + "=?";
        String[] selectionArgs = new String[]{ bucketId };
        String bucketOrderBy = "datetaken DESC";

        Cursor cursor = context.getContentResolver().query(
                mediaUri,
                projection,
                selection,
                selectionArgs,
                bucketOrderBy
        );

        if (cursor != null) {
            List<LocalMediaResource> resourceList = new ArrayList<LocalMediaResource>(cursor.getCount());
            try {
                while (cursor.moveToNext()) {
                    LocalMediaResource resource = new LocalMediaResource(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                            cursor.getString(0),
                            cursor.getInt(1),
                            cursor.getLong(2)
                    );
                    resourceList.add(resource);
                }
            } finally {
                cursor.close();
            }

            L.d(">>>>>>>>>>>> done loading images by bucketId: %s, %d", bucketId, resourceList.size());
            return resourceList;
        }

        L.d(">>>>>>>>>>>> done loading images by bucketId: %s, zero loaded", bucketId);
        return Collections.EMPTY_LIST;
    }

//    public static List<LocalMediaResource> loadVideos() {
//
//    }
}
