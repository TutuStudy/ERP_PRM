package com.yeelight.erp.crm.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpRequestSend {

    private <T> Map<String, String> parseParam(Map<String, String> param) {
        String beforeEncrypt = getSortedParamString(param);

        String dataValue = null;
        try {
            dataValue = encrypt(beforeEncrypt);
        } catch (InvalidKeyException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchPaddingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (BadPaddingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalBlockSizeException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchProviderException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, String> signParam = new TreeMap<String, String>(new MapKeyComparator());
        signParam.put("data", dataValue);
        signParam.put("timestamp", timeStamp);
        signParam.put("partner_id", "23");
        String beforeSign = getSortedParamString(signParam);
		/*InputStream invoicePdf = null;
		try {
			invoicePdf = new FileInputStream(new File("/Users/wuguofu/Downloads/97.pdf"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = null;
		try {
			buffer = new byte[invoicePdf.available()];
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int n = 0;
		try {
			while ((n = invoicePdf.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(buffer.length);
*/
        byte[] abyte = beforeSign.getBytes();
        //byte[] bbyte = buffer;
        String str = "39d87b5bece276b9e3ebd7e77f78eff9";
        byte[] dbyte = str.getBytes();
        int alen = abyte.length;
        //int blen = bbyte.length;
        //byte[] cbyte = new byte[alen+blen+dbyte.length];
        byte[] cbyte = new byte[alen + dbyte.length];
        for (int i = 0; i < abyte.length; i++) {
            cbyte[i] = abyte[i];
        }
        /*for(int i=0;i<bbyte.length;i++){
        	cbyte[alen+i] = bbyte[i];
        }*/
        for (int i = 0; i < dbyte.length; i++) {
            //cbyte[alen+blen+i] = dbyte[i];
            cbyte[alen + i] = dbyte[i];
        }
        System.out.println(new String(cbyte));
        System.out.println(cbyte.length);
        String sign = null;
        sign = DigestUtils.md5Hex(cbyte);

        signParam.put("sign", sign);
        return signParam;
    }


    private String getSortedParamString(Map<String, String> param) {
        boolean isFirst = true;
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : param.keySet()) {
            if (!isFirst) {
                stringBuilder.append("&");
            }
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(param.get(key));
            isFirst = false;
        }
        return stringBuilder.toString();
    }


    private String encrypt(String value) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        SecretKeySpec aesKey = null;
        aesKey = new SecretKeySpec("59eb0420a2c2fa02".getBytes(Charset.forName("UTF-8")), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = null;
        encrypted = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
        String base64Encoding = Base64.encodeBase64String(encrypted);
        return base64Encoding;
    }

    class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://openapi.test.youpin.mi.com/openapi/shop/orderlist");
        httpPost.setHeader("User-Agent", "SOHUWapRebot");
        httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpPost.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.7");
        httpPost.setHeader("Connection", "keep-alive");
        Map<String, String> params = new HashMap<String, String>();
        params.put("partner_id", "23");
        //params.put("status", "1");
        params.put("beginTime", "1516204800");
        params.put("endTime", "1516377600");
        //params.put("order_id", "103905555");
        HttpRequestSend hrs = new HttpRequestSend();
        Map<String, String> result = hrs.parseParam(params);

        JSONObject param2 = new JSONObject();

        param2.put("data", result.get("data").toString());
        param2.put("timestamp", result.get("timestamp").toString());
        param2.put("sign", result.get("sign").toString());
        param2.put("partner_id", result.get("partner_id").toString());
        StringEntity mutiEntity = null;
        System.out.println(param2);
        mutiEntity = new StringEntity(param2.toString());
        System.out.println(mutiEntity);


 /*

        MultipartEntity mutiEntity = new MultipartEntity();
        //File file = new File("/Users/wuguofu/Downloads/97.pdf");
        try {
            mutiEntity.addPart("data", new StringBody(result.get("data").toString()));
            mutiEntity.addPart("timestamp", new StringBody(result.get("timestamp").toString()));
           mutiEntity.addPart("sign", new StringBody(result.get("sign").toString()));
            mutiEntity.addPart("partner_id", new StringBody(result.get("partner_id").toString()));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
  */

        //mutiEntity.addPart("file", new FileBody(file));


        httpPost.setEntity(mutiEntity);
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            String content = EntityUtils.toString(httpEntity, "utf-8");
            System.out.println(content);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

