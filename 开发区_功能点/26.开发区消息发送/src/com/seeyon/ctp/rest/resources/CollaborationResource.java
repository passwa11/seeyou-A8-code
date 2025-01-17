/**
 * Author : xuqw
 *   Date : 2015年11月19日 下午5:49:20
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.ai.event.AIRemindEvent;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.BackgroundDealResult;
import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.DeleteAjaxTranObj;
import com.seeyon.apps.collaboration.bo.FormLockParam;
import com.seeyon.apps.collaboration.bo.LockObject;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.BackgroundDealType;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColListType;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.manager.ColIndexEnableImpl;
import com.seeyon.apps.collaboration.manager.ColLockManager;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColOpenFromUtil;
import com.seeyon.apps.collaboration.util.ColOpenFromUtil.ColSummaryType;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.ColListSimpleVO;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.multicall.api.MultiCallApi;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.apps.taskmanage.util.MenuPurviewUtil;
import com.seeyon.apps.xiaoz.bo.card.XiaozCommondCard;
import com.seeyon.apps.xiaoz.bo.card.XiaozListCard;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.batch.bo.BatchResult;
import com.seeyon.ctp.common.batch.manager.BatchManager;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.Plugins;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.datarelation.api.DataRelationApi;
import com.seeyon.ctp.common.datarelation.manager.DataRelationHandler;
import com.seeyon.ctp.common.datarelation.po.DataRelationPO;
import com.seeyon.ctp.common.datarelation.po.json.ProjectConfigs;
import com.seeyon.ctp.common.datarelation.vo.BaseConfigVO;
import com.seeyon.ctp.common.datarelation.vo.ProjectConfigVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.permission.bo.CustomAction;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.CtpPermissionOperationManager;
import com.seeyon.ctp.common.permission.manager.PermissionLayoutManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionMiniVO;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.trace.api.TraceWorkflowManager;
import com.seeyon.ctp.common.track.bo.TrackAjaxTranObj;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.handover.constants.HandoverConstant;
import com.seeyon.ctp.handover.api.HandoverManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.plugins.resources.CollaborationPluginUtils;
import com.seeyon.ctp.plugins.resources.PluginResourceLocation;
import com.seeyon.ctp.plugins.resources.PluginResourceScope;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.portlet.ImagePortletLayout;
import com.seeyon.ctp.report.engine.api.ReportConstants.UserConModel;
import com.seeyon.ctp.report.engine.api.manager.ReportResultApi;
import com.seeyon.ctp.rest.resources.vo.ColForwordCommentVO;
import com.seeyon.ctp.rest.resources.vo.CollGotoParamsVO;
import com.seeyon.ctp.rest.resources.vo.CollQueryParamsVO;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.common.security.SecurityCheckParam;
import com.seeyon.v3x.system.util.WaterMarkUtil;

import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;

/**
 * <p>Title       : 协同Rest接口</p>
 * <p>Description : 协同Rest接口</p>
 * <p>Copyright   : Copyright (c) 2015</p>
 * <p>Company     : seeyon.com</p>
 */
@Path("coll")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces(MediaType.APPLICATION_JSON)
public class CollaborationResource  extends BaseResource {
    private static final Log    LOGGER             = CtpLogFactory.getLog(CollaborationResource.class);
    
    /* 错误码的Key */
    private static final String ERROR_KEY          = "error_msg";
    private static final String INFO_KEY          = "info_msg";
    private static final String JSON_PARAMS        = "_json_params";

    private static final String SUCCESS_KEY        = "success"; 
    private static final String SUCCESS_VALUE_TRUE        = "true";
    private static final String SUCCESS_VALUE_FALSE        = "false";
    
    private static final String ICON_TYPE_MEMBER    = "Member";
    
    /** 全部意见 **/
    private static final String COMMENT_TYPE_ALL = "all";
    /** 点赞意见 **/
    private static final String COMMENT_TYPE_LIKE = "like";
    /** 同意意见 **/
    private static final String COMMENT_TYPE_AGREE = "agree";
    /** 不同意意见 **/
    private static final String COMMENT_TYPE_DISAGREE = "disagree";
    
    
    
    private ColLockManager      colLockManager     = (ColLockManager) AppContext.getBean("colLockManager");
    private ColManager          colManager         = (ColManager) AppContext.getBean("colManager");
    private CollaborationApi    collaborationApi   = (CollaborationApi) AppContext.getBean("collaborationApi");
    private AffairManager       affairManager      = (AffairManager) AppContext.getBean("affairManager");
    private OrgManager          orgManager         = (OrgManager) AppContext.getBean("orgManager");
    private AttachmentManager   attachmentManager  = (AttachmentManager) AppContext.getBean("attachmentManager");
    private MainbodyManager     ctpMainbodyManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
    private TemplateManager     templateManager    = (TemplateManager) AppContext.getBean("templateManager");
    private CommentManager      commentManager     = (CommentManager) AppContext.getBean("ctpCommentManager");
    private FileManager         fileManager        = (FileManager) AppContext.getBean("fileManager");
    private PermissionManager   permissionManager  = (PermissionManager) AppContext.getBean("permissionManager");
    private SuperviseManager    superviseManager   = (SuperviseManager) AppContext.getBean("superviseManager");
    private WorkflowApiManager  wapi               = (WorkflowApiManager) AppContext.getBean("wapi");
    private DocApi              docApi             = (DocApi) AppContext.getBean("docApi");
    private EnumManager         enumManager        = (EnumManager)AppContext.getBean("enumManagerNew");
    private SystemConfig        systemConfig       = (SystemConfig)AppContext.getBean("systemConfig");
    private CustomizeManager    customizeManager   = (CustomizeManager)AppContext.getBean("customizeManager");
    private MenuPurviewUtil     menuPurviewUtil    = (MenuPurviewUtil)AppContext.getBean("menuPurviewUtil");
    private CtpTrackMemberManager   trackManager   = (CtpTrackMemberManager)AppContext.getBean("trackManager");
    private BatchManager        batchManager       = (BatchManager)AppContext.getBean("batchManager");
    private ColIndexEnableImpl  colIndex           = (ColIndexEnableImpl)AppContext.getBean("colIndex");
    private MultiCallApi        multiCallApi       = (MultiCallApi) AppContext.getBean("multiCallApi");
    private DataRelationApi dataRelationApi = (DataRelationApi)AppContext.getBean("dataRelationApi");
    private DataRelationHandler dataRelationHandler = (DataRelationHandler) AppContext.getBean("colDataRelationHandler");
    private ProjectApi          projectApi         = (ProjectApi) AppContext.getBean("projectApi");
    private UserMessageManager  userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
    private CAPFormManager capFormManager = (CAPFormManager)AppContext.getBean("capFormManager");
    private DataRelationHandler colDataRelationHandler = (DataRelationHandler)AppContext.getBean("colDataRelationHandler");
	private TraceWorkflowManager traceWorkflowManager = (TraceWorkflowManager)AppContext.getBean("traceWorkflowManager");
	private PermissionLayoutManager permissionLayoutManager = (PermissionLayoutManager)AppContext.getBean("permissionLayoutManager");
    private CtpPermissionOperationManager ctpPermissionOperationManager =  (CtpPermissionOperationManager)AppContext.getBean("ctpPermissionOperationManager");
    private PortalApi portalApi = (PortalApi)AppContext.getBean("portalApi");
 //   private CollaborationPluginManager collaborationPluginManager = (CollaborationPluginManager)AppContext.getBean("collaborationPluginManager");
    private HandoverManager handoverManager = (HandoverManager)AppContext.getBean("handoverManager");
    private ReportResultApi reportResultApi = (ReportResultApi)AppContext.getBean("reportResultApi");
    
    
    /**
     * 获取协同详细展现信息
     * @param openFrom	String | 必填 | 打开来源
     * <pre>
     * listPending - 待办列表
     * listDone - 已办列表
     * listSent - 已发列表
     * listWaitSent - 待发列表
     * formStatistical, //表单统计
     * formQuery,//表单查询
     * formRelation,//表单关联穿透
     * docLib -文档中心
     * 参考枚举：com.seeyon.apps.collaboration.enums.ColOpenFrom
     * </pre>
     * @param affairId	Long | 必填 | 事项ID
     * @param summaryId	Long | 必填 | 协同ID
     * @param pigeonholeType Sting | 非必填 | 预归档（值：0）/ 普通的操作归档（值：1）， 参考 {@link com.seeyon.apps.doc.constants.DocConstants.PigeonholeType} 
     * @param operationId  String | 非必填 | 表单操作权限，表单制作的时候设置,等同于affair表中的form_operation_id
     * 
     * @param docResId String | 非必填 | 文档ID，用于权限验证 ,归档目录下当前文档的id
     * @param baseObjectId String | 非必填 | 关联文档属于的数据ID，协同的话是summaryId,会议的话是meetingId 等等
     * @param baseApp String | 非必填 | 关联文档属于的数据所在的模块 ，1：协同，6：会议，4：公文，3：知识管理 等等
     * @param designId String | 非必填 | CAP4 查询穿透， 校验权限使用的参数
     * 
     * @return	map
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("summary/{openFrom}/{affairId}/{summaryId}")
    public Response summary(@PathParam("openFrom") String openFrom, 
                            @PathParam("affairId") Long affairId, 
                            @PathParam("summaryId") Long summaryId,
                            @QueryParam("pigeonholeType") String pigeonholeType,
                            @QueryParam("operationId") String operationId,
            
                            //权限相关的参数
                            @QueryParam("docResId") String docResId,
                            @QueryParam("baseObjectId") String baseObjectId,
                            @QueryParam("baseApp") String baseApp,
                            @QueryParam("taskId") String taskId,
                            @QueryParam("designId") String designId) throws BusinessException {

        User user = AppContext.getCurrentUser();
        fixUser(user);
        
        boolean isHistory = false;
        ColSummary summary =  null;
        CtpAffair affair = null;
        
        String strSummaryId = summaryId == null ? null : summaryId.toString();
        summary = colManager.getMainSummary4FormQueryAndStatic(openFrom, strSummaryId);
        if(summary != null){
            summaryId = summary.getId();
            strSummaryId = summaryId.toString();
        }
        
        if(null == summary && affairId == -1l && "listPending".equals(openFrom) && null != summaryId){
        	summary = colManager.getSummaryById(summaryId);
        	Map<String, Object> findSourceInfo = colIndex.findSourceInfo(summaryId);
        	Object object = findSourceInfo.get("sourceId");
        	if(null!= object){
        		affairId = (Long)object;
        	}
        }
        
        ColSummaryVO colSummaryVO = new ColSummaryVO();
        colSummaryVO.setSummaryId(strSummaryId);
        
        //兼容只有summaryId的场景
        if(Long.valueOf(-1).equals(affairId)){
            colSummaryVO.setAffairId(null);
        }else{
            colSummaryVO.setAffairId(affairId);
        }

        //summaryVO.setProcessId
        isHistory =  colManager.getDisplayData2VO(colSummaryVO, summary, affair, isHistory);
        summary = colSummaryVO.getSummary();
        affair = colSummaryVO.getAffair();
        
        //经过兼容后， affairId可能已经被替换
        if(affair != null){
            affairId = affair.getId();
        }
        
        if(affair==null && summaryId != -1) {
            //文档中心、关联文档、督办
            if(ColOpenFrom.docLib.name().equals(openFrom) 
                    || ColOpenFrom.glwd.name().equals(openFrom) 
                    || ColOpenFrom.supervise.name().equals(openFrom)
                    || ColOpenFrom.repealRecord.name().equals(openFrom)) {
            	
                isHistory = false;
                Long aId = affairId;
                if(aId == null || aId.intValue() == -1){
                    aId = summaryId;
                }
                affair = affairManager.get(aId);
                if(affair == null && AppContext.hasPlugin("fk")){
                    isHistory = true;
                    affair = affairManager.getByHis(aId);
                }
            }else if(ColOpenFrom.formRelation.name().equals(openFrom)
            	  || ColOpenFrom.formQuery.name().equals(openFrom)){
            	affair = affairManager.getSenderAffair(summaryId);
            	if(affair == null  && AppContext.hasPlugin("fk")){
            		affair = affairManager.getSenderAffairByHis(summaryId);
            		if(affair != null){
            			isHistory = true;
            		}
            	}
            }else{
                
                if (affair == null) {
                    isHistory = false;
                    affair = affairManager.getSenderAffair(affairId);
                    if(affair == null){
                        isHistory = true;
                        affair = affairManager.getSenderAffairByHis(affairId);
                    }
                }
            }
            
        } else {
            if(affair != null && !ColOpenFrom.docLib.name().equals(openFrom) 
                    && !ColOpenFrom.glwd.name().equals(openFrom) 
                    && !ColOpenFrom.supervise.name().equals(openFrom)
                    && !ColOpenFrom.repealRecord.name().equals(openFrom)
                    && !ColOpenFrom.capQuery.name().equals(openFrom)
                    && !ColOpenFrom.formRelation.name().equals(openFrom)) {
                
                
                Map<String, String> errorRet = new HashMap<String, String>();
                if(!this.isValidAffair(affair, errorRet,affair.getNodePolicy())) {
                    
                    return ok(errorRet);
                }
            }
        }
        
        //到这里affair 一定不能为null 
        if(affair != null && summary == null){
            
            if(isHistory){
                summary = colManager.getColSummaryByIdHistory(affair.getObjectId());
            }else{
                summary = colManager.getColSummaryById(affair.getObjectId());
            }
        }
        
      //拼装内容
        Map<String, Object> map = new HashMap<String, Object>();
        
        //基本数据校验
        Map<String,String> errorMap = new HashMap<String,String>();
        if(!ColOpenFrom.repealRecord.name().equals(openFrom) 
                && !ColOpenFrom.formRelation.name().equals(openFrom)
                && !ColOpenFrom.capQuery.name().equals(openFrom)
                && !ColOpenFrom.capStatistical.name().equals(openFrom)
                && (summary == null || affair == null || !isValidAffair(affair, errorMap,affair.getNodePolicy()))){
            
            String errorMsg = ColUtil.getErrorMsgByAffair(affair);
            errorMap.put(ERROR_KEY, errorMsg);
            return ok(errorMap);
        }
        
        // 协同被删除了
        if(affair == null){
            String errorMsg = ColUtil.getErrorMsgByAffair(affair);
            errorMap.put(ERROR_KEY, errorMsg);
            return ok(errorMap);
        }
        
        //权限校验
        SecurityCheckParam param = new SecurityCheckParam(ApplicationCategoryEnum.collaboration, user, affair.getId());
        param.setAffair(affair);
        param.addExt("openFrom", openFrom);
        param.addExt("docResId", docResId);
        param.addExt("baseObjectId", baseObjectId);
        param.addExt("baseApp", baseApp);
        param.addExt("taskId", taskId);
        param.addExt("designId", designId);
        
        /*param.addExt("preArchiveId", preArchiveId);
        param.addExt("fromEditor", request.getParameter("fromEditor"));
        param.addExt("eventId", request.getParameter("eventId"));
        param.addExt("relativeProcessId", request.getParameter("relativeProcessId"));*/
        
        param.addExt("col_Summary",summary);
        SecurityCheck.isLicit(param);
        if(!param.getCheckRet()){
            
            //协同驾驶舱统计， A统计B的数据， A和B在同一个流程里面，提示A无权查看B的事项，这里做一次防护
            //解决一种场景：在流程中，但是打开的不是自己的事项，以前在SecurityControlColImpl中有判断是否在流程中的逻辑，虽然返回true，但是打开的不是自己的事项，存在权限泄漏，
            //放在外边来，可以解决这种情况，始终打开自己的事项
            CtpAffair aff = findMemberAffair(affair.getObjectId(), isHistory, user.getId());
            if(aff != null){
                affair = aff;
                colSummaryVO.setAffair(aff);
                colSummaryVO.setAffairId(aff.getId());
            }else{
                
                String msg = param.getCheckMsg();
                if(Strings.isBlank(msg)){
                    msg = ResourceUtil.getString("collaboration.common.unauthorized");//越权访问
                }
                errorMap.put(ERROR_KEY, msg);
                return ok(errorMap);
            }
        }
        
        //summary 基本属性 
        ColSummaryDetailVO summaryVO = ColSummaryDetailVO.valueOf(summary);
        summaryVO.setActivityId(affair.getActivityId());
        summaryVO.setAffairWorkitemId(affair.getSubObjectId());
        summaryVO.setAffairId(affair.getId());
        summaryVO.setAffairIsDelete(affair.isDelete());
        summaryVO.setHasFavorite(affair.getHasFavorite() ? "1" : "0");
        //附件
        List<Attachment> attachments = attachmentManager.getByReference(summary.getId(), summary.getId());
        summaryVO.setAttachments(attachments);

        //相关数据所需数据
        summaryVO.setDr(affair.getRelationDataId() == null ? "" : String.valueOf(affair.getRelationDataId()));

        //affair状态
        summaryVO.setAffairState(affair.getState());
        summaryVO.setAffairSubState(affair.getSubState());
        summaryVO.setAffairTrack(affair.getTrack());
        //affair对应的当前节点id
        summaryVO.setActivityId(affair.getActivityId());
        
        CtpTemplate template = null;
        if(null != summary.getTempleteId()){
        	
            template = templateManager.getCtpTemplate(summary.getTempleteId());
        	if(null!=template){
        		boolean isSpecialSteped = affair != null && affair.getSubState() == SubStateEnum.col_pending_specialBacked.key();
    			if (template != null && template.getWorkflowId() != null) { // 系统模板 // & // 个人模板
    				if (!isSpecialSteped && TemplateUtil.isSystemTemplate(template) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState())) {
    					summaryVO.setTemplateProcessId(template.getWorkflowId());
    					summaryVO.setProcessId(null);
    				}
    			}
        		if (template.isSystem()) {
        		    summaryVO.setSystemTemplate(true);
        		}
        		if(template.isSystem() && template.getWorkflowId() != null){
        			summaryVO.setProcessTemplate(true);
        		}
        		//扫码
        		summaryVO.setCanScanCode(null == template.getScanCodeInput() ? "0" : template.getScanCodeInput() ? "1" :"0");
        		//点赞
        		summaryVO.setCanPraise(template.getCanPraise());
        	}
        	
