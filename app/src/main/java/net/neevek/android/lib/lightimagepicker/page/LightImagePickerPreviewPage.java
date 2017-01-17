package net.neevek.android.lib.lightimagepicker.page;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;

import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.pojo.LocalMediaResource;
import net.neevek.android.lib.lightimagepicker.util.ToolbarHelper;
import net.neevek.android.lib.paginize.Page;
import net.neevek.android.lib.paginize.PageActivity;
import net.neevek.android.lib.paginize.annotation.InjectView;
import net.neevek.android.lib.paginize.annotation.PageLayout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lilith Games
 * Created by JiaminXie on 16/01/2017.
 */

@PageLayout(R.layout.light_image_picker_page_preview)
public class LightImagePickerPreviewPage extends Page
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ViewPager.OnPageChangeListener {
    private final static int HIDE_BARS_ANIMATION_DURATION = 150;

    @InjectView(R.id.light_image_picker_toolbar)
    private Toolbar mToolbar;
    @InjectView(R.id.light_image_picker_top_bar)
    private View mViewTopBar;
    @InjectView(R.id.light_image_picker_bottom_bar)
    private View mViewBottomBar;
    @InjectView(value = R.id.light_image_picker_vp_photo_pager, listenerTypes = ViewPager.OnPageChangeListener.class)
    private ViewPager mVpPhotoPager;
    @InjectView(value = R.id.light_image_picker_cb_select, listenerTypes = CompoundButton.OnCheckedChangeListener.class)
    private CheckBox mCbSelectImage;
    @InjectView(value = R.id.light_image_picker_btn_send, listenerTypes = View.OnClickListener.class)
    private TextView mBtnSend;

    private List<LocalMediaResource> mResourceList;
    private Set<LocalMediaResource> mSelectedItemSet = new LinkedHashSet<LocalMediaResource>();

    private OnImagesSelectedListener mOnImagesSelectedListener;

    private int mMaxAllowedSelection = 9;
    private int mStartItemIndex;
    private int mOrigSystemUiVisibility;

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
        switch (v.getId()) {
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
            case R.id.light_image_picker_iv_preview_image:
                break;
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

    private class PreviewImagePagerAdapter extends PagerAdapter implements GestureController.OnGestureListener {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            GestureImageView ivPreviewImage = (GestureImageView)getContext().getLayoutInflater().inflate(R.layout.light_image_picker_preview_item, container, false);
            ivPreviewImage.getController().enableScrollInViewPager(mVpPhotoPager);
            ivPreviewImage.getController().setOnGesturesListener(this);
            ivPreviewImage.getController().getSettings().setMaxZoom(5);

            LocalMediaResource resource = mResourceList.get(position);

            container.addView(ivPreviewImage);

            Glide.with(getContext())
                    .load(resource.path)
                    .crossFade()
                    .into(ivPreviewImage);

            return ivPreviewImage;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
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


        private void toggleTopBarAndBottomBar() {
            Window window = getContext().getWindow();
            if (mViewTopBar.getTranslationY() == 0) {
                if (mOrigSystemUiVisibility == 0) {
                    mOrigSystemUiVisibility = window.getDecorView().getSystemUiVisibility();
                }
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                mViewTopBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationYBy(-(mViewTopBar.getHeight()+mViewTopBar.getTop())).start();
                mViewBottomBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationYBy(mViewBottomBar.getHeight()).start();
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                mViewTopBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationY(0).start();
                mViewBottomBar.animate().setDuration(HIDE_BARS_ANIMATION_DURATION).translationY(0).start();
            }
        }

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
