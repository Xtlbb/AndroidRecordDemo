package com.example.recorddemotwo.utils;
/**
 * Created by Administrator on 2018/7/16 0016.
 * 创建时间： 2018/7/16 0016
 * 创建人：Tina
 * 邮箱：1208156801@qq.com
 * 描述：
 */

public    class DateUtils   {
    /**
     * 根据毫秒返回时分秒
     * @param time
     * @return
     */
    public static String getFormatHMST(long time){
        time=time/1000;//总秒数
        int s= (int) (time%60);//秒
        int m= (int) (time/60);//分
        int h=(int) (time/3600);//秒
        return String.format("%02d:%02d:%02d",h,m,s);
    }
}
