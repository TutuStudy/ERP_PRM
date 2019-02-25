package com.yeelight.erp.crm.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yeelight.erp.crm.constants.Constants;
import com.yeelight.erp.crm.service.CrmOrderProcessService;
import com.yeelight.erp.crm.service.KingDeeTransferService;
import com.yeelight.erp.crm.utils.InvokeHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RestController
public class HelloController {
    private static final String formid = Constants.FORMID;
    private static final String dbId = Constants.DBID;
    private static final String uid = Constants.UID;
    private static final String pwd = Constants.PWD;
    private static final int lang = Constants.LANG;

    @Resource
    KingDeeTransferService kingDeeTransferService;
    @Resource
    CrmOrderProcessService crmOrderProcessService;


    //@Scheduled(cron = "0 0 * * * ?" )
    @RequestMapping("/checkPurchaseOrderQty")
    public String checkPurchaseOrderQty(){
        //从物料列表查询需要检查数量的物料
        JSONArray jsonArrayMaterialidFromKD = kingDeeTransferService.getCheckedMaterialId();
        //对于超过数量的物料进行采购价格表编号的查询
        ArrayList arrayList=kingDeeTransferService.checkMaterialPrice(jsonArrayMaterialidFromKD);
        //对采购价格表进行禁用、更新处理,对更新后的物料进行更新操作防止重复检查
        String result = kingDeeTransferService.updatePriceList(arrayList);


        return result;
    }

    //@Scheduled(cron = "0 0 2 1 * 1,4,7,10 *" )
    @RequestMapping("/getQuarterRebateForm")
    public String getQuarterRebateForm() {

        //从金蝶获取季度返利中的客户
        JSONArray jsonArrayCustomerFromKD = kingDeeTransferService.getQuarterRebateCustomerId();
        //将每个客户的返利金额形成返利单同步到金蝶
        JSONArray jsonArrayRebateFormKD=crmOrderProcessService.getCrmProductRebateForm(jsonArrayCustomerFromKD);
        //将单据同步单金蝶系统
        return kingDeeTransferService.transferToKingdee(jsonArrayRebateFormKD, "KD_FLDBD","unPassBack");

    }

    //@Scheduled(cron = "0 0 2 1 * 1,4,7,10 *" )
    @RequestMapping("/getQuarterRebateModule")
    public String getQuarterRebateModule() {

        //从金蝶获取季度返利中的客户
        JSONArray jsonArrayCustomerFromKD = kingDeeTransferService.getQuarterRebateCustomerId();
        //针对每个客户去Crm中找订单金额并形成返利金额
        JSONArray jsonArrayRebateFromCrm = crmOrderProcessService.getCrmCustomerRebate(jsonArrayCustomerFromKD);
        //将返利金额更新到金蝶中
        return crmOrderProcessService.transferRebate2prm(jsonArrayRebateFromCrm);

    }


    @RequestMapping("/getRebateModule")
    public String getRebateModule() {

        //获取金蝶系统中的返利模块内容
        JSONArray jsonArray = kingDeeTransferService.getRebateModule();
        //将返利金额更新到销售易中
        return crmOrderProcessService.transferRebate2prm(jsonArray);
    }


    @Scheduled(cron = "0 0 * * * ?" )
    @RequestMapping("/getCrmOrder")
    public String getCrmOrder() {

        //同步采购订单
        JSONArray jsonArrayOrder = crmOrderProcessService.getCrmOrder("po");
        //同步金蝶系统形成订单并返回订单编号到销售易
        return kingDeeTransferService.transferToKingdee(jsonArrayOrder, "SAL_SaleOrder","PassBack");
    }

    @RequestMapping("/getCrmReturnOrder")
    public String getCrmReturnOrder() {

        //同步退货订单的销售订单
        JSONArray jsonArraySaleOrder = crmOrderProcessService.getCrmOrder("rop");
        //同步金蝶系统形成销售订单
        kingDeeTransferService.transferToKingdee(jsonArraySaleOrder, "SAL_SaleOrder","PassBack");

        //同步退货订单
        JSONArray jsonArrayReturnOrder = crmOrderProcessService.getCrmOrder("ro");
        //同步金蝶系统形成退换货订单
        return kingDeeTransferService.transferToKingdee(jsonArrayReturnOrder, "SAL_RETURNSTOCK","PassBack");
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello Spring Boot!";
    }

