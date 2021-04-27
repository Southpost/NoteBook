package com.example.notebook;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.notebook.Alarm.AlarmReceiver;
import com.example.notebook.Alarm.EditAlarmActivity;
import com.example.notebook.Alarm.Plan;
import com.example.notebook.Alarm.PlanAdapter;
import com.example.notebook.Alarm.PlanDatabase;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;


public class MainActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private NoteDatabase dbHelper;
    private PlanDatabase planDbHelper;
    private Toolbar myToolbar;
    TextView textView;
    private ListView lv;
    private Context context = this;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<Note>();
    private List<Plan> planList = new ArrayList<Plan>();
    //fab
    private FloatingActionButton mAddMemoFab, mAddNoteFab;
    private ExtendedFloatingActionButton mAddFab;
    TextView addMemoActionText, addNoteActionText;
    private Boolean isAllFabsVisible;

    private SharedPreferences sharedPreferences;
    private ToggleButton content_switch;

    private AlarmManager alarmManager;
    private Achievement achievement;
    private ListView lv_plan;
    private LinearLayout lv_layout;
    private LinearLayout lv_plan_layout;
    private PlanAdapter planAdapter;

    public static int curId = 5;

    String[] list_String = {"before one month", "before three months", "before six months", "before one year"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.greyMain));
        setContentView(R.layout.activity_main);
        //实例化闹钟管理器
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        achievement = new Achievement(context);
        initView();

    }

    @Override
    protected void needRefresh() {
        setTheme(R.style.AppTheme);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("opMode", 10);
        startActivity(intent);
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }

    public void initView() {
        initPrefs();
        textView = findViewById(R.id.et);
        lv = findViewById(R.id.lv);
        myToolbar = findViewById(R.id.myToolbar);
        content_switch = findViewById(R.id.content_switch);
        lv_plan = findViewById(R.id.lv_plan);
        lv_layout = findViewById(R.id.lv_layout);
        lv_plan_layout = findViewById(R.id.lv_plan_layout);
        refreshLvVisibility();
        adapter = new NoteAdapter(getApplicationContext(), noteList);
        planAdapter = new PlanAdapter(getApplicationContext(), planList);
        refreshListView();
        lv.setAdapter(adapter);
        lv_plan.setAdapter(planAdapter);

        //自定义状态栏
        setSupportActionBar(myToolbar);

        lv.setOnItemClickListener(this);   //点击操作
        lv_plan.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this); //长按操作
        lv_plan.setOnItemLongClickListener(this);

        //fab
        mAddFab = findViewById(R.id.add_fab);
        mAddMemoFab = findViewById(R.id.add_memo_fab);
        mAddNoteFab = findViewById(R.id.add_note_fab);
        addMemoActionText = findViewById(R.id.add_memo_action_text);
        addNoteActionText = findViewById(R.id.add_note_action_text);

        //设置fab
        mAddMemoFab.setVisibility(View.GONE);
        mAddNoteFab.setVisibility(View.GONE);
        addMemoActionText.setVisibility(View.GONE);
        addNoteActionText.setVisibility(View.GONE);

        isAllFabsVisible = false;

        //新增fab
        mAddFab.shrink();

        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        content_switch.setChecked(temp);//判断是看note还是plan
        content_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("content_switch", isChecked);  //Boolean类型的数据，content_switch为键名，isChecked为键值
                editor.commit();
                refreshLvVisibility();
            }
        });

        //主fab点击
        mAddFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAllFabsVisible) {
                    mAddMemoFab.show();
                    mAddNoteFab.show();
                    addMemoActionText.setVisibility(View.VISIBLE);
                    addNoteActionText.setVisibility(View.VISIBLE);

                    mAddFab.extend();

                    isAllFabsVisible = true;
                } else {
                    mAddMemoFab.hide();
                    mAddNoteFab.hide();
                    addMemoActionText.setVisibility(View.GONE);
                    addNoteActionText.setVisibility(View.GONE);

                    mAddFab.shrink();

                    isAllFabsVisible = false;
                }
            }
        });

        //添加日记
        mAddNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode", 4);  //新建日记
                startActivityForResult(intent, 1);
            }
        });

        //添加备忘录
        mAddMemoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditAlarmActivity.class);
                intent.putExtra("mode", 2); // MODE of 'new plan'
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
            }
        });
    }

    private void refreshLvVisibility() {
        //决定应该显示notes还是plans
        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        if(temp){
            lv_layout.setVisibility(GONE);
            lv_plan_layout.setVisibility(View.VISIBLE);
        }
        else{
            lv_layout.setVisibility(View.VISIBLE);
            lv_plan_layout.setVisibility(GONE);
        }
    }

    //接受返回的结果(包括：删除的)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        int returnMode;
        long note_Id;
        returnMode = data.getExtras().getInt("mode", -1);
        note_Id = data.getExtras().getLong("id", 0);

        if (returnMode == 1) {  //更新当前笔记内容
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            newNote.setId(note_Id);
            BaseCrud op = new BaseCrud(context);
            op.open();
            op.updateNote(newNote);
            achievement.editNote(op.getNote(note_Id).getContent(), content);
            op.close();
        }else if (returnMode == 0) {  // 创建新笔记
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            BaseCrud op = new BaseCrud(context);
            op.open();
            op.addNote(newNote);
            op.close();
            achievement.addNote(content);
        }else if(returnMode==2){ //删除已经创建好的笔记内容
            Note delNote=new Note();
            delNote.setId(note_Id);
            BaseCrud op = new BaseCrud(context);
            op.open();
            op.removeNote(delNote);
            op.close();
            achievement.deleteNote();
        }else if (returnMode == 11){  //编辑备忘录
            String title = data.getExtras().getString("title", null);
            String content = data.getExtras().getString("content", null);
            String time = data.getExtras().getString("time", null);
            Log.d(TAG, time);
            Plan plan = new Plan(title, content, time);
            plan.setId(note_Id);
            com.example.notebook.Alarm.AlarmCrud op = new com.example.notebook.Alarm.AlarmCrud(context);
            op.open();
            op.updatePlan(plan);
            op.close();
        }else if (returnMode == 12){  //删除存在的备忘录
            Plan plan = new Plan();
            plan.setId(note_Id);
            com.example.notebook.Alarm.AlarmCrud op = new com.example.notebook.Alarm.AlarmCrud(context);
            op.open();
            op.removePlan(plan);
            op.close();
        }else if (returnMode == 10){  //创建新的备忘录
            String title = data.getExtras().getString("title", null);
            String content = data.getExtras().getString("content", null);
            String time = data.getExtras().getString("time", null);
            Plan newPlan = new Plan(title, content, time);
            com.example.notebook.Alarm.AlarmCrud op = new com.example.notebook.Alarm.AlarmCrud(context);
            op.open();
            op.addPlan(newPlan);
            Log.d(TAG, "onActivityResult: "+ time);
            op.close();
        }
        refreshListView();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initPrefs() {
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("nightMode")) {
            editor.putBoolean("nightMode", false);
            editor.apply();
        }
        if (!sharedPreferences.contains("reverseSort")) {
            editor.putBoolean("reverseSort", false);
            editor.apply();
        }
        if (!sharedPreferences.contains("fabColor")) {
            editor.putInt("fabColor", -500041);
            editor.apply();
        }
        if (!sharedPreferences.contains("tagListString")) {
            String s = "no tag_life_study_work_play";
            editor.putString("tagListString", s);
            editor.apply();
        }
        if(!sharedPreferences.contains("content_switch")) {
            editor.putBoolean("content_switch", false);
            editor.apply();
        }
        if(!sharedPreferences.contains("fabPlanColor")){
            editor.putInt("fabPlanColor", -500041);
            editor.apply();
        }
        if(!sharedPreferences.contains("noteTitle")){
            editor.putBoolean("noteTitle", true);
            editor.apply();
        }
    }

    //菜单栏：删除的加入
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int mode = (content_switch.isChecked()? 2 : 1);
        final String itemName = (mode == 1 ? "notes" : "plans");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.menu_clear);
                if (view != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Delete all "+ itemName);
                            builder.setIcon(R.drawable.ic_error_outline_black_24dp);
                            builder.setItems(list_String, new DialogInterface.OnClickListener() {//列表对话框；
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {//根据这里which值，即可以指定是点击哪一个Item；
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("Do you want to delete all " + itemName + " " + list_String[which] + "? ")
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int a) {
                                                    Log.d(TAG, "onClick: " + which);
                                                    removeSelectItems(which, mode);
                                                    refreshListView();
                                                }

                                                //根据模式与时长删除对顶的计划s/笔记s
                                                private void removeSelectItems(int which, int mode) {
                                                    int monthNum = 0;
                                                    switch (which){
                                                        case 0:
                                                            monthNum = 1;
                                                            break;
                                                        case 1:
                                                            monthNum = 3;
                                                            break;
                                                        case 2:
                                                            monthNum = 6;
                                                            break;
                                                        case 3:
                                                            monthNum = 12;
                                                            break;
                                                    }
                                                    Calendar rightNow = Calendar.getInstance();
                                                    rightNow.add(Calendar.MONTH,-monthNum);//日期加3个月
                                                    Date selectDate = rightNow.getTime();
                                                    java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    String selectDateStr = simpleDateFormat.format(selectDate);
                                                    Log.d(TAG, "removeSelectItems: " + selectDateStr);
                                                    switch(mode){
                                                        case 1: //notes
                                                            dbHelper = new NoteDatabase(context);
                                                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                            Cursor cursor = db.rawQuery("select * from notes" ,null);
                                                            while(cursor.moveToNext()){
                                                                if (cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                    db.delete("notes", NoteDatabase.ID + "=?", new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)))});
                                                                }
                                                            }
                                                            db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                                            refreshListView();
                                                            break;
                                                        case 2: //plans
                                                            planDbHelper = new PlanDatabase(context);
                                                            SQLiteDatabase pdb = planDbHelper.getWritableDatabase();
                                                            Cursor cursor1 = pdb.rawQuery("select * from plans" ,null);
                                                            while(cursor1.moveToNext()){
                                                                if (cursor1.getString(cursor1.getColumnIndex(PlanDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                    pdb.delete("plans", PlanDatabase.ID + "=?", new String[]{Long.toString(cursor1.getLong(cursor1.getColumnIndex(PlanDatabase.ID)))});
                                                                }
                                                            }
                                                            pdb.execSQL("update sqlite_sequence set seq=0 where name='plans'");
                                                            refreshListView();
                                                            break;
                                                    }
                                                }
                                            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });
                }
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    //删除主界面全部便签
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                if(!content_switch.isChecked()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Delete All Notes ?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper = new NoteDatabase(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete("notes", null, null);//delete data in table NOTES
                                    db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Delete All Plans ?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    planDbHelper = new PlanDatabase(context);
                                    SQLiteDatabase db = planDbHelper.getWritableDatabase();
                                    db.delete("plans", null, null);
                                    db.execSQL("update sqlite_sequence set seq=0 where name='plans'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //实时更新列表内容
    private void refreshListView(){
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        BaseCrud op = new BaseCrud(context);
        op.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        //排序
        if (sharedPreferences.getBoolean("reverseSort", false)) sortNotes(noteList, 2);
        else sortNotes(noteList, 1);
        op.close();
        adapter.notifyDataSetChanged();

        //备忘录
        com.example.notebook.Alarm.AlarmCrud op1 = new com.example.notebook.Alarm.AlarmCrud(context);
        op1.open();
        if(planList.size() > 0) {
            cancelAlarms(planList);//删除所有闹钟
            planList.clear();
        }
        planList.addAll(op1.getAllPlans());
        startAlarms(planList);//添加所有新闹钟
        if (sharedPreferences.getBoolean("reverseSort", false)) sortPlans(planList, 2);
        else sortPlans(planList, 1);
        op1.close();
        planAdapter.notifyDataSetChanged();
        achievement.listen();
    }

    //设置很多提醒
    private void startAlarms(List<Plan> plans){
        for(int i = 0; i < plans.size(); i++) {
            startAlarm(plans.get(i));
        }
    }

    //设置提醒
    private void startAlarm(Plan p) {
        Calendar c = p.getPlanTime();
        if(!c.before(Calendar.getInstance())) {
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.putExtra("title", p.getTitle());
            intent.putExtra("content", p.getContent());
            intent.putExtra("id", (int)p.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) p.getId(), intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }

    //取消提醒
    private void cancelAlarm(Plan p) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)p.getId(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    //取消很多提醒
    private void cancelAlarms(List<Plan> plans){
        for(int i = 0; i < plans.size(); i++)
            cancelAlarm(plans.get(i));
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent!=null && intent.getIntExtra("mode", 0) == 1){
            content_switch.setChecked(true);
            refreshLvVisibility();
        }
    }

    //成就系统
    public class Achievement {
        private SharedPreferences sharedPreferences;
        private int noteNumber;
        private int wordNumber;
        private int noteLevel;
        private int wordLevel;

        public Achievement(Context context) {
            sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
            initPref();
            getPref();
        }

        private void getPref() {
            noteNumber = sharedPreferences.getInt("noteNumber", 0);
            wordNumber = sharedPreferences.getInt("wordNumber", 0);
            noteLevel = sharedPreferences.getInt("noteLevel", 0);
            wordLevel = sharedPreferences.getInt("wordLevel", 0);
        }

        private void initPref() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (!sharedPreferences.contains("noteLevel")) {
                editor.putInt("noteLevel", 0);
                editor.commit();
                if (!sharedPreferences.contains("wordLevel")) {
                    editor.putInt("wordLevel", 0);
                    editor.commit();
                    addCurrent(noteList);
                    if (sharedPreferences.contains("maxRemainNumber")) {
                        editor.remove("maxRemainNumber");
                        editor.commit();
                    }
                    if (sharedPreferences.contains("remainNumber")){
                        editor.remove("remainNumber");
                        editor.commit();
                    }
                    if (!sharedPreferences.contains("noteNumber")) {
                        editor.putInt("noteNumber", 0);
                        editor.commit();
                        addCurrent(noteList);
                        if (!sharedPreferences.contains("wordNumber")) {
                            editor.putInt("wordNumber", 0);
                            editor.commit();
                        }
                    }
                }
            }
        }

        //加入已写好的笔记
        private void addCurrent(List<Note> list) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int tempNN = list.size();
            editor.putInt("noteNumber", tempNN);
            if (tempNN >= 1000) editor.putInt("noteLevel", 4);
            else if (tempNN >= 100) editor.putInt("noteLevel", 3);
            else if (tempNN >= 10) editor.putInt("noteLevel", 2);
            else if (tempNN >= 1) editor.putInt("noteLevel", 1);
            int wordCount = 0;
            for (int i = 0; i < list.size(); i++) {
                wordCount += list.get(i).getContent().length();
            }
            editor.putInt("wordNumber", wordCount);
            if (wordCount >= 20000) editor.putInt("noteLevel", 5);
            else if (wordCount >= 5000) editor.putInt("noteLevel", 4);
            else if (wordCount >= 1000) editor.putInt("noteLevel", 3);
            else if (wordCount >= 500) editor.putInt("noteLevel", 2);
            else if (wordCount >= 100) editor.putInt("noteLevel", 1);
            editor.apply();
        }

        //添加笔记
        public void addNote(String content) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            noteNumber++;
            editor.putInt("noteNumber", noteNumber);
            wordNumber += content.length();
            editor.putInt("wordNumber", wordNumber);
            editor.apply();
        }

        //删除笔记
        public void deleteNote() {

        }

        //编辑笔记，修改字数
        public void editNote(String oldContent, String newContent) {
            if (newContent.length() > oldContent.length()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                wordNumber += (newContent.length() - oldContent.length());
                editor.putInt("wordNumber", wordNumber);
                editor.apply();
            }
        }

        //笔记数成就
        public void noteNumberAchievement(int num) {
            switch (num) {
                case 1:
                    if (noteLevel == 0) announcement("🎉🎉🎉 第一步!", 1, num);
                    break;
                case 10:
                    if (noteLevel == 1) announcement("🎉🎉🎉 Keep going, and don't give up", 1, num);
                    break;
                case 100:
                    if (noteLevel == 2) announcement("🎉🎉🎉 This has been a long way...", 1, num);
                    break;
                case 1000:
                    if (noteLevel == 3) announcement("🎉🎉🎉 Final achievement! Well Done!", 1, num);
                    break;
            }
        }

        //字数成就
        public void wordNumberAchievement(int num) {
            if (num > 20000 && wordLevel == 4) announcement("Final Achievement! 恭喜你!", 2, 20000);
            else if (num > 5000 && wordLevel == 3)
                announcement("A long story...", 2, 5000);
            else if (num > 1000 && wordLevel == 2)
                announcement("Double essays!", 2, 1000);
            else if (num > 500 && wordLevel == 1)
                announcement("You have written an essay!", 2, 500);
            else if (num > 100 && wordLevel == 0)
                announcement("Take it slow to create more possibilities!", 2, 100);
        }

        //对话框
        public void announcement(String message, int mode, int num) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(annoucementTitle(mode, num))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            setState(mode);
        }

        //对话框标题
        public String annoucementTitle(int mode, int num) {
            switch (mode) {
                case 1:
                    return "You have written " + num + " notes! ";
                case 2:
                    return "You have written " + num + " words! ";
                case 3:
                    return "You have " + num + " notes remaining visible!";
            }
            return null;
        }

        public void setState(int mode) {
            //如果重复宣布，则将相应状态设置为true
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (mode) {
                case 1:
                    noteLevel ++;
                    editor.putInt("noteLevel", noteLevel);
                    editor.commit();
                    break;
                case 2:
                    wordLevel ++;
                    editor.putInt("wordLevel", wordLevel);
                    editor.commit();
                    break;
            }
        }

        //监听
        public void listen() {
            noteNumberAchievement(noteNumber);
            wordNumberAchievement(wordNumber);
        }
    }

    //主界面跳转编辑界面
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv:
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);
                break;
            case R.id.lv_plan:
                Plan curPlan = (Plan) parent.getItemAtPosition(position);
                Intent intent1 = new Intent(MainActivity.this, EditAlarmActivity.class);
                intent1.putExtra("title", curPlan.getTitle());
                intent1.putExtra("content", curPlan.getContent());
                intent1.putExtra("time", curPlan.getTime());
                intent1.putExtra("mode", 1);
                intent1.putExtra("id", curPlan.getId());
                startActivityForResult(intent1, 1);
                break;
        }
    }

    //长按删除日记
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv:
                final Note note = noteList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确定")
                        .setMessage("确定要删除此条日记吗?")
                        .setIcon(R.drawable.ic_baseline_keyboard_voice_24)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BaseCrud op = new BaseCrud(context);
                                op.open();
                                op.removeNote(note);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.lv_plan:
                final Plan plan = planList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("确定要删除此条备忘录吗?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                com.example.notebook.Alarm.AlarmCrud op = new com.example.notebook.Alarm.AlarmCrud(context);
                                op.open();
                                op.removePlan(plan);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return true;
    }

    //格式转换string -> milliseconds
    @RequiresApi(api = Build.VERSION_CODES.N)
    public long dateStrToSec(String date) throws ParseException, java.text.ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long secTime = format.parse(date).getTime();
        return secTime;
    }

    //转换当前的long值：1, 0, -1
    public int ChangeLong(Long l) {
        if (l > 0) return 1;
        else if (l < 0) return -1;
        else return 0;
    }

    //按时间排序笔记
    public void sortNotes(List<Note> noteList, final int mode) {
        Collections.sort(noteList, new Comparator<Note>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public int compare(Note o1, Note o2) {
                try {
                    if (mode == 1) {
                        return ChangeLong(dateStrToSec(o2.getTime()) - dateStrToSec(o1.getTime()));
                    }
                    else if (mode == 2) {//reverseSort
                        return ChangeLong(dateStrToSec(o1.getTime()) - dateStrToSec(o2.getTime()));
                    }
                } catch (ParseException | java.text.ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }

    //按备忘录时间排序
    public void sortPlans(List<Plan> planList, final int mode){
        Collections.sort(planList, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                try {
                    if (mode == 1)
                        return ChangeLong(calStrToSec(o1.getTime()) - calStrToSec(o2.getTime()));
                    else if (mode == 2) //reverseSort
                        return ChangeLong(calStrToSec(o2.getTime()) - calStrToSec(o1.getTime()));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }
    public long calStrToSec(String date) throws java.text.ParseException {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        long secTime = Objects.requireNonNull(format.parse(date)).getTime();
        return secTime;
    }
}