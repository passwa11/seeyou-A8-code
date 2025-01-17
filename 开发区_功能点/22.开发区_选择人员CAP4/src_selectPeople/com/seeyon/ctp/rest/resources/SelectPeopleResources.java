package com.seeyon.ctp.rest.resources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.vo.CacheListData;
import com.seeyon.apps.customFieldCtrl.vo.ZJsonObject;
import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.exceptions.BusinessException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * 周刘成   2019/5/16
 */
@Path("cap4/selectPeople")
@Consumes({"application/json"})
@Produces({"application/json"})
public class SelectPeopleResources extends BaseResource {

    private Logger logger = LoggerFactory.getLogger(SelectPeopleResources.class);

    private static CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

    private static CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");

    private static Random rand = new Random();

    private CacheAccessable factory = CacheFactory.getInstance(SelectPeopleResources.class);
    private CacheMap<String, CacheListData> cacheMap;

    public SelectPeopleResources() {
        cacheMap = factory.createMap("people");
    }

    @POST
    @Produces({"application/json"})
    @Path("backfillpeopleInfo")
    public Response backfillpeopleInfo2(Map<String, Object> postMap) throws BusinessException {
        //fastjson 解析json字符串
        String masterId = (String) postMap.get("masterId");
        String dataInfo = (String) postMap.get("dataInfo");
        String isNext = (String) postMap.get("flag");
        JSONObject jsonObject = JSON.parseObject(dataInfo);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ZJsonObject> listJson = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<List<ZJsonObject>>() {
        });

        //formBean是主表的信息
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
        Map<String, Object> result = new HashMap<String, Object>();
        if (null == cacheFormData) {
            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }
        try {
            //明细行信息，使用cap4提供
            List<FormDataSubBean> subBeans = CAP4FormKit.getSubBeans(cacheFormData);

            //过滤明细行，排除有数据的明细行
            List<FormDataSubBean> excludeExist = new ArrayList<>();

//            List<ZJsonObject> zJsonObjectList = new ArrayList<>();
            for (int i = 0; i < subBeans.size(); i++) {
                Map<String, Object> map = subBeans.get(i).getRowData();
                for (String key : map.keySet()) {
                    if (key.startsWith("field")) {
                        Object fieldVal = map.get(key);
                        if (null == fieldVal) {
                            excludeExist.add(subBeans.get(i));
                            result.put("recordId", subBeans.get(i).getId()+"");
                            break;
                        } else {
                            String val = fieldVal + "";
                            for (int j = 0; j < listJson.size(); j++) {
                                ZJsonObject zj = listJson.get(j);
                                if (val.equals(zj.getField0001())) {
                                    listJson.remove(j);
                                }
                            }
                            break;
                        }
                    }
                }
            }

            String tableName = subBeans.get(0).getFormTable().getTableName();
            result.put("tableName", tableName);
            int toInt = Integer.parseInt(isNext);
            if (toInt <= 0) {
                result.put("add", false);
            } else {
                result.put("add", true);
            }

            result.put("dataCount", listJson.size());
            Map<String, Object> filldatas = null;

            List<List<Map<String, Object>>> listMap = new ArrayList<>();
            for (int i = 0; i < listJson.size(); i++) {
                Map<String, Object> masterMap = excludeExist.get(0).getRowData();
                ZJsonObject zJsonObject = null;
                if (i < listJson.size()) {
                    zJsonObject = listJson.get(i);
                    Map<String, Object> subTemp1 = new HashMap<>();

                    subTemp1.put("value", zJsonObject.getField0001());
                    subTemp1.put("display", zJsonObject.getField0001());

                    Map<String, Object> subTemp2 = new HashMap<>();
                    subTemp2.put("display", zJsonObject.getName());
                    subTemp2.put("value", zJsonObject.getField0002());

                    Map<String, Object> subTemp3 = new HashMap<>();
                    subTemp3.put("display", zJsonObject.getName());
                    subTemp3.put("value", zJsonObject.getName());

                    Map<String, Object> subTemp4 = new HashMap<>();
                    subTemp4.put("display", zJsonObject.getField0003());
                    subTemp4.put("value", zJsonObject.getField0003());

                    Map<String, Object> subTemp5 = new HashMap<>();
                    subTemp5.put("display", zJsonObject.getField0005());
                    subTemp5.put("value", zJsonObject.getField0005());

//                    Map<String, Object> subTemp6 = new HashMap<>();
//                    subTemp6.put("showValue", zJsonObject.getMval());
//                    subTemp6.put("showValue2", zJsonObject.getMval());
//                    subTemp6.put("value", zJsonObject.getMval());

                    Map<String, Object> subTemp7 = new HashMap<>();
                    subTemp7.put("display", zJsonObject.getField0007());
                    subTemp7.put("value", zJsonObject.getField0007());
                    int count = 1;

                    List<Map<String, Object>> list = new ArrayList<>();
                    for (String key : masterMap.keySet()) {
                        if (key.startsWith("field")) {
                            Object fieldVal = masterMap.get(key);
                            if (null == fieldVal) {
                                filldatas = new HashMap<>();

                                if (count == 1) {
                                    filldatas.put("tableName", tableName);
                                    filldatas.put("fieldId", key);
                                    filldatas.put("fieldData", subTemp1);
                                    list.add(filldatas);
                                }
                                if (count == 2) {
                                    filldatas.put("tableName", tableName);
                                    filldatas.put("fieldId", key);
                                    filldatas.put("fieldData", subTemp2);
                                    list.add(filldatas);
                                }

                                if (count == 3) {
                                    filldatas.put("tableName", tableName);
                                    filldatas.put("fieldId", key);
                                    filldatas.put("fieldData", subTemp4);
                                    list.add(filldatas);
                                }
                                if (count == 4) {
                                    filldatas.put("tableName", tableName);
                                    filldatas.put("fieldId", key);
                                    filldatas.put("fieldData", subTemp5);
                                    list.add(filldatas);
                                }
                                if (count == 5) {
                                    filldatas.put("tableName", tableName);
                                    filldatas.put("fieldId", key);
                                    filldatas.put("fieldData", subTemp7);
                                    list.add(filldatas);
                                }

                                count++;
                            }
                        }
                    }
                    listMap.add(list);
                }
            }

            result.put("data", listMap);

        } catch (Exception e) {
            System.out.println("选择人员回填数据报错了：" + e.getMessage());
            logger.error("选择人员回填数据报错了：" + e.getMessage());
        }