    @RequestMapping("/K3Cloud2TMS")
    public String K3Cloud2TMS() {
        //login kingdee
        try {
            if (login() == true) {
                query();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //2TMS
        return "success";
    }

    @RequestMapping("/login")
    public boolean login() {
        //  InvokeHelper.POST_K3CloudURL = "http://yeelight.ik3cloud.com/K3cloud/";
        try {
            //login kingdee
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping("/UserUpdate")
    public String UpdateUser() {
        return "successs";
    }

    @RequestMapping("/AutoUpdateUser")
    public String AutoUpdateUser() {
        InvokeHelper.POST_K3CloudURL = "http://yeelight.ik3cloud.com/K3cloud/";
        String s = "Login Log";

        try {
            //login kingdee
            if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
                for (int p = 0; p < 10000; p++) {
                    String queryResutl = userLincenseSave();
                    System.out.printf("Update " + p + " times success!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    @RequestMapping("/K3Cloud2MITMS")
    public String query() {
        String result = "";
        JSONObject jparm = new JSONObject();
        jparm.put("FormId", formid);
        jparm.put("FieldKeys", "FBillNo,FDate,FDate1,F_YEE_EMERGENCY,FCustomerID,FHEADDELIVERYWAY,F_YEE_PHONE,F_YEE_ATTEN,FRECEIVEADDRESS,F_YLK_PT,FEntity_FEntryID,FMaterialID,FMaterialName,FAuxpropID,FQty,FStockID");
        jparm.put("OrderString", "FBillNo");
        jparm.put("FilterString", "FStockID.F_YEE_SFGCC=1 and  FDocumentStatus='C'");
        jparm.put("TopRowCount", 0);
        jparm.put("Limit", "100000");

        try {
            result = InvokeHelper.ExecuteBillQuery(formid, jparm.toString());
        } catch (Exception e) {
            return e.toString();
        }

        List<JSONArray> jsonArrayList;
        JSONObject jsonObjectList = new JSONObject();

        jsonArrayList = JSONArray.parseArray(result, JSONArray.class);
        for (int i = 0; i < jsonArrayList.size(); i++) {

            JSONObject obj = new JSONObject();
            JSONArray jsonArray = jsonArrayList.get(i);
            try {
                obj.put("BIZ_CODE", "8");
                obj.put("FBillNo", jsonArray.get(0));
                obj.put("FDate", jsonArray.get(1).toString().substring(0, 10));
                obj.put("FDate1", jsonArray.get(2).toString().substring(0, 10));
                obj.put("F_YEE_EMERGENCY", jsonArray.get(3));
                obj.put("FCustomerID", jsonArray.get(4));
                obj.put("FHEADDELIVERYWAY", jsonArray.get(5));
                obj.put("F_YEE_PHONE", jsonArray.get(6));
                obj.put("F_YEE_ATTEN", jsonArray.get(7));
                obj.put("FRECEIVEADDRESS", jsonArray.get(8));
                obj.put("F_YLK_PT", jsonArray.get(9));
                obj.put("FEntity_FEntryID", jsonArray.get(10));
                obj.put("FMaterialID", jsonArray.get(11));
                obj.put("FMaterialName", jsonArray.get(12));
                obj.put("FAuxpropID", jsonArray.get(13));
                obj.put("FQty", jsonArray.get(14));
                obj.put("FStockID", jsonArray.get(15));
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(obj);
            try {
/*


                MiClient clientxiaomi = new DefaultMiClient.ClientBuilder("http://spopen.be.mi.com", "10198", "472aefc25c4f885f3398eb5d4c1876e4dad69c").builder();
                MiRequest request = new XiaomiBizInfoRequest(obj.toJSONString());
                //request.setFields("test");
                try {
                    XiaomiBizInfoResponse response = clientxiaomi.execute(request);
                    System.out.println("request cost : " + (response.getEndTimeStamp() - response.getStartTimeStamp()));
                    System.out.println(response.getValue());
                } catch (Exception e) {
                   log.error(e);
                }
 */

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        System.out.println(jsonObjectList);
        return result;
    }

    private String userLincenseSave() {

        //更新该用户一部为pro状态
        String content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":222534,\"FName\":\"贾文鹏\",\"FAppGroup\":\"Pro,All,BOS\",\"FIsLockTerminal\":false}}";
        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户一部为pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }
        //更新用户一部为非pro状态
        content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":222534,\"FName\":\"贾文鹏\",\"FAppGroup\":\"All,BOS\",\"FIsLockTerminal\":false}}";

        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户一部为非pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }
        //更新用户二部为pro状态
        content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":113265,\"FName\":\"kingdee\",\"FAppGroup\":\"Pro,All,BOS\",\"FIsLockTerminal\":false}}";

        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户二部为非pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }
        //更新用户二部为非pro状态
        content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":113265,\"FName\":\"kingdee\",\"FAppGroup\":\"All,BOS\",\"FIsLockTerminal\":false}}";

        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户二部为非pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }
        //更新用户三部为pro状态
        content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":147075,\"FName\":\"流程设计\",\"FAppGroup\":\"Pro,All,BOS\",\"FIsLockTerminal\":false}}";

        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户三部为非pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }
        //更新用户三部为非pro状态
        content = "{\"Creator\":\"\",\"NeedUpDateFields\":['FName','FAppGroup'],\"NeedReturnFields\":[],\"IsDeleteEntry\":\"True\",\"SubSystemId\":\"\",\"IsVerifyBaseDataField\":\"false\",\"IsEntryBatchFill\":\"True\",\"ValidateFlag\":\"True\",\"NumberSearch\":\"True\",\"InterationFlags\":\"\",\"IsAutoSubmitAndAudit\":\"false\",\"Model\":{\"FUserID\":147075,\"FName\":\"流程设计\",\"FAppGroup\":\"All,BOS\",\"FIsLockTerminal\":false}}";

        try {
            InvokeHelper.Save(formid, content);
            System.out.println("更新该用户三部为非pro状态已完成");
        } catch (Exception e) {
            return e.toString();
        }

/*
        JSONObject jsonObject = JSONObject.parseObject(content);
        JSONObject jparm = new JSONObject();
        jparm.put("formid", formid);
        jparm.put("Creator",jsonObject.toJSONString());

 */
        return "Update Success!";
    }


}


