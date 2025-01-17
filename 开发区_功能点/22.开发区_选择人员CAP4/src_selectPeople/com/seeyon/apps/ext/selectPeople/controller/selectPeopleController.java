package com.seeyon.apps.ext.selectPeople.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.selectPeople.manager.JtldEntityManager;
import com.seeyon.apps.ext.selectPeople.manager.JtldEntityManagerImpl;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.apps.ext.selectPeople.util.JDBCUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.alibaba.fastjson.JSONArray;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class selectPeopleController extends BaseController {

    private JtldEntityManager manager = new JtldEntityManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("apps/ext/Zselect/index");
    }

    public ModelAndView selectListData(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            String type = request.getParameter("type");
            List<Map<String, Object>> list = null;
            List<Map<String, Object>> revoler = new ArrayList<>();
            if (type.equals("29")) {
                list = manager.selectDangZhengBan0029(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue());
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "dangzheng29");
                    revoler.add(m);
                }
            } else if (type.equals("30")) {
                list = manager.selectJiGuan0030(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue());
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "jiguan30");
                    revoler.add(m);
                }
            } else if (type.equals("31")) {
                list = manager.selectZhenBan0031(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue());
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "zhenban31");
                    revoler.add(m);
                }
            } else if (type.equals("32")) {
                list = manager.selectZhuQu0032(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "zhuqu32");
                    revoler.add(m);
                }
            } else if (type.equals("gh")) {
                list = manager.selectGonghui(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "gonghui");
                    revoler.add(m);
                }
            } else if (type.equals("two")) {
                list = manager.selectTwoLevelDept(name);
                for (int j = 0; j < list.size(); j++) {
                    Map<String, Object> m = list.get(j);
                    String userId = (String) m.get("field0002");
                    String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
                    List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
                    for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = new HashMap<>();
                    for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                        m.put(entry.getKey(), entry.getValue() + "");
                    }
                    m.put("flag", "two");
                    revoler.add(m);
                }
            } else if (type.equals("dzbld")) {
                String tableName = "formmain_0568";
                String flag = "dzbld";
                list = manager.selectCommon(name, tableName);
                revoler = toRevokeListData(list, flag);
            } else if (type.equals("hwz")) {
                String tableName = "formmain_0570";
                String flag = "hwz";
                list = manager.selectCommon(name, tableName);
                revoler = toRevokeListData(list, flag);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", revoler.size());
            map.put("data", revoler);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> toRevokeListData(List<Map<String, Object>> list, String flag) {
        List<Map<String, Object>> revoler = new ArrayList<>();
        for (int j = 0; j < list.size(); j++) {
            Map<String, Object> m = list.get(j);
            String userId = (String) m.get("field0002");
            String sql = "select WMSYS.WM_CONCAT(name) name from ORG_MEMBER where id in(" + userId + ")";
            List<Map<String, Object>> l = JDBCUtil.doQuery(sql);
            for (Map.Entry<String, Object> entry : l.get(0).entrySet()) {
                m.put(entry.getKey(), entry.getValue() + "");
            }
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> m = new HashMap<>();
            for (Map.Entry<String, Object> entry : list.get(i).entrySet()) {
                m.put(entry.getKey(), entry.getValue() + "");
            }
            m.put("flag", flag);
            revoler.add(m);
        }
        return revoler;
    }

    /**
     * 给前台渲染json数据
     *
     * @param response
     * @param text
     */
    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ModelAndView insertData(HttpServletRequest request, HttpServletResponse response) {
        String info = request.getParameter("info");
        JSONArray jsonArray = JSON.parseArray(info);
        List<Formson0174> list = new ArrayList<>();
        for (Object object : jsonArray) {
            Formson0174 f0174 = new Formson0174();
            JSONObject jsonObject = (JSONObject) object;
            f0174.setField0044(jsonObject.getString("text"));
            f0174.setField0045(jsonObject.getString("value"));
            f0174.setField0046(jsonObject.getString("dept"));
            list.add(f0174);
        }
        manager.insertFormson0174(list);
        return null;
    }


}
