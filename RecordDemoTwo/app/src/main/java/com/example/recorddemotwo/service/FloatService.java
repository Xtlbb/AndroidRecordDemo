package com.example.recorddemotwo.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.recorddemotwo.MainActivity;
import com.example.recorddemotwo.R;
import com.example.recorddemotwo.act.MyWaveViewOneAct;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

/**
 * Created by simon on 2018/4/16.
 */

public class FloatService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowManagerParagram;
    private ConstraintLayout constraintLayout;
    private int statusBarHeight;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            windowManager.removeView(constraintLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1 获取 WindowManager 对象
     * 2 获取 WindowManager 参数，并设置参数
     * 3 调用 WindowManager.addView()方法
     * 4 对view设置点击和滑动事件
     * 5 销毁悬浮框
     */
    private void createToucher() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerParagram = new WindowManager.LayoutParams();
        windowManagerParagram.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SCALED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (Build.VERSION.SDK_INT < 26) {
            //这个一定要activity running
            //wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;//TYPE_TOAST
            //TYPE_TOAST targetSDK必须小于26
            windowManagerParagram.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            //需要权限
            if (Build.VERSION.SDK_INT >= 26) {
                windowManagerParagram.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else {
                windowManagerParagram.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
        }
        windowManagerParagram.flags = FLAG_NOT_FOCUSABLE;
        windowManagerParagram.format = PixelFormat.RGBA_8888;
        windowManagerParagram.x = 0;
        windowManagerParagram.y = 0;
        windowManagerParagram.height = 300;
        windowManagerParagram.width = 300;
        windowManagerParagram.gravity = Gravity.START | Gravity.TOP;

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        constraintLayout = (ConstraintLayout) layoutInflater.inflate(R.layout.float_layout, null);
        windowManager.addView(constraintLayout, windowManagerParagram);
//        constraintLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i("状态栏高度为", "状态栏高度为:" + statusBarHeight);

        constraintLayout.findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("", "CLICK");
                Intent intent = new Intent(FloatService.this, MyWaveViewOneAct.class);
                startActivity(intent);
            }
        });
        constraintLayout.findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(constraintLayout);

            }
        });
        constraintLayout.findViewById(R.id.tv_test).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("onTouch", "onTouch: " + "" + String.valueOf(event.getRawX()) + "\t" + String.valueOf(event.getRawY()));
                windowManagerParagram.x = (int) event.getRawX() - 150;
                windowManagerParagram.y = (int) event.getRawY() - statusBarHeight - 150;
                windowManager.updateViewLayout(constraintLayout, windowManagerParagram);
                return false;
            }
        });
    }
}
