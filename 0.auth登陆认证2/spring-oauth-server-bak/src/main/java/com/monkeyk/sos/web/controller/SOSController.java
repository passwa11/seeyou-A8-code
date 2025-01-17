package com.monkeyk.sos.web.controller;


import com.monkeyk.sos.service.OauthService;
import com.monkeyk.sos.service.dto.OauthClientDetailsDto;
import com.monkeyk.sos.web.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 2018/4/19
 * <p>
 * starup
 *
 * @author Shengzhao Li
 */
@Controller
public class SOSController {


    private static final Logger LOG = LoggerFactory.getLogger(SOSController.class);

    @Autowired
    private OauthService oauthService;


    /**
     * 首页
     */
    @RequestMapping(value = "/")
    public String index(Model model) {
        //model.addAttribute("formDto", new OauthClientDetailsDto());
        //return "clientdetails/register_client";
        List<OauthClientDetailsDto> clientDetailsDtoList = oauthService.loadAllOauthClientDetailsDtos();
        model.addAttribute("clientDetailsDtoList", clientDetailsDtoList);
        return "index";
    }


    //Go login
    @GetMapping(value = {"/login"})
    public String login(Model model) {
        LOG.info("Go to login, IP: {}", WebUtils.getIp());
        return "login";
    }


}