        return success(result);
    }

    @POST
    @Produces({"application/json"})
    @Path("clearCachePerple")
    public Response clearCachePerple() {
        cacheMap.remove("listKey");
        Map<String, Object> result = new HashMap<>();
        result.put("code", "0");
        return success(result);
    }

    //    修改原先回填数据的逻辑（因为回填数据加载过慢）
    @POST
    @Produces({"application/json"})
    @Path("backfillpeopleInfoOtimization")
    public Response backfillpeopleInfoOtimization(Map<String, Object> postMap) throws BusinessException {
        //fastjson 解析json字符串
        String masterId = (String) postMap.get("masterId");
        String dataInfo = (String) postMap.get("dataInfo");
        String isNext = (String) postMap.get("flag");
        JSONObject jsonObject = JSON.parseObject(dataInfo);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ZJsonObject> listJson = null;
        CacheListData ldata = cacheMap.get("listKey");
        if (null == ldata) {
            listJson = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<List<ZJsonObject>>() {
            });
            ldata = new CacheListData();
            ldata.setList(listJson);
            cacheMap.put("listKey", ldata);
        } else {
            listJson = ldata.getList();
        }

        //formBean是主表的信息
//        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
        Map<String, Object> result = new HashMap<String, Object>();
//        if (null == cacheFormData) {
//            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
//        }
        try {
            //明细行信息，使用cap4提供
//            List<FormDataSubBean> subBeans = CAP4FormKit.getSubBeans(cacheFormData);

            //过滤明细行，排除有数据的明细行
            List<FormDataSubBean> excludeExist = new ArrayList<>();

//            List<ZJsonObject> zJsonObjectList = new ArrayList<>();
//            for (int i = 0; i < subBeans.size(); i++) {
//                Map<String, Object> map = subBeans.get(i).getRowData();
//                for (String key : map.keySet()) {
//                    if (key.startsWith("field")) {
//                        Object fieldVal = map.get(key);
//                        if (null == fieldVal) {
//                            excludeExist.add(subBeans.get(i));
//                            break;
//                        } else {
//                            String val = fieldVal + "";
//                            for (int j = 0; j < listJson.size(); j++) {
//                                ZJsonObject zj = listJson.get(j);
//                                if (val.equals(zj.getField0001())) {
//                                    listJson.remove(j);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
//            }

//            String tableName = subBeans.get(0).getFormTable().getTableName();
//            result.put("tableName", tableName);
            result.put("tableName", "formson_0039");
            int toInt = Integer.parseInt(isNext);
            if (toInt <= 0) {
                result.put("add", false);
            } else {
                result.put("add", true);
            }

            result.put("dataCount", listJson.size());
            Map<String, Object> filldatas = null;

            Map<String, Object> dataMap = null;
            List<Object> listMap = new ArrayList<>();
//            for (int i = 0; i < excludeExist.size(); i++) {
//                Map<String, Object> masterMap = excludeExist.get(i).getRowData();
//                filldatas = new HashMap<>();
//                dataMap = new HashMap<>();
//                ZJsonObject zJsonObject = null;
//                if (i < listJson.size()) {
//                    zJsonObject = listJson.get(i);
//                    Map<String, Object> subTemp1 = new HashMap<>();
//
//                    subTemp1.put("showValue", zJsonObject.getField0001());
//                    subTemp1.put("showValue2", zJsonObject.getField0001());
//                    subTemp1.put("value", zJsonObject.getField0001());
//
//                    Map<String, Object> subTemp2 = new HashMap<>();
//                    subTemp2.put("showValue", zJsonObject.getName());
//                    subTemp2.put("showValue2", zJsonObject.getName());
//                    subTemp2.put("value", zJsonObject.getField0002());
//
//                    Map<String, Object> subTemp3 = new HashMap<>();
//                    subTemp3.put("showValue", zJsonObject.getName());
//                    subTemp3.put("showValue2", zJsonObject.getName());
//                    subTemp3.put("value", zJsonObject.getName());
//
//                    Map<String, Object> subTemp4 = new HashMap<>();
//                    subTemp4.put("showValue", zJsonObject.getField0003());
//                    subTemp4.put("showValue2", zJsonObject.getField0003());
//                    subTemp4.put("value", zJsonObject.getField0003());
//
//                    Map<String, Object> subTemp5 = new HashMap<>();
//                    subTemp5.put("showValue", zJsonObject.getField0005());
//                    subTemp5.put("showValue2", zJsonObject.getField0005());
//                    subTemp5.put("value", zJsonObject.getField0005());
//
////                    Map<String, Object> subTemp6 = new HashMap<>();
////                    subTemp6.put("showValue", zJsonObject.getMval());
////                    subTemp6.put("showValue2", zJsonObject.getMval());
////                    subTemp6.put("value", zJsonObject.getMval());
//
//                    Map<String, Object> subTemp7 = new HashMap<>();
//                    subTemp7.put("showValue", zJsonObject.getField0007());
//                    subTemp7.put("showValue2", zJsonObject.getField0007());
//                    subTemp7.put("value", zJsonObject.getField0007());
//                    int count = 1;
//
//                    for (String key : masterMap.keySet()) {
//                        if (key.startsWith("field")) {
//                            Object fieldVal = masterMap.get(key);
//                            if (null == fieldVal) {
//                                if (count == 1) {
//                                    filldatas.put(key, subTemp1);
//                                }
//                                if (count == 2) {
//                                    filldatas.put(key, subTemp2);
//                                }
//
//                                if (count == 3) {
//                                    filldatas.put(key, subTemp4);
//                                }
//                                if (count == 4) {
//                                    filldatas.put(key, subTemp5);
//                                }
//                                if (count == 5) {
//                                    filldatas.put(key, subTemp7);
//                                }
//
//                                count++;
//                                dataMap.put(masterMap.get("id") + "", filldatas);
//                            }
//                            dataMap.put("recordId", masterMap.get("id") + "");
//                        }
//                    }
//                    listMap.add(dataMap);
//                }
//            }


//            Map<String, Object> subTemp1 = new HashMap<>();
////
//            subTemp1.put("showValue", "1");
//            subTemp1.put("showValue2", "1");
//            subTemp1.put("value", "1");
//
//            Map<String, Object> subTemp2 = new HashMap<>();
//            subTemp2.put("showValue", "1");
//            subTemp2.put("showValue2", "1");
//            subTemp2.put("value", "1");
//
//            Map<String, Object> subTemp3 = new HashMap<>();
//            subTemp3.put("showValue", "1");
//            subTemp3.put("showValue2", "1");
//            subTemp3.put("value", "1");
//
//            Map<String, Object> subTemp4 = new HashMap<>();
//            subTemp4.put("showValue", "1");
//            subTemp4.put("showValue2","1");
//            subTemp4.put("value", "1");
//
//            Map<String, Object> subTemp5 = new HashMap<>();
//            subTemp5.put("showValue", "1");
//            subTemp5.put("showValue2", "1");
//            subTemp5.put("value", "1");
//
//            Map<String, Object> subTemp7 = new HashMap<>();
//            subTemp7.put("showValue", "1");
//            subTemp7.put("showValue2", "1");
//            subTemp7.put("value", "1");

            result.put("data", listMap);

        } catch (Exception e) {
            System.out.println("选择人员回填数据报错了：" + e.getMessage());
            logger.error("选择人员回填数据报错了：" + e.getMessage());
        }

        return success(result);
    }


}
