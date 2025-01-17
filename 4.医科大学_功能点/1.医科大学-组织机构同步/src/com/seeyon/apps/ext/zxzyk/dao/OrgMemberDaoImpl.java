package com.seeyon.apps.ext.zxzyk.dao;

import com.seeyon.apps.ext.zxzyk.po.CtpOrgUser;
import com.seeyon.apps.ext.zxzyk.po.OrgMember;
import com.seeyon.apps.ext.zxzyk.util.SyncConnectionUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.util.DBAgent;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class OrgMemberDaoImpl implements OrgMemberDao {

    @Override
    public List<OrgMember> queryNoEnableMember() {
        String sql = "select MM.id,mm.code,vm.is_enable,VM.name from V_ORG_MEMBER vm, M_ORG_MEMBER mm where VM.CODE=MM.code and VM.IS_ENABLE='0'";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setMemberid(rs.getString("id"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setMembercode(rs.getString("code"));
                orgMember.setMembername(rs.getString("name"));
                orgMember.setEnabled(false);
                memberList.add(orgMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return memberList;
    }

    @Override
    public void updateIsEnableOfMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        try {
            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("id", member.getMemberid());
                    memberMap.put("orgAccountId", member.getOrgAccountId());
                    memberMap.put("enabled", member.getEnabled());

                    JSONObject memberJson = client.get("/orgMember?loginName=" + member.getMembercode(), JSONObject.class);
                    if (null != memberJson) {
                        JSONObject json = client.put("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                String sql = "update m_org_member set ";
                                if (!member.getEnabled()) {
                                    sql = sql + " IS_ENABLE = '0' ";
                                } else {
                                    sql = sql + " IS_ENABLE = '1' ";
                                }

                                sql = sql + " where id = '" + member.getMemberid() + "' ";

                                SyncConnectionUtil.insertResult(sql);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrgMember> queryAddOrgMember() {
        //正式
        String sql = "select DISTINCT c2.code,c2.name,c2.id,c2.POSTID,c2.description,c2.mobile,M_ORG_UNIT.id unitId,M_ORG_LEVEL.id levelId ,c2.\"ou\" ou from " +
                "(select memb.*,M_ORG_POST.id postid from (" +
                "select * from V_ORG_MEMBER vm where not EXISTS(select * from M_ORG_MEMBER  where vm.code = M_ORG_MEMBER.code  and vm.name = M_ORG_MEMBER.name)) " +
                "memb,M_ORG_POST  where memb.org_post_id = M_ORG_POST.code) c2 LEFT JOIN M_ORG_UNIT  on nvl(c2.org_account_id,c2.sup_department_id) = M_ORG_UNIT.code " +
                "LEFT JOIN M_ORG_LEVEL  on c2.org_level_id=M_ORG_LEVEL.code";
        //测试
        String testsql = "select DISTINCT c2.code,c2.name,c2.id,c2.POSTID,c2.description,c2.mobile,M_test_unit.id unitId,M_test_LEVEL.id levelId from " +
                "                (select memb.*,M_ORG_POST.id postid from (" +
                "                select * from V_test_MEMBER vm where not EXISTS(select * from M_test_MEMBER  where vm.code = M_test_MEMBER.code  and vm.name = M_test_MEMBER.name)) " +
                "                memb,M_ORG_POST  where memb.org_post_id = M_ORG_POST.code) c2 LEFT JOIN M_test_unit  on nvl(c2.org_account_id,c2.sup_department_id) = M_test_unit.code " +
                "                LEFT JOIN M_test_LEVEL  on c2.org_level_id=M_test_LEVEL.code";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setMemberid(rs.getString("id"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setMembercode(rs.getString("code"));
                orgMember.setMembername(rs.getString("name"));
                orgMember.setLoginName(rs.getString("code"));
                orgMember.setOrgDepartmentId(rs.getString("unitid"));
                orgMember.setOrgLevelId(rs.getString("levelid"));
                orgMember.setOrgPostId(rs.getString("postid"));
                orgMember.setDescription(rs.getString("description"));
                orgMember.setTelNumber(rs.getString("mobile"));
                orgMember.setOu(rs.getString("ou"));
                memberList.add(orgMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return memberList;
    }

    @Override
    public void insertOrgMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        //正式
        String insertSql = "insert into m_org_member(id,code,name,login_name,org_department_id,org_level_id,description,mobile,org_post_id,ou) values (?,?,?,?,?,?,?,?,?,?)";
        //测试
        String testinsertSql = "insert into m_test_member(id,code,name,login_name,org_department_id,org_level_id,description,mobile,org_post_id) values (?,?,?,?,?,?,?,?,?)";

        try {
            connection = SyncConnectionUtil.getMidConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);

            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("name", member.getMembername());
                    memberMap.put("loginName", member.getLoginName());
                    memberMap.put("orgAccountId", member.getOrgAccountId());
                    memberMap.put("orgLevelId", member.getOrgLevelId());
                    memberMap.put("orgPostId", member.getOrgPostId());
                    memberMap.put("orgDepartmentId", member.getOrgDepartmentId());
                    memberMap.put("code", member.getMembercode());
                    memberMap.put("enabled", member.getEnabled());
                    memberMap.put("telNumber", member.getTelNumber());
                    memberMap.put("description", member.getDescription());

                    JSONObject memberJson = client.get("/orgMember?loginName=" + member.getMembercode(), JSONObject.class);
                    if (null == memberJson) {
                        JSONObject json = client.post("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String userid = ent.getString("id");
                                ps.setString(1, userid);
                                ps.setString(2, member.getMembercode());
                                ps.setString(3, member.getMembername());
                                ps.setString(4, member.getLoginName());
                                ps.setString(5, member.getOrgDepartmentId());
                                ps.setString(6, member.getOrgLevelId());
                                ps.setString(7, member.getDescription());
                                ps.setString(8, member.getTelNumber());
                                ps.setString(9, member.getOrgPostId());
                                ps.setString(10, member.getOu());
                                ps.addBatch();

                                CtpOrgUser orgUser = new CtpOrgUser();
                                orgUser.setId(Long.parseLong(userid));
                                orgUser.setType("ldap.member.openLdap");
                                orgUser.setLoginName(ent.getString("loginName"));
                                orgUser.setExLoginName(member.getMembercode());
                                orgUser.setExPassword("1");
                                orgUser.setExId(member.getMemberid());
                                orgUser.setExUserId(member.getMemberid());
                                orgUser.setMemberId(Long.parseLong(userid));
                                orgUser.setActionTime(new Date());
                                orgUser.setDescription("te");
                                orgUser.setExUnitCode("uid=" + ent.getString("loginName") + ",ou=" + member.getOu());
                                DBAgent.save(orgUser);
                            }
                        }
                    } else {
                        String userid = memberJson.getString("id");
                        ps.setString(1, userid);
                        ps.setString(2, member.getMembercode());
                        ps.setString(3, member.getMembername());
                        ps.setString(4, member.getLoginName());
                        ps.setString(5, "");
                        ps.setString(6, "");
                        ps.setString(7, member.getDescription());
                        ps.setString(8, member.getTelNumber());
                        ps.setString(9, "");
                        ps.setString(10, member.getOu());
                        ps.addBatch();
                        ps.executeBatch();
                        connection.commit();
                    }

                }
            }
            ps.executeBatch();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
    }

    @Override
    public List<OrgMember> queryUpdateOrgMember() {
        //正式
        String sql = "select t.id,t.code,k.ou,k.is_enable,k.name,k.org_department_id unitid,k.org_level_id levelid,k.org_post_id postid,k.description,k.mobile from  (select distinct m.id,v.code  from m_org_member m,   " +
                "(select v1.\"ou\" ou,v1.IS_ENABLE,v1.code,v1.name,u1.id org_department_id,l1.id org_level_id,p1.id org_post_id,v1.description,v1.mobile    from V_ORG_MEMBER v1,m_org_unit u1,m_org_level l1,m_org_post p1    " +
                "where v1.org_department_id = u1.code and v1.org_level_id = l1.code and v1.org_post_id = p1.code) v   where v.code = m.code   and (       nvl(m.name,'~') <> nvl(v.name,'~') or    " +
                "nvl(m.org_department_id,'~') <> nvl(v.org_department_id,'~') or       nvl(m.org_level_id,'~') <> nvl(v.org_level_id,'~') or       nvl(m.org_post_id,'~') <> nvl(v.org_post_id,'~') or   " +
                "nvl(m.description,'~') <> nvl(v.description,'~') or       nvl(m.mobile,'~') <> nvl(v.mobile,'~')  or nvl(m.ou,'~')!=v.ou   or nvl(m.IS_ENABLE,'1')!=v.ou ) ) t  left join   " +
                "(select v2.\"ou\" ou,v2.IS_ENABLE,v2.code,v2.name,u2.id org_department_id,l2.id org_level_id,p2.id org_post_id,v2.description,v2.mobile    " +
                "from V_ORG_MEMBER v2,m_org_unit u2,m_org_level l2,m_org_post p2   where v2.org_department_id = u2.code and v2.org_level_id = l2.code and v2.org_post_id = p2.code) k   on t.code = k.code ";
        //测试
        String testsql = "select t.id,t.code,k.name,k.org_department_id unitid,k.org_level_id levelid,k.org_post_id postid,k.description,k.mobile from  (select distinct m.id,v.code  from m_test_member m,  " +
                "                 (select v1.code,v1.name,u1.id org_department_id,l1.id org_level_id,p1.id org_post_id,v1.description,v1.mobile    from V_test_MEMBER v1,m_test_unit u1,m_test_level l1,m_org_post p1   " +
                "                where v1.org_department_id = u1.code and v1.org_level_id = l1.code and v1.org_post_id = p1.code) v   where v.code = m.code   and (       nvl(m.name,'~') <> nvl(v.name,'~') or   " +
                "                 nvl(m.org_department_id,'~') <> nvl(v.org_department_id,'~') or       nvl(m.org_level_id,'~') <> nvl(v.org_level_id,'~') or       nvl(m.org_post_id,'~') <> nvl(v.org_post_id,'~') or  " +
                "                 nvl(m.description,'~') <> nvl(v.description,'~') or       nvl(m.mobile,'~') <> nvl(v.mobile,'~')   ) ) t  left join  " +
                "                 (select v2.code,v2.name,u2.id org_department_id,l2.id org_level_id,p2.id org_post_id,v2.description,v2.mobile   " +
                "                from V_test_MEMBER v2,m_test_unit u2,m_test_level l2,m_org_post p2   where v2.org_department_id = u2.code and v2.org_level_id = l2.code and v2.org_post_id = p2.code) k   on t.code = k.code ";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setMemberid(rs.getString("id"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setMembercode(rs.getString("code"));
                orgMember.setMembername(rs.getString("name"));
                orgMember.setLoginName(rs.getString("code"));
                orgMember.setOrgDepartmentId(rs.getString("unitid"));
                orgMember.setOrgLevelId(rs.getString("levelid"));
                orgMember.setOrgPostId(rs.getString("postid"));
                orgMember.setDescription(rs.getString("description"));
                orgMember.setTelNumber(rs.getString("mobile"));
                orgMember.setOu(rs.getString("ou"));
                orgMember.setP1(rs.getString("is_enable"));
                memberList.add(orgMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return memberList;
    }

    @Override
    public void updateOrgMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        try {
            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("id", member.getMemberid());
                    memberMap.put("name", member.getMembername());
                    memberMap.put("loginName", member.getLoginName());
                    memberMap.put("orgAccountId", member.getOrgAccountId());
                    memberMap.put("orgLevelId", member.getOrgLevelId());
                    memberMap.put("orgPostId", member.getOrgPostId());
                    memberMap.put("orgDepartmentId", member.getOrgDepartmentId());
                    memberMap.put("code", member.getMembercode());
                    if(!"".equals(member.getP1()) && "1".equals(member.getP1())){
                        memberMap.put("enabled", true);
                    }else if ("0".equals(member.getP1())){
                        memberMap.put("enabled", false);
                    }else {
                        memberMap.put("enabled", true);
                    }
                    memberMap.put("telNumber", member.getTelNumber() != null ? member.getTelNumber() : "");
                    memberMap.put("description", member.getDescription() != null ? member.getDescription() : "");

                    JSONObject memberJson = client.get("/orgMember?loginName=" + member.getMembercode(), JSONObject.class);
                    if (null != memberJson) {
                        JSONObject json = client.put("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String userid = ent.getString("id");
                                CtpOrgUser user = DBAgent.get(CtpOrgUser.class, Long.parseLong(userid));
                                CtpOrgUser orgUser = new CtpOrgUser();
                                orgUser.setId(Long.parseLong(userid));
                                orgUser.setType("ldap.member.openLdap");
                                orgUser.setLoginName(ent.getString("loginName"));
                                orgUser.setExLoginName(member.getMembercode());
                                orgUser.setExPassword("1");
                                orgUser.setExId(member.getMemberid());
                                orgUser.setExUserId(member.getMemberid());
                                orgUser.setMemberId(Long.parseLong(userid));
                                orgUser.setActionTime(new Date());
                                orgUser.setDescription("te");
                                orgUser.setExUnitCode("uid=" + ent.getString("loginName") + ",ou=" + member.getOu());
                                if (null != user) {
                                    DBAgent.update(orgUser);
                                } else {
                                    DBAgent.save(orgUser);
                                }


                                String sql = "update m_org_member set ";
                                if (member.getMembername() != null && !"".equals(member.getMembername())) {
                                    sql = sql + " name = '" + member.getMembername() + "', ";
                                } else {
                                    sql = sql + " name = '', ";
                                }

                                if (member.getOrgDepartmentId() != null && !"".equals(member.getOrgDepartmentId())) {
                                    sql = sql + " org_department_id = '" + member.getOrgDepartmentId() + "', ";
                                } else {
                                    sql = sql + " org_department_id = '', ";
                                }

                                if (member.getOrgPostId() != null && !"".equals(member.getOrgPostId())) {
                                    sql = sql + " org_post_id = '" + member.getOrgPostId() + "', ";
                                } else {
                                    sql = sql + " org_post_id = '', ";
                                }

                                if (member.getOrgLevelId() != null && !"".equals(member.getOrgLevelId())) {
                                    sql = sql + " org_level_id = '" + member.getOrgLevelId() + "', ";
                                } else {
                                    sql = sql + " org_level_id = '', ";
                                }

                                if (member.getDescription() != null && !"".equals(member.getDescription())) {
                                    sql = sql + " description = '" + member.getDescription() + "', ";
                                } else {
                                    sql = sql + " description = '', ";
                                }

                                if (member.getOu() != null && !"".equals(member.getOu())) {
                                    sql = sql + " ou = '" + member.getOu() + "', ";
                                } else {
                                    sql = sql + " ou = '', ";
                                }

                                if (member.getP1() != null && !"".equals(member.getP1())) {
                                    sql = sql + " is_enable = '" + member.getP1() + "', ";
                                } else {
                                    sql = sql + " is_enable = '', ";
                                }

                                if (member.getTelNumber() != null && !"".equals(member.getTelNumber())) {
                                    sql = sql + " mobile = '" + member.getTelNumber() + "' ";
                                } else {
                                    sql = sql + " mobile = '' ";
                                }

                                sql = sql + " where id = '" + member.getMemberid() + "' ";

                                SyncConnectionUtil.insertResult(sql);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrgMember> queryNotExistOrgMember() {
        String sql = "select M_ORG_MEMBER.id from M_ORG_MEMBER where not EXISTS (select * from V_ORG_MEMBER where M_ORG_MEMBER.code=V_ORG_MEMBER.code )";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setMemberid(rs.getString("id"));
                memberList.add(orgMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
        return memberList;
    }

    @Override
    public void deleteOrgMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                StringBuffer dsql = new StringBuffer();
                dsql.append("delete from m_org_member where id in (0 ");
                for (OrgMember member : list) {

                    map = new HashMap();
                    map.put("id", member.getMemberid());
                    map.put("enabled", false);
                    JSONObject jsonObject = client.put("/orgMember/" + member.getMemberid() + "/enabled/false", map, JSONObject.class);
                    if (null != jsonObject) {

                        if (jsonObject.getBoolean("success")) {
                            dsql.append(",'" + member.getMemberid() + "'");
                        }
                    } else {
                        dsql.append(",'" + member.getMemberid() + "'");
                    }

                }
                dsql.append(")");
                SyncConnectionUtil.insertResult(dsql.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
