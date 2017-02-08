package net.neevek.android.lib.lightimagepicker.model;

import java.util.ArrayList;

/**
 * Lilith Games
 * Created by JiaminXie on 16/01/2017.
 */

public interface OnImagesSelectedListener {
    void onImagesSelected(ArrayList<String> images);
    void onCancelled();
}
