package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.ext.meetingInfoTip.po.MeetingAppHistory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.sql.SQLException;
import java.util.Map;

public interface MeetingRoomHistoryManager {

    void saveRoomappHistory(MeetingAppHistory history);

    FlipInfo findPageByCondition(FlipInfo flipInfo,Map<String, Object> map) throws SQLException, BusinessException;

}
