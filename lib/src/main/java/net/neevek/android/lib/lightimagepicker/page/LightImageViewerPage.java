package net.neevek.android.lib.lightimagepicker.page;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.neevek.android.lib.lightimagepicker.BuildConfig;
import net.neevek.android.lib.lightimagepicker.LightImagePickerActivity;
import net.neevek.android.lib.lightimagepicker.P;
import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.util.Async;
import net.neevek.android.lib.lightimagepicker.util.L;
import net.neevek.android.lib.lightimagepicker.util.ToolbarHelper;
import net.neevek.android.lib.lightimagepicker.util.Util;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectViewByName;
import net.neevek.android.lib.paginize.annotation.PageLayoutName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_SELECTED_IMAGES;
import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_SELECTED_IMAGES_START_INDEX;
import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_SHOW_SAVE_BUTTON;
import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_TITLE;

/**
 * Lilith Games
 * Created by JiaminXie on 16/01/2017.
 */

@PageLayoutName(P.layout.light_image_picker_page_viewer)
public class LightImageViewerPage extends Page implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private final static int HIDE_BARS_ANIMATION_DURATION = 150;
    private final static int SHOW_PAGE_ANIMATION_DURATION = 200;

    @InjectViewByName(P.id.light_image_picker_toolbar)
    private Toolbar mToolbar;
    @InjectViewByName(P.id.light_image_picker_top_bar)
    private View mViewTopBar;
    @InjectViewByName(value = P.id.light_image_picker_vp_photo_pager, listenerTypes = ViewPager.OnPageChangeListener.class)
    private ViewPager mVpPhotoPager;
    @InjectViewByName(value = P.id.light_image_picker_btn_save, listenerTypes = View.OnClickListener.class)
    private TextView mBtnSave;

    private List<String> mImageUriList;
    private int mStartItemIndex;

    public static LightImageViewerPage create(
            PageActivity pageActivity,
            ArrayList<String> selectedImages,
            int startItemIndex,
            boolean showSaveButton) {
        LightImageViewerPage viewerPage = new LightImageViewerPage(pageActivity);
        viewerPage.getBundle().putStringArrayList(LightImagePickerActivity.PARAM_SELECTED_IMAGES, selectedImages);
        viewerPage.getBundle().putInt(PARAM_SELECTED_IMAGES_START_INDEX, startItemIndex);
        viewerPage.getBundle().putBoolean(PARAM_SHOW_SAVE_BUTTON, showSaveButton);
        return viewerPage;
    }

    private LightImageViewerPage(PageActivity pageActivity) {
        super(pageActivity);
        ToolbarHelper.setNavigationIconEnabled(mToolbar, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().finish();
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARAM_SELECTED_IMAGES_START_INDEX, mVpPhotoPager.getCurrentItem());
    }

    @Override
    public void onShow() {
        super.onShow();

        mToolbar.setTitle(getBundle().getString(PARAM_TITLE));
        mImageUriList = getBundle().getStringArrayList(PARAM_SELECTED_IMAGES);
        mStartItemIndex = getBundle().getInt(PARAM_SELECTED_IMAGES_START_INDEX);

        mBtnSave.setVisibility(getBundle().getBoolean(PARAM_SHOW_SAVE_BUTTON) ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= 16) {
            getContext().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        mVpPhotoPager.setAdapter(new PreviewImagePagerAdapter());

        if (mImageUriList == null) {
            mImageUriList = Collections.EMPTY_LIST;
        }

        if (mStartItemIndex >= mImageUriList.size()) {
            mStartItemIndex = 0;
        }

        updateTitle(mStartItemIndex + 1);
        mVpPhotoPager.setCurrentItem(mStartItemIndex, false);
    }

    @Override
    public void onHide() {
        super.onHide();
        clearFullScreenFlags();
    }

    private void clearFullScreenFlags() {
        getContext().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void updateTitle(int index) {
        mToolbar.setTitle(getString(R.string.light_image_picker_preview_items, index, mImageUriList.size()));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override
    public void onPageScrollStateChanged(int state) { }
    @Override
    public void onPageSelected(int position) {
        updateTitle(position + 1);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.light_image_picker_layout_preview_container) {
            toggleTopBarAndBottomBar();

        } else if (v.getId() == R.id.light_image_picker_btn_save) {
            saveImage();
        }
    }

    private void saveImage() {
        if (mImageUriList.size() == 0) {
            Util.showToast(getContext(), getString(R.string.light_image_picker_image_not_existing));
            return;
        }

        final String itemUri = mImageUriList.get(mVpPhotoPager.getCurrentItem());
        int lastSlashIndex = itemUri.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            Util.showToast(getContext(), getString(R.string.light_image_picker_image_not_existing));
            return;
        }

        try {
            final File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            final File outputFile = new File(outputDir, "lip_" + itemUri.substring(lastSlashIndex + 1));

            if (outputFile.exists()) {
                Async.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.light_image_picker_save_image)
                                .setMessage(getString(R.string.light_image_picker_image_exists, outputFile.getAbsolutePath()))
                                .setPositiveButton(getString(R.string.light_image_picker_yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        doSaveImage(itemUri, outputFile);
                                    }
                                })
                                .setNegativeButton(getString(R.string.light_image_picker_no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                });

            } else {
                doSaveImage(itemUri, outputFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Util.showToast(getContext(), getString(R.string.light_image_picker_saving_image_failed));
        }
    }

    private void doSaveImage(final String imageUri, final File outputFile) {
        Async.run(new Runnable() {
            @Override
            public void run() {
                try {
                    Util.showToast(getContext(), getString(R.string.light_image_picker_saving));
                    final File srcFile = Glide.with(getContext())
                            .load(imageUri)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    if (srcFile == null || !srcFile.exists()) {
                        Util.showToast(getContext(), getString(R.string.light_image_picker_saving_image_failed));
                        return;
                    }

                    if (!Util.copyFile(srcFile, outputFile)) {
                        Util.showToast(getContext(), getString(R.string.light_image_picker_saving_image_failed));
                        return;
                    }

                    // save file to content provider
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, outputFile.getAbsolutePath());
                    try {
                        getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    } catch (SQLiteConstraintException e) {
                        // ignore constraint exception
                    }

                    Util.showToast(getContext(), getString(R.string.light_image_picker_image_saved));
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.showToast(getContext(), getString(R.string.light_image_picker_saving_image_failed));
                }
            }
        });
    }

    @Override
    public boolean canSwipeToHide() {
        return false;
    }

    @Override
    public boolean onPushPageAnimation(View oldPageView, View newPageView, AnimationDirection animationDirection) {
        newPageView.setScaleX(1.5f);
        newPageView.setScaleY(1.5f);
        newPageView.setAlpha(0);
        newPageView.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(getAnimationDuration())
                .start();
        return true;
    }

    @Override
    public boolean onPopPageAnimation(View oldPageView, View newPageView, AnimationDirection animationDirection) {
        oldPageView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(getAnimationDuration())
                .start();
        return true;
    }

    @Override
    public int getAnimationDuration() {
        return SHOW_PAGE_ANIMATION_DURATION;
    }

    private void toggleTopBarAndBottomBar() {
        Window window = getContext().getWindow();
        if (mViewTopBar.getTranslationY() == 0) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mViewTopBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationYBy(-(mViewTopBar.getHeight()+mViewTopBar.getTop())).start();
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mViewTopBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationY(0).start();
        }
    }

    private class PreviewImagePagerAdapter extends PagerAdapter implements GestureController.OnGestureListener {
        class ViewHolder {
            public ViewGroup layoutItemContainer;
            public GestureImageView ivPreviewImage;
            public ProgressBar pbPreview;
            public ViewHolder(ViewGroup layoutItemContainer) {
                this.layoutItemContainer = layoutItemContainer;
                ivPreviewImage = (GestureImageView) layoutItemContainer.findViewById (R.id.light_image_picker_iv_preview_image);
                pbPreview = (ProgressBar) layoutItemContainer.findViewById (R.id.light_image_picker_pb_preview);
            }
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final ViewHolder holder = new ViewHolder((ViewGroup)getContext().getLayoutInflater().inflate(R.layout.light_image_picker_preview_item, container, false));
            holder.layoutItemContainer.setTag(holder);

            holder.layoutItemContainer.setOnClickListener(LightImageViewerPage.this);

//            holder.ivPreviewImage.getController().enableScrollInViewPager(mVpPhotoPager);
            holder.ivPreviewImage.getController().setOnGesturesListener(this);
            holder.ivPreviewImage.getController().getSettings().setMaxZoom(5);

            String imageUri = mImageUriList.get(position);

            container.addView(holder.layoutItemContainer);

            Glide.with(getContext())
                    .load(imageUri)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.ivPreviewImage.setVisibility(View.VISIBLE);
                            holder.pbPreview.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .override(getResources().getDisplayMetrics().widthPixels/3, getResources().getDisplayMetrics().heightPixels/3)
//                    .sizeMultiplier(0.3f)
                    .dontTransform()
                    .into(holder.ivPreviewImage);

//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                            ivPreviewImage.setImageBitmap(resource);
//                        }
//                    });

            return holder.layoutItemContainer;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ViewHolder holder = (ViewHolder)((View)object).getTag();
            if (BuildConfig.DEBUG) {
                GlideBitmapDrawable d = (GlideBitmapDrawable) holder.ivPreviewImage.getDrawable();
                L.d(">>>>>>> remove: %dx%d", d.getIntrinsicWidth(), d.getIntrinsicHeight());
            }
            container.removeView(holder.layoutItemContainer);
        }

        @Override
        public int getCount() {
            return mImageUriList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

//        @Override
//        public int getItemPosition(Object object) {
//            // see http://stackoverflow.com/a/7287121/668963
//            return POSITION_NONE;
//        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            return false;
        }
        @Override
        public void onDown(@NonNull MotionEvent event) { }
        @Override
        public void onUpOrCancel(@NonNull MotionEvent event) { }
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            toggleTopBarAndBottomBar();
            return true;
        }
        @Override
        public void onLongPress(@NonNull MotionEvent event) { }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) { return false; }
    }
}
