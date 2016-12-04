package com.huahua.gankio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.huahua.gankio.R;
import com.huahua.gankio.adapter.FragmentViewAdapter;
import com.huahua.gankio.application.MyApplication;
import com.huahua.gankio.bean.Girl;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Administrator on 2016/9/25.
 */

public class ActivityImageView extends AppCompatActivity {

    private Toolbar toolbar = null;
    private ViewPager mViewPager;

    private int currentPosition = 0;

    private List<Girl> mGirlList = new ArrayList<Girl>();
    private FragmentViewAdapter mFragmentViewAdapter;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        // 拿到ViewPager需要首先呈现页卡的位置参数
        currentPosition = getIntent().getIntExtra("currentPosition", 0);
        // 拿到数据集
        mGirlList = (List<Girl>) getIntent().getSerializableExtra("girlist");
        Log.i("test", "url : " + mGirlList.toString());


        mViewPager = (ViewPager) findViewById(R.id.viewpager_girl);
        // 创建适配器
        mFragmentViewAdapter = new FragmentViewAdapter(getSupportFragmentManager(), mGirlList);
        // 设置ViewPager的适配器
        mViewPager.setAdapter(mFragmentViewAdapter);
        // 设置ViewPager首先呈现的页卡
        mViewPager.setCurrentItem(currentPosition);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 要是直接按home键 也设置了parent activity的话 会重新加载一次parent activity
                // 调用onBackPressed就不会重新加载一次父activity了  回到在内存中的父activity实例
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}



















