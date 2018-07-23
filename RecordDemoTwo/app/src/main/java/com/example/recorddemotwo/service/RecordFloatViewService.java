package com.example.recorddemotwo.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.recorddemotwo.Config;
import com.example.recorddemotwo.R;
import com.example.recorddemotwo.act.MyWaveViewOneAct;
import com.example.recorddemotwo.act.RecordRAct;
import com.example.recorddemotwo.utils.AudioRecordUtil;
import com.example.recorddemotwo.utils.DateUtils;
import com.example.recorddemotwo.utils.IAudioRecordListenter;
import java.io.File;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static com.example.recorddemotwo.Config.mFolowSt;

/**
 * Created by Administrator on 2018/7/17 0017.
 * 创建时间： 2018/7/17 0017
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：录音的service
 */

public    class RecordFloatViewService  extends Service {
    private static String TAG = "RecordFloatViewService";

    private static final int MSG_INIT = 0;
    private static final int PUSH_TIME =1;
    //录音相关
    private AudioRecordUtil mAudioRecord;
    private MyRecordReceiver localRecevier;

    IntentFilter intentFilter;
    //计时器
    private boolean isPause =false;//是否暂停
    private long currentSencond =0;//当前毫秒数
    //////////////////////////////2018/07/16添加悬浮窗
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowManagerParagram;
    private int statusBarHeight;
    private RelativeLayout relative_timerecord;
    private TextView mTextViewTime;
//    private View floatView;

