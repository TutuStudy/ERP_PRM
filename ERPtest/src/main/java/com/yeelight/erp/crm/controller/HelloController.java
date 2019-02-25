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




}


