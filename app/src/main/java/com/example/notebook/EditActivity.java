package com.example.notebook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notebook.HandWriting.HandwritingActivity;

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
    private boolean isRead;
    Toast toast1, toast2;
    private Intent intentMusic;
    private int[] curColor = {
            R.color.blackColor,
            R.color.Violet,
            R.color.DoderBlue,
            R.color.Auqamarin,
            R.color.HotPink,
            R.color.white};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //跳转到手写页面
        Button handBtn = findViewById(R.id.edit_btn1);
        handBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EditActivity.this, HandwritingActivity.class);
                startActivity(intent);
            }
        });

        Button emojiBtn = findViewById(R.id.edit_btn2);
        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emojiArr[]=new String[78];
                for(int i=0;i<78;i++)
                {
                    emojiArr[i]= new String(Character.toChars(128513+i));
                }
                AlertDialog.Builder builder =new AlertDialog.Builder(EditActivity.this);
                //builder.setIcon(R.drawable.ic_launcher_foreground);
                builder.setTitle("选择你喜欢的表情:");
                builder.setItems(emojiArr, new DialogInterface.OnClickListener() {    //设置监听
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string = editText.getText().toString();
                        string=string + emojiArr[which];
                        editText.setText(string);
                    }
                });
                builder.create().show();                                  //创建、显示对话框
            }

        });

        toast1=Toast.makeText(getApplicationContext(), "您已进入阅读模式", Toast.LENGTH_SHORT);
        toast2=Toast.makeText(getApplicationContext(), "您已进入编辑模式", Toast.LENGTH_SHORT);
        isRead = false;

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

        //获取保存的背景色
        getWindow().setBackgroundDrawableResource(curColor[MainActivity.curId]);
    }

    @Override
    protected void needRefresh() {
        setTheme(R.style.AppTheme);
        startActivity(new Intent(this, EditActivity.class));
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
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
        stopService(intentMusic);
        return super.onKeyDown(keyCode, event);
    }

    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);  //渲染edit_menu
        return super.onCreateOptionsMenu(menu);
    }

    //完成删除功能、换肤功能
    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String[] colors={"护眼色", "紫罗兰", "道奇蓝","碧绿色","热情粉","纯白色"};
        final String[] musics={"奔跑吧","梦中的婚礼","安静","少女的心","剑仙","完美的邂逅"};

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
            case R.id.read:
                Drawable drawable = item.getIcon(); //图标颜色变换
                if(isRead){
                    drawable.setColorFilter(null);  //消除上一级的改变
                    drawable.setColorFilter(getResources().getColor(R.color.greyC), PorterDuff.Mode.SRC_IN);
                    toast1.setText("您已进入编辑模式");
                    toast2.show();
                    editText.setFocusableInTouchMode(true);
                    editText.setFocusable(true);
                    isRead = false;
                }else{
                    drawable.setColorFilter(null);
                    drawable.setColorFilter(getResources().getColor(R.color.p), PorterDuff.Mode.SRC_IN);
                    toast1.setText("您已进入阅读模式");
                    toast1.show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    editText.setFocusableInTouchMode(false);
                    editText.setFocusable(false);
                    isRead = true;
                }
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
                                        MainActivity.curId = 0;
                                        break;
                                    case 1:
                                        MainActivity.curId = 1;
                                        break;
                                    case 2:
                                        MainActivity.curId = 2;
                                        break;
                                    case 3:
                                        MainActivity.curId = 3;
                                        break;
                                    case 4:
                                        MainActivity.curId = 4;
                                        break;
                                    case 5:
                                        MainActivity.curId = 5;
                                        break;
                                    default:
                                        break;
                                }
                                getWindow().setBackgroundDrawableResource(curColor[MainActivity.curId]);
                            }
                        }).create().show();
                break;
            case R.id.music:
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle("选择一个背景音乐")
                        .setIcon(R.drawable.music_collection)
                        .setItems(musics, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_1 = MyIntentService.ACTION_MUSIC_1;
                                        // 设置action
                                        intentMusic.setAction(action_1);
                                        startService(intentMusic);
                                        break;
                                    case 1:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_2 = MyIntentService.ACTION_MUSIC_2;
                                        // 设置action
                                        intentMusic.setAction(action_2);
                                        startService(intentMusic);
                                        break;
                                    case 2:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_3 = MyIntentService.ACTION_MUSIC_3;
                                        // 设置action
                                        intentMusic.setAction(action_3);
                                        startService(intentMusic);
                                        break;
                                    case 3:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_4 = MyIntentService.ACTION_MUSIC_4;
                                        // 设置action
                                        intentMusic.setAction(action_4);
                                        startService(intentMusic);
                                        break;
                                    case 4:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_5 = MyIntentService.ACTION_MUSIC_5;
                                        // 设置action
                                        intentMusic.setAction(action_5);
                                        startService(intentMusic);
                                        break;
                                    case 5:
                                        // 启动服务播放背景音乐
                                        intentMusic = new Intent(EditActivity.this, MyIntentService.class);
                                        String action_6 = MyIntentService.ACTION_MUSIC_6;
                                        // 设置action
                                        intentMusic.setAction(action_6);
                                        startService(intentMusic);
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