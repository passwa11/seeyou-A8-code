/**
 * $Author: wangwy $
 * $Rev: 17968 $
 * $Date:: 2015-08-06 11:00:09#$:
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.login.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.seeyon.apps.agent.utils.AgentUtil;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.eip.manager.EipApi;
import com.seeyon.apps.ext.oauthLogin.manager.oauthLoginManager;
import com.seeyon.apps.ext.oauthLogin.manager.oauthLoginManagerImpl;
import com.seeyon.apps.ext.oauthLogin.po.LoginRecord;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.apps.ldap.config.LDAPConfig;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.apps.m3.api.M3Api;
import com.seeyon.apps.taskmanage.util.ProductEditionUtil;
import com.seeyon.apps.uc.api.UcApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ServerState;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.SystemInitializer;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.LockLoginInfoFactory;
import com.seeyon.ctp.common.authenticate.domain.IdentificationDog;
import com.seeyon.ctp.common.authenticate.domain.IdentificationDogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.domain.UserHelper;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.Constants.login_sign;
import com.seeyon.ctp.common.constants.Constants.login_useragent_from;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.LoginConstants;
import com.seeyon.ctp.common.constants.LoginResult;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.constants.ProductVersionEnum;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.exceptions.InfrastructureException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.BrowserEnum;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.i18n.domain.ResourceInfo;
import com.seeyon.ctp.common.i18n.manager.I18nResourceCacheHolder;
import com.seeyon.ctp.common.i18n.util.I18nUtil;
import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.ctp.common.po.applog.AppLog;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.web.filter.CTPRequestFacade;
import com.seeyon.ctp.common.web.filter.UserLastOperationRecorder;
import com.seeyon.ctp.common.web.filter.UserLastOperationRecorder.Operation;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.dubbo.RefreshInterfacesAfterUpdate;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.login.HomePageParamsInterface;
import com.seeyon.ctp.login.LoginActiveX;
import com.seeyon.ctp.login.LoginAuthentication;
import com.seeyon.ctp.login.LoginAuthenticationException;
import com.seeyon.ctp.login.LoginControl;
import com.seeyon.ctp.login.bo.MenuBO;
import com.seeyon.ctp.login.logonlog.manager.LogonLogManager;
import com.seeyon.ctp.login.manager.UcLoginSecurityManager;
import com.seeyon.ctp.login.online.OnlineManager;
import com.seeyon.ctp.login.online.OnlineRecorder;
import com.seeyon.ctp.login.online.OnlineUser;
import com.seeyon.ctp.login.online.OnlineUser.LoginInfo;
import com.seeyon.ctp.login.po.LogonLog;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgDao;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.GuestManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.engine.model.VPortalObj;
import com.seeyon.ctp.portal.expansion.ExpandJspForHomePage;
import com.seeyon.ctp.portal.nav.bo.PortalNavBo;
import com.seeyon.ctp.portal.po.PortalHotspot;
import com.seeyon.ctp.portal.po.PortalIconStyle;
import com.seeyon.ctp.portal.po.PortalLoginTemplate;
import com.seeyon.ctp.portal.po.PortalSet;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.po.PortalTemplate;
import com.seeyon.ctp.portal.po.PortalTemplateSetting;
import com.seeyon.ctp.portal.space.bo.SpaceBO;
import com.seeyon.ctp.privilege.manager.PrivilegeMenuManager;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.Cookies;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.LeaveLockDecode;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.mobile.login.manager.MobileLoginManager;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;
import com.seeyon.v3x.onlineCustomerService.manager.OnlineCustomerServiceManager;
import com.seeyon.v3x.onlineCustomerService.util.OnlineCustomerServiceUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.seeyon.ctp.system.Constants.V5CLOUD_HELP_URL;
import static com.seeyon.ctp.system.Constants.V5CLOUD_LOGIN_SERVICE_URL;

/**
 * <p>Title: T1开发框架</p>
 * <p>Description: 登陆页展现、登陆、首页展现等处理Controller。</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 *
 * @since CTP2.0
 */
public class MainController extends BaseController implements SystemInitializer {
    private static final Log log = LogFactory.getLog(MainController.class);
    private static final int expiry = 60 * 60 * 24 * 1;
    private static final int expiry10year = 10 * 365 * 24 * 60 * 60;
    private SystemConfig systemConfig;
    private LoginControl loginControl;
    private ConfigManager configManager;

    private final String DefaultFramePage = "/frame.jsp";
    private final String DefaultLoginPage = "/login.jsp";
    private static String appDefaultPath = "/indexOpenWindow.jsp";
    private Map<Long, Integer> attemptAuthenticateCounter = new ConcurrentHashMap<Long, Integer>();

    private String cframePage;
    private String cloginPage;
    private PortalApi portalApi;
    private OnlineManager onlineManager;
    private MobileMessageManager mobileMessageManager;
    private UcApi ucApi;
    private CustomizeManager customizeManager;
    private EnumManager enumManagerNew;
    private UserMessageManager userMessageManager;
    private OrgManager orgManager;
    private PrincipalManager principalManager;
    private OrgDao orgDao;
    private EipApi eipApi;
    private LogonLogManager logonLogManager;
    private AppLogManager appLogManager;
    private GuestManager guestManager;
    private M3Api m3Api;
    private EdocApi edocApi;
    private OnlineCustomerServiceManager onlineCustomerServiceManager;
    private PrivilegeMenuManager privilegeMenuManager;
    private OrgManagerDirect orgManagerDirect;
    private FormApi4Cap3 formApi4Cap3;
    private MobileLoginManager mobileLoginManager;
    private UcLoginSecurityManager ucLoginSecurityManager;

    public UcLoginSecurityManager getUcLoginSecurityManager() {
        return ucLoginSecurityManager;
    }

    public void setUcLoginSecurityManager(UcLoginSecurityManager ucLoginSecurityManager) {
        this.ucLoginSecurityManager = ucLoginSecurityManager;
    }

    public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

    public void setM3Api(M3Api m3Api) {
        this.m3Api = m3Api;
    }

