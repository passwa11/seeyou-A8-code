<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<script type="text/javascript" src="${path}/common/workflow/workflowDesigner_ajax.js${ctp:resSuffix()}"></script>
<script type="text/javascript" src="${path}/common/workflow/workflowDesigner_decode.js${ctp:resSuffix()}"></script>
<script type="text/javascript">
<!--
var isHistoryFlag = "${isHistoryFlag}";
var currentUserId = '<%=AppContext.currentUserId()%>';
var isModalDialog = "${isModalDialog}";
var oldProcessTemplateId = "${ctp:escapeJavascript(oldProcessTemplateId)}";
var scene = "${ctp:escapeJavascript(scene)}";
var isTemplate = "${isTemplate}";
var processState = parseInt("${processState}");
var showToMe = "${showToMe}";
var showReGo = "${showReGo}";
var showPeopleTip = "${showPeopleTip}";
var ruleContentValue = "${workflowRule}";
//这个是运行态的SubProcessRunning数据
var subProcessJson = ${subProcessJson};
var subProcessSettingJson = ${subProcessSettingJson};
var currentProcesssId = '${ctp:escapeJavascript(processId)}';
var initialize_processXml= '${process_xml}';
var initialize_caseLogXml= '${case_log_xml}';
var debug_caseLogXml= '${debug_case_log_xml}';
var initialize_caseWorkitemLogXml= '${case_workitem_log_xml}';
var initialize_flashArgsXml= '${flash_args_xml}';
var verifyResultXml= "${verifyResultXml}";
var superviseId = "${superviseId}";
var appName = "${ctp:escapeJavascript(appName)}";
var subAppName = "${ctp:escapeJavascript(subAppName)}";
var urlParams = "${urlParams}";
var summaryId = "${param.summaryId}";
var processId = "${ctp:escapeJavascript(processId)}";
var paramProcessId = "${param.processId}";
var formAppId = "${ctp:escapeJavascript(formApp)}"; //表单ID
var formApp = formAppId; //表单ID
var accountExcludeElements= "${accountExcludeElements}";
var selectPeoplePanels = "${selectPeoplePanels}";
var selectPeopleTypes = "${selectPeopleTypes}";
var caseId= "${caseId}";
var defaultPolicyId= "${ctp:escapeJavascript(defaultPolicyId)}";
var defaultPolicyName= "${ctp:escapeJavascript(defaultPolicyName)}";
var endFlag = "${endFlag}";
var permissionAccountId = "${param.permissionAccountId}";
var configCategory = "${param.configCategory}";
var flowPermAccountId = "${ctp:escapeJavascript(flowPermAccountId)}";
var formMutilOprationIds = "${formMutilOprationIds}";
var wendanId = "${wendanId}";
var stepBackCount= "${stepBackCount}";
var canAddSubProcess = "${canAddSubProcess}";
var processEventJson = '${process_event}';
var v = "${ctp:digest_1(CurrentUser.id)}";
var action = "${param.action}";
var addWorkflowExpirationTimeLock = "${param.addWorkflowExpirationTimeLock}";
var isvalidate = "${ctp:escapeJavascript(param.needValidate)}";
var oldWendanId = "${ctp:escapeJavascript(param.oldWendanId)}";
var openFromIframe = "${ctp:escapeJavascript(param.openFromIframe)}";
var isCIPModel = "${isCIPModel}";
// svg 模式画流程图
var isSVG = "${svgSupport eq 'true'}" === "true";
var canCopyFlow = "${canCopyFlow eq 'true'}";
function getProcessId(){
    return processId;
}
//-->
</script>

<script type="text/javascript" src="${path }/common/workflow/svg-designer.min.js${ctp:resSuffix()}_bufix_20191213"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/common/workflow/workflowDesigner-min_svg.js${ctp:resSuffix()}"></script>
