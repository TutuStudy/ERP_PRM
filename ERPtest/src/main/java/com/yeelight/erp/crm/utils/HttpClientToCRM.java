package com.yeelight.erp.crm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class HttpClientToCRM {

    public static HttpPost getPost(String url) {

        //创建post
        HttpPost post = new HttpPost(url);
        //获取 access token
        String accessToken = getAccessToken();
        //构造post头部
        post.setHeader("Authorization", accessToken);
        //设置post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        return post;
    }

    public static String getAccessToken() {
        String accessToken = "";
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://api.xiaoshouyi.com/oauth2/token");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("client_id", "da20ab4fe759304705f983ee31308701"));
        params.add(new BasicNameValuePair("client_secret", "255be38adb76838a6db454d0bbcf4b21"));
        params.add(new BasicNameValuePair("redirect_uri", "http://yeelight.ik3cloud.com/K3cloud/"));
        params.add(new BasicNameValuePair("username", "18353385998"));
        params.add(new BasicNameValuePair("password", "jwp46540nHLGCYMj"));

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            CloseableHttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                accessToken = EntityUtils.toString(entity);
                System.out.println("access_token内容：" + accessToken);
            }
            response.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObjectFromAccessToken = JSON.parseObject(accessToken);
        jsonObjectFromAccessToken.getString("access_token");

        String str = "Bearer " + jsonObjectFromAccessToken.getString("access_token");

        return str;
    }


    public static String getCrmContext(String url, String name, String value) {

        //1. 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //创建post
        HttpPost post = new HttpPost(url);
        //获取 access token
        String accessToken = getAccessToken();
        //构造post头部
        post.setHeader("Authorization", accessToken);
        //设置post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //设置param参数
        params.add(new BasicNameValuePair(name, value));

        String entityOfString = null;

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            post.setEntity(entity);
            //发送到CRM执行
            return entityOfString = transferOrderToCrm(post, entityOfString, httpClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "transferOrderToCrm Failed";
    }

    private static String transferOrderToCrm(HttpPost post, String entityOfString, CloseableHttpClient httpClient) {

        try {
            CloseableHttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                System.out.println("CRM的返回内容：");
                entityOfString = EntityUtils.toString(entity);
                System.out.println(entityOfString);
            }
            response.close();
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entityOfString;
    }


    public void returnCrmOrder(String crmorder, String orderId) {
        String url = "https://api.xiaoshouyi.com/data/v1/objects/order/update";
        //1. 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //获取post
        HttpPost post = getPost(url);
        //设置post参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", orderId);
        jsonObject.put("dbcVarchar1", crmorder);

        try {
            //设置post参数
            post.setEntity(getPostString(jsonObject));
            //传输到CRM执行
            String entityOfString = null;
            transferOrderToCrm(post, entityOfString, httpClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private StringEntity getPostString(JSONObject jsonObject) throws UnsupportedEncodingException {
        String json = jsonObject.toJSONString();
        System.out.println(json);
        StringEntity postingString = new StringEntity(json);// json传递
        return postingString;
    }

    public void returnCrmRebate(JSONArray jsonArray) {
        String url = "https://api.xiaoshouyi.com/data/v1/objects/customize/create";

        //1. 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //获取post
        HttpPost post = getPost(url);
        //设置post参数
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("record", jsonArray.getJSONObject(i));
            jsonObject.put("belongId", "101134798");
            //执行
            String json = jsonObject.toJSONString();
            System.out.println(json);
            String entityOfString = null;
            try {
                StringEntity postingString = new StringEntity(json);// json传递
                post.setEntity(postingString);
                //传输到Crm去执行
                transferOrderToCrm(post, entityOfString, httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray getCrmContextJSONArray(String url, String name, String executeSQL) {
        String productStr = getCrmContext(url, name, executeSQL);
        JSONObject jsonObjectProduct = JSON.parseObject(productStr);
        JSONArray jsonArrayRecords = jsonObjectProduct.getJSONArray("records");
        return jsonArrayRecords;
    }
}
