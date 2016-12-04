package com.huahua.gankio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.huahua.gankio.adapter.MyFragmentPagerAdapter;
import com.huahua.gankio.fragment.fragmentAndroid;
import com.huahua.gankio.fragment.fragmentIOS;
import com.huahua.gankio.fragment.fragmentGirl;
import com.huahua.gankio.fragment.fragmentQianDuan;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    // 装载数据源Fragment的集合
    private List<Fragment> mFragmentList;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private Fragment mFragmentGirl;
    private Fragment mFragmentAndroid;
    private Fragment mFragmentIOS;
    private Fragment mFragmentQianDuan;

    Long exitTime = 0L ; // 按两次回退键退出程序

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * 初始化控件
         */
        initView();

    }

    /**
     * 初始化控件
     */
    private void initView() {

        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);


        mFragmentList = new ArrayList<>();

        mFragmentGirl = new fragmentGirl();
        mFragmentAndroid = new fragmentAndroid();
        mFragmentIOS = new fragmentIOS();
        mFragmentQianDuan = new fragmentQianDuan();

        mFragmentList.add(mFragmentGirl);
        mFragmentList.add(mFragmentAndroid);
        mFragmentList.add(mFragmentIOS);
        mFragmentList.add(mFragmentQianDuan);


        mFragmentPagerAdapter = new MyFragmentPagerAdapter(
                getSupportFragmentManager(),
                this,
                mFragmentList);

        //  mFragmentList.size  全部页面都会缓存  这样子的话用
        // 设置缓存页面，当前页面的相邻N各页面都会被缓存
        mViewPager.setOffscreenPageLimit(mFragmentList.size());
        mViewPager.setAdapter(mFragmentPagerAdapter);

        // 将TabLayout与ViewPager联系起来
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, ActivityAbout.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(MainActivity.this, "再点一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}




























