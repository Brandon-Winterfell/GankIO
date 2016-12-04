package com.huahua.gankio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * 关于页面
 *
 * Created by Administrator on 2016/12/4.
 */

public class ActivityAbout extends AppCompatActivity {

    TextView mProjectAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mProjectAddress = (TextView) findViewById(R.id.textview_address);
        mProjectAddress.setTextIsSelectable(true);

    }
}

































