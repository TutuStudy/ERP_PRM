package com.yeelight.erp.crm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class MyUntilClass {
    public static long getStartTime() {
        long ts = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00");
        try {
            Date date = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            ts = date.getTime();
            String res = String.valueOf(ts);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;

    }

    public static long getEndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime nowTime = LocalDateTime.now();
        //获取当前日期
        LocalDate nowDate = LocalDate.now();
        //设置零点
        LocalDateTime beginTime = LocalDateTime.of(nowDate, LocalTime.MIN);
        //将时间进行格式化
        String time1 = beginTime.format(dtf);

        //设置当天的结束时间
        LocalDateTime endTime = LocalDateTime.of(nowDate, LocalTime.MAX);
        //将时间进行格式化
        String time2 = dtf.format(endTime);
        System.out.println("今天开始的时间beginTime：" + time1);
        System.out.println("今天结束的时间endTime：" + time2);
        long ts = 0;
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = simpleDateFormat1.parse(time2);
            ts = date.getTime();
            String res = String.valueOf(ts);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;

    }

    public String returnsql(String value) {
        MyUntilClass myUntilClass = new MyUntilClass();
        long startTime = myUntilClass.getStartTime();
        long endTime = myUntilClass.getEndTime();
        if (value == "po") {
            //只同步日期为生效日期为当天的采购订单，且没有销售订单编号的
            String sql = "select id from _order where  poStatus=2 and po like '%SO%' and dbcVarchar1='yeelight' and entityType=8008618 and effectiveDate >'" + startTime + "' and effectiveDate<'" + endTime + "'";
            return sql;
        }
        if (value == "ro") {
            //同步生效日期为退货的退货订单
            String sql = "select id from _order where  poStatus=2 and roStatus=2 and po like '%RO%'and dbcVarchar1 like '%XSDD%'   and effectiveDate >'" + startTime + "' and effectiveDate<'" + endTime + "'";
            return sql;
        }
        if (value == "rop") {
            //同步生效日期为退货的销售订单，ERP订单编号先为yeelight
            String sql = "select id from _order where  poStatus=2 and roStatus=2 and po like '%RO%' and dbcVarchar1 = 'yeelight'  and effectiveDate >'" + startTime + "' and effectiveDate<'" + endTime + "'";
            return sql;
        }
        return "ERROR";
    }

    public String returnfilterString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime nowTime = LocalDateTime.now();
        //获取当前日期
        LocalDate nowDate = LocalDate.now();
        //设置零点
        LocalDateTime beginTime = LocalDateTime.of(nowDate, LocalTime.MIN);
        //将时间进行格式化
        String time1 = beginTime.format(dtf);

        //设置当天的结束时间
        LocalDateTime endTime = LocalDateTime.of(nowDate, LocalTime.MAX);
        //将时间进行格式化
        String time2 = dtf.format(endTime);
        System.out.println("今天开始的时间beginTime：" + time1);
        System.out.println("今天结束的时间endTime：" + time2);
        String elsesql = " FDOCUMENTSTATUS='C' and ";
        String returnsql = "FModifyDate>={ts'" + time1 + "'} and FModifyDate  <= {ts'" + time2 + "'}";
        return returnsql;
    }

    public long returnStartQuaterTime() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.MONTH, ((int) startCalendar.get(Calendar.MONTH) / 3 - 1) * 3);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(startCalendar.getTime());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);


        Date startTimeCalender = startCalendar.getTime();
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTimeCalender);
        long startTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startTimeString, new ParsePosition(0)).getTime() / 1000;
        System.out.println(startTime);


        return startTime;
    }

    public long returnEndQuaterTime() {

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.MONTH, ((int) endCalendar.get(Calendar.MONTH) / 3 - 1) * 3 + 2);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        System.out.println(endCalendar.getTime());
        endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
        endCalendar.set(Calendar.MILLISECOND, endCalendar.getActualMaximum(Calendar.MILLISECOND));


        Date endTimeCalender = endCalendar.getTime();
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTimeCalender);
        long endTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endTimeString, new ParsePosition(0)).getTime() / 1000;
        System.out.println(endTime);

        return endTime;
    }

    public String returnQuaterTime() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.MONTH, ((int) startCalendar.get(Calendar.MONTH) / 3 - 1) * 3);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(startCalendar.getTime());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);


        Date startTimeCalender = startCalendar.getTime();
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTimeCalender);
        return startTimeString;
    }


    public int getPaginationOffSet(String offSetSQL) {
        HttpClientToCRM conn = new HttpClientToCRM();
        String url = "https://api.xiaoshouyi.com/data/v1/query";
        String name = "q";
        String productStr = conn.getCrmContext(url, name, offSetSQL);
        JSONObject jsonObjectProduct = JSON.parseObject(productStr);
        int totalSize = jsonObjectProduct.getIntValue("totalSize");
        if (totalSize > 300) {
            //进行分页
            int i = totalSize / 300;
            int j = totalSize % 300;
            if (j > 0) {
                return ++i;
            }else{
                return i;
            }
        }
        int count = jsonObjectProduct.getIntValue("count");
        return 1;
    }
}

