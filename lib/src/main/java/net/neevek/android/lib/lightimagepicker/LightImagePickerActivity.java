package net.neevek.android.lib.lightimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.bumptech.glide.request.target.ViewTarget;

import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.lightimagepicker.page.LightImageViewerPage;
import net.neevek.android.lib.paginize.PageActivity;

import java.util.ArrayList;

public class LightImagePickerActivity extends PageActivity implements OnImagesSelectedListener {
    public final static String PARAM_OPENING_OPTION = "param_opening_option";
    public final static String PARAM_TITLE = "param_title";
    public final static String PARAM_SELECTED_IMAGES_START_INDEX = "param_selected_images_start_index";
    public final static String PARAM_SELECTED_IMAGES = "param_selected_images";
    public final static String PARAM_SHOW_SAVE_BUTTON = "param_show_save_button"; // for LightImageViewerPage only
    public final static String RESULT_SELECTED_IMAGES = "result_selected_images";

    public enum OpeningOption {
        IMAGE_PICKER,
        IMAGE_VIEWER;
    }

    public static void show(Activity activity,
                            int requestCode,
                            OpeningOption openingOption) {
        show(activity, requestCode, openingOption, null, null, 0, false);
    }

    public static void show(Activity activity,
                            int requestCode,
                            OpeningOption openingOption,
                            String title,
                            ArrayList<String> selectedImages,
                            int startIndex,
                            boolean showSaveButton) {
        Intent intent = new Intent(activity, LightImagePickerActivity.class);
        intent.putExtra(PARAM_OPENING_OPTION, openingOption.ordinal());
        intent.putExtra(PARAM_TITLE, title);
        intent.putExtra(PARAM_SELECTED_IMAGES, selectedImages);
        intent.putExtra(PARAM_SELECTED_IMAGES_START_INDEX, startIndex);
        intent.putExtra(PARAM_SHOW_SAVE_BUTTON, showSaveButton);
        if (openingOption == OpeningOption.IMAGE_PICKER) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);

        int openingOption = getIntent().getIntExtra(PARAM_OPENING_OPTION, -1);
        if (openingOption == -1) {
            if (savedInstanceState == null) {
                finish();
            }
            return;
        }

        if (savedInstanceState == null) {
            String title = getIntent().getStringExtra(PARAM_TITLE);
            ArrayList<String> selectedImages = getIntent().getStringArrayListExtra(PARAM_SELECTED_IMAGES);
            if (openingOption == OpeningOption.IMAGE_PICKER.ordinal()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                LightImagePickerPage
                        .create(this, title, selectedImages)
                        .setOnImagesSelectedListener(this)
                        .show(false);
            } else if (openingOption == OpeningOption.IMAGE_VIEWER.ordinal()) {
                boolean showSaveButton = getIntent().getBooleanExtra(PARAM_SHOW_SAVE_BUTTON, false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                LightImageViewerPage
                        .create(this, selectedImages, getIntent().getIntExtra(PARAM_SELECTED_IMAGES_START_INDEX, 0), showSaveButton)
                        .show(false);
            } else {
                finish();
            }
        }
    }

    @Override
    public void onImagesSelected(ArrayList<String> images) {
        Intent data = new Intent();
        data.putExtra(RESULT_SELECTED_IMAGES, images);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onCancelled() {
        setResult(RESULT_CANCELED, null);
    }

    static {
        try {
            ViewTarget.setTagId(R.id.light_image_picker_glide_tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
