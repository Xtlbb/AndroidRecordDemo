package com.example.recorddemotwo.act;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recorddemotwo.Config;
import com.example.recorddemotwo.R;
import com.example.recorddemotwo.service.RecordFileToUpload;
import com.example.recorddemotwo.service.RecordFloatViewService;
import com.example.recorddemotwo.view.MyWaveView;

/**
 * Created by Administrator on 2018/7/17 0017.
 * 创建时间： 2018/7/17 0017
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：
 */

public    class RecordRAct  extends Activity implements View.OnClickListener {

    private ImageView mChangeImageView;
    private TextView mRecordStatus;
    private TextView mRecordTime;
    private ImageView mRecordChange;
    private TextView mTextviewStauts;

    private RelativeLayout relativelayout_showwaveview;
    private View mView;
    private MyWaveView myWaveViewOne;
    private MyWaveView myWaveViewTwo;
    private MyWaveView myWaveViewThree;
    private MyWaveView myWaveViewFoure;
    private MyWaveView myWaveViewFive;
    //添加弹窗
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1;
    Intent mRecordBocast;
    //刷新界面
    private MyRecordReceiverUI localRecevier;
    IntentFilter intentFilter;
    String currentTime;
    private static final  int CURRENTPUSH =1;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CURRENTPUSH:
                    mRecordTime.setText(currentTime);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_record_layout);
        initService();
        initView();
        initToBroacast();
    }

    private void initToBroacast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Config.mRecordStatusStartUI);
        localRecevier = new MyRecordReceiverUI();
        registerReceiver(localRecevier,intentFilter);
    }

    private void initService() {
        mRecordBocast = new Intent();
        Intent intent = new Intent(this,RecordFloatViewService.class);
        startService(intent);
    }
    private void initView() {
        mChangeImageView = findViewById(R.id.iamgeview_change);
        mChangeImageView.setOnClickListener(this);
        mRecordStatus = findViewById(R.id.textview_recordstatus);
        mRecordTime = findViewById(R.id.textview_recordtime);
        mRecordChange = findViewById(R.id.imageview_recordstatus);
        mRecordChange.setOnClickListener(this);
        mTextviewStauts = findViewById(R.id.textview_rstatus);
        relativelayout_showwaveview= findViewById(R.id.relativelayout_showwaveview);
        mView = findViewById(R.id.view_showviewstatus);
        myWaveViewOne = findViewById(R.id.mywaveview_one);
        myWaveViewTwo = findViewById(R.id.mywaveview_two);
        myWaveViewTwo.setWaveSpeed(20);
        myWaveViewTwo.setmColor(1);
        myWaveViewThree = findViewById(R.id.mywaveview_three);
        myWaveViewThree.setWaveSpeed(30);
        myWaveViewThree.setmColor(1);
        myWaveViewFoure = findViewById(R.id.mywaveview_foure);
        myWaveViewFoure.setWaveSpeed(40);
        myWaveViewFoure.setmColor(1);
        myWaveViewFive = findViewById(R.id.mywaveview_five);
        myWaveViewFive.setWaveSpeed(60);
        myWaveViewFive.setmColor(1);
        if (Config.mRecordStatus.equals("rRecording")){
//            Config.mRecordStatus="rStop";
            mRecordChange.setBackgroundResource(R.drawable.bg_record_tostop);
            mTextviewStauts.setText("结束");
            mRecordStatus.setText("正在录音");
            mView.setVisibility(View.INVISIBLE);
            relativelayout_showwaveview.setVisibility(View.VISIBLE);

        }else {
//            Config.mRecordStatus="rRecording";
            mRecordChange.setBackgroundResource(R.drawable.bg_record_tostart);
            mTextviewStauts.setText("开始");
            mRecordStatus.setText("准备录音");
            mView.setVisibility(View.VISIBLE);
            relativelayout_showwaveview.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Config.mFolowStatus.equals("rShowing")){
            mRecordBocast.putExtra("mRecordStatus","rClose");
            mRecordBocast.setAction(Config.mRecordStatusStart);
            sendBroadcast(mRecordBocast);
        }else {

        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iamgeview_change:
                //点击左上角（判断是否在录音中，如果在录音中弹窗，或者直接关闭）
               if (Config.mRecordStatus.equals("rStop")){
                   finish();
               }else {
                   if (!Settings.canDrawOverlays(this)) {
                       Toast.makeText(this, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
                       Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                               Uri.parse("package:" + getPackageName()));
                       startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                   } else {
                       Config.mFolowStatus = "rShowing";
                       mRecordBocast.putExtra("mRecordStatus","rShowing");
                       mRecordBocast.setAction(Config.mRecordStatusStart);
                       sendBroadcast(mRecordBocast);
                       finish();
//                    startService(new Intent(this, FloatService.class));
                   }
               }

                break;
            case R.id.imageview_recordstatus:
               if (Config.mRecordStatus.equals("rStop")){
                   Config.mRecordStatus="rRecording";
                   mRecordChange.setBackgroundResource(R.drawable.bg_record_tostop);
                   mTextviewStauts.setText("结束");
                   mRecordStatus.setText("正在录音");
                   mView.setVisibility(View.INVISIBLE);
                   relativelayout_showwaveview.setVisibility(View.VISIBLE);
                   mRecordBocast.putExtra("mRecordStatus","recordStart");
                   mRecordBocast.setAction(Config.mRecordStatusStart);
                   sendBroadcast(mRecordBocast);

               }else {
                   Config.mRecordStatus="rStop";
                   mRecordChange.setBackgroundResource(R.drawable.bg_record_tostart);
                   mTextviewStauts.setText("开始");
                   mRecordStatus.setText("准备录音");
                   mView.setVisibility(View.VISIBLE);
                   relativelayout_showwaveview.setVisibility(View.INVISIBLE);
                   mRecordBocast.putExtra("mRecordStatus","recordStop");
                   mRecordBocast.setAction(Config.mRecordStatusStart);
                   sendBroadcast(mRecordBocast);
               }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localRecevier);

    }
    public class MyRecordReceiverUI extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
             currentTime = intent.getExtras().getString("currenttime");
            mHandler.sendEmptyMessage(CURRENTPUSH);

        }
    }
}