    public EdocApi getEdocApi() {
        return edocApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public void setMobileLoginManager(MobileLoginManager mobileLoginManager) {
        this.mobileLoginManager = mobileLoginManager;
    }

    private String mobileLoginType = null;

    private String getMobileLoginType() {
        if (Strings.isBlank(mobileLoginType)) {
            mobileLoginType = (String) MclclzUtil.invoke(c2, "getMxProductLine", null, null, null);
        }
        return mobileLoginType;
    }

    public EipApi getEipApi() {
        return eipApi;
    }

    public void setEipApi(EipApi eipApi) {
        this.eipApi = eipApi;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public void setOnlineManager(OnlineManager onlineManager) {
        this.onlineManager = onlineManager;
    }

    public void setLoginControl(LoginControl loginControl) {
        this.loginControl = loginControl;
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
        this.mobileMessageManager = mobileMessageManager;
    }

    public void setUcApi(UcApi ucApi) {
        this.ucApi = ucApi;
    }

    public EnumManager getEnumManagerNew() {
        return enumManagerNew;
    }

    public void setEnumManagerNew(EnumManager enumManagerNew) {
        this.enumManagerNew = enumManagerNew;
    }

    public CustomizeManager getCustomizeManager() {
        return customizeManager;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setPrincipalManager(PrincipalManager principalManager) {
        this.principalManager = principalManager;
    }

    public void setOrgDao(OrgDao orgDao) {
        this.orgDao = orgDao;
    }

    public void setLogonLogManager(LogonLogManager logonLogManager) {
        this.logonLogManager = logonLogManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public OnlineCustomerServiceManager getOnlineCustomerServiceManager() {
        return onlineCustomerServiceManager;
    }

    public void setOnlineCustomerServiceManager(OnlineCustomerServiceManager onlineCustomerServiceManager) {
        this.onlineCustomerServiceManager = onlineCustomerServiceManager;
    }

    public void setPrivilegeMenuManager(PrivilegeMenuManager privilegeMenuManager) {
        this.privilegeMenuManager = privilegeMenuManager;
    }

    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }

    //不要做集群
    private List<HomePageParamsInterface> HomePageParamsInterfaces = new ArrayList<HomePageParamsInterface>();
    private List<ExpandJspForHomePage> expansionJspForHomepage = new ArrayList<ExpandJspForHomePage>();

    @Override
    public void initialize() {
        //增加外部模块需要在首页增加参数控制的接口
        fillHomePageParamsInterface();

        //增加外部模块需要在首页增加jsp页面引用的接口
        fillExpandJspForHomePage();

        log.info("初始化外部模块需要在首页增加参数控制、JSP页面引用的接口");
    }

    @RefreshInterfacesAfterUpdate(inface = HomePageParamsInterface.class)
    public void fillHomePageParamsInterface() {
        Map<String, HomePageParamsInterface> paramsCreaters = AppContext.getBeansOfType(HomePageParamsInterface.class);
        if (paramsCreaters != null && paramsCreaters.size() > 0) {
            for (Map.Entry<String, HomePageParamsInterface> entry : paramsCreaters.entrySet()) {
                HomePageParamsInterface paramsCreater = entry.getValue();

                HomePageParamsInterfaces.add(paramsCreater);
            }
        }
    }

    @RefreshInterfacesAfterUpdate(inface = ExpandJspForHomePage.class)
    public void fillExpandJspForHomePage() {
        Map<String, ExpandJspForHomePage> expansionJspCreaters = AppContext.getBeansOfType(ExpandJspForHomePage.class);
        if (expansionJspCreaters != null && expansionJspCreaters.size() > 0) {
            for (Map.Entry<String, ExpandJspForHomePage> entry : expansionJspCreaters.entrySet()) {
                ExpandJspForHomePage expansionJspCreater = entry.getValue();
                expansionJspForHomepage.add(expansionJspCreater);
            }
        }
    }

    @NeedlessCheckLogin
    public ModelAndView qrCodeHelp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/portal/QRCodeHelp");
        return mav;
    }

    public void loginSystem(HttpServletRequest request, HttpServletResponse response, String guestLoginName) throws BusinessException {
        Long loginTime = System.currentTimeMillis();
        login_useragent_from userAgentFrom = login_useragent_from.pc;
        Locale locale = LocaleContext.make4Frontpage(request);
        HttpSession session = request.getSession();

        V3xOrgMember member = orgManager.getMemberByLoginName(guestLoginName);
        if (member == null) {
            throw new BusinessException("特殊账号不存在：" + guestLoginName);
        }
        User currentUser = new User();
        LocaleContext.setLocale(session, orgManagerDirect.getMemberLocaleById(member.getId()));

        currentUser = new User();
        currentUser.setLoginTimestamp(loginTime);
        currentUser.setSecurityKey(UUIDLong.longUUID());

        currentUser.setId(member.getId());
        currentUser.setName(member.getName());
        currentUser.setLoginName(member.getLoginName());
        currentUser.setAccountId(member.getOrgAccountId());
        currentUser.setLoginAccount(member.getOrgAccountId());
        currentUser.setDepartmentId(member.getOrgDepartmentId());
        currentUser.setLevelId(member.getOrgLevelId());
        currentUser.setPostId(member.getOrgPostId());
        currentUser.setInternal(member.getIsInternal());
        currentUser.setExternalType(member.getExternalType());

        currentUser.setUserAgentFrom(userAgentFrom.name());
        currentUser.setSessionId(session.getId());
        currentUser.setRemoteAddr(Strings.getRemoteAddr(request));
        currentUser.setLocale(locale);
        // 判断客户端浏览器
        BrowserEnum browser = BrowserEnum.valueOf(request);
        if (browser == null) {
            browser = BrowserEnum.IE;
        }
        currentUser.setBrowser(browser);

        session.setAttribute(Constants.SESSION_CURRENT_USER, currentUser);
        session.setMaxInactiveInterval(-1);//登录前guest账号session永不超期
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, currentUser);
        AppContext.initSystemEnvironmentContext(request, response, true);

        UserHelper.setResourceJsonStr(JSONUtil.toJSONString(privilegeMenuManager.getResourceCode(currentUser.getId(), currentUser.getLoginAccount())));
        CurrentUser.set(currentUser);
    }

    /**
     * 用户密码校验，为登录后锁屏提供，校验当前已登录用户的认证信息是否正确。
     *
     * @param request
     * @param response
     */
    public void authenticate(HttpServletRequest request, HttpServletResponse response) {
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        Integer failCount = attemptAuthenticateCounter.get(userId);
        if (failCount != null) {
            int max = 5;
            try {
                max = Integer.parseInt(systemConfig.get(IConfigPublicKey.LEAVE_LOCK_FAIL_COUNT));
            } catch (NumberFormatException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            if (failCount >= max) {
                Map result = new HashMap();
                result.put("success", false);
                result.put("logout", true);
                outputJson(response, result);
            }
        }
        boolean dogSuccess = false;
        String msg = "";
        final String dogId = request.getParameter("dogSessionId");
        IdentificationDogManager dogManager = IdentificationDogManager.getInstance();
        if (Strings.isNotBlank(dogId)) {
            IdentificationDog dog = dogManager.getDogByEncodeId(dogId);
            if (dog != null) {
                if (AppContext.currentUserId() == dog.getMemberId()) {
                    if (dog.isNeedCheckUsername()) {
                        String pwd = request.getParameter(LoginConstants.PASSWORD);
                        //解密
                        if (Strings.isNotBlank(pwd)) {
                            pwd = LeaveLockDecode.atob(pwd);
                        }
                        dogSuccess = principalManager.authenticate(user.getLoginName(), pwd);
                    } else {
                        dogSuccess = true;
                    }
                }
            }
        }
        boolean success = false;
        if (dogSuccess) {
            success = true;
        } else {
            msg = ResourceUtil.getString("login.label.ErrorCode.18");
            HttpServletRequest req = new CTPRequestFacade(request) {
                @Override
                public String getParameter(String paramName) {
                    // 不接受前端传入到的登录名，使用当前用户
                    if (paramName.equals(LoginConstants.USERNAME)) {
                        return AppContext.currentUserLoginName();
                    }
                    if (paramName.equals(LoginConstants.PASSWORD)) {
                        String pass = super.getParameter(paramName);
                        if (Strings.isNotBlank(pass)) {
                            pass = LeaveLockDecode.atob(pass);
                        }
                        return pass;
                    }
                    return super.getParameter(paramName);
                }
            };
            if (dogManager.isMustUseDogLogin(user.getLoginName())) {
                success = false;
                if (dogId != null && dogId.indexOf("Err") > -1) {
                    msg = ResourceUtil.getString("login.label.ErrorCode.19");
                }
            } else {
                for (Map.Entry<String, LoginAuthentication> entry : loginControl.getLoginAuthentications().entrySet()) {
                    LoginAuthentication loginAuthentication = entry.getValue();
                    try {
                        String[] _success = loginAuthentication.authenticate(req, response);
                        if (_success != null && _success.length > 1) {
                            success = true;
                            break;
                        }
                    } catch (LoginAuthenticationException e) {// 终止本次登录
                        //	                loginResultCheck(e.getLoginReslut());
                    } catch (Exception e) {
                        //	                loginResultCheck(LoginResult.ERROR_UNKNOWN_USER);
                    }
                }
            }
        }
        if (success) {
            attemptAuthenticateCounter.remove(userId);
            UserLastOperationRecorder.getInstance(Operation.LAST_ACCESS).op(userId);
        } else {
            if (failCount == null) {
                failCount = 0;
            }
            failCount++;
            attemptAuthenticateCounter.put(userId, failCount);
        }
        Map result = new HashMap();
        result.put("success", success);
        result.put("message", msg);
        outputJson(response, result);
    }

    private void outputJson(HttpServletResponse response, Object result) {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.write(JSONUtil.toJSONString(result));
            out.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 获取加密种子使用（防止用户在登录页停留时间长，session失效，加密种子失效）
     * 造成用户密码输入正确但提示有误的情况
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @NeedlessCheckLogin
    public void updateLoginSeed(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(true);
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);
        //Session线程变量更新
        String seed = SecurityHelper.getSessionContextSeed();
        super.rendText(response, seed);
    }

    /**
     * 登陆页显示
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unused"})
    @NeedlessCheckLogin
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!SystemEnvironment.isSuitDeployMode()) {// 非套装，退出到协同云登录页面
            toV5CLoudLoginPage(response, null);
            return null;
        }
        try {
            if (formApi4Cap3.isUpgrading()) {//表单升级中
                AppContext.putSessionContext(LoginConstants.Result, ResourceUtil.getString("login.label.ErrorCode.50"));
            } else if (!formApi4Cap3.isUpgradedV5()) {
                AppContext.putSessionContext(LoginConstants.Result, ResourceUtil.getString("login.label.ErrorCode.51"));
            }

            //V7.1 企业公文升级到政务公文
            String versionStr = ProductEditionEnum.getCurrentProductEditionEnum().getValue();
            if (formApi4Cap3.isUpgradedV5() && formApi4Cap3.isUpgradedV5() && edocApi != null
                    && !versionStr.equals(ProductEditionEnum.government.getValue())
                    && !versionStr.equals(ProductEditionEnum.governmentgroup.getValue())) {
                int upgradeCode = edocApi.isV5EdocUpgrade();
                if (upgradeCode != 2 && upgradeCode != 5) {
                    edocApi.setUpdatePageLocale();
                    ModelAndView view = new ModelAndView("edoc/upgrade/edocTo71Upgrade");
                    view.addObject("govdocUpgrad", upgradeCode);
                    if (edocApi.noFileExists()) {
                        view.addObject("noFiles", "1");
                    }
                    return view;
                }
            }

        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage());
        }
        String loginFrom = request.getParameter("loginFrom");
        if (AppContext.getRawSession() == null) {
            AppContext.initSystemEnvironmentContext(request, response, true);
            ;
        }
        User currentUser = AppContext.getCurrentUser();
        Map<String, Object> festivalLoginPageFileIdsMap = portalApi.getFestivalLoginPageFileIds();//特殊日期登录页设置
        boolean toLoginPage = false;
        List<String> festivalLoginPageFileIds = null;
        if (MapUtils.isNotEmpty(festivalLoginPageFileIdsMap)) {
            toLoginPage = MapUtils.getBoolean(festivalLoginPageFileIdsMap, "festivalLoginPageFlag", false);
            Object festivalLoginPageFileIdsObj = festivalLoginPageFileIdsMap.get("fileIdList");
            if (festivalLoginPageFileIdsObj != null && festivalLoginPageFileIdsObj instanceof List) {
                festivalLoginPageFileIds = (List<String>) festivalLoginPageFileIdsObj;
            }
        }
        if ((Strings.isBlank(loginFrom) || !"loginPortal".equals(loginFrom)) && (null == currentUser || currentUser.isDefaultGuest())) {
            //不是来自登录栏目请求，才判断下是否开启登录前Guest账号，如果开启，则自动登录Guest账号，并跳转到登录前门户
            boolean isLoginGuestOpened = guestManager.isGuestEnable();
            boolean isLoginPortalOpened = portalApi.isLoginPortalOpened();
            if (isLoginGuestOpened && isLoginPortalOpened && AppContext.hasPlugin("vPortalAdvanced") && !toLoginPage) {//开启登录前Guest账号，且登录前门户处于启动状态
                //用登录前Guest账号登录系统
                String guestLoginName = guestManager.getDefaultLoginName();
                this.loginSystem(request, response, guestLoginName);
                //跳转到portal首页
                String url = "/main.do?method=main&loginPortal=1&portalId=" + com.seeyon.ctp.portal.util.Constants.preLoginPortalId;
                return super.redirectModelAndView(url);
            }
        }
        if (cloginPage == null) {
            String loginPage = systemConfig.get("login_page");
            if (loginPage != null)
                cloginPage = loginPage;
            else
                cloginPage = DefaultLoginPage;
        }
        String selectedPath = null;
        PortalTemplateSetting setting = (PortalTemplateSetting) request.getAttribute("PortalLoginTemplateSetting");
        Long loginAccountId = (Long) request.getAttribute("loginAccountId");
        List<PortalHotspot> hotSpots = null;
        if (setting == null) {
            setting = portalApi.getLoginSettingBy(OrgConstants.GROUPID.longValue(), OrgConstants.GROUPID.longValue());
            //setting = portalTemplateSettingManager.getLoginSettingBy(OrgConstants.GROUPID.longValue(), OrgConstants.GROUPID.longValue());
            //hotSpots = portalHotSpotManager.getHotSpotsBy(setting.getTemplateId(), null, OrgConstants.GROUPID.longValue(), OrgConstants.GROUPID.longValue());
            hotSpots = portalApi.getHotSpotsBy(setting.getTemplateId(), null, OrgConstants.GROUPID.longValue(), OrgConstants.GROUPID.longValue());
        } else {
            //hotSpots = portalHotSpotManager.getHotSpotsBy(setting.getTemplateId(), null, loginAccountId, loginAccountId);
            hotSpots = portalApi.getHotSpotsBy(setting.getTemplateId(), null, loginAccountId, loginAccountId);
        }
        if (CollectionUtils.isNotEmpty(festivalLoginPageFileIds) && CollectionUtils.isNotEmpty(hotSpots)) {
            //用户设置了有效的特殊日期登录页，需要修改热点值
            List<PortalHotspot> festivalHotSpotList = new ArrayList<>(hotSpots.size());
            for (PortalHotspot hotSpot : hotSpots) {
                String hotspotkey = hotSpot.getHotspotkey();
                if ("changebgi".equals(hotspotkey)) {
                    //需要把这个给替换成特殊节日的
                    PortalHotspot festivalHostSpot = BeanUtils.clone(hotSpot);
                    StringBuffer hotValuebuffer = new StringBuffer();
                    StringBuffer ext3Buffer = new StringBuffer();
                    for (String fileId : festivalLoginPageFileIds) {
                        hotValuebuffer.append("fileId=").append(fileId).append(",");
                        ext3Buffer.append("1,");
                    }
                    String hotValue = new String(hotValuebuffer);
                    String ext3 = new String(ext3Buffer);
                    if (hotValue.length() > 1) {
                        hotValue = hotValue.substring(0, hotValue.length() - 1);
                        ext3 = ext3.substring(0, ext3.length() - 1);
                    }
                    festivalHostSpot.setHotspotvalue(hotValue);
                    festivalHostSpot.setExt3(ext3);
                    festivalHotSpotList.add(festivalHostSpot);
                } else if ("changebgtype".equals(hotspotkey)) {
                    PortalHotspot festivalHostSpot = BeanUtils.clone(hotSpot);
                    String hotspotvalue = festivalHostSpot.getHotspotvalue();
                    if ("video".equals(hotspotvalue)) {
                        festivalHostSpot.setHotspotvalue("image");
                    }
                    festivalHotSpotList.add(festivalHostSpot);
                } else {
                    festivalHotSpotList.add(hotSpot);
                }
            }
            hotSpots = festivalHotSpotList;

        }
        //PortalLoginTemplate plt = DBAgent.get(PortalLoginTemplate.class, setting.getTemplateId());
        PortalLoginTemplate plt = portalApi.getLoginTemplate(setting.getTemplateId());
        if (plt != null) {
            String path = plt.getPath();
            if (path != null && path.trim().length() > 0) {
                selectedPath = "/main/login/" + path;
            }
        }
        ModelAndView modelAndView = null;
        if (selectedPath != null) {
            modelAndView = new ModelAndView("raw:" + selectedPath);
        } else {
            modelAndView = new ModelAndView("raw:" + cloginPage);
        }
        //二维码,判断登录页二维码显示哪个端的二维码,默认m3
        String mobile = "m3";
        boolean m1 = AppContext.hasPlugin("m1");
        boolean m3 = AppContext.hasPlugin("m3");
        if (m1) {
            mobile = "m1";
        }
        if (m3) {//同时有m3和m1插件的时候显示m3
            mobile = "m3";
            String m3ServerBarCode = m3Api.getServerBarCodeId();
            modelAndView.addObject("m3ServerBarCode", m3ServerBarCode);
        }
        if (m1 && m3) {
            String mx = SystemEnvironment.getMxVersion();
            if (Strings.isNotBlank(mx)) {
                mobile = mx.toLowerCase();
            }
        }
        modelAndView.addObject("mobile", mobile);
        Locale currentLocale = LocaleContext.make4Frontpage(request);
        List localeCode = new ArrayList();
        Map localeCodeCfg = new HashMap();
        localeCode.add(localeCodeCfg);
        localeCodeCfg.put("eleid", LoginConstants.LOCALE);
        localeCodeCfg.put("defaultValue", currentLocale.toString());
        localeCodeCfg.put("options", getLocaleInfo());

        String loginTitleName = Functions.getPageTitle();
        modelAndView.addObject("templatesJsonStr", JSONUtil.toJSONString(plt));
        modelAndView.addObject("hotSpotsJsonStr", JSONUtil.toJSONString(hotSpots));
        String layout = "all";
        if (CollectionUtils.isNotEmpty(hotSpots) && plt.getPreset() == 1) {
            //预置的
            for (PortalHotspot hotspot : hotSpots) {
                if ("note".equals(hotspot.getHotspotkey())) {
                    String noteName = hotspot.getHotspotvalue();
                    if (noteName == null || noteName.trim().length() == 0 || "null".equals(noteName)) {
                        loginTitleName = Functions.getVersion();
                    } else {
                        String suffix = SystemProperties.getInstance().getProperty("portal.loginTitle");
                        if ((Boolean) SysFlag.sys_isGovVer.getFlag()) {
                            //G6的版本国际化比较特殊需要特殊处理一下看看是否是个性化过
                            if (noteName != null && (noteName.equals(ResourceUtil.getString(noteName)))) {
                                loginTitleName = ResourceUtil.getString(noteName) + " " + Functions.getVersion();
                            } else {
                                loginTitleName = ResourceUtil.getString(noteName + suffix) + " " + Functions.getVersion();
                            }
                        } else {
                            loginTitleName = ResourceUtil.getString(noteName + suffix) + " " + Functions.getVersion();
                        }
                    }
                }
                if ("layout".equals(hotspot.getHotspotkey())) {
                    layout = hotspot.getHotspotvalue();
                }
                if ("showQr".equals(hotspot.getHotspotkey())) {
                    String item = "qrInfoUpdate";
                    String cacheDirect = portalApi.getPortalGlobalConfigFromCacheDirect(item, -1L, -1L);
                    boolean isupdate = false;
                    if (cacheDirect == null) {
                        //没有升级过，升级处理一下
                        if (!"1".equals(hotspot.getHotspotvalue())) {
                            portalApi.updateQrInfo(item);
                            isupdate = true;
                        }
                    }
                    if (isupdate) {
                        modelAndView.addObject("showQr", "1".equals(hotspot.getHotspotvalue()));
                    } else {
                        modelAndView.addObject("showQr", true);
                    }
                }
                if ("QrInfo".equals(hotspot.getHotspotkey())) {
                    modelAndView.addObject("QrInfo", hotspot.getHotspotvalue());
                }
                if ("shownumber".equals(hotspot.getHotspotkey())) {
                    modelAndView.addObject("shownumber", !"hide".equals(hotspot.getHotspotvalue()));
                }
            }
        }
        Map<String, String> bgImgSize = portalApi.getCurrentLoginImgSize(hotSpots);
        modelAndView.addObject("bgImgSize", JSONUtil.toJSONString(bgImgSize));
        modelAndView.addObject("layout", layout);
        boolean domestic = SystemEnvironment.isDomestic();//是否为国产化环境
        modelAndView.addObject("isNotDomestic", !domestic);
        Map<String, LoginActiveX> activeXMap = loginControl.getLoginActiveXes();
        StringBuilder activeXLoader = new StringBuilder();
        if (activeXMap != null) {
            Iterator<String> ite = activeXMap.keySet().iterator();
            String key;
            while (ite.hasNext()) {
                key = ite.next();
                LoginActiveX loginActiveX = activeXMap.get(key);
                String activeX = loginActiveX.getActiveX(request, response);
                activeXLoader.append(activeX);
            }
        }

        modelAndView.addObject("currentLocale", currentLocale);
        modelAndView.addObject("locales", JSONUtil.toJSONString(localeCode));
//        modelAndView.addObject("locales", JSONMapper.toJSON(localeCode).render(false));
        //modelAndView.addObject("loginImgFileName", MainDataLoader.getInstance().getLoginImagePath());
        modelAndView.addObject("loginTitleName", loginTitleName);
        modelAndView.addObject("productCategory", ProductEditionEnum.getCurrentProductEditionEnum().getName());
        //并发数
        this.setProductInfo(modelAndView);
        modelAndView.addObject("ServerState", ServerState.getInstance().isShutdown());
        modelAndView.addObject("ServerStateComment", Strings.toHTML(ServerState.getInstance().getComment()));
        if (onlineManager == null) {
            modelAndView.addObject("OnlineNumber", "...");
        } else {
            modelAndView.addObject("OnlineNumber", this.onlineManager.getOnlineNumber());
        }
        boolean verrifyCodeEnabled = "enable".equals(this.systemConfig.get("verify_code"));
//        if(!verrifyCodeEnabled) {
//        	HttpSession session = request.getSession(false);
//        	if(session!=null && "true".equals(session.getAttribute("VERIFYCODE_ENABLED"))) {
//        		verrifyCodeEnabled = true;
//        	}
//        }
        modelAndView.addObject("verifyCode", verrifyCodeEnabled);
        modelAndView.addObject("activeXLoader", activeXLoader.toString());
        if (LDAPConfig.getInstance().getIsEnableLdap()
                && request.getServerName().equalsIgnoreCase(LDAPConfig.getInstance().getA8ServerDomainName())) {
            String adssoToken = request.getHeader("authorization");
            if (adssoToken == null) {
                modelAndView.addObject("adSSOEnable", true);
            } else {
                //             modelAndView.addObject("adLoginName",ADSSOEvent.getInstance().getADLoginName(adssoToken));
                modelAndView.addObject("authorization", adssoToken);
            }
        }
        loadCAPlugIn(request, modelAndView);
        //modelAndView.addObject("hasPluginCA", true);
        String exceptPlugin = "";
        String ucServerIpOrPort = "NULL/NULL/5222";
        if (!AppContext.hasPlugin("videoconference")) {
            exceptPlugin += "@videoconf";
        }
        if (!AppContext.hasPlugin("https")) {
            exceptPlugin += "@seeyonRootCA";
        }
        if (!AppContext.hasPlugin("identification")) {
            exceptPlugin += "@identificationDog";
        }
        if (!AppContext.hasPlugin("officeOcx")) {
            exceptPlugin += "@officeOcx";
        }
        if (!AppContext.hasPlugin("barCode")) {
            exceptPlugin += "@erweima";
        }
        if (!AppContext.hasPlugin("u8")) {
            exceptPlugin += "@U8Reg";
        }
        exceptPlugin += "@wizard";

        //有zx插件才显示
        if (!AppContext.hasPlugin("zx")) {
            exceptPlugin += "@zhixin";
        }

        modelAndView.addObject("ucServerIpOrPort", ucServerIpOrPort);
        modelAndView.addObject("exceptPlugin", exceptPlugin);
        // 判断是否能发送手机短信
        boolean isCanUseSMS = false;
//        if (AppContext.hasPlugin("sms")) {
        if (mobileMessageManager.isCanUseSMS()) {
            isCanUseSMS = true;
        }
//        }
        modelAndView.addObject("isCanUseSMS", isCanUseSMS);

        //判断系统配置是否启用口令加密传输，启用则生成加密种子传到页面
        if (SecurityHelper.isCryptPassword()) {
            modelAndView.addObject("_SecuritySeed", SecurityHelper.getSessionContextSeed());
            HttpSession currentSession = request.getSession(false);
            if (currentSession != null) {
                int sessionTimeOutInSecond = currentSession.getMaxInactiveInterval();//获取Session的失效时间
                modelAndView.addObject("_SecuritySeedTimeOut", sessionTimeOutInSecond);
            }
        }
        //产品服务到期,产品授权到期
        ConfigItem dueRemind = configManager.getConfigItem("login_page", "dueRemind");
        modelAndView.addObject("dueRemindV", dueRemind == null ? "1" : dueRemind.getConfigValue());
        if (dueRemind == null || "1".equals(dueRemind.getConfigValue())) {
            modelAndView.addObject("dueRemind", portalApi.getDueRemind());
        }

        String loginPageURL = (String) request.getAttribute("loginPageURL");
        Cookie cookie = null;
        if (loginPageURL == null) {
            loginPageURL = "";
        }
        cookie = new Cookie("loginPageURL", loginPageURL);
        cookie.setMaxAge(expiry);
        cookie.setPath("/");
        response.addCookie(cookie);
        modelAndView.addObject("loginFrom", loginFrom);
        String rowList = request.getParameter("rowList");//显示字段
        String btnbgc = request.getParameter("btnbgc");//登录框按钮颜色
        String btombgc = request.getParameter("btombgc");//登录框底部色
        String bgc = request.getParameter("bgc");//背景颜色
        if (Strings.isBlank(rowList)) {
            rowList = "";
        }
        if (Strings.isBlank(btnbgc)) {
            btnbgc = "";
        }
        if (Strings.isBlank(btombgc)) {
            btombgc = "";
        }
        if (Strings.isBlank(bgc)) {
            bgc = "";
        }
        modelAndView.addObject("rowList", rowList);
        modelAndView.addObject("btnbgc", btnbgc);
        modelAndView.addObject("btombgc", btombgc);
        modelAndView.addObject("bgc", bgc);
        String canLocation = onlineManager.getCanLocation();
        String locationUrl = SystemProperties.getInstance().getProperty("portal.weather.locationUrl");
        modelAndView.addObject("canLocation", canLocation);
        modelAndView.addObject("locationUrl", locationUrl);

        modelAndView.addObject("isShowVerifyCode", LockLoginInfoFactory.getInstance().getShowVerifyCode(Strings.getRemoteAddr(request)));
        String verifyCodeTimes = systemConfig.get(IConfigPublicKey.ADD_AUTH_CODE_TIMES);

        //验证码锁定保护输入0次时，弹出验证码
        HttpSession session = request.getSession();
        if ("enable".equals(systemConfig.get(IConfigPublicKey.IS_OPEN_LOCK_PROTECT)) && "0".equals(verifyCodeTimes)) {
            session.setAttribute("userShowVerifyCode", true);
        } else if ("disable".equals(systemConfig.get(IConfigPublicKey.IS_OPEN_LOCK_PROTECT))) {//客户bug 弹出验证码后，关闭验证开关，刷新还会显示验证码
            session.setAttribute("userShowVerifyCode", false);
        }

        //多语言显示条件
        boolean multiLanguageIsopen = I18nUtil.multiLanguageIsEnable();
        modelAndView.addObject("multiLanguageIsopen", multiLanguageIsopen);
        return modelAndView;
    }

    private Map localeInfo = null;

    private Map getLocaleInfo() {
        if (null != localeInfo && localeInfo.size() < LocaleContext.getAllLocales().size()) {
            putLocaleInfo();
        } else {
            putLocaleInfo();
        }
        return localeInfo;
    }

    private void putLocaleInfo() {
        localeInfo = new LinkedHashMap();
        List<Locale> locales = LocaleContext.getAllLocales();
        String locStr;
        for (Locale loc : locales) {
            locStr = loc.toString();
            localeInfo.put(locStr, ResourceUtil.getString("localeselector.locale." + locStr));
        }
    }


    private void loadCAPlugIn(HttpServletRequest request, ModelAndView modelAndView)
            throws UnsupportedEncodingException {
        String caFactory = SystemProperties.getInstance().getProperty("ca.factory");
        String factoryJsp = "/WEB-INF/jsp/ca/ca4" + caFactory + ".jsp";
        String sslVerifyCertValue = "no";
        String keyNum = "noKey";
        boolean hasPluginCA = AppContext.hasPlugin("ca");
        //如果是格尔CA厂商,从cookie中获取
        if ("koal".equals(caFactory)) {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                cookies = new Cookie[0];
            }
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                //是否通过CA校验
                if ("SSL_VERIFY_CERT".equals(cookie.getName())) {
                    sslVerifyCertValue = new String(java.net.URLDecoder.decode(cookie.getValue())
                            .getBytes("ISO-8859-1"), "utf-8");
                }
                if ("KOAL_CERT_CN".equals(cookie.getName())) {
                    keyNum = new String(java.net.URLDecoder.decode(cookie.getValue()).getBytes("ISO-8859-1"), "utf-8");
                }
            }
        }
        if ("Jit".equals(caFactory)) {
            this.loadJitCAPlugin(request, modelAndView);
        }
        modelAndView.addObject("caFactory", caFactory);
        modelAndView.addObject("sslVerifyCertValue", sslVerifyCertValue);
        modelAndView.addObject("keyNum", keyNum);
        modelAndView.addObject("hasPluginCA", hasPluginCA);
        modelAndView.addObject("pageUrl", factoryJsp);
        File jspFile = new File(SystemEnvironment.getApplicationFolder() + factoryJsp);
        if (hasPluginCA && !"koal".equals(caFactory) && jspFile.exists()) {
            modelAndView.addObject("includeJsp", true);
        } else {
            modelAndView.addObject("includeJsp", false);
        }
    }

    private void loadJitCAPlugin(HttpServletRequest request, ModelAndView modelAndView) {
        HttpSession session = request.getSession();
        String randNum = generateRandomNum();
        /**************************
         * 第三步 服务端返回认证原文   *
         **************************/
        // 设置认证原文到session，用于程序向后传递，通讯报文中使用
        AppContext.putSessionContext("ToSign", randNum);

        // 设置认证原文到页面，给页面程序提供参数，用于产生认证请求数据包
        modelAndView.addObject("original", randNum);
    }

    /**
     * 产生认证原文
     */
    private String generateRandomNum() {
        /**************************
         * 第二步 服务端产生认证原文   *
         **************************/
        String num = "1234567890abcdefghijklmnopqrstopqrstuvwxyz";
        int size = 6;
        char[] charArray = num.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb
                    .append(charArray[((int) (Math.random() * 10000) % charArray.length)]);
        }
        return sb.toString();
    }

