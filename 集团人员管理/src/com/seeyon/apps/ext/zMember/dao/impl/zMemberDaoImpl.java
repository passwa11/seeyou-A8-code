package com.seeyon.apps.ext.zMember.dao.impl;

import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.util.JDBCUtil;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 周刘成   2019/6/20
 */
public class zMemberDaoImpl implements zMemberDao {

    @Override
    public List<OrgMember> getAllMemberPO_New(Map<String, Object> param, Boolean isInternal, Boolean enable, FlipInfo flipInfo) {
        StringBuilder sql = new StringBuilder();

        int page = (flipInfo.getPage() - 1) * 20;
        int pages = flipInfo.getPage() * 20;

        sql.append("select * from ( select tw.*,rownum as rowno from ( select m.*  ");
        if (null != param && param.containsKey("loginName")) {
            sql.append(" FROM ORG_MEMBER as m, ORG_PRINCIPAL as p ");
            sql.append(" WHERE m.ID=p.MEMBER_ID ");
        } else {
            sql.append(" FROM ORG_MEMBER as m ");
            sql.append(" WHERE 1=1 ");
        }

        if (isInternal != null) {
            if (isInternal == true) {
                sql.append(" AND m.IS_INTERNAL=1");
            } else {
                sql.append(" AND m.IS_INTERNAL=0");
            }
        }
        if (enable != null) {
            if (enable == true) {
                sql.append(" AND m.IS_ENABLE=1 ");
            } else {
                sql.append(" AND m.IS_ENABLE=0 ");
            }
        }

        sql.append(" AND m.IS_DELETED=0 AND m.IS_ADMIN=0 AND m.IS_VIRTUAL=0 AND m.IS_ASSIGNED=1 ");
        if (null != param) {
            Set<Map.Entry<String, Object>> paramSet = param.entrySet();
            for (Map.Entry<String, Object> entry : paramSet) {
                String condition = entry.getKey();
                Object feildvalue = entry.getValue();
                if (StringUtils.isNotBlank(condition) && !"null".equals(condition)) {
                    if ("workLocal".equals(condition)) {
                        if ("".equals(entry.getValue())) {
                            sql.append(" AND (m.EXT_ATTR_36").append("like '%" + feildvalue + "%' or m.EXT_ATTR_36 is null)");
                        } else {
                            sql.append(" AND m.EXT_ATTR_36").append(" like '%" + feildvalue + "%'");
                        }
                    } else if (feildvalue instanceof String) {
                        if ("loginName".equals(condition)) {
                            sql.append(" AND (p.LOGIN_NAME").append(" LIKE '%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        } else if ("name".equals(condition)) {
                            sql.append(" AND (m.NAME").append(" LIKE '%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        } else if ("code".equals(condition)) {
                            sql.append(" AND (m.CODE").append(" LIKE '" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        }
                    }
                }
            }
        }
        sql.append(" ORDER BY m.SORT_ID ASC ) tw where rownum<=" + pages + " ) thr where thr.rowno > " + page + " ");

        List<Map<String, Object>> result = JDBCUtil.executeUpdate4Oracle04(sql.toString());
        return null;
    }

    @Override
    public List<OrgMember> getAllMemberPO(Map<String, Object> param, Boolean isInternal, Boolean enable, FlipInfo flipInfo) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");

        if (null != param && param.containsKey("loginName")) {
            hql.append(" FROM OrgMember as m, OrgPrincipal as p ");
            hql.append(" WHERE m.id=p.memberId ");
        } else {
            hql.append(" FROM OrgMember as m ");
            hql.append(" WHERE 1=1 ");
        }

        if (isInternal != null) {
            hql.append(" AND m.internal=:internal");
            params.put("internal", isInternal);
        }
        if (enable != null) {
            hql.append(" AND m.enable=:enable ");
            params.put("enable", enable);
        }

        hql.append(" AND m.deleted=false AND m.admin=false AND m.virtual=false AND m.assigned=true");
        if (null != param) {
            Set<Map.Entry<String, Object>> paramSet = param.entrySet();
            for (Map.Entry<String, Object> entry : paramSet) {
                String condition = entry.getKey();
                Object feildvalue = entry.getValue();
                if (StringUtils.isNotBlank(condition) && !"null".equals(condition)) {
                    if ("workLocal".equals(condition)) {
                        if ("".equals(entry.getValue())) {
                            hql.append(" AND (m.extAttr36").append("like :" + condition + " or m.extAttr36 is null)");
                        } else {
                            hql.append(" AND m.extAttr36").append(" like :" + condition);
                        }
                        params.put(condition, feildvalue);
                    } else if (feildvalue instanceof String) {
                        if ("loginName".equals(condition)) {
                            hql.append(" AND (p.").append(condition).append(" LIKE :" + condition + SQLWildcardUtil.setEscapeCharacter() + ")");
                        } else {
                            hql.append(" AND (m.").append(condition).append(" LIKE :" + condition + SQLWildcardUtil.setEscapeCharacter() + ")");
                        }
                        feildvalue = "%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%";
                        params.put(condition, String.valueOf(feildvalue));
                    } else if (feildvalue != null) {
                        hql.append(" AND m.").append(condition).append("=:" + condition);
                        params.put(condition, feildvalue);
                    }
                }
            }
        }
        hql.append(" ORDER BY m.sortId ASC");
        return DBAgent.find(hql.toString(), params, flipInfo);
    }

    @Override
    public int selectUnitPeopleCount() {
        String sql = "select m.name from org_member m where 1=1  AND m.IS_DELETED=0 AND m.IS_ADMIN=0 AND m.IS_VIRTUAL=0 AND m.IS_ASSIGNED=1 and IS_ENABLE=1";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        int count = 0;
        if (list != null) {
            count = list.size();
        }
        return count;
    }
}
