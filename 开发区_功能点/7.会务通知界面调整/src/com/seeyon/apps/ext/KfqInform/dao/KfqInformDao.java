package com.seeyon.apps.ext.KfqInform.dao;

import com.seeyon.apps.ext.KfqInform.po.KfqInform;

import java.util.List;
import java.util.Map;

public interface KfqInformDao {

    void saveInform(KfqInform inform);

    void updateInform(List<KfqInform> inform);

    List<KfqInform> findInformbyUserid(Map map);
    List<KfqInform> findInformbySummaryid(Map map);

}
