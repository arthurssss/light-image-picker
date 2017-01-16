package net.neevek.android.lib.lightimagepicker.page;

import android.Manifest;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.model.LocalMediaResourceLoader;
import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.pojo.Bucket;
import net.neevek.android.lib.lightimagepicker.pojo.LocalMediaResource;
import net.neevek.android.lib.lightimagepicker.util.Async;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectView;
import net.neevek.android.lib.paginize.annotation.PageLayout;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lilith Games
 * Created by JiaminXie on 12/01/2017.
 */

@PageLayout(R.layout.light_image_picker_page_album)
public class LightImagePickerPage extends Page implements ResourceBucketManager.OnBucketSelectedListener, View.OnClickListener {
    private final static String PARAM_TITLE = "param_title";
    private final static String PARAM_SELECTED_IMAGES = "param_selected_images";

    @InjectView(R.id.light_image_picker_toolbar)
    private Toolbar mToolbar;
    @InjectView(R.id.light_image_picker_rv_photo_list)
    private RecyclerView mRvPhotoList;
    @InjectView(R.id.light_image_picker_rv_bucket_list)
    private RecyclerView mRvBucketList;
    @InjectView(value = R.id.light_image_picker_view_bucket_list_bg, listenerTypes = View.OnClickListener.class)
    private View mViewBucketListBg;
    @InjectView(value = R.id.light_image_picker_tv_select_bucket, listenerTypes = View.OnClickListener.class)
    private TextView mTvSelectBucket;
    @InjectView(value = R.id.light_image_picker_tv_preview, listenerTypes = View.OnClickListener.class)
    private TextView mTvPreview;
    @InjectView(value = R.id.light_image_picker_btn_send, listenerTypes = View.OnClickListener.class)
    private TextView mBtnSend;

    private AlbumListAdapter mAdapter;
    private List<LocalMediaResource> mResourceList;
    private Set<LocalMediaResource> mSelectedItemSet = new LinkedHashSet<LocalMediaResource>();

    private Map<String, List<LocalMediaResource>> mCachedResourceList = new HashMap<String, List<LocalMediaResource>>();
    private ResourceBucketManager mResourceBucketManager;

    private OnImagesSelectedListener mOnImagesSelectedListener;
    private int mMaxAllowedSelection = 9;

    public static LightImagePickerPage create(PageActivity pageActivity, String title, String[] selectedImages) {
        LightImagePickerPage lightImagePickerPage = new LightImagePickerPage(pageActivity);
        lightImagePickerPage.getBundle().putString(PARAM_TITLE, title);
        lightImagePickerPage.getBundle().putStringArray(PARAM_SELECTED_IMAGES, selectedImages);
        return lightImagePickerPage;
    }

    private LightImagePickerPage(PageActivity pageActivity) {
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

    @Override
    public void onShow() {
        super.onShow();

        mToolbar.setTitle(getBundle().getString(PARAM_TITLE));
        String[] images = getBundle().getStringArray(PARAM_SELECTED_IMAGES);
        if (images != null) {
            for (int i = 0; i < images.length; ++i) {
                mSelectedItemSet.add(new LocalMediaResource(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, images[i], 0, 0));
            }
        }
    }

    @Override
    public void onUncover(Object arg) {
        super.onUncover(arg);
        mAdapter.notifyDataSetChanged();
        updateButtonsState();
    }

    public void setMaxAllowedSelection(int maxAllowedSelection) {
        mMaxAllowedSelection = maxAllowedSelection;
    }

    public void setOnImagesSelectedListener(OnImagesSelectedListener onImagesSelectedListener) {
        mOnImagesSelectedListener = onImagesSelectedListener;
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
            case R.id.light_image_picker_tv_preview:
                LightImagePickerPreviewPage.create(getContext(), mMaxAllowedSelection)
                        .setData(null, mSelectedItemSet)
                        .show(true);
                break;
            case R.id.light_image_picker_btn_send:
                if (mOnImagesSelectedListener != null) {
                    String[] selectedImages = new String[mSelectedItemSet.size()];
                    int index = 0;
                    for (LocalMediaResource res : mSelectedItemSet) {
                        selectedImages[index] = res.path;
                        ++index;
                    }
                    mOnImagesSelectedListener.onImagesSelected(selectedImages);
                }
                break;
        }
    }

    private void toggleBucketList() {
        int visibility = mRvBucketList.getVisibility();
        mViewBucketListBg.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mRvBucketList.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void updateButtonsState() {
        mTvPreview.setEnabled(mSelectedItemSet.size() > 0);
        mBtnSend.setEnabled(mSelectedItemSet.size() > 0);
        if (mSelectedItemSet.size() == 0) {
            mTvPreview.setText(R.string.light_image_picker_preview);
            mBtnSend.setText(R.string.light_image_picker_send);
        } else {
            mTvPreview.setText(getString(R.string.light_image_picker_preview_selected_items, mSelectedItemSet.size()));
            mBtnSend.setText(getString(R.string.light_image_picker_send_selected_items, mSelectedItemSet.size()));
        }
    }

    @Override
    public void onBucketSelected(Bucket selectedBucket) {
        loadPhotoListByBucketId(selectedBucket.bucketId);
        toggleBucketList();
    }

    private class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder>
            implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = new ViewHolder(getContext().getLayoutInflater().inflate(R.layout.light_image_picker_photo_list_item, parent, false));
            holder.cbItemCheckbox.setOnCheckedChangeListener(this);
            holder.ivItemImage.setOnClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LocalMediaResource resource = mResourceList.get(position);
            Glide.with(getContext())
                    .load(resource.path)
                    .centerCrop()
                    .crossFade()
                    .into(holder.ivItemImage);

            boolean selected = mSelectedItemSet.contains(resource);
            holder.viewItemMask.setVisibility(selected ? View.VISIBLE : View.GONE);
            holder.cbItemCheckbox.setChecked(selected);

            holder.cbItemCheckbox.setTag(position);
            holder.ivItemImage.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mResourceList != null ? mResourceList.size() : 0;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!buttonView.isPressed()) {
                return;
            }

            if (isChecked && mSelectedItemSet.size() >= mMaxAllowedSelection) {
                buttonView.setChecked(false);
                Toast.makeText(getContext(), getString(R.string.light_image_picker_select_item_count_limit, mMaxAllowedSelection), Toast.LENGTH_SHORT).show();
                return;
            }

            Integer position = (Integer) buttonView.getTag();
            if (position == null) {
                return;
            }

            LocalMediaResource resource = mResourceList.get(position);
            if (isChecked) {
                mSelectedItemSet.add(resource);
            } else {
                mSelectedItemSet.remove(resource);
            }
            mAdapter.notifyItemChanged(position);

            updateButtonsState();
        }

        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            if (position == null) {
                return;
            }
            LightImagePickerPreviewPage.create(getContext(), mMaxAllowedSelection)
                    .setData(mResourceList, mSelectedItemSet)
                    .show(true);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivItemImage;
            private View viewItemMask;
            private CheckBox cbItemCheckbox;

            public ViewHolder(View itemView) {
                super(itemView);
                ivItemImage = (ImageView) itemView.findViewById(R.id.light_image_picker_iv_item_image);
                viewItemMask = itemView.findViewById(R.id.light_image_picker_view_photo_list_item_mask);
                cbItemCheckbox = (CheckBox) itemView.findViewById(R.id.light_image_picker_cb_photo_list_item_checkbox);
            }
        }
    }
}