            if (ColUtil.isForm(summary.getBodyType()) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState())
                    && (template == null || Boolean.TRUE.equals(template.isDelete()))) {
                /*
                                                     * 因为表单模板在待发中，要去读流程模板绑定的表单权限、但是如果模板被删除，ProcessTemplate被物理删除，
                                                     * 所以这里防护一下。 后续如果processTemplate改为逻辑删除，这个IF就可以干掉了
                 */
                errorMap.put(ERROR_KEY, ResourceUtil.getString("workflow.wapi.exception.msg001"));
                return ok(errorMap);
            }
        }
        //正文设置
		boolean isBlank = Strings.isBlank(pigeonholeType) || "null".equals(pigeonholeType) ||"undefined".equals(pigeonholeType);
		pigeonholeType = isBlank ? String.valueOf(PigeonholeType.edoc_account.ordinal()) : pigeonholeType;
		 
		Long fileId = 0l;
        ColSummaryContentVO contentVO = findSummaryContent(summary, affair, template,openFrom, pigeonholeType, operationId);
        if(contentVO!=null && ColUtil.isForm(summary.getBodyType())) {
        	summaryVO.setFormAppId(summary.getFormAppid());
        	summaryVO.setFormRecordId(summary.getFormRecordid());
        	summaryVO.setRightId(contentVO.getRightId());
        	
        	//多视图兼容
        	if(Strings.isNotBlank(contentVO.getFormRightId())){
        		summaryVO.setRightId(contentVO.getFormRightId());
        	}
        	
        	if(AffairUtil.isFormReadonly(affair)) {
            	summaryVO.setAffairReadOnly(Boolean.TRUE);
            }
        	
        	List<Map<String,String>> contentList = contentVO.getContentList();
        	if(Strings.isNotEmpty(contentList)) {
        		Map<String,String> contentMap = new HashMap<String, String>();
        		Iterator it = contentList.iterator();
        		while(it.hasNext()) {
        			contentMap = (Map<String, String>) it.next();
        			if(contentMap.get("isOffice")=="true") {
        				if(Strings.isDigits(contentMap.get("fileId"))) {
        					fileId = Long.parseLong(contentMap.get("fileId"));
        				}
        				break;
        			}
        		}
        	}
        	
        }else if(contentVO != null && contentVO.getContentDataId() != null) {
        	fileId = contentVO.getContentDataId();
        }
        boolean allowTrans = false;
      //office正文是否能正常转换
        if(!fileId.equals(0l)) {
        	V3XFile file = fileManager.getV3XFile(fileId);
        	allowTrans = OfficeTransHelper.allowTrans(file);	
        }
        map.put("allowTrans", allowTrans);
        int viewState = CtpContentAllBean.viewState_readOnly;
        if(ColUtil.isForm(String.valueOf(affair.getBodyType()))
                && Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())
                && !"inform".equals(ColUtil.getPolicyByAffair(affair).getId())
                && !AffairUtil.isFormReadonly(affair)
                && !AffairUtil.isSuperNode(affair)
                && !ColOpenFrom.glwd.name().equals(openFrom)
                && !ColOpenFrom.listDone.name().equals(openFrom)){//isFormReadonly表单只读
        	
        	 viewState = CtpContentAllBean.viewState__editable;
        	 
        }
        
        summaryVO.setNodePolicy(affair.getNodePolicy());
        summaryVO.setCanModify(summary.getCanModify() != null && summary.getCanModify() ? "true" : "false");
        summaryVO.setCanForward(summary.getCanForward() != null && summary.getCanForward() ? "1" : "0");
        summaryVO.setCanArchive((canArchive(user) && summary.getCanArchive() != null && summary.getCanArchive()) ? "1" : "0");
        summaryVO.setCanDeleteNode(summary.getCanDeleteNode() == null ? true : summary.getCanDeleteNode());
        
        //发起人附言, 不分页
        List<ColSummaryCommentVO> senderCommonts =  summarySenderComments(summary.getId(), isHistory);
        
        List<ColForwordCommentVO> forwordCommentList = null;
        if(Strings.isNotBlank(summary.getForwardMember())){
          //转发附言
            Map<String, Object> other = new HashMap<String, Object>();
            other.put("forwardCount_ne", Integer.valueOf(0));
            FlipInfo forwordCommontListFip = commentManager.findComments(ModuleType.collaboration, summary.getId(), Comment.CommentType.comment, null, other, false, isHistory);
            List<Comment> forwordCommontList = forwordCommontListFip.getData();
            forwordCommentList = ColForwordCommentVO.valueOf(forwordCommontList, commentManager);
        }else{
            forwordCommentList = Collections.emptyList();
        }
        
        //页面设置参数， 界面显示操作等权限参数设置
        Map<String, Object> pageConfig = new HashMap<String, Object>();
        Map<String, Object> draftCommentMap = new HashMap<String, Object>();
        
        boolean isTemplete = false;
        if (template != null && template.isSystem()) {
            isTemplete = true;
        }
        //设置节点权限
        affairNodeProperty(pageConfig, openFrom, affair, summary, user, isTemplete);
        
        SeeyonPolicy currentPolicy = null;
        Boolean noFindPermission =  (Boolean) pageConfig.get("noFindPermission");
        if(noFindPermission != null && noFindPermission){
            map.put(INFO_KEY, ResourceUtil.getString("collaboration.summary.noFindNode"));
            String newPermission = (String)pageConfig.get("newPermission");
            currentPolicy = new SeeyonPolicy(newPermission, BPMSeeyonPolicy.getShowName(newPermission));
        }else{
            currentPolicy = ColUtil.getPolicyByAffair(affair);
        }
        
        //当前节点权限
        map.put("currentPolicy", currentPolicy);
        map.put("subState", affair.getSubState());
        
        
        //意见草稿
        boolean canDeal = (Boolean)pageConfig.get("canDeal");
        summaryDraftComment(draftCommentMap, affair, summary, canDeal);
        
        //设置协同已读
        if(!isHistory){
        	colManager.updateAffairStateWhenClick(affair);
        }
        
        Long flowPermAccountId = summary.getOrgAccountId();
        if(flowPermAccountId == null){
            flowPermAccountId = user.getLoginAccount();
        }
        
        //默认节点权限
        PermissionVO defPermission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(), flowPermAccountId);
        
        //默认个人设置
        String trackProcess = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_PROCESS);
        if("false".equals(trackProcess)){
        	trackProcess = (Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack()) || affair.getTrack() == null) ? "false" : "true";
        }
       
        String listType = ColOpenFromUtil.getListType(openFrom);
        
        if(!ColSummaryType.listPending.name().equals(listType) 
                || affair.getState() != StateEnum.col_pending.key()) {
            summaryVO.setCanScanCode("0");
        }   
        
        if(affair.getState().intValue()==StateEnum.col_done.key()) {//如果从待办中打开已经处理过的公文
            if(ColSummaryType.listPending.name().equals(listType)) {
                listType = ColSummaryType.listDone.name();
            }   
        }
        
        if((!user.getId().equals(summary.getStartMemberId()) 
                && Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()))
                || affair.isDelete()){
        	//打开已发事项，但是当前用户不是发起人的时候的时候不能操作，例如督办
        	listType = ColOpenFromUtil.ColSummaryType.onlyView.name();
        }
        
        //设置打开权限参数
        summaryVO.setListType(listType);
        summaryVO.setIsCanComment(ColOpenFromUtil.isCanComment(openFrom));
        //inInSpecialSB为false 就是 处于指定回退状态 
        boolean inInSpecialSB = false;
    	if(StateEnum.col_pending.getKey() == affair.getState()) {
    		if(SubStateEnum.col_pending_specialBack.getKey() != affair.getSubState() &&
    			SubStateEnum.col_pending_specialBacked.getKey() !=affair.getSubState() &&
    			SubStateEnum.col_pending_specialBackCenter.getKey() != affair.getSubState()){//15 16 17
    			if(summary.getCaseId() != null) {
    			    //接口返回的是是否可以回退，参数命名上就是取非，不能回退则是在指定回退状态
    			    inInSpecialSB = !wapi.isInSpecialStepBackStatus(summary.getCaseId(), isHistory);
    			}
    		}
    	}

        //设置头部存在单位转发时显示单位简称
        summaryVO.setStartMemberName(Functions.showMemberName(summary.getStartMemberId()));
        //新建节点权限
        NodePolicyVO newPolicy = colManager.getNewColNodePolicy(affair.getOrgAccountId());
        //判断是否有新建权限
        boolean isHaveNewColl = MenuPurviewUtil.isHaveNewColl(user);
        
        if(!summaryVO.isFinished() && summaryVO.getState() != null){
            summaryVO.setFinished(CollaborationEnum.flowState.terminate.ordinal() == summaryVO.getState()
                    || CollaborationEnum.flowState.finish.ordinal() == summaryVO.getState());
        }
        
        //处理列表数量设置
        Map<String, String> countsMap = new HashMap<String, String>(2);
        
        if(isHistory){
        	Map<String,Integer> cntMap = affairManager.getAffairsStateCntHis(summary.getId(),ApplicationCategoryEnum.collaboration);
        	countsMap.put("all", cntMap.get("all") == null ? "" : cntMap.get("all").toString());
        	countsMap.put("running", cntMap.get("running") == null ? "" : cntMap.get("running").toString());
        }else{
        	Map<String,Integer> cntMap = affairManager.getAffairsStateCnt(summary.getId(),ApplicationCategoryEnum.collaboration);
        	countsMap.put("all", cntMap.get("all") == null ? "" : cntMap.get("all").toString());
        	countsMap.put("running", cntMap.get("running") == null ? "" : cntMap.get("running").toString());
        }
		/*Map<String,Object> params = new HashMap<String,Object>();
		List<Integer> states = new ArrayList<Integer>(2);
		states.add(StateEnum.col_pending.getKey());
		params.put("delete", Boolean.valueOf(false));
		params.put("app", ApplicationCategoryEnum.collaboration.getKey());
		params.put("objectId", summary.getId());
		params.put("state", states);
		int count1 = 0;
		int count2 = 0;
		if(isHistory){
		    count1 = affairManager.getCountByConditionsHis(params);
		    states.add(StateEnum.col_done.getKey());
		    count2 = affairManager.getCountByConditionsHis(params);
		}else{
			count1 = affairManager.getCountByConditions(params);
			states.add(StateEnum.col_done.getKey());
			count2 = affairManager.getCountByConditions(params);
		}
		
		
		countsMap.put("all", String.valueOf(count2));
		countsMap.put("running", String.valueOf(count1));*/
        map.put("affairCount", countsMap);

        summaryVO.setHasWorkFlowAdvance(AppContext.hasPlugin("workflowAdvanced"));
        map.put("trackProcess", trackProcess);
        map.put("summary", summaryVO);
        map.put("isHaveNewColl", isHaveNewColl);
        map.put("content", contentVO);
        map.put("senderCommonts", senderCommonts);
        map.put("pageConfig", pageConfig);
        map.put("draftComment", draftCommentMap);
        map.put("forwordCommentList", forwordCommentList);
        map.put("currentUser", CurrentUserInfoVO.valueOf(user));
        map.put("defPolicy", PermissionMiniVO.valueOf(defPermission));
        map.put("likeCommentCount", getCommentCount(affair.getObjectId(), COMMENT_TYPE_LIKE, isHistory));
        map.put("allCommentCount", getCommentCount(affair.getObjectId(), COMMENT_TYPE_ALL, isHistory));
        map.put("newPolicy", newPolicy);
        map.put("inInSpecialSB", inInSpecialSB);
        map.put("isHistory", isHistory);
        map.put("affairMemberId", affair.getMemberId());
        
        map.put("cancelOpinionPolicy", String.valueOf(defPermission.getCancelOpinionPolicy()));
        map.put("_viewState", viewState);
        map.put("SystemCurrentTimeMillis", System.currentTimeMillis());
        
        Map<String, Object> map_workflowCheck = new HashMap<String, Object>();
        map_workflowCheck.put("formAppId", summary.getFormAppid());
        map_workflowCheck.put("formViewOperation", affair.getMultiViewStr());
        map.put("workflowCheckParam", map_workflowCheck);
        
        //表单高级
        if(AffairUtil.isFormReadonly(affair)){
        	map.put("deeReadOnly", 1);
        }else{
        	map.put("deeReadOnly", 0);
        }
        
        //表单不能提交
        if(viewState == CtpContentAllBean.viewState_readOnly) {
        	map.put("formCanSubmit", 0);
        } else {
        	map.put("formCanSubmit", 1);
        }
    
        //如果是模板，处理流程追溯配置情况
        map.put("canTrackWorkflow", "0");
        //编辑office正文需要这个参数
        map.put("createDate", Datetimes.format(summary.getCreateDate(), "yyyy-MM-dd"));
        if(template != null){
        	if(!Integer.valueOf(0).equals(template.getCanTrackWorkflow())){
        		map.put("canTrackWorkflow", template.getCanTrackWorkflow());
        	}
        	//节点描述
        	if(Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())){
        		String nodeDesc =  wapi.getBPMActivityDesc(template.getWorkflowId(),String.valueOf(affair.getActivityId()));
        		map.put("nodeDesc", nodeDesc);
        	}
        }
        //新建协同的时候是否选择修改正文权限
        map.put("canEditContent", summary.getCanEdit());
        
        //是否已收藏
    	Map<String,String> para = new HashMap<String,String>();
    	para.put("checkBtns", "Favorite");
    	para.put("affairId", String.valueOf(affair.getId()));
    	Map<String, String> m = colManager.showMoreBtn(para);
    	if("true".equals(m.get("collect"))){
    		map.put("isFavorite", true);
    	}
        //添加表单锁
        map.put("formIsLock", false);
        if(canDeal && ColUtil.isForm(summaryVO.getBodyType())){
            Map<String, String> lockParam = new HashMap<String, String>();
            if(summaryVO.getFormAppId() != null){
                lockParam.put("formAppId", summaryVO.getFormAppId().toString());
            }
            if(summaryVO.getFormRecordId() != null){
                lockParam.put("formRecordId", summaryVO.getFormRecordId().toString());
            }
            lockParam.put("rightId", summaryVO.getRightId());
            lockParam.put("affairId", summaryVO.getAffairId().toString());
            lockParam.put("affairState", summaryVO.getAffairState().toString());
            lockParam.put("nodePolicy", summaryVO.getNodePolicy());
            lockParam.put("affairReadOnly", summaryVO.getAffairReadOnly().toString());
            LockObject lockObject = lockForm(lockParam);
            if("0".equals(lockObject.getCanSubmit())){
                map.put("formIsLock", true);
                map.put("formLockMsg", ResourceUtil.getString("collaboration.common.flag.editingForm", lockObject.getLoginName(), lockObject.getFrom()));
            }
        }
        
        // 流程预测参数
        boolean canExePrediction = false;
        if(ColUtil.isForm(summaryVO.getBodyType()) && AppContext.hasPlugin("workflowAdvanced")) {
         // 流程中是否有当前人员
            if(affair.getMemberId().equals(user.getId())) {
                canExePrediction = true;
            }else {
                // 查找当前人员是否在流程中
                List<Long> members = new ArrayList<Long>(1);
                members.add(user.getId());
                canExePrediction = affairManager.isAffairInProcess(ApplicationCategoryEnum.collaboration, summary.getId(), members);
            }
        }
        map.put("canExePrediction", canExePrediction);
        
        if("listPending".equals(openFrom) && AppContext.hasPlugin("multicall") && user.isV5Member()){
        	//是否显示电话会议按钮
        	map.put("showMeetingBtn", true);
        }
        if(affair.getState().intValue() == StateEnum.col_pending.key() &&
        		(affair.getSubState().intValue() == SubStateEnum.col_pending_Back.key() ||
        		 affair.getSubState().intValue() == SubStateEnum.col_pending_specialBacked.key())){
        	map.put("needPromptedBeBacked", "1");
        }
        //增加点击事件监听，记录查看次数
        if(AppContext.hasPlugin("ai")) {
        	AIRemindEvent remindEvt = new AIRemindEvent(this);
        	remindEvt.setAffairId(affair.getId());
        	remindEvt.setObjectId(affair.getObjectId());
        	remindEvt.setState(affair.getState());
        	remindEvt.setSubject(affair.getSubject());
        	remindEvt.setOpenFrom(openFrom);
        	remindEvt.setTrack(affair.getTrack());
        	remindEvt.setMemberId(affair.getMemberId());
        	EventDispatcher.fireEventAfterCommit(remindEvt);
        }
        
        //获取水印的数据
        Map<String, Object> waterMarkMap = new HashMap<String, Object>();
    	WaterMarkUtil.getWaterMarkSetings(waterMarkMap);
    	map.put("waterMarkMap", waterMarkMap);
        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), summaryVO.getAffairId());
        
        
        
        PluginResourceLocation locationParam = new PluginResourceLocation();
        locationParam.setAffair(affair);
        locationParam.setTemplate(template);
        locationParam.setLocation(PluginResourceScope.COLL_MOBILE_DEAL);
        locationParam.putExtParams("summary", summary);
        locationParam.setM3(isLoginFromM3());
        
        Map<String,List<String>> pluginResources = CollaborationPluginUtils.getPluginRersources(locationParam);
        
        map.put("pluginJsFiles", pluginResources.get("pluginJsFiles"));
        map.put("pluginCssFiles", pluginResources.get("pluginCssFiles"));
        
        return ok(map);
    }
    
    /**
     * 获取电话会议参数
     * @return	map
     */
    @GET
    @Path("multiCall")
    public Response multiCall() throws BusinessException {
    	Map<String, String> meetingParams = new HashMap<String, String>();
    	if(AppContext.hasPlugin("multicall")){
    		multiCallApi.isShowMultiCallBtn();
        	try {
        		meetingParams = multiCallApi.getConferenceCallParams();
			} catch (Exception e) {
				LOGGER.info("调用电话会议接口获取参数报错：", e);
			}
        }
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("meetingParams", meetingParams);
    	return success(map);
    }
    /**
     * 获取协同的所有节点权限
     * @param appName			String | 必填 | 类型 如 ：协同（collaboration）、表单(form)……
     * @param defalutPolicyName	String | 必填 | 默认节点权限
     * @param accountId			long | 必填 | 当前所在模版的单位id
     * @param isTemplate		boolean | 必填 | 是否是模版
     * @return	List<Permission>
     * @throws BusinessException
     */
    private List<PermissionMiniVO> findPermissions(String appName,String categoryName,String defalutPolicyName,long accountId,boolean isTemplate) throws BusinessException{
        List<Permission> result = permissionManager.getPermissions4WFNodeProperties(appName,categoryName, defalutPolicyName,accountId,isTemplate);
        return PermissionMiniVO.valueOf(result);
    }
    
    /**
     * 打印前端日志
     * @param params Map<String,Object> | 非必填 | map对象参数(传什么输出什么，不涉及业务操作)
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("logJs")
    public Response logJs(Map<String,Object> params) throws BusinessException {
        String logstr = ParamUtil.getString(params, "logstr", ""); 
        LOGGER.error(AppContext.currentUserName()+",[移动M1/Wechart协同日志]："+logstr);
        return success("");
    }
    /**
     * 获取协同所有的节点权限
     * @param params	Map<String,Object> | 非必填 | map对象参数
     * <pre>
     * defaultPolicyId   String | 非必填 | 默认节点权限(默认值 collaboration)
     * bodyType          String | 非必填 | 类型(默认值 10)com.seeyon.ctp.common.content.mainbody.MainbodyType  10(HTML)、20(FORM)
     * accountId         Long | 非必填 | 单位ID(默认值 当前登录单位ID)
     * isSystemTemplate  String | 非必填 | 是否系统模板  true(是)、false(否)
     * </pre>
     * @return	List<PermissionMiniVO>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("permissions")
    public Response permissions(Map<String,Object> params) throws BusinessException {
        
        User user = AppContext.getCurrentUser();
        
    	String appName = ApplicationCategoryEnum.collaboration.name();
    	String defalutPolicyName = ParamUtil.getString(params, "defaultPolicyId", "");
    	String bodyType = ParamUtil.getString(params, "bodyType", String.valueOf(MainbodyType.HTML.getKey()));
        Long accountId = ParamUtil.getLong(params, "accountId", Long.valueOf(AppContext.currentAccountId()));
        String systemTemplate = ParamUtil.getString(params, "isSystemTemplate");
        boolean isSystemTemplate = false;
        if ("true".equals(systemTemplate)) {
        	isSystemTemplate = true;
        }
        String categoryName= EnumNameEnum.col_flow_perm_policy.name();
        if (ColUtil.isForm(bodyType)) {
        	//appName = ApplicationCategoryEnum.form.name();
        	categoryName= "form_flow_perm_policy";
        }
        
        //获取默认节点权限
        if("".equals(defalutPolicyName)){
            PermissionVO defPermission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(),user.getLoginAccount());
            defalutPolicyName = defPermission.getName();
        }
        
        List<PermissionMiniVO> ps = this.findPermissions(appName,categoryName, defalutPolicyName, accountId, isSystemTemplate);
        
        return ok(ps);
    }

    /**
     * 查询出未启用的模板
     * @param params
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("findFormRightId")
    public Response findFormRightId(Map<String,Object> params) throws BusinessException {
        Long templateId = ParamUtil.getLong(params, "templateId", null);
        String  formRightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(null, templateId);
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("formRightId",formRightId);
        return ok(ret);
    }
    /**
     * 构造M3进入新建页面所需要的数据
     * @param params	Map<String,Object> | 必填 | MAP对象参数
     * <pre>
     * @param summaryId		Long   | 必填  | 协同ID，待发进入新建页面这个参数是必须的，表示待发的那条协同
     * @param templateId	Long   | 必填  | 模板ID，调用模板进入新建页面这个参数是必须的。
     * @param subject		String | 必填  | 协同标题，语音机器人进入新建，subject\ members \ sourceId，这3个参数是必须的
     * @param members		String | 必填  | 流程的接受人员，语音机器人进入新建，subject\ members \ sourceId，这3个参数是必须的， 格式：Member|id1,Member|id2,Member|id3……
     * @param sourceId		Long   | 必填  | 此处是机器人产生的关联id，用于关联传透对应的数据
     * </pre>
     * @return	Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("new")
    public Response newColl(Map<String,Object> params) throws BusinessException {
    	Long summaryId = ParamUtil.getLong(params, "summaryId", null);
    	Long templateId = ParamUtil.getLong(params, "templateId", null);
    	String subject = ParamUtil.getString(params, "subject", null);
    	String members = ParamUtil.getString(params, "members", null);
    	Long sourceId = ParamUtil.getLong(params, "sourceId", null);
    	String relationId = ParamUtil.getString(params, "relationId", null);
        relationId = Strings.escapeJavascript(relationId);
        User user = AppContext.getCurrentUser();
        fixUser(user);
        Map<String, Object> ret = new HashMap<String, Object>();
        
        ColSummary summary = null;
        CtpAffair ctpAffair = null;
        ColSummary templateSummary = null;
        ColSummaryDetailVO summaryVO = null;
        String formRightId = "";
        String bodyType = MainbodyType.HTML.getValue();
        //构建协同数据
        CtpTemplate ctpTemplate = null;
        String defaultKey ="1";
        String workflowNodeNames = "";
        //跟踪状态
        String trackSend = "true";
        Integer trackType = 1;
        String zdgzry="";
        boolean isNew = true;
        String subState = "";
        if(summaryId != null){//待发列表编辑
            
            //标记是编辑
            isNew = false;
            
            summary = collaborationApi.getColSummary(summaryId);
            
            if(!user.getId().equals(summary.getStartMemberId())){
                //越权了
                throw new BusinessException(ResourceUtil.getString("collaboration.common.unauthorized"));//无权访问
            }
            
            summaryVO = ColSummaryDetailVO.valueOf(summary);
            
            ctpAffair = affairManager.getSenderAffair(summaryId);
            
            Integer trackInt = ctpAffair.getTrack();
            
            subState = ctpAffair.getSubState() == null ? "" : String.valueOf(ctpAffair.getSubState());
            
            if(null != trackInt){
                if(trackInt.intValue() == 0){
                    trackSend ="false";
                }else{
                    trackType =  trackInt.intValue();
                    if(trackType.intValue() == 2){
                        List<CtpTrackMember> tList = trackManager.getTrackMembers(ctpAffair.getId());
                        StringBuilder trackNames = new StringBuilder();
                        StringBuilder trackIds=new StringBuilder();
                        if(tList.size() > 0){
                            for(CtpTrackMember ctpT : tList){
                                trackNames.append("Member|")
                                          .append(ctpT.getTrackMemberId())
                                          .append(",");
                                trackIds.append(ctpT.getTrackMemberId()+",");
                            }
                            if(trackNames.length() > 0){
                                zdgzry = trackIds.substring(0, trackIds.length()-1);
                            }
                        }
                    }
                }
            }

            bodyType =  summaryVO.getBodyType();
            
            summaryVO.setActivityId(ctpAffair.getActivityId());
            summaryVO.setAffairWorkitemId(ctpAffair.getSubObjectId());
            summaryVO.setAffairId(ctpAffair.getId());
            summaryVO.setAffairSubState(ctpAffair.getSubState());
            
          //设置流程信息
            boolean isSpecialSteped = ctpAffair != null && ctpAffair.getSubState() == SubStateEnum.col_pending_specialBacked.key();
            summaryVO.setSpecialStepback(isSpecialSteped);
            
            if(ColUtil.isForm(bodyType) && null != summaryVO.getTemplateId()){
            	templateId = summaryVO.getTemplateId();
            }
            
            
            if(null != templateId){
                
            	ctpTemplate = templateManager.getCtpTemplate(templateId);
                
            	templateSummary = XMLCoder.decoder(ctpTemplate.getSummary(),ColSummary.class);
                
            	formRightId = ContentUtil.findRightIdbyAffairIdOrTemplate(null, ctpTemplate);
            	
            	colManager.addFormRightIdToFormCache(formRightId, ctpTemplate.getFormAppId());
            	
            	bodyType = ctpTemplate.getBodyType();
            	
            	//附件 关联文档
            	List<Attachment> attList = attachmentManager.getByReference(summary.getId(),summary.getId());
            	
            	ret.put("attList", attList);
            	 
            	//关联项目
            	if(null != summary.getProjectId()){
            		summaryVO.setProjectId(summary.getProjectId());
            	}
            	//(高级)归档
            	if(AppContext.hasPlugin("doc") && summary.getArchiveId() != null){
    				boolean docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());
    				if(docResourceExist){
    					JSONObject jo;
    					try {
    						if(Strings.isNotBlank(summary.getAdvancePigeonhole())){
    							jo = new JSONObject(summary.getAdvancePigeonhole());
    							summaryVO.setAdvancePigeonhole(summary.getAdvancePigeonhole());
    							String archiveFolder = "";
    							String tempFolder = "";
    							if (jo.has("archiveFieldName")) {
    								archiveFolder = jo.get("archiveFieldName").toString();
    								tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId())+"\\{"+archiveFolder+"}";
    							}else{
    								tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId());
    							}
    							summaryVO.setArchiveId(summary.getArchiveId());
    							summaryVO.setArchiveName(tempFolder);
    							summaryVO.setArchiveAllName(tempFolder);
    						}else{
    							summaryVO.setArchiveId(summary.getArchiveId());
    							summaryVO.setArchiveName(ColUtil.getArchiveNameById(summary.getArchiveId()));
    							summaryVO.setArchiveAllName(ColUtil.getArchiveAllNameById(summary.getArchiveId()));
    						}
    					} catch (JSONException e) {
    						LOGGER.error("", e);
    					}
    				}else{
    					summaryVO.setArchiveId(null);
    				}
    			}
            	
				//附件归档
				summaryVO.setAttachmentArchiveId(summary.getAttachmentArchiveId());
            	
                summaryVO.setFormViewOperation(formRightId);
                
                if (ctpTemplate.getWorkflowId() != null) { // 系统模板 // & // 个人模板
                    if (!isSpecialSteped && TemplateUtil.isSystemTemplate(ctpTemplate)) {
                        summaryVO.setTemplateProcessId(ctpTemplate.getWorkflowId());
                        summaryVO.setProcessId(null);
                    }
                }
            }
            
        }else{
            
          //新建的逻辑
            summaryVO = new ColSummaryDetailVO();
            if(sourceId != null) {  // 说明是从机器人传入数据，直接使用机器人传递的sourceId为summaryId
                summaryVO.setId(sourceId);
            } else {
                summaryVO.setId(UUIDLong.longUUID());                
            }
            
            trackSend = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_SEND);
            if(null != templateId){//新建表单协同
                
                // 如果这里是个人模板这里保留个人模板
                CtpTemplate srcTempate = templateManager.getCtpTemplate(templateId); 
                
                ctpTemplate = srcTempate;
                
              //设置模板标题
                String temSub = ctpTemplate.getSubject();
                Date _date = new Date();
                String formatDate = Datetimes.formatDatetimeWithoutSecond(_date);
                temSub =  temSub +"("+user.getName()+" "+formatDate+")";
                summaryVO.setSubject(temSub);
                
                if(Boolean.valueOf(false).equals(ctpTemplate.isSystem()) && ctpTemplate.getFormParentid() != null){
                    ctpTemplate = templateManager.getCtpTemplate(ctpTemplate.getFormParentid());
                }
                
                boolean isEnable = templateManager.isTemplateEnabled(ctpTemplate, user.getId());
                if(!isEnable){
                    //越权了
                    throw new BusinessException(ResourceUtil.getString("collaboration.send.fromSend.templeteDelete"));//模板已经被删除，或者您已经没有该模板的使用权限
                }
                
                formRightId = ContentUtil.findRightIdbyAffairIdOrTemplate(null, ctpTemplate);
                
                colManager.addFormRightIdToFormCache(formRightId, ctpTemplate.getFormAppId());
                
                bodyType = ctpTemplate.getBodyType();
                summaryVO.setBodyType(bodyType);
                
                //附件 关联文档
                List<Attachment> attList = attachmentManager.getByReference(templateId);
                ret.put("attList", attList);
                ret.put("needClone", true);//需要克隆附件
                
                //设置模板流程
                summaryVO.setTemplateProcessId(ctpTemplate.getWorkflowId());
                 
                //模板summary对象
                summary = XMLCoder.decoder(srcTempate.getSummary(),ColSummary.class);
                
                templateSummary = summary;
                
                //关联项目
                if(null != srcTempate.getProjectId()){
                    summaryVO.setProjectId(srcTempate.getProjectId());
                }
                //(高级)归档
                if(null != summary.getArchiveId()){
                    if(AppContext.hasPlugin("doc")){
                        boolean docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());
                        if(docResourceExist){
                            try {
                                if(Strings.isNotBlank(summary.getAdvancePigeonhole())){
                                    JSONObject jo = new JSONObject(summary.getAdvancePigeonhole());
                                    summaryVO.setAdvancePigeonhole(summary.getAdvancePigeonhole());
                                    String archiveFolder = "";
                                    String tempFolder = "";
                                    if (jo.has("archiveFieldName")) {
                                        archiveFolder = jo.get("archiveFieldName").toString();
                                        tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId())+"\\{"+archiveFolder+"}";
                                    }else{
                                        tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId());
                                    }
                                    summaryVO.setArchiveId(summary.getArchiveId());
                                    summaryVO.setArchiveName(tempFolder);
                                    summaryVO.setArchiveAllName(tempFolder);
                                }else{
                                    summaryVO.setArchiveId(summary.getArchiveId());
                                    summaryVO.setArchiveName(ColUtil.getArchiveNameById(summary.getArchiveId()));
                                    summaryVO.setArchiveAllName(ColUtil.getArchiveAllNameById(summary.getArchiveId()));
                                }
                            } catch (JSONException e) {
                                LOGGER.error("", e);
                            }
                        }else{
                            summaryVO.setArchiveId(null);
                        }
                    }
                }
                
                //附件归档
                if (summary.getAttachmentArchiveId() != null && AppContext.hasPlugin("doc")) {
                    boolean docResourceExist = docApi.isDocResourceExisted(summary.getAttachmentArchiveId());
                    if(docResourceExist){
                        summaryVO.setAttachmentArchiveId(summary.getAttachmentArchiveId());
                    }
                }
                
                summaryVO.setFormViewOperation(formRightId);
            }
        }
        
        //summary转VO
        if(summary != null){
        	
            summaryVO.setProcessTermType(summary.getProcessTermType());
            summaryVO.setRemindInterval(summary.getRemindInterval());
            summaryVO.setMergeDealType(summary.getMergeDealType());
            summaryVO.setReplyCounts(summary.getReplyCounts());
            
            summaryVO.setImportantLevel(summary.getImportantLevel());
            defaultKey = summary.getImportantLevel()+"";
            summaryVO.setCanEdit(summary.getCanEdit() ? "1" :"0");
            summaryVO.setCanEditAttachment(summary.getCanEditAttachment() ? "1" :"0");
            summaryVO.setCanForward(summary.getCanForward() ? "1" :"0");
            summaryVO.setCanArchive(summary.getCanArchive() ? "1" :"0");
            summaryVO.setCanModify(summary.getCanModify() ? "1" :"0");
            
            
          //流程期限
            String deadlineTemplate = summary.getDeadlineTemplate();
            if(Strings.isBlank(deadlineTemplate)) {
                Long summaryDeadLine = summary.getDeadline();
                if(summaryDeadLine != null) {
                    deadlineTemplate = summaryDeadLine.toString();
                }
            }
            
            
            summaryVO.setDeadlineTemplate(deadlineTemplate);
            summaryVO.setAdvanceRemind(summary.getAdvanceRemind());
            if(summary.getDeadlineDatetime()!=null){
                summaryVO.setDeadlineDatetime(Datetimes.formatDatetimeWithoutSecond(summary.getDeadlineDatetime()));
            }
            if(summary.getDeadlineDatetime() == null 
                    && Strings.isNotBlank(deadlineTemplate)
                    && !"0".equals(deadlineTemplate) && !deadlineTemplate.startsWith("field")){
                Map<String,String> _mMap = new HashMap<String,String>();
                _mMap.put("minutes", Strings.isBlank(deadlineTemplate) ? "0" :String.valueOf(deadlineTemplate));
                Long calculateWorkDatetime = colManager.calculateWorkDatetime(_mMap);
                Date date = new Date(calculateWorkDatetime);
                String format2 = Datetimes.formatDatetimeWithoutSecond(date);
                summaryVO.setDeadlineDatetime(format2);
            }
        }
        
        //新建或者编辑待发是都需要从模板中设置值的点在这里处理
        if (ctpTemplate != null) {

        	if(templateSummary != null){
        		String deadlineTemplate = templateSummary.getDeadlineTemplate();
        		summaryVO.setDeadlineTemplate(deadlineTemplate);
        	}
        	
            summaryVO.setFormAppid(ctpTemplate.getFormAppId());
            
            // 流程标题
            if (Strings.isNotBlank(ctpTemplate.getColSubject())) {
                summaryVO.setSubject(ctpTemplate.getColSubject());
                ret.put("formTitle", ctpTemplate.getColSubject());
            }
            // 流程信息回写
            EnumManager enumManager = (EnumManager) AppContext.getBean("enumManagerNew");
            CtpEnumBean nodePermissionPolicy = enumManager.getEnum(EnumNameEnum.col_flow_perm_policy.name());
            
            //FIXME 这里可以优化一下，前端不需要这个设置了
            String[] nodeInfos = wapi.getWorkflowInfos(String.valueOf(ctpTemplate.getWorkflowId()),
                    ModuleType.collaboration.name(), nodePermissionPolicy);
            summaryVO.setDr(nodeInfos[1]);
            workflowNodeNames = nodeInfos[0];

            summaryVO.setCanScanCode(
                    null == ctpTemplate.getScanCodeInput() ? "0" : ctpTemplate.getScanCodeInput() ? "1" : "0");
            summaryVO.setCanSetSupervise(
                    null == ctpTemplate.getCanSupervise() ? "0" : ctpTemplate.getCanSupervise() ? "1" : "0");
            
            summaryVO.setTemplateId(ctpTemplate.getId());
        }
        
        //目前只有表单能编辑
        if(ColUtil.isForm(bodyType)){
            
            Long contentModuleId = null;
            if(!isNew){
                contentModuleId = summary.getId();
            }else if(ctpTemplate != null){
                contentModuleId = ctpTemplate.getId();
            }
            
            boolean isCAP4 = false;
          //是否为CAP4表单
            if(ctpTemplate != null){
                isCAP4 = capFormManager.isCAP4Form(ctpTemplate.getFormAppId());
                ret.put("isCAP4", isCAP4);
            }
            
            List<CtpContentAllBean> contentList = null;
            Long oldContentId = null;
            if(isCAP4){
                List<CtpContentAll> ctpContentAll = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, contentModuleId, false);
                if(Strings.isNotEmpty(ctpContentAll)){
                    contentList = Collections.singletonList(new CtpContentAllBean(ctpContentAll.get(0)));
                }
            }else {
                contentList = ctpMainbodyManager.transContentViewResponse(ModuleType.collaboration,
                        contentModuleId, CtpContentAllBean.viewState_readOnly, formRightId, 0, -1l, true,new HashMap<String, Object>(1));
            }
            
            if(Strings.isNotEmpty(contentList)){
                CtpContentAllBean ctpContentAll = contentList.get(0);
                oldContentId = ctpContentAll.getId();
                //Map<String, Object> ext = ctpContentAll.getExtraMap();
                Map<String, String> formRightInfo = capFormManager.getFormRightInfo(ctpContentAll.getContentTemplateId(),formRightId,ctpContentAll);
                ret.put("isLightForm", "1".equals(formRightInfo.get("phone"))  ? true:false);
                ret.put("hasPCForm", "1".equals(formRightInfo.get("pc"))  ? true:false);
                
                if(!isNew){
                    //保存待发的正文ID
                    ret.put("cttId", oldContentId);
                }
            }
        }
        
        
        //来自语音机器人
        if(Strings.isNotBlank(subject)){
            summaryVO.setSubject(subject);        	
        }
        
        // 插件
        summaryVO.setHasWorkFlowAdvance(AppContext.hasPlugin(Plugins.WORKFLOW_ADVANCED));
        
        //重要程度枚举
        //common_importance
        Map<String, Object> importEnum = getEnumItems(EnumNameEnum.common_importance, defaultKey); 
        
        //默认节点权限
        PermissionVO permission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(),user.getLoginAccount());
        
        //新建节点权限
        NodePolicyVO newPolicy = colManager.getNewColNodePolicy(user.getLoginAccount());
       

        //默认接收人
        if(Strings.isNotBlank(members)){
            List<Map<String, String>> mList = new ArrayList<Map<String,String>>();
            String[] ms = members.split(",");
            for(String m : ms){
                String mId = "";
                if(Strings.isDigits(m)){
                    mId = m;
                }else{
                    String[] memberArr = m.split("[|]");
                    if("Member".equals(memberArr[0])) {
                        mId = memberArr[1];
                    }
                }
                if(!"".equals(mId)){
                    V3xOrgMember orgMember = orgManager.getMemberById(Long.parseLong(mId));
                    if(orgMember != null){
                        V3xOrgAccount orgAccount = orgManager.getAccountById(orgMember.getOrgAccountId());
                        Map<String, String> mMap = new HashMap<String, String>();
                        mMap.put("id", orgMember.getId().toString());
                        mMap.put("name", orgMember.getName());
                        mMap.put("accountId", orgAccount.getId().toString());
                        mMap.put("accountName", orgAccount.getName());
                        
                        mList.add(mMap);
                    }
                }
                
            }
            //初始化接收人
            ret.put("receivers", mList);
        }
        //添加获取按钮的权限
        Map<String,Object>  baseListBtns  = colManager.getNewColBaseListBtns(user.getLoginAccount(),2);
        ret.put("basicButs", baseListBtns.get("basicButs"));
        ret.put("trackSend", trackSend);
        ret.put("trackType", trackType);
        ret.put("zdgzry", zdgzry);
        ret.put("formRightId", formRightId);
        if(ctpTemplate != null){
            ret.put("templateId", templateId);
        }
        ret.put("bodyType", bodyType);
        //当前用户信息
        ret.put("currentUser", CurrentUserInfoVO.valueOf(user));
        ret.put("summary", summaryVO);
        ret.put("isNew", isNew);
        ret.put("importEnum", importEnum);
        ret.put("defPolicy", PermissionMiniVO.valueOf(permission));
        ret.put("newPolicy", newPolicy);
        ret.put("hasDocPlug",AppContext.hasPlugin("doc"));
        ret.put("workflowNodeNames", workflowNodeNames);
        ret.put("relationId", relationId);
        
        ret.put("subState", subState);

        //增加授权
        if(ctpTemplate != null){
            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.collaboration, String.valueOf(templateId), AppContext.currentUserId());//OA-66280
        }
        AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.collaboration, String.valueOf(summaryVO.getId()), user.getId());
      //获取水印的数据
        Map<String, Object> waterMarkMap = new HashMap<String, Object>();
    	WaterMarkUtil.getWaterMarkSetings(waterMarkMap);
    	ret.put("waterMarkMap", waterMarkMap);
    	
    	
    	// 加载插件资源文件
    
        PluginResourceLocation locationParam = new PluginResourceLocation();
        locationParam.setAffair(ctpAffair);
        locationParam.setTemplate(ctpTemplate);
        locationParam.setLocation(PluginResourceScope.COLL_MOBILE_SEND);
        locationParam.putExtParams("summary", summary);
        locationParam.setM3(isLoginFromM3());
        
        Map<String,List<String>> pluginResources = CollaborationPluginUtils.getPluginRersources(locationParam);
        
        ret.put("pluginJsFiles", pluginResources.get("pluginJsFiles"));
        ret.put("pluginCssFiles", pluginResources.get("pluginCssFiles"));
        return ok(ret);
    }
    
    
    /**
     * 获取系统枚举
     * @param enumKey	EnumNameEnum | 必填 | 
     * @param defVal	String | |
     * @return	Map<String, Object>
     */
    private Map<String, Object> getEnumItems(EnumNameEnum enumKey, String defVal){
        
        Map<String, Object> ret = new HashMap<String, Object>();
        List<Map<String, String>> items = new ArrayList<Map<String,String>>();
        Map<String, String> defMap = null;
        
        List<CtpEnumItem> metaItems =  enumManager.getEnumItems(enumKey);
        if(Strings.isNotEmpty(metaItems)){
            int len = metaItems.size();
            for(int i = 0; i < len; i++){
                CtpEnumItem m = metaItems.get(i);
                if(m.getState() != 0){//非禁用
                    Map<String, String> item = new HashMap<String, String>();
                    item.put("key", m.getValue());
                    item.put("label", ResourceUtil.getString(m.getLabel()));
                    items.add(item);
                    if(m.getValue().equals(defVal)){
                        defMap = item;
                    }
                }
            }
            
            if(defMap == null){
                defMap = items.get(0);
            }
        }
        
        
        ret.put("defVal", defMap);
        ret.put("items", items);
        
        return ret;
        
    }
    
    /**
     * 发送协同
     * @param params	Map<String, Object> | 必填 | map类型参数
     * <pre>
     * colMainData	Map | | 协同数据
     *    bodyType			Integer | | 正文类型{@link com.seeyon.ctp.common.content.mainbody.MainbodyType#getKey()}
     *       <pre>
     *       10(HTML)、20(FORM)
     *       <pre>
     *    subject			String | | 标题
     *    importantLevel	Integer | | 重要程度
     *       <pre>
     *       1(普通)、2(重要)、3(非常重要)
     *       </pre>
     *    canTrack			Integer | | 跟踪 1 - 跟踪， 0 - 不跟踪
     * workflow_definition	Map | | 流程数据
     *    process_desc_by		String | | 数据类型，值为xml
     *    process_xml			String | | 流程XML
     *    readyObjectJSON		String | | 不清楚这个字段是什么
     *    workflow_data_flag	String | | 不知道这个是什么，值为 WORKFLOW_SEEYON
     *    moduleType			String | | 应用类型,值为1 {@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum#getKey()}
     *    process_info			String | | 流程显示内容
     *    process_info_selectvalue	String | |
     *    process_subsetting	String | |
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("send")
    public Response send (Map<String, Object> params) throws BusinessException {
        
        Map<String, Object> ret = new HashMap<String, Object>();
        
        Map<String, String> sendRet = sendColl(params);
        
      //结果封装
        if(sendRet.containsKey(ERROR_KEY)){
            ret.put(ERROR_KEY,sendRet.get(ERROR_KEY));
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            
            if(sendRet != null && Strings.isNotBlank(sendRet.get("snInfos"))) {
                ret.put("snInfos", sendRet.get("snInfos"));
            }
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }

    /** 
     * <pre>
     * 提取为私有方法：实际发送协同
     * </pre> 
     */ 
    private Map<String, String> sendColl(Map<String, Object> params) {
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        
        if(params == null){
        	ret.put(ERROR_KEY, ResourceUtil.getString("collaboration.param.notEmpty"));
        	return ret;
        }
        
        String jsonStr = (String) params.get(JSON_PARAMS);
        putThreadContext(jsonStr);
        
        String checkDatas = checkHttpParamValid();
        Long summaryId = 0l;
        boolean isLock = false;
        
        try {
            
            boolean check = true;
            
            if(Strings.isNotBlank(checkDatas)){
                check = false;
                ret.put(ERROR_KEY, checkDatas);
            }
            
            
            //构建协同VO对象
            Map para = ParamUtil.getJsonDomain("colMainData");
            ColSummary summary = (ColSummary) ParamUtil.mapToBean(para, new ColSummary(), false);
            
            //锁检验
            summaryId =  summary.getId();
            isLock = colLockManager.canGetLock(summaryId);
            if(!isLock) {
            	check = false;
            	ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable30"));
            	LOGGER.error( AppContext.currentUserLoginName()+"正在发送中，不要重复点击summaryId"+summaryId);
            }
            
            ColInfo info = new ColInfo();
			/* CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
			if(sendAffair != null && StateEnum.col_waitSend.getKey() != sendAffair.getState().intValue()){
				return null;
			}
			info.setSenderAffair(sendAffair);*/
            
            if(check){
                
              //完善用户数据
                fixUser(user);
                
                //新建发送设置summary新ID
                String subject = Strings.nobreakSpaceToSpace(summary.getSubject());
                subject = subject.replaceAll("\r", "").replaceAll("\n", "");
                summary.setSubject(subject);
                //流程自动终止
                if (para.get("canAutostopflow") == null) {
                    summary.setCanAutostopflow(false);
                }
                String isNewBusiness = (String) para.get("newBusiness");
                info.setNewBusiness("1".equals(isNewBusiness) ? true : false);
                
                info.setSummary(summary);
                ColConstant.SendType sendType = ColConstant.SendType.normal;
                info.setCurrentUser(user);
                
                //跟踪的相关逻辑代码(根据Id来去值) 并且判断出跟踪的类型指定人还是全部人
                String canTrack = (String) para.get("canTrack");
                int track = 0;
                if ("1".equals(canTrack)) {
                    track = 1;//affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
                    if("2".equals((String)para.get("trackType"))){
                    	track = 2;
                    }
                    info.getSummary().setCanTrack(true);
                } else {
                    //如果没勾选跟踪，这里讲值设置为false
                    info.getSummary().setCanTrack(false);
                }
                //模板ID(最原始的那个ID)
                if (Strings.isNotBlank((String) para.get("tId"))) {
                    info.settId(Long.valueOf((String) para.get("tId")));
                }
                info.setTrackType(track);
                String trackMemberId = (String)para.get("zdgzry");
                info.setTrackMemberId(trackMemberId);
                
                String caseId = (String) para.get("caseId");
                info.setCaseId(Strings.isEmpty(caseId) ? null : Long.parseLong(caseId));
                
                String currentaffairId = (String) para.get("currentaffairId");
                info.setCurrentAffairId(Strings.isBlank(currentaffairId) ? null : Long.parseLong(currentaffairId));
                String currentProcessId = (String) para.get("oldProcessId");
                info.setCurrentProcessId(Strings.isBlank(currentProcessId) ? null : Long.parseLong(currentProcessId));
                info.setTemplateHasPigeonholePath(String.valueOf(Boolean.TRUE).equals(para.get("isTemplateHasPigeonholePath")));
                
                String formViewOperation = (String)para.get("formViewOperation");
                info.setFormViewOperation(formViewOperation);
                
                if(Strings.isNotBlank((String) para.get("contentTemplateId"))){
                	summary.setFormAppid(Long.valueOf((String) para.get("contentTemplateId")));
                }
                info.setM3Flag(true);

                info.setDR( (String)para.get("DR"));
                
                Map<String, String> sendRet = colManager.transSend(info, sendType);
                
                ret.putAll(sendRet);
                //附件 TODO ...
            }
            
        } catch (BusinessException e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("H5处理协同异常", e);
        }finally {
            if(isLock){
            	colLockManager.unlock(summaryId);
            }
            removeThreadContext();
        }
        
        return ret;
    }
        
    /**
     * 保存待发协同
     * @param params	Map<String, Object> | 必填 | map类型参数
     * <pre>
     * colMainData	Map | | 协同数据
     *    bodyType			Integer | | 正文类型{@link com.seeyon.ctp.common.content.mainbody.MainbodyType#getKey()}
     *       <pre>
     *       10(HTML)、20(FORM)
     *       <pre>
     *    subject			String | | 标题
     *    importantLevel	Integer | | 重要程度
     *       <pre>
     *       1(普通)、2(重要)、3(非常重要)
     *       </pre>
     *    canTrack			Integer | | 跟踪 1 - 跟踪， 0 - 不跟踪
     * workflow_definition	Map | | 流程数据
     *    process_desc_by		String | | 数据类型，值为xml
     *    process_xml			String | | 流程XML
     *    readyObjectJSON		String | | 不清楚这个字段是什么
     *    workflow_data_flag	String | | 不知道这个是什么，值为 WORKFLOW_SEEYON
     *    moduleType			String | | 应用类型,值为1 {@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum#getKey()}
     *    process_info			String | | 流程显示内容
     *    process_info_selectvalue	String | |
     *    process_subsetting	String | |
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("saveDraft")
    public Response saveDraft(Map<String, Object> params) throws BusinessException {
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        
        String jsonStr = (String) params.get(JSON_PARAMS);
        putThreadContext(jsonStr);
        
        try {
                
          //完善用户数据
            fixUser(user);

            ColInfo info = new ColInfo();
            
            
            Map para = ParamUtil.getJsonDomain("colMainData");
            ColSummary summary = (ColSummary)ParamUtil.mapToBean(para, new ColSummary(), false);
            
            String subject = Strings.nobreakSpaceToSpace(summary.getSubject());
            subject = subject.replaceAll("\r", "").replaceAll("\n", "");
            summary.setSubject(subject);

            //TODO  校验模板是否存在
            info.setSummary(summary);
            
            
            String newSubject = "";
            boolean saveProcessFlag = true;
            if(Strings.isNotBlank((String)para.get("tId"))){
            	Long templateIdLong = Long.valueOf((String)para.get("tId"));
                info.settId(templateIdLong);
                CtpTemplate ct = templateManager.getCtpTemplate(templateIdLong);
                if(!"text".equals(ct.getType())){
                	saveProcessFlag = false;//流程模板和协同模板的 保存待发 才存入processId
                }
                ColSummary summary1 = XMLCoder.decoder(ct.getSummary(),ColSummary.class);
    			//判断动态更新是否勾选
    			if(summary1 != null && Boolean.TRUE.equals(summary1.getUpdateSubject())){
    				newSubject = ColUtil.makeSubject(ct, summary, user);
    				info.getSummary().setSubject(newSubject);
    			}
            }
            
            info.setSubjectForCopy("");
            String isNewBusiness = (String)para.get("newBusiness");
            info.setNewBusiness("1".equals(isNewBusiness)?true:false);
            
            info.setCurrentUser(user);
            
          //跟踪的相关逻辑代码(根据Id来去值) 并且判断出跟踪的类型指定人还是全部人
            String canTrack = (String) para.get("canTrack");
            int track = 0;
            if ("1".equals(canTrack)) {
                track = 1;//affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
                if("2".equals((String)para.get("trackType"))){
                	track = 2;
                }
                info.getSummary().setCanTrack(true);
            } else {
                //如果没勾选跟踪，这里讲值设置为false
                info.getSummary().setCanTrack(false);
            }
            info.setTrackType(track);
            String trackMemberId = (String)para.get("zdgzry");
            info.setTrackMemberId(trackMemberId);
            
            String contentSaveId = (String)para.get("contentSaveId");
            info.setContentSaveId(contentSaveId);
            info.setM3Flag(true);
            Map map =colManager.saveDraft(info,saveProcessFlag,para);
            
            //附件 TODO ...
            
        } catch (BusinessException e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("H5处理协同异常", e);
        }finally {
            removeThreadContext();
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 数据完整性验证
     * @return
     */
    private  String checkHttpParamValid(){
        String result = "";
        
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        
        String workflow_data_flag = wfdef.get("workflow_data_flag");
        if(Strings.isBlank(workflow_data_flag) || "undefined".equals(workflow_data_flag.trim()) || "null".equals(workflow_data_flag.trim())){
            result = ResourceUtil.getString("coll.summary.validate.lable31");
        }
        return result;
    }
    
    
    /**
     * 保存正文
     * @param param		Map<String, Object> | 必填 | map对象参数
     * <pre>
     *    _currentDiv   Map | 必填 | map对象参数
     *       <pre>
     *          _currentDiv   String | 必填 | 第几个正文(直接写0)
     *       </pre>
     *    mainbodyDataDiv_0   Map | 必填 | map对象参数
     *       <pre>
     *          createId    Long | 必填 | 创建人
     *          moduleType  Integer | 必填 | 模块类型(1)
     *          moduleId    Long | 必填 | 协同ID
     *          contentType Integer | 必填 | 类型(10)
     *          moduleTemplateId   Long | 必填 | 模板ID(0)
     *          contentTemplateId  Long | 必填 | 表单ID(0)
     *          sort        Integer | 必填 | 排序(0)
     *          title       String | 必填 | 标题
     *          status      String | 必填 | 当前内容状态，新增提交还是修改提交，新建查看还是浏览查看(STATUS_RESPONSE_NEW)
     *          viewState   String | 必填 | 前内容所处的业务状态(1)
     *          content     String | 必填 | 正文内容
     *       </pre>
     * </pre>
     * @return	DataContainer
     * @throws BusinessException
     */
    @POST
    @Path("saveOrUpdate")
    @Consumes({MediaType.APPLICATION_JSON })
    public Response saveOrUpdate(Map<String, Object> param) throws BusinessException {

        DataContainer dc = new DataContainer();
        CtpContentAllBean contentAll = null;
        try {
            
            contentAll = convertToContentAll(param);
            // 保存
            ctpMainbodyManager.transContentSaveOrUpdate(contentAll);
            
            // 需要返回生成的ContentAll对象json对象和保存状态是否成功，供应用层使用
            contentAll.setContent("");
            
            dc.add("success", "true");
            CtpContentAll c = contentAll.toContentAll();
            c.setTitle(Strings.escapeJson(c.getTitle()));
            dc.add("contentAll", c);
            dc.add("sn", (DataContainer) contentAll.getAttr("sn"));
        } catch (Exception e) {
            dc.add("success", "false");
            if (contentAll != null) {
                contentAll.setContent("");
                CtpContentAll c = contentAll.toContentAll();
                c.setTitle(Strings.escapeJson(c.getTitle()));
                dc.add("contentAll", c);
            }
            String errorMsg = "";
            if (e.getMessage() != null) {
                errorMsg = e.getMessage();
            }
            dc.add("errorMsg", errorMsg);
            LOGGER.error(e.getMessage(), e);
        }finally {
            removeThreadContext();
        }

        return ok(dc);
    }

    /** 
     * <pre>
     *  将Map的param转换成CtpContentAllBean对象
     *     
     * @throws BusinessException      
     * @return: CtpContentAllBean  
     * @date:   2019年7月4日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    private CtpContentAllBean convertToContentAll(Map<String, Object> param) throws BusinessException {
        String jsonStr = (String) param.get(JSON_PARAMS);
        putThreadContext(jsonStr);
        
        CtpContentAllBean contentAll = null;
        // 获取当前是第几个正文
        Map<String, String> div = ParamUtil.getJsonDomain("_currentDiv");
        String curDiv = div.get("_currentDiv");
        
        // 获取正文参数，组装正文对象, ContentAll
        contentAll = (CtpContentAllBean) ParamUtil.mapToBean(ParamUtil.getJsonDomain("mainbodyDataDiv_" + curDiv),
                CtpContentAllBean.class, true);

        String needCheckRule = "false";// 是否需要校验表单业务规则
        contentAll.putExtraMap("needCheckRule", needCheckRule);
        
        return contentAll;
    }
    
    
    /**
     * 完善用户信息，rest接口获取不全
     * @param user	用户
     * @throws BusinessException 
     */
    private void fixUser(User user) throws BusinessException{
        
        V3xOrgMember member = orgManager.getMemberById(user.getId());
        user.setDepartmentId(member.getOrgDepartmentId());
        
        V3xOrgAccount account = orgManager.getAccountById(user.getLoginAccount());
        user.setLoginAccountName(account.getName());
        user.setLoginAccountShortName(account.getShortName());
    }
    
    /**
     *  获取协同正文信息
     *  {POST} /content 
     * @param {Map}  [params] 
     * <pre>
     *    {Long}   affairId  affairId
     *    {String} [openFrom]  打开来源
     *    {String} [formStyle] 轻表单标识, light-轻表单
     * </pre>
     */
   /* @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("content")
    public Response content (Map<String, Object> params) throws BusinessException {

        Long affairId = getLong(params, "affairId", 0l);
        String openFrom = ParamUtil.getString(params, "openFrom", "");
                
        CtpAffair affair = affairManager.get(affairId);
        ColSummary summary = collaborationApi.getColSummary(affair.getObjectId());
        
        boolean isLightForm = ColUtil.isForm(summary.getBodyType())
                && "light".equals(params.get("formStyle"));
        
        ColSummaryContentVO contentVO = findSummaryContent(summary, affair, isLightForm, openFrom);
        
        //拼装内容
        return ok(contentVO);
    }
    */
    
    /**
     * 协同的评论列表
     * @param type		String | 必填 | 评论类型,只能取值：all-全部列表， like-点赞评论列表，agree-同意意见列表，disagree-不同意意见列表
     * @param summaryId	Long | 必填 | 协同ID
     * @param openFrom	Stirng | 必填 | 来源
     * @param affairID	Long | 必填 | 事项ID
     * @return	FlipInfo
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("comments/{type}/{summaryId}")
    public Response summaryComment(@PathParam("type") String type, @PathParam("summaryId") Long summaryId,@QueryParam("openFrom") String openFrom,@QueryParam("affairId") Long affairID) throws BusinessException {

        FlipInfo flipInfo = getFlipInfo();
        
        ColSummary summary = collaborationApi.getColSummary(summaryId);
        
        //评论，分页取20条
        List<ColSummaryCommentVO> vos = new ArrayList<ColSummaryCommentVO>(); 
        List<Comment> commentList = null;
        
        boolean isHistory = false;
        
        if(Strings.isNotBlank(openFrom) && null != affairID && !affairID.equals(Long.valueOf(-1))){
        	CtpAffair affair = affairManager.get(affairID);
        	if(affair == null && AppContext.hasPlugin("fk")){
                affair = affairManager.getByHis(affairID);
                if(affair != null){
                	isHistory = true;
                }
            }
        	
        	//控制隐藏的评论对发起人可见
        	AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY,summary.getStartMemberId());
        	if (!ColOpenFrom.supervise.name().equals(openFrom) && !ColOpenFrom.repealRecord.name().equals(openFrom)) {
        		Long _memberId = affair.getMemberId();
        		//协同V5 OA-146095 【内部协同】M3上表单查询模版查询出来的数据，可以查看隐藏的处理意见
        		if(ColOpenFrom.formQuery.name().equals(openFrom) || ColOpenFrom.formStatistical.name().equals(openFrom)){
        			List<CtpAffair> affairs = affairManager.getAffairs(summary.getId());
        			if(Strings.isNotEmpty(affairs)){
        				Long cId = AppContext.getCurrentUser().getId();
        				for(CtpAffair aff: affairs){
        					if(aff.getMemberId().equals(cId)){
        						_memberId =  aff.getMemberId();
        						break;
        					}
        				}
        			}
        		}
        		AppContext.putThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID,affair==null?"":_memberId);
        	}
        	
        	if(ColOpenFrom.glwd.name().equals(openFrom)){
        		List<Long> memberIds = affairManager.getAffairMemberIds(ApplicationCategoryEnum.collaboration, summary.getId());
        		AppContext.putThreadContext(Comment.THREAD_CTX_PROCESS_MEMBERS,Strings.isNotEmpty(memberIds) ? memberIds : new ArrayList<Long>());
        	}
        }
        
        
        if(COMMENT_TYPE_LIKE.equals(type)){
            flipInfo = commentManager.findLikeComment(ModuleType.collaboration, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, false, isHistory);
        } else if (COMMENT_TYPE_AGREE.equals(type)) {//同意
            flipInfo = commentManager.findCommentByAttitude(ModuleType.collaboration, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, CommentExtAtt1Enum.agree.i18nLabel(), false, isHistory);
        }else if(COMMENT_TYPE_DISAGREE.equals(type)){
            flipInfo = commentManager.findCommentByAttitude(ModuleType.collaboration, 
                    summary.getId(), Comment.CommentType.comment, flipInfo, CommentExtAtt1Enum.disagree.i18nLabel(), false, isHistory);
        }else{
            flipInfo = commentManager.findComments(ModuleType.collaboration, summary.getId(), Comment.CommentType.comment, flipInfo, null, false, isHistory);
            if(summary.getReplyCounts() == null || 
            	 (	summary.getReplyCounts() != null && summary.getReplyCounts().intValue() !=  flipInfo.getTotal() )){
            	if(!isHistory && (summary.getReplyCounts() == null || summary.getReplyCounts() == 0) && flipInfo.getTotal() != 0){//OA-121525转储数据不能进行更新，因为update的底层代码使用的是merge，如果调用merge，会向主库也插入一条数据。
            	    summary.setReplyCounts(flipInfo.getTotal());
            		colManager.updateColSummary(summary);
            	}
            }
        }
        
        commentList = flipInfo.getData();
        
        if (Strings.isNotEmpty(commentList)) {//获取子回复
            List<Long> commentIds = new ArrayList<Long>();
            for (Comment c : commentList) {
                commentIds.add(c.getId());
            }
            Map<Long, List<Comment>> subComments = commentManager.findCommentReplay(commentIds);
            for (Comment c : commentList) {
                ColSummaryCommentVO vo = ColSummaryCommentVO.valueOf(c);
                //子回复
                List<Comment> subReplysList = subComments.get(c.getId());
                vo.setSubReplys(ColSummaryCommentVO.valueOf(subReplysList));
                vos.add(vo);
            }
            flipInfo.setData(vos);
        }
        
        AppContext.removeThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY);
        AppContext.removeThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID);
        AppContext.removeThreadContext(Comment.THREAD_CTX_PROCESS_MEMBERS);
        
        return ok(flipInfo);
    }
    
    /**
     * 获取协同意见分类数量
     * @param summaryId	Long | 必填 | 协同ID
     * @param types		String | 必填 | 类型组合， all-全部列表， like-点赞评论列表，agree-同意意见列表，disagree-不同意意见列表, 中间使用,分隔
     * @return	Map<String, Integer>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("commentsCount/{summaryId}")
    public Response commentsCount(@PathParam("summaryId") Long summaryId, @QueryParam("types") String types) throws BusinessException {

        Map<String, Integer> countMap = new HashMap<String, Integer>(); 
        //getCommentCount
        if(Strings.isNotBlank(types)){
            String[] typeList = types.split(",");
            for(String t : typeList){
                if(Strings.isNotBlank(t)){
                    countMap.put(t, getCommentCount(summaryId, t, false));
                }
            }
        }
        
        return ok(countMap);
    }
    
    
    /**
     * 添加评论
     * @param summaryId		Long | 必填 | 协同ID
     * @param params		Map<String, Object> | 必填 | map类型参数
     * <pre>
     * content		String | | 评论内容
     * ctype		Integer | | 类型 {@link com.seeyon.ctp.common.content.comment.Comment.CommentType}
     *    -1, "发起人附言", 1, "回复"
     * fileUrlIds	String | | 附件ID串，使用,分隔
     * 
     * 如果ctype 值为  -1 则传递以下参数
     *    toSendMsg	String | | 是否发送消息 true - 发送消息, false 不发送
     * 如果ctype 值为 1 则传递以下参数
     *    affairId	Long | | 打开协同使用的affairId
     *    toSendMsg	String | | 是否发送消息 true - 发送消息, false 不发送
     *    hide		String | | 是否隐藏意见 true - 隐藏， false - 不隐藏
     *    hideToSender	String | | 对发起者隐藏， 需要 hide == true才有用， true - 对发起者隐藏，false-对发起者不隐藏
     *    commentId	Long | | 回复的意见ID
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("comment/{summaryId}")
    public Response comment(@PathParam("summaryId") Long summaryId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        
        try {
            
            User user = AppContext.getCurrentUser();
            String content = ParamUtil.getString(params, "content");
            Integer cType = ParamUtil.getInt(params, "ctype");
            
            //附件
            /*String fileUrlIds = ParamUtil.getString(params, "fileUrlIds", null);
            List<Attachment> attrs = createAttachments(summaryId, fileUrlIds);*/
            String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
            
            if(CommentType.sender.getKey() == cType){
                
                String toSendMsg = ParamUtil.getString(params, "toSendMsg");
                
                //附言
                collaborationApi.addSenderComment(user.getId(), summaryId, content, relateInfo, "true".equals(toSendMsg));
            }else if(CommentType.reply.getKey() == cType) {
                //回复意见
                String toSendMsg = ParamUtil.getString(params, "toSendMsg");
                String hide = ParamUtil.getString(params, "hide");
                String hideToSender = ParamUtil.getString(params, "hideToSender");
                Long affairId = getLong(params, "affairId", null);
                Long commentId = getLong(params, "commentId", null);
                
                CtpAffair affair = affairManager.get(affairId);
                Comment c = commentManager.getComment(commentId);
                
                Comment comment = new Comment();
                comment.setContent(Strings.removeEmoji(content));
                comment.setTitle(affair.getSubject());
                
                StringBuilder showToIds = new StringBuilder();
                boolean isHide = "true".equals(hide);
                boolean isHideToSender = "true".equals(hideToSender);
                boolean _hide = isHide || isHideToSender;
                comment.setHidden(_hide);
                
                if(_hide){
                	//移动端默认对被回执者不进行隐藏
                	showToIds.append(",Member|").append(c.getCreateId());
                	
                	//移动端默认对震荡回执者不进行隐藏
                	showToIds.append(",Member|").append(affair.getMemberId());
                	
                	if(!isHide) {
                		List<CtpAffair> affairs = affairManager.getAffairs(affair.getObjectId());
                		for(CtpAffair af : affairs){
                			if(!af.getMemberId().equals(affair.getSenderId())){
                				showToIds.append(",Member|").append(af.getMemberId());
                			}
                		}
                	}
                	if(!isHideToSender){
                		showToIds.append(",Member|").append(affair.getSenderId());
                	}
                	
                	comment.setShowToId(showToIds.toString().substring(1));
                }
                
                boolean isSendMsg = "true".equals(toSendMsg);
                comment.setPushMessage(isSendMsg);
                if(isSendMsg){
                	List<Map<String,String>> dataList = new ArrayList();
                	Map<String,String> dataMap = new HashMap<String,String>();
                	dataMap.put("affairId",String.valueOf(c.getAffairId()));
                	dataMap.put("memberId",String.valueOf(c.getCreateId()));
                	dataList.add(dataMap);
                    comment.setPushMessageToMembers(JSONUtil.toJSONString(dataList));
                }
                
                comment.setClevel(c.getClevel() + 1);
                comment.setModuleType(c.getModuleType());
                comment.setModuleId(c.getModuleId());
                comment.setCtype(CommentType.reply.getKey());
                comment.setAffairId(affairId);
                
                //附件
                /*String relateInfo = "[]";
                if(Strings.isNotEmpty(attrs)){
                    relateInfo = JSONUtil.toJSONString(attrs);
                }*/
                comment.setRelateInfo(relateInfo);
                
                //at相关信息
                formatAtWho(comment, affair, params);
                
                Comment returnComment = collaborationApi.replyComment1(user.getId(), commentId, comment);
                ret.put("comment_info", JSONUtil.toJSONString(ColSummaryCommentVO.valueOf(returnComment)));
            }else{
              //其他回复
            }
        } catch (BusinessException e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("评论异常", e);
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 点赞或取消赞
     * @param commentId		Long | 必填 | 意见ID
     * @return	Map<String, String>
     * @throws BusinessException
     */
    @GET
    @Path("comment/like/{commentId}")
    public Response likeComment(@PathParam("commentId") Long commentId) throws BusinessException{
        
        Map<String, String> ret = new HashMap<String, String>();
        
        try {
            
            User user = AppContext.getCurrentUser();
            Comment c = commentManager.getComment(commentId);
            if(c.getPraiseToComment()){
                //取消赞{"moduleId":"-6906525693771035927","praiseMemberId":"-2996745466409759480"}
                Map<String, String> params = new HashMap<String, String>();
                params.put("moduleId", c.getId().toString());
                params.put("praiseMemberId", user.getId().toString());
                commentManager.deletePraise(params);
            }else{
                //点赞
                Map<String, String> params = new HashMap<String, String>();
                params.put("moduleId", c.getId().toString());
                params.put("praiseMemberId", user.getId().toString());
                params.put("subject", c.getModuleId().toString());
                commentManager.addPraise(params);
            }
        } catch (BusinessException e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("点赞异常", e);
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 协同处理人列表
     * @param type		String | 必填 | 类型 all-全部, running-待处理
     * @param summaryId	Long | 必填 | 协同ID
     * @return	FlipInfo
     * @throws BusinessException
     */
    @GET
    @Path("handlers/{type}/{summaryId}")
    public Response handlers(@PathParam("type") String type, @PathParam("summaryId") Long summaryId) throws BusinessException{
        
        FlipInfo flipInfo = getFlipInfo();
        try {
            User user = AppContext.getCurrentUser();
            
            List<StateEnum> states = new ArrayList<StateEnum>();
            if("running".equals(type)){
                states.add(StateEnum.col_pending);
            }else if("all".equals(type)){
                states.add(StateEnum.col_pending);
                states.add(StateEnum.col_done);
            }
            
//            List<Long> memberIds = affairManager.findMembers(ApplicationCategoryEnum.collaboration, 
//                    summaryId, states, flipInfo);
            List<CtpAffair> oldAffair = affairManager.getAffairs(summaryId, states);
            List<CtpAffair> affair = new ArrayList<CtpAffair>();
            if(Strings.isNotEmpty(oldAffair)){
            	for(CtpAffair aff : oldAffair){
            		if(aff.getApp().equals(Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()))
            				&& !aff.isDelete()){
            			affair.add(aff);
            		}
            	}
            }
            
            
            List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
            if(Strings.isNotEmpty(affair)){
                
                boolean readSwitch = "enable".equalsIgnoreCase(systemConfig.get(IConfigPublicKey.READ_STATE_ENABLE));
                
                for(CtpAffair a : affair){
                    Map<String, Object> dataMap = new HashMap<String, Object>();
                    
                    V3xOrgMember member = orgManager.getMemberById(a.getMemberId());
                    dataMap.put("memberName", member.getName());
                    
                    V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
                    if(post != null){
                    	dataMap.put("postName", post.getName());
                    } else {
                    	String memberPost = OrgHelper.getExtMemberPriPost(member);
                    	if (Strings.isBlank(memberPost)) {
                        	memberPost = "-";
                        }
                    	dataMap.put("postName", memberPost);
                    }
                    
                    //其他单位人员显示简称
                    StringBuilder accountName = new StringBuilder();
                    if(!user.getLoginAccount().equals(member.getOrgAccountId())){
                        V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
                        accountName.append("(").append(account.getShortName()).append(")");
                    }
                    dataMap.put("accountName", accountName.toString());

                    dataMap.put("memberId", a.getMemberId());
                    dataMap.put("state",a.getState());
                    
                    Integer subState = a.getSubState();
                    if(subState != null 
                            && subState == SubStateEnum.col_pending_read.getKey()
                            && !readSwitch){
                        subState = SubStateEnum.col_pending_unRead.getKey();
                    }
                    dataMap.put("subState", subState);
                    dataMap.put("backFromId",a.getBackFromId());
                    datas.add(dataMap);
                }
            }
            

            DBAgent.memoryPaging(datas, flipInfo);
        } catch (BusinessException e) {
            LOGGER.error("获取协同处理人列表错误", e);
        }
        
        return ok(flipInfo);
    } 
    
    /**
     * 提交前检查affair状态是否正常
     * @param affairId	String | 必填 | 事项ID
     * @param pageNodePolicy	String | 必填 | 页面上的节点权限
     * @return	Map<String, String>
     * @throws BusinessException
     */
    @GET
    @Path("checkAffairValid")
    public Response checkAffairValid(@QueryParam("affairId") String affairId,@QueryParam("pageNodePolicy") String pageNodePolicy) throws BusinessException {
        
        Map<String, String> ret = new HashMap<String, String>();
        
        isValidAffair(affairId, ret,pageNodePolicy);
        if(ret.isEmpty()){
        	ret.put("success", "true");
        }
        return ok(ret);
    }
     
    /**
     * 处理协同
     * 接口说明:接口需要传入工作流的信息("_json_params:{}"),传入错误或者未传会给出错误提示"com.seeyon.ctp.workflow.exception.BPMException: 数据接请稍后重试！"
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map | 必填 | 其他参数
     * <pre>
     * content	String | | 意见内容
     * hide		String | | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * tracking	String | | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * attitude	String | | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * likeSummary	String | | 是否点赞， true-点赞了， false-没有
     * fileUrlIds	String | | 附件ID串，使用,分隔
     * commentId	Long | | 使用草稿的评论ID  
     * </pre>
     * 
     * @return  Map<String, String>
     * @throws BusinessException
     * 
     */
    @POST
    @Path("finishWorkItem/{affairId}")
    public Response finishWorkItem(@PathParam("affairId") Long affairId, Map<String, Object> params) throws BusinessException {

        Map<String, String> ret = new HashMap<String, String>();
        
        User user = AppContext.getCurrentUser();
        
        CtpAffair affair = null;
        ColSummary summary = null;
        boolean canDeal = false;
        boolean isLock = false;
        try {
            
            LOGGER.info(user.getName() + "," + AppContext.getCurrentUser().getUserAgentFrom() + ", Rest开始处理:" + affairId);
            
            String jsonStr = ParamUtil.getString(params, JSON_PARAMS, "");
            putThreadContext(jsonStr);
            
            //获取对象
            affair = affairManager.get(affairId);
            if(affair==null){
            	ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
            	ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable32"));
            	return ok(ret);
            }
           /* if(!Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())){
                ret.put(ERROR_KEY, "事项不是待办状态，不能处理");
                return ok(ret);
            }*/
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            //检验锁和状态是否正常
            canDeal = checkCanDeal(summary, affair, ret);
            if(canDeal){
            	isLock = colLockManager.canGetLock(affairId);
            	if(!isLock) {
            		canDeal = false;
            		ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable33"));
            		LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
            	}
            }
            
            if(canDeal){
                //WorkflowBpmContext arg0, CPMatchResultVO arg1
                //WFAjax.transBeforeInvokeWorkFlow(context, resultVO);
                //TODO 预提交， 加锁。。。
            }
            
            if (canDeal) {

                // 意见
                Comment comment = formComment(summary, affair, user, ColHandleType.finish, params);

                // 处理参数
                Map<String, Object> dealParams = new HashMap<String, Object>();
                
                //跟踪
                dealParams.put("trackParam", formTrackParam(params));
                //处理后归档
                String archiveValue = ParamUtil.getString(params, "archiveValue", "");
                dealParams.put("archiveValue", archiveValue);
                
                //取出模板信息
                //Map<String,Object> templateMap = (Map<String,Object>)ParamUtil.getJsonDomain("colSummaryData");
                if(summary.getTempleteId() != null){
                	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                	dealParams.put("templateColSubject", template.getColSubject());
                	dealParams.put("templateWorkflowId", template.getWorkflowId());
                }
                
                collaborationApi.finishWorkItem(affair, summary, comment, dealParams);
                if(ret.isEmpty()){
                	ret.put("succ_msg", "处理成功!");
                }
            }

        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            if(isLock){
    			colLockManager.unlock(affairId);
    		}
            removeThreadContext();
            if(canDeal){
            	colManager.unlockCollAll(affairId,affair,summary);
            }
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 暂存待办协同
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map | 必填 | 其他参数
     * <pre>
     * content	String | | 意见内容
     * hide		String | | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * tracking	String | | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * attitude	String | | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * likeSummary	String | | 是否点赞， true-点赞了， false-没有
     * fileUrlIds	String | | 附件ID串，使用,分隔
     * commentId	Long | | 使用草稿的评论ID  
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Path("doZCDB/{affairId}")
    public Response doZCDB(@PathParam("affairId") Long affairId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        //前端参数完整性校验TODO...
        
        CtpAffair affair = null;
        ColSummary summary = null;
        
        boolean canDeal = true;
        boolean isLock = false;
        try {
            
            String jsonStr = ParamUtil.getString(params, JSON_PARAMS, "");
            putThreadContext(jsonStr);
            
            //获取对象
            affair = affairManager.get(affairId);
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            
            String[] wfCheck = wapi.canTemporaryPending(toString(affair.getSubObjectId()));
            if("false".equals(wfCheck[0])){
                canDeal = false;
                ret.put(ERROR_KEY, wfCheck[1]);
            }
            
            if(canDeal){
                canDeal = lockWorkFlow(summary, user, 14, ret);
            }
            
            if(canDeal){
                //检验是否可以处理
                canDeal = checkCanDeal(summary, affair, ret);
            }
            if(canDeal){
            	isLock = colLockManager.canGetLock(affairId);
            	if(!isLock) {
            		canDeal = false;
            		ret.put(ERROR_KEY, "同时项两个人正在同时处理");
            		LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
            	}
            }
            
            if (canDeal) {

                //意见
                Comment comment = formComment(summary, affair, user, ColHandleType.wait, params);
               
                // 处理参数
                Map<String, Object> dealParams = new HashMap<String, Object>();
                
                if(summary.getTempleteId() != null){
                	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                    //取出模板信息
                	dealParams.put("templateColSubject", template.getColSubject());
                	dealParams.put("templateWorkflowId", template.getWorkflowId());
                }
                //跟踪
                dealParams.put("trackParam", formTrackParam(params));

                collaborationApi.doZCDB(affair, summary, comment, dealParams);
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            if(isLock){
    			colLockManager.unlock(affairId);
    		}
            removeThreadContext();
            if(canDeal){
            	colManager.unlockCollAll(affairId,affair,summary);
            }
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
    /**
     * 立即发送，用于待发发送
     * @param affairId	Long | 必填 | 事项Id
     * @param params	Map<String, Object> | 必填 | 流程相关数据
     * <pre>
     * key : workflow_definition
     * value : map 
     * 		   key : workflow_node_peoples_input 节点选人信息
     * 		   key : workflow_node_condition_input 分支选人信息
     *         key : workflow_newflow_input    子流程
     * </pre>
     * 		
     * @return  Map<String, String>
     */
    @SuppressWarnings("unchecked")
	@POST
    @Path("sendImmediate/{affairId}")
    public Response sendImmediate(@PathParam("affairId") Long affairId, Map<String, Object> params) {        
        Map<String, String> ret = new HashMap<String, String>();
        String _summaryId = "";
        boolean isLock = false;
        try {
        	CtpAffair affair = affairManager.get(affairId);
        	if (affair == null) {
        		ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable34"));
        		ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        		return ok(ret);
        	}
        	String _affairId = String.valueOf(affairId);
        	_summaryId = String.valueOf(affair.getObjectId());
        	
        	 //锁检验
            isLock = colLockManager.canGetLock(Long.valueOf(_summaryId));
            if(!isLock) {
            	ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable30"));
            	LOGGER.error( AppContext.currentUserLoginName()+"正在发送中，不要重复点击 - sendImmediate - summaryId"+_summaryId);
            }
        	if(isLock){
        		
        		if(StateEnum.col_waitSend.getKey() != affair.getState().intValue()){
                	return null;
                }
                
        		Map<String, String> wfParamMap =  (Map<String, String>) params.get("workflow_definition");
        		colManager.transSendImmediate(_summaryId, _affairId, Boolean.FALSE, wfParamMap.get("workflow_node_peoples_input"), wfParamMap.get("workflow_node_condition_input"), wfParamMap.get("workflow_newflow_input"),"");
        	}
        		
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        }finally {
        	if(isLock){
        		colLockManager.unlock(Long.valueOf(_summaryId));
        	}
		}
      
        //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 处理加锁
     * @param summary	ColSummary | 必填 | 协同对象
     * @param user		User | 必填 | 用户
     * @param type		int | 必填 | 类型
     * @param ret
     * @return	boolean
     * @throws BusinessException 
     */
    private boolean lockWorkFlow(ColSummary summary, User user, int type, Map<String, String> ret) throws BusinessException{
        boolean canDeal = true;
        String[] wfLockCheck = wapi.checkWorkflowLock(summary.getProcessId(), user.getId().toString(), type);
        if(wfLockCheck == null){
            canDeal = false;
            ret.put(ERROR_KEY, ResourceUtil.getString("workflow.wapi.exception.msg002"));
        }else if("false".equals(wfLockCheck[0])){
            canDeal = false;
            ret.put(ERROR_KEY, wfLockCheck[1]);
        }
        return canDeal;
    }
    
    /**
     * 回退协同
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	String | 非必填 | 意见内容
     * hide		String | 非必填 | 是否隐藏意见, 1-隐藏意见，0 -不隐藏
     * isWFTrace	String | 非必填 | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * traceWorkflow String | 非必填 | 是否追溯流程, true - 追溯流程, false - 不追溯
     * attitude	String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * fileUrlIds	String | 非必填 | 附件ID串，使用,分隔
     * commentId	Long | 非必填 | 使用草稿的评论ID
     * affairId		Long | 必填 | 事项ID
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("stepBack/{affairId}")
    public Response stepBack(@PathParam("affairId") Long affairId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        CtpAffair affair = null;
        ColSummary summary = null;
        
        boolean canDeal = true;
        
        try {
          //获取对象
            affair = affairManager.get(affairId);
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            String[] wfCheck = wapi.canStepBack(toString(affair.getSubObjectId()), 
                        toString(summary.getCaseId()), toString(summary.getProcessId()), 
                                        toString(affair.getActivityId()), "", "");
            if("false".equals(wfCheck[0])){
                canDeal = false;
                ret.put(ERROR_KEY, wfCheck[1]);
            }
        } catch (Exception e1) {
            ret.put(ERROR_KEY, "stepback collaboration failed, cause by:" + e1.getLocalizedMessage());
            canDeal = false;
            LOGGER.error(e1.getMessage(), e1);
        }
        boolean isLock = false;
        if(canDeal){
            try {
                
                if(canDeal){
                    canDeal = lockWorkFlow(summary, user, 9, ret);
                }
                
                if(canDeal){
                    //检验是否可以处理
                    canDeal = checkCanDeal(summary, affair, ret);
                }
                if(canDeal){
                	isLock = colLockManager.canGetLock(affairId);
                    if(!isLock) {
                        canDeal = false;
                        ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable33"));
                        LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                    }
                }
                
                
                if (canDeal) {
                    
                    //意见
                    Comment comment = formComment(summary, affair, user, ColHandleType.stepBack, params);
                    //是否修改正文
                    String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                    String jsonComment = transComment2Str(comment,modifyFlag);
                    putThreadContext(jsonComment);
                   

                    Map<String,Object> tempMap=new HashMap<String, Object>();
                    tempMap.put("affairId", affair.getId().toString());
                    tempMap.put("summaryId", summary.getId().toString());
                    tempMap.put("targetNodeId", "");

                    String stepStopRet = colManager.transStepBack(tempMap);
                    if(Strings.isNotBlank(stepStopRet)){
                        ret.put(ERROR_KEY, stepStopRet);
                    }
                }
            } catch (Exception e) {
                ret.put(ERROR_KEY, e.getMessage());
                LOGGER.error("处理系统异常", e);
            } finally {
                if(isLock){
        			colLockManager.unlock(affairId);
        		}
                removeThreadContext();
                if(canDeal){
                	colManager.unlockCollAll(affairId,affair,summary);
                }
            }
        }

        //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 终止协同
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	String | 非必填 | 意见内容
     * hide		String | 非必填 | 是否隐藏意见, true-隐藏意见，false-不隐藏
     * tracking	String | 非必填 | 是否跟踪  ， true - 跟踪全部 ， false - 不跟踪
     * attitude	String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
     * fileUrlIds	String | 非必填 | 附件ID串，使用,分隔
     * commentId	Long | 非必填 | 使用草稿的评论ID
     * affairId		Long | 必填 | 事项ID
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("stepStop/{affairId}")
    public Response stepStop(@PathParam("affairId") Long affairId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        CtpAffair affair = null;
        ColSummary summary = null;
        boolean canDeal = false;
        boolean isLock = false;
        try {
        	Object _objectCaseId = params.get("caseId");
        	if(null != _objectCaseId){
        		String _caseId = (String)_objectCaseId;
            	String[] canStopFlow = wapi.canStopFlow(_caseId);
            	if(null != canStopFlow && canStopFlow.length>1 && "false".equals(canStopFlow[0])){
            		ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
            		ret.put(ERROR_KEY, canStopFlow[1]);
            		return ok(ret);
            	}
        	}
            //获取对象
            affair = affairManager.get(affairId);
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            //流程验证和加锁
            canDeal = lockWorkFlow(summary, user, 11, ret);
            
            if(canDeal){
                //检验是否可以处理
                canDeal = checkCanDeal(summary, affair, ret);
            }
            if(canDeal){
            	isLock = colLockManager.canGetLock(affairId);
                if(!isLock) {
                    canDeal = false;
                    ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable33"));
                    LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                }
            }
                        if (canDeal) {
                
                //意见
                Comment comment = formComment(summary, affair, user, null, params);
                //是否修改正文
                String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                String jsonComment = transComment2Str(comment,modifyFlag);
                putThreadContext(jsonComment);
               
                Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("affairId", affair.getId().toString());
                
                if(summary.getTempleteId() != null){
                	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                    //取出模板信息
                	tempMap.put("templateColSubject", template.getColSubject());
                	tempMap.put("templateWorkflowId", template.getWorkflowId());
                }     
               String stepStopRet = colManager.transStepStop(tempMap);
               if(Strings.isNotBlank(stepStopRet)){
                   ret.put(ERROR_KEY, stepStopRet);
               }
               if(ret.isEmpty()){
            	   ret.put("succ_msg", ResourceUtil.getString("coll.summary.validate.lable35"));
               }
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, Strings.isNotBlank(e.getMessage())? e.getMessage()+e.getLocalizedMessage() : e.getLocalizedMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            if(isLock){
    			colLockManager.unlock(affairId);
    		}
            removeThreadContext();
            if(canDeal){
            	colManager.unlockCollAll(affairId,affair,summary);
            }
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }

        return ok(ret);
    }
    
    /**
     *  发起人撤销协同
     *  {POST} /transRepal 
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	String | 非必填 | 意见内容 
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("transRepeal/{affairId}")
    public Response transRepal(@PathParam("affairId") Long affairId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        
        //前端参数完整性校验TODO...
        
        CtpAffair affair = null;
        ColSummary summary = null;
        boolean canDeal = false;
        try {
            
            //获取对象
            affair = affairManager.get(affairId);
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
//            boolean canDeal = true;
//            Map<String, String> checkMap = new HashMap<String, String>();
//            checkMap.put("summaryId", summary.getId().toString());
//            Map<String, String> checkRetMap = colManager.checkIsCanRepeal(checkMap);
//            if(checkRetMap != null && Strings.isNotBlank(checkRetMap.get("msg"))){
//                canDeal = false;
//                ret.put(ERROR_KEY, checkRetMap.get("msg"));
//            }
            
//            if(canDeal){
            canDeal = isValidAffair(affairId.toString(), ret,affair.getNodePolicy());
//            }
            
            if(canDeal){
                String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), 
                                     toString(summary.getProcessId()), toString(affair.getActivityId()));
                if("false".equals(wfCheck[0])){
                    canDeal = false;
                    ret.put(ERROR_KEY, wfCheck[1]);
                }
            }
            
            if(canDeal){
                canDeal = lockWorkFlow(summary, user, 12, ret);
            }
            
            //TODO 表单collaborationFormBindEventListener.achieveTaskType
            
            
            if (canDeal) {
                
                Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("affairId", affair.getId().toString());
                tempMap.put("summaryId", summary.getId().toString());
                tempMap.put("repealComment", params.get("content"));
                
                //流程会用到这个参数
                tempMap.put("isWFTrace", ParamUtil.getString(params, "isWFTrace", "0"));
                
                String repalRet = colManager.transRepal(tempMap);
                
                //formatAtWho(comment, affair, params);
                
                if(Strings.isNotBlank(repalRet)){
                    ret.put(ERROR_KEY, repalRet);
                }else{
                    // 清除表单缓存
                }
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
        	if(canDeal){
        		colManager.unlockCollAll(affairId,affair,summary);
        	}
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     *  处理人撤销协同
     *  {POST} /repeal 
     *  
     * @param affairId	Long | 必填 | 事项ID
     * @param params	Map<String, Object> | 必填 | 其他参数
     * <pre>
     * content	   String | 非必填 | 意见内容 
     * commentId   String | 非必填 | 回复意见ID
	 * hide        String | 非必填 | 意见隐藏
     * traceWorkflow String | 非必填 | 是否追溯流程, true - 追溯流程, false - 不追溯
	 * attitude    String | 非必填 | 态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
	 * likeSummary String | 非必填 | 点赞
	 * fileJson    String | 非必填 | 附件信息    
     * </pre>
     * @return  Map<String, String>
     */
    @POST
    @Path("repeal/{affairId}")
    public Response repeal(@PathParam("affairId") Long affairId, Map<String, Object> params){
        
        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        
        //前端参数完整性校验TODO...
        
        CtpAffair affair = null;
        ColSummary summary = null;
        
        boolean canDeal = true;
        try {
            
            //获取对象
            affair = affairManager.get(affairId);
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("summaryId", summary.getId().toString());
            Map<String, String> checkRetMap = colManager.checkIsCanRepeal(checkMap);
            if(checkRetMap != null && Strings.isNotBlank(checkRetMap.get("msg"))){
                canDeal = false;
                ret.put(ERROR_KEY, checkRetMap.get("msg"));
            }
            
            if(canDeal){
                canDeal = isValidAffair(affairId.toString(), ret,affair.getNodePolicy());
            }
            
            if(canDeal){
                String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), 
                                     toString(summary.getProcessId()), toString(affair.getActivityId()));
                if("false".equals(wfCheck[0])){
                    canDeal = false;
                    ret.put(ERROR_KEY, wfCheck[1]);
                }
            }
            
            if(canDeal){
                canDeal = lockWorkFlow(summary, user, 12, ret);
            }
            
            
			
            if (canDeal) {
                
                Map<String,Object> tempMap=new HashMap<String, Object>();
                tempMap.put("summaryId", summary.getId().toString());
                tempMap.put("affairId", affair.getId().toString());
                tempMap.put("repealComment", params.get("content"));

                //意见
                Comment comment = formComment(summary, affair, user, null, params);
                //是否修改正文
                String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                String jsonComment = transComment2Str(comment,modifyFlag);
                putThreadContext(jsonComment);
                
                String repalRet = colManager.transRepal(tempMap);
                if(Strings.isNotBlank(repalRet)){
                    ret.put(ERROR_KEY, repalRet);
                }
            }
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        } finally {
            removeThreadContext();
            if( canDeal ){
            	colManager.unlockCollAll(affairId,affair,summary);
            }
        }

      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        return ok(ret);
    }
    
    /**
     *  跟踪或者取消跟踪操作
     *  {GET} /getTrackInfo 
     *  @param affairId  Long | 必填 | 事项ID
     *  @return  Map<String, String>
     */
    @GET
    @Path("trackInfo/{affairId}")
    public Response getTrackInfo(@PathParam("affairId") Long affairId){
        
        Map<String, String> ret = new HashMap<String, String>();

        try {
            
            //获取对象
            CtpAffair affair = affairManager.get(affairId);

            //OA-117897 REST接口测试，已结束的流程依然可以设置跟踪状态
            if (!affair.isFinish()) {
                TrackAjaxTranObj obj = new TrackAjaxTranObj();
                obj.setAffairId(affairId.toString());
                obj.setObjectId(affair.getObjectId().toString());
                obj.setOldTrackType(affair.getTrack() == null ? "0" : affair.getTrack().toString());

                String newTrackType = (affair.getTrack() == null || affair.getTrack() == 0) ? "1" : "0";

                obj.setNewTrackType(newTrackType);
                obj.setTrackMemberIds("");
                obj.setSenderId(affair.getSenderId().toString());

                colManager.getTrackInfo(obj);
                if ("0".equals(newTrackType)) {
                    ret.put("succ_msg", ResourceUtil.getString("coll.summary.validate.lable36"));
                } else {
                    ret.put("succ_msg", ResourceUtil.getString("coll.summary.validate.lable37"));
                }
            }
            else{
            	ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable38"));
            }
            
        } catch (Exception e) {
            ret.put(ERROR_KEY, e.getMessage());
            LOGGER.error("处理系统异常", e);
        }

        //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    
    
    /*
     * 转化成String
     */
    private String toString(Object o){
        String ret = "";
        if(o != null){
            ret = o.toString();
        }
        return ret;
    }
    
    /*
     * 检验协同是否可以处理，防止两个客户端同时处理
     */
    private boolean checkCanDeal(ColSummary summary, CtpAffair affair, Map<String, String> ret) throws NumberFormatException, BusinessException{
        
        boolean canDeal = true;
        
      //锁检验
       canDeal = isValidAffair(affair.getId().toString(), ret,affair.getNodePolicy());

        if(canDeal){
          //状态检验
            String msg = checkAffairState(affair, StateEnum.col_pending);
            if(Strings.isNotBlank(msg)){
                canDeal = false;
                ret.put(ERROR_KEY, msg);
            }
        }
        
        if(canDeal){
          //检查代理，避免不是处理人也能处理了。
            String agentMsg =  ColUtil.ajaxCheckAgent(affair.getMemberId(), affair.getSubject(), ModuleType.collaboration);
            if(Strings.isNotBlank(agentMsg)){
                canDeal = false;
                ret.put(ERROR_KEY, agentMsg);
            }
        }
        
        return canDeal;
    }
    
    /**
     * 原PC验证方式验证方式
     * @return
     * @throws BusinessException 
     * @throws NumberFormatException 
     */
    private boolean isValidAffair(String affairId, Map<String, String> ret,String pageNodePolicy) throws NumberFormatException, BusinessException{
        
        boolean canDeal = true;
      //状态检验
        String msg = colManager.checkAffairValid(affairId,pageNodePolicy);
        if(Strings.isNotBlank(msg)){
            canDeal = false;
            ret.put(ERROR_KEY, msg);
        }
        return canDeal;
    }
    
   private boolean isValidAffair(CtpAffair affair, Map<String, String> ret,String pageNodePolicy){
        
        boolean canDeal = true;
      //状态检验
        String msg = colManager.checkAffairValid(affair,false,pageNodePolicy);
        if(Strings.isNotBlank(msg)){
            canDeal = false;
            ret.put(ERROR_KEY, msg);
        }
        return canDeal;
    }
    /**
     * 组装跟踪参数
     * @param params
     * @return
     */
    private Map<String, String> formTrackParam(Map<String, Object> params){
        
     // 跟踪
        Map<String, String> trackParam = new HashMap<String, String>();
        String tracking = ParamUtil.getString(params, "tracking");
        String isTracking = "1".equals(tracking) ? "1" : null;
        trackParam.put("isTrack", isTracking);
        
        return trackParam;
    }
    
    /**
     * 将意见转化成字符串
     * @Author      : xuqw
     * @Date        : 2015年12月14日下午1:04:15
     * @param comment
     * @return
     */
    private String transComment2Str(Comment comment,String modifyFlag){
        
        Map<String, Object> ret = new HashMap<String, Object>();
        
      //设置意见参数
        Map<String, String> comment_deal = new HashMap<String, String>();
        comment_deal.put("id", toString(comment.getId()));
        comment_deal.put("draftCommentId", toString(comment.getId()));
        comment_deal.put("pid", toString(comment.getPid()));
        comment_deal.put("clevel", toString(comment.getClevel()));
        comment_deal.put("path", toString(comment.getPath()));
        comment_deal.put("moduleType", toString(comment.getModuleType()));
        comment_deal.put("moduleId", toString(comment.getModuleId()));
        comment_deal.put("extAtt1", toString(comment.getExtAtt1()));
        comment_deal.put("extAtt4", toString(comment.getExtAtt4()));
        comment_deal.put("ctype", toString(comment.getCtype()));
        comment_deal.put("content", toString(comment.getContent()));
        comment_deal.put("hidden", toString(comment.isHidden()));
        comment_deal.put("showToId", toString(comment.getShowToId()));
        comment_deal.put("affairId", toString(comment.getAffairId()));
        comment_deal.put("relateInfo", toString(comment.getRelateInfo()));
        comment_deal.put("pushMessage", toString(comment.isPushMessage()));
        comment_deal.put("pushMessageToMembers", toString(comment.getPushMessageToMembers()));
        comment_deal.put("praiseInput", comment.getPraiseToSummary()?"1":"0");
        
        ret.put("comment_deal", comment_deal);
        if("1".equals(modifyFlag)){
        	Map<String,String> modifyMap = new HashMap<String,String>();
        	modifyMap.put("modifyFlag", modifyFlag);
        	ret.put("colSummaryData", modifyMap);
        }
        return JSONUtil.toJSONString(ret);
    }
    
    /**
     * 组装意见
     * @param params
     * @return
     * @throws BusinessException 
     */
    private Comment formComment(ColSummary summary, CtpAffair affair,
            User user, ColHandleType handleType, Map<String, Object> params) throws BusinessException{
        
        // 意见
        Comment comment = new Comment();

        Long commentId = getLong(params, "commentId", null);
        comment.setId(commentId);

        String content = ParamUtil.getString(params, "content");
        
        comment.setContent(Strings.removeEmoji(content));

        // 意见隐藏
        String hide = ParamUtil.getString(params, "hide");
        boolean isHide = "1".equals(hide);
        comment.setHidden(isHide);

        StringBuilder showToIds = new StringBuilder();
        if (isHide) {
            showToIds.append("Member|").append(summary.getStartMemberId());
        }

        comment.setShowToId(showToIds.toString());
        comment.setTitle(summary.getSubject());
        comment.setPid(0l);
        comment.setClevel(1);
        //path先这样判空设置，正常情况肯定能取到值的，待平台方改好后直接取值即可
        comment.setPath(AppContext.getCurrentUser().getUserAgentFrom()==null?"pc":AppContext.getCurrentUser().getUserAgentFrom());
        comment.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
        comment.setModuleId(summary.getId());
        comment.setCtype(CommentType.comment.getKey());
        comment.setAffairId(affair.getId());
        
        comment.setCreateDate(DateUtil.currentDate());

        // 态度
        String attitude = ParamUtil.getString(params, "attitude");
        String attitudeCode = ParamUtil.getString(params, "attitudeCode");
        String actionAttitude = ParamUtil.getString(params, "actionAttitude");
        //封装有值的时候,默认优先使用封装值
        if (Strings.isNotBlank(actionAttitude)) {
        	comment.setExtAtt1(actionAttitude);
        	comment.setExtAtt4(ParamUtil.getString(params, "actionAttitudeCode"));
		} else {
			comment.setExtAtt1(attitude);
			comment.setExtAtt4(attitudeCode);
		}

        // 点赞
        String likeSummary = ParamUtil.getString(params, "likeSummary");
        comment.setPraiseToSummary("true".equals(likeSummary));

        // 附件
        /*String fileUrlIds = ParamUtil.getString(params, "fileUrlIds", null);
        List<Attachment> attrs = createAttachments(summary.getId(), fileUrlIds);
        String relateInfo = "[]";
        if (Strings.isNotEmpty(attrs)) {
            relateInfo = JSONUtil.toJSONString(attrs);
        }*/
        //现在H5传回附件参数为fileJson对象json
        String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
        comment.setRelateInfo(relateInfo);
        
        //at信息
        formatAtWho(comment, affair, params);
        
      //判断是否代理人
        Long userId = user.getId();
        if(!userId.equals(affair.getMemberId())){
            comment.setExtAtt2(user.getName());
        }
        if (handleType == ColHandleType.wait) {
            comment.setExtAtt1("");
            comment.setExtAtt3("collaboration.dealAttitude.temporaryAbeyance");
        }
        else if(handleType == ColHandleType.specialback || handleType == ColHandleType.stepBack ){
            comment.setExtAtt3("collaboration.dealAttitude.rollback");
        }
        return comment;
    }
    
    /**
     * 拼装at相关信息
     * 
     * @param comment 评论
     * @param affair 当前事项
     * @param params 评论参数
     * @throws BusinessException 
     *
     * @Author      : xuqw
     * @Date        : 2016年11月20日下午5:22:02
     *
     */
    private void formatAtWho(Comment comment, CtpAffair affair, Map<String, Object> params) throws BusinessException{
        
      //@人员
        String atWhoMembers = ParamUtil.getString(params, "atWhoSelected", "");
        if(Strings.isNotBlank(atWhoMembers) && Strings.isNotBlank(comment.getContent())){
            List<Map<String, String>> atWhos = JSONUtil.parseJSONString(atWhoMembers, List.class);
            if(Strings.isNotEmpty(atWhos)){
                
                Collections.sort(atWhos, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        
                        int ret = 1;
                        
                        String name1 = o1.get("name");
                        String name2 = o2.get("name");
                        if(name1 != null){
                            if(name2 == null){
                                ret = -1;
                            }else if(name1.length() > name2.length()){
                                ret = -1;
                            }else{
                                ret = 1;
                            }
                        }
                        return ret;
                    }
                });
                
                
                //拼装html
                boolean hasAtAll = false;
                // String htmlContent = Strings.toHTML(comment.getContent());
                List<String> fiterList = new ArrayList<String>();
                //用于防止短名字替换长名字， 比如 ： user1, user111， 这种情况
//                Map<String, String> richText = new HashMap<String, String>();
                
                List<Map<String, String>> listAtMembers = new ArrayList<Map<String,String>>();
                Map<String, String> mapAtMembers;
                for(Map<String, String> item : atWhos){
                    if(fiterList.contains(item.get("name"))){
                        continue;
                    }
                    mapAtMembers = new HashMap<String, String>();
                    StringBuilder it = new StringBuilder("<button class=\"atwho-inserted\" ");
                    it.append(" checkaffair=\"").append(affair.getId()).append("\"");
                    it.append(" data-atwho-at-query=\"@\"");
                    it.append(" data-atwho-at-validate=\"@").append(item.get("name")).append("\"");
                    it.append(" atinfo=\"@{affairId:'").append(item.get("affairId")).append("',memberId:'").append(item.get("memberId")).append("'}\"");
                    it.append(" onclick=\"return false;\" contenteditable=\"false\" style=\"color: rgb(49, 142, 217); font-size: 14px; border-width: 0px; padding: 0px; margin: 0px; background-color: transparent; background-image: none;\"");
                    it.append(">@").append(item.get("name")).append("</button>");
                    
                    fiterList.add(item.get("name"));
                    String newKey = UUID.randomUUID().toString();
//                    richText.put(newKey, it.toString());
                    // htmlContent = htmlContent.replace("@" + item.get("name"), newKey);
                    
                    if("All".equals(item.get("affairId"))){
                    	hasAtAll = true;
                    }else{
                    	mapAtMembers.put("affairId", item.get("affairId"));
                    	mapAtMembers.put("memberId", item.get("memberId"));
                    	listAtMembers.add(mapAtMembers);
                    }
                }
                
                /*if(richText.size() > 0){
                    for(String key : richText.keySet()){
                        htmlContent = htmlContent.replace(key, richText.get(key));
                    }
                }*/
                
                if(hasAtAll){
                    Map<String, String> p = new HashMap<String, String>();
                    p.put("summaryId", affair.getObjectId().toString());
                    List<Map<String, Object>> ret = colManager.pushMessageToMembersList(p);
                    if(Strings.isNotEmpty(ret)){
                        for(Map<String, Object> item : ret){
                        	mapAtMembers = new HashMap<String, String>();
                        	mapAtMembers.put("affairId", item.get("id").toString());
                        	mapAtMembers.put("memberId", item.get("memberId").toString());
                        	listAtMembers.add(mapAtMembers);
                        }
                    }
                }
                if(Strings.isNotEmpty(listAtMembers)){
                	comment.setPushMessageToMembers(JSONUtil.toJSONString(listAtMembers));
                }
                //comment.setRichContent(htmlContent);
            }
        }
    }
    
    
    /**
     * 处理时查看affair的状态
     * @param affair
     * @param expact
     * @return
     */
    private String checkAffairState(CtpAffair affair, StateEnum expact){
        
        String ret = "";
        
        if(affair==null || affair.getState() != expact.key()){
            ret = ColUtil.getErrorMsgByAffair(affair);
        }
        return ret;
    }
    
    
	/**
	 * 获取待办事项
	 * @param params Map<String,String> | 必填 | 其他参数
	 * <pre>
     *  pageNo      int| 必填 | 分页信息，第几页(默认第一页)
     *  pageSize    int| 必填 | 分页信息，每页多少条数据(默认20条)
     *  startMemberName String | 非必填  | 发起人姓名(用于查询)
     *  templeteIds     String | 非必填  | 模板ID
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 待办按发起时间查询(用于查询) createDate=2016-10-01#2016-11-17
     * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("pendingAffairs")
	public Response findPendingAffairs(Map<String,String> params){
		User currentUser = AppContext.getCurrentUser();
		params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
		params.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_pending.key()));
        params.put("needSummary", "1");//m3需要查询数量
		FlipInfo fi = super.getFlipInfo();
		fi.setPage(Integer.valueOf(params.get("pageNo")));
		fi.setSize(Integer.valueOf(params.get("pageSize")));
		try {
			fi = collaborationApi.findPendingAffairs(fi, params);
			convertListSimpleVO2Affairs(fi, currentUser, StateEnum.col_pending.key());
		} catch (Exception e) {
			LOGGER.error("获取待办事项失败！", e);
		}
		return ok(fi);
	}
	/**
	 * 获取已办事项
	 * @param params Map<String,String> | 必填 | 其他参数 
	 * <pre>
     *  pageNo      int| 必填 | 分页信息，第几页(默认第一页)
     *  pageSize    int| 必填 | 分页信息，每页多少条数据(默认20条)
     *  bodyType    String | 非必填 | 正文类型 ，传值为如下的数字[标准正文(10)\表单(20)\OfficeWord(41)\OfficeExcel(42)\WpsWord(43)\ WpsExcel(44)\Pdf(45)]
     *  startMemberName String | 非必填  | 发起人姓名(用于查询)
     *  templeteIds     String | 非必填  | 模板ID
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 发起时间(用于查询) createDate=2016-10-01#2016-11-17
     * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("doneAffairs")
	public Response findDoneAffairs(Map<String,String> params){
		User currentUser = AppContext.getCurrentUser();
        // 重大项目(Major projects):转储数据支持转发、邮件、归档
        String dumpData = ParamUtil.getString(params, "dumpDataColDoneContain", "false");
        params.put(ColQueryCondition.dumpData.name(),dumpData);
		String bodyType = ParamUtil.getString(params, "bodyType","");
		if (Strings.isNotBlank(bodyType)) {
			params.put(ColQueryCondition.bodyType.name(),bodyType);
		}
		params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
		params.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_done.key()));
        params.put("needSummary", "1");//m3需要查询数量
		FlipInfo fi = super.getFlipInfo();
		fi.setPage(Integer.valueOf(params.get("pageNo")));
		fi.setSize(Integer.valueOf(params.get("pageSize")));
		try {
			fi = collaborationApi.findDoneAffairs(fi, params);
			convertListSimpleVO2Affairs(fi, currentUser, StateEnum.col_done.key());
		} catch (Exception e) {
			LOGGER.error("获取待办事项失败！", e);
		}
		return ok(fi);
	}
	
	/**
	 *  获取已发事项
	 * @param params Map<String,String> | 必填 | 其他参数 
	 * <pre>
     *  pageNo      int| 必填 | 分页信息，第几页(默认第一页)
     *  pageSize    int| 必填 | 分页信息，每页多少条数据(默认20条)
     *  bodyType    String | 非必填 | 正文类型 ，传值为如下的数字[标准正文(10)\表单(20)\OfficeWord(41)\OfficeExcel(42)\WpsWord(43)\ WpsExcel(44)\Pdf(45)]
     *  templeteIds     String | 非必填  | 模板ID
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 发起时间(用于查询)createDate=2016-10-01#2016-11-17
     * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("sentAffairs")
	public Response findSentAffairs(Map<String,String> params){
		User currentUser = AppContext.getCurrentUser();
        // 重大项目(Major projects):转储数据支持转发、邮件、归档
        String dumpData = ParamUtil.getString(params, "dumpDataColSentContain", "false");
        params.put(ColQueryCondition.dumpData.name(),dumpData);
		String bodyType = ParamUtil.getString(params, "bodyType","");
		if (Strings.isNotBlank(bodyType)) {
			params.put(ColQueryCondition.bodyType.name(),bodyType);
		}
		params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
		params.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_sent.key()));
        params.put("needSummary", "1");//m3需要查询数量
		FlipInfo fi = super.getFlipInfo();
		fi.setPage(Integer.valueOf(params.get("pageNo")));
		fi.setSize(Integer.valueOf(params.get("pageSize")));
		try {
			fi = collaborationApi.findSentAffairs(fi, params);
			convertListSimpleVO2Affairs(fi, currentUser, StateEnum.col_sent.key());
		} catch (Exception e) {
			LOGGER.error("获取待办事项失败！", e);
		}
		return ok(fi);
	}
	
	/**
	 * 获取待发事项
	 * @param params Map<String, String> | 必填 | 其他参数 
	 * <pre>
     *  pageNo      int| 必填 | 分页信息，第几页(默认第一页)
     *  pageSize    int| 必填 | 分页信息，每页多少条数据(默认20条)
     *  bodyType    String | 非必填 | 正文类型 ，传值为如下的数字[标准正文(10)\表单(20)\OfficeWord(41)\OfficeExcel(42)\WpsWord(43)\ WpsExcel(44)\Pdf(45)]
     *  templeteIds     String | 非必填  | 模板ID
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 发起时间(用于查询)createDate=2016-10-01#2016-11-17
     * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("waitSentAffairs")
	public Response findWaitSentAffairs(Map<String,String> params){
		User currentUser = AppContext.getCurrentUser();
		String bodyType = ParamUtil.getString(params, "bodyType","");
		if (Strings.isNotBlank(bodyType)) {
			params.put(ColQueryCondition.bodyType.name(),bodyType);
		}
		params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
		params.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_waitSend.key()));
        params.put("needSummary", "1");//m3需要查询数量
		FlipInfo fi = super.getFlipInfo();
		fi.setPage(Integer.valueOf(params.get("pageNo")));
		fi.setSize(Integer.valueOf(params.get("pageSize")));
		try {
			fi = collaborationApi.findWaitSentAffairs(fi, params);
			convertListSimpleVO2Affairs(fi, currentUser, StateEnum.col_waitSend.key());
		} catch (Exception e) {
			LOGGER.error("获取待办事项失败！", e);
		}
		return ok(fi);
	}
	
	@POST
	@Path("handoverAffairs")
	public Response findHandoverAffairs(Map<String, String> params){
		FlipInfo fi = super.getFlipInfo();
		fi.setPage(Integer.valueOf(params.get("pageNo")));
		fi.setSize(Integer.valueOf(params.get("pageSize")));
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("type", HandoverConstant.Type.toMe.name());
		condition.put("state", StateEnum.col_sent.key()+","+StateEnum.col_done.key());
		condition.put("subject", params.get("subject"));
		condition.put("memberName", params.get("memberName"));
		
		try {
			fi = handoverManager.listAffairs(fi, condition);
		} catch (BusinessException e) {
			LOGGER.error("获取交接事项失败！", e);
		}
		return ok(fi);
	}
	
	@SuppressWarnings("unchecked")
    private void convertListSimpleVO2Affairs(FlipInfo info, User user, int state) {
        if (info != null) {
            List<ColListSimpleVO> list = info.getData();
            if (Strings.isNotEmpty(list)) {
                List<SummaryListVO> affairs = new ArrayList<SummaryListVO>();
                for (ColListSimpleVO c : list) {
                    if(state == StateEnum.col_done.key()){
                        //已发列表不显示回退信息
                        c.setBackFromId(null);
                    }
                    SummaryListVO vo = new SummaryListVO(c);
                    vo.setCurrrentLoginAccountId(String.valueOf(user.getAccountId()));
                    affairs.add(vo);
                }
                info.setData(affairs);
            }
        }
    }
	
	
	/**
     *  获取协同正文
     * @param summary ColSummary| 必填 |  协同对象
     * @param affair  CtpAffair| 必填 | affair对象
     * @param openFrom String | 必填 | 来源，用于权限判断， 目前不知道这个参数干什么用
     * @return 
     * 
     */
    private ColSummaryContentVO findSummaryContent(ColSummary summary, CtpAffair affair, CtpTemplate template,String openFrom, String pigeonholeType, String operationId) {

        ColSummaryContentVO contentVO = null;

        // 正文
        try {
          
            String rightId = "";
            boolean isCAP4 = false;
            
            //表单才进行权限查看
            if(ColUtil.isForm(summary.getBodyType())){
                
                // 归档的时候有视图设置， 直接使用归档的视图设置
                if(ColOpenFrom.docLib.name().equals(openFrom)){
                    boolean isAcountPighole = String.valueOf(PigeonholeType.edoc_account.ordinal()).equals(pigeonholeType);
                    String docLibrightid = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair,template,isAcountPighole, "");
                    operationId = docLibrightid;
                }
                
                if (Strings.isBlank(operationId) || ColOpenFrom.subFlow.name().equals(openFrom)) {
                    rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, template, false,null);
                } else {
                    rightId = operationId;
                    if(Strings.isNotBlank(rightId)){
                        rightId = rightId.replaceAll("[|]","_");
                    }
                }
                
                colManager.addFormRightIdToFormCache(rightId, summary.getFormAppid());
                
                //是否为CAP4表单
                if(template != null){
                    isCAP4 = capFormManager.isCAP4Form(template.getFormAppId());
                }
            }else if(null != summary.getFormAppid()){
            	isCAP4 =  capFormManager.isCAP4Form(summary.getFormAppid());
            }
            
            LOGGER.info(AppContext.currentUserName()+"查找正文调用接口："+",summary.getId():"+summary.getId()+",rightId:"+rightId + ", 是否是CAP4：" + isCAP4);
            
            List<CtpContentAllBean> contentList = null;
            if(isCAP4){
                List<CtpContentAll> ctpContentAll = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId(), false);
                if(Strings.isNotEmpty(ctpContentAll)){
                    contentList = Collections.singletonList(new CtpContentAllBean(ctpContentAll.get(0)));
                }
            }else {
                contentList = ctpMainbodyManager.transContentViewResponse(ModuleType.collaboration,
                        summary.getId(), CtpContentAllBean.viewState_readOnly, rightId, 0, -1l);
            }
            
            int size = contentList.size();
            LOGGER.info(AppContext.currentUserName()+"正文列表："+size+",id:"+ (size >0 ? contentList.get(0).getId() : -1l));

            CtpContentAllBean contentAllBean = contentList.get(0);

            if(isCAP4){
            	Map<String, String> formRightInfo = capFormManager.getFormRightInfo(contentAllBean.getContentTemplateId(),rightId,contentAllBean);
            	contentAllBean.putExtraMap("isLightForm", "1".equals(formRightInfo.get("phone")) ? true : false);
            	contentAllBean.putExtraMap("hasPCForm", "1".equals(formRightInfo.get("pc")) ? true : false);
            }
            
            contentVO = ColSummaryContentVO.valutOf(contentAllBean);
            contentVO.setFormRightId(rightId);
            contentVO.setCAP4(isCAP4);
            
            if(ColUtil.isForm(contentAllBean.getContentType())){
                //表单不需要正文
                contentAllBean.setContentHtml("");
                
                //多视图或正文
                if(size > 1){
                    List<Map<String, String>> cList = new ArrayList<Map<String,String>>(contentList.size());
                    int index = -1;
                    for(CtpContentAllBean b : contentList){
                        index++;
                        Map<String, Object> ext = b.getExtraMap();
                        if(ext != null){
                            Map<String, String> m = new HashMap<String, String>();
                            m.put("index", String.valueOf(index));
                            m.put("viewTitle", String.valueOf(ext.get("viewTitle")));
                            m.put("isOffice", ext.get("isOffice") == null ? "false" : String.valueOf(ext.get("isOffice")));
                            m.put("isLightForm", ext.get("isLightForm") == null ? "false" : String.valueOf(ext.get("isLightForm")));
                            
                            String content = b.getContent();
                            if("true".equals(m.get("isOffice")) && content != null){
                                contentVO.setHasOffice(true);
                                Map<String, String> c = JSONUtil.parseJSONString(content, Map.class);
                                m.put("extension", c.get("extension"));
                                contentVO.setNeedSkipMainBody(c.get("needSkipMainBody"));
                                m.put("fileId", c.get("fileId"));
                                if(Strings.isDigits(contentAllBean.getContent())){
                            		Long contentId = Long.valueOf(contentAllBean.getContent());
                            		List<Attachment> attachments = attachmentManager.getByReference(contentAllBean.getContentDataId(), contentId);
                            		if(attachments != null){
                            			V3XFile v3XFile = fileManager.getV3XFile(attachments.get(0).getFileUrl());
                                    	if(v3XFile != null){
                                    		m.put("lastModified",DateUtil.format(v3XFile.getUpdateDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
                                    	}
                            		}
                            	}
                            }
                            cList.add(m);
                        }
                    }
                    contentVO.setContentList(cList);
                }
            }
            
            //表单转发协同
            if(Strings.isNotBlank(summary.getForwardMember()) 
                    && summary.getParentformSummaryid() != null && !summary.getCanEdit()){
                
                String contentHtml = contentAllBean.getContentHtml();
                if(null != contentHtml && contentHtml.indexOf("class=\"lightForm-phonePage\"") == -1){
                    //移动表单的视图

                    //表单转发的时候由表单组件进行查看
                    contentAllBean.setContentHtml("");
                    contentVO.setForwardForm(true);
                }
            }
            
            //非html或表单正文需要获取最后一次更新时间和文件后缀
            if(!Integer.valueOf(MainbodyType.HTML.getKey()).equals(contentAllBean.getContentType())
                    && !ColUtil.isForm(contentAllBean.getContentType())
                    && contentAllBean.getContentDataId() != null){
                
                V3XFile v3XFile = fileManager.getV3XFile(contentAllBean.getContentDataId());
                if(v3XFile != null){
                    contentVO.setLastModified(DateUtil.format(v3XFile.getUpdateDate(), DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
                    contentVO.setSubfix(fileManager.getOfficeSuffix(v3XFile));
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取协同正文失败."+e.getMessage(), e);
        }

        return contentVO;
    }
    
    /**
	 *  {GET} 删除待发中的数据
	 * @param affairId
	 * @return
	 */
	/*@SuppressWarnings("finally")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("waitSentAffair/remove/{affairId}")
	public Response deleteWaitSentAffair(@PathParam("affairId") Long affairId){
		Map<String,String> success = new HashMap<String,String>();
		if (affairId != null) {
			try {
				collaborationApi.deleteAffair(ColListType.draft.name(), affairId);
				success.put("message", "true");
			} catch (BusinessException e) {
				LOGGER.error("删除待发中的数据失败！", e);
				success.put("message", "false");
			} finally{
				return ok(success);
			}
		}
		return ok(success);
	}
	*/
    /**
	 *  催办
	 *  @param map Map<String, Object> | 必填 | 其他参数
	 * <pre>
     *    isAllHasten String | 必填 |  是否催办全部(true,false)
     *    memberIds	String | 非必填 | 被催办人ID					
     *    content   String | 非必填 | 催办信息内容
     *    app		String | 非必填 | 等于4为公文催办
     *    affairId	  Long | 非必填 |   事项ID
     * </pre>
	 * @param {Map}params
	 * @return
	 */
	@POST
	@Path("hasten")
	public Response hasten(Map<String,Object> map){
		
		Map<String,String> messages = new HashMap<String,String>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		
        //是否催办全部
        String isAllHasten = ParamUtil.getString(map, "isAllHasten");
        if(Strings.isEmpty(isAllHasten) || (!"true".equals(isAllHasten) && !"false".equals(isAllHasten))){
        	messages.put("message", ResourceUtil.getString("collaboration.error.params"));
        	return ok(messages);
        }
        
		//催办人
		Long memberId = 0l;
        Long summaryId = 0l;
		Long affairId = getLong(map, "affairId", null);
		
		try {
		    //发起人催办
            if(affairId != null){
                CtpAffair affair = affairManager.get(affairId);
                if(!ColUtil.isAfffairValid(affair)){
                	messages.put("message", ResourceUtil.getString("collaboration.error.hasten1"));
                	return ok(messages);
                }
                memberId = affair.getMemberId();
                summaryId = affair.getObjectId();
            }else{//催办人催办
                User user = AppContext.getCurrentUser();
                memberId = user.getId();
            }
        
            //催办个人
            String memberIds = ParamUtil.getString(map, "memberIds", "");
            List<Long> memberIdList = new ArrayList<Long>();
            String[] idArray = memberIds.split(",");
            
    		if(!"true".equals(isAllHasten)){
    		    if(Strings.isNotBlank(memberIds)){
    		        for(String id : idArray){
    		            Long mId = Long.parseLong(id);
    		            memberIdList.add(mId);
    		            
    		            //催办后续需要使用activeId
    		            List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.collaboration,
    		                    summaryId, mId);
    		            Long activityId = null;
    		            for(CtpAffair affair : affairs){
    		                if(affair.getState() == StateEnum.col_pending.getKey()){
    		                    activityId = affair.getActivityId();
    		                    params.put("activityId", activityId.toString());
    		                    break;
    		                }
    		            }
    		        }
    		    }
    		}else{
				for(String id : idArray){
					if(Strings.isNotBlank(id)){
						Long mId = Long.parseLong(id);
						memberIdList.add(mId);
					}
				}
    		}
    		
    		params.put("personIds", memberIdList);
    		
		} catch (BusinessException e1) {
            LOGGER.error("催办异常", e1);
        }
		
		String activityId = ParamUtil.getString(map, "activityId", "");
		if(Strings.isNotBlank(activityId)){
			params.put("activityId", activityId);
		}
		params.put("content", map==null?null:map.get("content"));
		params.put("isAllHasten", isAllHasten);
		params.put("app", map==null?null:map.get("app"));
		params.put("checkUser", "true");
		
		try {
			String success = superviseManager.hastenSummary(memberId, summaryId, params);
			messages.put("message", success);
		} catch (BusinessException e) {
			LOGGER.error("催办失败！", e);
		}
		
		return ok(messages);
	}
	
	/**
	 *  取回
     *  @param affairId Long | 必填 |  事项ID
     *  @param isSaveOpinion boolean | 必填 | 是否对愿意见修改
	 * @return  Map<String,String>
	 */
	@GET
	@Path("takeBack")
	public Response takeBack(@QueryParam("affairId") Long affairId,@QueryParam("isSaveOpinion") boolean isSaveOpinion) {
		
	    Map<String,String> messages = new HashMap<String,String>();
	    User user = AppContext.getCurrentUser();
	    
	    CtpAffair affair = null;
	    ColSummary summary = null;
	    boolean isLock = false;
		try {
			/*  取回逻辑做了判断了， 这里不做这个潘丹
			isLock = colLockManager.canGetLock(affairId);
            if(!isLock) {
            	String msg =  ResourceUtil.getString("collaboration.summary.notDuplicateSub");
                LOGGER.error( AppContext.currentUserLoginName()+msg+"-takeBack,affairId"+affairId);
            }
            if(isLock){
            }
            */
            	
        	//获取对象
        	affair = affairManager.get(affairId);
        	summary = collaborationApi.getColSummary(affair.getObjectId());
        	
        	//检验锁和状态是否正常
        	boolean canDeal = isValidAffair(affair.getId().toString(), messages,affair.getNodePolicy());
        	
        	if(canDeal){
        		String wfCheck = wapi.canTakeBack(ApplicationCategoryEnum.collaboration.name(), toString(summary.getProcessId()),
        				toString(affair.getActivityId()), toString(affair.getSubObjectId()));
        		Map<String, String> wfCheckMap = JSONUtil.parseJSONString(wfCheck, Map.class);
        		if("false".equals(wfCheckMap.get("canTakeBack"))){
        			canDeal = false;
        			messages.put(ERROR_KEY, ResourceUtil.getString("collaboration.takeBackErr."+wfCheckMap.get("state")+".msg"));
        		}
        	}
        	
        	if(canDeal){
        		canDeal = lockWorkFlow(summary, user, 13, messages);
        	}
        	
        	if(canDeal){
        		String message = collaborationApi.takeBack(affairId, isSaveOpinion);
        		messages.put("message", message);
        		if(Strings.isNotBlank(message)){
        		    // 统一错误方式
        		    messages.put(ERROR_KEY, message);
        		}
        	}
		} catch (BusinessException e) {
			LOGGER.error("取回失败！", e);
			messages.put(ERROR_KEY, e.getMessage());
		} finally{
			if(isLock){
				colLockManager.unlock(affairId);
			}
            try {
                colManager.colDelLock(summary, affair);
            } catch (BusinessException e) {
                LOGGER.error("表单锁解锁失败", e);
            }
		}
		
		//结果封装
        if(messages.containsKey(ERROR_KEY)){
            messages.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            messages.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
		return ok(JSONUtil.toJSONString(messages));
	}
    
	
	/*
	 * 平台的ParamUtil.getLong方法有问题，这里自己实现
	 */
    private Long getLong(Map<String, Object> param, String key, Long defualt){
        Long ret = defualt;
        
        if(param != null && param.get(key) != null){
            Object o = param.get(key);
            ret = Long.parseLong(o.toString());
        }
        return ret;
    }
    /**
	 *  获取协同关联文档列表
	 * @param params Map<String, String> | 必填 | 其他参数
	 * <pre>
     *  pageNo     int | 非必填 | 分页信息，第几页(默认第一页)
     *  pageSize   int | 非必填 | 分页信息，每页多少条数据(默认20条)
     *  state           String | 必填      | 列表类型 [2(已发)/3(待办)/4(已办 )]
     *  startMemberName String | 非必填  | 发起人姓名(用于查询)
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 发起时间(用于查询)createDate=2016-10-01#2016-11-17
	 * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("colQuotes")
    public Response getColQuotes(Map<String,String> params){
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
		flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
		try {
			User currentUser = AppContext.getCurrentUser();
			params.put(ColQueryCondition.state.name(), ParamUtil.getString(params, "state","2"));
			params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
			flipInfo = collaborationApi.findColQuote(flipInfo, params);
		} catch (BusinessException e) {
			LOGGER.error("获取协同关联文档列表问题！", e);
		}
		return  ok(flipInfo);
    }
    /**
	 *  查询关联文档交接事项列表
	 * @param params Map<String, String> | 必填 | 其他参数
	 * <pre>
     *  pageNo     int | 非必填 | 分页信息，第几页(默认第一页)
     *  pageSize   int | 非必填 | 分页信息，每页多少条数据(默认20条)
     *  startMemberName String | 非必填  | 发起人姓名(用于查询)
     *  subject         String | 非必填  | 标题 (用于查询)
     *  createDate      String | 非必填  | 发起时间(用于查询)createDate=2016-10-01#2016-11-17
	 * </pre>
	 * @return FlipInfo
	 */
	@POST
	@Path("handoverQuotes")
    public Response getHandoverQuotes(Map<String,String> params){
		FlipInfo flipInfo = super.getFlipInfo();
		flipInfo.setPage(ParamUtil.getInt(params, "pageNo",1));
		flipInfo.setSize(ParamUtil.getInt(params, "pageSize",20));
		try {
			User currentUser = AppContext.getCurrentUser();
			flipInfo = collaborationApi.findHandoverQuote(flipInfo, params);
		} catch (BusinessException e) {
			LOGGER.error("获取协同关联文档列表问题！", e);
		}
		return  ok(flipInfo);
    }
	
	/**
	 *  {GET} 获取协同关联文档列表各总数
	 * @return  key (已发：listSent/待办：listPending/已办：listDone ) value:数目
	 */
	@GET
	@Path("colQuoteCounts")
	public Response getColQuoteCounts() {
		Map<String,String> params = new HashMap<String,String>();
		User currentUser = AppContext.getCurrentUser();
		params.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			map = collaborationApi.getColAffairsCount(params);
		} catch (BusinessException e) {
			LOGGER.error("获取协同关联文档列表各总数出错！", e);
		}
		return ok(map);
	}
    
    /**
     * 发起人附言设置
     * @param summary ColSummary | 必填 | 协同对象
     * @throws BusinessException
     */
    private List<ColSummaryCommentVO> summarySenderComments(Long summaryId, boolean isHistory) throws BusinessException{
        
        FlipInfo senderCommentfpi = commentManager.findCommentByType(ModuleType.collaboration, 
        		summaryId, Comment.CommentType.sender, null, false, isHistory);
        List<Comment> senderCommentList = senderCommentfpi.getData();
        return ColSummaryCommentVO.valueOf(senderCommentList);
    }
    
    /**
     * 协同详情页面，评论数量统计
     * @param summaryId Long | 必填 | 协同ID
     * @param cType String || 类型  参见常量 COMMENT_TYPE_LIKE 等
     * @param isHistory Boolean || 类型  是否是转储数据
     * 
     * @throws BusinessException
     */
    private int getCommentCount(Long summaryId, String type, boolean isHistory) throws BusinessException{
        
        FlipInfo finfo = new FlipInfo();
        
        int ret = 0;
        
        //点赞数量
        if(COMMENT_TYPE_LIKE.equals(type)){
            
            finfo = commentManager.findLikeComment(ModuleType.collaboration, 
                    summaryId, Comment.CommentType.comment, finfo, true, isHistory);
            ret = finfo.getTotal();
        
        }else if (COMMENT_TYPE_AGREE.equals(type)) {
            
            //同意数量
            finfo = commentManager.findCommentByAttitude(ModuleType.collaboration, 
                    summaryId, Comment.CommentType.comment, finfo, CommentExtAtt1Enum.agree.i18nLabel(), true, isHistory);
            ret = finfo.getTotal();
            
        }else if (COMMENT_TYPE_DISAGREE.equals(type)) {
            
            finfo = commentManager.findCommentByAttitude(ModuleType.collaboration, 
                    summaryId, Comment.CommentType.comment, finfo, CommentExtAtt1Enum.disagree.i18nLabel(), true, isHistory);
            ret = finfo.getTotal();
            
        } else if(COMMENT_TYPE_ALL.equals(type)) {
            finfo = commentManager.findCommentByType(ModuleType.collaboration, 
                    summaryId, Comment.CommentType.comment, finfo, true, isHistory);
            ret = finfo.getTotal();
        }
        
        
        return ret;
    }
    
    /**
     * 节点权限，控制页面显示等设置， 页面参数，权限等相关配置
     * @param pageConfig Map<String, Object> || 其他参数
     * @param openFrom String || 打开来源
     * @param affair CtpAffair || 事项信息
     * @param summary ColSummary || 协同信息
     * @param boolean isTemplete || 是否系统模板
     * @param user 用户
     * @throws BusinessException
     */
    private void affairNodeProperty(Map<String, Object> pageConfig,
            String openFrom, CtpAffair affair, ColSummary summary, User user, boolean isTemplete) throws BusinessException{
        
    	//设置是否可以进行回复
        boolean canReply = (ColOpenFrom.stepBackRecord.name().equals(openFrom) 
        								||ColOpenFrom.repealRecord.name().equals(openFrom)
        								||ColOpenFrom.glwd.name().equals(openFrom) 
        								|| ColOpenFrom.listPending.name().equals(openFrom) 
        								|| ColOpenFrom.listDone.name().equals(openFrom) 
        								|| ColOpenFrom.listSent.name().equals(openFrom)
        								|| (ColOpenFrom.listWaitSend.name().equals(openFrom) && Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState())));

        
        pageConfig.put("isTemplete", isTemplete);
        
        boolean isFinish = new Integer(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())
        		|| new Integer(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState());
        if(isFinish){
        	String anyReply = systemConfig.get("anyReply_enable");
        	if(Strings.isNotBlank(anyReply) && "disable".equals(anyReply)){
        		canReply= false;
        	}
        }
        pageConfig.put("canReply", canReply);
        //是否可以添加附言
        pageConfig.put("canAddSenderComment", affair.getSenderId().equals(user.getId()) && canReply);
        
        //处理状态
        boolean canDeal = false;
        if(ColOpenFrom.listPending.name().equals(openFrom) && affair.getState() == StateEnum.col_pending.getKey()
                && !Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState())){
            canDeal = true;
        }
        pageConfig.put("canDeal", canDeal); 
        
        boolean canHasten = !isFinish
                && ((ColOpenFrom.listSent.name().equals(openFrom) && user.getId().equals(summary.getStartMemberId()))
                        || (ColOpenFrom.listDone.name().equals(openFrom) && superviseManager.isSupervisor(user.getId(), summary.getId())));
        pageConfig.put("canHasten", canHasten);
        
        
        
        //节点权限
        Permission permission = colManager.getPermisson(affair, summary);
        
        //用于判断当前节点权限是否存在
        if(permission != null && canDeal) { 
            if (!permission.getName().equals(affair.getNodePolicy())) {
                pageConfig.put("noFindPermission", true);
                pageConfig.put("newPermission", permission.getName());
            }
        }
        
        pageConfig.put("permissionName", permission.getLabel());
        
        List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
        List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
        List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);
        
        //超级节点只能有提交和回退
        int superNodestatus= 0;
        boolean isSuperNode = AffairUtil.isSuperNode(affair);
        if(isSuperNode){
            
            if(affair.getState().intValue() == StateEnum.col_pending.getKey()){
                superNodestatus = wapi.getSuperNodeStatus(summary.getProcessId(), String.valueOf(affair.getActivityId()));
            }
        }
        if(superNodestatus != 0){
            
            String submit = "ContinueSubmit";
            String zcdb = "Comment";
            // String stepBack = "Return";
            // 7.1 升级后，  回退为 -1
            String stepBack = "-1";
            String opinion = "Opinion";
            String commonPhrase = "CommonPhrase";
            
            List<String> newBasicActionList = new ArrayList<String>(1);
            if(superNodestatus== 2 || superNodestatus ==1){
                newBasicActionList.add(submit);
            }/*else if(superNodestatus== 1){
                newBasicActionList.add(submit);
            }*/
          /*  if(basicActionList.contains(opinion)){
                newBasicActionList.add(opinion);
            }
            if(basicActionList.contains(commonPhrase)){
                newBasicActionList.add(commonPhrase);
            }*/
            basicActionList = newBasicActionList;
            
         /*   if(commonActionList.contains(stepBack) 
                    || advanceActionList.contains(stepBack)){*/
                commonActionList = new ArrayList<String>(1);
                basicActionList.add(stepBack);
            /*}else {
                commonActionList.clear();
            }*/
            advanceActionList.clear();
            
            // 超级节点状态
            pageConfig.put("superNodestatus", superNodestatus);
            pageConfig.put("isSuperNode", true);
        }
        
        //根据条件移除转发操作
        List<String> hasForWardList = null;
        String forwardAction = "Forward";
        if(commonActionList.contains(forwardAction)){
            hasForWardList = commonActionList;
        }else if(advanceActionList.contains(forwardAction)){
            hasForWardList = advanceActionList;
        }
        
        if(hasForWardList != null){
            
            //如果是待发、已发列表则判断新建权限能否转发
            if(!summary.getCanForward()){
                hasForWardList.remove(forwardAction);
            }else if(affair.getState() == StateEnum.col_waitSend.getKey() 
                    || affair.getState() == StateEnum.col_sent.getKey()) {
                Permission permission1 = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), "newCol", user.getLoginAccount());
                NodePolicyVO nodePolicy = new NodePolicyVO(permission1);
                if (!nodePolicy.isForward()) {
                    hasForWardList.remove(forwardAction);
                }
            } 
        }
        
        //不同意操设置
        CustomAction customAction = permission.getNodePolicy().getCustomAction();
        if (customAction != null) {
        	pageConfig.put("customAction", customAction);
        }
        
        //已发或待发列表
        String track = "Track";
        if(!basicActionList.contains(track) 
                && (affair.getState() == StateEnum.col_sent.getKey() || affair.getState() == StateEnum.col_waitSend.getKey())){
            basicActionList.add(track);
        }
       
        //VJOIN外部人员不能使用常用语
        if (!user.isV5Member()) { 
            basicActionList.remove("CommonPhrase"); 
        }
        
        //处理普通操作和高级操作(详情页面只能有转发\收藏\电话会议)
        List<String> returnCommonAction = new ArrayList<String>(1);
        List<String> returnAdvanceAction = new ArrayList<String>(1);
    	for (String commonAction : commonActionList) {
    		//移动端不能有新建计划
    		if (Strings.isNotBlank(commonAction) && !commonAction.contains("newplan|common|PortletCategory")) {
    			if (!"Forward".equals(commonAction)) {
    				returnAdvanceAction.add(commonAction);
    			} else {
    				returnCommonAction.add("Forward");
    			}
    		}
    	}
    	if (advanceActionList.contains("Forward")) {
    		returnCommonAction.add("Forward");
    		advanceActionList.remove("Forward");
    	}
    	
    	List<String> invalidPortals = new ArrayList<String>();
    	//应用磁贴参数
        String portalValue = permission.getNodePolicy().getPortalValue();
        if (Strings.isNotBlank(portalValue) && portalApi != null) {
        	Map<String, String> portalUrlMap = new HashMap<String, String>();
        	List<ImagePortletLayout> portalList = portalApi.getImagePorletsByIds(Arrays.asList(portalValue.split(",")));
        	for (ImagePortletLayout portal : portalList) {
        		if (Strings.isNotBlank(portal.getId())) {
        			if (Strings.isNotBlank(portal.getMobileUrl())) {
        				portalUrlMap.put(portal.getId(), portal.getMobileUrl());
        			} else {
        				invalidPortals.add(portal.getId());
					}
        		}
        	}
        	pageConfig.put("portalUrlMap", portalUrlMap);
        }
        
    	for (String advanceAction : advanceActionList) {
    		//移动端不能有新建计划/公文模板
    		if (Strings.isNotBlank(advanceAction) && !invalidPortals.contains(advanceAction)) {
    			returnAdvanceAction.add(advanceAction);
    		}
    	}
    	if (Strings.isNotBlank(portalValue)) {
    		for(Iterator<String> it = basicActionList.iterator();it.hasNext();){
    			String basicAction = it.next();
    			//移动端不能有新建计划/公文模板
    			if (Strings.isNotBlank(basicAction) && invalidPortals.contains(basicAction)) {
    				it.remove();
    			}
    		}
    	}
    	
    	
    	colManager.checkCanAction(affair, summary, basicActionList, returnCommonAction, returnAdvanceAction, isTemplete,permission.getFlowPermId());
    	
        pageConfig.put("nodeActions", basicActionList);//基本操作和常用操作集合
        pageConfig.put("commonActions", returnCommonAction);//高级操作
        pageConfig.put("advanceActions", returnAdvanceAction);//高级操作
        
        Map<String,Object> layoutMap = permissionLayoutManager.getLayOutDealObject(permission.getFlowPermId(),basicActionList,returnCommonAction,returnAdvanceAction,0, false,1);
        pageConfig.put("advancedButs", layoutMap.get("advancedButs"));
        pageConfig.put("basicButs", layoutMap.get("basicButs"));
        
        NodePolicy nodePolicy = permission.getNodePolicy();
        
        String defaultAttitude = "";
        String nodeDefaultAttitude = nodePolicy.getDefaultAttitude();
        //态度按钮显示设置
        if(superNodestatus != 0){
            //超级节点处理不要态度
            pageConfig.put("attitudeBtns", "");
        }else{
        	StringBuffer nodeattitude = new StringBuffer();
        	
        	String attitude = permission.getNodePolicy().getAttitude();
        	DetailAttitude detailAttitude = permission.getNodePolicy().getDatailAttitude();
        	
        	if (Strings.isNotBlank(attitude)) {
        		List<Map<String, String>> attitudeList = new ArrayList<Map<String, String>>();
        		String[] attitudeArr = attitude.split(",");
        		for (String att : attitudeArr) {
        			Map<String, String> attitudeMap = new HashMap<String,String>();
        			if (Strings.isNotBlank(nodeattitude.toString())) {
        				nodeattitude.append(",");
        			}
        			if ("haveRead".equals(att)) {
        				attitudeMap.put("value", detailAttitude.getHaveRead());
        				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getHaveRead()));
        				nodeattitude.append(detailAttitude.getHaveRead());
        			} else if ("agree".equals(att)) {
        				attitudeMap.put("value", detailAttitude.getAgree());
        				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getAgree()));
        				nodeattitude.append(detailAttitude.getAgree());
        			} else if ("disagree".equals(att)) {
        				attitudeMap.put("value", detailAttitude.getDisagree());
        				attitudeMap.put("showValue", ResourceUtil.getString(detailAttitude.getDisagree()));
        				nodeattitude.append(detailAttitude.getDisagree());
        			} 
        			attitudeMap.put("code", att);
        			
        			attitudeList.add(attitudeMap);
        			if (Strings.isNotBlank(nodeDefaultAttitude) && nodeDefaultAttitude.equals(att)) {
        				defaultAttitude = attitudeMap.get("value");
        			}
        		}
        		//可能存在态度显示为:同意\不同意.但是默认态度值为:已阅. 兼容处理一下
        		if (Strings.isBlank(defaultAttitude) && attitudeList.size() > 0) {
        			defaultAttitude = attitudeList.get(0).get("value");
        		}
        		pageConfig.put("attitudeBtns", attitudeList);
        	}
        }
        pageConfig.put("defaultAttitude", defaultAttitude);
        pageConfig.put("defaultAttitudeCode", nodePolicy.getDefaultAttitude());
        
        //指定回退提交模式
        Integer submitStyle = nodePolicy.getSubmitStyle();
        if (null != submitStyle) {
        	pageConfig.put("submitStyleCfg", submitStyle);
        } else {
        	pageConfig.put("submitStyleCfg", 2);
        }
        
        //意见必填设置
        //得到所有'意见不能为空'的节点权限名称 集合,用于协同待办列表，如果该Affair的节点权限意见不能为空，则不能直接删除和归档.
        boolean forceComment = !isSuperNode && isExpectValue(nodePolicy.getOpinionPolicy(), 1);
        boolean forceCommentWhenCancel = !isSuperNode && isExpectValue(nodePolicy.getCancelOpinionPolicy(), 1);
        boolean forceCommentWhenDisagree = !isSuperNode && isExpectValue(nodePolicy.getDisAgreeOpinionPolicy(), 1);
        pageConfig.put("forceComment", forceComment);
        pageConfig.put("forceCommentWhenCancel", forceCommentWhenCancel);
        pageConfig.put("forceCommentWhenDisagree", forceCommentWhenDisagree);
        
        //是否能收藏  和PC端逻辑一致
        boolean isContainOperation  = (Boolean)basicActionList.contains("Archive");
        //已发、待发事项都是受新建的归档限制
        if (StateEnum.col_sent.getKey() == affair.getState()
                ||StateEnum.col_waitSend.getKey() == affair.getState()) {
        	isContainOperation  = (Boolean)basicActionList.contains("Pigeonhole");
        }
        boolean isSenderOrCanArchive = Boolean.TRUE.equals(summary.getCanArchive()) ;
        boolean hasResourceCode = (AppContext.getCurrentUser().hasResourceCode("F04_docIndex")||AppContext.getCurrentUser().hasResourceCode("F04_myDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_accDocLibIndex")
        		||AppContext.getCurrentUser().hasResourceCode("F04_proDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_eDocLibIndex")||AppContext.getCurrentUser().hasResourceCode("F04_docLibsConfig"));
        hasResourceCode =  hasResourceCode && AppContext.hasPlugin("doc");
        String systemVer = AppContext.getSystemProperty("system.ProductId");
      //A6没有收藏功能。相对就没有权限
        boolean isA6 = false;
        if ("0".equals(systemVer) || "7".equals(systemVer)) {
            isA6 = true;
        }
        //否能收藏的权限无关
        String propertyFav = SystemProperties.getInstance().getProperty("doc.collectFlag");
        boolean propertyFavFlag = "true".equals(propertyFav);
        boolean canFavorite = isContainOperation && isSenderOrCanArchive && hasResourceCode && !isA6 && propertyFavFlag;
        if(!canFavorite){
        	LOGGER.info("協同ID：<"+summary.getId()+">,没有收藏权限,isContainOperation:"+isContainOperation+" isSenderOrCanArchive:"+isSenderOrCanArchive+" hasResourceCode："+hasResourceCode+",propertyFav:"+propertyFav+",!isA6:"+!isA6);
        }
        pageConfig.put("canFavorite", canFavorite);
    }

    //是否为预期的值
    private boolean isExpectValue(Integer i, Integer expect){
        boolean ret = false;
        if(i != null && i.equals(expect)){
            ret = true;
        }
        return ret;
    }
    
    /**
     *  设置意见草稿
     * @param draftCommentMap map 
     * @param affair
     * @param summary
     * @param canDeal
     * @throws BusinessException
     */
    private void summaryDraftComment(Map<String, Object> draftCommentMap, CtpAffair affair,
            ColSummary summary, boolean canDeal) throws BusinessException{
        
        if(canDeal){
            CtpCommentAll draftComment = commentManager.getDrfatComment(affair.getId());
            if(draftComment != null){
                
                draftCommentMap.put("hide", draftComment.isHidden());
                draftCommentMap.put("attitude", draftComment.getExtAtt1());
                draftCommentMap.put("attitudeCode", draftComment.getExtAtt4());
                draftCommentMap.put("likeSummary", Integer.valueOf(1).equals(draftComment.getPraiseToSummary()));
                draftCommentMap.put("attachments", draftComment.getRelateInfo());
                draftCommentMap.put("commentId", draftComment.getId());
                draftCommentMap.put("content", draftComment.getContent());
            }
        }
    }
    
    /**
     * 收藏
     * @param affairId | 必填 |  affairId事项Id
     * @param from | 非填 |  暂时没用到
     * @return 
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("favoriteAffair")
    public Response favoriteAffair(@QueryParam("affairId") Long affairId, @QueryParam("from") String from) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Map<String, Object> map = new HashMap<String, Object>();
        CtpAffair affair = affairManager.get(affairId);
        map.put("success", "true");
        if (affair != null) {
            Long summaryId = affair.getObjectId();
            ColSummary summary = colManager.getSummaryById(summaryId);
            Boolean hasAtt = ColUtil.isHasAttachments(summary);
            Integer favoriteType = 3;
            try {
                affair.setHasFavorite(Boolean.TRUE);
                affairManager.updateAffair(affair);
                List<String> nodePermissions = new ArrayList<String>();
                colManager.getPermisson(affair, summary, nodePermissions);
                if (nodePermissions.contains("Archive")) {
                    docApi.favorite(user.getId(),user.getLoginAccount(), affairId, favoriteType, ApplicationCategoryEnum.collaboration.key(), hasAtt);
                }
            } catch (BusinessException e) {
            	map.put("success","false");
            	map.put("message", e.getMessage());
            	LOGGER.error("", e);
            }
        }else{
        	map.put("success", "false");
        }
        return ok(map);
    }
    
    /**
     * 取消收藏
     * @param affairId | 必填 |  affairId事项Id
     * @param from | 非必填 |  暂时没用到
     * @return 
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("cancelFavoriteAffair")
    public Response cancelFavoriteAffair(@QueryParam("affairId") Long affairId, @QueryParam("from") String from) throws BusinessException {
    	Map<String, Object> ret = new HashMap<String, Object>();
    	CtpAffair affair = affairManager.get(affairId);
        if (affair != null) {
            affair.setHasFavorite(Boolean.FALSE);
            affairManager.updateAffair(affair);
        }
        if (affairId != null) {
            Boolean cancelFavorite = docApi.cancelFavorite(null, affairId);
            ret.put("success","true");
            ret.put("message",cancelFavorite);
        }else{
        	ret.put("success","false");
        }
        return ok(ret);
    }
    
    
    /**
     *  [POST]转发协同， 表单协同只能转发成原表单
     * @param params Map<String,String> | 必填 | 其他参数 
     * <pre> 
     *  params._json_params String |必填| 请求参数， 值为json转换的字符串
     *  
     *  e.g.
     *  
     *   该示例是json格式， 具体使用事，请把json转换成字符串赋值给params._json_params
     *  
     *      _json_params = {
     *          "MainData" : {
     *               "affairId" : "-2940733133576384592", 必传     //被转发协同事项ID
     *               "summaryId" : "6468673352357016448", 必传     //被转发协同ID
     *               "forwardOriginalNote" : "1",         必传     //转发原附言(String="1" or "0")
     *               "forwardOriginalopinion" : "1",      必传     // 转发原意见(String="1" or "0")
     *               "track" : "1",                       必传     //是否跟踪(String="1" 是 or "0" 否)
     *               "comment" : ""                       非必传 //发起附言
     *           },
     *           "workflow_definition" : {
     *               "process_desc_by" : "xml",             //工作流数据存储类型， 目前传固定值 xml
     *               "process_xml" : "3878885470249831010", //工作流xml信息， 传后台缓存ID，或具体的xml信息
     *               "workflow_data_flag" : "WORKFLOW_SEEYON",//产生校验变量，传固定值 WORKFLOW_SEEYON
     *               "moduleType" : "1", //模块信息， 传固定值 1， 表示是协同
     *               
     *               //工作流数据， 由程序自动生成
     *               "workflow_node_condition_input" : "{\"condition\":[{\"nodeId\":\"-64653792702871975\",\"isDelete\":\"false\"},{\"nodeId\":\"-1048517176410356398\",\"isDelete\":\"false\"}],\"matchRequestToken\":\"H5--8492409909120645741-1500444289199\"}",
     *               "toReGo" : "false",//传固定值 false
     *               "caseId" : "-1",//传固定值 "-1"
     *               "subObjectId" : "-1",//传固定值 -1
     *               "currentNodeId" : "start"//传固定值 start
     *           },
     *           "attachmentInputs" : []//附件信息
     *      }
     *          
     * </pre>
     * @return String
     */
	@POST
    @Path("transColForward")
    public Response transColForward(Map<String,String> sParams) {
    	User user = AppContext.getCurrentUser();
    	
    	String jsonStr = sParams.get(JSON_PARAMS);
    	putThreadContext(jsonStr);
        
    	Map<String, String> params = ParamUtil.getJsonDomain("MainData");
    	String message = "true";
    	if(params.isEmpty()){
    		message = "false";
    	}
    	
    	String infoPathKey = "style";
    	//转发只转成原表单
        AppContext.putThreadContext(infoPathKey, "1");
        
    	try {
    		
    	    Map parseJSONString = (Map)JSONUtil.parseJSONString(jsonStr);
    		if(null != parseJSONString.get("forwardRightId")){
    			params.put("forwardRightId", parseJSONString.get("forwardRightId").toString());
    		}
    		
			collaborationApi.transDoForward(user, params);
		} catch (BusinessException e) {
			LOGGER.error("", e);
			message = "false";
		} finally {
		    removeThreadContext();
		    AppContext.removeThreadContext(infoPathKey);
		}
    	
    	return ok(message);
    }
    
    /**
     * 转发新建流程需要的参数
     * @param summaryId String |必填| 协同ID
     * @return Map<String,Object>
     */
    @GET
    @Path("forwardParams")
    public Response forwardParams(@QueryParam("String") String summaryId){
    	User user = AppContext.getCurrentUser();
    	Map<String,Object> params = new HashMap<String,Object>();
    	try {
			fixUser(user);
			//默认节点权限
	        PermissionVO permission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(),user.getLoginAccount());
	        String defaultNodeName = "";
	        String defaultNodeLable = "";
	        if (permission != null) {
	        	defaultNodeName = permission.getName();
	        	defaultNodeLable = permission.getLabel();
	        }
	        params.put("defaultNodeName", defaultNodeName);
	        params.put("defaultNodeLable", defaultNodeLable);
			//当前用户信息
			params.put("currentUser", CurrentUserInfoVO.valueOf(user));
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
    	return ok(params);
    }
    
    /**
     * 查看节点所有人员
     * @param nodeId String |必填| 节点ID
     * @param summaryId String |必填| 协同ID
     * </pre>
     * @return FlipInfo
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("node/members")
    public Response showNodeMembers(@QueryParam("nodeId") String nodeId, @QueryParam("summaryId") String summaryId){
    	List<CtpAffair> affairs;
        FlipInfo flipInfo = getFlipInfo();
        
		try {
			affairs = affairManager.getAffairsByObjectIdAndNodeId(Long.valueOf(summaryId), Long.valueOf(nodeId));
			List<Map<String,Object>> node2Affairs = new ArrayList<Map<String,Object>>();
	        if(Strings.isNotEmpty(affairs)){
	        	for(Iterator<CtpAffair> it = affairs.iterator();it.hasNext();){
	        		CtpAffair affair = it.next();
	        		if(!affairManager.isAffairValid(affair, true)){
	        			it.remove();
	        		}
	        	}
	        	
	        	for(CtpAffair a : affairs){
	                if(a.getActivityId() == null ||
	                        (a.getState() != StateEnum.col_done.getKey()
	                            && a.getState() != StateEnum.col_pending.getKey())){
	                    continue;
	                }
	                Map<String,Object> map = new HashMap<String,Object>();
	                V3xOrgMember member = orgManager.getMemberById(a.getMemberId());
	                if(member != null){
	                    V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
	                    // 岗位名称
	                    map.put("gwName",post==null?"":post.getName());
	                    // 头像
	                    String iconPath = Functions.getAvatarImageUrl(a.getMemberId());
	                    map.put("iconPath",iconPath);
	                    
	                    map.put("memberId",a.getMemberId());
	                    map.put("memberName",Functions.showMemberName(a.getMemberId()));
	                    map.put("state",a.getState());
	                    map.put("subState",a.getSubState());
	                    map.put("backFromId",a.getBackFromId());
	                    map.put("readSwitch", systemConfig.get(IConfigPublicKey.READ_STATE_ENABLE));
	                    node2Affairs.add(map);
	                }
	            }
	        }
	        
	        DBAgent.memoryPaging(node2Affairs, flipInfo);
		} catch (NumberFormatException e) {
			LOGGER.error("", e);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
    	return ok(flipInfo);
    }
    
    /**
     * 表单锁 ,表单协同处理界面加锁 
     * @param sParams Map<String,String> | 必填  | 其他参数
     * <pre>
     * formAppId       String | 非必填  |  表单ID
     * formRecordId    String | |  表单数据ID
     *    <pre>
     *    formAppId不为空时，此字段不为空；为空时，此字段为空
     *    </pre>
     * rightId         String | 必填  |  新建内容权限ID，默认值-1，例如表单模板权限ID
     * affairId        String | 必填  |  事项ID
     * affairState     String | 必填  |  affair状态
     *    <pre>
     *    com.seeyon.ctp.common.content.affair.constants.StateEnum
     *    	col_waitSend(1), // 协同-待发
     *    	col_sent(2), // 协同-已发
     *    	col_pending(3), // 协同-待办   - 只有为3的时候，才会加锁
     *    	col_pending_repeat_auto_deal(30),  //待办-重复处理自动跳过
     *    	col_done(4), // 协同-已办
     *
     *    	col_cancel(5), // 协同-取消
     *    	col_stepBack(6), // 协同-回退
     *    	col_takeBack(7), // 协同-取回
     *    	col_competeOver(8), // 协同-竞争执行结束
     *    	col_stepStop(15), //协同-终止
     *    </pre>
     * nodePolicy      String | 必填  |  节点权限
     * affairReadOnly  String | 必填  |  只读
     * </pre>
     * @return	FormLockParam
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("lockCollForm")
    public Response lockCollForm(Map<String,String> sParams) throws BusinessException {
    	
    	return ok(lockForm(sParams));
    }
    
    /**
     * 表单加锁
     * 
     * @param sParams {@link #lockCollForm(Map)}
     * @return
     * @throws BusinessException 
     *
     */
    private LockObject lockForm(Map<String,String> sParams) throws BusinessException{
        FormLockParam lockParam = new FormLockParam();
        lockParam.setFormAppId(Strings.isBlank(sParams.get("formAppId")) ? -1L : Long.parseLong(sParams.get("formAppId")));
        lockParam.setFormRecordId(Strings.isBlank(sParams.get("formRecordId")) ? -1L : Long.parseLong(sParams.get("formRecordId")));
        lockParam.setRightId(sParams.get("rightId"));
        lockParam.setAffairId(Strings.isBlank(sParams.get("affairId")) ? -1L : Long.parseLong(sParams.get("affairId")));
        lockParam.setAffairState(Strings.isBlank(sParams.get("affairState")) ? -1 : Integer.parseInt(sParams.get("affairState")));
        lockParam.setNodePolicy(sParams.get("nodePolicy"));
        lockParam.setAffairReadOnly(Strings.isBlank(sParams.get("affairReadOnly")) ? Boolean.FALSE : Boolean.parseBoolean(sParams.get("affairReadOnly")));
        LockObject lockObject = colManager.formAddLock(lockParam);
        return lockObject;
    }
    
    /**
     * 解除协同有关的所有锁
     * @param affairId Long |必填|  事项ID
     * @return String
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("unlockCollAll")
    public Response unlockCollAll(@QueryParam("affairId") Long affairId) throws BusinessException {
    	colManager.unlockCollAll(affairId, null, null);
    	return ok("true");
    }
    /**
     * 模板校验
     * @param templateId String |必填|  模板ID
     * @return Map<String, String> 返回模板是否能正常使用 不能正常使用返回cannot 能正常使用还会返回“isTextTemplate” 格式模板
     * 							 uuid 则是 流程模板的流程ID  
     * @throws BusinessException
     */
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkCollTemplate")
    public Response checkCollTemplate(@QueryParam("templateId") String templateId) throws BusinessException {
    	String result = colManager.checkCollTemplate(templateId);
    	Map<String, String> ret = new HashMap<String, String>();
    	ret.put("result", result);
    	
    	//结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
    	
    	return ok(ret);
    }
    
	/**
     * 模板校验
     * @param params  模板数据
     * <pre>
 *        类型    名称             必填     备注
 *        Map<String,Object>   data     Y     多个 事项ID+"_"+模板ID 的组合 中间用逗号分开 例如   affair1_templateID1,affair2_templateID2
	 *  </pre>
     * @return Map<String, String> 返回模板是否能正常使用  
     * 						success为"true"  能正常使用
     * 	                    success为"false" 不能正常使用     
     * @throws BusinessException
     */
	@POST
    @Produces({ MediaType.APPLICATION_JSON  })
    @Path("checkTemplateCanUse")
    public Response checkTemplateCanUse(Map<String,Object> params) throws BusinessException {
		
		Map<String, String> ret = new HashMap<String, String>();
		String data = ParamUtil.getString(params, "data");
    	
    	 String[] ds = data.split("[,]");
         for (String d1 : ds) {
             if (Strings.isBlank(d1)) {
                 continue;
             }

             String[] d1s = d1.split("[_]");

             long affairId = Long.parseLong(d1s[0]);
             String templateId = d1s[1];
             ret = colManager.checkTemplateCanUse(templateId);
             if("can".equals((String)ret.get("flag"))){
                 ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
             }else {
                 ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
                 ret.put("code",d1);
                 return ok(ret);
             }
        }
         return ok(ret);
    	
    }
	
	/**
     * 指定回退
     * @param params Map<String,Object> |必填| 其它参数
     * <pre>
     * affairId              Long |必填|            事项ID
	 * activityId            String |必填|          节点ID
	 * submitStyle           String |必填|          处理提交方式
	 *    <pre>
	 *    1(直接提交给我)、0(流程重走)
	 *    <pre>
	 * isWfTrace             String |必填|          是否追溯流程
	 *    <pre>
	 *    1(是)、2(否)
	 *    <pre>
	 * isCircleBack          String |非必填|          是否环形回退，用于流程追述的数据类型产生，默认指定回退
	 *    <pre>
	 *    0(否)、1(是)
	 *    <pre>
	 * theStepBackNodeId     String |必填|          被回退节点ID
	 * processId             String |必填|          流程ID
	 * caseId                String |必填|          流程实例ID（一个流程  对应一个 流程实例ID）
	 * workitemId            String |必填|          当前记录对应的ID（一个流程有多个节点 每个节点有一个或多个人员  每个人员会对应一条数据  这条数据的ID 就是 当前记录对应的ID）
	 * summaryId             String |必填|          协同ID
	 * commentId             Long   |非必填|         意见对象的ID  如果有会拿他做意见对象的主键 没有的话 会自动生成一个UUID到意见对象。是存为草稿的时候的意见对应的id 
	 * content               String |非必填|          意见
	 * hide                  String |非必填|          意见隐藏 true-隐藏意见，false-不隐藏
	 * attitude              String |非必填|          态度 {@link com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum}
     *    <pre>
     *        collaboration.dealAttitude.haveRead,已阅
     *        collaboration.dealAttitude.agree,同意
     *        collaboration.dealAttitude.disagree，不同意
     *    </pre>
	 * likeSummary           String |非必填|          点赞， true-点赞了， false-没有
	 * fileJson              String |非必填|          附件信息
	 * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("updateAppointStepBack")
    public Response updateAppointStepBack(Map<String,Object> params) throws BusinessException {
    	
    	Long affairId = getLong(params, "affairId", null);
    	
    	
    	 Map<String, String> ret = new HashMap<String, String>();
         
         User user = AppContext.getCurrentUser();
         boolean canDeal = false;
         CtpAffair affair = null;
         ColSummary summary = null;
         boolean isLock = false;
         try {
             
             //获取对象
             affair = affairManager.get(affairId);
             
             summary = collaborationApi.getColSummary(affair.getObjectId());
             
             //检验锁和状态是否正常
             canDeal = checkCanDeal(summary, affair, ret);
             if(canDeal){

                 isLock = colLockManager.canGetLock(affairId);
                 if(!isLock) {
                     canDeal = false;
                     ret.put(ERROR_KEY, ResourceUtil.getString("coll.summary.validate.lable33"));
                     LOGGER.error( AppContext.currentUserLoginName()+"同时项两个人正在同时处理。affairId"+affairId);
                 } 
             }
             
             
             if (canDeal) {

                 // 意见
                 Comment comment = formComment(summary, affair, user, ColHandleType.specialback, params);
                 //是否修改正文
                 String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
                 String jsonComment = transComment2Str(comment,modifyFlag);
                 putThreadContext(jsonComment);
                 if(params!=null){
                	 params.put("affair", affair);
                     params.put("summary", summary);
                     params.put("comment", comment);
                     params.put("user", user);
                     if(summary.getTempleteId() != null){
                     	CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                         //取出模板信息
                     	params.put("templateColSubject", template.getColSubject());
                     	params.put("templateWorkflowId", template.getWorkflowId());
                     }
                 	 colManager.updateAppointStepBack(params);
                 }
             }
         } catch (Exception e) {
             ret.put(ERROR_KEY, e.getMessage());
             LOGGER.error("处理系统异常", e);
         } finally {
             if(isLock){
     			colLockManager.unlock(affairId);
     		 }
             removeThreadContext();
             if(canDeal){
            	 colManager.unlockCollAll(affairId,affair,summary);
             }
         }
         
       //结果封装
         if(ret.containsKey(ERROR_KEY)){
             ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
         }else {
             ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
         }

         return ok(ret);
    }
    
    /**
     * 当前人员的菜单权限（协同）
     * @return Map<String,Object>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("collaboration/user/privMenu")
    public Response collaborationUserPeivMenu() throws BusinessException {
    	
    	Map<String,Object> params = new HashMap<String,Object>();
    	User user = AppContext.getCurrentUser();
		
		params.put("canArchive", canArchive(user));
		params.put("isHaveNewColl", menuPurviewUtil.isHaveNewColl(user));
		//是否显示列表判断
		params.put("isHaveColPending", menuPurviewUtil.isHaveColPending(user));
		params.put("isHaveColDone", menuPurviewUtil.isHaveColDone(user));
		params.put("isHaveColSent", menuPurviewUtil.isHaveColSent(user));
		params.put("isHaveWaitSend", user.hasResourceCode("F01_listWaitSend"));
		params.put("isHaveHandover", handoverManager.hasHandoverConfig(AppContext.currentUserId()));
        params.put("hasPluginFK", AppContext.hasPlugin("fk")); // 判断是否有转库服务
		//当需要展示的列表数量等于或超过5个的时候，需要使用横向滑动组件
		int tabCount = 0;
		if(Boolean.valueOf(params.get("isHaveColPending").toString())){
			tabCount +=1;
		}
		if(Boolean.valueOf(params.get("isHaveColDone").toString())){
			tabCount +=1;
		}
		if(Boolean.valueOf(params.get("isHaveColSent").toString())){
			tabCount +=1;
		}
		if(Boolean.valueOf(params.get("isHaveWaitSend").toString())){
			tabCount +=1;
		}
		if(Boolean.valueOf(params.get("isHaveHandover").toString())){
			tabCount +=1;
		}
		if(tabCount >= 5){
			params.put("useIscroll", true);
		}
		
		//结果封装
        if(params.containsKey(ERROR_KEY)){
            params.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            params.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
		
    	return ok(params);
    }
    
    /**
     * 当前人员的菜单权限（协同）和 协同的待办，已办，已发数量
     * @return Map<String,Object>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("collaboration/user/privMenuAndQuoteCounts")
    public Response collUserPrivMenuAndQuoteCounts() throws BusinessException {
    	
    	Map<String,Object> params = new HashMap<String,Object>();
    	User user = AppContext.getCurrentUser();
    	
    	Object[] obj = new Object[5];
    	
    	obj[0] = menuPurviewUtil.isHaveColPending(user);
		obj[1] = menuPurviewUtil.isHaveColDone(user);
		obj[2] = menuPurviewUtil.isHaveColSent(user);
		obj[3] = user.hasResourceCode("F01_listWaitSend");
		obj[4] = menuPurviewUtil.isHaveNewColl(user);
		
		Map<String, Object> haveTab = new HashMap<String, Object>();
		haveTab.put("isHaveColPending", menuPurviewUtil.isHaveColPending(user));
		haveTab.put("isHaveColDone", menuPurviewUtil.isHaveColDone(user));
		haveTab.put("isHaveColSent", menuPurviewUtil.isHaveColSent(user));
		haveTab.put("isHaveWaitSend", user.hasResourceCode("F01_listWaitSend"));
		haveTab.put("isHaveNewColl", menuPurviewUtil.isHaveNewColl(user));
		haveTab.put("isHaveHandover", handoverManager.hasHandoverConfig(AppContext.currentUserId()));
		
		params.put("haveTab", haveTab);
		params.put("canArchive", canArchive(user));
		
		//列表数量
		Map<String,String> paramList = new HashMap<String,String>();
		paramList.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		try {
			map = collaborationApi.getColAffairsCount(paramList);
		} catch (BusinessException e) {
			LOGGER.error("获取协同关联文档列表各总数出错！", e);
		}
		params.put("countMap", map);
		
		//结果封装
        if(params.containsKey(ERROR_KEY)){
            params.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            params.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
		
    	return ok(params);
    }
    
    
    /**
     * 撤销时校验
     * @param params Map<String,Object> | 必填 | 其它参数
     * <pre>
     *    summaryId String |必填| 协同ID
     * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transRepalValid")
    public Response transRepalValid(Map<String,Object> params) throws BusinessException  {
    	
    	Map<String, String> checkMap = new HashMap<String, String>();
    	String summaryId = ParamUtil.getString(params, "summaryId");
    	
        checkMap.put("summaryId", summaryId);
        Map<String, String> checkRetMap = colManager.checkIsCanRepeal(checkMap);
        if(checkRetMap.isEmpty()){
            
        	checkRetMap.put("succ_msg", ResourceUtil.getString("coll.summary.validate.lable39"));
        	
        	String affairId = ParamUtil.getString(params, "affairId");
            
            if(Strings.isNotBlank(summaryId) && Strings.isNotBlank(affairId)){
                ColSummary summary = colManager.getSummaryById(Long.parseLong(summaryId));
                CtpAffair affair = affairManager.get(Long.parseLong(affairId));
                if(null != summary && null != affair){
                    String[] wfCheck = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), 
                            toString(summary.getProcessId()), toString(affair.getActivityId()));
                    if("false".equals(wfCheck[0])){
                        
                        checkRetMap.put(ERROR_KEY, wfCheck[1]);
                    }
                }
            }
            
        }else{
            checkRetMap.put(ERROR_KEY, checkRetMap.get("msg"));
            checkRetMap.remove("msg");
        }
    	
    	//结果封装
        if(checkRetMap.containsKey(ERROR_KEY)){
            checkRetMap.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            checkRetMap.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(checkRetMap);
    }
    
    /**
     * 回退时校验
     * @param params Map<String,Object> | 必填 | 其它参数
     * <pre>
     *    affairId String |必填| 协同ID
     * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transStepBackValid")
    public Response transStepBackValid(Map<String,Object> params) throws BusinessException  {
    	
    	Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        CtpAffair affair = null;
        ColSummary summary = null;
        
        String affairId = ParamUtil.getString(params, "affairId");
        
        try {
          //获取对象
            affair = affairManager.get(Long.valueOf(affairId));
            summary = collaborationApi.getColSummary(affair.getObjectId());
            
            String[] wfCheck = wapi.canStepBack(toString(affair.getSubObjectId()), 
                        toString(summary.getCaseId()), toString(summary.getProcessId()), 
                                        toString(affair.getActivityId()), "", "");
            if("false".equals(wfCheck[0])){
                ret.put(ERROR_KEY, wfCheck[1]);
            }
        } catch (BPMException e1) {
            LOGGER.error(e1.getMessage(), e1);
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 移交功能
     * @param params Map<String,Object> | 必填 | 其它参数
     * <pre>
     *    affairId String |必填| 转办事项的人的 事项ID
     *    transferMemberId  String |必填| 转办对象ID
     * </pre>
     * @return Map<String, String>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transfer")
    public Response transfer(Map<String,Object> params) throws BusinessException  {
    	
    	Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();

        String affairId = ParamUtil.getString(params, "affairId");
        String transferMemberId = ParamUtil.getString(params, "transferMemberId");
        params.remove("affairId");
        params.remove("transferMemberId");
        
        //获取对象
        CtpAffair  affair = affairManager.get(Long.valueOf(affairId));
        ColSummary summary = collaborationApi.getColSummary(affair.getObjectId());
        Comment comment = formComment(summary, affair, user, ColHandleType.finish, params);
        AppContext.putRequestContext("m3Comment", comment);
        //ret.put("m3comment", comment);
    	ret.put("affairId", affairId);
    	ret.put("transferMemberId", transferMemberId);
    	//是否修改正文
        String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
    	ret.put("modifyFlag", modifyFlag);
    	boolean isLock = false;
    	try {
        	isLock = colLockManager.canGetLock(affair.getId());
        	if(!isLock) {
        		String msg =  ResourceUtil.getString("collaboration.summary.notDuplicateSub");
        		ret.put(ERROR_KEY, msg);
        		LOGGER.error( AppContext.currentUserLoginName()+msg+",affairId"+affairId);
        	}
        	else{
        		String message = this.colManager.transColTransfer(ret);
        		if(Strings.isNotBlank(message)){
        			ret.put(ERROR_KEY, message);
        		}
        	}
		} finally {
			if(isLock){
				colLockManager.unlock(Long.valueOf(affairId));
			}
		}
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 是否能批量删除协同
     * @param params  affairIds String | 非必填 |  列表多个删除用,隔开，没传就不进行校验
     * @param params  from      String  | 非必填 |  from列表来源：待发列表 待办列表 已发列表 已办列表
     * <pre>
     * from : listPending(待办列表)  | listDone (已办列表 ) | listSent ( 已发列表) |  listWaitSent (待发列表)
     * </pre>
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("checkCanDelete")
    public Response checkCanDelete(@QueryParam("affairIds") String affairIds,@QueryParam("from") String from) throws BusinessException {
        
        
        Map<String,String> ret = new HashMap<String,String>();
        
        DeleteAjaxTranObj obj = new DeleteAjaxTranObj();
        obj.setAffairIds(affairIds);
        obj.setPageType(from);
        String checkCanDelete = colManager.checkCanDelete(obj);
        if(!"success".equals(checkCanDelete)){
        	ret.put(ERROR_KEY,checkCanDelete);
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 删除多个协同
     * @param 类型     |  名称     |   必填      |   备注
     * @param String | from | Y |  列表来源  ：待发列表 待办列表 已发列表 已办列表
     * from:listPending - 待办列表    listDone - 已办列表 listSent - 已发列表  listWaitSent - 待发列表
     * @param String | affairId | Y | 数据affairId,多个数据删除时用‘,’隔开
     * @return  Map<String, String>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("deleteAffairs/{from}/{affairIds}")
    public Response deleteAffairs(@PathParam("from") String from,@PathParam("affairIds") String affairIds) throws BusinessException {
        
        Map<String,String> ret = new HashMap<String,String>();
        String[] affairs = affairIds.split("[,]");
        String userId = String.valueOf(AppContext.getCurrentUser().getId());
        if("listWaitSend".equals(from)) {
        	from = ColListType.draft.name();
        }
        for (String affairId : affairs) {
            Long _affairId = Long.valueOf(affairId);
            try{
                colManager.deleteAffair(from,_affairId);
            } catch (BusinessException e) {
                LOGGER.error(e.getMessage(),e);
                ret.put(ERROR_KEY, "fail");
            } finally {
                CtpAffair affair = affairManager.get(_affairId);
                if (affair != null) {
                	wapi.releaseWorkFlowProcessLock(affair.getProcessId(), userId);
                }
            }
            
        }
        
      //结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
        
        return ok(ret);
    }
    
    /**
     * 获取单个协同附件
     * @param 			类型               |  名称             |   必填                                                                    |   备注
     * @param params    LONG     |summaryId  | 必填	                  		|summaryId	
     * @param params    String   |attType   | 必填                                                                         | 类型   "0"代表附件,"2"代表关联文档， "0,2"代表附件和关联文档
     * @return  String
     * @throws BusinessException
     */
    @GET
    @RestInterfaceAnnotation
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("attachments/{summaryId}/{attType}")
    public Response attachments(@PathParam("summaryId") Long summaryId,@PathParam("attType") String attType) throws BusinessException {
    	List<Attachment> attList = null;
        if(null != summaryId){
        	attList = attachmentManager.getByReference(summaryId);
        }
        if(null != attList && Strings.isNotBlank(attType)){
			String[] split = attType.split(",");
			if(null != split && split.length != 2){
				String type = split[0];
				for(int a = attList.size()-1 ; a > -1; a--){
					Attachment attachment = attList.get(a);
					if(!attachment.getType().toString().equals(type)){
						attList.remove(a);
					}
				}
			}
        }
        
        if(null != attList) {
        	//附件按照时间的降序排序
            Collections.sort(attList,new Comparator<Attachment>(){  
                @Override  
                public int compare(Attachment a1, Attachment a2) {  
                	//附件列表需倒序
            		Date d1=a1.getCreatedate();
            		Date d2=a2.getCreatedate();
            		int res=0;
            		if(d1!=null&&d2!=null){
            			res=d1.compareTo(d2);
            		}else if(d1 == null&&d2!=null){
            			res=-1;
            		}else if(d1!=null&&d2 == null){
            			res=1;
            		}
            		return res==0?0:res>0?-1:1;  
                }  
                  
            });
        }
        return ok(attList);
    }
    
    /**
     * 每隔三分钟向服务器发送AJAX请求 更新锁的有效时长
     * @param formRecordId		Long | 非必填 | 表单masterID  (校验表单锁)
     * @param processId	Long | 非必填 | 流程ID   (校验流程锁)
     * @param loginPlatform	String | 非必填 | 登陆平台 登陆平台分为两种：1："pc" 为电脑 2："phone" 为手机
     * @return	Map<String, Object>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("updateLockTime")
    public Response updateLockTime(@QueryParam("formRecordId") Long formRecordId,@QueryParam("processId") Long processId,@QueryParam("loginPlatform") String loginPlatform) {
    	Map<String,String> ret = new HashMap<String,String>();
    	Map<String,String> lockParam = new HashMap<String,String>();
    	User currentUser = AppContext.getCurrentUser();
    	String from = Strings.isNotBlank(loginPlatform) ? loginPlatform: Constants.login_sign.stringValueOf(currentUser.getLoginSign());
    	lockParam.put("curUserId", currentUser.getId().toString());
    	if(null != formRecordId){
    		lockParam.put("formMasterId", formRecordId.toString());
    	}
    	if(null != processId){
    		lockParam.put("processId", processId.toString());
    	}
    	//if(Strings.isNotBlank(loginPlatform)){
    		lockParam.put("loginPlatform", from);
    	//}
    	try {
			colManager.activeLockTime(lockParam);
			
		} catch (BusinessException e) {
		    ret.put(ERROR_KEY, e.getLocalizedMessage());
		}
    	
    	//结果封装
        if(ret.containsKey(ERROR_KEY)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
    	
    	return ok(ret);
    }
    
    
    /**
     * 获取协同消息推送人员列表
     * @param summaryId Long | 必填 | 协同ID
     * @return  List<Map<String, Object>>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("pushMsgMembers")
    public Response pushMessageToMembersList(@QueryParam("summaryId") String summaryId, 
                                             @QueryParam("name") String name) throws BusinessException {
        
        FlipInfo fli = getFlipInfo();
        List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
        
        if(ColUtil.isLong(summaryId)){
            Map<String, String> params = new HashMap<String, String>();
            params.put("summaryId", summaryId);
            List<Map<String, Object>> members = colManager.pushMessageToMembersList(params, true);
            
            boolean blankName = Strings.isBlank(name);
            if(fli == null && blankName){
                ret = members;
            }else{
                int start = 0;
                int size = members.size();
                if(fli != null){
                    start = fli.getStartAt();
                    size = fli.getSize();
                }
                //内存分页
                int now = 0;
                for(Map<String, Object> m : members){
                    
                    if(blankName || ((String) m.get("name")).indexOf(name) != -1){
                        
                        now++;
                        if(now > start){
                            ret.add(m);
                            size--;
                        }
                        if(size == 0){
                            break;
                        }
                    }
                }
            }
        }
        
        return ok(ret);
    }
    
    /**
     * 校验同一个人是否将协同归档到同一个目录
     * url:coll/getIsSamePigeonhole
     * 
     * @param params 
     *  <pre>
     *    类型                   名称                                 必填                                备注
     *    String     affairId         Y              事项ID
     *    Long       destFolderId     Y              归档的目录ID 
     *  </pre>
     * @return  map  
     * 			 <pre>
     * 				存在同一目录: retMsg=在该文件夹下已存在<>的归档链接，确定继续归档么？
     * 			 </pre>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getIsSamePigeonhole")
    public Response getIsSamePigeonhole(Map<String,Object> params) throws BusinessException {
    	
    	String affairIds = ParamUtil.getString(params, "affairId");
    	Long destFolderId = getLong(params, "destFolderId", null);
    	
    	String[] _affairId = affairIds.split(","); 
    	List<String> collIds = new ArrayList<String>();
    	Map<String,String> map = new HashMap<String,String>();
    	for(String id:_affairId){
    		if(Strings.isNotBlank(id) && NumberUtils.isNumber(id)){
    			collIds.add(id);
    		}
    	}
    	
    	String retMsg = colManager.getIsSamePigeonhole(collIds, destFolderId,false);
    	map.put("retMsg", retMsg);
        return ok(map);
    }
    
    
    /**
     * 取一个协同已发事项的ID
     * url:coll/getSenderAffairId
     * 
     * @param params 
     *  <pre>
     *    类型                   名称                                 必填                                备注
     *    String     objectId         Y              协同运用的ID
     *  </pre>
     * @return  map  
     * 			 <pre>
     * 				已发事项的ID
     * 			 </pre>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getSenderAffairId/{objectId}")
    public Response getSenderAffairId(@PathParam("objectId") String objectId) throws BusinessException {
    	
    	Map<String,String> map = new HashMap<String,String>();
    	String affairId = dataRelationApi.getSenderAffairId(objectId);
    	if(Strings.isBlank(affairId)){
    		map.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
    	}else{
    		map.put("affairId", affairId);
    		map.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
    	}
    	
        return ok(map);
    }
    /**
     * 校验是否可以转发协同
     * URL:coll/checkForwardPermission
     * @param params
     * <pre>
     *    类型                   名称                                 必填                                备注
     *    String     data             Y              affairId组合成的字符串，格式：{affairId,affairId}
     *  </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("check/forward")
	public Response checkForwardPermission(Map<String, Object> params) throws BusinessException {
		String data = ParamUtil.getString(params, "data");
		List<String> noPermissionCol = colManager.checkForwardPermission(data);
		if (Strings.isNotEmpty(noPermissionCol)) {
			return fail(Strings.join(noPermissionCol, "<br>"));
		}
		else {
			return success("");
		}
	}
    
    /**
     * 归档条件判断
     * URL:coll/getPigeonholeRight
     * @param params
     * <pre>
     *    类型                   名称                                 必填                                备注
     *    String     affairIds       Y              事项ID串，每个ID直接用','分隔
     *  </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getPigeonholeRight")
    public Response getPigeonholeRight(Map<String,Object> params) throws BusinessException{
    	
    	String affairIds = ParamUtil.getString(params, "affairIds");
    	
    	String[] ids = affairIds.split(",");
    	List<String> collIds = new ArrayList<String>();
    	Map<String,String> map = new HashMap<String,String>();
    	if(ids.length>0){
    		for(String _id:ids){
    			collIds.add(_id);
    		}
    	}
    	
    	String result = colManager.getPigeonholeRightForM3(collIds,true);
    	map.put(SUCCESS_KEY, result);
    	return ok(map);
    }
    
    /**
     * 归档
     * URL:coll/transPigeonhole
     * @param params
     *  <pre>
     *    类型                   名称                                 必填                                备注
     *    String     affairIds       Y              事项ID串，每个ID直接用','分隔
     *    String     destFolderId    Y              目标文件夹ID
     *    String     listType        Y              归档操作出发源类型，包括待办pending，已办listDone，已发listSent
     *  </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transPigeonhole")
    public Response transPigeonhole(Map<String,Object> params) throws BusinessException{
    	
    	String affairIds = ParamUtil.getString(params, "affairIds");
    	String destFolderId = ParamUtil.getString(params, "destFolderId");
    	String listType = ParamUtil.getString(params, "listType");
    	String pageNodePolicy = ParamUtil.getString(params, "pageNodePolicy");
    	String[] ids = affairIds.split(",");
    	Map<String,String> map = new HashMap<String,String>();
    	String errorMsg = "";
    	boolean hasError = false;
    	boolean allError = true;
    	if(ids.length>0){
    		for(String _id:ids){
    			String checkAffairValid = colManager.checkAffairValid(_id,pageNodePolicy);
    	    	if(Strings.isNotBlank(checkAffairValid)){
    	    		errorMsg += checkAffairValid+"<br/>";
    	    		hasError = true;
    	    	}else{
    	    		allError = false;
    	    		colManager.transPigeonhole(Long.valueOf(_id),Long.valueOf(destFolderId),listType);
    	    	}
    		}
    	}
    	if(hasError){
    		map.put("success_msg", errorMsg);
    		map.put(SUCCESS_KEY, "false");
    	}else{
    		if(allError){
    			map.put(SUCCESS_KEY, "false");
    		}else{
    			map.put(SUCCESS_KEY,  ResourceUtil.getString("coll.summary.validate.lable40"));
    		}
    	}
    	return ok(map);
    }
    
    /**
     * 新建协同发送重复提交的校验，校验事项状态（参数isNeedCheckAffair控制）和 是否正在发送
     * @param summaryId String | 必填 | 协同ID
     * @param isNeedCheckAffair String | 非必填 | "true"/"false" 是否校验affair的state
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
 	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("check/send")
    public Response checkAffairAndLock4NewCol(@QueryParam("summaryId") String summaryId,@QueryParam("isNeedCheckAffair") String isNeedCheckAffair) throws  BusinessException{
    	   
        Map<String,String> ret = new HashMap<String,String>();
        
        String msg  = colManager.checkAffairAndLock4NewCol(summaryId, isNeedCheckAffair);
      //结果封装
        if(Strings.isBlank(msg)){
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }else {
            ret.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
            ret.put(ERROR_KEY, msg);
        }
    	
    	return ok(ret);
    }
    /**
     * 新建节点权限（协同）
     * @return Map<String,Object>
     * @throws BusinessException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("newCollaborationPolicy")
    public Response newCollaborationPolicy() throws BusinessException {
    	
    	Map<String,Object> params = new HashMap<String,Object>();
    	User user = AppContext.getCurrentUser();
    	
    	NodePolicyVO newPolicy = colManager.getNewColNodePolicy(user.getAccountId());
    	boolean docPolicy = AppContext.hasPlugin("doc");
		params.put("newPolicy", newPolicy);
		params.put("docPolicy", docPolicy);
		//结果封装
        if(params.containsKey(ERROR_KEY)){
            params.put(SUCCESS_KEY, SUCCESS_VALUE_FALSE);
        }else {
            params.put(SUCCESS_KEY, SUCCESS_VALUE_TRUE);
        }
		
    	return ok(params);
    }
    
    /**
     * 批处理预提交（协同）
     * URL:coll/batch/checkPreBatch
     * @param params
     *  <pre>
     *    类型                   名称                                 必填                                备注
     *    String     affairs       	 Y              事项ID串，每个ID之间用','分隔
     *    String     summarys        Y              协同ID串，每个ID之间用','分割
     *    String     categorys       Y              应用模块（目前只有协同模块在使用），根据实际情况对应前面的affairs和summarys
     *    											对应的位置是什么运用模块来确定  协同用1 公文用4 之间用','分割
     *    											
     *  </pre>
     * @return
     * @return Map<String,Object>
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/checkPreBatch")
    public Response checkPreBatch(@QueryParam("affairs") String affairs, @QueryParam("summarys") String summarys, @QueryParam("categorys") String categorys) throws BusinessException {
    	Map paramMap = new HashMap();
    	paramMap.put("affairs", Arrays.asList(affairs.split(",")));
    	paramMap.put("summarys", Arrays.asList(summarys.split(",")));
    	paramMap.put("categorys", Arrays.asList(categorys.split(",")));
    	
    	BatchResult[] result = batchManager.checkPreBatch(paramMap);
    	
    	Map params = new HashMap();
    	params.put("result", result);
    	params.put("currentUserId", AppContext.currentUserId());
    	
    	return ok(params);
    }
    
    
    /**
     * 模板是否被删除
     * URL:coll/forward/check/template
     * @param      templateIds  | String |  必填    | 模板ID,可以传递多个，以逗号分隔
     * @return     
     * <pre>
     *  成功：
     *      {code:0,data:{delTemplateIds:[删除的模板Id]}}
     *  失败：
     *      {code:1,message:提示语}
     * </pre>
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("forward/check/template")
    public Response isTemplateDeleted(@QueryParam("templateIds") String templateIds) throws BusinessException {
        
        if(Strings.isBlank(templateIds)){
            return fail(ResourceUtil.getString("coll.summary.validate.lable41"));
        }
        
        String[] ids = templateIds.split("[,]");
        String msg = "";
        List<Long> delIds = new ArrayList<Long>();
        for(String s : ids){
            if(!Strings.isDigits(s)){
                if(Strings.isNotEmpty(msg)){
                    msg+="\r\n";
                }
                msg += ResourceUtil.getString("coll.summary.validate.lable42",s);
                continue;
            }
            CtpTemplate template =templateManager.getCtpTemplate(Long.valueOf(s));
            
            if(template != null){
                Boolean isDelete = template.isDelete();
                if(isDelete == null){
                    isDelete = Boolean.FALSE;
                }
                if(isDelete){
                    delIds.add(template.getId());
                }
            }
        }
        
        if(Strings.isBlank(msg)){
            Map<String,Object> m = new HashMap<String,Object>();
            m.put("delTemplateIds", delIds);
            return success(m);
        }
        
        return fail(msg);
    }
    
    
    
    /**
     * 批处理提交
     * URL:coll/batch/doBatchColl
     * @param params
     *  <pre>
     *    类型                   名称                                 	必填                                备注
     *    String     attitude       	 Y              态度：批处理选择的态度key,比如:haveRead
     *    String     content        	 Y              意见内容
     *    String     summarys        	 Y              协同ID串，每个ID之间用','分割
     *    String     conditionsOfNodes   Y              节点条件 {"condition":[{"nodeId":"end","isDelete":"false"}]}
     *    String     affairs        	 Y              事项ID串，每个ID之间用','分隔
     *    String     categorys       	 Y              应用模块（目前只有协同模块在使用），之间用','分割
     *    String     attitudes       	 Y              后台节点权限设置:一个节点权限态度设置为数组格式(key固定,value为设置显示态度值),多个节点权限使用_分割.比如:
     *    												[{"haveRead":"已阅"},{"agree":"同意"},{"disagree":"不同意"}]_[{"haveRead":"已阅1"},{"agree":"同意1"},{"disagree":"不同意1"}]
     *    String     opinions       	 Y              意见节点权限(有意见节点权限0，无2)之间用','分割
     *    String     defaultAttitudes    Y              意见节点权限默认态度,多个节点权限之间用'_'分割.(默认态度只能有haveRead\agree\disagree).比如:haveRead_agree
     *  </pre>
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/doBatchColl")
    public Response doBatchColl(Map<String,Object> params) throws BusinessException {
    	Map paramMap = new HashMap();
    	
    	String attitude = (String)params.get("attitude");
    	String action = (String)params.get("action");
    	String content = (String)params.get("content");
		//List conditionsOfNodes =  (List)params.get("conditionsOfNodes");
    	String[] affairArr = ((String)params.get("affairs")).split(",");
    	String[] summaryArr = ((String)params.get("summarys")).split(",");
    	String[] categoryArr = ((String)params.get("categorys")).split(",");
    	String[] attitudeArr = ((String)params.get("attitudes")).split("_");
    	String[] defaultAttitudeArr = ((String)params.get("defaultAttitudes")).split("_");
    	String[] opinionArr = ((String)params.get("opinions")).split(",");

    	paramMap.put("attitude", attitude);
    	paramMap.put("content", content);
    	if(Strings.isNotBlank(action)){
    	    paramMap.put("action", action);
    	}
    	
    	
    	Map retMap = new HashMap();
    	List<Object> list = new ArrayList<Object>();
    	if(affairArr.length > 0) {
	    	for(int i=0; i<affairArr.length; i++) {
	    		paramMap.put("affairId", affairArr[i]);
	    		paramMap.put("summaryId", summaryArr[i]);
	    		paramMap.put("category", categoryArr[i]);
	    		//paramMap.put("conditionsOfNodes", conditionsOfNodes.get(i));
	    		
	    		//无意见框
	    		if("2".equals(opinionArr[i])) {
	    			paramMap.put("parameter", attitudeArr[i]+"_"+opinionArr[i]);	
	    		} else {
	    			paramMap.put("parameter", attitudeArr[i]+"_"+content);
	    		}
	    		
	    		// java.lang.ArrayIndexOutOfBoundsException: 0 容错处理
	    		String defaultAttitude = null;
	    		if(i < defaultAttitudeArr.length) {
	    		    defaultAttitude = defaultAttitudeArr[i];
	    		}else {
	    		    defaultAttitude = "";
	    		}
	    		paramMap.put("defaultAttitude", defaultAttitude);
	    		
	    		Object object = batchManager.transDoBatch(paramMap);
	    		list.add(object);
	    	}    	
    	}
    	
    	retMap.put("batchResult", list);
    	return ok(retMap);
    }
    
    
    
    /**
     * 快速提交
     * URL:coll/quick/finish
     * @param params
     *  <pre>
     *    类型                   名称                                   必填                                备注
     *    String                 attitude                                Y                              态度：已阅(1),同意(2)，不同意(3)
     *    String                 affairId                                Y                              事项ID
     *  </pre>
     * @throws BusinessException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("quick/finish")
    public Response quickfinishWorkItem(Map<String,Object> params) throws BusinessException {
        Map paramMap = new HashMap();
        
        String affairId = (String)params.get("affairId");
        String attitude = (String)params.get("attitude");
        String attitudeKey = (String)params.get("attitudeKey");
        
        
        if(Strings.isEmpty(affairId) || Strings.isEmpty(attitude)){
            return fail("affairId or attitude is null");
        }
        
        
        CtpAffair affair = affairManager.get(Long.valueOf(affairId));
        
        BackgroundDealParamBO dealBO = new BackgroundDealParamBO();
        dealBO.setAffair(affair);
        dealBO.setDealType(BackgroundDealType.QUICK);
        dealBO.setRetry(false);
        dealBO.getExtParam().put("attitude", attitude);
        dealBO.getExtParam().put("attitudeKey", attitudeKey);
        BackgroundDealResult dealResult  = collaborationApi.transfinishWorkItemInBackground(dealBO);
        
        if(dealResult.isCan()){
            return success("");
        }
        else{
            return fail(ResourceUtil.getString("collaboration.deal.cannotQuickDeal",dealResult.getMsg()));
        }
    }
    
    /**
     * 批处理回退
     * URL:coll/batch/doBatchStepbackColl
     * @param params
     *  <pre>
     *    类型                   名称                                 	必填                                备注
     *    String     attitude       	 Y              态度：不同意(disagree)
     *    String     content        	 Y              意见内容
     *    String     summarys        	 Y              协同ID串，每个ID之间用','分割
     *    String     affairs        	 Y              事项ID串，每个ID之间用','分隔
     *    String     categorys       	 Y              应用模块（目前只有协同模块在使用），之间用','分割,协同传值1
     *  </pre>
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/doBatchStepbackColl")
    public Response doBatchStepbackColl(Map<String,Object> params) throws Exception {
    	
    	Map paramMap = new HashMap();
    	
    	String attitude = (String)params.get("attitude");
    	String content = (String)params.get("content");
		
    	List affairs = Arrays.asList(((String) params.get("affairs")).split(","));  
    	List summarys = Arrays.asList(((String) params.get("summarys")).split(","));  
    	List categorys = Arrays.asList(((String) params.get("categorys")).split(",")); 
    	String trackWorkflowType = (String)params.get("trackWorkflowType");
    	String isM3 = (String)params.get("isM3");
    	paramMap.put("affairs", affairs);
    	paramMap.put("summarys", summarys);
    	paramMap.put("categorys", categorys);
    	paramMap.put("attitude", attitude);
    	paramMap.put("content", content);
    	paramMap.put("trackWorkflowType", trackWorkflowType);
    	paramMap.put("isM3","1");
    	Map retMap = new HashMap();
    	
    	List<Object> doStepStopBatch = batchManager.doStepBackBatch(paramMap);
    	
    	retMap.put("batchResult", doStepStopBatch);
    	return ok(retMap);
    }
    
    /**
     * 批处理回退
     * URL:coll/batch/checkOperation
     * @param params
     *  <pre>
     *    类型                   名称                                 	必填                                备注
     *    String     summarys        	 Y              协同ID串，每个ID之间用','分割
     *    String     affairs        	 Y              事项ID串，每个ID之间用','分隔
     *    String     categorys       	 Y              应用模块（目前只有协同模块在使用），之间用','分割
     *  </pre>
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/checkOperation")
    public Response checkOperation(Map<String,Object> params) throws Exception {
    	
    	Map paramMap = new HashMap();
    	List affairs = Arrays.asList(((String) params.get("affairs")).split(","));  
    	List summarys = Arrays.asList(((String) params.get("summarys")).split(","));  
    	List categorys = Arrays.asList(((String) params.get("categorys")).split(",")); 
    	String isM3 = (String)params.get("isM3");
    	paramMap.put("affairs", affairs);
    	paramMap.put("summarys", summarys);
    	paramMap.put("categorys", categorys);
    	
    	List<String> checkOperation = batchManager.checkOperation(paramMap);
    	
    	return ok(checkOperation);
    }
    
    /**
     * 批处理撤销
     * URL:coll/batch/doBatchRepealColl
     * @param params
     *  <pre>
     *    类型                   名称                                 	必填                                备注
     *    String     attitude       	 Y              态度：不同意(3)
     *    String     content        	 Y              意见内容
     *    String     summarys        	 Y              协同ID串，每个ID之间用','分割
     *    String     affairs        	 Y              事项ID串，每个ID之间用','分隔
     *    String     categorys       	 Y              应用模块（目前只有协同模块在使用），之间用','分割
     *  </pre>
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/doBatchRepealColl")
    public Response doBatchRepealColl(Map<String,Object> params) throws Exception {
    	Map paramMap = new HashMap();
    	
    	String attitude = (String)params.get("attitude");
    	String content = (String)params.get("content");
		
    	List affairs = Arrays.asList(((String) params.get("affairs")).split(","));  
    	List summarys = Arrays.asList(((String) params.get("summarys")).split(","));  
    	List categorys = Arrays.asList(((String) params.get("categorys")).split(","));  
    	String trackWorkflowType = (String)params.get("trackWorkflowType");
    	paramMap.put("affairs", affairs);
    	paramMap.put("summarys", summarys);
    	paramMap.put("categorys", categorys);
    	paramMap.put("attitude", attitude);
    	paramMap.put("content", content);
    	paramMap.put("trackWorkflowType", trackWorkflowType);
    	paramMap.put("isM3","1");
    	
    	Map retMap = new HashMap();
    	
    	List<Object> doStepStopBatch = batchManager.doRepealBatch(paramMap);
    	
    	retMap.put("batchResult", doStepStopBatch);
    	return ok(retMap);
    }
    
    /**
     * 批处理终止
     * URL:coll/batch/doBatchTerminateColl
     * @param params
     *  <pre>
     *    类型                   名称                                 	必填                                备注
     *    String     attitude       	 Y              态度：不同意(3)
     *    String     content        	 Y              意见内容
     *    String     summarys        	 Y              协同ID串，每个ID之间用','分割
     *    String     affairs        	 Y              事项ID串，每个ID之间用','分隔
     *    String     categorys       	 Y              应用模块（目前只有协同模块在使用），之间用','分割
     *  </pre>
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("batch/doBatchTerminateColl")
    public Response doBatchTerminateColl(Map<String,Object> params) throws Exception {
    	Map paramMap = new HashMap();
    	
    	String attitude = (String)params.get("attitude");
    	String content = (String)params.get("content");
		
    	List affairs = Arrays.asList(((String) params.get("affairs")).split(","));  
    	List summarys = Arrays.asList(((String) params.get("summarys")).split(","));  
    	List categorys = Arrays.asList(((String) params.get("categorys")).split(","));  
    	
    	paramMap.put("affairs", affairs);
    	paramMap.put("summarys", summarys);
    	paramMap.put("categorys", categorys);
    	paramMap.put("attitude", attitude);
    	paramMap.put("content", content);
    	paramMap.put("isM3","1");
    	
    	Map retMap = new HashMap();
    	
    	List<Object> doStepStopBatch = batchManager.doStepStopBatch(paramMap);
    	
    	retMap.put("batchResult", doStepStopBatch);
    	return ok(retMap);
    }
    
    /********************************** 语音机器人 start ******************************************/
    
    /**
     * 语音机器人-搜索
     * @param params	Map<String, Object> | 必填 | map类型参数
     * <pre>
     * 	  startMemberName 协同发起人  String
     *    startMemberId 协同发起人ID String Member|289327873612736 只能单人
     * 	  subject 协同标题 String
     *    status 协同状态 String | 选填(没有就默认待办、已办) 格式col_waitSend,col_sent,col_pending,col_done 对应待发、已发、待办、已办
     *    startDate : String 开始时间  格式 时间戳字符串
     *    endDate : String  结束时间  格式 时间戳字符串
     *    pageNo 当前页 Integer | 默认值:1
     *    pageSize 查询条数 Integer | 默认值:3
     * </pre>
     * @throws BusinessException
     */
	@POST
    @Path("getCollListByRobot")
    public Response getCollListByRobot(Map<String, Object> params) throws BusinessException {
		String status = ParamUtil.getString(params, "status", "col_pending,col_done");
		String startMemberName = ParamUtil.getString(params, ColQueryCondition.startMemberName.name(), "");
		String startMemberId = ParamUtil.getString(params, ColQueryCondition.startMemberId.name(),null);
		String subject = ParamUtil.getString(params, ColQueryCondition.subject.name(), "");
		String startDate = ParamUtil.getString(params, "startDate", "");
		String endDate = ParamUtil.getString(params, "endDate", "");
		
		if(Strings.isNotBlank(startMemberId)) {
			String[] memberArr = startMemberId.split("[|]");
			if("Member".equals(memberArr[0])) {
				startMemberId = memberArr[1];
			}
		}
		
		String date = "";
		if(Strings.isDigits(startDate)) {
		    Date sDate = new Date(Long.valueOf(startDate));
		    date = Datetimes.format(sDate, Datetimes.dateStyle);
		}
		if(Strings.isDigits(endDate)) {
            Date eDate = new Date(Long.valueOf(endDate));
            date += "#" + Datetimes.format(eDate, Datetimes.dateStyle); // 拼接成后面需要的数据结构
        }
		
		Integer pageNo = ParamUtil.getInt(params, "pageNo", 1);
		Integer pageSize = ParamUtil.getInt(params, "pageSize", 3);
		
		List<XiaozListCard> datas = new ArrayList<XiaozListCard>();
		
		FlipInfo fi = super.getFlipInfo();
        fi.setPage(pageNo);
        fi.setSize(pageSize);
        
		try {
			Map<String, String> conditionMap = new HashMap<String, String>();
			User currentUser = AppContext.getCurrentUser();
			conditionMap.put(ColQueryCondition.currentUser.name(), String.valueOf(currentUser.getId()));
			conditionMap.put(ColQueryCondition.startMemberName.name(), startMemberName);
			conditionMap.put(ColQueryCondition.startMemberId.name(), startMemberId);
			conditionMap.put(ColQueryCondition.subject.name(), subject);
			conditionMap.put("needSummary","1"); // 需要Summary相关数据
			
			XiaozListCard card = null;
			String[] statusArr = status.split(",");
			
			for (String st : statusArr) {
		        
                if(StateEnum.col_waitSend.name().equals(st)) { // 待发
                    conditionMap.put(ColQueryCondition.createDate.name(), date);
                    fi = collaborationApi.findWaitSentAffairs(fi, conditionMap);
                    card = convertListSimpleVO2XiaozList(fi,conditionMap,StateEnum.col_waitSend, 
                                ColOpenFrom.listWaitSend.name(), ResourceUtil.getString("coll.toSend.list"));
                    // 需要移除，以免对后续查询产生影响，后面同理
                    conditionMap.remove(ColQueryCondition.createDate.name(), date);
                } else if (StateEnum.col_sent.name().equals(st)) { // 已发
                    conditionMap.put(ColQueryCondition.createDate.name(), date);
                    fi = collaborationApi.findSentAffairs(fi, conditionMap);
                    card = convertListSimpleVO2XiaozList(fi,conditionMap,StateEnum.col_sent,
                                ColOpenFrom.listSent.name(), ResourceUtil.getString("coll.send.list")); 
                    conditionMap.remove(ColQueryCondition.createDate.name(), date);
                } else if (StateEnum.col_pending.name().equals(st)) { // 待办
                    conditionMap.put(ColQueryCondition.receiveDate.name(), date);
                    fi = collaborationApi.findPendingAffairs(fi, conditionMap);
                    card = convertListSimpleVO2XiaozList(fi,conditionMap,StateEnum.col_pending,
                                ColOpenFrom.listPending.name(), ResourceUtil.getString("coll.pending.list")); 
                    conditionMap.remove(ColQueryCondition.receiveDate.name(), date);
                } else if (StateEnum.col_done.name().equals(st)) { // 已办
                    conditionMap.put(ColQueryCondition.completeDate.name(), date);
                    fi = collaborationApi.findDoneAffairs(fi, conditionMap);
                    card = convertListSimpleVO2XiaozList(fi,conditionMap,StateEnum.col_done,
                            ColOpenFrom.listDone.name(), ResourceUtil.getString("coll.done.list.label"));
                    conditionMap.remove(ColQueryCondition.completeDate.name(), date);
                } else {
                    card = null; // 置空
                }
                if(card != null) {
                    datas.add(card);
                }
            }			
		} catch (Exception e) {
			LOGGER.error("获取事项列表失败！", e);
			return success(datas, ResourceUtil.getString("coll.query.error"), 
			                      Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		String message = null;
		// 接口规定，如果数据都为空，直接返回null
		if(Strings.isEmpty(datas)) {
		    datas = null;
		    message = ResourceUtil.getString("coll.query.nocoll");    // 没有找到相关协同
		} else {
		    message = ResourceUtil.getString("coll.query.findcoll");  // 为您找到以下数据
		}
		
		return success(datas, message, Response.Status.OK.getStatusCode());
	}
    
    private XiaozListCard convertListSimpleVO2XiaozList(FlipInfo info, Map<String, String> conditionMap,
            StateEnum status,String openFrom, String classifyName) {
        
        XiaozListCard listCard = new XiaozListCard(); 
        
        if (info != null && Strings.isNotEmpty(info.getData())) {
            List<XiaozCommondCard> commondCards = new ArrayList<XiaozCommondCard>();
            
            List<ColListSimpleVO> data = info.getData();
            XiaozCommondCard card = null;
            for (ColListSimpleVO c : data) {
                card = new XiaozCommondCard();
                
                card.setTitle(c.getSubject());
                card.setIconType(ICON_TYPE_MEMBER);
                card.setIconValue(String.valueOf(c.getStartMemberId()));
                card.setMemberName(c.getStartMemberName());                
                card.setBeginDate(String.valueOf(c.getStartDate().getTime()));
                
                if(!StateEnum.col_waitSend.equals(status)) { // 待发不显示回复数，其他显示
                    Integer replyCounts = c.getReplyCounts();
                    card.setExtInfo(ResourceUtil.getString("collaboration.pending.replyCounts.label", 
                            replyCounts == null ? 0 : replyCounts));                    
                }
                
                CollGotoParamsVO gotoParams = new CollGotoParamsVO();
                gotoParams.setSummaryId(c.getSummaryId());
                
                card.setGotoParams(gotoParams);
                
                commondCards.add(card);
            }
            
            listCard.setData(commondCards);
        } else {
            // 如果数据为空，直接返回null
            return null;
        }
        
        listCard.setClarifyName(classifyName);
        
        CollQueryParamsVO param = CollQueryParamsVO.convert(conditionMap);
        param.setStatus(status.name());
        param.setOpenFrom(openFrom);
        
        listCard.setQueryParams(param);
        
        return listCard;
    }

    /**
     * 语音机器人-发送协同
     * @param params	Map<String, Object> | 必填 | map类型参数
     * <pre>
     * 	  subject | 必填 | 协同标题
     *    content | 可选 | 协同内容
     *    members | 必填 | 发送人员(并发) String
     *    	格式：Member|111,Member|222,Member|333
     *    sourceId | 必填 | 关联ID，此处是小致产生的关联id，用于关联传透对应的数据
     * </pre>
     * @return Map<String, Object>
     * <pre>
     * 	  error_msg 报错信息
     * </pre>
     * @throws BusinessException
     */
    @SuppressWarnings("rawtypes")
	@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("sendByRobot")
    public Response sendByRobot(Map<String, Object> params) throws BusinessException {
    	Response response = null;
    	
    	User currentUser = AppContext.getCurrentUser();
    	
    	String subject = ParamUtil.getString(params, "subject", "");
    	String content = ParamUtil.getString(params, "content", "");
    	String members = ParamUtil.getString(params, "members", "");
    	String sourceId = ParamUtil.getString(params, "sourceId", "");
    	
    	if(Strings.isBlank(members)) {
    		return success(null, ResourceUtil.getString("coll.summary.validate.lable43"), 
    		                     Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    	}
    	
    	if(!Strings.isDigits(sourceId)) {
    	    // source id 非法
            return success(null, ResourceUtil.getString("coll.summary.validate.sourceidillegal"),
                                 Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    	}
    	
    	// 直接使用小致传递的sourceId为summaryId, 经过沟通小致sourceId也是使用的UUID
    	Long summaryId = Long.valueOf(sourceId);
    	
    	//组装流程参数process_xml
    	String process_xml = "";
		try {
			//保存并获取process_xml保存的临时ID
			Map<String, String> wfParams = new HashMap<String, String>();
			wfParams.put("members", members);
			wfParams.put("action", "sendByRobot");
			String[] result = createRobotProcessXmlAndJson(wfParams);
			process_xml = result[0];
		} catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
    	
        try {
        	saveRobotContent(summaryId, subject, content, currentUser.getId());
        	
        	String jsonParams = getRobotCollJsonParams(summaryId, subject, process_xml, -1L);
        	params.put(JSON_PARAMS, jsonParams);
        	
        	// 发送协同
        	Map<String, String> ret = sendColl(params);
        	XiaozCommondCard card = null;
        	if(ret.containsKey(ERROR_KEY)){
                response = success(null, ret.get(ERROR_KEY), 
                                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }else {
                card =  buildXiaozCommonCard(ret,subject,currentUser);
                response = success(card,ResourceUtil.getString("coll.send.success"), // 好的，已发送成功
                        Response.Status.OK.getStatusCode());
            }
        } catch(Exception e) {
        	LOGGER.error("发送协同错误:",e);
        	response = success(null, ResourceUtil.getString("coll.send.fail"), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
        	removeThreadContext();
        }
        
    	return response; 
    }
    
    /**
     * 
     * <pre>
     *      构建小致公共卡片回传参数
     *     
     * @param sendRet 发送成功后返回数据
     * @param subject 协同标题
     * @param currentUser 当前用户数据
     * @return: XiaozCommondCard  
     * @date:   2019年5月29日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre>
     */
    private XiaozCommondCard buildXiaozCommonCard(Map<String, String> sendRet, String subject, User currentUser) {
        
        XiaozCommondCard card = new XiaozCommondCard();
        card.setTitle(subject);
        
        card.setIconType(ICON_TYPE_MEMBER);
        card.setIconValue(String.valueOf(currentUser.getId()));
        card.setMemberName(currentUser.getName());
        long sDate = Datetimes.parse(sendRet.get("startDate")).getTime();
        card.setBeginDate(String.valueOf(sDate));
        
        CollGotoParamsVO gotoParams = new CollGotoParamsVO();
        gotoParams.setSummaryId(sendRet.get("summaryId"));
        
        card.setGotoParams(gotoParams);
        
        return card;
    }
    /**
     * 语音机器人- 获取协同数据
     * @param params    Map<String, Object> | 必填 | map类型参数
     * <pre>
     *    sourceId | 必填 | 关联ID，此处是小致产生的关联id，用于关联传透对应的数据
     * </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getSummary4Robot")
    public Response getSummary4Robot(Map<String,String> params) throws BusinessException {
        
        Response response = null;
        
        String sourceId = ParamUtil.getString(params, "sourceId", "");
        
        // 如果传入不是数字类型
        if(!Strings.isDigits(sourceId)) {
            // source id 非法
            return success(null, ResourceUtil.getString("coll.summary.validate.sourceidillegal"),
                                 Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        
        try {
            Long summaryId = Long.valueOf(sourceId);
            ColSummary colSummary = colManager.getColSummaryById(summaryId);
            
            XiaozCommondCard card = buildXiaozCommonCard(colSummary);
            // 获取发起人事项
            CtpAffair senderAffair = affairManager.getSenderAffair(summaryId);
            String message = null;
            if(senderAffair != null && senderAffair.getState() != null) {
                Integer state = senderAffair.getState();
                if(StateEnum.col_waitSend.getKey() == state) {
                    message = ResourceUtil.getString("coll.saveToSend.success"); // 保存待发成功
                } else if (StateEnum.col_sent.getKey() == state) {
                    message = ResourceUtil.getString("coll.send.success"); // 好的，已发送成功
                }
            }

            if(Strings.isBlank(message)) {
                message = ResourceUtil.getString("coll.query.success");  // 数据查询成功
            }
            
            response = success(card, message, Response.Status.OK.getStatusCode());
        } catch(Exception e) {
            LOGGER.error("语音机器人- 获取协同数据错误：",e);
            response = success(null, ResourceUtil.getString("coll.query.error"), 
                                     Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            removeThreadContext();
        }
        
        return response;
    }
    
    /** 
     * <pre>
     * 构建小致公共卡片回传参数
     *     
     * @param colSummary 协同主表数据
     * @return: XiaozCommondCard  
     * @date:   2019年5月30日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    private XiaozCommondCard buildXiaozCommonCard(ColSummary colSummary) {
        
        if(colSummary == null) {
            return null;
        }
        
        XiaozCommondCard card = new XiaozCommondCard();
        card.setTitle(colSummary.getSubject());
        card.setIconType(ICON_TYPE_MEMBER);
        card.setIconValue(String.valueOf(colSummary.getStartMemberId()));
        
        try {
            V3xOrgMember member = orgManager.getMemberById(colSummary.getStartMemberId());
            
            if(member != null) {
                card.setMemberName(member.getName());                
            }
        } catch (BusinessException e) {
            LOGGER.error("获取人员错误:" + e.getMessage());
        }
        Date startDate = colSummary.getStartDate();
        if(startDate != null) {
            card.setBeginDate(String.valueOf(startDate.getTime()));            
        }
        Date finishDate = colSummary.getFinishDate();
        if(finishDate != null) {
            card.setEndDate(String.valueOf(finishDate.getTime()));
        }
        
        CollGotoParamsVO gotoParams = new CollGotoParamsVO();
        gotoParams.setSummaryId(String.valueOf(colSummary.getId()));
        
        card.setGotoParams(gotoParams);
        
        return card;
    }
    /**
     * 语音机器人-新建协同加载流程
     * @param params	Map<String, Object> | 必填 | map类型参数
     * <pre>
     *    members 发送人员(并发) String
     *    	格式：Member|111,Member|222,Member|333
     * </pre>
     * @return String[]
     * @throws BusinessException
     */
    @POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getCollProcessXmlandJson")
	public Response getCollProcessXmlandJson(Map<String, String> params) throws ServiceException {
		try {
        	String[] result = createRobotProcessXmlAndJson(params);
			return ok(result);
		} catch (BusinessException e) {
			throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
		}
	}

    /**
     * 根据模板id、节点id、关联id查找配置信息
     * @param templateId	String	|	必填	|	模板ID
     * @param activityId	String	|	必填	|	节点ID
     * @param DR	String	|	必填	|	相关数据配置关联ID
     * @param affairId	String	| 必填	|	事项ID
     * @param projectId	String	| 必填	|	项目ID
     * @param summaryId	String	| 必填	|	协同ID
     * @param memberId	String	| 必填	|	人员ID
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getDataRelationByDR")
    public Response getDataRelationByDR(@QueryParam("templateId") String str_templateId,
                                         @QueryParam("activityId") String activityId,
                                         @QueryParam("DR") String DR,
                                         @QueryParam("affairId") String str_affairId,
                                         @QueryParam("projectId") String str_projectId,
                                         @QueryParam("summaryId") String str_summaryId,
                                         @QueryParam("memberId") String str_memberId
    ) throws BusinessException, SQLException {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("templateId", Long.valueOf(str_templateId));
        params.put("activityId", activityId);
        params.put("DR", DR);
        Long affairId = Long.valueOf(str_affairId);
        params.put("affairId", affairId);
        if("null".equals(str_projectId) || "undefined".equals(str_projectId)){
            str_projectId = "-1";
        }
        params.put("projectId", Long.valueOf(str_projectId));
        params.put("summaryId",  Long.valueOf(str_summaryId));
        params.put("memberId", Long.valueOf(str_memberId));
        params.put("isM3", "1");
        //获取配置的对象
        List<BaseConfigVO> list = dataRelationApi.findRelationDatasByDR(params);

        //获取配置的查询项
        Map<String, List<Map<String, Object>>> map_queryType = new HashMap<String, List<Map<String, Object>>>();
        for(BaseConfigVO vo : list){
            if("formStat".equals(vo.getDataTypeName()) || "formSearch".equals(vo.getDataTypeName())){
                map_queryType.put(String.valueOf(vo.getId()), dataRelationApi.findFormSearchCond(vo.getId(), affairId));
            }else{
                map_queryType.put(String.valueOf(vo.getId()), new ArrayList<Map<String, Object>>());
            }
        }

        Map<String, Object> map_result = new HashMap<String, Object>();
        map_result.put("configVO", list);
        map_result.put("queryType", map_queryType);
        return success(map_result);
    }

    /**
     * 查找自由协同相关数据
     * @param activityId	String	|	必填	|	节点ID
     * @param affairId	String	| 必填	|	事项ID
     * @param projectId	String	| 必填	|	项目ID
     * @param summaryId	String	| 必填	|	协同ID
     * @param memberId	String	| 必填	|	人员ID
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getSelfCollConfig")
    public Response getSelfCollConfig(@QueryParam("activityId") String activityId,
                                        @QueryParam("affairId") String str_affairId,
                                        @QueryParam("projectId") String str_projectId,
                                        @QueryParam("summaryId") String str_summaryId,
                                        @QueryParam("memberId") String str_memberId
    ) throws BusinessException, SQLException {

    	if(Strings.isBlank(activityId) || Strings.isBlank(str_affairId) || Strings.isBlank(str_projectId) ||
    		Strings.isBlank(str_summaryId) || Strings.isBlank(str_memberId)){
        	Map<String, Object> ret = new HashMap<String, Object>();
        	ret.put(ERROR_KEY, ResourceUtil.getString("collaboration.param.notEmpty"));
        	return ok(ret);
        }
    	
        Long summaryId = Long.valueOf(str_summaryId);
        Long affairId = Long.valueOf(str_affairId);
        if("null".equals(str_projectId)){
            str_projectId = "-1";
        }
        Long projectId = Long.valueOf(str_projectId);
        Long memberId = Long.valueOf(str_memberId);
        //获取配置的对象
        
        Map<String,Object> drParmas = new HashMap<String,Object>();
        drParmas.put("summaryId", summaryId);
        drParmas.put("affairId", affairId);
        drParmas.put("activityId", activityId);
        drParmas.put("projectId", projectId);
        drParmas.put("memberId", memberId);
        
        List<BaseConfigVO> list = dataRelationApi.findSelfCollConfig(drParmas);

        Map<String, Object> map_result = new HashMap<String, Object>();
        map_result.put("configVO", list);
        return success(map_result);
    }

    /**
     * 获取相关数据列表
     * @param templateId	String	|	必填	|	模板ID
     * @param activityId	String	|	必填	|	节点ID
     * @param DR	String	|	必填	|	相关数据配置关联ID
     * @param affairId	String	| 必填	|	事项ID
     * @param projectId	String	| 必填	|	项目ID
     * @param summaryId	String	| 必填	|	协同ID
     * @param memberId	String	| 必填	|	人员ID
     * @param senderId	String	| 必填	|	发起人ID
     * @param formMasterId	String	| 必填	|	主表记录ID
     * @param poIds	String	| 必填	|	请求的模板ID
     * @param nodePolicy	String	| 必填	|	当前权限
     * @param pageConditions	String	| 必填	|	查询条件
     *    示例：{"-3517166301961043435":[{"rightChar":"","fieldName":"field0001","field0001":"-4607932205667103316",
     *    "leftChar":"","rowOperation":"and","trunkFieldName":"field0012","operation":"=","fieldValue":"-4607932205667103316"}
     *    ,{"rightChar":"","fieldName":"field0005","field0005":"","leftChar":"","rowOperation":"and",
     *    "trunkFieldName":"field0015","operation":"like","fieldValue":""}]}
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getByDataRelationIds")
    public Response getByDataRelationIds(Map<String,Object> paramsCon ) throws BusinessException, SQLException {
        String str_templateId = ParamUtil.getString(paramsCon, "templateId", "");
        String activityId = ParamUtil.getString(paramsCon, "activityId", "");
        String DR = ParamUtil.getString(paramsCon, "DR", "");
        String str_affairId = ParamUtil.getString(paramsCon, "affairId", "");
        String str_projectId = ParamUtil.getString(paramsCon, "projectId", "");
        String str_summaryId = ParamUtil.getString(paramsCon, "summaryId", "");
        String str_memberId = ParamUtil.getString(paramsCon, "memberId", "");
        String str_senderId = ParamUtil.getString(paramsCon, "senderId", "");
        String str_formMasterId = ParamUtil.getString(paramsCon, "formMasterId", "");
        String str_poIds = ParamUtil.getString(paramsCon, "poIds", "");
        String str_nodePolicy = ParamUtil.getString(paramsCon, "nodePolicy", "");
        String str_pageConditions = ParamUtil.getString(paramsCon, "pageConditions", "");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activityId", activityId);
        params.put("DR", DR);
        params.put("nodePolicy", str_nodePolicy);
        params.put("senderId", Long.valueOf(str_senderId));
        if(Strings.isEmpty(str_templateId) || "null".equals(str_templateId)){
            params.put("templateId", null);
        }else{
            params.put("templateId", Long.valueOf(str_templateId));
        }

        params.put("affairId", Long.valueOf(str_affairId));
        params.put("formMasterId", Long.valueOf(str_formMasterId));
        params.put("summaryId",  Long.valueOf(str_summaryId));
        params.put("memberId", Long.valueOf(str_memberId));
        if("null".equals(str_projectId) || "undefined".equals(str_projectId) ||Strings.isEmpty(str_projectId)){
            str_projectId = "-1";
        }
        params.put("projectId", Long.valueOf(str_projectId));
        String[] poIds = str_poIds.split(",");
        List<String> list = Arrays.asList(poIds);
        params.put("poIds", list);
        params.put("pageConditions",  JSONUtil.parseJSONString(str_pageConditions, Map.class));

        Map<String, Object> map = dataRelationApi.findByDataRelationIds(params);
        return success(map);
    }

    /**
     * 获取相关数据说明信息    功能暂时取消、先注释
     * @param templateId	String	|	必填	|	模板ID
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("getDataRelationInstruction")
    public Response getDataRelationInstruction(@QueryParam("templateId") String str_templateId
    ) throws BusinessException, SQLException {

    	Map<String, Object> result = new HashMap<String, Object>();
    	
    	if(Strings.isNotBlank(str_templateId)){//不是自由协同存为的模板
	        Long templateId = Long.valueOf(str_templateId);
	        CtpTemplate template = templateManager.getCtpTemplate(templateId);
	        String departName = "";
	        String formName = "";
	        String createMemberName = "";
	        String templateName = "";
	        FormBean fb = new FormBean();
	        if(template != null){
	        	templateName = template.getSubject();
	        	if(template.getFormParentid() != null){
	        		CtpTemplate parentTemplate = templateManager.getCtpTemplate(template.getFormParentid());
	        		try {
	        			if(parentTemplate != null &&  parentTemplate.getFormAppId()!=null){
	        				fb = formManager.getForm(parentTemplate.getFormAppId());
	        			}
					} catch (Exception e) {
						LOGGER.error("", e);
					}
	        	}else{
	        		templateName = template.getSubject();
	        		try {
	        			if(template != null &&  template.getFormAppId()!=null){
	        				fb = formManager.getForm(template.getFormAppId());
	        			}
					} catch (Exception e) {
						LOGGER.error("", e);
					}
	        	}
	        	if(fb != null){
	        		formName  = fb.getFormName();
	        	}
	        	V3xOrgMember member = orgManager.getMemberById(template.getMemberId());
	        	if(member != null){
	        		try{
	        			createMemberName = Functions.showMemberName(member);
	        			departName = OrgHelper.showDepartmentFullPath(member.getOrgDepartmentId());
	        		}catch(Exception e){
	        			LOGGER.error("", e);
	        		}
	        	}
	        }
	        result.put("templateName",templateName);//模板名称
	        result.put("formName",formName);//表单名称
	        result.put("createMemberName",createMemberName);//创建人名称
	        result.put("departName",departName);//部门
        }
    	return success(result);
    }
     */

    /**
     * 模板数据更多
     * @param id	String	|	必填	|	配置的模板ID
     * @param templateId	String	|	必填	|	模板ID
     * @param memberId	String	|	必填	|	人员ID
     * @param summaryId	String	|	必填	|	协同ID
     * @param senderId	String	|	必填	|	发送人员ID
     * @param searchCondition	String	|	非必填	|	查询条件
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("templateDealMore")
    public Response templateDealMore(@QueryParam("id") String str_id,
            						@QueryParam("templateId") String str_templateId,
            						@QueryParam("memberId") String str_memberId,
            						@QueryParam("summaryId") String str_summaryId,
            						@QueryParam("senderId") String str_senderId,
                                    @QueryParam("searchCondition") String str_searchCondition
    ) throws BusinessException, SQLException {
    	FlipInfo flipInfo = getFlipInfo();
    	
    	Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", Long.valueOf(str_id));
        param.put("templateId", Long.valueOf(str_templateId));
        param.put("memberId", Long.valueOf(str_memberId));
        param.put("senderId", Long.valueOf(str_senderId));
        param.put("nodePolicy", "collaboration");
        param.put("summaryId", str_summaryId);

        if(!"undefined".equals(str_searchCondition) && Strings.isNotBlank(str_searchCondition)){
            Map searchCondition = JSONUtil.parseJSONString(str_searchCondition, Map.class);
            Map item = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("item")), Map.class);
            String condition = String.valueOf(item.get("condition"));
            List searchKey = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("searchKey")), List.class);
            if("subject".equals(condition)){
                param.put("subject", searchKey.get(0));
            }else if("sender".equals(condition)){
                param.put("startMemberName", searchKey.get(0));
            }else if("sendDate".equals(condition)){
                param.put("createDate", searchKey.get(0) + "#" + searchKey.get(1));
            }else if("receiverDate".equals(condition)){
                param.put("receiveDate", searchKey.get(0) + "#" + searchKey.get(1));
            }
        }

        flipInfo = dataRelationHandler.getDataByDealTemplate(flipInfo, param);
    	return ok(flipInfo);
    }

    /**
     * 模板数据更多（新建）
     * @param id	String	|	必填	|	配置的模板ID
     * @param templateId	String	|	必填	|	模板ID
     * @param summaryId	String	|	必填	|	协同ID
     * @param searchCondition	String	|	非必填	|	查询条件
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("templateSendMore")
    public Response templateSendMore(@QueryParam("id") String str_id,
                                     @QueryParam("templateId") String str_templateId,
                                     @QueryParam("summaryId") String str_summaryId,
                                     @QueryParam("searchCondition") String str_searchCondition
    ) throws BusinessException, SQLException {
        FlipInfo flipInfo = getFlipInfo();

        if(Strings.isBlank(str_id) || Strings.isBlank(str_templateId) || Strings.isBlank(str_summaryId)){
        	Map<String, Object> ret = new HashMap<String, Object>();
        	ret.put(ERROR_KEY, ResourceUtil.getString("collaboration.param.notEmpty"));
        	return ok(ret);
        }
        
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", Long.valueOf(str_id));
        param.put("templateId", Long.valueOf(str_templateId));
        param.put("summaryId", Long.valueOf(str_summaryId));
        param.put("openFrom", "more");
        param.put("affairId", -1l);

        if(!"undefined".equals(str_searchCondition) && Strings.isNotBlank(str_searchCondition)){
            Map searchCondition = JSONUtil.parseJSONString(str_searchCondition, Map.class);
            Map item = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("item")), Map.class);
            String condition = String.valueOf(item.get("condition"));
            List searchKey = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("searchKey")), List.class);
            if("subject".equals(condition)){
                param.put("subject", searchKey.get(0));
            }else if("sendDate".equals(condition)){
                param.put("createDate", searchKey.get(0) + "#" + searchKey.get(1));
            }
        }

        flipInfo = dataRelationHandler.getDataBySendTemplate(flipInfo, param);
        return ok(flipInfo);
    }

    /**
     * 自由协同更多
     * @param id	String	|	必填	|	配置的模板ID
     *     示例：我收到发起人的协同(10000);发起人收到我的协同(10001);流程追溯数据(999999)
     * @param memberId	String	|	必填	|	人员ID
     * @param summaryId	String	|	必填	|	协同ID
     * @param affairId	String	|	必填	|	事项ID
     * @param senderId	String	|	必填	|	发送人员ID
     * @param searchCondition	String	|	非必填	|	查询条件
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("selfCollMore")
    public Response selfCollMore(@QueryParam("id") String str_id,
                                 @QueryParam("memberId") String str_memberId,
                                 @QueryParam("summaryId") String str_summaryId,
                                 @QueryParam("affairId") String str_affairId,
                                 @QueryParam("senderId") String str_senderId,
                                 @QueryParam("searchCondition") String str_searchCondition
    ) throws BusinessException, SQLException {
        FlipInfo flipInfo = getFlipInfo();

        if(Strings.isBlank(str_id) || Strings.isBlank(str_memberId) || Strings.isBlank(str_summaryId)
        	|| Strings.isBlank(str_affairId) || Strings.isBlank(str_senderId)){
        	Map<String, Object> ret = new HashMap<String, Object>();
        	ret.put(ERROR_KEY, ResourceUtil.getString("collaboration.param.notEmpty"));
        	return ok(ret);
        }
        
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", Long.valueOf(str_id));
        param.put("summaryId", Long.valueOf(str_summaryId));
        param.put("affairId", Long.valueOf(str_affairId));
        param.put("memberId", Long.valueOf(str_memberId));
        param.put("senderId", Long.valueOf(str_senderId));
        param.put("openFrom", "more");

        if(!"undefined".equals(str_searchCondition) && Strings.isNotBlank(str_searchCondition)){
            Map searchCondition = JSONUtil.parseJSONString(str_searchCondition, Map.class);
            Map item = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("item")), Map.class);
            String condition = String.valueOf(item.get("condition"));
            List searchKey = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("searchKey")), List.class);
            if("subject".equals(condition)){
                param.put("subject", searchKey.get(0));
            }else if("sender".equals(condition)){
                param.put("startMemberName", searchKey.get(0));
            }else if("sendDate".equals(condition)){
                param.put("createDate", searchKey.get(0) + "#" + searchKey.get(1));
            }else if("receiverDate".equals(condition)){
                param.put("receiveDate", searchKey.get(0) + "#" + searchKey.get(1));
            }
        }

        flipInfo = dataRelationHandler.getSelfCollData(flipInfo, param);
        return ok(flipInfo);
    }

    /**
     * 项目更多
     * @param id	String	|	必填	|	配置的模板ID
     * @param searchCondition	String	|	非必填	|	查询条件
     * 		示例：searchCondition={"item":{"type":"text","condition":"subject","text":"标题"},"searchKey":["负责人"]}
     * @param templateId	String	|	非必填	|	模板ID
     * @param affairId	String	|	非必填	|	事项ID
     * @return	Map<String, Object>
     * @throws BusinessException
     * @throws SQLException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("projectMore")
    public Response projectMore(@QueryParam("id") String str_id,
                                @QueryParam("searchCondition") String str_searchCondition,
                                @QueryParam("templateId") String str_templateId,
                                @QueryParam("affairId") String str_affairId
    ) throws BusinessException, SQLException {
        if(Strings.isBlank(str_id)){
        	Map<String, Object> ret = new HashMap<String, Object>();
        	ret.put(ERROR_KEY, ResourceUtil.getString("collaboration.param.notEmpty"));
        	return ok(ret);
        }
        
    	FlipInfo flipInfo = getFlipInfo();
    	DataRelationPO po = dataRelationApi.getById(Long.valueOf(str_id));
    	BaseConfigVO baseVO = dataRelationApi.getBaseConfigVO(po);
    	ProjectConfigVO config = (ProjectConfigVO)baseVO;
        List<Long> projectIds = config.getIds();
        
        //判断当前配置是否是默认配置,如果是默认配置查询协同中的关联项目.
        ProjectConfigs configs = JSONUtil.parseJSONString(po.getConfigs(), ProjectConfigs.class);
        if (Strings.isEmpty(projectIds) && configs.getHasProject() && Long.valueOf(-1L).equals(po.getProcessId()) 
        	&& "-1".equals(po.getActivityId()) && Strings.isNotBlank(str_templateId) && Strings.isNotBlank(str_affairId)) {//默认关联项目，查找有权限的项目
            //非关联项目
            CtpTemplate template = this.templateManager.getCtpTemplate(Long.valueOf(str_templateId));
            Long projectId =  colDataRelationHandler.getProjectIdByAffairId(Long.valueOf(str_affairId));
            
            if (projectId != null) {
                projectIds.add(projectId);
            } else if (template != null && template.getProjectId() != null) {
                projectIds.add(template.getProjectId());
            }
        }
        
        if (Strings.isNotEmpty(projectIds)) {
        	//查找某人的所有有权限的项目
            List<ProjectBO> projectList = this.projectApi.findProjectsByMemberId(AppContext.currentUserId());
            //根据有权限的项目返回
            List<Long> hasAclProjectIds = new ArrayList<Long>();
            for (ProjectBO bo : projectList) {
                hasAclProjectIds.add(bo.getId());
            }
            hasAclProjectIds.retainAll(projectIds);
            
            List<ProjectBO> boList = projectApi.findProjectByIds(hasAclProjectIds);

            if(!"undefined".equals(str_searchCondition) && Strings.isNotBlank(str_searchCondition)){
                Map searchCondition = JSONUtil.parseJSONString(str_searchCondition, Map.class);
                Map item = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("item")), Map.class);
                String condition = String.valueOf(item.get("condition"));
                List searchKey = JSONUtil.parseJSONString(String.valueOf(searchCondition.get("searchKey")), List.class);
                if("subject".equals(condition)){
                    String subject = String.valueOf(searchKey.get(0));
                    Iterator it = boList.iterator();
                    while(it.hasNext()){
                        ProjectBO projectBO = (ProjectBO)it.next();
                        if(projectBO.getProjectName() != null && projectBO.getProjectName().indexOf(subject) == -1){
                            it.remove();
                        }
                    }
                }
            }

            DBAgent.memoryPaging(boList, flipInfo);
        }
    	return ok(flipInfo);
    }

    
    

    /**
     * 语音机器人-发送/新建协同时，通过传参创建流程
     * @param params
     * @return
     * @throws BusinessException
     */
    private String[] createRobotProcessXmlAndJson(Map<String, String> params) throws BusinessException {
    	User currentUser = AppContext.getCurrentUser();
    	
    	//语音机器人-发送人员
    	String members = ParamUtil.getString(params, "members", "");
    	
		String[] membersArr = members.split(",");
		StringBuilder mBuffer = new StringBuilder();
		
		for(String member : membersArr) {
			String[] memberArr = member.split("[|]");
			if("Member".equals(memberArr[0])) {
			    V3xOrgMember orgMember = orgManager.getMemberById(Long.parseLong(memberArr[1]));
			    V3xOrgAccount orgAccount = orgManager.getAccountById(orgMember.getOrgAccountId());
				if(mBuffer.length() != 0) {
					mBuffer.append(",");
				}
				mBuffer.append("{");
				mBuffer.append("\"id\" : \"" + orgMember.getId() + "\",");
				mBuffer.append("\"name\" : \"" + orgMember.getName() + "\",");
				mBuffer.append("\"excludeChildDepartment\" : \"false\",");
				mBuffer.append("\"includeChild\" : \"true\",");
				mBuffer.append("\"accountId\" : \"" + orgAccount.getId() + "\",");
				mBuffer.append("\"accountName\" : \"" + orgAccount.getName() + "\",");
				mBuffer.append("\"entityType\" : \"Member\"");
	    		mBuffer.append("}");
			}
		}
		String orgJson = "[" + mBuffer.toString() + "]";
		
		//工作流xml串，值为null,就表示全新选人，有值，就是在现有的流程上编辑
		String workflowXml = "";
		//当前节点Id
		String currentNodeId = "";
		//0：串发，1：并发，2：会签
		String type = "1";
		//当前登录人ID
		String currentUserId = String.valueOf(currentUser.getId());
		//当前登录人名称
		String currentUserName = currentUser.getName();
		//当前登录人所在单位ID
		String currentAccountId = String.valueOf(currentUser.getLoginAccount());
		//当前登录人所在单位名称
		String currentAccountName = currentUser.getLoginAccountName();
		//默认节点权限
        PermissionVO permission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(),AppContext.getCurrentUser().getLoginAccount());
		//默认节点权限ID
		String defaultPolicyId = permission.getName();
		//默认节点权限名称
		String defaultPolicyName = permission.getLabel();
		//流程实例ID
		String caseId = ParamUtil.getString(params, "caseId", "");
		
	    String[] result = new String[2];
	    
		try {
		    
		    List<BPMHumenActivity> addHumanNodes = new ArrayList<BPMHumenActivity>();
			String processXml = wapi.freeAddNode(workflowXml,orgJson,currentNodeId,type,currentUserId,currentUserName,currentAccountId,
					currentAccountName,defaultPolicyId,defaultPolicyName, addHumanNodes);
			
			//重置processXml返回结果
			result[0]= wapi.savedProcessXmlTempAndReturnId(null, processXml, currentNodeId, currentUserId, "-1");
			
			if(!"sendByRobot".equals(ParamUtil.getString(params, "action"))) {
				//流程处理过程中，加签 ，当前会签 只显示的局部节点
			    String showNodes =  ParamUtil.getString(params, "showNodes", "");
			    List<String> showList = new ArrayList<String>();
			    if(Strings.isNotBlank(showNodes)){
			        String[] ns = showNodes.split(",");
			        for(String i : ns){
			        	showList.add(i);
			        }
			        for(BPMHumenActivity b : addHumanNodes){
	                    showList.add(b.getId());
	                }
			    }
			    String processJson = wapi.getWorkflowJsonForMobile(processXml, showList, caseId);
			    result[1]= processJson;
			}
		} catch (Exception e) {
			LOGGER.error("获取流程图数据异常,workflowXml:="+workflowXml+";orgJson:="+orgJson+";currentNodeId:="+currentNodeId+";type:="+type, e);
			throw new BusinessException(e);
		}
		return result;
    }
    
    private String getRobotCollJsonParams(Long summaryId, String subject, String process_xml, Long contentSaveId) {
    	
    	//组装流程参数
        StringBuilder wfBuffer = new StringBuilder();
    	wfBuffer.append("   \"workflow_definition\" : {");
    	wfBuffer.append("	   \"process_desc_by\" : \"xml\",");
    	wfBuffer.append("	   \"process_xml\" : \"" + process_xml + "\",");
    	wfBuffer.append("	   \"readyObjectJSON\" : \"\",");
    	wfBuffer.append("	   \"workflow_data_flag\" : \"WORKFLOW_SEEYON\",");
    	wfBuffer.append("	   \"process_info\" : \"\",");
    	wfBuffer.append("	   \"process_info_selectvalue\" : \"\",");
    	wfBuffer.append("	   \"process_subsetting\" : \"\",");
    	wfBuffer.append("	   \"moduleType\" : \"1\",");
    	wfBuffer.append("	   \"workflow_newflow_input\" : \"\",");
    	wfBuffer.append("	   \"process_rulecontent\" : \"\",");
    	wfBuffer.append("	   \"workflow_node_peoples_input\" : \"\",");
    	wfBuffer.append("	   \"workflow_node_condition_input\" : \"\",");
    	wfBuffer.append("	   \"toReGo\" : \"\",");
    	wfBuffer.append("	   \"processId\" : \"\",");
    	wfBuffer.append("	   \"caseId\" : \"-1\",");
    	wfBuffer.append("	   \"subObjectId\" : \"-1\",");
    	wfBuffer.append("	   \"currentNodeId\" : \"start\",");
    	wfBuffer.append("	   \"process_message_data\" : \"\",");
    	wfBuffer.append("	   \"processChangeMessage\" : \"\",");
    	wfBuffer.append("	   \"process_event\" : \"\"");
    	wfBuffer.append("	}");
    	
    	//组装参数_json_params
        StringBuilder buffer = new StringBuilder();
    	buffer.append("{");
    	buffer.append("	 \"attFileDomain\" : [],");
    	buffer.append("  \"colMainData\" : {");
    	buffer.append("    \"subject\" : \"" + Strings.toHTML(subject) + "\",");
    	buffer.append("	   \"colArchiveName\" : \"\",");
    	buffer.append("	   \"importantLevel\" : \"1\",");
    	buffer.append("	   \"canTrack\" : \"1\",");
    	buffer.append("	   \"trackType\" : \"1\",");
    	buffer.append("	   \"zdgzry\" : \"\",");
    	buffer.append("	   \"resend\" : \"false\",");
    	buffer.append("	   \"newBusiness\" : \"1\",");
    	buffer.append("    \"id\" : " +summaryId + ",");
    	buffer.append("	   \"prevArchiveId\" : \"\",");
    	buffer.append("	   \"colPigeonhole\" : \"\",");
    	buffer.append("	   \"bodyType\" : \"10\",");
    	buffer.append("	   \"tId\" : \"\",");
    	buffer.append("	   \"formRecordid\" : \"\",");
    	buffer.append("	   \"formParentid\" : \"\",");
    	buffer.append("	   \"formViewOperation\" : \"\",");
    	buffer.append("	   \"formAppid\" : \"\",");
    	buffer.append("	   \"DR\" : \"\",");
    	buffer.append("	   \"contentTemplateId\" : \"\",");
    	buffer.append("	   \"contentRightId\" : \"\",");
    	buffer.append("	   \"contentDataId\" : \"\",");
    	buffer.append("	   \"contentSaveId\" : \"" + contentSaveId + "\",");
    	buffer.append("	   \"contentZWID\" : \"" + contentSaveId + "\",");
    	buffer.append("	   \"canForward\" : \"1\",");
    	buffer.append("	   \"canModify\" : \"1\",");
    	buffer.append("	   \"canEdit\" : \"1\",");
    	buffer.append("	   \"canEditAttachment\" : \"1\",");
    	buffer.append("	   \"canArchive\" : \"1\",");
    	buffer.append("	   \"canScanCode\" : \"0\",");
    	buffer.append("	   \"canSetSupervise\" : \"1\",");
    	buffer.append("	   \"projectId\" : \"\",");
    	buffer.append("	   \"archiveId\" : \"\",");
    	buffer.append("	   \"deadline\" : \"\",");
    	buffer.append("	   \"deadlineDatetime\" : \"\",");
    	buffer.append("	   \"awakeDate\" : \"\",");
    	buffer.append("	   \"advanceRemind\" : \"\",");
    	buffer.append("	   \"advancePigeonhole\" : \"\",");
    	buffer.append("	   \"archiveName\" : \"\",");
    	buffer.append("	   \"archiveAllName\" : \"\",");
    	buffer.append("	   \"canMergeDeal\" : \"0\",");
    	buffer.append("	   \"canAnyMerge\" : \"0\",");
    	buffer.append("	   \"attachmentArchiveId\" : \"\",");
    	buffer.append("	   \"caseId\" : \"\",");
    	buffer.append("	   \"currentaffairId\" : \"\",");
    	buffer.append("	   \"oldProcessId\" : \"\",");
    	buffer.append("	   \"isTemplateHasPigeonholePath\" : \"false\"");
    	buffer.append("  },");
    	
    	//流程参数
    	buffer.append(wfBuffer.toString());
    	
    	buffer.append("  }");
    	
    	return buffer.toString();
    }
    
    private void saveRobotContent(Long summaryId, String subject, String content, Long currentUserId) throws BusinessException {
    	//组装正文参数
        StringBuilder _currentDivBuffer = new StringBuilder();
    	_currentDivBuffer.append("\"_currentDiv\" : {");
    	_currentDivBuffer.append("	\"_currentDiv\" : \"0\"");
    	_currentDivBuffer.append("}");

        StringBuilder mainbodyDataBuffer = new StringBuilder();
    	mainbodyDataBuffer.append("	\"mainbodyDataDiv_0\" : {");
    	mainbodyDataBuffer.append("	\"modifyDate\" : \"\",");
    	mainbodyDataBuffer.append("	\"moduleType\" : \"1\",");
    	mainbodyDataBuffer.append("	\"contentTemplateId\" : \"0\",");
    	mainbodyDataBuffer.append("	\"modifyId\" : \"\",");
    	mainbodyDataBuffer.append("	\"sort\" : \"0\",");
    	mainbodyDataBuffer.append("	\"title\" : \"" + Strings.toHTML(subject) + "\",");
    	mainbodyDataBuffer.append("	\"content\" : \"" + Strings.toHTML(content) + "\",");
    	mainbodyDataBuffer.append("	\"createId\" : \"" + currentUserId + "\",");
    	mainbodyDataBuffer.append("	\"contentDataId\" : \"\",");
    	mainbodyDataBuffer.append("	\"rightId\" : \"\",");
    	mainbodyDataBuffer.append("	\"viewState\" : \"1\",");
    	mainbodyDataBuffer.append("	\"moduleTemplateId\" : \"0\",");
    	mainbodyDataBuffer.append("	\"id\" : \"\",");
    	mainbodyDataBuffer.append("	\"moduleId\" : \"" + summaryId + "\",");
    	mainbodyDataBuffer.append("	\"contentType\" : \"10\",");
    	mainbodyDataBuffer.append("	\"createDate\" : \"\",");
    	mainbodyDataBuffer.append("	\"status\" : \"STATUS_RESPONSE_NEW\"");
    	mainbodyDataBuffer.append("}");

        StringBuilder contentBuffer = new StringBuilder();
    	contentBuffer.append("{");
    	contentBuffer.append(_currentDivBuffer.toString());
    	contentBuffer.append(",");
    	contentBuffer.append(mainbodyDataBuffer.toString());
    	contentBuffer.append("}");
    	
    	Map<String, Object> params = new HashMap<String, Object>();
    	
    	params.put("_currentDiv", _currentDivBuffer.toString());
    	params.put("mainbodyDataDiv_0", mainbodyDataBuffer.toString());
    	params.put(JSON_PARAMS, contentBuffer.toString());
    	
        try {
        	CtpContentAllBean contentAll = convertToContentAll(params);
            // 保存
            ctpMainbodyManager.transContentSaveOrUpdate(contentAll);
        } finally {
        	removeThreadContext();
        }
    }
    /********************************** 语音机器人 end ******************************************/
    
    
    /**
     *  将 _json_params 设置到 线程中
     * 
     * @param jsonStr
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月6日下午7:35:16
     *
     */
    private void putThreadContext(String jsonStr){
        
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY, jsonStr);
        
        Object jsonObj = JSONUtil.parseJSONString(Strings.removeEmoji(jsonStr), Map.class);
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY, jsonObj);
    }
    
    /**
     * 清理缓存数据
     * 
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月5日下午4:05:17
     *
     */
    private void removeThreadContext(){
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY);
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY);
    }

    /**
     * 是否有归档权限
     * 
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年5月20日上午10:36:54
     *
     */
    private boolean canArchive(User user){
        
        boolean ret = false;
        
        ret = AppContext.hasPlugin(ApplicationCategoryEnum.doc.name());
        
        if(ret){
            boolean hasResource = false;
            String[] docCodes = new String[]{"F04_docIndex", "F04_myDocLibIndex", "F04_accDocLibIndex",
                                 "F04_proDocLibIndex", "F04_eDocLibIndex", "F04_docLibsConfig"};
            
            for(int i = 0, size = docCodes.length; i < size && !hasResource; i++){
                hasResource = user.hasResourceCode(docCodes[i]);
            }
            ret = hasResource;
        }
        
        return ret;
    }
    
    /**
     * 随机获取人员在协同里面对应的affair
     * @Author      : xuqw
     * @Date        : 2016年2月19日下午4:32:55
     * @param summaryId
     * @param _isHistoryFlag
     * @param memberId
     * @return
     * @throws BusinessException
     */
    private CtpAffair findMemberAffair(Long summaryId, boolean _isHistoryFlag, Long memberId)
                                                           throws BusinessException{
        
        CtpAffair ret = null;
        
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (_isHistoryFlag) {
            affairs = affairManager.getAffairsHis(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        } else {
            affairs = affairManager.getAffairs(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        }
        if (Strings.isNotEmpty(affairs)) {
            for (CtpAffair aff : affairs) {
                if (!aff.isDelete()) {
                    ret = aff;
                    break;
                }
            }
        }
        return ret;
    }
    
    
    /**
     * 是否是M3登录
     * 
     * @return
     *
     * @Since A8-V5 7.1SP1
     * @Author      : xuqw
     * @Date        : 2019年5月21日下午3:08:51
     *
     */
    private boolean isLoginFromM3() {
        
        String loginFrom = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
        
        return Constants.login_sign.phone.toString().equals(loginFrom);
    }

    
    @GET
    @Path("getSenderComments/{summaryId}")
    public Response getSenderComments(@PathParam("summaryId") Long summaryId) throws BusinessException {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	//发起人附言, 不分页
        List<ColSummaryCommentVO> senderComments =  summarySenderComments(summaryId, false);
        
        map.put("senderCommonts", senderComments);
        
        return ok(map);
	}
    
    @POST
    @Path("cacheUserCondition2Report")
    public Response cacheUserCondition2Report(Map<String,Object> params) throws BusinessException {
    	Map<String, String> resultMap = new HashMap<String, String>();
    	Long drPoId = ParamUtil.getLong(params, "drPoId");
    	Long affairId = ParamUtil.getLong(params, "affairId");
        String recordStrId = ParamUtil.getString(params, "recordId");
        String dataTypeName = ParamUtil.getString(params, "dataTypeName");
        Long recordId = null;
        if(Strings.isNotBlank(recordStrId)){
        	recordId = Long.parseLong(recordStrId);
        }
    	String pageCondJSON = ParamUtil.getString(params, "pageCondJSON");
    	Pair<? extends List<Map<String, Object>>,String> formConditionPair = dataRelationApi.findFormMoreCond(drPoId, affairId, recordId, dataTypeName, pageCondJSON);
    	List<Map<String, Object>> userConditionList = formConditionPair.getLeft();
    	resultMap.put("category", formConditionPair.getRight());
    	
    	//将相关数据的查询条件缓存到报表,根据conditionId报表更多页面取数据回填
		String conditionId = reportResultApi.setUserCondition(UserConModel.ADVANCE,userConditionList).toString();
		resultMap.put("conditionId", conditionId);
		
    	return ok(resultMap);
    }
    

    /** 
     * <pre>
     *      获取协同中所有的图片附件
     *     
     * @param summaryId 协同ID
     * @return      
     * @return: Response  
     * @date:   2019年6月13日 
     * @author: yaodj
     * @since   v7.1 sp1	
     * </pre> 
     */ 
    @GET
    @Path("findAttachmentImgList/{summaryId}")
    public Response findAttachmentImgList(@PathParam("summaryId") Long summaryId) {
        
        List<Attachment> attachmentsImgs = new ArrayList<Attachment>();
        
        try {
            List<Attachment> attachments = attachmentManager.getByReference(summaryId);
            
            if(attachments != null) {
                for (Attachment attachment : attachments) {
                    String mimeType = attachment.getMimeType();
                    if(mimeType != null && mimeType.startsWith("image")) {
                        attachmentsImgs.add(attachment);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取协同中所有的图片附件失败！", e);
            return success(attachmentsImgs, ResourceUtil.getString("coll.query.error"),  // 数据查询错误
                                  Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        
        return success(attachmentsImgs,"", Response.Status.OK.getStatusCode()); 
    }
}

