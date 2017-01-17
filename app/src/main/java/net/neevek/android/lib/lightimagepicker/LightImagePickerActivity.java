package net.neevek.android.lib.lightimagepicker;

import android.os.Bundle;

import com.bumptech.glide.request.target.ViewTarget;

import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.paginize.PageActivity;

public class LightImagePickerActivity extends PageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);

        if (savedInstanceState == null) {
            LightImagePickerPage.create(this, "图片", null).show(false);
        }
    }

    static {
        ViewTarget.setTagId(R.id.glide_tag);
    }
}
