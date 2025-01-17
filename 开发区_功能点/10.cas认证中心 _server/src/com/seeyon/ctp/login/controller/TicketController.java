package com.seeyon.ctp.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.login.server.listener.GlobalSessions;
import com.seeyon.ctp.login.server.util.Constant;
import com.seeyon.ctp.login.server.util.StringUtilSso;
import com.seeyon.ctp.login.server.util.TicketUtil;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TicketController extends BaseController {

    @NeedlessCheckLogin
    public ModelAndView verify(String ticket, String localSessionId, String localLoginOutUrl,
                               String globalSessionId, HttpServletRequest request,HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        String account = TicketUtil.get(ticket);
        TicketUtil.remove(ticket);
        if (StringUtilSso.isUnEmpty(account)) {
            logger.info("令牌认证成功");
            // TODO 保存本地会话id和退出接口到 全局会话
            HttpSession httpSession = GlobalSessions.get(globalSessionId);
            Map<String, String> loginOutMap = null;
            if (httpSession.getAttribute("loginOutMap") != null) {
                loginOutMap = (Map<String, String>) httpSession.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<应用退出接口，应用服务器会话id>
            } else {
                loginOutMap = new HashMap<>();
                httpSession.setAttribute("loginOutMap", loginOutMap);
            }
            loginOutMap.put(localSessionId, localLoginOutUrl);
            // 返回数据
            map.put("code", Constant.CODE_SUCCESS);
            map.put("msg", "令牌认证成功!");
            //map.put("globalSessionId", globalSessionId);// 应用发送给SSO退出请求时使用(应该无需返回)，之前登录生成令牌回调已经发送了全局会话id
            map.put("account", account);
        } else {
            logger.info("令牌认证失败");
            map.put("code", Constant.CODE_FAIL);
            map.put("msg", "令牌认证失败");
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
