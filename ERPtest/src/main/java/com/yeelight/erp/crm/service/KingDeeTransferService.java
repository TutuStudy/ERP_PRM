package com.yeelight.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yeelight.erp.crm.constants.Constants;
import com.yeelight.erp.crm.utils.HttpClientToCRM;
import com.yeelight.erp.crm.utils.InvokeHelper;
import com.yeelight.erp.crm.utils.MyUntilClass;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class KingDeeTransferService {


    private static final String dbId = Constants.DBID;
    private static final String uid = Constants.UID;
    private static final String pwd = Constants.PWD;
    private static final int lang = Constants.LANG;


    // 回传订单保存
    public String transferToKingdee(JSONArray JsonArrayfromOrder, String sFormId, String value) {
        if (JsonArrayfromOrder == null || JsonArrayfromOrder.size() == 0) {
            return "noDataTransfer";
        }
        try {
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                //从array中获取内容
                for (int i = 0; i < JsonArrayfromOrder.size(); i++) {
                    String sContent = JsonArrayfromOrder.getString(i);
                    System.out.println("把这条数据传到金蝶" + sContent);
                    //保存arry中的第i条数据到金蝶***********记得去Save里改一下
                    String transfermsg = InvokeHelper.Save(sFormId, sContent);
                    if (value.equals("PassBack")) {
                        //将返回的金蝶订单字段同步回销售易
                        String returnmsg = returnKingdeeSaleOrdeId(sContent, transfermsg);
                        if ("return failed!".equals(returnmsg)) {
                            return returnmsg;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "数据同步结束！";
    }

    //金蝶回传销售易销售订单
    private String returnKingdeeSaleOrdeId(String Content, String transfermsg) {

        //转换金蝶的回传数据为JSON结构
        JSONObject jsonObjectFromKingDeeOrder = JSON.parseObject(transfermsg);
        JSONObject jsonObjectFromKingDeeOrder2 = JSON.parseObject(Content);

        //取值
        String jsonObjectResult = jsonObjectFromKingDeeOrder.getString("Result");
        String jsonObjectResult2 = jsonObjectFromKingDeeOrder2.getString("Model");

        //赋值
        JSONObject jsonObjectID = JSON.parseObject(jsonObjectResult);
        JSONObject jsonObjectID2 = JSON.parseObject(jsonObjectResult2);
        String crmOrderId = jsonObjectID.get("Number").toString();
        String orderID = jsonObjectID2.get("F_YEE_CRMORDERID").toString();

        //获取生成的销售订单ID
        if (jsonObjectID.get("Number").toString() == null) {
            return "return failed!";
        }
        System.out.println("生成销售订单ID为：" + orderID);
        HttpClientToCRM tranfferCrmOrder = new HttpClientToCRM();
        tranfferCrmOrder.returnCrmOrder(crmOrderId, orderID);
        return "SUCCESS";
    }

    public JSONArray getRebateModule() {
        JSONArray jsonObjectList = new JSONArray();
        try {
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                // 业务对象Id
                String sFormId = "KD_REBATEMODULE";
                //设置同步金蝶内容的参数和字段
                String result = "";
                JSONObject jparm = new JSONObject();
                jparm.put("FormId", sFormId);
                jparm.put("FieldKeys", "F_YEE_FLLB,F_YEE_KHDMC,F_YEE_DBJE,F_YEE_FLD,F_YEE_FLJE");
                jparm.put("OrderString", "FNUMBER");
                //FModifyDate  >= {ts'2019-01-22 00:00:00'} and FModifyDate  <= {ts'2019-01-22 23:59:59'}
                //设置只同步当日的返利
                MyUntilClass myUntilClass = new MyUntilClass();
                String filterString = myUntilClass.returnfilterString();
                jparm.put("FilterString", filterString);
                jparm.put("TopRowCount", 0);
                jparm.put("Limit", "100000");

                //从金蝶获取内容
                result = InvokeHelper.ExecuteBillQuery(sFormId, jparm.toString());

                List<JSONArray> jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
                for (int i = 0; i < jsonArrayList.size(); i++) {

                    JSONObject obj = new JSONObject();
                    JSONArray jsonArray = jsonArrayList.get(i);
                    //返利类别
                    obj.put("customItem11__c", jsonArray.get(0));
                    //返利客户
                    String accountID = CrmOrderProcessService.getAccountId(jsonArray.get(1).toString());
                    obj.put("customItem8__c", accountID);
                    /*
                    //达标金额
                    obj.put("customItem3__c", jsonArray.get(2));
                    //返利点
                    double rebat = Double.parseDouble(jsonArray.get(3).toString());
                    double reba1 = rebat / 100;
                    obj.put("customItem10__c", reba1);
                     */
                    //返利金额
                    obj.put("customItem12__c", jsonArray.get(4));
                    jsonObjectList.add(obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObjectList;

    }


    public JSONArray getQuarterRebateCustomerId() {
        {
            {
                JSONArray jsonObjectList = new JSONArray();
                try {
                    if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                        // 业务对象Id
                        String sFormId = "KD_REBATEMODULE";
                        //设置同步金蝶内容的参数和字段
                        String result = "";
                        JSONObject jparm = new JSONObject();
                        jparm.put("FormId", sFormId);
                        jparm.put("FieldKeys", "F_YEE_FLLB,F_YEE_KHDMC,F_YEE_DBJE,F_YEE_FLD");
                        jparm.put("OrderString", "FNUMBER");
                        //年度返利
                        //jparm.put("FilterString", "F_YEE_FLLB='4'");
                        //季度返利
                        jparm.put("FilterString", " F_YEE_FLLB='5'");
                        jparm.put("TopRowCount", 0);
                        jparm.put("Limit", "100000");

                        //从金蝶获取内容
                        result = InvokeHelper.ExecuteBillQuery(sFormId, jparm.toString());

                        List<JSONArray> jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
                        for (int i = 0; i < jsonArrayList.size(); i++) {
                            JSONObject obj = new JSONObject();
                            JSONArray jsonArray = jsonArrayList.get(i);
                            //返利类别
                            obj.put("customItem11__c", jsonArray.get(0));
                            //返利客户
                            String accountID = CrmOrderProcessService.getAccountId(jsonArray.get(1).toString());
                            obj.put("customerId", jsonArray.get(1).toString());
                            obj.put("customItem8__c", accountID);
                            //达标金额
                            obj.put("customItem3__c", jsonArray.get(2));
                            //返利点
                            double rebat = Double.parseDouble(jsonArray.get(3).toString());
                            double reba1 = rebat / 100;
                            obj.put("customItem10__c", reba1);
                            jsonObjectList.add(obj);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return jsonObjectList;

            }
        }
    }

    public JSONArray getCheckedMaterialId() {
        {
            {
                JSONArray jsonArrayFromKd = new JSONArray();
                try {
                    if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                        // 业务对象Id
                        String sFormId = "BD_MATERIAL";
                        //设置同步金蝶内容的参数和字段
                        JSONObject jparm = new JSONObject();
                        jparm.put("FormId", sFormId);
                        jparm.put("FieldKeys", "FMATERIALID,FNumber,F_YEE_CGJGJCSL");
                        jparm.put("OrderString", "FMATERIALID");
                        //检查价格
                        jparm.put("FilterString", "F_YEE_CGJGJCSL<>'0'");
                        jparm.put("TopRowCount", 0);
                        jparm.put("Limit", "100000");

                        //从金蝶获取内容
                        String result = InvokeHelper.ExecuteBillQuery(sFormId, jparm.toString());

                        List<JSONArray> jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
                        for (int i = 0; i < jsonArrayList.size(); i++) {
                            JSONObject obj = new JSONObject();
                            JSONArray jsonArray = jsonArrayList.get(i);
                            //物料列表
                            obj.put("materialid", jsonArray.get(0));
                            obj.put("fnumber", jsonArray.get(1));
                            obj.put("checkQty", jsonArray.get(2));
                            jsonArrayFromKd.add(obj);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return jsonArrayFromKd;
            }
        }
    }

    public ArrayList checkMaterialPrice(JSONArray jsonArrayMaterialidFromKD) {

        //用户存储所有的物料编码和编号
        ArrayList arrayListMaterial = new ArrayList();
        for (int i = 0; i < jsonArrayMaterialidFromKD.size(); i++) {
            //用于存储每个物料的ID和编号
            HashMap<String, String> hashMap = new HashMap<>();
            String materialid = jsonArrayMaterialidFromKD.getJSONObject(i).getString("materialid");
            String materialFnumber = jsonArrayMaterialidFromKD.getJSONObject(i).getString("fnumber");
            double checkQty = jsonArrayMaterialidFromKD.getJSONObject(i).getDoubleValue("checkQty");
            //检查物料的采购累计下单量,返回需要更新采购订单的单号
            materialFnumber = checkMaterialQtyPrice(materialid, materialFnumber, checkQty);
            if (materialFnumber != null) {
                hashMap.put("materialid", materialid);
                hashMap.put("fnumber", materialFnumber);
                arrayListMaterial.add(hashMap);
            }
        }
        return arrayListMaterial;
    }

    private String checkMaterialQtyPrice(String materialid, String fnumber, double checkQty) {
        int accumulateQty = 0;
        JSONArray jsonArrayFromKd = new JSONArray();
        try {
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                // 业务对象Id
                String sFormId = "PUR_PurchaseOrder";
                //设置同步金蝶内容的参数和字段
                JSONObject jparm = new JSONObject();
                jparm.put("FormId", sFormId);
                jparm.put("FieldKeys", "FMaterialId,FQty,FPrice");
                jparm.put("OrderString", "FMaterialId");
                String filterString = "FMaterialId=" + materialid + "";
                //放置检查数量和产品ID
                jparm.put("FilterString", filterString);
                jparm.put("TopRowCount", 0);
                jparm.put("Limit", "100000");

                //从金蝶获取内容
                String result = InvokeHelper.ExecuteBillQuery(sFormId, jparm.toString());

                List<JSONArray> jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
                for (int i = 0; i < jsonArrayList.size(); i++) {
                    JSONArray jsonArray = jsonArrayList.get(i);
                    //物料列表
                    int fqty = jsonArray.getIntValue(1);
                    accumulateQty += fqty;
                }
            }

            if (accumulateQty > checkQty) {
                return fnumber;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateProceList() {
    }

    public String updatePriceList(ArrayList fnumberlist) {
        JSONArray jsonArraytoKingdeeList = new JSONArray();
        ArrayList arrayList = new ArrayList();
        KingDeeTransferService kingDeeTransferService = new KingDeeTransferService();
        if (fnumberlist == null) {
            return null;
        }
        for (int i = 0; i < fnumberlist.size(); i++) {
            HashMap hashMap = (HashMap) fnumberlist.get(i);
            //查询该物料的单号
            String materialFnumber = (String) hashMap.get("fnumber");
            String PriceCategoryFnumber = queryPriceCategoryFnumber(materialFnumber);
            if (PriceCategoryFnumber != null) {
                String materialID = (String) hashMap.get("materialid");
                arrayList.add(materialID);
            }
            //查询该采购价目表
            List<JSONArray> PriceCategoryContent = queryPriceCategory(PriceCategoryFnumber);
            //对采购价目表进行处理形成JSON
            JSONObject jsonObjectToKingdee = managePriceCategory(PriceCategoryContent);
            //加到同步集合
            if (jsonObjectToKingdee != null) {
                jsonArraytoKingdeeList.add(jsonObjectToKingdee);
            }
        }
        //调用金蝶保存方法
        String result = kingDeeTransferService.transferToKingdee(jsonArraytoKingdeeList, "PUR_PriceCategory", "PUR");
        //将更新后的物料做字段更新，防止重复检查
        JSONArray jsonArrayToKingdeeMaterial = kingDeeTransferService.updateMaterialParams(arrayList, result);
        //调用金蝶保存方法
        kingDeeTransferService.transferToKingdee(jsonArrayToKingdeeMaterial, "BD_MATERIAL", "BD_MATERIAL");

        return result;
    }

    private JSONObject managePriceCategory(List<JSONArray> priceCategoryContent) {
        if (priceCategoryContent == null) {
            return null;
        }
        JSONArray jsonArrayToKingdeeEntity = new JSONArray();
        JSONObject jsonObjectToKingdee = new JSONObject();
        JSONObject jsonObjectToKingdeeModel = new JSONObject();
        String id = null;

        String firstId;
        String secondId;
        for (int i = 0; i < priceCategoryContent.size(); i++) {
            JSONArray jsonArray = priceCategoryContent.get(i);
            //取从的数量
            double fromqty = jsonArray.getDoubleValue(2);
            //取ID
            id = jsonArray.getString(0);
            //找出需要进行禁用的一条数据
            if (fromqty == 0) {
                firstId = jsonArray.getString(1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("FEntryID", firstId);
                String FEntryEffectiveDate = jsonArray.getString(4);
                jsonObject.put("FEntryExpiryDate", FEntryEffectiveDate);
                jsonArrayToKingdeeEntity.add(jsonObject);
            }
            //找出需要进行更新数量的数据
            if (fromqty != 0) {
                secondId = jsonArray.getString(1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("FEntryID", secondId);
                jsonObject.put("FFROMQTY", 0);
                jsonObject.put("FToQty", 0);
                jsonArrayToKingdeeEntity.add(jsonObject);
            }
        }
        //形成保存SQL
        jsonObjectToKingdeeModel.put("FPriceListEntry", jsonArrayToKingdeeEntity);
        jsonObjectToKingdeeModel.put("FID", id);
        jsonObjectToKingdee.put("Model", jsonObjectToKingdeeModel);


        return jsonObjectToKingdee;

    }

    private List<JSONArray> queryPriceCategory(String PriceCategoryFnumber) {
        if (PriceCategoryFnumber == null) {
            return null;
        }
        String sFormId = "PUR_PriceCategory";
        String fieldKeys = "FID,FPriceListEntry_FEntryId,FFROMQTY ,FToQty,FEntryEffectiveDate";
        String OrderString = "";
        String filterString = "FID ='" + PriceCategoryFnumber + "'";

        List<JSONArray> jsonArray = getKingdeeResult(sFormId, fieldKeys, OrderString, filterString);
        return jsonArray;
    }

    private String queryPriceCategoryFnumber(String materialid) {
        String sFormId = "PUR_PriceCategory";
        String fieldKeys = "FID";
        String OrderString = "FID";
        String filterString = "F_YEE_WLBM ='" + materialid + "'";

        List<JSONArray> jsonArrayMaterild = getKingdeeResult(sFormId, fieldKeys, OrderString, filterString);
        if (jsonArrayMaterild.size() == 0) {
            return null;
        }
        JSONArray jsonArray = jsonArrayMaterild.get(0);
        String PriceCategoryFnumber = jsonArray.getString(0);
        return PriceCategoryFnumber;
    }

    private List<JSONArray> getKingdeeResult(String sFormId, String fieldKeys, String OrderString, String filterString) {
        String result = "";
        try {
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                //设置同步金蝶内容的参数和字段
                JSONObject jparm = new JSONObject();
                jparm.put("FormId", sFormId);
                jparm.put("FieldKeys", fieldKeys);
                jparm.put("OrderString", OrderString);
                jparm.put("FilterString", filterString);
                jparm.put("TopRowCount", 0);
                jparm.put("Limit", "100000");

                //从金蝶获取内容
                result = InvokeHelper.ExecuteBillQuery(sFormId, jparm.toString());
                List<JSONArray> jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
                return jsonArrayList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray updateMaterialParams(ArrayList fnumberArrayList, String result) {
        JSONArray jsonArraytoKingdeeList = new JSONArray();
        if (fnumberArrayList == null || result.equals("noDataTransfer")) {
            return jsonArraytoKingdeeList;
        }
        for (int i = 0; i < fnumberArrayList.size(); i++) {
            //查询该物料的单号
            String PriceCategoryFnumber = (String) fnumberArrayList.get(i);
            //对采购价目表进行处理形成JSON
            JSONObject jsonObjectToKingdee = manageMaterial(PriceCategoryFnumber);
            //加到同步集合
            jsonArraytoKingdeeList.add(jsonObjectToKingdee);
        }

        return jsonArraytoKingdeeList;
    }

    private JSONObject manageMaterial(String priceCategoryFnumber) {
        JSONObject jsonObjectMaterial = new JSONObject();
        JSONObject jsonObjectMaterialModel = new JSONObject();
        //jsonObjectMaterial.put("NeedUpDateFields", "['F_YEE_CGJGJCSL']");
        jsonObjectMaterialModel.put("FMATERIALID", priceCategoryFnumber);
        jsonObjectMaterialModel.put("F_YEE_CGJGJCSL", 0);
        jsonObjectMaterial.put("Model", jsonObjectMaterialModel);
        return jsonObjectMaterial;
    }
}


