package com.example.recorddemotwo.act;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.recorddemotwo.Config;
import com.example.recorddemotwo.R;
import com.example.recorddemotwo.service.FloatService;
import com.example.recorddemotwo.service.RecordFileToUpload;
import com.example.recorddemotwo.view.MyWaveView;

/**
 * Created by Administrator on 2018/7/16 0016.
 * 创建时间： 2018/7/16 0016
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：
 */

public    class MyWaveViewOneAct   extends Activity implements View.OnClickListener {
    private MyWaveView waveview_one;
    private MyWaveView waveview_two;
    private MyWaveView waveview_three;

    private Button mStart;
    private Button mStop;
    Intent mRecordBocast;
    private Button change_layout;
    //添加弹窗
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.layout_mywaveview);
      initService();
      initView();
    }

    private void initService() {
        mRecordBocast = new Intent();
        Intent intent = new Intent(this,RecordFileToUpload.class);
        startService(intent);
    }
    private void initView() {
        waveview_one = findViewById(R.id.waveview_one);
//        waveview_one.setWaveSpeed(0);
        waveview_two = findViewById(R.id.waveview_two);
        waveview_two.setmColor(1);
        waveview_two.setWaveSpeed(60);
        waveview_three = findViewById(R.id.waveview_three);
        waveview_two.setmColor(1);
        waveview_three.setWaveSpeed(30);

        mStart = findViewById(R.id.button_star);
        mStart.setOnClickListener(this);
        mStop = findViewById(R.id.button_stop);
        mStop.setOnClickListener(this);
        change_layout = findViewById(R.id.change_layout);
        change_layout.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Config.isCreate.equals("isCreate")){
            mRecordBocast.putExtra("isRecord","createno");
            mRecordBocast.setAction("com.broadcast.recordstar");
            sendBroadcast(mRecordBocast);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_star:
                mRecordBocast.putExtra("isRecord","start");
                mRecordBocast.setAction("com.broadcast.recordstar");
                sendBroadcast(mRecordBocast);
                break;
            case R.id.button_stop:
                mRecordBocast.putExtra("isRecord","stop");
                mRecordBocast.setAction("com.broadcast.recordstar");
                sendBroadcast(mRecordBocast);
                break;
            case R.id.change_layout:
                //添加addView
//                mRecordBocast.putExtra("isRecord","create");
//                mRecordBocast.setAction("com.broadcast.recordstar");
//                sendBroadcast(mRecordBocast);
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                } else {
                    Config.isCreate = "isCreate";
                    mRecordBocast.putExtra("isRecord","create");
                    mRecordBocast.setAction("com.broadcast.recordstar");
                    sendBroadcast(mRecordBocast);
                    finish();
//                    startService(new Intent(this, FloatService.class));
                }

            break;
        }
    }
}