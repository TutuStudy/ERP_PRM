package com.yeelight.erp.crm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;




public class HttpURLConnectionTest {


    public String getConn(){
        try {
            //获取销售易URL
            URL url =new URL("https://api.xiaoshouyi.com/data/v1/query");
            //得到网络访问对象
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();

            //connection.setRequestProperty("Authorization","Bearer e06a086379d8990cc20ff136600483647782a98d3be0b63c6a60b3195074eceb.Mzg2MTUw");

            //设置请求参数
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

             //连接
            connection.connect();

            //返回状态码
            //int code=connection.getResponseCode();
            DataOutputStream out= new DataOutputStream(connection.getOutputStream());

                JSONObject jObj = new JSONObject();
                out.writeBytes(jObj.toString());
                out.flush();
                out.close();


            connection.disconnect();
            return "success";

        } catch (Exception e) {
            e.printStackTrace();
        }
            return "error";
    }
}