    /**
     * Locale切换
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView changeLocale(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Locale locale = LocaleContext.parseLocale((String) ParamUtil.getJsonParams().get(LoginConstants.LOCALE));
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String loginPageURL = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginPageURL".equals(cookie.getName()) && cookie.getValue().length() > 0) {
                    loginPageURL = cookie.getValue();
                }
            }
        }
        LocaleContext.setLocale(request, locale);
//        if (locale.equals(LocaleContext.getAllLocales().get(0))) {
//            //OA-24343 减少不必要的Cookie存储
//            Cookies.remove(response, LoginConstants.LOCALE);
//        } else {
        Cookies.add(response, LoginConstants.LOCALE, locale.toString(), Cookies.COOKIE_EXPIRES_FOREVER);
//        }
        User user = AppContext.getCurrentUser();
        if (user != null)
            user.setLocale(locale);
        String loginFrom = request.getParameter("loginFrom");
        request.setAttribute("loginFrom", loginFrom);

        String enc = request.getParameter("enc");
        if (null == loginPageURL || "".equals(loginPageURL)) {
            //邮箱链接二次认证
            if (enc != null && !"".equals(enc)) {
                String mailParam = request.getQueryString();
                mailParam = mailParam.substring(mailParam.indexOf("enc="));
                response.sendRedirect("thirdpartyController.do?method=access&" + mailParam);
                return null;
            }

            return this.index(request, response);
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setView(new RedirectView(loginPageURL));
            return mav;
        }
    }

    /**
     * 首页模板切换，当前用户拥有多首页模板的授权时可用于在可用模板间切换
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    public ModelAndView changeTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long portalTemplateId = ParamUtil.getLong(request.getParameterMap(), "portalTemplateId");
        String showSkinchoose = request.getParameter("showSkinchoose");
        //集团/单位管理员是否刚切换过布局（尚未保存到数据库）
        String isPortalTemplateSwitching = request.getParameter("isPortalTemplateSwitching");
        loginControl.transChangeTemplate(portalTemplateId);
        StringBuilder url = new StringBuilder("main.do?method=main");
        if (showSkinchoose != null) {
            url.append("&showSkinchoose=true");
        }
        if (isPortalTemplateSwitching != null) {
            url.append("&isPortalTemplateSwitching=true");
        }
        response.sendRedirect(url.toString());
        return null;
    }

    /**
     * 登陆动作
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginFrom = request.getParameter("loginFrom");
        //OA-24354 注销原有session，创建新的session
        HttpSession session = request.getSession(false);

        try {
            if (Strings.isBlank(loginFrom) || !"loginPortal".equals(loginFrom)) {
                try {
                    if (formApi4Cap3.isUpgrading()) {//表单升级中
                        BusinessException be = getBusinessException(new BusinessException("login.label.ErrorCode.50"));
                        goout(request, session, response, be, true);
                        return null;
                    }
                    if (!formApi4Cap3.isUpgradedV5()) {
                        super.rendJavaScriptUnclose(response, "alert(\"" + ResourceUtil.getString("login.label.ErrorCode.51") + "\");");
                        ModelAndView view = new ModelAndView("ctp/form/upgrade/formUpgradeIframe");
                        view.addObject("viewUpgrade", true);
                        return view;
                    }
                } catch (Exception e) {
                    logger.warn(e.getLocalizedMessage());
                }
            }

            //校验码转移
            String verifyCode = null;
            //iTrusCA校验码转移
            String oriToSign = null;
            //登陆口令加密种子
            String seed = null;

            //判断失败次数达到演示验证码，切换用户登录，也需要进行验证码验证
            Object isShowVerifyCode = null;
            if (session != null) {
                verifyCode = (String) session.getAttribute(LoginConstants.VerifyCode);
                oriToSign = (String) session.getAttribute("ToSign");
                seed = (String) session.getAttribute(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY);
                isShowVerifyCode = session.getAttribute("userShowVerifyCode");
                try {
                    session.invalidate();
                } catch (Throwable t) {
                    //ignore it
                }
            }

            session = request.getSession(true);
            session.setAttribute("userShowVerifyCode", isShowVerifyCode);
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);
            if (Strings.isNotBlank(verifyCode)) {
                AppContext.putSessionContext(LoginConstants.VerifyCode, verifyCode);
                log.info("verifyCode=" + verifyCode);
            }
            if (Strings.isNotBlank(oriToSign)) {
                AppContext.putSessionContext("ToSign", oriToSign);
            }
            if (Strings.isNotBlank(seed)) {
                AppContext.putSessionContext(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY, seed);
            }
            //Session线程变量更新
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);

            //OA-180658，对于审计管理员的特殊处理，如果没有开启审计管理员，那么不应该走密码校验
            String login_username = request.getParameter("login_username");
            if ("audit-admin".equals(login_username)) {
                String isAuditAdminSwitch = systemConfig.get(IConfigPublicKey.AUDIT_ENABLE);
                if (!("enable".equals(isAuditAdminSwitch))) {
                    //审计管理员未开启直接提示用户不存在
                    LocaleContext.make4Frontpage(request);
                    throw new BusinessException(ResourceUtil.getString("login.label.ErrorCode.1"));
                }
            }
            loginControl.transDoLogin(request, session, response);
            String passwd = request.getParameter("login_password") == null ? "" : request.getParameter("login_password").toString();
            String login_validatePwdStrength = StringUtil.getPasswdStrong(passwd);
            AppContext.putSessionContext("login_validatePwdStrength", login_validatePwdStrength);
            AppContext.putSessionContext("login.smsVerifyCode.success", request.getParameter("login.smsVerifyCode"));

            User user = AppContext.getCurrentUser();
            V3xOrgMember member = orgManager.getMemberById(user.getId());
            if (member.isVJoinExternal()) {//可登录人员：内部人员，编外人员，guest账号；V-Join用户不能登录V5
                LoginResult error = LoginResult.ERROR_UNKNOWN_USER;
                BusinessException e = new BusinessException("login.label.ErrorCode." + error.getStatus(), error.getParameters());
                e.setCode(String.valueOf(error.getStatus()));
                log.info(user.getLoginName() + " 不是v5人员!");
                throw e;
            }
            String username = user.getLoginName();
            String password = user.getPassword();
            String userAgentFrom = user.getUserAgentFrom();
            Locale locale = user.getLocale();
            String fontSize = (String) request.getParameter("fontSize");
            if (Strings.isNotBlank(fontSize)) {
                AppContext.putSessionContext("fontSize", fontSize);
            }
            attemptAuthenticateCounter.remove(user.getId());
            AppContext.putSessionContext("ssoFrom", Strings.escapeNULL(request.getParameter("ssoFrom"), "PC"));
            AppContext.putSessionContext("User-Agent", request.getHeader("User-Agent"));

            //登录成功的目标页面
            String destination = null;
            if (Strings.isBlank(loginFrom) || !"loginPortal".equals(loginFrom)) {
                destination = this.getDestination(request, session);
            }

            String province = request.getParameter("province");
            String city = request.getParameter("city");
            String rectangle = request.getParameter("rectangle");
            onlineManager.setOnlineUserLngLat(province, city, rectangle);

            writeCookie(request, response, session, username, password, userAgentFrom, locale);
            //OA-23900 首页展现增加随机数种子校验，解决部分浏览器关闭窗口后还能打开首页操作的安全漏洞
        /*int random = SecurityHelper.randomInt();
        AppContext.putSessionContext(GlobalNames.SESSION_CONTEXT_MAINPAGE_SEED_KEY, random);
        StringBuilder buf = new StringBuilder(destination);
        if (buf.indexOf("?") != -1)
            buf.append('&');
        else
            buf.append('?');
        buf.append("r=").append(random);
        response.sendRedirect(buf.toString());*/
            if (Strings.isNotBlank(destination)) {
                //安全校验下：只能是站内地址
                response.sendRedirect(destination);
            } else {
                //response.addHeader("LoginOk", "ok");
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("ok");
                out.close();
            }
        } catch (Throwable e) {
            BusinessException be = getBusinessException(e);
            if (!(e instanceof InfrastructureException)) {
                log.error(e.getLocalizedMessage(), e);
            } else {
//            	session.setAttribute("login.result", e.getCause().getLocalizedMessage());
                // 用户名密码错误不输出堆栈信息
                log.debug(e.getCause().getLocalizedMessage(), e);
            }
            if (be != null) {
                String code = be.getCode();
                if ("15".equals(code)) {
                    //短信验证码无效或者过期
                    session.setAttribute("isSmsError", "true");

                }
            }
            if (Strings.isNotBlank(loginFrom) && "loginPortal".equals(loginFrom)) {
                response.addHeader("LoginOk", "fail");
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                String message = "";
                if (null != be) {
                    message = be.getMessage();
                }
                if (Strings.isNotBlank(message)) {
                    message = message.replaceAll("<br/>", "\n\r").replaceAll("<strong>", "").replaceAll("</strong>", "");
                }
                if (Strings.isBlank(message)) {
                    message = ResourceUtil.getString("login.label.ErrorCode.1");
                }
                out.println(message);
                out.close();
            } else {
                goout(request, session, response, be, true);
            }
        }
        return null;
    }

    /**
     * Vjoin系统登录
     */
    @NeedlessCheckLogin
    public ModelAndView login4Vjoin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        try {
            //清除session
            this.logout4Session(request, response);

            session = request.getSession(true);
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);

            if (!AppContext.hasPlugin("vjoin")) {
                LoginResult error = LoginResult.ERROR_DOG_EXPIRED;
                BusinessException e = new BusinessException("login.label.ErrorCode." + error.getStatus(), error.getParameters());
                e.setCode(String.valueOf(error.getStatus()));
                throw e;
            }

            String username = request.getParameter(LoginConstants.USERNAME);//用户名
            if (!principalManager.isExist4Vjoin(username)) {
                LoginResult error = LoginResult.ERROR_UNKNOWN_USER;
                BusinessException e = new BusinessException("login.label.ErrorCode." + error.getStatus(), error.getParameters());
                e.setCode(String.valueOf(error.getStatus()));
                throw e;
            }

            loginControl.transDoLogin(request, session, response);

            User user = AppContext.getCurrentUser();
            if (!AppContext.hasPlugin("vjoin") || user.getExternalType() != OrgConstants.ExternalType.Interconnect1.ordinal()) {
                LoginResult error = LoginResult.ERROR_UNKNOWN_USER;
                BusinessException e = new BusinessException("login.label.ErrorCode." + error.getStatus(), error.getParameters());
                e.setCode(String.valueOf(error.getStatus()));
                throw e;
            }
        } catch (Throwable e) {
            BusinessException be = getBusinessException(e);
            if (!(e instanceof InfrastructureException)) {
                log.error(e.getLocalizedMessage(), e);
            }
            goout(request, session, response, be, false);
            return null;
        }
        return null;
    }

    /**
     * 致信登录（单独）
     */
    //致信3.0登录
    @NeedlessCheckLogin
    public ModelAndView login4Ucpc3(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return loginForUcpc(request, response, false);
    }

    /**
     * @param request
     * @param response
     * @param isIgnoreOnline 是否忽略不添加至为登录用户（true：不占并发，false：占并发）
     * @return
     * @throws Exception
     */
    private ModelAndView loginForUcpc(HttpServletRequest request, HttpServletResponse response, Boolean isIgnoreOnline) throws Exception {
        HttpSession session = null;
        try {
            session = request.getSession(false);
            //校验码转移
            String verifyCode = null;
            //iTrusCA校验码转移
            String oriToSign = null;
            //登陆口令加密种子
            String seed = null;
            if (session != null) {
                verifyCode = (String) session.getAttribute(LoginConstants.VerifyCode);
                oriToSign = (String) session.getAttribute("ToSign");
                seed = (String) session.getAttribute(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY);
                try {
                    session.invalidate();
                } catch (Throwable t) {
                    //ignore it
                }
            }

            session = request.getSession(true);
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);
            if (Strings.isNotBlank(verifyCode)) {
                AppContext.putSessionContext(LoginConstants.VerifyCode, verifyCode);
                log.info("verifyCode=" + verifyCode);
            }
            if (Strings.isNotBlank(oriToSign)) {
                AppContext.putSessionContext("ToSign", oriToSign);
            }
            if (Strings.isNotBlank(seed)) {
                AppContext.putSessionContext(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY, seed);
            }
            //Session线程变量更新
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);

            loginControl.transDoLogin(request, session, response);
            AppContext.putSessionContext("login_validatePwdStrength", request.getParameter("login_validatePwdStrength"));

            User user = AppContext.getCurrentUser();
            Locale locale = LocaleContext.make4Frontpage(request);
            if (locale != null) {
                user.setLocale(locale);
                CurrentUser.set(user);
            }

            //V-Join用户不能登录致信
            if (user.getExternalType() != OrgConstants.ExternalType.Inner.ordinal()) {
                LoginResult error = LoginResult.ERROR_UNKNOWN_USER;
                BusinessException e = new BusinessException("login.label.ErrorCode." + error.getStatus(), error.getParameters());
                e.setCode(String.valueOf(error.getStatus()));
                throw e;
            }
            String username = user.getLoginName();
            String password = user.getPassword();
            String userAgentFrom = user.getUserAgentFrom();
            locale = user.getLocale();
            String fontSize = (String) request.getParameter("fontSize");
            if (Strings.isNotBlank(fontSize)) {
                AppContext.putSessionContext("fontSize", fontSize);
            }
            AppContext.putSessionContext("ssoFrom", Strings.escapeNULL(request.getParameter("ssoFrom"), "PC"));
            writeCookie(request, response, session, username, password, userAgentFrom, locale);
            if (userAgentFrom.equals(login_useragent_from.ucpc.name())) {
                HashMap<String, Object> resultMap = Maps.newHashMap();
                resultMap.put("LoginOK", response.getHeader("LoginOK"));
                resultMap.put("JSESSIONID", session.getId());
                Cookie[] cookie = request.getCookies();
                if (cookie != null) {
                    String route = "";
                    for (int i = 0; i < cookie.length; i++) {
                        if (cookie[i].getName().trim().equalsIgnoreCase("route")) {
                            route = cookie[i].getValue().trim();
                        }
                    }
                    if (Strings.isNotBlank(route)) {
                        resultMap.put("route", route);
                    }
                }
                resultMap.put("ucLogin", response.getHeader("ucLogin"));
                resultMap.put("loginName", user.getLoginName());
                resultMap.put("memberid", response.getHeader("memberid"));
                //密码过期/前度校验信息
                boolean checkPwd = canCheckPwd(user);
                Object from = request.getParameter("from");
                if ("backToHome".equals(from)) {
                    checkPwd = false;
                }
                Object[] pwdExpirationInfo = (Object[]) AppContext.getSessionContext("PwdExpirationInfo-" + user.getLoginName());
                boolean isPwdExpirationInfoNotEmpty = (pwdExpirationInfo != null) && (pwdExpirationInfo.length != 0);
                //判断是否密码到期或者不符合强度要求强制校验
                String pwdModifyForceEnable = systemConfig.get("pwdmodify_force_enable");
                String login_validatePwdStrength = StringUtil.getPasswdStrong(request.getParameter(LoginConstants.PASSWORD));
                AppContext.putSessionContext("login_validatePwdStrength", login_validatePwdStrength);
                boolean validateLoginPwdStrength = getValidateLoginPwdStrength(session);

                Map<String, String> secretModify = Maps.newHashMap();
                String type = request.getParameter("loginType");
                if ("autoLogin".equals(type)) {
                    secretModify.put("alertEnable", "0");
                    secretModify.put("forceModify", "0");
                } else {
                    boolean alertEnable = checkPwd && (isPwdExpirationInfoNotEmpty || !validateLoginPwdStrength);
                    secretModify.put("alertEnable", alertEnable ? "1" : "0");
                    secretModify.put("forceModify", (alertEnable && "enable".equals(pwdModifyForceEnable)) ? "1" : "0");
                }
                secretModify.put("modifyEnable", "1");
                resultMap.put("verification", ucLoginSecurityManager.buildVerification());
                resultMap.put("modifySecretSetting", secretModify);
                response.getWriter().write(JSONUtil.toJSONString(resultMap));
            }
        } catch (Throwable e) {
            BusinessException be = getBusinessException(e);
            if (!(e instanceof InfrastructureException)) {
                log.error(e.getLocalizedMessage(), e);
            }
            goout4Ucpc(request, session, response, be);
            HashMap<String, String> resultMap = new HashMap<String, String>();
            resultMap.put("LoginOK", "fail");
            resultMap.put("code", be.getCode());
            if ("3404".equals(be.getCode())) {
                resultMap.put("message", ResourceUtil.getString("login.label.ErrorCode.3404"));
            } else if ("3405".equals(be.getCode())) {
                resultMap.put("message", ResourceUtil.getString("login.label.ErrorCode.3405"));
            } else if ("3406".equals(be.getCode())) {
                String error_message = response.getHeader("error_message");
                if (Strings.isNotBlank(error_message)) {
                    resultMap.put("message", error_message);
                } else {
                    resultMap.put("message", be.getMessage());
                }
            } else {
                resultMap.put("message", be.getMessage());
            }
            PrintWriter out;
            try {
                response.setContentType("text/JavaScript;charset=utf-8");
                out = response.getWriter();
                out.write(JSONUtil.toJSONString(resultMap));
            } catch (Exception eee) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private String getDestination(HttpServletRequest request, HttpSession session) {
        String destination = request.getParameter(LoginConstants.DESTINATION);
        session.removeAttribute(LoginConstants.DESTINATION);
        if (Strings.isBlank(destination) || destination.equals(request.getContextPath())) {
            String contextPath = request.getContextPath();
            if ("/".equals(contextPath)) {
                contextPath = "";
            }

            return contextPath + appDefaultPath;
        }
        String path = SystemEnvironment.getContextPath();
        if (destination.startsWith(path + "/")) {
            AppContext.putSessionContext(LoginConstants.DESTINATION, destination);
            return destination;
        }
        log.warn(" login.destination must start with " + path + "/, eg:" + path + "/xxx, but your value is " + destination);
        return null;
    }

    private BusinessException getBusinessException(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof BusinessException)
            return (BusinessException) e;
        else {
            return getBusinessException(e.getCause());
        }
    }

    @CheckRoleAccess(roleTypes = {Role_NAME.SystemAdmin, Role_NAME.AuditAdmin, Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator})
    public ModelAndView managementIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/portal/management/managementIndex");
        String localHelp = SystemEnvironment.getApplicationFolder() + File.separator + "help" + File.separator + SystemProperties.getInstance().getProperty("system.ProductId") + File.separator + "user.html";
        mav.addObject("localHelp", new File(localHelp).exists());
        mav.addObject("productVersion", Functions.getVersion());
        String accountName = AppContext.currentAccountName();
        boolean isGroupVer = (Boolean) SysFlag.sys_isGroupVer.getFlag();
        if (!isGroupVer) {
            V3xOrgAccount rootAccount = orgManager.getRootAccount();
            if (rootAccount != null) {
                accountName = rootAccount.getName();
            }
            //非集团版
            mav.addObject("isGroupVer", "false");
        }
        mav.addObject("accountName", accountName);
        String menus = portalApi.getAdminMenus();
        mav.addObject("menus", menus);
        mav.addObject("onlineForAll", OnlineRecorder.getOnlineUserNumber4ALL());
        String from = "";
        if (request.getParameter("from") != null) {
            from = request.getParameter("from").toString();
        }
        mav.addObject("from", from);

        String locationUrl = SystemProperties.getInstance().getProperty("portal.weather.locationUrl");
        mav.addObject("locationUrl", locationUrl);
        String messageIntervalSecond = systemConfig.get(IConfigPublicKey.MESSAGE_INTERVAL_SECOND);
        mav.addObject("messageIntervalSecond", messageIntervalSecond);

        // a6协同云帮助url  位置不同
        if (!SystemEnvironment.isSuitDeployMode()) {
            String v5cloudHelpurl = SystemProperties.getInstance().getProperty(V5CLOUD_HELP_URL);
            mav.addObject("v5cloudHelpurl", v5cloudHelpurl);
        }
        return mav;
    }

    @CheckRoleAccess(roleTypes = {Role_NAME.SystemAdmin, Role_NAME.AuditAdmin, Role_NAME.GroupAdmin, Role_NAME.AccountAdministrator})
    public ModelAndView managementMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/portal/management/managementMain");
        User user = AppContext.getCurrentUser();

        //获取当前系统所有人数 /当前单位所有人数
        int memberSize = 0;
        if (user.isAdministrator()) {
            memberSize = orgDao.getAllMemberPONumsByAccountId(user.getAccountId(), null, null, true, null, null);
        } else {
            memberSize = orgDao.getAllMemberPONumsByAccountId(null, null, null, true, null, null);
        }
        mav.addObject("members", memberSize);

        //获取当前系统购买模式（PC/M3）
        Object o = MclclzUtil.invoke(c1, "getInstance", new Class[]{String.class}, null, new Object[]{""});
        //1是注册数.2是并发数
        Integer serverType = (Integer) MclclzUtil.invoke(c1, "getserverType", null, o, null);
        Integer serverTypeM = (Integer) MclclzUtil.invoke(c1, "getm1Type", null, o, null);
        mav.addObject("serverType", serverType);
        mav.addObject("serverTypeM", serverTypeM);
        Long maxOnline = (Long) MclclzUtil.invoke(c1, "getTotalservernum", null, o, null);
        Long maxOnlineM = (Long) MclclzUtil.invoke(c1, "getTotalm1num", null, o, null);

        boolean isGroup = (Boolean) SysFlag.sys_isGroupVer.getFlag();
        if (isGroup && user.isAdministrator()) {
            Object obj = MclclzUtil.invoke(c1, "getInstance", new Class[]{String.class}, null, new Object[]{String.valueOf(user.getAccountId())});
            if (null != obj) {
                if ("2".equals(MclclzUtil.invoke(c1, "getServerPermissionType"))) {//按单位分配
                    maxOnline = (Long) MclclzUtil.invoke(c1, "getTotalservernum", null, obj, null);
                }
                if ("2".equals(MclclzUtil.invoke(c1, "getM1PermissionType"))) {//按单位分配
                    maxOnlineM = (Long) MclclzUtil.invoke(c1, "getTotalm1num", null, obj, null);
                }
            }
        }

        mav.addObject("maxOnline", maxOnline);
        mav.addObject("maxOnlineM", maxOnlineM);

        //获取当前在线人数（PC/M3） 
        int currentOnlineP = OnlineRecorder.getOnlineUserNumber4Server();
        mav.addObject("currentOnlineP", currentOnlineP);
        int currentOnlineM = OnlineRecorder.getOnlineUserNumber4M1();
        mav.addObject("currentOnlineM", currentOnlineM);

        //获取上次登录时间
        LogonLog lastLogonLog = logonLogManager.getLastLogonLog(user.getId());
        Date lastLogonTime = null;
        String lastIpAddress = null;
        if (lastLogonLog != null) {
            lastLogonTime = lastLogonLog.getLogonTime();
            lastIpAddress = lastLogonLog.getIpAddress();
        }
        mav.addObject("lastLogonTime", lastLogonTime != null ? Datetimes.formatDatetime(lastLogonTime) : ResourceUtil.getString("common.default"));
        mav.addObject("lastIpAddress", Strings.isNotBlank(lastIpAddress) ? lastIpAddress : ResourceUtil.getString("common.default"));

        mav.addObject("productCategory", ProductEditionEnum.getCurrentProductEditionEnum().getName());
        mav.addObject("productVersion", Functions.getVersion());
        mav.addObject("buildId", "B" + Datetimes.format(SystemEnvironment.getProductBuildDate(), "yyMMdd") + "." + SystemEnvironment.getProductBuildVersion() + ".CTP" + SystemEnvironment.getCtpProductBuildVersion());

        //密码过期/前度校验信息
        boolean checkPwd = canCheckPwd(user);
        Object from = request.getParameter("from");
        if (from != null && "backToHome".equals(from)) {
            checkPwd = false;
        }
        Object[] pwdExpirationInfo = (Object[]) AppContext.getSessionContext("PwdExpirationInfo-" + user.getLoginName());

        //判断是否密码到期或者不符合强度要求强制校验
        String pwdModifyForceEnable = systemConfig.get("pwdmodify_force_enable");
        HttpSession session = request.getSession(false);
        boolean validateLoginPwdStrength = getValidateLoginPwdStrength(session);
        mav.addObject("validateLoginPwdStrength", validateLoginPwdStrength);
        mav.addObject("pwdModifyForceEnable", pwdModifyForceEnable);
        mav.addObject("pwdExpirationInfo", pwdExpirationInfo);
        mav.addObject("checkPwd", checkPwd);

        //获取升级信息
        List<ConfigItem> upgradeLogs = configManager.listAllAccountConfigByCategory("system_upgrade");
        Collections.sort(upgradeLogs, new Comparator<ConfigItem>() {
            public int compare(ConfigItem o1, ConfigItem o2) {
                if (o1.getCreateDate() == null || o2.getCreateDate() == null) {
                    return 0;
                }
                if (o1.getCreateDate().before(o2.getCreateDate())) {
                    return 1;
                } else if (o1.getCreateDate().after(o2.getCreateDate())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        mav.addObject("upgradeLogs", upgradeLogs);

        //获取操作日志
        List<Long> actionUserIds = new ArrayList<Long>();
        actionUserIds.add(user.getId());
        Pagination.setNeedCount(false);//不需要count
        List<AppLog> appLogs = appLogManager.queryAppLogs(null, null, actionUserIds, null, null, null, null);
        mav.addObject("appLogs", appLogs);

        String menus = portalApi.getAdminMenus();
        mav.addObject("menus", menus);
        return mav;
    }

    /**
     * 首页展现，登陆后或首页模板切换后调用
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        HttpSession session = request.getSession(false);
        V3xOrgMember member = null;
        if (null != user) {
            member = orgManager.getMemberById(user.getId());
        }

        try {
            if (formApi4Cap3.isUpgrading()) {//表单升级中
                AppContext.putSessionContext(LoginConstants.Result, ResourceUtil.getString("login.label.ErrorCode.50"));
            } else if (!formApi4Cap3.isUpgradedV5()) {
                AppContext.putSessionContext(LoginConstants.Result, ResourceUtil.getString("login.label.ErrorCode.51"));
            }

            //V7.1 企业公文升级到政务公文
            String versionStr = ProductEditionEnum.getCurrentProductEditionEnum().getValue();
            if (formApi4Cap3.isUpgradedV5() && formApi4Cap3.isUpgradedV5() && edocApi != null
                    && !versionStr.equals(ProductEditionEnum.government.getValue())
                    && !versionStr.equals(ProductEditionEnum.governmentgroup.getValue())) {
                int upgradeCode = edocApi.isV5EdocUpgrade();
                if (upgradeCode != 2 && upgradeCode != 5) {
                    edocApi.setUpdatePageLocale();
                    ModelAndView view = new ModelAndView("edoc/upgrade/edocTo71Upgrade");
                    view.addObject("govdocUpgrad", upgradeCode);
                    if (edocApi.noFileExists()) {
                        view.addObject("noFiles", "1");
                    }
                    return view;
                }
            }

        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage());
        }
        /* OnlineManager onLineManager = (OnlineManager)AppContext.getBean("onlineManager");*/
        if (session == null || null == member /*|| Constants.LoginUserState.offline == onLineManager.getOnlineState(user)*/) {
        	/*if(session != null && Constants.LoginUserState.offline == onLineManager.getOnlineState(user)){
        		// 针对currentUser没在线，但是是offline，销毁session
		        session.invalidate();
	        }*/
            //return index(request, response);
            //设置Seed防止IE刷新退出无法登陆
            session = request.getSession(true);
            AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);
            SecurityHelper.getSessionContextSeed();
            String url = "/main.do?method=index";
            return super.redirectModelAndView(url);
        }

        if (user != null) {
            if (user.isAdmin() && !user.isVJoinMember()) {
                return this.managementIndex(request, response);
            }
            String bigScreen = request.getParameter("bigScreen");
            //大屏用户跳转到大屏门户
            if (!user.isDefaultGuest() && user.isScreenGuest()) {
                if (Strings.isBlank(bigScreen)) {
                    return this.redirectGuestIndex(request, response);
                }
            }
            String portalId = request.getParameter("portalId");
            String loginPortal = request.getParameter("loginPortal");
            String subPortal = request.getParameter("subPortal");


            if (Strings.isBlank(loginPortal) && Strings.isBlank(bigScreen)) {
                //不是来自登录栏目请求，才判断下是否开启登录前Guest账号，如果开启，则自动登录Guest账号，并跳转到登录前门户
                boolean isLoginGuestOpened = guestManager.isGuestEnable();
                boolean isLoginPortalOpened = portalApi.isLoginPortalOpened();
                if (isLoginGuestOpened && isLoginPortalOpened && user.isGuest() && AppContext.hasPlugin("vPortalAdvanced")) {//开启登录前Guest账号，且登录前门户处于启动状态
                    //跳转到portal首页
                    String url = "/main.do?method=main&loginPortal=1&portalId=" + com.seeyon.ctp.portal.util.Constants.preLoginPortalId;
                    return super.redirectModelAndView(url);
                }
            } else if (Strings.isNotBlank(loginPortal) && Strings.isBlank(bigScreen) && Strings.isBlank(subPortal) && !user.isDefaultGuest()) {
                String url = "/main.do?method=index";
                return super.redirectModelAndView(url);
            }
            //有种情况打开首页会没有初始化数据,通过access打开协同或消息后 session 是有了,但是没有执行inituser的方法,所以此时跳转到main会没有菜单等信息
//            Object menus=user.getProperty("menus");Object spaceJson=user.getProperty("spacesList");
//            if(menus==null||spaceJson==null){
//                this.loginControl.initLoginUser();
//            }
            ModelAndView modelAndView = new ModelAndView("ctp/portal/index");
            String localHelp = SystemEnvironment.getApplicationFolder() + File.separator + "help" + File.separator + SystemProperties.getInstance().getProperty("system.ProductId") + File.separator + "user.html";
            modelAndView.addObject("localHelp", new File(localHelp).exists());
            modelAndView.addObject("productVersion", Functions.getVersion());
            String messageIntervalSecond = systemConfig.get(IConfigPublicKey.MESSAGE_INTERVAL_SECOND);
            modelAndView.addObject("messageIntervalSecond", messageIntervalSecond);
            VPortalObj vPortalObj = null;
            String selectedSpaceId = request.getParameter("spaceId");
            if (Strings.isNotBlank(bigScreen) || Strings.isNotBlank(loginPortal) || Strings.isNotBlank(subPortal)) {//大屏用户，guest用户,子门户
                if (Strings.isNotBlank(loginPortal)) {
                    boolean isLoginGuestOpened = guestManager.isGuestEnable();
                    boolean isLoginPortalOpened = portalApi.isLoginPortalOpened();
                    if (!(isLoginGuestOpened && isLoginPortalOpened) || !AppContext.hasPlugin("vPortalAdvanced")) {//如果没有开启登录前门户，则直接跳转到index页面
                        //跳转到portal首页
                        String url = "/main.do?method=index";
                        return super.redirectModelAndView(url);
                    }
                } else if (Strings.isNotBlank(bigScreen)) {
                    PortalSet portalSet = portalApi.getPortalSetFromCache(Long.parseLong(portalId));
                    if (portalSet == null || portalSet.getIsdelete() == 1 || portalSet.getState() == 0 || !user.isScreenGuest()) {
                        //跳转到portal首页
                        String url = "/main.do?method=logout";
                        return super.redirectModelAndView(url);
                    }
                }
                if (Strings.isNotBlank(portalId)) {
                    if (Strings.isNotBlank(selectedSpaceId) && Strings.isDigits(selectedSpaceId)) {
                        PortalSpaceFix spaceFix = portalApi.getSpaceFix(Long.valueOf(selectedSpaceId));
                        if (spaceFix == null) {
                            throw new BusinessException("portal.noexit.space");
                        }
                    }
                    PortalSet set = portalApi.getPortalSetFromCache(Long.parseLong(portalId));
                    if (set == null || set.getIsdelete() == 1) {
                        throw new BusinessException("portal.noexit.portalset");
                    }
                }
                vPortalObj = portalApi.getVPortalObj(user, portalId, selectedSpaceId);
                if (Strings.isNotBlank(loginPortal)) {
                    vPortalObj.setType("3");
                }
                if (Strings.isNotBlank(bigScreen)) {
                    vPortalObj.setType("2");
                }
                if (Strings.isNotBlank(subPortal)) {
                    vPortalObj.setSubPortal("true");
                }
            } else {
                if (Strings.isNotBlank(portalId) && Strings.isNotBlank(selectedSpaceId)) {
                    vPortalObj = portalApi.getVPortalObj(user, portalId, selectedSpaceId);
                } else {
                    vPortalObj = portalApi.getVPortalObj(user);
                }
            }
            modelAndView.addObject("vPortalObj", vPortalObj);

            //增加外部模块需要在首页增加jsp页面引用的接口            
            List<String> ExpansionJsps = new ArrayList<String>();
            if (CollectionUtils.isNotEmpty(expansionJspForHomepage)) {
                for (ExpandJspForHomePage expansionJspCreater : expansionJspForHomepage) {
                    List<String> expansionJsp = expansionJspCreater.expandJspForHomePage(null);
                    if (expansionJsp != null && expansionJsp.size() > 0) {
                        ExpansionJsps.addAll(expansionJsp);
                    }

                }
                if (CollectionUtils.isNotEmpty(expansionJspForHomepage)) {
                    modelAndView.addObject("ExpansionJsp", ExpansionJsps);
                }
            }

            //NC专用
            String currentSpaceForNC = request.getParameter("currentSpaceForNC");
            modelAndView.addObject("currentSpaceForNC", currentSpaceForNC);

            String openFrom = request.getParameter("from");
            if (Strings.isNotBlank(openFrom)) {
                modelAndView.addObject("openFrom", openFrom);
            } else {
                modelAndView.addObject("openFrom", "");
            }

            long random = System.currentTimeMillis();
            modelAndView.addObject("random", random);
            Cookie cookie = new Cookie("avatarImageUrl", String.valueOf(AppContext.currentUserId()));
            cookie.setMaxAge(expiry10year);
            cookie.setPath("/");
            response.addCookie(cookie);
            modelAndView.addObject("selectedSpaceId", selectedSpaceId);
            modelAndView.addObject("isLeave", UserLastOperationRecorder.getInstance(Operation.LAST_ACCESS).isLeave(user.getId()));
            return modelAndView;
        } else {
            //用户登录信息失效
            BusinessException e = new BusinessException("loginUserState.unknown");
            e.setCode("-1");
            goout(request, session, response, e, true);
            return null;
        }
    }


    /**
     * vPortal公共数据生成
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView vPortalData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/javascript;charset=UTF-8");
        User user = AppContext.getCurrentUser();
        long time = user != null ? user.getLoginTimestamp().getTime() : 0;
        String etag = "e" + SystemEnvironment.getProductBuildVersion() + "" + time + "";

        if (WebUtil.checkEtag(request, response, etag)) { //匹配，没有修改，浏览器已经做了缓存
            return null;
        }
        response.setStatus(200);
        HttpSession session = request.getSession(false);
        ModelAndView modelAndView = new ModelAndView("ctp/portal/vPortalData");
        String layoutId = request.getParameter("mls");
        String spaceId = request.getParameter("fsid");//第一个空间ID
        String spacePath = request.getParameter("fspath");//第一个空间path
        String spaceType = request.getParameter("fstype");//第一个空间类型
        if (Strings.isNotBlank(spacePath)) {
            PortalSpaceFix templateSpaceFix = portalApi.getRealTemplateSpaceFix(spacePath);
            if (templateSpaceFix != null) {
                String path = templateSpaceFix.getPath();
                Long id = templateSpaceFix.getId();
                if (id != null && Strings.isNotBlank(path)) {
                    spaceId = id.toString();
                    spacePath = path;
                }

            }
        }
        String portalId = request.getParameter("portalId");
        String themeId = request.getParameter("themeId");
        String onlySpace = request.getParameter("onlySpace");
        String menuSource = request.getParameter("menuSource");
        String spaceShowStr = request.getParameter("from");
        Object firstLogin = session.getAttribute("fl");
        boolean spaceShow = false;
        if (Strings.isNotBlank(spaceShowStr) && "previewVPortalSpace".equals(spaceShowStr)) {
            spaceShow = true;
        }
        PortalTemplate pageLayout = null;
        if (Strings.isNotBlank(layoutId)) {
            pageLayout = portalApi.getPortalTemplate(Long.parseLong(layoutId));
        }
        PortalSet portalSet = null;
        if (Strings.isNotBlank(portalId)) {
            portalSet = portalApi.getPortalSetFromCache(Long.parseLong(portalId));
        }
        if (null != user && (pageLayout == null || pageLayout.getType() != 1)) {//PC门户
            String firstSpaceSummary = portalApi.getSpaceFragmentsAndProperties(spaceId, spacePath, spaceType);
            String spaceJsonStr = "[]";
            if (Strings.isBlank(onlySpace) || !"true".equals(onlySpace)) {
                String onlineStatuArr = "[";
                onlineStatuArr += "{'i':'0','n':'" + ResourceUtil.getString("portal.onlineState.label1") + "'},";
                onlineStatuArr += "{'i':'1','n':'" + ResourceUtil.getString("portal.onlineState.label2") + "'},";
                onlineStatuArr += "{'i':'2','n':'" + ResourceUtil.getString("portal.onlineState.label3") + "'},";
                onlineStatuArr += "{'i':'3','n':'" + ResourceUtil.getString("portal.onlineState.label4") + "'}";
                onlineStatuArr += "]";
                modelAndView.addObject("onlineStatuArr", onlineStatuArr);

                OnlineUser onlineUser = OnlineRecorder.getOnlineUser(user.getLoginName());
                if (null != onlineUser) {
                    int onlineNum = onlineManager.getOnlineNumber();

                    modelAndView.addObject("onlineNumber", onlineNum);
                    modelAndView.addObject("currentState", onlineUser.getOnlineSubState().ordinal());
                }

                //判断是否具有发送手机短信的权限(是否显示手机图标)
                boolean smsSendEnabled = false;
                if (mobileMessageManager.isCanSend(user.getId(), user.getLoginAccount())) {
                    smsSendEnabled = true;
                }

                //是否开启个人打卡 V7.0变动：考勤开关去掉，统一由插件模块启停控制
                boolean cardEnabled = AppContext.hasPlugin("attendance") && user.isInternal();

                boolean searchEnabled = AppContext.hasPlugin("index");

                //是否开启薪资查看
                String sal = systemConfig.get(IConfigPublicKey.SALARY_ENABLE);
                boolean salaryEnabled = sal != null && "enable".equals(sal) && AppContext.hasPlugin("salary") && user.isInternal();
                //检查是否有在线客服功能授权
                boolean OnlineCustomerServiceEnabled = false;


                String hasserviceEndDate = null;
                if (SystemEnvironment.isSuitDeployMode()) {

                    if (onlineCustomerServiceManager.checkOnlineCustomerService(user.getId()) || user.isAdmin()) {
                        OnlineCustomerServiceEnabled = true;
                    }
                    //在线客服加密狗获取到期日期
                    Object serviceEndDate = onlineCustomerServiceManager.getOnlineCustomerServiceMessage().get(OnlineCustomerServiceUtil.SERVICE_END_DATE);
                    //获取星级
                    String serviceLevel = (String) onlineCustomerServiceManager.getOnlineCustomerServiceMessage().get(OnlineCustomerServiceUtil.SERVIC_ELEVEL);
                    if (null != serviceEndDate) {
                        Integer expireDayNum = DateUtil.beforeDays(DateUtil.currentDate(), (Date) serviceEndDate);
                        //增加不同星卡等级过期时间延长提醒
                        if ((expireDayNum < 0 && (Strings.isBlank(serviceLevel) || "1".equals(serviceLevel))) || (expireDayNum < -15 && "2".equals(serviceLevel)) || (expireDayNum < -30 && "3".equals(serviceLevel))) {
                            hasserviceEndDate = "no";
                        } else {
                            hasserviceEndDate = "yes";
                        }
                    }
                } else { // TODO : 非套装模式进行相应的调整
                    hasserviceEndDate = "yes";
                }

                boolean appCenterEnabled = true;
                if (ProductEditionEnum.isU8OEM()) {
                    cardEnabled = false;//U8不显示考勤打卡
                    appCenterEnabled = false;//云应用中心不显示
                }
                boolean checkPwd = canCheckPwd(user);
                Object[] pwdExpirationInfo = (Object[]) AppContext.getSessionContext("PwdExpirationInfo-" + user.getLoginName());

                //判断是否密码到期或者不符合强度要求强制校验
                String pwdModifyForceEnable = systemConfig.get("pwdmodify_force_enable");
                boolean validateLoginPwdStrength = getValidateLoginPwdStrength(session);

                boolean msgSoundEnable = isMsgSoundEnable(user);
                boolean msgClosedEnable = isMsgClosedEnable(user);
                //标题
                String pageTitle = portalApi.getPageTitle();
                //子门户的系统标题为门户名称 OA-147518 宇飞
                if (portalSet != null && !portalApi.isMasterPortal(portalSet)) {
                    pageTitle = ResourceUtil.getString(portalSet.getPortalName());
                }
                String[] orgInfo = portalApi.getCurrentOrgInfo();
                //集团简称名称
                String groupShortName = orgInfo[0];
                modelAndView.addObject("groupShortName", Strings.escapeJavascript(groupShortName));
                //集团外文名称
                String groupSecondName = orgInfo[1];
                modelAndView.addObject("groupSecondName", Strings.escapeJavascript(groupSecondName));
                //当前单位简称
                String accountShortName = orgInfo[2];
                modelAndView.addObject("accountShortName", Strings.escapeJavascript(accountShortName));
                //当前单位外文名称
                String accountSecondName = orgInfo[3];
                modelAndView.addObject("accountSecondName", Strings.escapeJavascript(accountSecondName));
                String departmentName = orgInfo[4];
                modelAndView.addObject("departmentName", Strings.escapeJavascript(departmentName));
                String postName = orgInfo[5];
                modelAndView.addObject("postName", Strings.escapeJavascript(postName));

                modelAndView.addObject("pageTitle", pageTitle);
                modelAndView.addObject("groupSecondName", Strings.escapeJavascript(groupSecondName));
                modelAndView.addObject("accountSecondName", Strings.escapeJavascript(accountSecondName));
                modelAndView.addObject("msgClosedEnable", msgClosedEnable);
                modelAndView.addObject("msgSoundEnable", msgSoundEnable);
                modelAndView.addObject("validateLoginPwdStrength", validateLoginPwdStrength);
                modelAndView.addObject("pwdModifyForceEnable", pwdModifyForceEnable);
                modelAndView.addObject("pwdExpirationInfo", pwdExpirationInfo);
                modelAndView.addObject("checkPwd", checkPwd);
                modelAndView.addObject("appCenterEnabled", appCenterEnabled);
                modelAndView.addObject("salaryEnabled", salaryEnabled);
                modelAndView.addObject("cardEnabled", cardEnabled);
                modelAndView.addObject("searchEnabled", searchEnabled);
                modelAndView.addObject("smsSendEnabled", smsSendEnabled);
                modelAndView.addObject("OnlineCustomerServiceEnabled", OnlineCustomerServiceEnabled);
                modelAndView.addObject("hasserviceEndDate", hasserviceEndDate);
                if (null != portalSet) {
                    String navCustomSwith = portalApi.getNavCustomSwitch(portalSet);
                    String menuCustomSwith = portalApi.getMenuCustomSwitch(portalSet);
                    modelAndView.addObject("navCustomSwitch", navCustomSwith);
                    modelAndView.addObject("menuCustomSwitch", menuCustomSwith);
                    boolean isMasterPortal = portalApi.isMasterPortal(portalSet.getId());
                    if (isMasterPortal) {
                        long entityId = AppContext.currentUserId();
                        long accountId = AppContext.currentAccountId();
                        String key = "menu" + "_" + portalId + "_" + entityId + "_" + accountId;
                        boolean customseMenu = portalApi.isCustomseNav(key);
                        modelAndView.addObject("customseMenu", customseMenu);
                    }
                }

                if (null != portalSet && portalSet.getPortalType() != 0) {//这段不能再注释掉了，注释之前请先找王朝文确认，大屏、登录前要用到这个
                    List<PortalNavBo> spaceNavList = new ArrayList<PortalNavBo>();
                    List<SpaceBO> spacesList = null;
                    if (spaceShow) {//空间预览的情况
                        spacesList = new ArrayList<SpaceBO>();
                        SpaceBO spaceBo = portalApi.getSpaceBoById(spaceId, spacePath, user.getLoginAccount());
                        if (null != spaceBo) {
                            spacesList.add(spaceBo);
                        }
                    } else {
                        spacesList = portalApi.getSpaceBoList(user, "pc", portalId);
                    }
                    if (Strings.isNotEmpty(spacesList)) {
                        int index = 0;
                        for (SpaceBO spaceBO : spacesList) {
                            PortalNavBo nav = portalApi.createPortalNavBoFromSpaceBO(spaceBO);
                            VPortalObj spacePortalObj = portalApi.getSpacePortalObj(nav.getId());
                            nav.setSpacePortalObj(spacePortalObj);
                            if (index == 0) {
                                if (spacePortalObj != null) {
                                    modelAndView.addObject("spacePortalObjStr", JSONUtil.toJSONString(spacePortalObj));
                                }
                            }
                            index++;
                            spaceNavList.add(nav);
                        }
                    }
                    spaceJsonStr = JSONUtil.toJSONString(spaceNavList);
                } else if (spaceShow || (Strings.isNotBlank(spaceId) && Strings.isDigits(spaceId))) {
                    List<PortalNavBo> spacesList = new ArrayList<PortalNavBo>();
                    SpaceBO space = portalApi.getSpaceBoById(spaceId, spacePath, user.getLoginAccount());
                    if (null != space) {
                        PortalNavBo nav = portalApi.createPortalNavBoFromSpaceBO(space);
                        VPortalObj spacePortalObj = portalApi.getSpacePortalObj(nav.getId());
                        nav.setSpacePortalObj(spacePortalObj);
                        spacesList.add(nav);
                        if (spacePortalObj != null) {
                            modelAndView.addObject("spacePortalObjStr", JSONUtil.toJSONString(spacePortalObj));
                        }
                    }
                    spaceJsonStr = JSONUtil.toJSONString(spacesList);
                } else {
                    if (Strings.isBlank(firstLogin == null ? "" : firstLogin.toString())) {
                        processPcAndMobileOnlineAtSameTime(user, onlineUser);
                        session.setAttribute("fl", "1");
                    }
                }

                //获得代理信息
                //判断是否有代理
                boolean hasAgent = AgentUtil.hasAgentInfo();
                String agentMessage = "";
                String agentIds = "";
                if (hasAgent) {
                    //判断是否弹出代理 提示
                    String agentInfo = AgentUtil.agentSettingAlert();
                    if (agentInfo != null && !"".equals(agentInfo)) {
                        String info[] = agentInfo.split("::");
                        agentMessage = info[0];
                        agentIds = info[1];
                    }
                }
                String messageForAgentAlert = Functions.urlEncoder(agentMessage);
                String idsForAgentAlert = agentIds;
                boolean isMessageForAgentAlertNotEmpty = false;
                if (Strings.isNotBlank(agentMessage)) {
                    isMessageForAgentAlertNotEmpty = true;
                }
                boolean isIdsForAgentAlertNotEmpty = false;
                if (Strings.isNotBlank(agentIds)) {
                    isIdsForAgentAlertNotEmpty = true;
                }
                modelAndView.addObject("messageForAgentAlert", messageForAgentAlert);
                modelAndView.addObject("idsForAgentAlert", idsForAgentAlert);
                modelAndView.addObject("isMessageForAgentAlertNotEmpty", isMessageForAgentAlertNotEmpty);
                modelAndView.addObject("isIdsForAgentAlertNotEmpty", isIdsForAgentAlertNotEmpty);
            } else if (Strings.isNotBlank(spaceId)) {//主题空间要用这个，不能注释掉，如有疑问找王朝文确认
                SpaceBO spaceBO = portalApi.getSpaceBoById(spaceId, spacePath, user.getLoginAccount());
                if (spaceBO != null) {
                    String id = spaceBO.getSpaceId();
                    VPortalObj spacePortalObj = portalApi.getSpacePortalObj(id);
                    if (spacePortalObj != null) {
                        spacePortalObj.setFirstSpaceId(id);
                        modelAndView.addObject("ThemSpacePortalObjStr", JSONUtil.toJSONString(spacePortalObj));
                    }
                }
                List<SpaceBO> spacesList = new ArrayList<SpaceBO>();
                spacesList.add(spaceBO);
                spaceJsonStr = JSONUtil.toJSONString(spacesList);
            }

            Locale locale = AppContext.getLocale();
            boolean isDevelop = AppContext.isRunningModeDevelop();
            String editionI18nSuffix = ProductEditionEnum.getCurrentProductEditionEnum().getI18nSuffix();
            modelAndView.addObject("editionI18nSuffix", editionI18nSuffix);
            modelAndView.addObject("locale", locale);
            modelAndView.addObject("isDevelop", isDevelop);
            modelAndView.addObject("spaceJsonStr", spaceJsonStr);
            modelAndView.addObject("spaceId", spaceId);
            modelAndView.addObject("firstSpaceSummary", firstSpaceSummary);

            String portalSetStr = "{}";
            String showType = "operation";
            if (null != portalSet) {
                portalSetStr = JSONUtil.toJSONString(portalSet);
                if (portalSet.getPortalType() == 0 || portalSet.getPortalType() == 5) {//只有普通门户和业务门户才有必要查菜单数据

                    showType = portalApi.getMessageShowType("pc");

                    boolean isMasterPortal = false;
                    Long portalLongId = Long.parseLong(portalId);
                    if (portalLongId.longValue() == 1l) {
                        isMasterPortal = true;
                    } else {
                        if (portalSet != null && portalSet.getParentId() != null && portalSet.getParentId().longValue() == 1l) {
                            isMasterPortal = true;
                        }
                    }

                    List<MenuBO> menus = null;

                    if (portalSet.getPortalType() != 0) {
                        menus = portalApi.getPortalMenus(portalId);
                        if (isMasterPortal) {//追加代理菜单
                            UserHelper.setMenus(menus);//从initUser挪到这里
                            MenuBO agentMenuBo = portalApi.createAgentMenuBo();
                            if (null != agentMenuBo) {
                                menus.add(0, agentMenuBo);
                            }
                        }
                    }
                    if (menus == null) {
                        menus = new ArrayList<MenuBO>(0);
                    }
                    modelAndView.addObject("portalMenus", JSONUtil.toJSONString(menus));
                }
            }
            modelAndView.addObject("portalSet", portalSetStr);
            modelAndView.addObject("messageShowType", showType);
        } else {
            modelAndView = new ModelAndView("ctp/portal/vPortalDataTest");
        }
        String avatar = SystemEnvironment.getContextPath() + "/apps_res/v3xmain/images/personal/pic.gif";
        if (user != null) {
            avatar = OrgHelper.getAvatarImageUrl(user.getId());
        }
        modelAndView.addObject("memberImageUrl", avatar);
        modelAndView.addObject("portalId", portalId);
        modelAndView.addObject("themeId", themeId);
        String datatplJsArr = "";
        if (Strings.isNotBlank(layoutId)) {
            datatplJsArr = portalApi.getPortaLayoutDataTpls(Long.parseLong(layoutId));
        }
        if (Strings.isBlank(datatplJsArr)) {
            datatplJsArr = "[]";
        }
        modelAndView.addObject("datatplJsArr", datatplJsArr);
        modelAndView.addObject("onlySpace", onlySpace);
        String currentCity = onlineManager.getCurrentCity();
        if (Strings.isNotBlank(currentCity) && currentCity.endsWith("市")) {
            currentCity = currentCity.substring(0, currentCity.length() - 1);
        }
        String currentProvince = onlineManager.getCurrentProvince();
        String currentX = onlineManager.getCurrentX();
        String currentY = onlineManager.getCurrentY();
        String canLocation = SystemProperties.getInstance().getProperty("portal.weather.location");
        String locationUrl = SystemProperties.getInstance().getProperty("portal.weather.locationUrl");
        modelAndView.addObject("canLocation", canLocation);
        modelAndView.addObject("locationUrl", locationUrl);
        modelAndView.addObject("currentProvince", currentProvince);
        modelAndView.addObject("currentCity", currentCity);
        modelAndView.addObject("currentX", currentX);
        modelAndView.addObject("currentY", currentY);
        modelAndView.addObject("ctxPath", request.getContextPath());

        WebUtil.writeETag(request, response, etag, 1000L * 60 * 5);
        return modelAndView;
    }

    /**
     * 异步处理：登录时PC和移动端同时在线时的消息提醒
     *
     * @param user
     * @param onlineUser
     */
    private void processPcAndMobileOnlineAtSameTime(User user, OnlineUser onlineUser) throws Exception {
        if (!user.isAdmin() && null != onlineUser) {// 非管理员
            String pLang = user.getCustomize(CustomizeConstants.LOCALE);
            String userAgentFrom = user.getUserAgentFrom();
            Long userId = user.getId();
            Map<login_sign, LoginInfo> loginInfoMapper = onlineUser.getLoginInfoMap();
            String loginType = getMobileLoginType();
            userMessageManager.processPcAndMobileOnlineAtSameTime(userId, pLang, userAgentFrom, loginInfoMapper, loginType);
        }
    }

    /**
     * 消息查看后是否需要从消息框中移出  2009年7月23日 dongyj
     *
     * @param user
     * @return
     */
    private boolean isMsgClosedEnable(User user) {
        boolean result = !"false".equals(user.getCustomize(CustomizeConstants.MESSAGEVIEWREMOVED));
        return result;
    }

    /**
     * 是否需要播放声音
     *
     * @param user
     * @return
     */
    private boolean isMsgSoundEnable(User user) {
        boolean msgSoundEnable = false;
        String enableMsgSoundConfig = this.systemConfig.get(IConfigPublicKey.MSG_HINT);
        if (enableMsgSoundConfig != null) {
            if ("enable".equals(enableMsgSoundConfig)) {
                msgSoundEnable = "true".equals(user.getCustomize(CustomizeConstants.MESSAGESOUNDENABLED));
            }
        }
        return msgSoundEnable;
    }

    /**
     * 登陆时输入的密码的密码强度
     *
     * @param session
     * @return
     * @throws BusinessException
     */
    private boolean getValidateLoginPwdStrength(HttpSession session) throws BusinessException {
        Long memberId = AppContext.getCurrentUser().getId();
        V3xOrgMember member = orgManager.getMemberById(memberId);
        if (member == null || !member.isValid()) {
            return false;
        }

        Object smsVerifyCode = session.getAttribute("login.smsVerifyCode.success");
        if (smsVerifyCode != null) {//如果是短信验证码登陆，不校验密码强度
            return true;
        }

        //登陆时输入的密码的密码强度
        String login_validatePwdStrength = String.valueOf(session.getAttribute("login_validatePwdStrength"));
        //如果没有取到密码强度，设置强度为最高（可能是从其他途径 致信等方式跳转过来的）
        Double loginPwdStrength = (Strings.isNotBlank(login_validatePwdStrength) && !"null".equals(login_validatePwdStrength)) ? Double.valueOf(login_validatePwdStrength) : 4.0;

        //密码强度
        String pwd_strong_require = systemConfig.get("pwd_strong_require");
        int requirePwdStrength = 1;
        if (pwd_strong_require.isEmpty()) {
            pwd_strong_require = "weak";
        }
        if ("weak".equals(pwd_strong_require)) {
            requirePwdStrength = 1;
        }
        if ("medium".equals(pwd_strong_require)) {
            requirePwdStrength = 2;
        }
        if ("strong".equals(pwd_strong_require)) {
            requirePwdStrength = 3;
        }
        if ("best".equals(pwd_strong_require)) {
            requirePwdStrength = 4;
        }

        //不符合强度要求
        if (loginPwdStrength < requirePwdStrength) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 普通用户修改密码时，提示过期或者强度不符合要求的情况。
     * 2.没有开启目录绑定条目，提示。
     * 3.开启了目录绑定条目
     * a.没有绑定，用oa账号进行登陆（开启了可以用oa账号登陆的设置），提示。
     * b.绑定了，用ad账号进行登陆，系统设置中设置了允许在协同中修改LDAP密码，并使用ssl方式，提示.其他情况不提示。
     */
    private boolean canCheckPwd(User user) {
        boolean checkPwd = true;
        if (!user.isAdmin()) {
            if (!LdapUtils.isLdapEnabled()) {
                checkPwd = true;
            } else {
                if (!LdapUtils.isBind(AppContext.getCurrentUser().getId())) {
                    checkPwd = true;
                } else {
                    if (LdapUtils.isOaCanModifyLdapPwd() && LDAPConfig.getInstance().getIsEnableSSL()) {
                        checkPwd = true;
                    } else {
                        checkPwd = false;
                    }
                }
            }
        }
        return checkPwd;
    }

    private String getErrorDestination(HttpServletRequest request, HttpSession session) {
        String error_destination = request.getParameter(Constants.LOGIN_ERROR_DESTINATION);
        if (session != null) {
            if (error_destination != null)
                AppContext.putSessionContext(Constants.LOGIN_ERROR_DESTINATION, error_destination);
            else
                session.removeAttribute(Constants.LOGIN_ERROR_DESTINATION);
        }

        if (error_destination == null) {
            error_destination = request.getContextPath();
            if (error_destination == null || "".equals(error_destination)) {
                error_destination = "/main.do";
            } else {
                error_destination += "/main.do";
            }
        }

        return error_destination;
    }

    private void goout(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                       BusinessException be, boolean jumpDestination) {
        //登录失败目标页面
        String error_destination = getErrorDestination(request, session);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginPageURL".equals(cookie.getName()) && cookie.getValue().length() > 0) {
                    error_destination = request.getContextPath() + cookie.getValue();
                    cookie.setMaxAge(0);
                    cookie.setValue(null);
                    response.addCookie(cookie);
                }
            }
        }
        if (be != null && session != null) {
            //清除已有session信息
            Enumeration<String> attsEnu = session.getAttributeNames();
            List<String> attrs = new ArrayList<String>();
            while (attsEnu.hasMoreElements()) {
                attrs.add(attsEnu.nextElement());
            }
            for (String name : attrs) {
                //为了保证用户失败验证达到一定次数弹出验证码，需要保证一个有判断的session值
                if ("userShowVerifyCode".equals(name) || "isSmsError".equals(name)) {
                    continue;
                }
                session.removeAttribute(name);
            }
            AppContext.putSessionContext(LoginConstants.Result, be.getMessage());
            response.addHeader("LoginError", be.getCode());
        }
        try {
            if (jumpDestination) {
                if (SystemEnvironment.isSuitDeployMode()) {
                    response.sendRedirect(response.encodeURL(error_destination));
                } else {//非套装，退出到协同云登录页面
                    toV5CLoudLoginPage(response, ImmutableMap.of("errorCode", be.getCode()));
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void goout4Ucpc(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                            BusinessException be) {
        // 登录失败目标页面
        String error_destination = getErrorDestination(request, session);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginPageURL".equals(cookie.getName()) && cookie.getValue().length() > 0) {
                    error_destination = request.getContextPath() + cookie.getValue();
                    cookie.setMaxAge(0);
                    cookie.setValue(null);
                    response.addCookie(cookie);
                }
            }
        }
        if (be != null && session != null) {
            // 清除已有session信息
            Enumeration<String> attsEnu = session.getAttributeNames();
            List<String> attrs = new ArrayList<String>();
            while (attsEnu.hasMoreElements()) {
                attrs.add(attsEnu.nextElement());
            }
            for (String name : attrs) {

                session.removeAttribute(name);
            }
            AppContext.putSessionContext(LoginConstants.Result, be.getMessage());
            response.addHeader("LoginError", be.getCode());
        }

    }

    private static void writeCookie(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                                    String username, String password, String userAgentFrom, Locale locale) {
        if (login_useragent_from.mobile.name().equals(userAgentFrom)) {
            boolean rememberName = request.getParameterValues(LoginConstants.rememberName) != null;
            //boolean rememberPassword = request.getParameterValues(LoginConstants.rememberPassword) != null;
            boolean rememberPassword = false;
            if (rememberName) {
                rememberPassword = true;
            }

            if (Boolean.TRUE.equals(rememberName)) {
                Cookies.add(response, LoginConstants.USERNAME, username, Cookies.COOKIE_EXPIRES_FOREVER, true);
                Cookies.add(response, LoginConstants.rememberName, "true", Cookies.COOKIE_EXPIRES_FOREVER);
                session.removeAttribute(LoginConstants.rememberName);
            } else {
                Cookies.remove(response, LoginConstants.USERNAME);
                Cookies.remove(response, LoginConstants.rememberName);
            }

            if (Boolean.TRUE.equals(rememberPassword)) {
                Cookies.add(response, LoginConstants.PASSWORD, password, Cookies.COOKIE_EXPIRES_FOREVER, true);
                Cookies.add(response, LoginConstants.rememberPassword, "true", Cookies.COOKIE_EXPIRES_FOREVER);
                session.removeAttribute(LoginConstants.rememberPassword);
            } else {
                Cookies.remove(response, LoginConstants.PASSWORD);
                Cookies.remove(response, LoginConstants.rememberPassword);
            }

            Cookies.add(response, "u_login_from", userAgentFrom, Cookies.COOKIE_EXPIRES_FOREVER, false);
            Cookies.add(response, "u_login_name", username, Cookies.COOKIE_EXPIRES_One_day, true);
            Cookies.add(response, "u_login_password", password, Cookies.COOKIE_EXPIRES_One_day, true);
        }

        if (locale != null) {
            String lang = request.getHeader("Accept-Language");
            Locale defaultLocale = lang == null ?
                    LocaleContext.getSysSetDefaultLocale() :
                    LocaleContext.parseLocale(lang);
//            if (locale.equals(defaultLocale)) {
//                //OA-24343 减少不必要的Cookie存储
//                Cookies.remove(response, LoginConstants.LOCALE);
//            } else {
            //不remove locale的cookie了，因为会导致部分ie浏览器在退出后，不会保存前一次登录的语种，而是使用默认的语种（remove掉了，原因是ie的accept-language转化后的会与user的locale）
            Cookies.add(response, LoginConstants.LOCALE, locale.toString(), Cookies.COOKIE_EXPIRES_FOREVER);
//            }
        }
    }

    /**
     * 系统退出
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragrma", "no-cache");

        HttpSession session = request.getSession(false);

        //OA-43979 退出时进行登陆相关的session信息复制，避免退出和消息遍历可能的同时退出动作导致登陆相关验证数据错误
        //校验码转移
        String verifyCode = null;
        //iTrusCA校验码转移
        String oriToSign = null;
        //登陆口令加密种子
        String seed = null;
        if (session != null) {
            verifyCode = (String) session.getAttribute(LoginConstants.VerifyCode);
            oriToSign = (String) session.getAttribute("ToSign");
            seed = (String) session.getAttribute(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY);
        }
        String destination = loginControl.transDoLogout(request, session, response);
        session = request.getSession(true);

        //Session线程变量更新
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_SESSION_KEY, session);
        if (Strings.isNotBlank(verifyCode)) {
            AppContext.putSessionContext(LoginConstants.VerifyCode, verifyCode);
        }
        if (Strings.isNotBlank(oriToSign)) {
            AppContext.putSessionContext("ToSign", oriToSign);
        }
        if (Strings.isNotBlank(seed)) {
            AppContext.putSessionContext(GlobalNames.SESSION_CONTEXT_SECURITY_SEED_KEY, seed);
        }

        if ("close".equals(destination)) {
            response.setContentType("text/html; charset=UTF-8");

            PrintWriter out = response.getWriter();
            out.println("<script>top.window.close();</script>");
            out.close();
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (("loginPageURL".equals(cookie.getName())) && cookie.getValue().length() > 0) {
                        destination = cookie.getValue();
                        cookie.setMaxAge(0);
                        cookie.setValue(null);
                        response.addCookie(cookie);
                    }
                }
            }
            oauthLoginManager manager = new oauthLoginManagerImpl();
            if (SystemEnvironment.isSuitDeployMode()) {
                //zhou:在这里添加判断
                Map<String, Object> map = new HashMap<>();
                if (null == user.getLoginName()) {
                    response.sendRedirect(SystemEnvironment.getContextPath() + destination);
                } else {
                    map.put("loginName", user.getLoginName());
                    List<LoginRecord> recordList = manager.selectLoginRecordByLoginName(map);
                    if (recordList.size() > 0) {
                        LoginRecord lr = recordList.get(0);
                        if ("auth".equals(lr.getLoginType())) {
                            lr.setLoginType("");
                            manager.updateLoginRecord(lr);
                            PropUtils propUtils = new PropUtils();
                            response.sendRedirect(propUtils.getSSOOAuthLogout() + "&clientId=" + propUtils.getSSOClientId() + "&returnUrl=" + java.net.URLEncoder.encode(propUtils.getSSOClientHomePage(), "utf-8"));
                        }
                    } else {
                        response.sendRedirect(SystemEnvironment.getContextPath() + destination);
                    }
                }
            } else {//非套装，退出到协同云登录页面
                toV5CLoudLoginPage(response, null);
            }
        }
        if (AppContext.isSystemAdmin()) {
            //这个对象只有系统管理员才会用,有点浪费空间,退出之时,清理掉缓存的信息,节约点内存
            I18nResourceCacheHolder.setPcResourceInfo(new ConcurrentHashMap<String, List<Map<Locale, ResourceInfo>>>());
        }
        return null;
    }

    /**
     * 协同云环境 退出到统一登录页面
     *
     * @param response
     * @throws Exception
     */
    private void toV5CLoudLoginPage(HttpServletResponse response, ImmutableMap<String, Object> params) throws Exception {
        String configUrl = SystemProperties.getInstance().getProperty(V5CLOUD_LOGIN_SERVICE_URL);
        StringBuilder cloudLoginUrl = new StringBuilder(configUrl);
        if (MapUtils.isNotEmpty(params)) {
            cloudLoginUrl.append("/#/login?");
            ImmutableSet<Map.Entry<String, Object>> entries = params.entrySet();
            boolean isFrist = true;
            for (Map.Entry<String, Object> entry : entries) {
                if (!isFrist) {
                    cloudLoginUrl.append("&");
                } else {
                    isFrist = false;
                }
                cloudLoginUrl.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            }
        }
        response.sendRedirect(cloudLoginUrl.toString());
		/*
		if(Arrays.asList("/cloud14,/cloud13,/seeyonCloud2".split("\\,")).contains(SystemEnvironment.getContextPath())){
        }else{
            response.sendRedirect(SystemEnvironment.getContextPath() + destination);
        }
        */
    }

    /**
     * Vjoin系统退出
     */
    @NeedlessCheckLogin
    public ModelAndView logout4Vjoin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragrma", "no-cache");

        HttpSession session = request.getSession(false);
        loginControl.transDoLogout(request, session, response);

        return null;
    }

    /**
     * 致信系统退出
     */
    @NeedlessCheckLogin
    public ModelAndView logout4ZX(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        try {
            logout4Vjoin(request, response);
            result.put("status", "successed");
        } catch (Throwable e) {
            result.put("status", "failed");
        }
        PrintWriter out = response.getWriter();
        response.setContentType("text/JavaScript; charset=utf-8");
        out.write(JSONUtil.toJSONString(result));
        return null;
    }

    /**
     * Vjoin清除会话
     */
    @NeedlessCheckLogin
    public ModelAndView logout4Session(HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.clearSession(request, response);
        return null;
    }

    private void clearSession(HttpServletRequest request, HttpServletResponse response) {
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragrma", "no-cache");
        HttpSession session = request.getSession(false);
        try {
            if (session != null) {
                try {
                    session.invalidate();
                } catch (Throwable t) {
                    //ignore it
                }
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getValue().length() > 0) {
                        cookie.setMaxAge(0);
                        cookie.setValue(null);
                        response.addCookie(cookie);
                    }
                }
            }
        } catch (Throwable e) {
            //ignore it
        }
    }

    /**
     * 切换登陆单位
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    public ModelAndView changeLoginAccount(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginAccount = request.getParameter(LoginConstants.LOGIN_ACCOUNT_ID);
        if (Strings.isNotBlank(loginAccount)) {
            loginControl.transChangeLoginAccount(Long.parseLong(loginAccount));
        }
        String isRefresh = request.getParameter("isRefresh");
        String shortCutId = request.getParameter("shortCutId");
        String showSkinchoose = request.getParameter("showSkinchoose");
        String isPortalTemplateSwitching = request.getParameter("isPortalTemplateSwitching");
        String portal_default_page = request.getParameter("portal_default_page");
        String param = "";

        if (Strings.isNotBlank(isRefresh)) {
            param += "&isRefresh=" + isRefresh;
        }
        if (Strings.isNotBlank(showSkinchoose)) {
            param += "&showSkinchoose=true";
        }
        if (Strings.isNotBlank(isPortalTemplateSwitching)) {
            param += "&isPortalTemplateSwitching=true";
        }
        if (Strings.isNotBlank(portal_default_page)) {
            param += "&portal_default_page=default";
        }
        if (Strings.isNotBlank(shortCutId)) {
            param += "&shortCutId=" + shortCutId;
        }
        response.sendRedirect("main.do?method=main&fl=1" + param);

        return null;
    }

    /**
     * 系统关于对话框
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    public ModelAndView showAbout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/about");

        modelAndView.addObject("productVersion", Functions.getVersion());
        modelAndView.addObject(
                "buildId",
                "B" + Datetimes.format(SystemEnvironment.getProductBuildDate(), "yyMMdd") + "."
                        + SystemEnvironment.getProductBuildVersion() + ".CTP"
                        + SystemEnvironment.getCtpProductBuildVersion());
        String edition = ProductEditionEnum.getCurrentProductEditionEnum().getName();
        int releaseYear = ProductVersionEnum.getCurrentVersion().getReleaseDate().getYear() + 1900;

        modelAndView.addObject("releaseYear", String.valueOf(releaseYear));
        modelAndView.addObject("productCategory", edition);

        String docNo = (String) MclclzUtil.invoke(c2, "getDogNo", null, null, null);
        modelAndView.addObject("docNo", docNo);

        this.setProductInfo(modelAndView);

        return modelAndView;
    }

    /**
     * 头公共JS生成
     *
     * @param request  Servlet请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView headerjs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/javascript;charset=UTF-8");

        User user = AppContext.getCurrentUser();
        long time = user != null ? user.getLoginTimestamp().getTime() : 0;
        String etag = "e" + SystemEnvironment.getProductBuildVersion() + "" + time + "";

        if (WebUtil.checkEtag(request, response, etag)) { //匹配，没有修改，浏览器已经做了缓存
            return null;
        }
        response.setStatus(200);
        ModelAndView modelAndView = new ModelAndView("common/header_js");

        WebUtil.writeETag(request, response, etag, 1000L * 60 * 5);
        return modelAndView;
    }

    /**
     * 用户状态刷新/挂起
     *
     * @param request  请求对象
     * @param response Servlet应答对象
     * @return Spring MVC对象
     * @throws Exception
     */
    @NeedlessCheckLogin
    public ModelAndView hangup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = AppContext.getCurrentUser();
        if (currentUser != null)
            this.onlineManager.updateOnlineState(currentUser);
        return null;
    }

    private void setProductInfo(ModelAndView modelAndView) {
        Object o = MclclzUtil.invoke(c1, "getInstance", new Class[]{String.class}, null, new Object[]{""});
        //1是注册数.2是并发数
        Integer serverType = (Integer) MclclzUtil.invoke(c1, "getserverType", null, o, null);
        //1是注册数.2是并发数
        Integer m1ServerType = (Integer) MclclzUtil.invoke(c1, "getm1Type", null, o, null);

        modelAndView.addObject("serverType", serverType);
        modelAndView.addObject("m1ServerType", m1ServerType);
        modelAndView.addObject("maxOnline", MclclzUtil.invoke(c1, "getTotalservernum", null, o, null));
        modelAndView.addObject("maxOnline", MclclzUtil.invoke(c1, "getTotalservernum", null, o, null));
        modelAndView.addObject("m1MaxOnline", MclclzUtil.invoke(c1, "getTotalm1num", null, o, null));
        String MxVersion = SystemEnvironment.getMxVersion();
        modelAndView.addObject("MxVersion", MxVersion);
    }

    private Integer expireDayNum = null;

    /**
     * 获取剩余使用天数，正数表示还有多少天到期，负数表示超期多少天
     */
    private Integer getExpireDayNum() {
        if (expireDayNum == null) {
            Date executeDate = null;
            try {
                //取授权到期日期
                Class<?> c1 = MclclzUtil.ioiekc("com.seeyon.ctp.product.ProductInfo");
                String endDateStr = (String) MclclzUtil.invoke(c1, "getUseEndDate");
                if (StringUtils.isNotBlank(endDateStr) && endDateStr.length() >= 8) {
                    executeDate = DateUtil.parse(endDateStr);
                    Date currentDate = DateUtil.currentDate();
                    //取授权剩余多少天
                    expireDayNum = DateUtil.beforeDays(currentDate, executeDate);
                } else {
                    //如果无授权到期日期则认为是无限制狗，随后就不再做检查了
                    expireDayNum = null;
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return expireDayNum;
    }

    private static final Class<?> c1 = MclclzUtil.ioiekc("com.seeyon.ctp.permission.bo.LicensePerInfo");
    private static final Class<?> c2 = MclclzUtil.ioiekc("com.seeyon.ctp.product.ProductInfo");

    /**
     * 大屏用户跳转到大屏门户
     *
     * @param request
     * @param response
     * @return
     */
    private ModelAndView redirectGuestIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/portal/guest/guestIndex");
        List<PortalSet> portals = portalApi.getPortalSets(AppContext.getCurrentUser(), "pc");
        //过滤停用的大屏门户
        for (Iterator<PortalSet> i = portals.iterator(); i.hasNext(); ) {
            PortalSet p = i.next();
            if (p.getState().equals(0)) {
                i.remove();
            }
        }
        if (portals.size() == 1) {
            return new ModelAndView("redirect:main.do?method=main&bigScreen=1&portalId=" + portals.get(0).getId());
        } else {
            for (PortalSet portalSet : portals) {
                String imgUrl = "<img src=\"" + SystemEnvironment.getContextPath() + "/common/designer/images/portal_set_default.png\">";
                if (Strings.isNotBlank(portalSet.getImgId()) && Strings.isDigits(portalSet.getImgId())) {
                    imgUrl = "<img src=\"" + SystemEnvironment.getContextPath() + "/fileUpload.do?method=showRTE&fileId=" + portalSet.getImgId() + "&type=image\">";
                } else if (Strings.isNotBlank(portalSet.getImgId()) && !Strings.isDigits(portalSet.getImgId())) {
                    imgUrl = "<span class=\"vportal " + portalSet.getImgId() + "\" style=\"font-size:80px;line-height:160px;\"></span>";
                }
                portalSet.setImgId(imgUrl);
            }
        }
        String iconCustomPath = "";
        PortalIconStyle file = portalApi.getPortalIconStyleFromCache(1L);
        if (file != null && null != file.getModifiedCssId()) {
            FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
            V3XFile f = fileManager.getV3XFile(file.getModifiedCssId());
            if (f != null) {
                Date createDate = f.getCreateDate();
                File file1 = fileManager.getFile(file.getModifiedCssId(), createDate);
                if (null != file1 && file1.exists()) {
                    iconCustomPath = "/portal/portalController.do?method=cssData&fileId=" + file.getModifiedCssId();
                }
            }
        }
        mav.addObject("iconCustomPath", iconCustomPath);
        mav.addObject("portalSetList", portals);
        return mav;
    }

    public ModelAndView getAllUserDomainIDs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        List<Long> domainIDs = orgManager.getAllUserDomainIDs(user.getId());
        log.debug(Strings.join(domainIDs, ","));
        return null;
    }

    public void setGuestManager(GuestManager guestManager) {
        this.guestManager = guestManager;
    }
}