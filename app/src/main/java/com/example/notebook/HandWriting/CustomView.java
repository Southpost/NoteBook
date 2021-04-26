package com.example.notebook.HandWriting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * 使用内部类 自定义一个简单的View
 * @author Wang
 *
 */
class CustomView extends View {

    Paint paint;

    public CustomView(Context context) {
        super(context);
        paint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        paint.setColor(Color.YELLOW);//颜色
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);//画笔大小
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //直接将View显示区域用某个颜色填充满
        //canvas.drawColor(Color.BLUE);

        //绘圆
        canvas.drawCircle(100, 100, 90, paint);
        //绘线
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        canvas.drawLine(300,300,400,500,paint);

        RectF rect = new RectF(100, 100, 300, 300);

        //绘制弧线区域
        paint.setColor(Color.RED);
        canvas.drawArc(rect, //弧线所使用的矩形区域大小
                0,  //开始角度
                120, //扫过的角度
                true, //是否使用中心
                paint);

        //矩形区域内切椭圆
        rect = new RectF(500,500,600,700);
        canvas.drawOval(rect, paint);

        //绘矩形
        paint.setColor(Color.BLUE);
        rect = new RectF(800,800,1000,1000);
        canvas.drawRect(rect,paint);

        //绘圆角矩形
        paint.setColor(Color.YELLOW);
        canvas.drawRoundRect(rect,
                50, //x轴的半径
                50, //y轴的半径
                paint);

        Path path = new Path(); //定义一条路径
        path.moveTo(100, 500); //移动到 坐标10,10
        path.lineTo(300, 600);
        path.lineTo(200,500);
        path.lineTo(100, 500);
        canvas.drawPath(path, paint);

    }
}
