package com.example.notebook.Alarm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.notebook.BaseActivity;
import com.example.notebook.R;
import java.util.Calendar;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

public class EditAlarmActivity extends BaseActivity implements View.OnClickListener {

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private EditText et_title;
    private EditText et;
    private Button set_date;
    private Button set_time;

    private TextView date;
    private TextView time;
    private Plan plan;
    private int[] dateArray = new int[3];
    private int[] timeArray = new int[2];

    private int openMode = 0;
    private String old_title = "";
    private String old_content = "";
    private String old_time = "";
    private long id = 0;
    private boolean timeChange = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memo_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_alarm_layout);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        final Intent intent = getIntent();
        openMode = intent.getExtras().getInt("mode", 0);
        if(openMode == 1){
            id = intent.getLongExtra("id", 0);
            old_title = intent.getStringExtra("title");
            old_content = intent.getStringExtra("content");
            old_time = intent.getStringExtra("time");
            et_title.setText(old_title);
            et_title.setSelection(old_title.length());
            et.setText(old_content);
            et.setSelection(old_content.length());

            String[] wholeTime = old_time.split(" ");
            String[] temp = wholeTime[0].split("-");
            String[] temp1 = wholeTime[1].split(":");
            setDateTV(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]));
            setTimeTV(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]));
        }
         myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!canBeSet()) {
                    Toast.makeText(com.example.notebook.Alarm.EditAlarmActivity.this, "无效的时间", Toast.LENGTH_SHORT).show();
                }else if(et.getText().toString().length() + et_title.getText().toString().length() == 0 && openMode == 2){
                    Intent intent1 = new Intent();
                    intent1.putExtra("mode", -1);//nothing new happens.
                    setResult(RESULT_OK, intent1);
                    finish();//返回
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
                else if (et_title.getText().toString().length() == 0) {
                    Toast.makeText(com.example.notebook.Alarm.EditAlarmActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                }
                else {
                    isTimeChange();
                    Intent intent = new Intent();
                    if (openMode == 2) {
                        intent.putExtra("mode", 10); // 新的备忘录;
                        intent.putExtra("title", et_title.getText().toString());
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                        Log.d(TAG, date.getText().toString() + time.getText().toString());
                    } else {
                        if (et.getText().toString().equals(old_content) && et_title.getText().toString().equals(old_title) && !timeChange) {
                            intent.putExtra("mode", -1); // 没有编辑
                        }
                        else {
                            intent.putExtra("mode", 11); //编辑内容
                            intent.putExtra("title", et_title.getText().toString());
                            intent.putExtra("content", et.getText().toString());
                            intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                            intent.putExtra("id", id);
                        }
                    }
                    setResult(RESULT_OK, intent);
                    finish();//返回
                    overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                }
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        } else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if(!canBeSet()) {
                Toast.makeText(com.example.notebook.Alarm.EditAlarmActivity.this, "Invalid Time", Toast.LENGTH_SHORT).show();
            }else if(et.getText().toString().length() + et_title.getText().toString().length() == 0 && openMode == 2){
                Intent intent1 = new Intent();
                intent1.putExtra("mode", -1);//nothing new happens.
                setResult(RESULT_OK, intent1);
                finish();//返回
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            }
            else if (et_title.getText().toString().length() == 0) {
                Toast.makeText(com.example.notebook.Alarm.EditAlarmActivity.this, "标题不能为空！", Toast.LENGTH_SHORT).show();
            }
            else {
                isTimeChange();
                Intent intent = new Intent();
                if (openMode == 2) {
                    intent.putExtra("mode", 10); // 一个新的备忘录
                    intent.putExtra("title", et_title.getText().toString());
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                    Log.d(TAG, date.getText().toString() + time.getText().toString());
                } else {
                    if (et.getText().toString().equals(old_content) && et_title.getText().toString().equals(old_title) && !timeChange) {
                        intent.putExtra("mode", -1); //没有编辑
                    }
                    else {
                        intent.putExtra("mode", 11); //编辑内容
                        intent.putExtra("title", et_title.getText().toString());
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", date.getText().toString() + " " + time.getText().toString());
                        intent.putExtra("id", id);
                    }
                }
                setResult(RESULT_OK, intent);
                finish();//返回
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.delete:
                new AlertDialog.Builder(com.example.notebook.Alarm.EditAlarmActivity.this)
                        .setMessage("确定删除这条备忘录?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode == 2){
                                    intent.putExtra("mode", -1); // delete the plan
                                    setResult(RESULT_OK, intent);
                                }
                                else {
                                    intent.putExtra("mode", 12); // delete the plan
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void needRefresh() {
        setTheme(R.style.AppTheme);
        startActivity(new Intent(this, com.example.notebook.Alarm.EditAlarmActivity.class));
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }

    private void init(){
        plan = new Plan();
        dateArray[0] = plan.getYear();
        dateArray[1] = plan.getMonth() + 1;
        dateArray[2] = plan.getDay();
        timeArray[0] = plan.getHour();
        timeArray[1] = plan.getMinute();
        et_title = findViewById(R.id.et_title);
        et = findViewById(R.id.et);
        set_date = findViewById(R.id.set_date);
        set_time = findViewById(R.id.set_time);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

        //初始化两个textView
        setDateTV(dateArray[0], dateArray[1], dateArray[2]);
        setTimeTV((timeArray[1]>54? timeArray[0]+1 : timeArray[0]), (timeArray[1]+5)%60);
        Log.d(TAG, "init: "+dateArray[1]);

        set_date.setOnClickListener(this);
        set_time.setOnClickListener(this);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setDateTV(year, month+1, dayOfMonth);
                
            }
        };
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTimeTV(hourOfDay, minute);
            }
        };
    }

    private void setDateTV(int y, int m, int d){
        //更新tv和dateArray
        String temp = y + "-";
        if(m<10) temp += "0";
        temp += (m + "-");
        if(d<10) temp +="0";
        temp += d;
        date.setText(temp);
        dateArray[0] = y;
        dateArray[1] = m;
        dateArray[2] = d;
    }

    private void setTimeTV(int h, int m){
        String temp = "";
        if(h<10) temp += "0";
        temp += (h + ":");
        if(m<10) temp += "0";
        temp += m;
        time.setText(temp);
        timeArray[0] = h;
        timeArray[1] = m;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_date: //选择日期
                DatePickerDialog dialog = new DatePickerDialog(com.example.notebook.Alarm.EditAlarmActivity.this,
                        R.style.DayDialogTheme, dateSetListener,
                        dateArray[0], dateArray[1] - 1, dateArray[2]);
                dialog.show();
                break;
            case R.id.set_time://选择时间
                TimePickerDialog dialog1 = new TimePickerDialog(com.example.notebook.Alarm.EditAlarmActivity.this,
                        R.style.DayDialogTheme, timeSetListener,
                        timeArray[0], timeArray[1], true);
                dialog1.show();
                break;
        }
    }
    
    private void isTimeChange(){
        String newTime = date.getText().toString() + " " + time.getText().toString();
        if(!newTime.equals(old_time)) timeChange = true;
    }

    private boolean canBeSet(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateArray[0], dateArray[1] - 1, dateArray[2], timeArray[0], timeArray[1]);
        Calendar cur = Calendar.getInstance();
        Log.d(TAG, "canBeSet: " + cur.getTime().toString() + calendar.getTime().toString());
        if(cur.before(calendar)) return true;
        else {
            Toast.makeText(this, "请设置正确的时间", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
