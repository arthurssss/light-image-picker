package net.neevek.android.lib.lightimagepicker;

import android.os.Bundle;

import net.neevek.android.lib.lightimagepicker.page.LightImagePickerPage;
import net.neevek.android.lib.paginize.PageActivity;

public class LightImagePickerActivity extends PageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPageManager().setDebug(BuildConfig.DEBUG);
        getPageManager().enableSwipeToHide(true);
        getPageManager().useSwipePageTransitionEffect();

        new LightImagePickerPage(this).show(false);
    }
}
