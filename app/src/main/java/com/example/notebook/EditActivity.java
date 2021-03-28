package com.example.notebook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends BaseActivity {

    private EditText editText;

    private String old_content = "";
    private String old_time = "";
    private int old_Tag = 1;
    private long id = 0;
    private int openMode = 0;
    private int tag = 1;
    public Intent intent = new Intent(); //信息的发送
    private boolean tagChange = false;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editText = findViewById(R.id.et);
        myToolbar=findViewById(R.id.myToolbar);

        //编辑界面的头部
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //实现返回主界面
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSetMessage();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //返回编辑内容的相关操作
        Intent getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);

        if (openMode == 3) {//打开已存在的note
            id = getIntent.getLongExtra("id", 0);
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            old_Tag = getIntent.getIntExtra("tag", 1);
            editText.setText(old_content);
            editText.setSelection(old_content.length());
        }
    }

    //返回编辑页并保存内容
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            autoSetMessage();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //添加删除按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);  //渲染edit_menu
        return super.onCreateOptionsMenu(menu);
    }


    //完成删除功能、换肤功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String[] colors={"护眼色", "紫罗兰", "道奇蓝","碧绿色","热情粉"};
        switch (item.getItemId()){
            case R.id.delete:
                new AlertDialog.Builder(EditActivity.this)
                        .setMessage("确定删除当前便签吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode==4){
                                    intent.putExtra("mode",-1);  //新笔记，返回-1，什么也不做
                                }else {
                                    intent.putExtra("mode",2);  //已经存在的笔记，用于返回操作
                                    intent.putExtra("id",id);
                                }
                                setResult(RESULT_OK,intent);
                                finish();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                        }
                        }).create().show();
                break;
            case R.id.change:
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle("选择一个背景色")
                        .setIcon(R.drawable.tomato)
                        .setItems(colors, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        getWindow().setBackgroundDrawableResource(R.color.blackColor);
                                        break;
                                    case 1:
                                        getWindow().setBackgroundDrawableResource(R.color.Violet);
                                        break;
                                    case 2:
                                        getWindow().setBackgroundDrawableResource(R.color.DoderBlue);
                                        break;
                                    case 3:
                                        getWindow().setBackgroundDrawableResource(R.color.Auqamarin);
                                        break;
                                    case 4:
                                        getWindow().setBackgroundDrawableResource(R.color.HotPink);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create().show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void autoSetMessage() {
        if (openMode == 4) {
            if (editText.getText().toString().length() == 0) {
                intent.putExtra("mode", -1); //没有信息
            } else {
                intent.putExtra("mode", 0); // 有一个新的
                intent.putExtra("content", editText.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("tag", tag);
            }
        } else {
            if (editText.getText().toString().equals(old_content) && !tagChange)
                intent.putExtra("mode", -1); // 没有修改
            else {
                intent.putExtra("mode", 1); //有修改
                intent.putExtra("content", editText.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("id", id);
                intent.putExtra("tag", tag);
            }
        }
    }
    //时间戳
    public String dateToStr () {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}