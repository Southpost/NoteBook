package com.example.notebook;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.LayoutInflaterCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private NoteDatabase dbHelper;

    private Toolbar myToolbar;
    private FloatingActionButton btn;
    TextView textView;
    private ListView lv;
    private Context context = this;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<Note>();
    private TextView setting_text;
    private ImageView setting_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn=findViewById(R.id.fab);
        textView=findViewById(R.id.et);
        lv=findViewById(R.id.lv);
        myToolbar=findViewById(R.id.myToolbar);
        adapter = new NoteAdapter(getApplicationContext(), noteList);
        refreshListView();
        lv.setAdapter(adapter);

        //自定义状态栏
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //弹窗初始化
        initPopUpWindow();
        //设置三条杠
        myToolbar.setNavigationIcon(R.drawable.ic_menu_24);
        //实现三条杠点击
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpView();
            }
        });
        lv.setOnItemClickListener(this);   //点击操作
        lv.setOnItemLongClickListener(this); //长按操作

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,EditActivity.class);
                intent.putExtra("mode",4);  //新建笔记
                startActivityForResult(intent,0);
            }
        });
    }

    //弹出设置栏
    private PopupWindow popupWindow;
    private PopupWindow popupCover;  //设置蒙版
    private ViewGroup customView;
    private ViewGroup coverView;
    private LayoutInflater layoutInflater;  //用于渲染布局
    private RelativeLayout main;
    private WindowManager wm;
    private DisplayMetrics metrics;

    //初始化弹出窗口
    public void initPopUpWindow(){
        layoutInflater=(LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView=(ViewGroup)layoutInflater.inflate(R.layout.setting_layout,null);
        coverView=(ViewGroup)layoutInflater.inflate(R.layout.setting_cover,null);   //美化弹窗
        main=findViewById(R.id.main_layout);
        wm=getWindowManager();
        metrics=new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
    }

    //展示弹窗
    public void showPopUpView(){
        int width=metrics.widthPixels;
        int height=metrics.heightPixels;
        popupCover=new PopupWindow(coverView,width,height,false); //不对焦
        popupWindow=new PopupWindow(customView,(int)(width*0.7),(int)(height*0.5),true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //在主界面加载成功后，显示弹窗
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main,Gravity.NO_GRAVITY,0,0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);

                setting_text = customView.findViewById(R.id.setting_settings_text);
                setting_image = customView.findViewById(R.id.setting_settings_image);


                setting_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));

                    }
                });

                setting_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
                    }
                });

                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });

            }
        });


    }


    //接受返回的结果(包括：删除的)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        int returnMode;
        long note_Id;
        returnMode = data.getExtras().getInt("mode", -1);
        note_Id = data.getExtras().getLong("id", 0);

        if (returnMode == 1) {             //更新当前笔记内容
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            newNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.updateNote(newNote);
            op.close();
        }else if (returnMode == 0) {  // 创建新笔记
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            CRUD op = new CRUD(context);
            op.open();
            op.addNote(newNote);
            op.close();
        }else if(returnMode==2){ //删除已经创建好的笔记内容
            Note delNote=new Note();
            delNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.removeNote(delNote);
            op.close();
        }else {
            //其他一些操作
        }
        refreshListView();
        super.onActivityResult(requestCode, resultCode, data);

    }

    //菜单栏：删除的加入
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);  //渲染menu：删除
        //搜索栏：搜索功能
        MenuItem mSearch=menu.findItem(R.id.action_search);
        SearchView mSearchView= (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("搜索...");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);  //核心
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //删除主界面全部便签
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("确定删除全部便签吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper=new NoteDatabase(context);
                                SQLiteDatabase database=dbHelper.getWritableDatabase();  //数据库操作：可读可写
                                database.delete("notes",null,null);
                                database.execSQL("update sqlite_sequence set seq=0 where name='notes'");//将笔记的条目归0
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
        return super.onOptionsItemSelected(item);
    }

    //实时更新列表内容
    private void refreshListView(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        CRUD op = new CRUD(context);
        op.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        //排序
        if (sharedPreferences.getBoolean("reverseSort", false)) sortNotes(noteList, 2);
        else sortNotes(noteList, 1);

        op.close();
        adapter.notifyDataSetChanged();
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
        }
    }
    //长按删除笔记
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv:
                final Note note = noteList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确定")
                        .setMessage("确定要删除此项内容吗?")
                        .setIcon(R.drawable.ic_baseline_keyboard_voice_24)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CRUD op = new CRUD(context);
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
}