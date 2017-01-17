package net.neevek.android.lib.lightimagepicker;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.request.target.ViewTarget;

import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.paginize.PageActivity;

public class LightImagePickerActivity extends PageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);
        getPageManager().enableSwipeToHide(true);
        getPageManager().useSwipePageTransitionEffect();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (savedInstanceState == null) {
            LightImagePickerPage.create(this, "图片", null).show(false);
        }
    }

    static {
        ViewTarget.setTagId(R.id.glide_tag);
    }
}
