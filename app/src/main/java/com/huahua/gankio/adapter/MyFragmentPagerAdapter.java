package com.huahua.gankio.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016/8/17.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{

    private Context mContext;

    // 标题的集合
    private static final String[] mTitles = {"福利", "Android", "iOS", "前端"};

    // 作为数据源的Fragment的list集合
    private List<Fragment> mFragmentList;

    // 构造方法
    public MyFragmentPagerAdapter(FragmentManager fm,
                                  Context context,
                                  List<Fragment> fragmentList) {
        super(fm);
        this.mContext = context;  // 传入上下文
        this.mFragmentList = fragmentList; // 传入作为数据源的Fragment的list集合
    }

    // 返回当前位置对应的Fragment项
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    // 返回数据源的个数
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    // Tab页卡的标题
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

}




















