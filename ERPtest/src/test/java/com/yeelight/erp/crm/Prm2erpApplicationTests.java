package com.yeelight.erp.crm;

import com.alibaba.fastjson.JSONObject;
import com.yeelight.erp.crm.utils.HttpClientToCRM;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Prm2erpApplicationTests {


    @Test
    public  void Test2(){
        JSONObject jsonObject1=new JSONObject();
        JSONObject jsonObject2=new JSONObject();

        jsonObject1.put("NAME","JSON");

        for (int i = 0; i <5 ; i++) {
            jsonObject2.put("KD"+i,jsonObject1);
        }

        System.out.println(jsonObject2);
        System.out.println(jsonObject1);

    }
    @Test
    public void Test1() {
        int totalSize = 932;
        if (totalSize > 300) {
           int i = totalSize / 300;
            int j = totalSize % 300;
            if (j > 0) {
                System.out.println("result: "+ ++i);
            }
            System.out.println("result: "+ i);

        }
    }

    @Test
    public void getLastQuarter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.MONTH, ((int) startCalendar.get(Calendar.MONTH) / 3 - 1) * 3);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(startCalendar.getTime());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);


        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.MONTH, ((int) endCalendar.get(Calendar.MONTH) / 3 - 1) * 3 + 2);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        System.out.println(endCalendar.getTime());
        endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
        endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
        endCalendar.set(Calendar.MILLISECOND, endCalendar.getActualMaximum(Calendar.MILLISECOND));


        Date startTimeCalender = startCalendar.getTime();
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTimeCalender);
        long startTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startTimeString, new ParsePosition(0)).getTime() / 1000;
        System.out.println(startTime);


        Date endTimeCalender = endCalendar.getTime();
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTimeCalender);
        long endTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(endTimeString, new ParsePosition(0)).getTime() / 1000;
        System.out.println(endTime);

    }


    @Test
    public void returnfilterString() {
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

        String returnsql = "FModifyDate>={ts'" + time1 + "'} and FModifyDate  <= {ts'" + time2 + "'}";
        System.out.println(returnsql);
    }


    @Test
    public void math() {
        String s = "3";
        double rebat = Double.parseDouble(s);
        double reba1 = rebat / 100;
    }

    @Test
    public void contextLoads() {
        Map<String, Object> map = new HashMap<>();
        map.put("FNUMBER", "XSDD01_SYS");
        JSONObject jsonObject1 = (JSONObject) new JSONObject().put("FNUMBER", "XSDD01_SYS");
        JSONObject jsonObject2 = new JSONObject();
        JSONObject jsonObject3 = new JSONObject();
        JSONObject jsonObject4 = new JSONObject(map);

        jsonObject2.put("FNUMBER", "XSDD01_SYS");
        jsonObject3.put("FNUMBER", jsonObject2);

        System.out.println("测试：");
        System.out.println(jsonObject1);
        System.out.println(jsonObject2);
        System.out.println(jsonObject3);
        System.out.println(jsonObject4);
    }

    @Test
    public void getDatatime() {


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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = null;
        try {
            date = simpleDateFormat.parse(time2);
            System.out.println(date);
            long ts = date.getTime();
            System.out.println(ts);
            String res = String.valueOf(ts);
            System.out.println(res);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void updateOrder() {
        String url = "https://api.xiaoshouyi.com/data/v1/objects/order/update";
        //1. 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //创建post
        HttpPost post = new HttpPost(url);
        //获取 access token
        String accessToken = HttpClientToCRM.getAccessToken();
        //构造post头部
        post.setHeader("Authorization", accessToken);
        //设置post参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "2785430");
        jsonObject.put("comment", "订单更新2");
        String json = jsonObject.toJSONString();
        System.out.println(json);
        String entityOfString = null;
        try {
            StringEntity postingString = new StringEntity(json);// json传递
            post.setEntity(postingString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            CloseableHttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                System.out.println("CRM的返回内容：");
                entityOfString = EntityUtils.toString(entity);
                System.out.println(entityOfString);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
