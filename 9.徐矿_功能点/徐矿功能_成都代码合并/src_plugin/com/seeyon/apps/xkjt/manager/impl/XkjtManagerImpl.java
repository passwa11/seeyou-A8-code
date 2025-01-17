package com.seeyon.apps.xkjt.manager.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.apps.xkjt.po.EdocFormInfo;
import com.seeyon.apps.xkjt.po.XkjtLeaderBanJie;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.apps.xkjt.po.XkjtOpenMode;
import com.seeyon.apps.xkjt.po.XkjtSummaryAttachment;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRelationship;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.WorkflowAjaxManager;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class XkjtManagerImpl implements XkjtManager {
	private static Log log = LogFactory.getLog(XkjtManagerImpl.class);
	private OrgManager orgManager;
	private XkjtDao xkjtDao;
    private AffairManager affairManager;
    private EdocManager edocManager;
    private ColManager colManager;
	

	public ColManager getColManager() {
		return colManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setXkjtDao(XkjtDao xkjtDao) {
		this.xkjtDao = xkjtDao;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	@AjaxAccess
	public FlipInfo findAllRoles(FlipInfo fi, Map param) throws BusinessException {
        Long accountId = Long.parseLong(param.get("orgAccountId").toString());
        //单位角色列表
        List<V3xOrgRole> allRoles = orgManager.getAllRoles(accountId);
        fi.setData(allRoles);
        return fi;
    }
	
	@AjaxAccess
	public Map<String, String> checkRoles(String roleIds) throws BusinessException{
			Map<String, String> result = new HashMap();
			String[] roleIdArr = roleIds.replace("[", "").replace("]", "").replace("\"", "").split(",");
			String roleStr = "";
			for (String roleId : roleIdArr) {
				V3xOrgRole role = this.orgManager.getRoleById(Long.valueOf(roleId));
				if (Strings.isBlank(roleStr)) {
					roleStr = role.getShowName();
				} else {
					roleStr = roleStr + "," + role.getShowName();
				}
			}
			result.put("success", "true");
			result.put("roleStr", roleStr);
			return result;
	}

	@AjaxAccess
	@Override
	public Map<String, String> saveNodeOpenMode(Map param) throws BusinessException {
		/**项目：徐矿集团 【修复在设置发起者公开方式时报错问题】 作者：jiangchenxi 时间：2019年8月27日 start*/
		if(String.valueOf(param.get("nodeId")).equals("start")){
			return null;
		}
		/**项目：徐矿集团 【修复在设置发起者公开方式时报错问题】 作者：jiangchenxi 时间：2019年8月27日 end*/
		Long nodeId = Long.valueOf(String.valueOf(param.get("nodeId")));
		String roleIds = String.valueOf(param.get("roleIds"));
		try{
		List list = xkjtDao.findOpenModeByNodeId(nodeId);
		if(list.size()==0){
			String type = String.valueOf(param.get("type"));
			if("1".equals(type)){
				
			}else{
				
				String[] roleIdArr = roleIds.replace("[", "").replace("]", "").replace("\"", "").split(",");
				String roleIdsStr = "";
				for (int i = 0; i < roleIdArr.length; i++) {
					if(i==0){
						roleIdsStr += roleIdArr[i];
					}else{
						roleIdsStr += ","+roleIdArr[i];
					}
				}
				
					XkjtOpenMode xkjtOpenMode = new XkjtOpenMode();
					xkjtOpenMode.setIdIfNew();
					xkjtOpenMode.setNodeId(nodeId);
					xkjtOpenMode.setRoleIds(roleIdsStr);
					xkjtOpenMode.setIsDeleted(0);
					xkjtDao.saveOpenMode(xkjtOpenMode);
				
			}
			
			
		}else{
			String type = String.valueOf(param.get("type"));
			if("1".equals(type)){
				
					XkjtOpenMode om = (XkjtOpenMode) list.get(0);
					om.setIsDeleted(1);
					xkjtDao.updateOpenMode(om);
				
			}else{
				
					String[] roleIdArr = roleIds.replace("[", "").replace("]", "").replace("\"", "").split(",");
					String roleIdsStr = "";
					for (int i = 0; i < roleIdArr.length; i++) {
						if(i==0){
							roleIdsStr += roleIdArr[i];
						}else{
							roleIdsStr += ","+roleIdArr[i];
						}
					}
					XkjtOpenMode om = (XkjtOpenMode) list.get(0);
					om.setNodeId(nodeId);
					om.setRoleIds(roleIdsStr);
					om.setIsDeleted(0);
					xkjtDao.updateOpenMode(om);
				
			}
			
		}
		}catch(Exception e){
			log.error("保存公开方式出错", e);
		}
		
		
		
		return null;
	}

	@AjaxAccess
	@Override
	public Map getOpenModeByNodeId(Map param) throws BusinessException {
		Long nodeId = Long.valueOf(String.valueOf(param.get("nodeId")));
		Map<String, String> result = new HashMap();
		List list = xkjtDao.findOpenModeByNodeId(nodeId);
		String roleIds = "";
		String roleStr = "";
		for (Object object : list) {
			XkjtOpenMode xkjtOpenMode = (XkjtOpenMode) object;
			roleIds = xkjtOpenMode.getRoleIds();
			String[] roleIdArr = roleIds.split(",");
			for (String roleId : roleIdArr) {
				V3xOrgRole role = this.orgManager.getRoleById(Long.valueOf(roleId));
				if (Strings.isBlank(roleStr)) {
					roleStr = role.getShowName();
				} else {
					roleStr = roleStr + "," + role.getShowName();
				}
			}
		}
		result.put("roleStr", roleStr);
		result.put("roleIds", roleIds);
		return result;
	}
	
	@Override
	public boolean isOpen(Long affairId)  throws BusinessException{
		User user = AppContext.getCurrentUser();
		CtpAffair affair = affairManager.get(affairId);
		if(affair == null || affair.isFinish() || affair.getState() == 3) {
			//流程完结或者当前用户通过待办进入
			return true;
		}
		List<MemberRole> roles = orgManager.getMemberRoles(user.getId(), user.getAccountId());
		List<CtpAffair> affairs = affairManager.getAffairs(affair.getObjectId());
		List<List<String>> modesList = new ArrayList();
		for(CtpAffair ctpAffair : affairs) {
			if(ctpAffair.getState() == 3) {
				List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(ctpAffair.getActivityId());
				if(openModes.size() > 0 && openModes.get(0).getRoleIds() != null) {
					modesList.add(new ArrayList<String>(Arrays.asList(openModes.get(0).getRoleIds().split(","))));
				}
			}
		}
		if(modesList.size()>0) {
			List<String> commonRole = modesList.get(0);
			for(int i = 1;i<modesList.size();i++) {
				List<String> roleList = modesList.get(i);
				List<String> newList = new ArrayList();
				newList.addAll(commonRole);
				newList.removeAll(roleList);
				commonRole.removeAll(newList);
			}
			//获取所有单位下有权限的角色id
			List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
			List<String> allCommonRole = new ArrayList();
			for(String roleId : commonRole) {
				V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
				for(V3xOrgAccount account : accounts) {
					List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
					if(acountRole != null && acountRole.size()>0) {
						V3xOrgRole allRole = acountRole.get(0);
						allCommonRole.add(String.valueOf(allRole.getId()));
					}
				}
			}
			if(commonRole.size()>0) {
				for(MemberRole memberRole : roles) {
					V3xOrgRole role = memberRole.getRole();
					log.info("获取到的角色名称："+role.getName());
					log.info("获取到的角色Id："+role.getId());
					if(allCommonRole.contains(String.valueOf(role.getId()))) {
						//存在可访问权限
						return true;
					}
				}
			}else {
				//没有共同的访问权限
				return false;
			}
		}else {
			//都是公开权限
			return true;
		}
		//没有权限访问
		return false;
	}

	@Override
	public List getOpenOpinions(List list, int type) throws BusinessException {
		User user = AppContext.getCurrentUser();
		List<MemberRole> roles = orgManager.getMemberRoles(user.getId(), user.getAccountId());
//		CtpAffair affair = affairManager.get(affairId);
//		if(affair.isFinish() || affair.getState() == 3) {
//			return list;
//		}
		if(list == null || list.size() == 0) {
			return list;
		}
		if(type == 1) {
			EdocOpinion userAffair = (EdocOpinion) list.get(0);
			CtpAffair affair = affairManager.get(userAffair.getAffairId());
			EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
			if(summary.getFinished()) {
				return list;
			}
			List remove = new ArrayList();
			for(int i = 0; i < list.size(); i++) {
				EdocOpinion edocOpinion = (EdocOpinion) list.get(i);
				List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(edocOpinion.getNodeId());
				if(openModes.size() == 0) {

				}else {
					XkjtOpenMode openMode = openModes.get(0);
					int flag = 0;
					if(openMode.getRoleIds() == null) {
						return list;
					}
					
					//获取所有单位下有权限的角色id
					List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
					List<String> allCommonRole = new ArrayList();
					List<String> commonRole = new ArrayList<String>(Arrays.asList(openMode.getRoleIds().split(",")));
					for(String roleId : commonRole) {
						V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
						for(V3xOrgAccount account : accounts) {
							List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
							if(acountRole != null && acountRole.size()>0) {
								V3xOrgRole allRole = acountRole.get(0);
								allCommonRole.add(String.valueOf(allRole.getId()));
							}
						}
					}
					for(MemberRole memberRole : roles) {
						V3xOrgRole role = memberRole.getRole();
						if(allCommonRole.contains(String.valueOf(role.getId()))) {
							flag++;
						}
					}
					if(flag == 0) {
						remove.add(list.get(i));
					}
				}
			}
			list.removeAll(remove);
			return list;
		}else if(type == 2) {
			Comment userAffair = (Comment) list.get(0);
			CtpAffair affairFlag = affairManager.get(userAffair.getAffairId());
        	ColSummary summary = colManager.getSummaryById(affairFlag.getObjectId());
			if(summary.getState() == 3) {
				return list;
			}
			List remove = new ArrayList();
			for(int i = 0; i < list.size(); i++) {
				Comment comment = (Comment) list.get(i);
				CtpAffair affair = affairManager.get(comment.getAffairId());
				List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(affair.getActivityId());
				if(openModes.size() == 0) {

				}else {
					XkjtOpenMode openMode = openModes.get(0);
					int flag = 0;
					if(openMode.getRoleIds() == null) {
						return list;
					}
					//获取所有单位下有权限的角色id
					List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
					List<String> commonRole = new ArrayList<String>(Arrays.asList(openMode.getRoleIds().split(",")));
					List<String> allCommonRole = new ArrayList();
					for(String roleId : commonRole) {
						V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
						for(V3xOrgAccount account : accounts) {
							List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
							if(acountRole != null && acountRole.size()>0) {
								V3xOrgRole allRole = acountRole.get(0);
								allCommonRole.add(String.valueOf(allRole.getId()));
							}
						}
					}
					for(MemberRole memberRole : roles) {
						V3xOrgRole role = memberRole.getRole();
						if(allCommonRole.contains(String.valueOf(role.getId()))) {
							flag++;
						}
					}
					if(flag == 0) {
						remove.add(list.get(i));
					}
				}
			}
			list.removeAll(remove);
			return list;
		}
		return list;
	}
	@Override
	public boolean isOpenBySender(Long affairId)
			throws BusinessException {
		CtpAffair affair = affairManager.get(affairId);
		V3xOrgMember orgMember = orgManager.getMemberById(affair.getSenderId());
		List<MemberRole> roles = orgManager.getMemberRoles(orgMember.getId(), orgMember.getOrgAccountId());
		boolean isOpen = true;
		if(affair!=null){
			List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(affair.getActivityId());
			if(openModes!=null && openModes.size()>0){
				XkjtOpenMode openMode = openModes.get(0);
				if(openMode.getRoleIds() == null) {
					return isOpen;
				}
				//获取所有单位下有权限的角色id
				List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
				List<String> commonRole = new ArrayList<String>(Arrays.asList(openMode.getRoleIds().split(",")));
				List<String> allCommonRole = new ArrayList();
				for(String roleId : commonRole) {
					V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
					for(V3xOrgAccount account : accounts) {
						List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
						if(acountRole != null && acountRole.size()>0) {
							V3xOrgRole allRole = acountRole.get(0);
							allCommonRole.add(String.valueOf(allRole.getId()));
						}
					}
				}
				for(MemberRole memberRole : roles) {
					V3xOrgRole role = memberRole.getRole();
					if(!allCommonRole.contains(String.valueOf(role.getId()))) {
						isOpen = false;
					}
				}
			}
		}
		return isOpen;
	}
	
	@AjaxAccess
	@Override
	public Map<String, String> saveMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment)throws BusinessException {
		
		xkjtDao.saveMainAttachment(xkjtSummaryAttachment);
		return null;
	}

	@Override
	public XkjtSummaryAttachment findMainAttachmentBySummaryId(Long summaryId)
			throws BusinessException {
		XkjtSummaryAttachment xkjtSummaryAttachment =  xkjtDao.findMainAttachmentBySummaryId(summaryId);
		return xkjtSummaryAttachment;
	}

	@Override
	public void updateMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException {
		xkjtDao.updateMainAttachment(xkjtSummaryAttachment);
	}

	@Override
	public void saveXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue)
			throws BusinessException {
		xkjtDao.saveXkjtLeaderDaiYue(xkjtLeaderDaiYue);
		
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocId(Long edocId,Long leaderId)
			throws BusinessException {
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderDaiYueByEdocId(edocId,leaderId);
		return xkjtLeaderDaiYues;
	}

	/**项目：徐州矿物集团【待阅栏目：因使用的是JDBCAgent，所以将返回值改为Object】 作者：wxt.xiangrui 时间：2019-6-3 start*/
	@Override
	public List<Object> findXkjtLeaderDaiYueByMemberId(Long memberId) throws BusinessException {
		List<Object> xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderDaiYueByMemberId(memberId);
		return xkjtLeaderDaiYues;
	}
	/**项目：徐州矿物集团【待阅栏目：因使用的是JDBCAgent，所以将返回值改为Object】 作者：wxt.xiangrui 时间：2019-6-3 end*/

	
	@AjaxAccess
	@Override
	public void updateXkjtLeaderDaiYueByCondition(String id,String status) throws BusinessException {
		try{
			Date date = new Date();       
			//获取当前时间
			Timestamp nowTime = new Timestamp(date.getTime());
			
			List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = null;
			xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderDaiYueById(Long.valueOf(id));
			
			XkjtLeaderDaiYue xkjtLeaderDaiYue = xkjtLeaderDaiYues.get(0);
			xkjtLeaderDaiYue.setStatus(Integer.valueOf(status));
			/**项目：徐州矿物集团【添加了一个新字段SIGN_FOR_DATE，且阅读后添加进当前时间】 作者：wxt.xiangrui 时间：2019-6-3 start*/
			// status ： 11 为待阅  12 为已阅  13 为已撤销
			//只有状态改为已阅时才能添加签收日期
			if("12".equals(status)) {
				xkjtLeaderDaiYue.setSignForDate(nowTime);
			}
			/**项目：徐州矿物集团【添加了一个新字段SIGN_FOR_DATE，且阅读后添加进当前时间】 作者：wxt.xiangrui 时间：2019-6-3 end*/
			xkjtDao.updateXkjtLeaderDaiYue(xkjtLeaderDaiYue);
		}catch(Exception e){
			log.error("chenq---更新待阅件状态错误", e);
		}
	}
	
	public void updateXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException {
		try{
			xkjtDao.updateXkjtLeaderDaiYue(xkjtLeaderDaiYue);
		}catch(Exception e){
			log.error("chenq---更新待阅件状态错误", e);
		}
	}

	@AjaxAccess
	@Override
	public FlipInfo findMoreXkjtLeaderDaiYue(FlipInfo fi, Map params) throws BusinessException {
		User user = AppContext.getCurrentUser();
		params.put("leaderId",user.getId());
		xkjtDao.findMoreXkjtLeaderDaiYueByMemberId(fi, params);
		return fi;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueById(Long id) throws BusinessException {
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderDaiYueById(id);
		return xkjtLeaderDaiYues;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocIdAndSendId(Long sendRecordId, Long edocId) throws BusinessException {
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderDaiYueByEdocIdAndSendId(sendRecordId, edocId);
		return xkjtLeaderDaiYues;
	}




	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderYiYueByMemberId(Long memberId)
			throws BusinessException {
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = xkjtDao.findXkjtLeaderYiYueByMemberId(memberId);
		return xkjtLeaderDaiYues;
	}
	

	@AjaxAccess
	@Override
	public FlipInfo findMoreXkjtLeaderYiYue(FlipInfo fi, Map params) throws BusinessException {
		User user = AppContext.getCurrentUser();
		params.put("leaderId",user.getId());
		xkjtDao.findMoreXkjtLeaderYiYueByMemberId(fi, params);
		
		return fi;
	}

	
	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	@Override
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId)
			throws BusinessException {
		List<Object> xkjtLeaderBanJies = xkjtDao.findXkjtLeaderBanJieByMemberId(memberId);
		return xkjtLeaderBanJies;
	}
	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 end*/
	
	@Override
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId, String templetIds) throws BusinessException {
		List<Object> xkjtLeaderBanJies = xkjtDao.findXkjtLeaderBanJieByMemberId(memberId, templetIds);
		return xkjtLeaderBanJies;
	}
	
	/**项目：徐州矿物集团【办结栏目更多页】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	@AjaxAccess
	@Override
	public FlipInfo findMoreXkjtLeaderBanJie(FlipInfo fi, Map<String,Object> params) throws BusinessException {
		User user = AppContext.getCurrentUser();
		params.put("memberId",user.getId());
		xkjtDao.findMoreXkjtLeaderBanJieByMemberId(fi, params);
		return fi;
	}
	/**项目：徐州矿物集团【办结栏目更多页】 作者：wxt.xiangrui 时间：2019-5-29 end*/
		@Override
	@AjaxAccess
	public Map<String, Object> getEdocContentInfo(Long edocId) {
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			EdocSummary summary = edocManager.getEdocSummaryById(edocId, true);
			Long fileId = null;
			String type = null;
			if (summary != null) {
				Set<EdocBody> edocBodies = summary.getEdocBodies();
				for (EdocBody each : edocBodies) {
					if (each.getContentType().equals("OfficeWord")) {
						fileId = Long.valueOf(each.getContent());
						type = "word";
						break;
					} else if (each.getContentType().equals("WpsWord")) {
						fileId = Long.valueOf(each.getContent());
						type = "word";
						break;
					} else if (each.getContentType().equals("Pdf")) {
						fileId = Long.valueOf(each.getContent());
						type = "pdf";
						break;
					}
				}
			}
			if (fileId != null && Strings.isNotBlank(type)) {
				FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
				if (fileManager != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					V3XFile v3xFile = fileManager.getV3XFile(fileId);
					if (v3xFile != null) {
						data.put("fileId", fileId);
						data.put("date", sdf.format(v3xFile.getCreateDate()));
						data.put("name", summary.getSubject());
						data.put("type", type);
					}
				}
			}
		} catch (Exception e) {

		}
		return data;
	}
	
	@Override
	@AjaxAccess
	public Map<String, Object> judgeAffairIsLast(Map<String, Object> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		Long affairId = params.get("affairId") == null ? null : Strings.isBlank(params.get("affairId").toString()) ? null : Long.valueOf(params.get("affairId").toString());
		try {
			if (affairId != null) {
				CtpAffair affair = affairManager.get(affairId);
				if (affair != null && affair.getSubObjectId() != null) {
					WorkflowAjaxManager wfAjax = (WorkflowAjaxManager) AppContext.getBean("WFAjax");
					WorkflowBpmContext context = new WorkflowBpmContext();
					context.setProcessId(affair.getProcessId());
					context.setCaseId(affair.getCaseId());
					context.setCurrentActivityId(affair.getActivityId() + "");
					context.setCurrentWorkitemId(affair.getSubObjectId());
					context.setCurrentUserId(AppContext.currentUserId() + "");
					context.setCurrentAccountId(AppContext.currentAccountId() + "");
					context.setIsValidate(true);
					context.setDebugMode(false);
					CPMatchResultVO matchResultVO = new CPMatchResultVO();
					matchResultVO.setPop(false);
					matchResultVO.setToken("");
					matchResultVO.setLast("false");
					matchResultVO.setAlreadyChecked("false");
					matchResultVO.setAllNotSelectNodes(new HashSet<String>());
					matchResultVO.setAllSelectNodes(new HashSet<String>());
					matchResultVO.setAllSelectInformNodes(new HashSet<String>());
					CPMatchResultVO vo = wfAjax.transBeforeInvokeWorkFlow(context, matchResultVO);
					data.put("isLast", Boolean.valueOf(vo.getLast()));
					data.put("state", true);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return data;
	}

	@Override
	public EdocFormInfo getByFormId(Long formId) {
		return xkjtDao.getByFormId(formId);
	}

	@Override
	public void updateEdocFormInfo(EdocFormInfo info) throws BusinessException {
		xkjtDao.updateEdocFormInfo(info);
	}
	
	@Override
	public void saveEdocFormInfo(EdocFormInfo info) throws BusinessException {
		xkjtDao.saveEdocFormInfo(info);
	}
	
		@Override
	public boolean issendmessage(Long affairId,Long memberId)  throws BusinessException{
		
		//User user = AppContext.getCurrentUser();
		CtpAffair affair = affairManager.get(affairId);
		V3xOrgMember memberById = orgManager.getMemberById(memberId);
		List<MemberRole> roles = orgManager.getMemberRoles(memberId, memberById.getOrgAccountId());
		//List<CtpAffair> affairs = affairManager.getAffairs(affair.getObjectId());
		List<List<String>> modesList = new ArrayList();
		List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(affair.getActivityId());
		if(openModes.size() > 0 && openModes.get(0).getRoleIds() != null) {
			modesList.add(new ArrayList<String>(Arrays.asList(openModes.get(0).getRoleIds().split(","))));
		}
		if(modesList.size()>0) {
			List<String> commonRole = modesList.get(0);
			for(int i = 1;i<modesList.size();i++) {
				List<String> roleList = modesList.get(i);
				List<String> newList = new ArrayList();
				newList.addAll(commonRole);
				newList.removeAll(roleList);
				commonRole.removeAll(newList);
			}
			//获取所有单位下有权限的角色id
			List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
			List<String> allCommonRole = new ArrayList();
			for(String roleId : commonRole) {
				V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
				for(V3xOrgAccount account : accounts) {
					List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
					if(acountRole != null && acountRole.size()>0) {
						V3xOrgRole allRole = acountRole.get(0);
						allCommonRole.add(String.valueOf(allRole.getId()));
					}
				}
			}
			if(commonRole.size()>0) {
				for(MemberRole memberRole : roles) {
					V3xOrgRole role = memberRole.getRole();
					log.info("获取到的角色名称："+role.getName());
					log.info("获取到的角色Id："+role.getId());
					if(allCommonRole.contains(String.valueOf(role.getId()))) {
						//存在可访问权限
						return true;
					}
				}
			}else {
				//没有共同的访问权限
				return false;
			}
		}else {
			//都是公开权限
			return true;
		}
		//没有权限访问
		return false;
	}

	@Override
	@AjaxAccess
	public Map<String, String> saveNodeOpenModes(Map param) throws BusinessException {
		if(String.valueOf(param.get("nodeId")).equals("start")){
			return null;
		}
		
		log.info("zelda-进入加签保存");
		String nodeIdsStr = String.valueOf(param.get("nodeId"));
		String roleIds = String.valueOf(param.get("roleIds"));
		log.info("zelda-获取到加签节点id：" + nodeIdsStr + " 角色id:" + roleIds);
		if(Strings.isBlank(roleIds)) {
			log.error("zelda-没有角色id，不保存指定公开！");
			return null;
		}
		
		String[] nodeIdStrs = null;
		if(Strings.isNotBlank(nodeIdsStr) && !"null".equals(nodeIdsStr)) {
			nodeIdStrs = nodeIdsStr.split(",");
		}
		
		String[] roleIdArr = roleIds.replace("[", "").replace("]", "").replace("\"", "").split(",");
		String roleIdsStr = "";
		for (int j = 0; j < roleIdArr.length; j++) {
			if(j == 0){
				roleIdsStr += roleIdArr[j];
			}else{
				roleIdsStr += ","+roleIdArr[j];
			}
		}
		
		if(nodeIdStrs != null) {
			List<XkjtOpenMode> xkjtOpenModes = new ArrayList<XkjtOpenMode>();
			for(int i = 0; i < nodeIdStrs.length; i++) {
				if("".equals(nodeIdStrs[i])) {
					continue;
				}
				Long nodeId = Long.parseLong(nodeIdStrs[i]);
				try{
					XkjtOpenMode xkjtOpenMode = new XkjtOpenMode();
					xkjtOpenMode.setIdIfNew();
					xkjtOpenMode.setNodeId(nodeId);
					xkjtOpenMode.setRoleIds(roleIdsStr);
					xkjtOpenMode.setIsDeleted(0);
					xkjtOpenModes.add(xkjtOpenMode);
				}catch(Exception e){
					log.error("保存公开方式出错", e);
				}
			}
			xkjtDao.saveOpenModes(xkjtOpenModes);
		}
		return null;
	}

	@Override
	@AjaxAccess
	public Map<String, String> getDefaultRoll(Map param) throws BusinessException {
		Map<String, String> result = new HashMap<String, String>();
		User currentUser = AppContext.getCurrentUser();
		Long loginAccount = currentUser.getLoginAccount();
		String openRoleStr = AppContext.getSystemProperty("xkjt.openRole");
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		V3xOrgRole openRole = orgManager.getRoleByName(openRoleStr, currentUser.getLoginAccount());
		if(openRole != null) {
			result.put("code", "1");
			result.put("message", "success");
			result.put("roleIds", String.valueOf(openRole.getId()));
			result.put("roleNames", openRole.getName());
			return result;
		}else {
			result.put("code", "-1");
			result.put("message", "为获取到当前单位的" + openRoleStr + "角色");
			result.put("roleIds", "");
			result.put("roleNames", "");
			log.info("zelda-" + "为获取到当前单位的" + openRoleStr + "角色");
			return result;
		}
		
	}
	/**项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 start*/
	@Override
	public List getOpenOpinions1(List list,Long affairId, int type) throws BusinessException {
		User user = AppContext.getCurrentUser();
		List<MemberRole> roles = orgManager.getMemberRoles(user.getId(), user.getAccountId());
//		CtpAffair affair = affairManager.get(affairId);
//		if(affair.isFinish() || affair.getState() == 3) {
//			return list;
//		}
		if(list == null || list.size() == 0) {
			return list;
		}
		if(type == 1) {
			EdocOpinion userAffair = (EdocOpinion) list.get(0);
			CtpAffair affair = affairManager.get(userAffair.getAffairId());
			EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), true);
			CtpAffair affairs = affairManager.get(affairId);
			boolean ispending = false;
			if((affairs.getState()==3||affairs.getState()==1) && affairs.getMemberId().equals(user.getId())){
				ispending = true;
			}
			if(summary.getFinished()) {
				return list;
			}
			List remove = new ArrayList();
			for(int i = 0; i < list.size(); i++) {
				EdocOpinion edocOpinion = (EdocOpinion) list.get(i);
				List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(edocOpinion.getNodeId());
				if(openModes.size() == 0) {

				}else {
					XkjtOpenMode openMode = openModes.get(0);
					int flag = 0;
					if(openMode.getRoleIds() == null) {
						return list;
					}
					
					//获取所有单位下有权限的角色id
					List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
					List<String> allCommonRole = new ArrayList();
					List<String> commonRole = new ArrayList<String>(Arrays.asList(openMode.getRoleIds().split(",")));
					for(String roleId : commonRole) {
						V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
						for(V3xOrgAccount account : accounts) {
							List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
							if(acountRole != null && acountRole.size()>0) {
								V3xOrgRole allRole = acountRole.get(0);
								allCommonRole.add(String.valueOf(allRole.getId()));
							}
						}
					}
					for(MemberRole memberRole : roles) {
						V3xOrgRole role = memberRole.getRole();
						if(allCommonRole.contains(String.valueOf(role.getId()))) {
							flag++;
						}
					}
					
					if(flag == 0 && !ispending) {
						remove.add(list.get(i));
					}
				}
			}
			list.removeAll(remove);
			return list;
		}else if(type == 2) {
			Comment userAffair = (Comment) list.get(0);
			CtpAffair affairFlag = affairManager.get(userAffair.getAffairId());
        	ColSummary summary = colManager.getSummaryById(affairFlag.getObjectId());
        	CtpAffair affairs = affairManager.get(affairId);
			boolean ispending = false;
			if((affairs.getState()==3||affairs.getState()==1) && affairs.getMemberId().equals(user.getId())){
				ispending = true;
			}
			if(summary.getState() == 3) {
				return list;
			}
			List remove = new ArrayList();
			for(int i = 0; i < list.size(); i++) {
				Comment comment = (Comment) list.get(i);
				CtpAffair affair = affairManager.get(comment.getAffairId());
				List<XkjtOpenMode> openModes = xkjtDao.findOpenModeByNodeId(affair.getActivityId());
				if(openModes.size() == 0) {

				}else {
					XkjtOpenMode openMode = openModes.get(0);
					int flag = 0;
					if(openMode.getRoleIds() == null) {
						return list;
					}
					//获取所有单位下有权限的角色id
					List<V3xOrgAccount> accounts = orgManager.getAllAccounts();
					List<String> commonRole = new ArrayList<String>(Arrays.asList(openMode.getRoleIds().split(",")));
					List<String> allCommonRole = new ArrayList();
					for(String roleId : commonRole) {
						V3xOrgRole orgRole = orgManager.getRoleById(Long.parseLong(roleId));
						for(V3xOrgAccount account : accounts) {
							List<V3xOrgRole> acountRole = orgManager.getRoleByCode(orgRole.getCode(), account.getId());
							if(acountRole != null && acountRole.size()>0) {
								V3xOrgRole allRole = acountRole.get(0);
								allCommonRole.add(String.valueOf(allRole.getId()));
							}
						}
					}
					for(MemberRole memberRole : roles) {
						V3xOrgRole role = memberRole.getRole();
						if(allCommonRole.contains(String.valueOf(role.getId()))) {
							flag++;
						}
					}
					if(flag == 0 && !ispending) {
						remove.add(list.get(i));
					}
				}
			}
			list.removeAll(remove);
			return list;
		}
		return list;
	}
	/**项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 start*/
}
