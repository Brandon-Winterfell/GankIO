package com.huahua.gankio.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huahua.gankio.ActivityWebView;
import com.huahua.gankio.network.IDataCallBack;
import com.huahua.gankio.network.OkhttpManager;
import com.huahua.gankio.R;
import com.huahua.gankio.bean.AndroidBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Administrator on 2016/8/17.
 */

public class fragmentAndroid extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private List<AndroidBean> mAndroidBeanList = new ArrayList<>();

    private MyAdapter mMyAdapter;

    private int lastVisibleItem;
    private int page = 1;

    private List<String> mOldSelected = new ArrayList<>(); // 取出保存以前看过文章的id
    List<String> newSelected = new ArrayList<>();

    private boolean isLoadingMore = false;  // 是否正在加载更多标识

    public fragmentAndroid() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从SharedPreference取出浏览过文章的id
        newSelected = loadArray(getContext());

    }

    /**
     * 从SharedPreference取出浏览过文章的id
     * @param context
     * @return
     */
    public List<String> loadArray(Context context) {
        SharedPreferences spf = context.getSharedPreferences("androidSharedPreferences", Context.MODE_PRIVATE);
        mOldSelected.clear();
        int size = spf.getInt("android_Status_size", 0);

        for (int i = 0; i < size; i++) {
            mOldSelected.add(spf.getString("android_Status_" + i, null));
        }

        return mOldSelected;
    }

    /**
     * 保存浏览过文章的id到SharedPreference里
     * @param context
     */
    public void saveArray(Context context) {
        if (newSelected == null) {
            return;
        }

        SharedPreferences spf = context.getSharedPreferences("androidSharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();

        // int oldSize = mOldSelected.size(); // 在后面追加就好了 // 那判断又不一样了
        int newSize = newSelected.size();
        editor.putInt("android_Status_size", newSize);

        for (int i = 0; i < newSize; i++) {
            editor.putString("android_Status_" + i, newSelected.get(i));
        }

        editor.commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_android,
                container,
                false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);  // 初始化控件
        setListener();  // 设置事件监听器


        // 这里应该调用下拉刷新  一进来就刷新数据然后呈现出来
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        saveArray(getContext()); // 保存浏览过文章的id
    }

    private void initView(View view) {

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_android);
        mSwipeRefreshLayout.setOnRefreshListener(this); // 绑定监听器

        // 设置下拉刷新控件的颜色
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);

        // 设置偏移量  不设置这个的话一开始不会显示出来
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        /**
         * 设置RecycleView
         */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_android);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // 默认的动画效果
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 添加适配器
        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
    }

    private void setListener() {

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!isFullScreen(mRecyclerView, mLinearLayoutManager)) {
                    // 不满一屏的情况下return
                    return;
                }

                // 0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余两个item时自动加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 2 >= mLinearLayoutManager.getItemCount()) {

                    if (isLoadingMore) {  // 是否是上拉LoadingMore的状态
                        return;
                    }
                    isLoadingMore = true;

                    // 更改文字为正在加载。。。。
                    mMyAdapter.changeMoreStatus(MyAdapter.LOADING_MORE);

                    OkhttpManager.getAsync("http://gank.io/api/data/Android/15/" + (++page), new IDataCallBack() {
                        @Override
                        public void requestFailure(Request request, IOException e) {
                            isLoadingMore = false;
                            Toast.makeText(getContext(), "something is worrng.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void requestSuccess(String result) {
                            // 这里将在UI线程调用，可以更新UI操作
                            isLoadingMore = false;
                            if (!TextUtils.isEmpty(result)) {

                                if (parseJSON(result) != null) {
                                    // mAndroidBeanList.clear(); // 下拉刷新才要清空数据
                                    mAndroidBeanList.addAll(parseJSON(result));
                                    mMyAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 获取加载的最后一个可见视图在适配器的位置。
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                if (isFullScreen(mRecyclerView, mLinearLayoutManager)) {
                    // 不满一屏的情况下return
                    return;
                }
                if (isLoadingMore) {
                    return;  // 如果是正在加载更多的状态就不需要再更改提示文字了
                }
                // 更改文字为上拉加载更多...
                mMyAdapter.changeMoreStatus(MyAdapter.PULLUP_LOAD_MORE);
            }
        });
    }

    public boolean isFullScreen(RecyclerView recyclerView, RecyclerView.LayoutManager layoutManager) {
        int childCounts = layoutManager.getChildCount();
        // 获取最后一个childView
        View lastChildView = recyclerView.getChildAt(childCounts - 1);
        // 获取第一个childView
        View firstChildView = recyclerView.getChildAt(0);

        if (firstChildView == null) {
            return false;
        }

        int top = firstChildView.getTop();
        int bottom = lastChildView.getBottom();

        // recycleview显示itemView的有效区域的bottom坐标Y
        int bottomEdge = recyclerView.getHeight() - recyclerView.getPaddingBottom();
        // recycleview显示itemView的有效区域的top坐标Y
        int topEdge = recyclerView.getPaddingTop();

        // 原点在哪
        // 第一个view的顶部小于top边界值，说明第一个view已经部分或者完全移出了界面
        // 最后一个view的底部小于bottom边界值，说明最后一个view已经完全显示在界面
        // 若满足这两个条件，说明所有子view已经填充满了RecycleView，RecycleView可以“真正地”滑动
        if (bottom <= bottomEdge && top >= topEdge) {
            // 这是不满一屏的情况
            return false;
        }
        return true;
    }


    @Override
    public void onRefresh() {
        // 下拉刷新
        page = 1;
        Log.i("text", "fragmentAndroid 我下拉了");

        // 设置下拉状态
        mSwipeRefreshLayout.setRefreshing(true);

        OkhttpManager.getAsync("http://gank.io/api/data/Android/15/1", new IDataCallBack() {
            @Override
            public void requestFailure(Request request, IOException e) {
                // 这里的操作都将会切换到主线程中执行
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "出错了...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void requestSuccess(String result) {
                // 这里的操作都将会切换到主线程中执行
                mSwipeRefreshLayout.setRefreshing(false);

                if (!TextUtils.isEmpty(result)) {

                    if (parseJSON(result) != null) {
                        mAndroidBeanList.clear(); // 下拉刷新才要清空数据
                        mAndroidBeanList.addAll(parseJSON(result));
                        mMyAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "返回数据空错误", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private List<AndroidBean> parseJSON(String jsonString) {
        // 其实可以return一个list 然后addall的 传入泛型
        // 好像不可以 因为对应着属性
        List<AndroidBean> newData = new ArrayList<AndroidBean>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            if (jsonArray.length() == 0) {
                return null;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String id = object.getString("_id");
                String desc = object.getString("desc");
                String url = object.getString("url");
                String publishedAt = object.getString("publishedAt");
                String who = object.getString("who");

                AndroidBean androidBean = new AndroidBean(id, url, desc, publishedAt, who);
                newData.add(androidBean);
            }

            return newData;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements View.OnClickListener {


        //上拉加载更多
        public static final int  PULLUP_LOAD_MORE=0;
        //正在加载中
        public static final int  LOADING_MORE=1;
        //上拉加载更多状态-默认为0
        private int load_more_status=0;


        // 布局状态标志
        private static final int TYPE_ITEM = 0;    // 普通Item View
        private static final int TYPE_FOOTER = 1; // 底部Foot View
        private static final int TYPE_NONE = -1; // 空的Foot View



        // view的创建以及数据绑定这两步进行分开管理
        // 用法就灵活、方便



        /**
         * 根据viewType创建返回不同的view
         *
         * view的创建
         *
         * item显示类型
         *
         * @param parent
         * @param viewType
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // 布局加载器
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());

            if (viewType == TYPE_ITEM) {
                View view = layoutInflater.inflate(R.layout.item_android, parent, false);
                view.setOnClickListener(this);
                MyViewHolder holder = new MyViewHolder(view);

                return holder;
            } else if (viewType == TYPE_FOOTER) {
                View view = layoutInflater.inflate(R.layout.recycler_foot_loadmore, parent,false);
                FootViewHolder holder = new FootViewHolder(view);

                return holder;
            } else if (viewType == TYPE_NONE) {
                View view = layoutInflater.inflate(R.layout.recycler_foot_empty_view, parent, false);
                FootEmptyViewHolder fevh = new FootEmptyViewHolder(view);

                return fevh;
            }

            return null;
        }

        /**
         *根据位置position创建对应的ViewHolder
         *
         * 数据的绑定显示
         *
         * 更新view
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof MyViewHolder) {
                MyViewHolder myViewHolder = (MyViewHolder)holder;

                AndroidBean androidBean = mAndroidBeanList.get(position);
                myViewHolder.tvTitle.setText(androidBean.getDesc());
                myViewHolder.tvAuthor.setText(androidBean.getWho());
                myViewHolder.tvDate.setText(androidBean.getPublishedAt());

                if (newSelected.contains(androidBean.getId())) {
                    myViewHolder.tvTitle.setTextColor(Color.GRAY);
                } else {
                    myViewHolder.tvTitle.setTextColor(Color.BLACK);
                }
            }else if (holder instanceof FootViewHolder) {
                FootViewHolder footViewHolder = (FootViewHolder)holder;
                switch (load_more_status){
                    case PULLUP_LOAD_MORE:
                        footViewHolder.foot_view_item_tv.setText("上拉加载更多...");
                        break;
                    case LOADING_MORE:
                        footViewHolder.foot_view_item_tv.setText("正在加载更多数据...");
                        break;
                }
            } else if (holder instanceof FootEmptyViewHolder) {
                FootEmptyViewHolder fevh = (FootEmptyViewHolder)holder;
            }

        }

        @Override
        public int getItemCount() { // 那你添加一个空的1像素的顶部布局 not 1dp
            return mAndroidBeanList.size() + 1;  // 添加一个底部布局
        }

        @Override
        public int getItemViewType(int position) {
            if (position + 1 == getItemCount()) {
                if (!isFullScreen(mRecyclerView, mRecyclerView.getLayoutManager())) {
                    return TYPE_NONE;  // 增多一个空的布局
                }
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
        }

        @Override
        public void onClick(View v) {
            // 设置点击过文章的title颜色作区分
            TextView tvtitle = (TextView) v.findViewById(R.id.item_android_title);
            tvtitle.setTextColor(Color.GRAY);

            // 根据对应的position找到对应的Bean对象
            int position = mRecyclerView.getChildAdapterPosition(v);
            AndroidBean androidBean = mAndroidBeanList.get(position);
            String url = androidBean.getUrl();

            // 保存id
            newSelected.add(androidBean.getId());

            // 跳转到webview的activity
            Intent intent = new Intent(fragmentAndroid.this.getActivity(), ActivityWebView.class);
            intent.putExtra("url", url);
            fragmentAndroid.this.getActivity().startActivity(intent);
        }


        /**
         * 自定义的ViewHolder
         * 持有每个Item的所有界面元素
         */
        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle;
            private TextView tvAuthor;
            private TextView tvDate;


             MyViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.item_android_title);
                LinearLayout llyt = (LinearLayout) view.findViewById(R.id.item_android_llyt_author);
                tvAuthor = (TextView) llyt.findViewById(R.id.item_android_author);
                tvDate = (TextView) llyt.findViewById(R.id.item_android_date);
            }
        }

        private class FootViewHolder extends RecyclerView.ViewHolder {
            private TextView foot_view_item_tv;
            FootViewHolder(View itemView) {  // FootView视图没有添加点击事件
                super(itemView);
                foot_view_item_tv = (TextView) itemView.findViewById(R.id.foot_view_item_tv);
            }
        }

        private class FootEmptyViewHolder extends RecyclerView.ViewHolder {
            private ImageView foot_empty_imageview;
            private LinearLayout empty_view_layout;
            FootEmptyViewHolder(View itemView) {  // FootEmptyView视图没有添加点击事件
                super(itemView);
                empty_view_layout = (LinearLayout)itemView.findViewById(R.id.empty_view_layout);
                foot_empty_imageview = (ImageView) itemView.findViewById(R.id.empty_imageview);
            }
        }

        /**
         * //上拉加载更多
         * PULLUP_LOAD_MORE=0;
         *  //正在加载中
         * LOADING_MORE=1;
         * //加载完成已经没有更多数据了
         * NO_MORE_DATA=2;
         * @param status
         */
        public void changeMoreStatus(int status){
            load_more_status=status;
            notifyDataSetChanged();
        }

    }
}























