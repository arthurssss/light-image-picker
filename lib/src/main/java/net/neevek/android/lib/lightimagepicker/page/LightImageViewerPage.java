package net.neevek.android.lib.lightimagepicker.page;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

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
import net.neevek.android.lib.lightimagepicker.util.L;
import net.neevek.android.lib.lightimagepicker.util.ToolbarHelper;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectViewByName;
import net.neevek.android.lib.paginize.annotation.PageLayoutName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_SELECTED_IMAGES;
import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_SELECTED_IMAGES_START_INDEX;
import static net.neevek.android.lib.lightimagepicker.LightImagePickerActivity.PARAM_TITLE;

/**
 * Lilith Games
 * Created by JiaminXie on 16/01/2017.
 */

@PageLayoutName(P.layout.light_image_picker_page_viewer)
public class LightImageViewerPage extends Page implements ViewPager.OnPageChangeListener {
    private final static int HIDE_BARS_ANIMATION_DURATION = 150;
    private final static int SHOW_PAGE_ANIMATION_DURATION = 200;

    @InjectViewByName(P.id.light_image_picker_toolbar)
    private Toolbar mToolbar;
    @InjectViewByName(P.id.light_image_picker_top_bar)
    private View mViewTopBar;
    @InjectViewByName(value = P.id.light_image_picker_vp_photo_pager, listenerTypes = ViewPager.OnPageChangeListener.class)
    private ViewPager mVpPhotoPager;

    private List<String> mImageUriList;
    private int mStartItemIndex;

    public static LightImageViewerPage create(PageActivity pageActivity, ArrayList<String> selectedImages, int startItemIndex) {
        LightImageViewerPage viewerPage = new LightImageViewerPage(pageActivity);
        viewerPage.getBundle().putStringArrayList(LightImagePickerActivity.PARAM_SELECTED_IMAGES, selectedImages);
        viewerPage.getBundle().putInt(PARAM_SELECTED_IMAGES_START_INDEX, startItemIndex);
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

    private class PreviewImagePagerAdapter extends PagerAdapter implements GestureController.OnGestureListener, View.OnClickListener {
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
        public void onClick(View v) {
            toggleTopBarAndBottomBar();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ViewHolder holder = new ViewHolder((ViewGroup)getContext().getLayoutInflater().inflate(R.layout.light_image_picker_preview_item, container, false));
            holder.layoutItemContainer.setTag(holder);

            holder.layoutItemContainer.setOnClickListener(this);

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
