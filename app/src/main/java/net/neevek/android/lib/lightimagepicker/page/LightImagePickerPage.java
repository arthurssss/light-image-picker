package net.neevek.android.lib.lightimagepicker.page;

import android.Manifest;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.model.LocalMediaResourceLoader;
import net.neevek.android.lib.lightimagepicker.pojo.Bucket;
import net.neevek.android.lib.lightimagepicker.pojo.LocalMediaResource;
import net.neevek.android.lib.lightimagepicker.util.Async;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectView;
import net.neevek.android.lib.paginize.annotation.PageLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lilith Games
 * Created by JiaminXie on 12/01/2017.
 */

@PageLayout(R.layout.light_image_picker_page_album)
public class LightImagePickerPage extends Page implements ResourceBucketManager.OnBucketSelectedListener, View.OnClickListener {
    @InjectView(R.id.light_image_picker_rv_photo_list)
    private RecyclerView mRvPhotoList;
    @InjectView(R.id.light_image_picker_rv_bucket_list)
    private RecyclerView mRvBucketList;
    @InjectView(value = R.id.light_image_picker_view_bucket_list_bg, listenerTypes = View.OnClickListener.class)
    private View mViewBucketListBg;
    @InjectView(value = R.id.light_image_picker_tv_select_bucket, listenerTypes = View.OnClickListener.class)
    private TextView mTvSelectBucket;

    private AlbumListAdapter mAdapter;
    private List<LocalMediaResource> mResourceList;

    private ResourceBucketManager mResourceBucketManager;

    private Map<String, List<LocalMediaResource>> mCachedResourceList = new HashMap<String, List<LocalMediaResource>>();

    public LightImagePickerPage(PageActivity pageActivity) {
        super(pageActivity);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRvPhotoList.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));
        } else {
            mRvPhotoList.setLayoutManager(new GridLayoutManager(getContext(), 6, LinearLayoutManager.VERTICAL, false));
        }
        mAdapter = new AlbumListAdapter();
        mRvPhotoList.setAdapter(mAdapter);

        requestPermission();

        loadBuckets();
    }

    private void loadBuckets() {
        Async.run(new Runnable() {
            @Override
            public void run() {
                final List<Bucket> bucketList = LocalMediaResourceLoader.getImageBuckets(getContext());
                Async.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResourceBucketManager = new ResourceBucketManager(getContext(), mRvBucketList, mTvSelectBucket, bucketList);
                        mResourceBucketManager.setOnBucketSelectedListener(LightImagePickerPage.this);

                        if (bucketList.size() > 0) {
                            loadPhotoListByBucketId(bucketList.get(0).bucketId);
                        }
                    }
                });
            }
        });
    }

    private void loadPhotoListByBucketId(final String bucketId) {
        Async.run(new Runnable() {
            @Override
            public void run() {
                List<LocalMediaResource> resourceList = mCachedResourceList.get(bucketId);
                if (resourceList == null) {
                    resourceList = LocalMediaResourceLoader.getImagesByBucketId(getContext(), bucketId);
                    if (resourceList != null) {
                        mCachedResourceList.put(bucketId, resourceList);
                    }
                }
                final List<LocalMediaResource> finalResourceList = resourceList;
                Async.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResourceList = finalResourceList;
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityCompat.requestPermissions(getContext(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.light_image_picker_tv_select_bucket:
            case R.id.light_image_picker_view_bucket_list_bg:
                toggleBucketList();
                break;
        }
    }

    private void toggleBucketList() {
        int visibility = mRvBucketList.getVisibility();
        mViewBucketListBg.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mRvBucketList.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBucketSelected(Bucket selectedBucket) {
        loadPhotoListByBucketId(selectedBucket.bucketId);
    }

    private class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getContext().getLayoutInflater().inflate(R.layout.light_image_picker_photo_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LocalMediaResource resource = mResourceList.get(position);
            Glide.with(getContext())
                    .load(resource.path)
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.drawable.light_image_picker_placeholder)
                    .into(holder.ivAlbumItem);
        }

        @Override
        public int getItemCount() {
            return mResourceList != null ? mResourceList.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivAlbumItem;

            public ViewHolder(View itemView) {
                super(itemView);
                ivAlbumItem = (ImageView) itemView.findViewById(R.id.light_image_picker_iv_album_item);
            }
        }
    }

}
