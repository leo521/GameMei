package gamemei.qiyun.com.gamemei.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by leo on 16/1/18.
 */
public class LineView extends View {
    private Paint paint;

    public LineView(Context context) {
        super(context);
        init();
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.GREEN);                    //设置画笔颜色
        canvas.drawColor(Color.WHITE);                  //设置背景颜色
        paint.setStrokeWidth((float) 10.0);             //设置线宽
        canvas.drawLine(20, 50, 100, 50, paint);        //绘制直线
    }
}
