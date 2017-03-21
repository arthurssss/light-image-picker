package net.neevek.android.lib.lightimagepicker.page;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.neevek.android.lib.lightimagepicker.LightImagePickerActivity;
import net.neevek.android.lib.lightimagepicker.P;
import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.pojo.LocalMediaResource;
import net.neevek.android.lib.lightimagepicker.util.ToolbarHelper;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectViewByName;
import net.neevek.android.lib.paginize.annotation.PageLayoutName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lilith Games
 * Created by JiaminXie on 16/01/2017.
 */

@PageLayoutName(P.layout.light_image_picker_page_preview)
public class LightImagePickerPreviewPage extends Page
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ViewPager.OnPageChangeListener {
    private final static int HIDE_BARS_ANIMATION_DURATION = 150;
    private final static int SHOW_PAGE_ANIMATION_DURATION = 200;

    @InjectViewByName(P.id.light_image_picker_toolbar)
    private Toolbar mToolbar;
    @InjectViewByName(P.id.light_image_picker_top_bar)
    private View mViewTopBar;
    @InjectViewByName(P.id.light_image_picker_bottom_bar)
    private View mViewBottomBar;
    @InjectViewByName(value = P.id.light_image_picker_vp_photo_pager, listenerTypes = ViewPager.OnPageChangeListener.class)
    private ViewPager mVpPhotoPager;
    @InjectViewByName(value = P.id.light_image_picker_cb_select, listenerTypes = CompoundButton.OnCheckedChangeListener.class)
    private CheckBox mCbSelectImage;
    @InjectViewByName(value = P.id.light_image_picker_btn_send, listenerTypes = View.OnClickListener.class)
    private TextView mBtnSend;

    private List<LocalMediaResource> mResourceList;
    private Set<LocalMediaResource> mSelectedItemSet = new LinkedHashSet<LocalMediaResource>();

    private OnImagesSelectedListener mOnImagesSelectedListener;

    private int mMaxAllowedSelection = 9;
    private int mStartItemIndex;

    public static LightImagePickerPreviewPage create(PageActivity pageActivity, int maxAllowedSelection) {
        LightImagePickerPreviewPage previewPage = new LightImagePickerPreviewPage(pageActivity);
        previewPage.mMaxAllowedSelection = maxAllowedSelection;
        return previewPage;
    }

    private LightImagePickerPreviewPage(PageActivity pageActivity) {
        super(pageActivity);
        ToolbarHelper.setNavigationIconEnabled(mToolbar, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide(true);
            }
        });
    }

    public LightImagePickerPreviewPage setData(@Nullable List<LocalMediaResource> resourceList, @NonNull Set<LocalMediaResource> selectedItemSet) {
        mSelectedItemSet = selectedItemSet;
        mResourceList = resourceList;
        return this;
    }

    public LightImagePickerPreviewPage setOnImagesSelectedListener(OnImagesSelectedListener onImagesSelectedListener) {
        mOnImagesSelectedListener = onImagesSelectedListener;
        return this;
    }

    public LightImagePickerPreviewPage setStartItemIndex(int startItemIndex) {
        mStartItemIndex = startItemIndex;
        return this;
    }

    @Override
    public void onShow() {
        super.onShow();

        if (Build.VERSION.SDK_INT >= 16) {
            getContext().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (mResourceList == null) {
            mResourceList = new ArrayList<LocalMediaResource>();
            if (mSelectedItemSet.size() > 0) {
                mResourceList.addAll(mSelectedItemSet);
            }
        }
        mVpPhotoPager.setAdapter(new PreviewImagePagerAdapter());

        updateSendButton();
        if (mStartItemIndex >= mResourceList.size()) {
            mStartItemIndex = 0;
        }
        if (mStartItemIndex < mResourceList.size()) {
            mCbSelectImage.setChecked(mSelectedItemSet.contains(mResourceList.get(mStartItemIndex)));
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
        mToolbar.setTitle(getString(R.string.light_image_picker_preview_items, index, mResourceList.size()));
    }

    private void updateSendButton() {
        mBtnSend.setEnabled(mSelectedItemSet.size() > 0);
        if (mSelectedItemSet.size() == 0) {
            mBtnSend.setText(R.string.light_image_picker_send);
        } else {
            mBtnSend.setText(getString(R.string.light_image_picker_send_selected_items, mSelectedItemSet.size()));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.light_image_picker_iv_preview_image_with_scaling) {
            toggleTopBarAndBottomBar();

        } else if (v.getId() == R.id.light_image_picker_btn_send) {
            finishSelectingImages();
        }
    }

    private void finishSelectingImages() {
        if (mOnImagesSelectedListener == null) {
            return;
        }

        ArrayList<String> selectedImages = new ArrayList<String>(mSelectedItemSet.size());
        for (LocalMediaResource res : mSelectedItemSet) {
            selectedImages.add(res.path);
        }
        mOnImagesSelectedListener.onImagesSelected(selectedImages);

        if (getContext() instanceof LightImagePickerActivity) {
            clearFullScreenFlags();
            getContext().finish();
        } else {
            hide(true);
        }
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

        LocalMediaResource resource = mResourceList.get(mVpPhotoPager.getCurrentItem());
        if (isChecked) {
            mSelectedItemSet.add(resource);
        } else {
            mSelectedItemSet.remove(resource);
        }

        updateSendButton();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override
    public void onPageScrollStateChanged(int state) { }
    @Override
    public void onPageSelected(int position) {
        mCbSelectImage.setChecked(mSelectedItemSet.contains(mResourceList.get(position)));
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
            mViewBottomBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationYBy(mViewBottomBar.getHeight()).start();
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mViewTopBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationY(0).start();
            mViewBottomBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationY(0).start();
        }
    }

    private class PreviewImagePagerAdapter extends PagerAdapter {
        class ViewHolder {
            public ViewGroup layoutItemContainer;
            public SubsamplingScaleImageView ivPreviewImageWithScaling;
            public ImageView ivPreviewImage;
            public ProgressBar pbPreview;
            public ViewHolder(ViewGroup layoutItemContainer) {
                this.layoutItemContainer = layoutItemContainer;
                ivPreviewImageWithScaling = (SubsamplingScaleImageView) layoutItemContainer.findViewById (R.id.light_image_picker_iv_preview_image_with_scaling);
                ivPreviewImage = (ImageView) layoutItemContainer.findViewById (R.id.light_image_picker_iv_preview_image);
                pbPreview = (ProgressBar) layoutItemContainer.findViewById (R.id.light_image_picker_pb_preview);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ViewHolder holder = new ViewHolder((ViewGroup)getContext().getLayoutInflater().inflate(R.layout.light_image_picker_preview_item, container, false));
            holder.layoutItemContainer.setTag(holder);

            LocalMediaResource resource = mResourceList.get(position);

            container.addView(holder.layoutItemContainer);

            if (resource.path.toLowerCase().endsWith(".gif")) {
                Glide.with(getContext())
                        .load(resource.path)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(new SimpleTarget<GifDrawable>() {
                            @Override
                            public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
                                holder.ivPreviewImageWithScaling.setVisibility(View.GONE);
                                holder.ivPreviewImage.setVisibility(View.VISIBLE);
                                holder.pbPreview.setVisibility(View.GONE);
                                holder.ivPreviewImage.setImageDrawable(resource);
                                resource.start();
                            }
                        });

            } else {
                holder.ivPreviewImageWithScaling.setOnClickListener(LightImagePickerPreviewPage.this);
                holder.ivPreviewImageWithScaling.setMaxScale(10);
                holder.ivPreviewImageWithScaling.setDoubleTapZoomScale(10);
                holder.ivPreviewImageWithScaling.setDoubleTapZoomDuration(200);
                holder.ivPreviewImageWithScaling.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);

                Glide.with(getContext())
                        .load(resource.path)
                        .asBitmap()
                        .override(getResources().getDisplayMetrics().widthPixels / 2, getResources().getDisplayMetrics().heightPixels / 2)
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                holder.ivPreviewImage.setVisibility(View.GONE);
                                holder.ivPreviewImageWithScaling.setVisibility(View.VISIBLE);
                                holder.pbPreview.setVisibility(View.GONE);
                                holder.ivPreviewImageWithScaling.setImage(ImageSource.bitmap(resource));
                            }
                        });
            }

            return holder.layoutItemContainer;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ViewHolder holder = (ViewHolder)((View)object).getTag();
            container.removeView(holder.layoutItemContainer);
        }

        @Override
        public int getCount() {
            return mResourceList.size();
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
    }
}
