package com.example.recorddemotwo.utils;
/**
 * Created by Administrator on 2018/7/16 0016.
 * 创建时间： 2018/7/16 0016
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：
 */

public    interface IAudioRecordListenter   {
    //开始录音
    void recordStart();

    //暂停录音
    void recordPause();

    /**
     * 录音成功（即录音结束）
     * @param mDurationRecordTime 总的录音时长（毫秒）
     */
    void recordSuccess(long mDurationRecordTime);

    /**
     * 录音失败
     * @param errorMsg
     */
    void recordFail(String errorMsg);
}
