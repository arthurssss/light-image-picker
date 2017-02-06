package net.neevek.android.lib.lightimagepicker.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.neevek.android.lib.lightimagepicker.LightImagePickerActivity;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_show_image_picker).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        LightImagePickerActivity.showPicker(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println("select result: " + Arrays.toString(data.getStringArrayExtra(LightImagePickerActivity.RESULT_SELECTED_IMAGES)));
        } else {
            System.out.println("result code: " + resultCode);
        }
    }
}