    private String mRecordStatus;
    Intent mRecordBocast;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_INIT:
                    if (Config.mRecordStatus.equals("rRecording")&& Config.mFolowStatus .equals("rShowing")){
                        mTextViewTime.setText(DateUtils.getFormatHMST(currentSencond));
                    }else {
                        Log.e(TAG, "handleMessage: ==="+Config.mRecordStatus+"---mFolowStatus"+ Config.mFolowStatus);//NoShow
                        mRecordBocast.putExtra("currenttime",DateUtils.getFormatHMST(currentSencond));
                        mRecordBocast.setAction(Config.mRecordStatusStartUI);
                        sendBroadcast(mRecordBocast);

                    }
                    break;
            }
        }
    };
    private Handler mhandle = new Handler();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Config.mRecordStatusStart);
        localRecevier = new MyRecordReceiver();
        registerReceiver(localRecevier,intentFilter);
        mRecordBocast = new Intent();
        initRecord();
    }

    private void initRecord() {
//        Toast.makeText(RecordServices.this, "++++", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mAudioRecord = AudioRecordUtil.newInstance();
        mAudioRecord.setAudioRecordListener(new IAudioRecordListenter() {
            @Override
            public void recordStart() {
//                Toast.makeText(RecordFileToUpload.this, "开始录音", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void recordPause() {

            }

            @Override
            public void recordSuccess(long mDurationRecordTime) {
//                Toast.makeText(MyApp.getContext(), "录音成功", Toast.LENGTH_SHORT).show();
//                Log.d("","=====/audioRecord/");
                File file = new File(mAudioRecord.getLatestRecordFile());
                if (file.exists()) {
//
//                    Log.d(TAG , "file exists"+file.getAbsolutePath());
//                    //作上传录音
//                    Intent intent = new Intent(RecordFileToUpload.this,UpLoadFileRecordService.class);
//                    intent.putExtra("filepath",file.getAbsolutePath());
//                    startService(intent);
                }else {
                    Log.d(TAG , "file not exists");
                }
            }
            @Override
            public void recordFail(String errorMsg) {
                Log.e(TAG, "recordFail: "+errorMsg);
                isPause =true;

//                Toast.makeText(MyApp.getContext(), "录音失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void startRecord(){
//        mAudioRecord.recordStart();

        mAudioRecord.recordStart();

    }

    public void stopRecord(){
        mAudioRecord.recordStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioRecord.onDestroy();
        unregisterReceiver(localRecevier);
        try {
            Config.mFolowStatus = "rNoShow";
            windowManager.removeView(relative_timerecord);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class MyRecordReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            mRecordStatus = intent.getExtras().getString("mRecordStatus");
            if (mRecordStatus.equals("recordStart")){
                startRecord();
                currentSencond=0;
                isPause =false;
                Thread t=new Thread(timeRunable);//开始计时
                t.start();
            }else if (mRecordStatus.equals("recordStop")){//recordStart
                isPause = true;
                stopRecord();
            }else if (mRecordStatus.equals("rShowing")){
                Log.e(TAG,"onReceive==="+mRecordStatus);
                //创建view

                    createToucher();
            }else {

                try {
                    Config.mFolowStatus = "rNoShow";
                    windowManager.removeView(relative_timerecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 获取WindowManager对象
     * 获取WindowManager参数，并设置参数
     * 调用WindowManager.add的方法
     * 对View设置点击和滑动事件
     * 销毁悬浮框
     */
    private void createToucher() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerParagram = new WindowManager.LayoutParams();
        windowManagerParagram.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | FLAG_NOT_FOCUSABLE
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
//        floatView = layoutInflater.inflate(R.layout.float_record_time, null);
        relative_timerecord = (RelativeLayout) layoutInflater.inflate(R.layout.float_record_time, null);
        windowManager.addView(relative_timerecord, windowManagerParagram);
//        constraintLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        relative_timerecord= relative_timerecord.findViewById(R.id.relative_timerecord);
        relative_timerecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFolowSt ="isUp";
                Intent intent = new Intent(RecordFloatViewService.this, RecordRAct.class);
                startActivity(intent);
//                try {
//                    Config.mFolowStatus = "rNoShow";
//                    windowManager.removeView(relative_timerecord);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


            }
        });
        relative_timerecord.setOnTouchListener(new View.OnTouchListener() {
            boolean result = false;

            float x = 0;
            float y = 0;
            float startX = 0;
            float startY = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("onTouch", "onTouch: " + "" + String.valueOf(event.getRawX()) + "\t" + String.valueOf(event.getRawY()));
//                windowManagerParagram.x = (int) event.getRawX() - 150;
//                windowManagerParagram.y = (int) event.getRawY() - statusBarHeight - 150;
//                windowManager.updateViewLayout(relative_timerecord, windowManagerParagram);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        result = false;
                        x = event.getX();
                        y = event.getY();
                        startX = event.getRawX();
                        startY = event.getRawY();
//                        VMLog.d("start x: %f, y: %f", startX, startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        VMLog.d("move x: %f, y: %f", event.getRawX(), event.getRawY());
                        // 当移动距离大于特定值时，表示是拖动悬浮窗，则不触发后边的点击监听
                        if (Math.abs(event.getRawX() - startX) > 20 || Math.abs(event.getRawY() - startY) > 20) {
                            result = true;
                        }
                        // getRawX 获取触摸点相对于屏幕的坐标，getX 相对于当前悬浮窗坐标
                        // 根据当前触摸点 X 坐标计算悬浮窗 X 坐标，
                        windowManagerParagram.x = (int) event.getRawX()-150;
//                        windowManagerParagram.x = (int) (event.getRawX() - x);
                        // 根据当前触摸点 Y 坐标计算悬浮窗 Y 坐标，减25为状态栏的高度
                        windowManagerParagram.y = (int) (event.getRawY() - y - 25);
                        // 刷新悬浮窗
                        windowManager.updateViewLayout(relative_timerecord, windowManagerParagram);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return result;
//                return false;
            }
        });

        mTextViewTime = relative_timerecord.findViewById(R.id.textview_time);
        mTextViewTime.setText("");
    }

    //编写计时器（限时20分钟）

    private Runnable timeRunable = new Runnable() {
        @Override
        public void run() {
            currentSencond = currentSencond + 1000;
//            contentView.setTextViewText(R.id.textview_recordtime,""+ DateUtils.getFormatHMST(currentSencond));

            Log.e(TAG,"timeRunable=="+currentSencond);
            mHandler.sendEmptyMessage(MSG_INIT);
            if (!isPause){
                //递归调用本地runable对象，实现每隔一秒一次执行任务
                mhandle.postDelayed(this,1000);
            }else {
//                notificationManager.cancelAll();

            }

        }
    };

}
