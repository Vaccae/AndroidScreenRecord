package dem.vaccae.mediaprojection.floatwindow;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jeremyliao.liveeventbus.LiveEventBus;

import dem.vaccae.mediaprojection.MediaPronUtil;
import dem.vaccae.mediaprojection.R;


/**
 * 作者: Vaccae
 * 邮箱: 3657447@qq.com
 * 作用:
 */
public class FloatWindowBigView extends LinearLayout {

    public static int viewWidth;
    public static int viewHeight;
    private static View view;

    public static void setViewWidth(int viewWidth) {
        view.getLayoutParams().width = viewWidth;
    }

    public static void setViewHeight(int viewHeight) {
        view.getLayoutParams().height = viewHeight;
    }

    public FloatWindowBigView(final Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.window_big, this);
        view = findViewById(R.id.big_window_layout);

        LayoutParams params =new LayoutParams(90, 280);
        view.setLayoutParams(params);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        ImageButton imgbig = (ImageButton) findViewById(R.id.imgbig);
        ImageButton imgcamera = (ImageButton) findViewById(R.id.imgcamera);
        ImageButton imgvoice = (ImageButton) findViewById(R.id.imgvoice);

        imgcamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveEventBus.get("MediaPron")
                        .postOrderly(1);
                Toast.makeText(context, "点击了开始录制", Toast.LENGTH_SHORT).show();
            }
        });

        imgvoice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveEventBus.get("MediaPron")
                        .postOrderly(2);
                Toast.makeText(context, "点击了保存文件", Toast.LENGTH_SHORT).show();
            }
        });

        imgbig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });
    }
}
