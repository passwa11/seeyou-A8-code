package com.seeyon.apps.ext.kydx.po;

import java.util.List;

/**
 * 部门
 */
public class OrgDept {

    private String id;
    private String deptName;
    private String deptCode;
    private String deptDescription;
    private boolean deptEnable = true;
    private String deptParentId;
    private String orgAccountId;

    private String shortName;
    private String dwid;
    private String oaParentId;
//新加字段
    private String isUse;
    private String param1;

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getIsUse() {
        return isUse;
    }

    public void setIsUse(String isUse) {
        this.isUse = isUse;
    }

    public String getOaParentId() {
        return oaParentId;
    }

    public void setOaParentId(String oaParentId) {
        this.oaParentId = oaParentId;
    }

    public String getDwid() {
        return dwid;
    }

    public void setDwid(String dwid) {
        this.dwid = dwid;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    private String parentId;

    private List<OrgDept> list;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<OrgDept> getList() {
        return list;
    }

    public void setList(List<OrgDept> list) {
        this.list = list;
    }

    public String getOrgAccountId() {
        return orgAccountId;
    }

    public void setOrgAccountId(String orgAccountId) {
        this.orgAccountId = orgAccountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptDescription() {
        return deptDescription;
    }

    public void setDeptDescription(String deptDescription) {
        this.deptDescription = deptDescription;
    }

    public boolean isDeptEnable() {
        return deptEnable;
    }

    public void setDeptEnable(boolean deptEnable) {
        this.deptEnable = deptEnable;
    }

    public String getDeptParentId() {
        return deptParentId;
    }

    public void setDeptParentId(String deptParentId) {
        this.deptParentId = deptParentId;
    }
}
