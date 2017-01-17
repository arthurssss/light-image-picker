package net.neevek.android.lib.lightimagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.request.target.ViewTarget;

import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.lightimagepicker.util.L;
import net.neevek.android.lib.paginize.PageActivity;

public class LightImagePickerActivity extends PageActivity implements OnImagesSelectedListener {
    public final static String PARAM_TITLE = "param_title";
    public final static String PARAM_SELECTED_IMAGES = "data_selected_images";
    public final static String DATA_SELECT_IMAGES = "data_selected_images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);

        if (savedInstanceState == null) {
            String title = getIntent().getStringExtra(PARAM_TITLE);
            String[] selectedImages = getIntent().getStringArrayExtra(PARAM_SELECTED_IMAGES);
            LightImagePickerPage
                    .create(this, TextUtils.isEmpty(title) ? getString(R.string.light_image_picker_album) : title, selectedImages)
                    .setOnImagesSelectedListener(this)
                    .show(false);
        }
    }

    @Override
    public void onImagesSelected(String[] images) {
        L.d(">>>>>>> selected images: %d", images.length);
        Intent data = new Intent();
        data.putExtra(DATA_SELECT_IMAGES, images);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onCancelled() {
        setResult(RESULT_CANCELED, null);
    }

    static {
        ViewTarget.setTagId(R.id.glide_tag);
    }
}
