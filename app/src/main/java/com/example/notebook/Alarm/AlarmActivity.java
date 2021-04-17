package com.example.notebook.Alarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;

import com.example.notebook.BaseActivity;
import com.example.notebook.R;
import java.util.Date;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

/**
 * 闹钟警报类（对话框显示）
 */
public class AlarmActivity extends BaseActivity{
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AlertDialog.Builder(AlarmActivity.this)
                .setIcon(R.drawable.alarm_24)
                .setTitle("闹钟提醒")
                .setMessage(
                        "有待办事项，请您及时查看"
                        +"\n"+"当前时间为:  "
                                + new SimpleDateFormat("yyyy-MM-dd HH:mm")
                                .format(new Date()))
                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmActivity.this.finish();
                    }
                }).show();
    }

    @Override
    protected void needRefresh() {

    }

    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memo_menu,menu);  //渲染memo_menu
        return super.onCreateOptionsMenu(menu);
    }
}
