package com.example.recorddemotwo.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/7/16 0016.
 * 创建时间： 2018/7/16 0016
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：
 */

public    class AudioRecordUtil   {
    private final String TAG = AudioRecordUtil.class.getSimpleName();
    /**
     * 编码采样默认
     */
    private int mDefaultEncodingBitRate = 96000;

    /**
     * 采样默认为44100（所有android都支持）
     */
    private int mDefaultSamplingRate = 44100;
    private static MediaRecorder mMediaRecorder;
    private IAudioRecordListenter mIAudioRecordListener;

    /**
     * 设置默认的文件名路径
     */
    private String mDefaultFilePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/audioRecord/"
            + System.currentTimeMillis()
            + ".m4a";

    //    private String mDefaultFilePath = "/mnt/sdcard/Benetech/Doctor/recorder/"
//            + System.currentTimeMillis()
//            + ".m4a";
    private File mAudioRecordFile;
    /**
     * 默认采集源为麦克风
     */
    private int mDefaultAudioSource = MediaRecorder.AudioSource.MIC;
    /**
     * 默认输出格式为mp4
     */
    private int mDefaultOutputFormat = MediaRecorder.OutputFormat.MPEG_4;
    /**
     * 默认的编码格式aac
     */
    private int mDefualtAudionEnoder = MediaRecorder.AudioEncoder.AAC;

    //录音开始时间，录音结束时间，录音时长
    private long mStartRecordTime, mStopRecordTime, mDurationRecordTime;
    private ExecutorService mExecutorService;
    //默认有效录音时长为3秒，如果低于3秒就录音无效
    private long mDefaultEffeicentTime = 3 * 1000;
    /**
     * 主线程Handler，用于在监听里更新UI
     */
    private Handler mMainHandler;
    private static AudioRecordUtil singleton;


    private AudioRecordUtil(){
        //JNI函数不具备线程安全性，所以要用单线程
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
    }
    /**
     * 23以上需要检查权限
     * Manifest.permission.WRITE_EXTERNAL_STORAGE
     * 和 Manifest.permission.RECORD_AUDIO
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.RECORD_AUDIO})
    public static AudioRecordUtil newInstance(){
        if(singleton == null){
            synchronized (AudioRecordUtil.class){
                if(singleton == null)
                    singleton = new AudioRecordUtil();
            }
        }
        return singleton;
    }

    private void initMediaRecorder(){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(mDefaultAudioSource);
        mMediaRecorder.setOutputFormat(mDefaultOutputFormat);
        mMediaRecorder.setAudioSamplingRate(mDefaultSamplingRate);
        mMediaRecorder.setAudioEncoder(mDefualtAudionEnoder);
        mMediaRecorder.setAudioEncodingBitRate(mDefaultEncodingBitRate);

        mMediaRecorder.setOutputFile(mAudioRecordFile.getAbsolutePath());//设置存放的录音文件
    }
    /**
     * 设置要保存的文件路径
     *
     * @param filePath
     */
    public void setFilePath(String filePath) {
        if (filePath != null) {
            this.mDefaultFilePath = filePath;
        }

    }

    /**
     * 设置采集源，默认为来自麦克风
     *
     * @param source
     */
    public void setAudioSource(int source) {
        this.mDefaultAudioSource = source;
    }

    /**
     * 设置输出格式，默认为mp4
     *
     * @param format
     */
    public void setOutputFormat(int format) {
        this.mDefaultOutputFormat = format;
    }

    /**
     * 设置采样率，默认为44100（所有Android都支持的采样率）
     *
     * @param samplingRate
     */
    public void setSamplingRate(int samplingRate) {
        this.mDefaultSamplingRate = samplingRate;
    }

    /**
     * 设置录音最小有效时间
     *
     * @param timeMills 毫秒
     */
    public void setMinEfficentRecordTime(long timeMills) {
        this.mDefaultEffeicentTime = timeMills;
    }

    /**
     * 开始录音
     * 在recordStart里回调
     */
    public void recordStart() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                recordRelease();
                if (!doStartRecord()) {
                    recordFail();
                }
            }
        });
    }

    /**
     * 开启录音操作
     * @return 是否开启成功
     */
    private boolean doStartRecord() {
        try {
            mAudioRecordFile = new File(mDefaultFilePath);
            mAudioRecordFile.getParentFile().mkdirs();
            mAudioRecordFile.createNewFile();
            initMediaRecorder();




            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();
        mStartRecordTime = System.currentTimeMillis();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mIAudioRecordListener != null) {
                    mIAudioRecordListener.recordStart();
                }

            }
        });


        return true;
    }

    /**
     * 停止录音
     * 在recordSuccess里回调
     */
    public void recordStop() {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStopRecord()) {
                    recordFail();
                }
                recordRelease();
            }
        });
    }

    /**
     * 停止录音操作
     * @return 是否停止成功
     */
    private boolean doStopRecord() {
        try {
            mMediaRecorder.stop();
            mStopRecordTime = System.currentTimeMillis();
            mDurationRecordTime = mStopRecordTime - mStartRecordTime;
            if (mDurationRecordTime > mDefaultEffeicentTime) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIAudioRecordListener.recordSuccess(mDurationRecordTime);
                    }
                });
            }else {
                mIAudioRecordListener.recordFail("录音时间过短，无法保存");
                mAudioRecordFile.delete();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 录音失败
     */
    private void recordFail() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mIAudioRecordListener != null) {
                    mIAudioRecordListener.recordFail("录音失败");
                }

            }
        });
    }

    private void recordRelease() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 设置录音过程的监听
     *
     * @param iAudioRecordListener
     */
    public void setAudioRecordListener(IAudioRecordListenter iAudioRecordListener) {
        this.mIAudioRecordListener = iAudioRecordListener;
    }

    /**
     * 释放资源，在生命周期的onDestroy里调用
     */
    public void onDestroy() {
        mExecutorService.shutdownNow();
        recordRelease();
    }

    /**
     * 获取最新的录制好的音频文件
     * @return 成功的话返回该文件的路径，否则返回null
     */
    public String getLatestRecordFile(){
        if(mAudioRecordFile != null && mAudioRecordFile.exists()){
            return mAudioRecordFile.getAbsolutePath();
        }else {
            return null;
        }
    }


}
