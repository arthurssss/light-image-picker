package net.neevek.android.lib.lightimagepicker;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.request.target.ViewTarget;

import net.neevek.android.lib.lightimagepicker.model.OnImagesSelectedListener;
import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.lightimagepicker.util.L;
import net.neevek.android.lib.paginize.PageActivity;

public class LightImagePickerActivity extends PageActivity implements OnImagesSelectedListener {
    public final static String DATA_SELECT_IMAGES = "data_selected_images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);

        if (savedInstanceState == null) {
            LightImagePickerPage
                    .create(this, "图片", null)
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
