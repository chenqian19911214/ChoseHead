package com.hansion.chosehead;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mHeader_iv;

    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //剪裁请求码
    private static final int CROP_REQUEST_CODE = 3;

    //调用照相机返回图片文件
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mHeader_iv = (ImageView) findViewById(R.id.mHeader_iv);
        Button mGoCamera_btn = (Button) findViewById(R.id.mGoCamera_btn);
        Button mGoAlbm_btn = (Button) findViewById(R.id.mGoAlbm_btn);
        mGoCamera_btn.setOnClickListener(this);
        mGoAlbm_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mGoCamera_btn:
                //getPicFromCamera();
                isSd();
                break;
            case R.id.mGoAlbm_btn:
                getPicFromAlbm();
                break;
            default:
                break;
        }
    }
/**
 *  mounted
 *  /storage/emulated/0
 *  /data/data/com.hansion.chosehead/cache
 * */


private List sdpath(){

    List pathsList = new ArrayList<String>();
    StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
    try {
        Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
        method.setAccessible(true);
        Object result = method.invoke(storageManager);
        if (result != null && result instanceof String[]) {
            String[] pathes = (String[]) result;
            StatFs statFs;
            for (String path : pathes) {
                if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                    statFs = new StatFs(path);
                    if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
                        pathsList.add(path);
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        File externalFolder = Environment.getExternalStorageDirectory();
        if (externalFolder != null) {
            pathsList.add(externalFolder.getAbsolutePath());
        }
    }

    pathsList.toArray(new String[pathsList.size()]);
       Log.i("",""+pathsList.toArray(new String[pathsList.size()]));


   return pathsList;

}
    private void isSd(){
        String issdke = Environment.getExternalStorageState();

        String sdpath = Environment.getExternalStorageDirectory().getPath();
        //Environment.getExternal
        String datapath = getBaseContext().getCacheDir().getPath();

        List list= sdpath();

        for (Object dss: list) {
            Log.i("","dss:"+dss);
        }
        Log.i("","issdke:"+issdke+"  sdpath:"+sdpath+"  datapath:"+datapath+"  sd:");

}
    /**
     * 从相机获取图片
     */
    private void getPicFromCamera() {
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.hansion.chosehead", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            Log.e("dasd", contentUri.toString());
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * 从相册获取图片
     */
    private void getPicFromAlbm() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
    }


    /**
     * 裁剪图片
     *
     * file:///storage/emulated/0/1513828126437.jpg
     */
    private void cropPhoto(Uri uri) {
        Log.i("","uri");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:   //调用相机后返回
                if (resultCode == RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.hansion.chosehead", tempFile);
                        cropPhoto(contentUri);
                    } else {
                        Log.i(" ","+tempFile:"+tempFile);
                        cropPhoto(Uri.fromFile(tempFile));
                    }
                }
                break;
            case ALBUM_REQUEST_CODE:    //调用相册后返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    cropPhoto(uri);
                }
                break;
            case CROP_REQUEST_CODE:     //调用剪裁后返回
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //在这里获得了剪裁后的Bitmap对象，可以用于上传
                    Bitmap image = bundle.getParcelable("data");
                    //设置到ImageView上
                    mHeader_iv.setImageBitmap(image);
                    //也可以进行一些保存、压缩等操作后上传
                   String path = saveImage("crop", image);
                }
                break;
        }
    }

    public String saveImage(String name, Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath());
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
