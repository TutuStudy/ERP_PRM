package com.yeelight.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yeelight.erp.crm.utils.HttpClientToCRM;
import com.yeelight.erp.crm.utils.InvokeHelper;
import com.yeelight.erp.crm.utils.MyUntilClass;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class CrmOrderProcessService {

    String url = "https://api.xiaoshouyi.com/data/v1/query";
    String name = "q";
    HttpClientToCRM conn = new HttpClientToCRM();
    MyUntilClass myUntilClass = new MyUntilClass();


    //获取CRM中客户的ID
    public static String getAccountId(String accountName) {
        HttpClientToCRM conn = new HttpClientToCRM();
        String url = "https://api.xiaoshouyi.com/data/v1/query";
        String name = "q";
        String value = "select id from account where dbcVarchar1='" + accountName + "'";

        //从CRM获取订单
        // ID集合
        String accountStr = conn.getCrmContext(url, name, value);
        JSONObject jsonObject = JSON.parseObject(accountStr);
        return jsonObject.getJSONArray("records").getJSONObject(0).getString("id");

    }

    private JSONObject transferKDSaleOrderJson(String str, String value) {

        JSONObject billTyped = new JSONObject();
        JSONObject FSaleOrgId = new JSONObject();
        JSONObject FCustId = new JSONObject();
        JSONObject FSaleDeptId = new JSONObject();
        JSONObject F_YLK_PT = new JSONObject();
        JSONObject FSalerId = new JSONObject();
        JSONObject FverifyId = new JSONObject();
        String date;
        String ifReceipt;


        //转换CRM的订单为JSON结构
        JSONObject jsonObjectFromCrmOrder = JSON.parseObject(str);
        //转换CRM的订单的JSONArray
        JSONArray jsonObjectEntityCrm = jsonObjectFromCrmOrder.getJSONArray("products");
        //设置传到金蝶中的JSON
        JSONObject jsonObjectTOKingdee = new JSONObject();
        //设置金蝶model内容的JSON
        JSONObject jsonObjectModelKingdee = new JSONObject(true);
        //设置金蝶Entity内容的JSON
        JSONArray jsonObjectEntityKingdee = new JSONArray();

        billTyped.put("FNUMBER", "XSDD01_SYS");
        FSaleOrgId.put("FNUMBER", "100");
        FCustId.put("FNUMBER", jsonObjectFromCrmOrder.getString("dbcJoin1"));
        FSaleDeptId.put("FNUMBER", "0702");
        F_YLK_PT.put("FNUMBER", "109");
        FSaleOrgId.put("FNUMBER", "100");
        FverifyId.put("FUSERACCOUNT","李孟洁");

        //对销售员做处理
        int salerId = jsonObjectFromCrmOrder.getIntValue("dbcSelect3");
        if (salerId == 1) {
            FSalerId.put("FNUMBER", "07020001");
        }
        if (salerId == 2) {
            FSalerId.put("FNUMBER", "2018070202");
        } else {
            FSalerId.put("FNUMBER", "07020001");
        }

        date = jsonObjectFromCrmOrder.get("effectiveDate").toString();

        JSONObject jsonObject1 = (JSONObject) new JSONObject().put("FNUMBER", "XSDD01_SYS");
        //设置订单的基本信息
        jsonObjectModelKingdee.put("FID", 0);
        //设置订单类型
        jsonObjectModelKingdee.put("FBillTypeID", billTyped);
        //设置时间
        jsonObjectModelKingdee.put("FDate", date);
        //设置组织机构为青岛亿联客
        jsonObjectModelKingdee.put("FSaleOrgId", FSaleOrgId);
        //设置客户
        jsonObjectModelKingdee.put("FCustId", FCustId);
        //设置销售员
        jsonObjectModelKingdee.put("FSalerId", FSalerId);
        //设置销售易审批人
        jsonObjectModelKingdee.put("F_YEE_XSYSPR", FverifyId);

        //设置部门ID为线下销售
        jsonObjectModelKingdee.put("FSaleDeptId", FSaleDeptId);
        //设置销售易订单的id
        jsonObjectModelKingdee.put("F_YEE_CRMORDERID", jsonObjectFromCrmOrder.get("id").toString());
        jsonObjectModelKingdee.put("FISINIT", false);
        jsonObjectModelKingdee.put("FIsMobile", false);
        //设置备注+原ERP订单
        if (value.equals("rop")) {
            String ERPOrderId = jsonObjectFromCrmOrder.get("dbcVarchar1").toString();
            jsonObjectModelKingdee.put("FNote", jsonObjectFromCrmOrder.get("comment").toString() + " Return Order From ERP:" + ERPOrderId);
        } else {
            jsonObjectModelKingdee.put("FNote", jsonObjectFromCrmOrder.get("comment").toString());
        }
        //设置平台
        jsonObjectModelKingdee.put("F_YLK_PT", F_YLK_PT);
        //设置是否出口退税
        jsonObjectModelKingdee.put("F_KD_SFCKTS", "1");
        //设置产品类型
        jsonObjectModelKingdee.put("F_KD_XSCPLX", "产成品");
        //设置开票类型
        ifReceipt = getReceiptType(jsonObjectFromCrmOrder.get("dbcSelect2").toString());
        jsonObjectModelKingdee.put("F_KD_KPLX", ifReceipt);
        //设置调拨类型
        jsonObjectModelKingdee.put("F_YEE_SFDB", 0);
        //设置订单的合计数量
        jsonObjectModelKingdee.put("F_YEE_DDSLHJ", jsonObjectFromCrmOrder.get("productsAmount"));
        //设置售后地址放在最后是避免客户更新上后清空地址
        jsonObjectModelKingdee.put("FReceiveAddress", jsonObjectFromCrmOrder.get("dbcVarchar2").toString() + jsonObjectFromCrmOrder.get("dbcVarchar3").toString() + jsonObjectFromCrmOrder.get("dbcVarchar4").toString());

        //设置订单明细
        for (int i = 0; i < jsonObjectEntityCrm.size(); i++) {
            JSONObject jsonObjectSalerOrderEntityKingdee = new JSONObject();
            //设置订单的基础信息
            jsonObjectSalerOrderEntityKingdee.put("FRowType", "Standard");
            //设置交货日期
            jsonObjectSalerOrderEntityKingdee.put("FDeliveryDate", date);
            //设置物料信息
            JSONObject FMaterialId = new JSONObject();
            FMaterialId.put("FNumber", jsonObjectEntityCrm.getJSONObject(i).get("dbcJoin1"));
            jsonObjectSalerOrderEntityKingdee.put("FMaterialId", FMaterialId);
            //设置销售数量
            double qty = jsonObjectEntityCrm.getJSONObject(i).getDoubleValue("quantity");
            jsonObjectSalerOrderEntityKingdee.put("FQty", qty);
            if (value.equals("po")) {
                //设置抵扣返利金额价格
                double deductionPrice = jsonObjectEntityCrm.getJSONObject(i).getDoubleValue("dbcReal2");
                //设置抵扣的返利金额价格
                jsonObjectSalerOrderEntityKingdee.put("F_YEE_SYFLJE", deductionPrice * qty);
            }
            //设置含税单价
            double unitPrice = jsonObjectEntityCrm.getJSONObject(i).getDoubleValue("unitPrice");
            //设置含税单价
            jsonObjectSalerOrderEntityKingdee.put("FTaxPrice", unitPrice);

            jsonObjectEntityKingdee.add(jsonObjectSalerOrderEntityKingdee);
        }

        jsonObjectModelKingdee.put("FSaleOrderEntry", jsonObjectEntityKingdee);
        System.out.println("要传到金蝶model的数据:");
        System.out.println(jsonObjectModelKingdee);

        jsonObjectTOKingdee.put("Model", jsonObjectModelKingdee);
        jsonObjectTOKingdee.put("IsAutoSubmitAndAudit", "false");
        System.out.println("要传到金蝶JSON的数据:");
        System.out.println(jsonObjectTOKingdee);

        return jsonObjectTOKingdee;
    }

    private String getReceiptType(String dbcSelect2) {
        String ifReceipt = "1";
        if (dbcSelect2.equals("1")) {
            ifReceipt = "专票";
            return ifReceipt;
        }
        if (dbcSelect2.equals("2")) {
            ifReceipt = "普票";
            return ifReceipt;

        }
        if (dbcSelect2.equals("3")) {
            ifReceipt = "不开票";
            return ifReceipt;

        }
        return ifReceipt;
    }

    //获取订单内容
    private String getJsonObject(String crmOrderid) {
        String url = "https://api.xiaoshouyi.com/data/v1/objects/order/info";
        String name = "id";
        String crmOrder;
        HttpClientToCRM conn = new HttpClientToCRM();
        crmOrder = conn.getCrmContext(url, name, crmOrderid);
        //返回订单String类型数据
        return crmOrder;
    }

    //获取CrmOrder的ID集合
    private String getCrmOrderid(String value) {
        HttpClientToCRM conn = new HttpClientToCRM();
        String url = "https://api.xiaoshouyi.com/data/v1/query";
        String name = "q";

        //从CRM获取订单
        // ID集合
        return conn.getCrmContext(url, name, value);
    }

    @Deprecated
    private void transferKD(JSONObject crmOrderJson) {
        InvokeHelper.POST_K3CloudURL = "http://yeelight.ik3cloud.com/K3cloud/";
        String dbId = "20170913104009";
        String uid = "administrator";
        String pwd = "yeelight.2017";

        int lang = 2052;
        String s = "Login Log";
    }

    //获取CRM的订单数据
    public JSONArray getCrmOrder(String value) {

        //根据传过来的参数判断获取的是采购订单还是退货订单
        MyUntilClass myUntilClass = new MyUntilClass();
        String sql = myUntilClass.returnsql(value);
        //获取当天订单ID的集合
        String crmOrderStringList = getCrmOrderid(sql);
        //通过ID的集合获取订单內容并转换成JSON并同步
        return transferID2Json(crmOrderStringList, value);

    }


    private JSONArray transferID2Json(String crmOrderStringList, String value) {
        JSONObject crmOrderJson;
        JSONArray jsonArray = new JSONArray();

        //通过ID获取订单并转换成JSON并同步
        if (crmOrderStringList != null) {
            //将传来的StringORder转换为JSON结构
            JSONObject jsonObjFromCrmOrder = JSON.parseObject(crmOrderStringList);
            //获取JSON中的Array的records内容
            JSONArray orderArray = jsonObjFromCrmOrder.getJSONArray("records");
            for (int i = 0; i < orderArray.size(); i++) {
                //获取单个订单的id
                Integer orderId = (Integer) orderArray.getJSONObject(i).get("id");
                //("第:{}次获取订单，id为:{}", i, orderId.toString());
                String crmOrderString = getJsonObject(orderId.toString());
                //转换CRM订单的内容为金蝶的JSON
                //判断传过来的参数是要什么JSON数据
                if (value.equals("po")) {
                    //如果为销售订单则
                    crmOrderJson = transferKDSaleOrderJson(crmOrderString, value);
                    jsonArray.add(crmOrderJson);
                }
                if (value.equals("rop")) {
                    //如果为退货的销售订单则
                    crmOrderJson = transferKDSaleOrderJson(crmOrderString, value);
                    jsonArray.add(crmOrderJson);
                }
                if (value.equals("ro")) {
                    //为退货订单则
                    crmOrderJson = transferKDReturnOrderJson(crmOrderString);
                    //将json加入jsonArray
                    jsonArray.add(crmOrderJson);
                }
            }
        }
        return jsonArray;
    }

    private JSONObject transferKDReturnOrderJson(String crmOrderString) {

        JSONObject billTyped = new JSONObject();
        JSONObject FSaleOrgId = new JSONObject();
        JSONObject FStockOrgId = new JSONObject();
        JSONObject FCustId = new JSONObject();
        JSONObject FSaleDeptId = new JSONObject();
        JSONObject F_YLK_PT = new JSONObject();
        JSONObject FSalerId = new JSONObject();

        String date;
        String ifReceipt;
        float rebatAmount;
        float rebatAmount1;


        //转换CRM的订单为JSON结构
        JSONObject jsonObjectFromCrmOrder = JSON.parseObject(crmOrderString);
        //转换CRM的订单的JSONArray
        JSONArray jsonObjectEntityCrm = jsonObjectFromCrmOrder.getJSONArray("products");
        //设置传到金蝶中的JSON
        JSONObject jsonObjectTOKingdee = new JSONObject();
        //设置金蝶model内容的JSON
        JSONObject jsonObjectModelKingdee = new JSONObject(true);
        //设置金蝶Entity内容的JSON
        JSONArray jsonObjectEntityKingdee = new JSONArray();

        billTyped.put("FNUMBER", "XSTHD01_SYS");
        FSaleOrgId.put("FNUMBER", "100");
        FCustId.put("FNUMBER", jsonObjectFromCrmOrder.getString("dbcJoin1"));
        FSaleDeptId.put("FNUMBER", "0702");
        F_YLK_PT.put("FNUMBER", "109");
        FSaleOrgId.put("FNUMBER", "100");
        FStockOrgId.put("FNUMBER", "100");
        FSalerId.put("FNUMBER", "07020001");

        date = jsonObjectFromCrmOrder.get("effectiveDate").toString();

        //设置订单的基本信息
        jsonObjectModelKingdee.put("FID", 0);
        //设置订单类型
        jsonObjectModelKingdee.put("FBillTypeID", billTyped);
        //设置时间
        jsonObjectModelKingdee.put("FDate", date);
        //设置组织机构为青岛亿联客
        jsonObjectModelKingdee.put("FSaleOrgId", FSaleOrgId);
        jsonObjectModelKingdee.put("FStockOrgId", FStockOrgId);
        //设置客户
        jsonObjectModelKingdee.put("FRetcustId", FCustId);

        //设置部门ID为线下销售
        jsonObjectModelKingdee.put("FSaledeptid", FSaleDeptId);
        //设置销售易订单的id
        jsonObjectModelKingdee.put("F_YEE_CRMORDERID", jsonObjectFromCrmOrder.get("id").toString());
        jsonObjectModelKingdee.put("FISINIT", false);
        jsonObjectModelKingdee.put("FIsMobile", false);
        //设置平台
        jsonObjectModelKingdee.put("F_YLK_PT", F_YLK_PT);
        //设置产品类型
        jsonObjectModelKingdee.put("F_KD_XSCPLX", "产成品");
        //设置开票类型
        ifReceipt = getReceiptType(jsonObjectFromCrmOrder.get("dbcSelect2").toString());
        jsonObjectModelKingdee.put("F_KD_KPLX", ifReceipt);

        //设置订单明细
        for (int i = 0; i < jsonObjectEntityCrm.size(); i++) {
            JSONObject jsonObjectSalerOrderEntityKingdee = new JSONObject();
            //设置订单的基础信息
            jsonObjectSalerOrderEntityKingdee.put("FRowType", "Standard");
            //设置退货日期
            jsonObjectSalerOrderEntityKingdee.put("FDeliveryDate", date);
            //设置物料信息
            JSONObject FMaterialId = new JSONObject();
            FMaterialId.put("FNumber", jsonObjectEntityCrm.getJSONObject(i).get("dbcJoin1"));
            jsonObjectSalerOrderEntityKingdee.put("FMaterialId", FMaterialId);
            //设置仓库
            JSONObject FStockId = new JSONObject();
            FStockId.put("FNumber", "300102");
            jsonObjectSalerOrderEntityKingdee.put("FStockId", FStockId);
            //设置销售数量
            jsonObjectSalerOrderEntityKingdee.put("FRealQty", jsonObjectEntityCrm.getJSONObject(i).get("quantity"));
            //设置含税价格
            //产品金额总计/订单总金额*产品的含税价格
            //    float FTaxPrice=getFTaxPrice(rebatAmount1,);
            jsonObjectSalerOrderEntityKingdee.put("FTaxPrice", jsonObjectEntityCrm.getJSONObject(i).get("unitPrice"));
            jsonObjectEntityKingdee.add(jsonObjectSalerOrderEntityKingdee);
        }

        jsonObjectModelKingdee.put("FEntity", jsonObjectEntityKingdee);
        System.out.println("要传到金蝶model的数据:");
        System.out.println(jsonObjectModelKingdee);

        jsonObjectTOKingdee.put("Model", jsonObjectModelKingdee);
        jsonObjectTOKingdee.put("IsAutoSubmitAndAudit", "false");
        System.out.println("要传到金蝶JSON的数据:");
        System.out.println(jsonObjectTOKingdee);

        return jsonObjectTOKingdee;

    }

    public String transferRebate2prm(JSONArray jsonArray) {
        if (jsonArray.size() == 0) {
            return "同步返利失败：无数据！";
        } else {
            HttpClientToCRM httpClientToCRM = new HttpClientToCRM();
            httpClientToCRM.returnCrmRebate(jsonArray);
            return "同步返利价格成功";
        }
    }


    public JSONArray getCrmCustomerRebate(JSONArray jsonArrayCustomerFromKD) {
        JSONArray jsonArrayRebateFromCrm = new JSONArray();
        String type = "rebate";
        //开始对客户循环检查返利并创建返利明细
        return jsonArrayRebateFromCrm = checkCustomerRebate(jsonArrayCustomerFromKD, type);
    }

    private JSONObject returnCrmRebatetoKingdee(double rebateAmount, String customerID, String rebateId, String quarterTime) {

        JSONObject obj = new JSONObject();
        //返利类别
        obj.put("customItem11__c", rebateId);
        //返利客户
        obj.put("customItem8__c", customerID);
        //返利金额
        obj.put("customItem12__c", rebateAmount);
        //季度时间
        obj.put("customItem14__c", quarterTime);
        return obj;
    }


    public JSONArray getCrmProductRebateForm(JSONArray jsonArrayCustomerFromKD) {
        JSONArray jsonArrayRebateFromCrm = new JSONArray();
        String type = "form";
        //开始对客户循环检查返利
        return jsonArrayRebateFromCrm = checkCustomerRebate(jsonArrayCustomerFromKD, type);
    }

    private JSONArray checkCustomerRebate(JSONArray jsonArrayCustomerFromKD, String type) {
        HttpClientToCRM conn = new HttpClientToCRM();
        JSONArray jsonArrayRebateFromCrm = new JSONArray();
        //获取上一季度的时间
        MyUntilClass myUntilClass = new MyUntilClass();
        long startQuarterTime = myUntilClass.returnEndQuaterTime();
        long endQuarterTime = myUntilClass.returnStartQuaterTime();
        String quarterTime = myUntilClass.returnQuaterTime();
        //开始对客户循环检查返利
        for (int i = 0; i < jsonArrayCustomerFromKD.size(); i++) {
            float orderAmount = 0;
            float deductionAmount = 0;
            //返利类别
            String rebateId = jsonArrayCustomerFromKD.getJSONObject(i).getString("customItem11__c");
            //返利客户
            String customerID = jsonArrayCustomerFromKD.getJSONObject(i).getString("customItem8__c");
            //金蝶客户customerId
            String customerFromKd = jsonArrayCustomerFromKD.getJSONObject(i).getString("customerId");
            //达标金额
            double reachAmount = jsonArrayCustomerFromKD.getJSONObject(i).getFloatValue("customItem3__c");
            //达标点
            double reba1 = jsonArrayCustomerFromKD.getJSONObject(i).getFloatValue("customItem10__c");
            double rebateAmount;
            double ifAmount;
            //获取返利金额和总金额
            //String value = "select  id,po,dbcJoin1,amount,dbcReal1,accountId  from  _order where createdAt>'" + startQuarterTime + "' and createdAt<'" + endQuarterTime + "' and accountId ='" + customerID + "'";
            String value = "select  id,po,dbcJoin1,amount,dbcReal1,accountId  from  _order where accountId ='" + customerID + "'";
            String accountStr = conn.getCrmContext(url, name, value);
            JSONObject jsonObject = JSON.parseObject(accountStr);
            JSONArray jsonArray1 = jsonObject.getJSONArray("records");
            for (int j = 0; j < jsonArray1.size(); j++) {
                orderAmount += jsonArray1.getJSONObject(j).getFloatValue("amount");
                deductionAmount += jsonArray1.getJSONObject(j).getFloatValue("dbcReal1");
            }
            //计算达标金额=达标金额-订单金额+ 折扣金额
            ifAmount = reachAmount - orderAmount + deductionAmount;
            //返利金额
            rebateAmount = (orderAmount - deductionAmount) * reba1;
            //对达标金额进行判断
            if (ifAmount >= 0) {
                continue;
            }
            if (ifAmount < 0) {
                if (type.equals("form")) {
                    //根据返利总金额计算每个单品的返利金额（返利单）
                    JSONObject obj = returnCrmProductRebatetoKingdee(rebateAmount, orderAmount, deductionAmount, customerID, customerFromKd);
                    jsonArrayRebateFromCrm.add(obj);
                } else if (type.equals("rebate")) {
                    //返回数据并对更新Crm的返利金额
                    JSONObject obj = returnCrmRebatetoKingdee(rebateAmount, customerID, rebateId, quarterTime);
                    jsonArrayRebateFromCrm.add(obj);
                }
            }
        }
        return jsonArrayRebateFromCrm;
    }

    private JSONObject returnCrmProductRebatetoKingdee(double rebateAmount, float orderAmount, float deductionAmount, String customerID, String customerfromKD) {
        //设置传到金蝶中的JSON
        JSONObject jsonObjectTOKingdee = new JSONObject();
        //设置金蝶model内容的JSON
        JSONObject jsonObjectModelKingdee = new JSONObject(true);


        //组织机构
        JSONObject orgJsonobj = new JSONObject();
        orgJsonobj.put("FNumber", "100");
        //客户
        JSONObject cusJsonobj = new JSONObject();
        cusJsonobj.put("FNUMBER", customerfromKD);
        //产品的值，获取产品明细
        JSONArray productJsonArray = new JSONArray();
        productJsonArray = returnProductJsonArray(rebateAmount, orderAmount, deductionAmount, customerID);
        //放值
        jsonObjectModelKingdee.put("FID", 0);
        jsonObjectModelKingdee.put("F_KD_YEAR", "2019");
        jsonObjectModelKingdee.put("F_KD_MONTH", "1");
        jsonObjectModelKingdee.put("F_KD_ORGID", orgJsonobj);
        jsonObjectModelKingdee.put("F_YEE_KH", cusJsonobj);
        jsonObjectModelKingdee.put("FEntity", productJsonArray);

        jsonObjectTOKingdee.put("Model", jsonObjectModelKingdee);
        jsonObjectTOKingdee.put("IsAutoSubmitAndAudit", "false");

        return jsonObjectTOKingdee;
    }

    private JSONArray returnProductJsonArray(double rebateAmount, float orderAmount, float deductionAmount, String customerID) {
        HttpClientToCRM conn = new HttpClientToCRM();
        JSONArray jsonArrayFromProduct = new JSONArray();
        JSONArray jsonArrayFromProductEntity = new JSONArray();
        //分页方式获取数据
        ArrayList arrayListProductEntity = getArrayListProductEntity(customerID);
        for (int i = 0; i < arrayListProductEntity.size(); i++) {
            jsonArrayFromProductEntity = (JSONArray) arrayListProductEntity.get(i);
            for (int j = 0; j < jsonArrayFromProductEntity.size(); j++) {
                JSONObject jsonObject = new JSONObject();
                //设置物料编号
                JSONObject materialObj = new JSONObject();
                String material = jsonArrayFromProductEntity.getJSONObject(j).getString("dbcJoin1");
                materialObj.put("FNUMBER", material);
                jsonObject.put("F_KD_WL", materialObj);
                //设置平台编号
                JSONObject platformJsonobj = new JSONObject();
                platformJsonobj.put("FNUMBER", "109");
                jsonObject.put("F_KD_PT", platformJsonobj);
                //设置金额
                //公式：单个记录总金额(不包括折扣金额)/所有订单金额(总金额-返利金额)*返利总金额
                double priceTotal = jsonArrayFromProductEntity.getJSONObject(j).getDoubleValue("priceTotal");
                float orderAllAmount = orderAmount - deductionAmount;
                double price = priceTotal / orderAllAmount * rebateAmount;
                jsonObject.put("F_KD_JE", price);

                jsonArrayFromProduct.add(jsonObject);
            }
        }
        return jsonArrayFromProduct;
    }

    private ArrayList getArrayListProductEntity(String customerID) {
        JSONObject jsonObjectProduct = new JSONObject();
        //通过客户ID，获取源订单ID，返回一个jsonArrayOrderId
        String executeSQL = "select  id  from  _order where  accountId ='" + customerID + "'";
        JSONArray jsonArrayOrderId = conn.getCrmContextJSONArray(url, name, executeSQL);
        //通过源订单ID分页查询获取订单明细
        ArrayList arrayListProduct = getCrmOrderProductEntity(jsonArrayOrderId);
        //arrayListProduct

        return arrayListProduct;
    }

    private ArrayList getCrmOrderProductEntity(JSONArray jsonArrayOrderId) {
        ArrayList arrayListProductEntity = new ArrayList();
        HttpClientToCRM conn = new HttpClientToCRM();
        for (int i = 0; i < jsonArrayOrderId.size(); i++) {
            String OrderId = jsonArrayOrderId.getJSONObject(i).getString("id");
            String offSetSQL = "select  id  from  orderProduct where  orderid ='" + OrderId + "'";
            //计算分页offset
            int offset = myUntilClass.getPaginationOffSet(offSetSQL);
            int count = 0;
            for (int j = 0; j < offset; j++) {
                //抓取所有订单明细
                String executeSQL = "select  id,unitPrice,quantity,discount,priceTotal,dbcJoin1,dbcReal1,dbcReal2   from  orderProduct  where  orderid ='" + OrderId + "' order by id  limit " + count + ",300 ";
                JSONArray jsonArrayOrderProductEntity = conn.getCrmContextJSONArray(url, name, executeSQL);
                count = j * 300 + 1;
                arrayListProductEntity.add(jsonArrayOrderProductEntity);
            }
        }
        return arrayListProductEntity;
    }


}



