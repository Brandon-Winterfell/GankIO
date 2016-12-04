package com.huahua.gankio.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huahua.gankio.R;
import com.huahua.gankio.application.MyApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Administrator on 2016/10/12.
 */
public class fragmentView extends Fragment {

    private Activity mActivity;

    List<String> list = new ArrayList<String>();

    private String mIndex;
    private String mUrl;
    private ImageView mImageView;
    private TextView mTextView;

    private Picasso mPicasso;
    private PhotoViewAttacher mAttacher;

    ExecutorService mSingelThreadExecutor;

    private static final String LOG_TAG = "fragmentView";

    public static fragmentView newInstance(String url, String index) {
        fragmentView f = new fragmentView();

        Bundle args = new Bundle();
        args.putString("url", url);  // 传进来图片的url
        args.putString("index", index); // 传进来图片在整个数据集中的索引
        f.setArguments(args);

        return f;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true); // 表示fragment能添加菜单项

        // MyApplication myApplication = (MyApplication)(getActivity().getApplication());
        // 拿到整个app的单例Picasso
        mPicasso = MyApplication.getmPicasso();
        mUrl = getArguments().getString("url");
        mIndex = getArguments().getString("index");

        // 单线程的线程池
        mSingelThreadExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 拿到宿主activity的实例
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_view, container, false);
        mTextView = (TextView) v.findViewById(R.id.index_textview);
        mImageView = (ImageView) v.findViewById(R.id.imageViewForGirl);

        // Picasso加载图片到ImageView
        mPicasso.load(mUrl)
                    .error(R.drawable.empty_photo)
                    .into(mImageView);
        // imageview关联photoview（能进行双击能放大等操作）
        mAttacher = new PhotoViewAttacher(mImageView);

        // 显示图片在数据集中的索引
        mTextView.setText(mIndex);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // 加载菜单
        inflater.inflate(R.menu.menu_image, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) { // 虽然现在只有一个选项

            // SD卡不能写 返回
            if (!isExternalStorageWritable()) {  // 是否挂载了SD卡
                Toast.makeText(mActivity, "保存失败，检测不到SD卡", Toast.LENGTH_LONG).show();
                return true;
            }

            // 假如是6.0以上的系统，需要动态申请权限 // 需要写SD卡权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!requestPermission()) {
                    return true;
                }
            }

            Toast.makeText(mActivity, "正在保存图片...", Toast.LENGTH_SHORT).show();

            /**
             * 判断目录操作
             */
            File dir = getAlbumStorageDir("GankIO");
            // 此时创建了文件夹目录GankIO 需要在里面保存文件
            if (dir == null) { // file等于null，表示目录创建失败
                Toast.makeText(mActivity, "图片保存失败", Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "GankIO目录创建失败");
            }

            savePicInExtStoragePublicDirectory(dir);


            //savePicInSDCard();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存图片到SD卡上
     * 目录 Environment.getExternalStorageDirectory() + GankIOPic
     */
    private void savePicInSDCard() {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // SD卡不能写 返回
                if (!isExternalStorageWritable()) {  // 是否挂载了SD卡
                    Toast.makeText(mActivity, "保存失败，检测不到SD卡", Toast.LENGTH_LONG).show();
                    return;
                }

                // 假如是6.0以上的系统，需要动态申请权限 // 需要写SD卡权限 没有权限  返回
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!requestPermission()) {
                        return;
                    }
                }
                Toast.makeText(mActivity, "正在保存图片...", Toast.LENGTH_SHORT).show();

                /**
                 * 判断存储目录
                 */
                Boolean dirExists = false;  // 目录是否存在
                // 获取SD卡对应的存储目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                Log.i(LOG_TAG, "Environment.getExternalStorageDirectory >>>>>> " + sdCardDir.getAbsolutePath());
                File sdCardGankIODir = new File(sdCardDir, "GankIOPic");
                if (!sdCardGankIODir.exists()) {
                    dirExists = sdCardGankIODir.mkdir();
                } else {
                    dirExists = true;
                }
                // 目录创建不成功 返回
                if (!dirExists) {
                    Toast.makeText(mActivity, "图片保存失败", Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, "GankIO目录创建失败");
                    return;
                }

                /**
                 * 保存图片的操作
                 */
                Log.i(LOG_TAG, "GankIOPic" + sdCardGankIODir.getAbsolutePath());
                // 目录也建出来了  那就执行保存图片到本地了 // 以当前系统的时间戳作为图片的名字
                String imageName = System.currentTimeMillis() + ".png";
                // 传入要创建图片的名字 及路径
                File file1 = new File(sdCardGankIODir.getAbsolutePath(), imageName);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file1);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();  // 关闭流操作
                    Log.i(LOG_TAG, "图片已经保存到" + sdCardGankIODir.getAbsolutePath());
                    Toast.makeText(mActivity,
                            "图片已经保存到" + sdCardGankIODir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    // 这里是主线程 TODO 开子线程保存图片的话 又涉及与主线程的通信
                    // TODO 开子线程的话 建一个单线程的线程池更好 如果用户很多的保存操作 就省了很多的开销
                    // 图片应该本来就在内存中了 断网测试一下 是的 断网也能保存图片
                    // 保存图片后发送广播通知更新数据库
                    Uri uri = Uri.fromFile(file1);
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, "图片保存失败", Toast.LENGTH_LONG).show();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(mActivity, "图片保存失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        /**
         * 指定target任务加载图片
         */
        mPicasso.load(mUrl).into(target);
    }


    /**
     * 要有写SD卡权限
     * Environment.getExternalStoragePublicDirectory
     */
    private void savePicInExtStoragePublicDirectory(final File dir) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                /**
                 * 保存图片的操作 放到子线程中去执行 不然UI会卡顿
                 */
                Runnable savePic = new Runnable() {
                    @Override
                    public void run() {
                        // 目录也建出来了  那就执行保存图片到本地了 // 以当前系统的时间戳作为图片的名字
                        String imageName = System.currentTimeMillis() + ".png";
                        // 传入要创建图片的名字 及路径
                        final File file1 = new File(dir.getAbsolutePath(), imageName);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file1);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();  // 关闭流操作
                            // 与主线程的通信
                            workthreadNeedPlayToastLong(mActivity, "图片已经保存到" + dir.getAbsolutePath());
                            Uri uri = Uri.fromFile(file1);
                            mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        } catch (IOException e) {
                            e.printStackTrace();
                            workthreadNeedPlayToastLong(mActivity, "图片保存失败");
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                };

                mSingelThreadExecutor.execute(savePic);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(mActivity, "图片保存失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        mPicasso.load(mUrl).into(target); // 指定target任务加载图片
    }

    /**
     * 子线程转到主线程打印长时间的吐司
     * @param activity
     * @param msg
     */
    private void workthreadNeedPlayToastLong(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    /* 检测 external storage 是否可读可写 */
    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            // 是否挂载了SD卡，且具有可读可写权限
            // 权限可是你配置的 写进Manifest中的 至于6.0则要动态申请权限
            return true;
        }

        return false;
    }

    /* 检测 external storage 是否只可读 */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /* 在公共的图片目录里创建一个新的相册目录 */
    public File getAlbumStorageDir(String albumName) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!(dir.mkdirs() || dir.isDirectory())) {
            // 检测是否已经创建了目录
            Log.e(LOG_TAG, albumName + "目录没有创建");
            return null;
        }
        return dir;

    }

    private final int SDCard_REQUEST_CODE = 1;

    // @TargetApi(Build.VERSION_CODES.M)
    private boolean requestPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下次请求shouldShowRequestPermissionRation（）返回true
            // 向用户解释为什么需要这个权限
            // 应该需要权限组的 读呀  不用读 我不读这个文件夹


            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(mActivity)
                        .setMessage(" GankIO 需要写SD卡权限，才能保存图片")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 申请写SD卡权限
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        SDCard_REQUEST_CODE);
                            }
                        }).show();
            } else {
                // 申请写SD卡权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SDCard_REQUEST_CODE);
            }

            return false;
        } else {
            Log.i(LOG_TAG, "写SD卡权限已存在");

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SDCard_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "写SD卡权限已成功申请");
            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

                    new AlertDialog.Builder(mActivity)
                            .setTitle("写SD卡权限已被禁止 无法保存图片")
                            .setMessage("GankIO 需要写SD卡权限，才能保存图片\n" +
                                    "请到 设置->应用->GankIO->权限 手动开启权限")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            }
        }
    }
}





















