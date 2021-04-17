package com.example.notebook;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WritePad extends BaseActivity {
    private ImageView mIVSign;
    private TextView mTVSign;
    private Bitmap mSignBitmap;

    public WritePad() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_pad);

        WritePadDialog mWritePadDialog = new WritePadDialog(WritePad.this, new WriteDialogListener() {
                    @Override
                    public void onPaintDone(Object object) {
                        mSignBitmap = (Bitmap) object;
                        createSignFile();
//                        mIVSign.setImageBitmap(mSignBitmap);
//                        mTVSign.setVisibility(View.GONE);
                    }
                });
                mWritePadDialog.show();
    }

    //创建签名文件
    private void createSignFile() {
        ByteArrayOutputStream baos = null;
        FileOutputStream fos = null;
        String path;
        File file;
        try {
            path = getExternalFilesDir(null) + File.separator + System.currentTimeMillis() + ".jpg";
            file = new File(path);
            fos = new FileOutputStream(file);
            baos = new ByteArrayOutputStream();
            //如果设置成Bitmap.compress(CompressFormat.JPEG, 100, fos) 图片的背景都是黑色的
            mSignBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            if (b != null) {
                fos.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void needRefresh() {

    }
}
