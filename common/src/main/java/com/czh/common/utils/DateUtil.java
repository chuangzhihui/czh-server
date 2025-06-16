package com.czh.common.utils;

import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DateUtil {
    /**
     * 获取当前日期字符串 默认 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getStrDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //设置为东八区
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date newDate = new Date();
        String dateStr = sdf.format(newDate);
        return dateStr;
    }

    /**
     * 获取当前日期字符串 自定义格式化
     * @param pattern
     * @return
     */
    public static String getStrDate(String pattern)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        //设置为东八区
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date newDate = new Date();
        String dateStr = sdf.format(newDate);
        return dateStr;
    }

    /**
     * 获取当前日期
     * @return
     */
    public static Date getDaDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //设置为东八区
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = new Date();
        String dateStr = sdf.format(date);
        //将字符串转成时间
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newDate=null;
        try {
            newDate = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    /**
     * 获取某个日期多少秒之后的日期
     * @param date
     * @param seconds
     * @return
     */
    public static Date addSeconds(Date date, int seconds)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    /**
     * 获取两个日期相差多少天
     * @param sDate
     * @param eDate
     * @return
     */
    public static Integer dateDifference(Date sDate,Date eDate)
    {
        LocalDate date1 =sDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate date2 = eDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(date1, date2);
        return Math.toIntExact(daysBetween);
    }

    /**
     * 根据条件获取时间段的开始和结束时间
     * @param type 1今日 2本周 3本月 4累计
     * @return
     */
    public static String[] getTimesByType(Integer type) {
        String stime="";
        String etime="";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        if(type==1)
        {
            stime=sdf.format(new Date())+" 00:00:00";
            etime=sdf.format(new Date())+" 23:59:59";
        }else if(type==2)
        {
            Calendar calendar=Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            stime=sdf.format(calendar.getTime())+" 00:00:00";
            etime=sdf.format(new Date())+" 23:59:59";
        }else if(type==3){
            //本月
            LocalDate today = LocalDate.now();
            LocalDate firstDay = today.withDayOfMonth(1);
            Date date = Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
            stime=sdf.format(date)+" 00:00:00";
            etime=sdf.format(new Date())+" 23:59:59";
        }else{
            stime="1970-01-01 00:00:00";
            etime=sdf.format(new Date())+" 23:59:59";
        }
        return new String[]{stime,etime};
    }

    //将开始时间和结束时间分割为date列表
    @SneakyThrows
    public static List<Date> rangeTimes(String stime, String etime)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date sdate=sdf.parse(stime);
        Calendar sc=Calendar.getInstance();
        sc.setTime(sdate);
        Date edate=sdf.parse(etime);
        Calendar ec=Calendar.getInstance();
        ec.setTime(edate);

        List<Date> dates = new ArrayList<>();
        Calendar currentDate = (Calendar) sc.clone();
        while (currentDate.before(ec) || currentDate.equals(ec)) {
            dates.add(((Calendar) currentDate.clone()).getTime());
            currentDate.add(Calendar.DATE, 1); // 增加一天
        }
        return dates;
    }

    //查询Date 在列表中的下标
    public static  int findIndex(List<Date> dates,Date findDate)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String findDateStr=sdf.format(findDate);
        for(int i=0;i<dates.size();i++)
        {
            Date date=dates.get(i);
            String dateStr=sdf.format(date);
            if(dateStr.equals(findDateStr))
            {
                return i;
            }
        }
        return -1;
    }
}
