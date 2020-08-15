package com.seeyon.apps.meetingroom.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MeetingRoomHistoryController extends BaseController {

    public ModelAndView toMeetingCancelHistoryPage(HttpServletResponse response, HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("meetingroom/meetingRoomCancelHistory");
        return modelAndView;
    }

}
