var paramObjs = null;
try {
    if (window.parentDialogObj['workflow_dialog_workflowDesigner_Id']) {
        paramObjs = window.parentDialogObj['workflow_dialog_workflowDesigner_Id'].getTransParams();
    }
} catch (e) {
}
/**
 * @desc 工作流流程设计器前端JS函数
 * @author wangchw
 * @date 2012-08-05
 */
var wfAjax = new WFAjax();
var _parent = parent;
if (isModalDialog == "true") {
    _parent = window.parent;  //模态对话框方式显
}
var _comm = "";
var isInternetExplorer = navigator.appName.indexOf("Microsoft") != -1;
var dwidth = 0;
var dheight = 0;
var returnValueWindow;
if (paramObjs) {
    dwidth = paramObjs.dwidths;
    dheight = paramObjs.dheights;
    returnValueWindow = paramObjs.returnValueWindow;
} else {
    dwidth = window.dialogArguments == undefined ? $(document.body).width() : window.dialogArguments.dwidths;
    dheight = window.dialogArguments == undefined ? $(document.body).height() : window.dialogArguments.dheights;
    returnValueWindow = window.dialogArguments == undefined ? window.parent : window.dialogArguments.returnValueWindow;
}
returnValueWindow = returnValueWindow == undefined ? parent : returnValueWindow;
var processEventObj = new Object();

var svgDesigner;
var hasFlashPlugin = false;

$(function () {
    //导出流程图按钮的控制，只有编辑模式显示导出按钮
    if (scene == "0") {
        $("#exportWorkflowDiagramBtn").show();
    }
    //这里的subProcessJson实际上是模版编辑时的SubProcessSetting数据
    if (window.dialogArguments && window.dialogArguments.subProcessJson) {
        $("#NewflowDIV").html(window.dialogArguments.subProcessJson);
    } else if (window.subProcessSettingJson) {
        $("#NewflowDIV").html($.toJSON(subProcessSettingJson));
    }
    if (processEventJson && processEventJson != "") {
        processEventObj = $.parseJSON(processEventJson);
    }
    if (returnValueWindow) {
        var parent_process_event = $("#process_event", returnValueWindow.document)[0];
        if (parent_process_event && parent_process_event.value && parent_process_event.value != "") {
            processEventObj = new Object();
            processEventObj = $.parseJSON(parent_process_event.value);
        }
    }
    if (window.dialogArguments && window.dialogArguments.workflowRule) {
        $("#ruleContent").attr("value", window.dialogArguments.workflowRule);
    } else {
        $("#ruleContent").attr("value", ruleContentValue);
    }
    if (!dwidth) {
        dwidth = $('body').width();
        if (dwidth > $(getCtpTop().document).width()) {
            dwidth = $(getCtpTop().document).width();
        }
        dheight = $('body').height();
        if (isModalDialog == "true" || isSVG) {
            window.onresize = _windowResize;
        }
    }

    //上面的代码没有获取到高度
    if (dheight == 0) {
        dheight = $('body').height();
    }

    returnValueWindow = window.dialogArguments == undefined ? window.parent : window.dialogArguments.returnValueWindow;
    returnValueWindow = returnValueWindow == undefined ? parent : returnValueWindow;

    var $flashContainer = $('#flashContainer');
    dwidth = $flashContainer.width();
    dheight = $flashContainer.height();

    //上面的代码没有获取到高度
    if (dheight == 0) {
        dheight = $('body').height();
    }

    if (isSVG) {
        // 加载svg设计器
        _initSVGDesigner();
    } else {

        loadFlash(isModalDialog == "true");
    }

    _onloadFunc();
});
var flashAction = null;
var flashActionArgs = null;
//复制时提交过来的校验信息
if (window.dialogArguments && window.dialogArguments.verifyResultXml) {
    verifyResultXml = window.dialogArguments.verifyResultXml;
}

if ((isvalidate == "true")) {
    showCloneTemplateWorkflowProcessXml();
}

//verifyResultXml= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vr s=\"false\"><n id=\"start\" s=\"false\"><r0>false</r0><r0msg><![CDATA[组织模型不存在！]]></r0msg><r1>true</r1><r1msg><![CDATA[]]></r1msg><r2>true</r2><r2msg><![CDATA[]]></r2msg></n><n id=\"13557266660931\" s=\"false\"><r0>false</r0><r0msg><![CDATA[组织模型不存在！]]></r0msg><r1>true</r1><r1msg><![CDATA[]]></r1msg><r2>true</r2><r2msg><![CDATA[]]></r2msg></n><l s=\"false\"><r2>false</r2><r2msg><![CDATA[id=6672249618781720003的部门不存在！]]></r2msg></l></vr>";
function callFlashAction(action, args) {
    lockSubmit(true);
    flashAction = action;
    flashActionArgs = args;
    if (isSVG) {
        svgMonitorObj[flashAction](flashActionArgs);
    } else {
        setTimeout("flashActionDispatcher()", 200);
    }
}

/**
 * 督办时候的催办功能
 */
function edocSupervise(args) {
    //args= decodeArgsFromFlash(args);
    //alert("sprint3中有该工作项，结束时，该功能肯定能完成！");
    var arr = [];
    if (typeof args == 'object') {
        arr = args;
    } else if (typeof args == 'string') {
        arr = args.split(",");
    }
    var activityId = arr[0];
    var processId = arr[1];
    var memberIds = arr[2];
    var result = wfAjax.addMemberIdToCache(activityId, memberIds);
    var tempAppName = 'collaboration';
    if (typeof appName == 'string') {
        tempAppName = appName;
    }
    if (result == "true" || result == true) {
        var dialog = $.dialog({
            url: _ctxPath + '/workflow/hasten.do?method=preHasten&activityId=' + activityId + "&processId=" + processId + "&appName=" + tempAppName,
            width: 400,
            height: 450,
            title: $.i18n("workflow.hasten.label"),
            targetWindow: getCtpTop(),
            buttons: [{
                text: $.i18n("common.button.ok.label"),
                handler: function () {
                    var result = dialog.getReturnValue();
                    try {
                        var obj = $.parseJSON(result);
                        if (obj == null) {
                            return;
                        }
                        if (obj.success == true) {
                            var memberIdArray = obj.memberIdArray;
                            var content = obj.content;
                            var sendPhoneMessage = obj.sendPhoneMessage;
                            var result = wfAjax.hasten(processId, activityId, appName, memberIdArray, superviseId, content, sendPhoneMessage);
                            if (result && result.success && result.message) {
                                //$.alert(result.message);
                                $.infor({
                                    'msg': result.message,
                                    ok_fn: function () {
                                        dialog.close();
                                    }
                                });
                            }
                            if (typeof parent.hastenCallBack == "function") {
                                parent.hastenCallBack();
                            }
//                             else {
//                            	$.alert("请在父页面提供一个名为hastenCallBack催办回调函数，以用于催办后的其他工作，比如刷新列表页面。");
//                            }
                        }
                    } catch (e) {
                        $.alert(e.message);
                    }
                }
            }, {
                text: $.i18n("common.button.cancel.label"),
                handler: function () {
                    dialog.close();
                }
            }]
        });
    }
}

/**
 * 已发督办
 */
function hurry(args) {
    edocSupervise(args);
}

//单击查看子流程或主流程数据
function viewRelateFlow(args) {
    //args= decodeArgsFromFlash(args);
    var nodeId = args[1];
    var tempNodeProcessJson = subProcessJson["" + nodeId];
    if (tempNodeProcessJson == null || tempNodeProcessJson.length == 0) {
        $.alert($.i18n("workflow.label.dialog.noSubFlow.js")/*"没有子流程！"*/);
        return;
    }
    if (tempNodeProcessJson.length > 1) {
        //多于一个的情况下，肯定是查看子流程
        var dialog = $.dialog({
            url: _ctxPath + '/workflow/subProcess.do?method=viewRelateFlow&appName=' + appName + '&nodeId=' + nodeId + "&processId=" + currentProcesssId + "&formMutilOprationIds=" + formMutilOprationIds + "&isHistoryFlag=" + isHistoryFlag,
            width: 560,
            height: 420,
            title: $.i18n("subflow.viewNewflow"),
            transParams: {
                currentProId: currentProcesssId
            },
            targetWindow: getCtpTop(),
            buttons: [{
                text: $.i18n("common.button.close.label"),
                handler: function () {
                    dialog.close();
                }
            }]
        });
    } else {
        var processId = tempNodeProcessJson[0].mainProcessId,
            caseId = tempNodeProcessJson[0].mainCaseId;
        workitemId = "", nodeId = tempNodeProcessJson[0].mainNodeId
            , width = $(getA8Top().document).width() - 100
            , height = $(getA8Top().document).height() - 50
            , title = "";
        if (currentProcesssId == tempNodeProcessJson[0].mainProcessId) {
            processId = tempNodeProcessJson[0].subProcessProcessId;
            caseId = tempNodeProcessJson[0].subProcessCaseId;
            workitemId = "";
            nodeId = "start";
        }
        var obj = wfAjax.getProcessTitleByAppNameAndProcessId(appName, processId);
        if (obj != null && obj.length > 0) {
            title = obj[0];
        }
        var url = _ctxPath + "/collaboration/collaboration.do?method=summary&processId=" + processId + "&openFrom=subFlow&relativeProcessId=" + currentProcesssId + "&formMutilOprationIds=" + formMutilOprationIds + "&dumpData=" + (isHistoryFlag == "true" ? "1" : "");
        //$.alert("查看主流程，弹出一个测试页面没有多少意义，后续迭代与协同联调时再弹出协同页面！");
        var transParams = {};
        var temp = $.dialog({
            url: url
            , width: width
            , height: height
            , title: title
            , targetWindow: getCtpTop()
            , transParams: transParams
            , close_fn: function () {
                temp.transParams = null;
                temp.targetWindow = null;
                temp = null;
                window.dialogDealColl = null;
            }
        });
        transParams.parentDialogObj = temp;
    }
}

//查看节点属性，处理时，单击节点触发的事件
function checkPropertyItem(args) {
    args = decodeArgsFromFlash(args);
    var nodeInfo = [];
    if (typeof args == 'object') {
        nodeInfo = args;
    } else if (typeof args == 'string') {
        nodeInfo = args.split(",");
    }

    var receiveTime = "";
    var dealTime = "";
    var overtopTime = "";
    var processMode = "";
    var remindTime = "";
    var cycleRemindTime = "";
    var templeteId = "";//主要为了草稿以及管理员页面
    var random = Math.random();//主要为了表单绑定模态窗口的缓存


    var stateStr = nodeInfo[0];
    var nodeName = nodeInfo[1];
    var nodePolicy = nodeInfo[2];
    if (!nodeInfo[3] || nodeInfo[3] == "null") {
        receiveTime = "";
    } else {
        receiveTime = nodeInfo[3];
    }

    if (!nodeInfo[4] || nodeInfo[4] == "null") {
        completeTime = "";
    } else {
        completeTime = nodeInfo[4];
    }
    if (!nodeInfo[5] || nodeInfo[5] == "null") {
        overtopTime = "";
    } else {
        overtopTime = nodeInfo[5];
    }
    if (!nodeInfo[6]) {
        processMode = "";
    } else {
        processMode = nodeInfo[6];
    }
    if (!nodeInfo[7]) {
        policyId = "";
    } else {
        policyId = nodeInfo[7];
    }
    if (!nodeInfo[8] || nodeInfo[8] == "null") {
        dealTime = "";
    } else {
        dealTime = nodeInfo[8];
    }

    if (!nodeInfo[9] || nodeInfo[9] == "null") {
        remindTime = "-1";
    } else {
        remindTime = nodeInfo[9];
    }
    var partyId = nodeInfo[10];
    var partyType = nodeInfo[11];
    var matchScope = nodeInfo[12];
    var nodeId = nodeInfo[13];
    var desc = nodeInfo[14];
    var formApp = nodeInfo[15];
    var formViewOperation = nodeInfo[16];
    var dealTermType = nodeInfo[17];
    var dealTermUserId = nodeInfo[18];
    var dealTermUserName = nodeInfo[19];
    var hst = nodeInfo[20];
    var hstv = nodeInfo[21];
    var rup = nodeInfo[22];
    var pup = nodeInfo[23];
    var na = nodeInfo[24];
    var tolerantModel = nodeInfo[25];
    var queryIds = nodeInfo[26];
    var statisticsIds = nodeInfo[27];
    var rScope = nodeInfo[28];

    var mu = nodeInfo[29];
    var na_i = nodeInfo[30];
    var na_b = nodeInfo[31];

    if (!nodeInfo[32] || nodeInfo[32] == "null") {
        cycleRemindTime = "-1";
    } else {
        cycleRemindTime = nodeInfo[32];
    }
    var vjoin = nodeInfo[33];
    var mergeDealType = nodeInfo[34];
    var customName = nodeInfo[35];
    var isOrderExecute = nodeInfo[36];
    var stepBackType = nodeInfo[37];


    if (queryIds == null || queryIds == "null") {
        queryIds = "";
    }
    if (statisticsIds == null || statisticsIds == "null") {
        statisticsIds = "";
    }
    if (nodeId == null || nodeId == "null" || nodeId == "undefined") {//例如从部门节点下来的人员中单击时，就不弹出属性页面了
        return;
    } else {//正常显示属性
        checkPolicy(stateStr, nodeName, nodePolicy, receiveTime, completeTime, overtopTime, isTemplate,
            processMode, policyId, dealTime, remindTime, appName, partyId, partyType,
            matchScope, templeteId, nodeId, summaryId, random, desc, formApp, formViewOperation, dealTermType,
            dealTermUserId, dealTermUserName, processId, hst, hstv, rup, pup, na, tolerantModel,
            queryIds, statisticsIds, rScope, mu, na_i, na_b, cycleRemindTime,
            vjoin, mergeDealType, customName, isOrderExecute, stepBackType);
    }
}

function checkPolicy(stateStr, nodeName, nodePolicy, receiveTime, completeTime, overtopTime, isTemplete, processMode,
                     policyId, dealTime, remindTime, appName, partyId, partyType, matchScope, templeteId, nodeId,
                     summaryId, random, desc, formApp, formViewOperation, dealTermType, dealTermUserId,
                     dealTermUserName, processId, hst, hstv, rup, pup, na, tolerantModel, queryIds, statisticsIds,
                     rScope, mu, na_i, na_b, cycleRemindTime, vjoin, mergeDealType, customName, isOrderExecute,
                     stepBackType) {

    var formRoleCode = "";
    if (partyId) {
        var index1 = partyId.indexOf("@");
        var index2 = partyId.indexOf("#");
        var index3 = partyId.lastIndexOf("#");

        if (index3 != -1 && index2 != index3) {
            formRoleCode = partyId.substr(index3 + 1);
        }

        if (index1 > -1 && index2 > -1) {
            partyId = partyId.substr(0, index2);
        }
    }

    try {
        var theHeight = 420;
        if (partyType == 'Post') {
            theHeight += 30;
            if ($.browser.mozilla) {
                theHeight += 10;
            }
        }
        if ($.browser.chrome || $.browser.safari || $.browser.mozilla || $.browser.opera) {//chrome
            theHeight += 40;
        }
        var dialog = $.dialog({
            id: 'workflow_dialog_showNodeProperty_Id',
            url: _ctxPath + "/workflow/designer.do?method=showNodeProperty&stateStr=" + encodeURIComponent(stateStr)
                + "&nodePolicy=" + escapeSpecialChar(encodeURIComponent(escape(nodePolicy)))
                + "&receiveTime=" + encodeURIComponent(receiveTime)
                + "&completeTime=" + encodeURIComponent(completeTime)
                + "&overtopTime=" + encodeURIComponent(overtopTime)
                + "&isTemplete=" + encodeURIComponent(isTemplete)
                + "&processMode=" + encodeURIComponent(processMode)
                + "&policyId=" + escapeSpecialChar(encodeURIComponent(policyId))
                + "&dealTime=" + encodeURIComponent(dealTime)
                + "&remindTime=" + encodeURIComponent(remindTime)
                + "&appName=" + encodeURIComponent(appName)
                + "&subAppName=" + encodeURIComponent(subAppName)
                + "&partyId=" + escapeSpecialChar(encodeURIComponent(escape(partyId)))
                + "&partyType=" + partyType
                + "&matchScope=" + matchScope
                + "&templeteId=" + templeteId
                + "&nodeId=" + nodeId
                + "&summaryId=" + summaryId
                + "&random=" + random
                + "&formApp=" + formApp

                + "&formViewOperation=" + formViewOperation

                + "&dealTermType=" + encodeURIComponent(dealTermType)
                + "&dealTermUserId=" + encodeURIComponent(dealTermUserId)
                + "&dealTermUserName=" + escapeSpecialChar(encodeURIComponent(dealTermUserName)) + "&processId=" + processId
                + "&nodeName=" + escapeSpecialChar(encodeURIComponent(escape(nodeName))) + "&hst=" + hst
                + "&hstv=" + hstv + "&scene=" + scene + "&rup=" + rup + "&pup=" + pup + "&na=" + na

                + "&tolerantModel=" + tolerantModel
                + "&queryIds=" + queryIds
                + "&statisticsIds=" + statisticsIds
                + "&isHistoryFlag=" + (isHistoryFlag || "false")
                + "&rScope=" + rScope
                + "&mu=" + mu
                + "&na_i=" + na_i
                + "&na_b=" + na_b
                + "&cycleRemindTime=" + cycleRemindTime
                + "&formRoleCode=" + formRoleCode
                + "&mergeDealType=" + mergeDealType
                + "&customName=" + encodeURIComponent(customName)
                + "&vj=" + vjoin
                + "&isOrderExecute=" + isOrderExecute
                + "&stepBackType=" + stepBackType,
            title: $.i18n('workflow.nodeProperty.title'),
            height: theHeight,
            width: 430,
            transParams: {
                "desc": desc
            },
            buttons: [{
                text: $.i18n("common.button.close.label"),
                handler: function () {
                    dialog.close();
                }
            }],
            targetWindow: getCtpTop()
        });
    } catch (e) {//兼容公文老代码
        var theHeight = 420;
        if (partyType == 'Post') {
            theHeight += 30;
            if ($.browser.mozilla) {
                theHeight += 10;
            }
        }
        if ($.browser.chrome || $.browser.safari || $.browser.mozilla || $.browser.opera) {//chrome
            theHeight += 40;
        }
        var dialog = $.dialog({
            id: 'workflow_dialog_showNodeProperty_Id',
            url: _ctxPath + "/workflow/designer.do?method=showNodeProperty&stateStr=" + encodeURIComponent(stateStr)
                + "&nodeName=" + escapeSpecialChar(encodeURIComponent(nodeName)) + "&nodePolicy=" + escapeSpecialChar(encodeURIComponent(nodePolicy))
                + "&receiveTime=" + encodeURIComponent(receiveTime) + "&completeTime=" + encodeURIComponent(completeTime)
                + "&overtopTime=" + encodeURIComponent(overtopTime) + "&isTemplete=" + encodeURIComponent(isTemplete)
                + "&processMode=" + encodeURIComponent(processMode) + "&policyId=" + escapeSpecialChar(encodeURIComponent(policyId))
                + "&dealTime=" + encodeURIComponent(dealTime) + "&remindTime=" + encodeURIComponent(remindTime)
                + "&appName=" + encodeURIComponent(appName)
                + "&subAppName=" + encodeURIComponent(subAppName)
                + "&partyId=" + escapeSpecialChar(encodeURIComponent(escape(partyId))) + "&partyType=" + partyType + "&matchScope=" + matchScope
                + "&templeteId=" + templeteId + "&nodeId=" + nodeId + "&summaryId=" + summaryId + "&random=" + random
                + "&formApp=" + formApp
                + "&formViewOperation=" + formViewOperation
                + "&dealTermType=" + encodeURIComponent(dealTermType)
                + "&dealTermUserId=" + encodeURIComponent(dealTermUserId)
                + "&dealTermUserName=" + escapeSpecialChar(encodeURIComponent(dealTermUserName)) + "&processId=" + processId
                + "&hst=" + hst + "&hstv=" + hstv + "&scene=" + scene + "&rup=" + rup + "&pup=" + pup + "&na=" + na
                + "&rScope=" + rScope
                + "&mu=" + mu
                + "&na_i=" + na_i
                + "&na_b=" + na_b
                + "&formRoleCode=" + formRoleCode
                + "&mergeDealType=" + mergeDealType
                + "&customName=" + encodeURIComponent(customName)
                + "&vj=" + vjoin
                + "&isOrderExecute=" + isOrderExecute
                + "&stepBackType=" + stepBackType,
            title: $.i18n('workflow.nodeProperty.title'),
            height: theHeight,
            width: 430,
            transParams: {
                "desc": desc
            },
            buttons: [{
                text: $.i18n("common.button.close.label"),
                handler: function () {
                    dialog.close();
                }
            }],
            targetWindow: window
        });
    }
}

function lockSubmit(lock) {
    var submitButton = document.getElementById('submitButton') || document.getElementById('confirmButton');
    if (submitButton) {
        submitButton.disabled = lock;
    }
}

function flashActionDispatcher() {
    var monitorObj = _getFlashMonitor();
    var result = flashActionArgs;
    if (flashAction == "selectAddNodePeople") {
        monitorObj.selectAddNodePeople(result);
    } else if (flashAction == "replaceHumenNode") {
        monitorObj.replaceHumenNode(result);
    } else if (flashAction == "setProp") {

        //没有传：7 "" 23 process_event,
        //0 policyOptionValue, 1 policyOptionName, 2 deadTerm ,3 remindTime, 4 processMode,
        //5 formApp, 6 formViewOperation, 8 matchScope, 9 hasNewflow,
        //10 isApplyToAll, 11 formFieldValue/newFormField, 12 desc, 13 dealTermType, 14 dealTermUserId,
        //15 dealTermUserName, 16 hasDesc, 17 isProIncludeChild, 18 "-1", 19 "-1",
        //20 rAutoUp, 21 pAutoUp, 22 nuAction, 24 "1",
        //25 nodeNameStr, 26 queryIds, 27 statisticsIds, 28 rMatchScope, 29 mu, 30 ignoreBlankV,
        //31 asBlankNodeV 32 cycleRemindTime
        monitorObj.setProp(result[0], result[1], result[2], result[3], result[4],
            result[5], result[6], result[8], result[9], result[10],
            result[11], result[12], result[13], result[14], result[15],
            result[16], result[17], result[18], result[19], result[20],
            result[21], result[22], result[24], result[25], result[26],
            result[27], result[28], result[29], result[30], result[31],
            result[32], result[33], result[34], result[35]);
    } else if (flashAction == "handCondition") {
        monitorObj.handCondition(result[0], result[1], result[2], result[3]);
    } else if (flashAction == "autoCondition") {
        monitorObj.autoCondition(result[0], result[1], result[2], result[3], result[4], result[5], result[6]);
    } else if (flashAction == "edocRefresh_Flash") {
        monitorObj.edocRefresh_Flash(result[0], result[1], result[2]);
    } else if (flashAction == "updateNewWorkflow") {
        monitorObj.updateNewWorkflow(result[0]);
    } else if (flashAction == "setConditionDesc") {
        monitorObj.updateConditionDesc(result);
    } else if (flashAction == "setStepBackTargetNode") {
        monitorObj.reCallBackStepBackTargetNode(result);
    } else if (flashAction == "pasteAndReplaceNode") {
        monitorObj.pasteAndReplaceNode();
    } else if (flashAction == "setAdvanceEventFlag") {
        monitorObj.setAdvanceEventFlag(result);
    }
    flashAction = null;
    flashActionArgs = null;
    lockSubmit(false);
}

/**
 * 判断用户是否安装了flash插件
 */
function isInstallFlashPlugin() {

    if (isSVG || hasFlashPlugin) {
        // SVG设计器
        return true;
    }

    var iFlash = false;
    var isIE = $.browser.msie;
    if (isIE) {//for IE
        try {
            var control = new ActiveXObject('ShockwaveFlash.ShockwaveFlash');
            iFlash = true;
        } catch (e) {
            iFlash = false;
        }
    } else {//for other
        if (navigator.plugins) {
            for (var i = 0; i < navigator.plugins.length; i++) {
                if (navigator.plugins[i].name.toLowerCase().indexOf("shockwave flash") >= 0) {
                    iFlash = true;
                    version = navigator.plugins[i].description.substring(navigator.plugins[i].description.toLowerCase().lastIndexOf("Flash ") + 6, navigator.plugins[i].description.length);
                }
            }
        }
    }
    return iFlash;
}

var workFlow = null;

function getXML(args) {

    var monitorObj = _getFlashMonitor();

    if (!monitorObj.showFlashDiagram) {
        setTimeout("getXML()", 100);
        return;
    }

    // 先布局，设置宽度和高度
    loadFlash(isModalDialog == "true");

    var process_xml = _getProcessXML();

    var case_log_xml = initialize_caseLogXml;
    var case_workitem_log_xml = initialize_caseWorkitemLogXml;
    var flash_args_xml = initialize_flashArgsXml;

    monitorObj.showFlashDiagram(process_xml, case_log_xml, case_workitem_log_xml, flash_args_xml, verifyResultXml,
        false, -1, -1);

    if (_comm == "toxml") {
        monitorObj.getDataXML();
    }
    addMouseWheelListener();//绑定事件
}

function getPositionProcessXml(args) {
    //alert("ok");
    args = decodeArgsFromFlash(args);
    var isBigDiagram = false;
    var myCustomObj = wfAjax.analysisProcessDataForFlash(args[0], caseId, true);
    var process_xml1 = myCustomObj.processXml;
    isBigDiagram = myCustomObj.bigDiagram;
    var imageHeight = myCustomObj.imageHeight;
    var imageWidth = myCustomObj.imageWidth;
    //alert(process_xml1);
    var monitorObj = _getFlashMonitor();
    //debugger
    if (monitorObj) {//flash展现
        monitorObj.showPositionProcessXml(process_xml1, imageHeight, imageWidth);
    }
}

function init() {
    monitorObj = _getFlashMonitor();
    jsReady = true;
}

/**
 * 显示流程规则输入框
 */
function showRuleAndResetFlash() {

    var monitorObj = _getFlashMonitor();
    if (monitorObj === false) {
        // flash没有启用
        return;
    }

    var ruleApanMsgObj = document.getElementById("ruleApanMsg");
    var ruleApanCloseImageObj = document.getElementById("ruleApanCloseImage");
    var ruleTRObj = document.getElementById("ruleTR");
    var ruleApanObj = document.getElementById("ruleApan");
    var flashContainerObjHeight = $(monitorObj).attr("height");
    var flashContainerObjWidth = $(monitorObj).attr("width");

    var height;
    var tempExtHeight = 0;

    if (ruleTRObj.style.display == "none") {
        tempExtHeight = 98;
        height = Number(flashContainerObjHeight) - 78;
        monitorObj.setAttribute("width", flashContainerObjWidth);
        monitorObj.setAttribute("height", height);

        ruleTRObj.style.display = "";
        ruleApanMsgObj.style.display = "";
        ruleApanCloseImageObj.style.display = "";
        ruleApanObj.style.display = "none";

    } else {
        tempExtHeight = 20;
        height = Number(flashContainerObjHeight) + 78;
        monitorObj.setAttribute("width", flashContainerObjWidth);
        monitorObj.setAttribute("height", height);

        ruleTRObj.style.display = "none";
        ruleApanMsgObj.style.display = "none";
        ruleApanCloseImageObj.style.display = "none";
        ruleApanObj.style.display = "";
    }

    var isNewBrowser = false;
    if ($.browser.msie) {
        if (parseInt($.browser.version) >= 11) {
            isNewBrowser = true;
        } else {
            var br = navigator.userAgent.toLowerCase();
            var browserVer = (br.match(/.+(?:rv)[\/: ]([\d.]+)/) || [0, '0'])[1];
            if (parseInt(browserVer) >= 11) {
                isNewBrowser = true;
            }
        }
    } else {
        isNewBrowser = true;
    }

    // IE8 才设置
    if (!isNewBrowser) {

        var htmlHeight = $("html").height();
        var flashNewHeight = htmlHeight - tempExtHeight;
        $("#flashContainerTD").height(flashNewHeight);
    }

    setTimeout(function () {
        monitorObj.resizeScrollPane(flashContainerObjWidth, height);
    }, 50);
}

/**
 * 按Esc键时，提示用户是否关闭当前页面
 */
function listenerKeyESC() {
    if (event.keyCode == 27) {
        window.close();
    }
}

/**
 * 流程图页面取消时调用此函数
 * @param args
 */
function cancel(args) {
    window.close();
}

/**
 * 页面load中载入flash流程图
 * @param isDialog 是否为弹出对话框:
 * true表示为弹出对话框方式打开flash流程图，
 * false以正常方式打开flash流程图
 */
function loadFlash(isDialog) {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }

    var isShowButtonInPage = false;

    if (scene == "0" || scene == "1" || scene == "4" || scene == "5") {
        isShowButtonInPage = true;
    }

    if (scene == "6") {
        dheight = dheight - 88;
    }
    resizeFlash(dwidth, dheight);
}

/**
 * 重新调整flash流程图大小
 * @param width 流程图宽度
 * @param height 流程图高度
 */
function resizeFlash(width, height) {

    var monitorObj = _getFlashMonitor();

    if (monitorObj) {

        if (!monitorObj.resizeScrollPane) {
            setTimeout("resizeFlash(" + width + "," + height + ")", 100);
            return;
        }

        monitorObj.setAttribute("width", width);
        monitorObj.setAttribute("height", height);

        monitorObj.resizeScrollPane(width, height);
    }
}

/**
 * 将节点添加到flash流程图中
 */
function dataToFlash(elements, replace) {
    if (!replace) {
        callFlashAction("selectAddNodePeople", elements);
    } else {
        callFlashAction("replaceHumenNode", elements);
    }
}

function setAdvanceEvent(args) {
    args = decodeArgsFromFlash(args);

    var activityId = args[0];
    var activityName = args[1];
    /*var policyId = args[2];
    var policyName = args[3];*/

    if (typeof (openNodeEventAdvancedSetting) != 'undefined') {
        var successCallBack = function (events, eventExt) {
            if (events == "") {
                delete processEventObj[activityId];
                //更新流程图中的标志
                callFlashAction("setAdvanceEventFlag", "");
            } else {
                processEventObj[activityId] = events;
                processEventObj["event_ext"] = eventExt;
                //更新流程图中的标志
                callFlashAction("setAdvanceEventFlag", "true");
            }
        }


        var processEventStr = "";
        if (processEventObj) {
            processEventStr = processEventObj[activityId];
        }


        var param = {
            processId: processId,
            activityId: activityId,
            activityName: activityName,
            success: successCallBack,
            cancle: function () {
                _cancelDesignerCommand();
            },
            events: processEventStr
        };

        //调用外部接口设置节点事件
        if (parent.openNodeEventAdvancedSetting) {
            parent.openNodeEventAdvancedSetting(param);
        } else {
            alert("没有找到名字为openNodeEventAdvancedSetting的接口");
            _cancelDesignerCommand();
        }

    }
}

function openNodeEventAdvancedSetting(param) {
    var _nodeId = param.activityId;
    var _processId = param.processId;
    var events = param.events;


    var dialogWidth = "500";
    if (_nodeId != "start") {
        dialogWidth = "700";
    }
    var dialog = $.dialog({
        id: "workflow_dialog_advancedSetting_id",
        url: _ctxPath + "/workflow/designer.do?method=advancedSetting&appName=form&from=node&nodeId=" + _nodeId + "&processId=" + _processId,
        title: "${ctp:i18n('workflow.advance.event.name')}",//"开发高级",
        width: 800,
        height: 400,
        transParams: {
            "process_event": events
        },
        targetWindow: getCtpTop(),
        buttons: [{
            text: $.i18n("common.button.ok.label"),
            id: "workflowEventAdvancedSetting",
            handler: function () {
                var returnValue = dialog.getReturnValue();
                if (returnValue == "error") {
                    return;
                }

                if (returnValue == "") {
                    delete processEventObj[nodeId];
                    //更新流程图中的标志
                    callFlashAction("setAdvanceEventFlag", "");
                } else {
                    processEventObj[_nodeId] = returnValue;
                    //更新流程图中的标志
                    callFlashAction("setAdvanceEventFlag", "true");
                }
                dialog.close();
            }
        }, {
            text: $.i18n("common.button.delete.label"),
            handler: function () {
                $("#process_event").val("");
                //更新流程图中的标志
                callFlashAction("setAdvanceEventFlag", "");
                dialog.close();
            }
        }, {
            text: $.i18n("common.button.cancel.label"),
            id: "exit",
            handler: function () {
                _cancelDesignerCommand();
                dialog.close();
            }
        }],
        closeParam: {
            show: true,
            autoClose: true,
            handler: function () {
                _cancelDesignerCommand();
            }
        }
    });
}

/**
 * @description dialog组件回调此方法
 */
function OK(jsonArgs) {
    //alert("jsonArgs:="+jsonArgs);
    //alert("jsoonArgs.innerButtonId:="+jsonArgs.innerButtonId);
    var innerButtonId = jsonArgs.innerButtonId;
    if (innerButtonId == 'ok') {
        saveWFContent();
        if (resultFlag) {
            return returnValue;
        }
        return resultFlag;
    } else if (innerButtonId == "supervious_ok") {//督办确定按钮
        var caseState = wfAjax.getCaseState(caseId);
        var isButtonDisabled = caseState[0] == 'true' ? true : false;
        if (isButtonDisabled) {
            $.alert($.i18n('workflow.designer.flow.isover'));
            return false;
        }
        var rs = saveSuperviousWFCaseContent();
        if (rs[0] == 'false') {
            $.alert(rs[1]);
            return false;
        } else {
            return true;
        }
    } else if (innerButtonId == "supervious_modify") {//督办修改按钮
        var caseState = wfAjax.getCaseState(caseId);
        var isButtonDisabled = caseState[0] == 'true' ? true : false;
        if (isButtonDisabled) {
            $.alert($.i18n('workflow.designer.flow.isover'));
            return false;
        }
        return modifySuperviousWFCaseContent();
    } else if (innerButtonId == "admin_repeal") {//管理员撤销流程
        var caseState = wfAjax.getCaseState(caseId);
        var isButtonDisabled = caseState[0] == 'true' ? true : false;
        if (isButtonDisabled) {
            $.alert($.i18n('workflow.designer.flow.isover'));
            return false;
        }
        var repealValue = new Array();
        repealValue.push(appName);
        repealValue.push(processId);
        repealValue.push(caseId);
        return repealValue;
    } else if (innerButtonId == "admin_stop") {//管理员终止流程
        var caseState = wfAjax.getCaseState(caseId);
        var isButtonDisabled = caseState[0] == 'true' ? true : false;
        if (isButtonDisabled) {
            $.alert($.i18n('workflow.designer.flow.isover'));
            return false;
        }
        var repealValue = new Array();
        repealValue.push(appName);
        repealValue.push(processId);
        repealValue.push(caseId);
        return repealValue;
    } else if (innerButtonId == "stepback_ok") {//指定回退
        var stepBackValue = new Array();
        var submitStyleAfterStepBack = null;
        if ($("#tome").attr("checked")) {
            submitStyleAfterStepBack = $("#tome").attr("value");
        }
        if ($("#rego").attr("checked")) {
            submitStyleAfterStepBack = $("#rego").attr("value");
        }
        if (stepBackTargetNodeId == null || submitStyleAfterStepBack == null) {
            stepBackValue.push(false);
        } else {
            stepBackValue.push(true);
        }
        stepBackValue.push(stepBackTargetNodeId);
        stepBackValue.push(submitStyleAfterStepBack);
        stepBackValue.push(stepBackTargetNodeName);

        //追述
        var _traceInput = parent.document.getElementById("trackWorkflow");
        var isTrace = 0;
        if (_traceInput) {
            isTrace = _traceInput.checked ? 1 : 0;
        }
        stepBackValue.push(isTrace);

        return stepBackValue;
    }
}

/**
 * @returnValue[0]存放的为flash返回的数据类型：process_desc_by
 * @returnValue[1]存放的为流程定义xml内容：process_xml
 * @returnValue[2]存放的为流程信息：workflowInfo
 * @returnValue[3]存放的为流程规则说明：ruleContent
 * @returnValue[4]存放的为 子流程设置信息：NewflowDIV
 * @returnValue[5]存放的为 流程事件信息：process_event
 */
var returnValue = new Array();
var resultFlag = true;

/**
 * @description:从flash中获得流程定义内容
 * @args 参数对象
 */
function submit(args) {
    args = decodeArgsFromFlash(args);
    var xml;
    if (typeof args == 'object') {
        xml = args;
    } else if (typeof args == 'string') {
        xml = args.split(",");
    }
    if (!xml || !xml[1] || xml[1] == "null") {
        if (__exporting == false) {
            showFlashAlert($.i18n('workflow.designer.selectWorkflow'));
        }
        resultFlag = false;
    } else if (document.getElementById("ruleContent") && document.getElementById("ruleContent").value.length > 2000) {
        showFlashAlert($.i18n('workflow.designer.ruleContentOverflow'));
        resultFlag = false;
    } else {
        resultFlag = true;
        returnValue = xml;
        var ruleContent = "";
        var NewflowDIV = "";
        if (scene == "0" || scene == "1") {
            if (document.getElementById("ruleContent")) {
                ruleContent = document.getElementById("ruleContent").value;//流程规则内容
            }
            if (document.getElementById("NewflowDIV")) {
                NewflowDIV = document.getElementById("NewflowDIV").innerText;//子流程设置信息
            }
        }
        returnValue.push(ruleContent);
        returnValue.push(NewflowDIV);
        returnValue.push($.toJSON(processEventObj));
    }
    if (resultFlag && _parent.saveWorkflowContent) {//如果是iframe内嵌desiger.do这种方式修改流程方式，则直接回调父页面的方法
        _parent.saveWorkflowContent(returnValue);
    }
}

var processXml_supervise = "";
var orginalReadyObjectJson_supervise = "";
var oldProcessLogJson_supervise = "";

function saveSuperviousWFCaseContent() {
    return wfAjax.saveModifyWorkflowData(currentProcesssId, caseId, appName, processXml_supervise,
        orginalReadyObjectJson_supervise, oldProcessLogJson_supervise);
}

function modifySuperviousWFCaseContent() {
    var monitorObj = _getFlashMonitor();
    if (monitorObj === false) {
        // flash没有启用
        return;
    }

    var modifySuperviousResult = new Array();
    modifySuperviousResult.push(monitorObj);
    modifySuperviousResult.push(initialize_processXml);
    modifySuperviousResult.push(initialize_caseLogXml);
    modifySuperviousResult.push(initialize_caseWorkitemLogXml);
    modifySuperviousResult.push(initialize_flashArgsXml);
    return modifySuperviousResult;
}

function notSpecCharWf(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");
    //修改[]之间的内容，其它部分不许修改
    if (/^[^\|\\"'<>]*$/.test(value)) {
        return true;
    } else {
        var msg = $.i18n('formValidate_specialCharacter', inputName);
        writeValidateInfoWf(element, msg);
        return false;
    }
}

/**
 * 打印出提示消息，并聚焦
 */
function writeValidateInfoWf(element, message) {
    $.alert(message);
    var onAfterAlert = element.getAttribute("onAfterAlert");
    if (onAfterAlert) {
        try {
            eval(onAfterAlert);
        } catch (e) {
        }
    } else {
        try {
            element.focus();
            element.select();
        } catch (e) {
        }
    }
};

/**
 * @description 确认编辑流程
 * @fromSentFlag 是否为已发送的流程
 * @userId 当前编辑流程的用户ID
 */
function saveWFContent() {
    var ruleContent = document.getElementById("ruleContent");
    if (ruleContent != null) {
        if (!notSpecCharWf(ruleContent)) {
            resultFlag = false;
            return true;
        }
    }


    var monitorObj = _getFlashMonitor();
    if (monitorObj === false) {
        // flash没有启用
        return;
    }

    //alert("monitorObj:="+monitorObj);
    setRelationDataToProcess(globalRelationDatas);
    if (monitorObj.submitDataXML) {
        monitorObj.submitDataXML();
    }

    /* if(fromSentFlag == "false"){
        var monitorObj = _getFlashMonitor();
        monitorObj.submitDataXML();
        var ruleObj = _parent.document.getElementById("workflowRule");
        if(ruleObj){
            ruleObj.value = document.getElementById("ruleContent").value;
        }
        var parentNFObj = _parent.document.getElementById("NewflowDIV");
        if(parentNFObj){
            parentNFObj.innerHTML = document.getElementById("NewflowDIV").innerHTML;
        }
    }else if(fromSentFlag == "true"){
        //提交修改后的流程数据
        var appName = "";
        if(_parent.appEnumStr){
            appName = _parent.appEnumStr;
        }else{
            appName = _parent.appName;
        }
        var currentProcessId = _parent.document.getElementById("processId").value;
        try{
            var requestCaller = new XMLHttpRequestCaller(this, "ajaxColManager", "saveModifyWorkflowData", false);
            requestCaller.addParameter(1, "String", currentProcessId);
            requestCaller.addParameter(2, "String", userId);
            requestCaller.addParameter(3, "String", _parent.summaryId);
            requestCaller.addParameter(4, "String", appName);
            requestCaller.serviceRequest();
        }catch(e){
            alert(e);
        }
        window.returnValue = true;
        window.close();
    } */
}

//设置子流程
function callNewflowSetting(args) {
    if (canAddSubProcess == "false") {
        $.alert($.i18n("workflow.label.dialog.cannotSetSubFlow.js")/*"当前状态下，无法设置子流程！"*/);
        _cancelDesignerCommand();
        return;
    }
    args = decodeArgsFromFlash(args);
    var nodeId = args[0];
    //var processXML = args[1];
    var templateId = paramProcessId;
//    var rs= wfAjax.isAutoSkipBeforeNewSetFlowOfNode(processXML,nodeId);
//    if(rs[0]=="TRUE" || rs[0]=="true"){//当前节点設置成了自動跳過,則不能再設置新流程,提示用戶
//      $.alert($.i18n("workflow.dealterm.skip.node.before.setnewflow"));
//      return;
//    }
    var result = wfAjax.hasMainProcess(templateId);
    if (result != null) {
        if (result == "true" || result == true) {
            $.alert($.i18n("subflow.tip.templeteIsAlreadyChild"));
            _cancelDesignerCommand();
            return;
        }
        var dialog = $.dialog({
            url: _ctxPath + '/workflow/subProcess.do?method=editSubProcessSetting&templateId=' + templateId + "&formId=" + formAppId + "&oldProcessTemplateId=" + oldProcessTemplateId,
            transParams: {
                "nodeId": nodeId
                , "subProcessJson": $("#NewflowDIV").text()
            },
            width: 660,
            height: 400,
            minParam: {show: false},
            maxParam: {show: false},
            closeParam: {
                show: true,
                autoClose: true,
                handler: function () {
                    _cancelDesignerCommand();
                }
            },
            title: $.i18n("subflow.setting.title"),
            buttons: [
                {
                    text: $.i18n("common.button.ok.label"),
                    isEmphasize: true,
                    handler: function () {
                        var result = dialog.getReturnValue();
                        if (result) {
                            try {
                                result = $.parseJSON(result);
                            } catch (e) {
                                result = {}
                            }
                            if (result.nodeId && result.subs.length > 0) {
                                var NewflowDIVObj = $("#NewflowDIV");
                                var subProcessJson = $.trim(NewflowDIVObj.html());
                                var subProcessObj = null;
                                try {
                                    subProcessObj = $.parseJSON(subProcessJson);
                                } catch (e) {
                                    subProcessObj = {}
                                }
                                if (result.subs && result.subs.length > 0) {
                                    subProcessObj[result.nodeId] = result.subs;
                                }
                                NewflowDIVObj.html($.toJSON(subProcessObj));
                                NewflowDIVObj = null;
                                callFlashAction("updateNewWorkflow", ["true"]);
                            }
                            dialog.close();
                        }
                    }
                },
                {
                    text: $.i18n("common.button.delete.label"),
                    handler: function () {
                        var NewflowDIVObj = $("#NewflowDIV");
                        var subProcessJson = $.trim(NewflowDIVObj.html());
                        var subProcessObj = null;
                        try {
                            subProcessObj = $.parseJSON(subProcessJson);
                        } catch (e) {
                            subProcessObj = {}
                        }
                        delete subProcessObj[nodeId];
                        var string = $.toJSON(subProcessObj);
                        if ('{}' == string) {
                            NewflowDIVObj.html("");
                        } else {
                            NewflowDIVObj.html(string);
                        }
                        NewflowDIVObj = null;
                        callFlashAction("updateNewWorkflow", ["false"]);
                        dialog.close();
                    }
                },
                {
                    text: $.i18n("common.button.cancel.label"),
                    handler: function () {
                        _cancelDesignerCommand();
                        dialog.close();
                    }
                }
            ],
            targetWindow: getCtpTop()
        });
    } else {
        _cancelDesignerCommand();
    }
}

var excludeElements = parseElements(accountExcludeElements);

/**
 * 添加流程节点
 */
function addNodeItem(args) {
    args = decodeArgsFromFlash(args);
    var nodeType;//节点类型
    var isTemplete = false;//是否为模版流程
    var appName;//应用类型
    var isAddBranchModel = false;//增加分支模式
    if (typeof args == 'object') {
        nodeType = args[0];
        isTemplete = args[1] == "true";
        appName = args[2];
        isAddBranchModel = args[3];
    } else if (typeof args == 'string') {
        var agrsArray = args.split(",");
        nodeType = agrsArray[0];
        isTemplete = agrsArray[1] == "true";
        appName = agrsArray[2];
        isAddBranchModel = agrsArray[3];
    }
    //alert("isTemplete:="+isTemplete+","+(typeof isTemplete));
    var unallowedSelectEmptyGroup = false;
    var myflowtype = "sequence";
    var isNeedCheckLevelScope = true;
    var showAllOuterDepartment = false;

    var _showFlowTypeRadio = window.selectPeopleTypes !== "WF_SUPER_NODE";
    var _maxSelectNum = _showFlowTypeRadio ? 100 : 1;

    isTemplete = (isTemplate == "true" || isTemplete);//模板流程
    if (isTemplete) {//串发
        myflowtype = "sequence";
        isNeedCheckLevelScope = false;
        showAllOuterDepartment = true;
    } else {//并发
        myflowtype = "concurrent";
        unallowedSelectEmptyGroup = true;
    }
    if (isAddBranchModel == "true") {
        myflowtype = "concurrent";
    }
    if (appName == 'edocSend' || appName == 'signReport' || appName == 'recEdoc' || appName == 'edoc' || appName == 'sendInfo' || appName == 'info') {//串发
        myflowtype = "sequence";
    }
    /* alert("nodeType:="+nodeType);
  alert("isTemplete:="+isTemplete);
  alert("appName:="+appName); */
    var hiddenColAssignRadio = false;
    if (nodeType == "StartNode" || nodeType == "SynNode" || isAddBranchModel == "true") {
        hiddenColAssignRadio = true;
    } else {
        hiddenColAssignRadio = false;
    }
    var tempFormId = formAppId, tempPanel = selectPeoplePanels;
    var selectPeopleObj = {
        /* mode:'open', */
        type: 'selectPeople',
        targetWindow: getCtpTop(),
        panels: selectPeoplePanels,
        selectType: selectPeopleTypes,
        showFlowTypeRadio: _showFlowTypeRadio,
        hiddenMultipleRadio: true,
        hiddenColAssignRadio: hiddenColAssignRadio,
        isConfirmExcludeSubDepartment: true,
        hiddenGroupLevel: true,
        unallowedSelectEmptyGroup: unallowedSelectEmptyGroup,
        isNeedCheckLevelScope: isNeedCheckLevelScope,
        showAllOuterDepartment: showAllOuterDepartment,
        flowtype: myflowtype,
        maxSize: _maxSelectNum,
        excludeElements: excludeElements,
        isCanSelectGroupAccount: false,
        params: {
            value: ''
        },
        callback: function (ret) {
            if (ret.obj) {

                if (_showFlowTypeRadio === false) {
                    // 超级节点，从新封装
                    var newObj = [];
                    newObj.push(ret.obj);
                    newObj.push("0");// 串发
                    dataToFlash(newObj, false);
                } else {
                    dataToFlash(ret.obj, false);
                }
            } else {
                _cancelDesignerCommand();
            }
        },
        canclecallback: function () {
            _cancelDesignerCommand();
        }
    }
    if (isCIPModel == 'true') {
        selectPeopleObj.extParameters = formAppId;
    } else if (tempPanel.indexOf("FormField") > -1 && tempFormId != "" && tempFormId != "-1" && tempFormId != "null" && tempFormId != "undefined") {
        selectPeopleObj.extParameters = formAppId + ",33330030";
    }
    $.selectPeople(selectPeopleObj);

}

/**
 * 替换流程节点
 */
function replaceNode(args) {
    args = decodeArgsFromFlash(args);
    var tempFormId = formAppId, tempPanel = selectPeoplePanels;
    var nodeType;//节点类型
    var isTemplete = false;//是否为模版流程
    var appName;//应用类型
    if (typeof args == 'object') {
        nodeType = args[0];
        isTemplete = args[1] == "true";
        appName = args[2];
    } else if (typeof args == 'string') {
        var agrsArray = args.split(",");
        nodeType = agrsArray[0];
        isTemplete = agrsArray[1] == "true";
        appName = agrsArray[2];
    }
    var unallowedSelectEmptyGroup = false;
    var isNeedCheckLevelScope = true;
    var showAllOuterDepartment = false;
    isTemplete = (isTemplate == "true" || isTemplete);
    if (!isTemplete) {
        unallowedSelectEmptyGroup = true;
    } else {
        isNeedCheckLevelScope = false;
        showAllOuterDepartment = true;
    }
    var selectPeopleObj = {
        type: 'selectPeople',
        /* mode:'open', */
        targetWindow: getCtpTop(),
        panels: selectPeoplePanels,
        selectType: selectPeopleTypes,
        showFlowTypeRadio: false,
        isConfirmExcludeSubDepartment: true,
        hiddenGroupLevel: true,
        unallowedSelectEmptyGroup: unallowedSelectEmptyGroup,
        isNeedCheckLevelScope: isNeedCheckLevelScope,
        showAllOuterDepartment: showAllOuterDepartment,
        excludeElements: excludeElements,
        isCanSelectGroupAccount: false,
        maxSize: 1,
        params: {
            value: ''
        },
        callback: function (ret) {
            //alert(ret.obj);
            if (ret.obj) {
                dataToFlash(ret.obj, true);
            } else {
                _cancelDesignerCommand();
            }
        },
        canclecallback: function () {
            _cancelDesignerCommand();
        }
    }
    if (tempPanel.indexOf("FormField") > -1 && tempFormId != "" && tempFormId != "-1" && tempFormId != "null" && tempFormId != "undefined") {
        selectPeopleObj.extParameters = formAppId + ",333300";
    }
    var ret = $.selectPeople(selectPeopleObj);
}

/**
 * 节点属性设置
 *
 * 2017-02-14节点权限设置页面打开回调方法（供协同和表单使用，该处args长度为42）-后续修改请维护该备注
 * 2017-10-08上节点匹配时，匹配最后一个处理人，args长度增加为43
 */
function propertyItem(args) {

    args = decodeArgsFromFlash(args);

    try {
        var propArr;
        if (typeof args == 'object') {
            propArr = args;
        } else if (typeof args == 'string') {
            propArr = args.split(",");
        }

        var formApp = "";//表单应用id
        var dealTerm = "";//处理期限
        var remindTime = "";//提醒时间
        var cycleRemindTime = "";//提醒时间


        var nodeName = propArr[0];//节点名称
        var policyName = propArr[1];//节点权限名称
        if (propArr[2] != null && propArr[2] != "null") {
            dealTerm = propArr[2];
        } else {
            dealTerm = 0;
        }
        if (propArr[3] != null && propArr[3] != "null") {
            remindTime = propArr[3];
        } else {
            remindTime = -1;
        }

        var processMode = propArr[4];
        if (propArr[5] != null && propArr[5] != "null" && propArr[5] != "undefined") {
            formApp = propArr[5];
        }
        var formViewOperation = propArr[6];//单据id
        var nodeType = propArr[7];
        var isEditor = propArr[8] == "true";
        var nodeId = propArr[9];
        var partyId = propArr[10];
        var partyType = propArr[11];
        var matchScope = propArr[12];
        var stateStr = propArr[13];
        var hasNewflow = propArr[14] == "true";
        var hasBranch = propArr[15] == "true";
        var formfiled = propArr[16] || "";
        var desc = propArr[17];
        var dealTermType = propArr[18];
        var dealTermUserId = propArr[19];
        var dealTermUserName = propArr[20];
        var isTemplete = propArr[21] == "true";
        //alert(desc);
        var appName = propArr[22];
        var isProIncludeChild = propArr[23] == "true";
        var scene = propArr[24];
        var hst = propArr[25];
        var hstv = propArr[26];
        var copyFrom = propArr[27];

        // 28 没有用
        var pasteTo = propArr[29];
        var rup = propArr[30];
        var pup = propArr[31];
        var na = propArr[32];
        var tolerantModel = propArr[33];
        var showSenderName = _parent.showSenderName;
        var queryIds = propArr[34];
        var statisticsIds = propArr[35];
        var rScope = propArr[36];
        var mu = propArr[37];
        var na_i = propArr[38];
        var na_b = propArr[39];

        cycleRemindTime = propArr[40];
        if (cycleRemindTime == null || cycleRemindTime == "null") {
            cycleRemindTime = -1;
        }

        if (queryIds == null || queryIds == "null") {
            queryIds = "";
        }
        if (statisticsIds == null || statisticsIds == "null") {
            statisticsIds = "";
        }
        var vjoin = propArr[41];
        var mergeDealType = propArr[42];
        var customName = propArr[43];
        var isOrderExcute = propArr[44];
        var hasCircleBranch = propArr[45];
        var hasAdvanceEvent = propArr[46];
    } catch (e) {
        showFlashAlert(e);
    }
    var isShowApplyToAll = (scene != "4" || scene != "5") && endFlag != "true";
    try {
        //alert(isTemplete);
        var result = selectPolicy(
            partyId,
            partyType,
            nodeName,
            policyName,
            dealTerm,
            remindTime,
            cycleRemindTime,
            processMode,
            matchScope,
            isTemplete,
            appName,
            formApp,
            formViewOperation,
            nodeType,
            isEditor,
            defaultPolicyId,
            showSenderName,
            stateStr,
            hasNewflow,
            hasBranch,
            isShowApplyToAll,
            formfiled,
            caseId,
            nodeId,
            desc,
            dealTermType,
            dealTermUserId,
            dealTermUserName,
            null,
            null,
            null,
            null,
            isProIncludeChild,
            scene,
            hst,
            hstv,
            copyFrom,
            rup,
            pup,
            na,
            tolerantModel,
            queryIds,
            statisticsIds,
            rScope,
            mu,
            na_i,
            na_b,
            vjoin,
            mergeDealType,
            customName,
            isOrderExcute,
            pasteTo,
            hasCircleBranch,
            hasAdvanceEvent
        );
    } catch (e) {
        alert(e);
    }
}

/**
 * 设置节点属性
 */
function selectPolicy(
    partyId,//节点上角色ID
    partyType,//节点上角 色名称
    nodeName,//节点名称
    policyName, //节点权限名称
    dealTerm, //节点上处理期限
    remindTime, //节点上提醒时间
    cycleRemindTime, //节点上多次提醒时间
    processMode, //节点上处理模式
    matchScope, //节点上匹配范围
    isTemplete, //是否为模版流程
    appName, //应用类型
    formApp, //表单应用ID
    formViewOperation, //单据ID
    nodeType,//节点类型
    isEditor, //是否可编辑
    defaultPolicyId,//默认节点权限ID
    showSenderName,//是否显示发起者名称
    nodeState, //节点状态
    hasNewflow, //是否有新流程
    hasBranch, //是否有分支
    isShowApplyToAll,//是否显示应用到所有节点
    formfiled,//表单字段
    caseId,//协同ID
    nodeId,//节点ID
    desc,//节点描述信息
    dealTermType,//处理期限到处理类型
    dealTermUserId,//转给指定人ID
    dealTermUserName,//转给指定人名称
    okFlag,//确定后的操作标志参数
    _processId,
    _activityId,
    _operationType,
    isProIncludeChild,
    scene,
    hst,
    hstv,
    copyFrom,
    rup,
    pup,
    na, tolerantModel,
    queryIds,
    statisticsIds,
    rScope,
    mu,
    na_i,
    na_b,
    vjoin,
    mergeDealType,
    customName,
    isOrderExecute,
    pasteTo,
    hasCircleBranch,
    hasAdvanceEvent
) {
    try {
        selectPolicyInner(getCtpTop(), partyId,//节点上角色ID
            partyType,//节点上角 色名称
            nodeName,//节点名称
            policyName, //节点权限名称
            dealTerm, //节点上处理期限
            remindTime, //节点上提醒时间
            cycleRemindTime, //节点上多次提醒时间
            processMode, //节点上处理模式
            matchScope, //节点上匹配范围
            isTemplete, //是否为模版流程
            appName, //应用类型
            formApp, //表单应用ID
            formViewOperation, //单据ID
            nodeType,//节点类型
            isEditor, //是否可编辑
            defaultPolicyId,//默认节点权限ID
            showSenderName,//是否显示发起者名称
            nodeState, //节点状态
            hasNewflow, //是否有新流程
            hasBranch, //是否有分支
            isShowApplyToAll,//是否显示应用到所有节点
            formfiled,//表单字段
            caseId,//协同ID
            nodeId,//节点ID
            desc,//节点描述信息
            dealTermType,//处理期限到处理类型
            dealTermUserId,//转给指定人ID
            dealTermUserName,//转给指定人名称
            okFlag,//确定后的操作标志参数
            _processId,
            _activityId,
            _operationType,
            isProIncludeChild,
            scene,
            hst,
            hstv,
            copyFrom,
            rup,
            pup,
            na,
            tolerantModel,
            queryIds,
            statisticsIds,
            rScope,
            mu,
            na_i,
            na_b,
            vjoin,
            mergeDealType,
            customName,
            isOrderExecute,
            pasteTo,
            hasCircleBranch,
            hasAdvanceEvent
        );
    } catch (e) {//兼容公文老代码
        selectPolicyInner(window.parent, partyId,//节点上角色ID
            partyType,//节点上角 色名称
            nodeName,//节点名称
            policyName, //节点权限名称
            dealTerm, //节点上处理期限
            remindTime, //节点上提醒时间
            cycleRemindTime, //节点上多次提醒时间
            processMode, //节点上处理模式
            matchScope, //节点上匹配范围
            isTemplete, //是否为模版流程
            appName, //应用类型
            formApp, //表单应用ID
            formViewOperation, //单据ID
            nodeType,//节点类型
            isEditor, //是否可编辑
            defaultPolicyId,//默认节点权限ID
            showSenderName,//是否显示发起者名称
            nodeState, //节点状态
            hasNewflow, //是否有新流程
            hasBranch, //是否有分支
            isShowApplyToAll,//是否显示应用到所有节点
            formfiled,//表单字段
            caseId,//协同ID
            nodeId,//节点ID
            desc,//节点描述信息
            dealTermType,//处理期限到处理类型
            dealTermUserId,//转给指定人ID
            dealTermUserName,//转给指定人名称
            okFlag,//确定后的操作标志参数
            _processId,
            _activityId,
            _operationType,
            isProIncludeChild,
            scene,
            hst,
            hstv,
            copyFrom,
            rup,
            pup,
            na,
            tolerantModel,
            queryIds,
            statisticsIds,
            rScope,
            mu,
            na_i,
            na_b,
            vjoin,
            mergeDealType,
            customName,
            isOrderExecute,
            pasteTo,
            hasCircleBranch,
            hasAdvanceEvent
        );
    }
}

var temp_process_xml = "";

function selectPolicyInner(tWindow, partyId,//节点上角色ID
                           partyType,//节点上角 色名称
                           nodeName,//节点名称
                           policyName, //节点权限名称
                           dealTerm, //节点上处理期限
                           remindTime, //节点上提醒时间
                           cycleRemindTime, //节点上多次提醒时间
                           processMode, //节点上处理模式
                           matchScope, //节点上匹配范围
                           isTemplete, //是否为模版流程
                           appName, //应用类型
                           formApp, //表单应用ID
                           formViewOperation, //单据ID
                           nodeType,//节点类型
                           isEditor, //是否可编辑
                           defaultPolicyId,//默认节点权限ID
                           showSenderName,//是否显示发起者名称
                           nodeState, //节点状态
                           hasNewflow, //是否有新流程
                           hasBranch, //是否有分支
                           isShowApplyToAll,//是否显示应用到所有节点
                           formfiled,//表单字段
                           caseId,//协同ID
                           nodeId,//节点ID
                           desc,//节点描述信息
                           dealTermType,//处理期限到处理类型
                           dealTermUserId,//转给指定人ID
                           dealTermUserName,//转给指定人名称
                           okFlag,//确定后的操作标志参数
                           _processId,
                           _activityId,
                           _operationType,
                           isProIncludeChild,//是否包含子部门
                           scene,
                           hst,
                           hstv,
                           copyFrom,
                           rup,
                           pup,
                           na,
                           tolerantModel,
                           queryIds,
                           statisticsIds,
                           rScope,
                           mu,
                           na_i,
                           na_b,
                           vjoin,
                           mergeDealType,
                           customName,
                           isOrderExecute,
                           pasteTo,
                           hasCircleBranch,
                           hasAdvanceEvent
) {
    var theHeight = 380;
    formfiled = formfiled || "";
    if (isTemplate == "true" || isTemplete) {
        if (nodeState == 1 || nodeState == "1") {
            theHeight += 50;
            if (partyType == 'Post') {
                theHeight += 30;
            }
        }
        if (formApp && formApp != 'undefined' && formApp != 'null' && formApp != '-1') {
            theHeight += 50;
        }
        //var monitorObj = _getFlashMonitor();
        //monitorObj.submitDataXMLForTermSkip(nodeId);
    }
    var tWindowHeight = tWindow.$("body").height();

    if (tWindowHeight <= 0) {
        tWindowHeight = $(getCtpTop()).height();
    }

    if (theHeight > tWindowHeight
        && (tWindowHeight > 0)) {
        theHeight = tWindowHeight - 100;
    }
    if (typeof (caseId) == "undefined") caseId = "-1";
    var isFormFieldRole = "false";
    var formRoleCode = "";
    if (partyId) {
        var index1 = partyId.indexOf("@");
        var index2 = partyId.indexOf("#");
        var index3 = partyId.lastIndexOf("#");

        if (index3 != -1 && index2 != index3) {
            formRoleCode = partyId.substr(index3 + 1);
            isFormFieldRole = "true";
        }
        if (index1 > -1 && index2 > -1) {
            partyId = partyId.substr(0, index2);
        }
    }
    var theWidth = 500;
    var theTitle = $.i18n('workflow.designer.selectPolicy.please.select');
    if (nodeName == 'split') {
        theHeight = 130;
        theWidth = 350;
        theTitle = $.i18n("workflow.matchResult.msg6");
    }
    var processEventStr = "";
    if (processEventObj) {
        processEventStr = processEventObj[nodeId];
    }


    //分支节点权限
    if ("split" == nodeName) {
        submitGetHandSelectOptions(nodeId, hst);
        if (hsbObj.isHasHandCodition == false) {
            $.alert(hsbObj.msg);
            _cancelDesignerCommand();
            return;
        }
    }


    var dialog = $
        .dialog({
            id: 'workflow_dialog_setWorkflowNodeProperty_Id',
            url: _ctxPath + "/workflow/designer.do?method=setWorkflowNodeProperty&partyId=" + escapeSpecialChar(encodeURIComponent(escape(partyId))) + "&partyType="
                + partyType + "&policyName=" + escapeSpecialChar(encodeURIComponent(escape(policyName)))
                + "&dealTerm=" + encodeURIComponent(dealTerm)
                + "&matchScope=" + matchScope
                + "&remindTime=" + encodeURIComponent(remindTime)
                + "&cycleRemindTime=" + encodeURIComponent(cycleRemindTime)
                + "&processMode=" + encodeURIComponent(processMode)
                + "&isTemplete=" + (isTemplate == "true" || isTemplete)
                + "&appName=" + encodeURIComponent(appName)
                + "&subAppName=" + encodeURIComponent(subAppName)
                + "&formApp=" + encodeURIComponent(formApp)
                + "&nodeType=" + encodeURIComponent(nodeType)
                + "&isEditor=" + encodeURIComponent(isEditor)
                + "&defaultPolicyId=" + escapeSpecialChar(encodeURIComponent(defaultPolicyId))
                + "&showSenderName=" + showSenderName
                + "&nodeState=" + nodeState
                + "&hasNewflow=" + hasNewflow
                + "&hasBranch=" + hasBranch
                + "&isShowApplyToAll=" + isShowApplyToAll
                + "&formfiled=" + escapeSpecialChar(encodeURIComponent(formfiled))
                + "&caseId=" + caseId
                + "&nodeId=" + nodeId
                + "&dealTermType=" + encodeURIComponent(dealTermType)
                + "&dealTermUserId=" + encodeURIComponent(dealTermUserId)
                + "&dealTermUserName=" + escapeSpecialChar(encodeURIComponent(dealTermUserName))
                + "&flowPermAccountId=" + flowPermAccountId + "&isProIncludeChild=" + isProIncludeChild
                + "&scene=" + scene + "&nodeName=" + escapeSpecialChar(encodeURIComponent(escape(nodeName)))
                + "&isFormFieldRole=" + isFormFieldRole + "&formRoleCode=" + formRoleCode + "&hst=" + hst + "&hstv=" + hstv
                + "&copyFrom=" + copyFrom
                + "&rup=" + rup
                + "&pup=" + pup
                + "&na=" + na
                + "&formViewOperation=" + encodeURIComponent(formViewOperation)
                + "&tolerantModel=" + tolerantModel
                + "&queryIds=" + queryIds
                + "&statisticsIds=" + statisticsIds
                + "&rScope=" + rScope
                + "&mu=" + mu
                + "&na_i=" + na_i
                + "&na_b=" + na_b
                + "&vj=" + vjoin
                + "&mergeDealType=" + mergeDealType
                + "&customName=" + encodeURIComponent(customName)
                + "&isOrderExecute=" + isOrderExecute
                + "&pasteTo=" + pasteTo
                + "&hasCircleBranch=" + hasCircleBranch
                + "&hasAdvanceEvent=" + hasAdvanceEvent
                + "&processId=" + processId,
            width: theWidth,
            height: theHeight,
            title: theTitle,
            targetWindow: tWindow,
            minParam: {show: false},
            maxParam: {show: false},
            transParams: {
                "processXml": temp_process_xml,
                "process_event": processEventStr,
                "desc": desc,
                "hsbObj": hsbObj
            },
            buttons: [{
                text: $.i18n('workflow.designer.page.button.ok'),
                isEmphasize: true,
                handler: function () {
                    var returnValueNodeProperty = dialog.getReturnValue({"innerButtonId": "ok"});
                    if (returnValueNodeProperty) {//目前长度已用到35

                        if (okFlag == 1) {  //类似流程管理：直接提交入库

                            var policyid = returnValueNodeProperty[0];
                            var policyName = returnValueNodeProperty[1];
                            var deadTerm = returnValueNodeProperty[2];
                            var remindTime = returnValueNodeProperty[3];
                            var processMode = returnValueNodeProperty[4];
                            //returnValueNodeProperty[5] 没用
                            var formViewOperation = returnValueNodeProperty[6];
                            //returnValueNodeProperty[7] 没用
                            var matchScope = returnValueNodeProperty[8];
                            //returnValueNodeProperty[9]没用
                            //returnValueNodeProperty[10]没用
                            var newFormField = returnValueNodeProperty[11];
                            var desc = returnValueNodeProperty[12];

                            var policyStr = new Array();

                            policyStr.push(policyid);//0
                            policyStr.push(policyName);//1
                            policyStr.push(deadTerm);//2
                            policyStr.push(remindTime);//3
                            policyStr.push(processMode);//4
                            policyStr.push(matchScope);//5
                            policyStr.push(formViewOperation);//6
                            if (desc) {
                                policyStr.push(desc);//7
                            } else {
                                policyStr.push("");//7
                            }
                            policyStr.push(returnValueNodeProperty[13]);//8
                            policyStr.push(returnValueNodeProperty[14]);//9
                            policyStr.push(returnValueNodeProperty[15]);//10
                            policyStr.push(returnValueNodeProperty[16]);//11
                            policyStr.push(returnValueNodeProperty[17]);//12
                            //returnValueNodeProperty[18]没用
                            //returnValueNodeProperty[19]没用
                            policyStr.push(newFormField);//13
                            policyStr.push(returnValueNodeProperty[20]);//14
                            policyStr.push(returnValueNodeProperty[21]);//15
                            policyStr.push(returnValueNodeProperty[22]);//16

                            //[23]：高级开发事件串，因为2个分支都要走，在下面一点设置。

                            policyStr.push(returnValueNodeProperty[24]);//17
                            policyStr.push(returnValueNodeProperty[25]);//18
                            policyStr.push(returnValueNodeProperty[26]);//19
                            policyStr.push(returnValueNodeProperty[27]);//20
                            policyStr.push(returnValueNodeProperty[28]);//21
                            policyStr.push(returnValueNodeProperty[29]);//mu //22
                            policyStr.push(returnValueNodeProperty[30]);//23
                            policyStr.push(returnValueNodeProperty[31]);//24
                            policyStr.push(returnValueNodeProperty[32]);//cycleRemindTime //25
                            policyStr.push(returnValueNodeProperty[33]);//mergeDealType //27(合并处理按节点设置:0-处理人就是发起人,1-处理人和上一步相同,2-处理人已处理过)
                            policyStr.push(returnValueNodeProperty[34]);//customName //28自定义节点名称
                            policyStr.push(returnValueNodeProperty[35]);//isOrderExecute
                            var flowProp = new Array();
                            flowProp.push(_processId);
                            flowProp.push(_activityId);
                            flowProp.push(_operationType);
                            var _result = null;
                            if (appName == "collaboration" || appName == "form") { //直接入库了！！
                                _result = updateFlash1(flowProp, policyStr, caseId, true);
                            } else {
                                _result = updateFlash1(flowProp, policyStr, caseId, false);
                            }
                            if (_result != null) {
                                var callbackResult = [_result[0], _result[1], _result[2]];
                                callFlashAction("edocRefresh_Flash", callbackResult);
                            }
                        } else {
                            callFlashAction("setProp", returnValueNodeProperty);
                        }


                        if (returnValueNodeProperty[23] == "") {
                            delete processEventObj[nodeId];
                        } else {
                            processEventObj[nodeId] = returnValueNodeProperty[23];
                        }

                        setTimeout(function () {
                            dialog.close();
                        }, 500);
                    }
                }
            }, {
                text: $.i18n('workflow.designer.page.button.cancel'),
                handler: function () {
                    _cancelDesignerCommand();
                    dialog.close();
                }
            }],
            closeParam: {
                show: true,
                autoClose: true,
                handler: function () {
                    _cancelDesignerCommand();
                }
            }
        });
}

/**
 * 设置为自动跳过时要先进行的校验
 */
function verifyTermSkip(args) {
    args = decodeArgsFromFlash(args);
    var xml;
    if (typeof args == 'object') {
        xml = args;
    } else if (typeof args == 'string') {
        xml = args.split(",");
    }
    temp_process_xml = xml[1];
}

function policyExplain() {
    try {
        policyExplainInner(getCtpTop());
    } catch (e) {//兼容公文老代码
        policyExplainInner(window.parent);
    }
}

function policyExplainInner(tWindow) {
    var dialog = $.dialog({
        url: _ctxPath + '/workflow/designer.do?method=policyExplain',
        transParams: window,
        width: 295,
        height: 275,
        title: $.i18n("node.policy.explain"),
        buttons: [
            {
                text: $.i18n("common.button.ok.label"),
                handler: function () {
                    dialog.transParams = null;
                    dialog.close();
                }
            }
        ],
        targetWindow: tWindow
    });
}


function showFlashAlert(args) {
    args = flashArgDecode(args);
    try {
        var alert = $.alert(args);
        //alert(args);
    } catch (e) {
        alert(args);
    }
}

var flashToolTip;

function showFlashTooltip(args) {
    return;
}

function showFlashTooltip_bak(args) {
    hideFlashTooltip("1");
    //ie7兼容设置
    if ($.browser.msie) {
        if ($.browser.version == "7.0") {
            $("#flashContainer").hide();
        }
    }

    try {
        var x = args[0];
        var y = args[1];
        var nodeName = args[2];
        var accountName = args[3];
        var partyType = args[4];
        var partyId = args[5];
        var sence = args[6];
        var myState = args[7];
        //alert("partyType:="+partyType+";partyId:="+partyId+";x:="+x+";y:="+y+";accountName:="+accountName+";myState="+myState+";sence:="+sence);
        if (showPeopleTip == "true") {
            if (sence == '2' || sence == '3' || sence == '6') {//查看模式
                if (sence == '6') {
                    y = y + 100;
                } else {
                    y = y + 60;
                }
                if (partyId == 'BlankNode') {

                } else if (myState == '1') {
                    var msgText1 = "<div class='font_size12'>";
                    msgText1 += args[2];
                    if (accountName != null && accountName != "" && accountName != "null") {
                        msgText1 += "(" + args[3] + ")";
                    } else {
                        msgText1 += "";
                    }
                    msgText1 += "</div>";
                    flashToolTip = $.tooltip({
                        width: 80,
                        openAuto: true,
                        openPoint: [x, y],
                        msg: msgText1,
                        direction: 'LT'
                    });
                    //ie7兼容设置
                    if ($.browser.msie) {
                        if ($.browser.version == "7.0") {
                            $("#flashContainer").show();
                        }
                    }
                } else if (partyType == 'user') {
                    var msgText1 = "<div class='font_size12'>";
                    msgText1 += args[2];
                    if (accountName != null && accountName != "" && accountName != "null") {
                        msgText1 += "(" + args[3] + ")";
                    } else {
                        msgText1 += "";
                    }
                    msgText1 += "</div>";
                    flashToolTip = $.tooltip({
                        width: 80,
                        openAuto: true,
                        openPoint: [x, y],
                        msg: msgText1,
                        direction: 'LT'
                    });
                }
            }
        }
    } catch (e) {
        alert(e.message);
    }
}

/**
 * 隐藏汽包提示信息框
 */
function hideFlashTooltip(args) {
    if (flashToolTip) {
        flashToolTip.close();
    }
}

/**
 * 设置处理期限到自动跳过时的校验方法：校验分支条件之前是否有自动跳过节点存在
 * @param linkid 分支id
 */
function verifyTermOfSkipBeforeBranch(linkid) {
    //var monitorObj = _getFlashMonitor();
    //monitorObj.submitDataXMLForTermSkipBeforeBranch(linkid);
}

/**
 * 设置为分支条件时要先进行的校验：分支前面是否有可以自动跳过的节点
 */
function verifyTermSkipBeforeBranch(args) {
    args = decodeArgsFromFlash(args);
    var xml;
    if (typeof args == 'object') {
        xml = args;
    } else if (typeof args == 'string') {
        xml = args.split(",");
    }
    temp_process_xml = xml[1];
}


//设置修改以后最新的DR
window.globalDRWindow = new Object();

var globalRelationDatas = new Object();
//原始的DR
var globalOrignalRelationDatas = new Object();

function relationData(args) {
    args = decodeArgsFromFlash(args);

    var activityId = args[0];
    var activityName = args[1];
    var policyId = args[2];
    var policyName = args[3];
    var nodeIdAndDRLists = args[4];
    var DR = args[5];

    var __processId = processId;
    if (__processId == '' || typeof (__processId) == 'undefined' || __processId == '-1' || __processId == -1) {
        __processId = "-1";
    }

    if (DR == '-1' || DR == null || DR == 'null' || typeof (DR) == 'undefined') {
        DR = '';
    }

    DR = getDR(activityId, DR);
    var cacheDRs = getAllCacheDRS();


    var nodeIdList = "";
    if (nodeIdAndDRLists != null) {
        var nodes = nodeIdAndDRLists.split(",");
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            var info = node.split("|");
            if (i == 0) {
                nodeIdList = info[0];
            } else {
                nodeIdList += "," + info[0];
            }
            globalOrignalRelationDatas[info[0]] = info[1];
        }
    }


    var url = _ctxPath + "/dataRelation.do?method=dataRelationConfig";
    url += "&activityId=" + activityId + "&processId=" + __processId + "&formAppId=" + formAppId
        + "&activityName=" + encodeURIComponent(activityName)
        + "&policyId=" + encodeURIComponent(policyId)
        + "&policyName=" + encodeURIComponent(policyName)
        + "&v=" + v
        + "&activityDataId=" + DR
        + "&cacheDRs=" + cacheDRs;
    if (activityId != "start") {
        url += "&nodeIdList=" + encodeURIComponent(nodeIdList)
    }


    openDRWindow(url, activityId);

    // openCtpWindow({'url':url,'id':activityId});

}

function getAllCacheDRS() {

    var cacheDRs = "";
    for (var i in globalRelationDatas) {

        if (cacheDRs == '') {
            cacheDRs = globalRelationDatas[i];
        } else {
            cacheDRs += "," + globalRelationDatas[i];
        }

    }
    return cacheDRs;
}

function openDRWindow(url, nodeId) {

    if (globalDRWindow._window && !globalDRWindow._window.closed) {
        alert($.i18n("workflow.label.dialog.existDataRealtionWin.js")/*"已经打开了一个关联数据的设置页面，请关闭以后再打开新的"*/);
        globalDRWindow._window.focus();
        return;
    }
    var width = parseInt(window.screen.availWidth) - 15;
    var height = parseInt(window.screen.availHeight) - 65;

    if ($.browser.safari) {
        height -= 30;
    }
    url += CsrfGuard.getUrlSurffix();
    var win = window.open(url, "_DRWINDOW",
        "top=0,left=0,scrollbars=yes,dialog=yes,minimizable=yes,modal=open,width="
        + width + ",height=" + height + ",resizable=yes");

    if (win == null) {
        return;
    }

    globalDRWindow._window = win;
    globalDRWindow._nodeId = nodeId;
    win.focus();
}

function onUnloadEvent() {
    if (globalDRWindow._window && !globalDRWindow._window.closed) {
        globalDRWindow._window.close();
    }
}

function getDR(activityId, dr) {
    var sdr = dr;
    if (dr != '') {
        var _dr = dr.split(",");
        if (_dr.length == 2) {
            sdr = _dr[1];
        }
    }

    return globalRelationDatas[activityId] == null ? sdr : globalRelationDatas[activityId];
}

/**
 * 数据关联设置页面保存方法
 * @param id
 */



function relationDataOK(relationDataNodes, win) {

    if (relationDataNodes != null) {
        for (var i = 0; i < relationDataNodes.length; i++) {
            var relationDataNode = relationDataNodes[i];
            globalRelationDatas[relationDataNode.activityId] = relationDataNode.activityDataId;
        }
    }
}

function setRelationDataToProcess() {
    var arr = new Array();
    for (var i in globalRelationDatas) {
        var obj = new Object();
        obj.activityId = i;
        var oldDR = globalOrignalRelationDatas[i] == '' ? globalRelationDatas[i] : globalOrignalRelationDatas[i];
        obj.activityDataId = oldDR + "," + globalRelationDatas[i];
        arr.push(obj);
    }

    var monitorObj = _getFlashMonitor();
    if (arr.length != 0) {
        monitorObj.setRelationDataToProcess(arr);
    }


    //Ajax更新Cache的NewId
    /*	var _updateObj = new Object();
	for(var i in globalRelationDatas){
		if(globalOrignalRelationDatas[i] != ''){
			_updateObj[globalOrignalRelationDatas[i]] = globalRelationDatas[i];
		}
	}

	var relationManager =  new dataRelationManager();
	relationManager.flashOkUpdate(_updateObj);*/
}

/**
 * 设置自动分支条件
 */
function autoOption(args) {
    args = decodeArgsFromFlash(args);
    var linkId = args[0];
    //verifyTermOfSkipBeforeBranch(linkId);
    //var rs= wfAjax.hasAutoSkipNodeBeforeSetCondition(temp_process_xml,linkId);
    //if(rs[0]=="TRUE" || rs[0]=="true"){//当前分支之前有设置自动跳过节点
    //$.alert(rs[1]);
    //return;
    //}
    var appName = args[1];
    var conditionId = args[2];
    var formCondition = args[3];
    var conditionTitle = args[4];
    var conditionType = args[5];
    var isForce = args[6];
    var conditionBase = args[7];
    var conditionDesc = args[8];
    var isCircle = "false";
    var submitStyle = "";
    if (args[9] != null && typeof (args[9]) != 'undefined') {
        isCircle = args[9]
    }
    if (args[10] != null && typeof (args[10]) != 'undefined') {
        submitStyle = args[10]
    }

    var isNew = 1;
    if ((conditionId != null && conditionId != undefined && conditionId.trim() != "" && conditionId.trim() != "null" && conditionId.trim() != "undefined")
        && (formCondition != null && formCondition != undefined && formCondition.trim() != "" && formCondition.trim() != "null" && formCondition.trim() != "undefined")
        && (conditionTitle != null && conditionTitle != undefined && conditionTitle.trim() != "" && conditionTitle.trim() != "null" && conditionTitle.trim() != "undefined")
        && (conditionBase != null && conditionBase != undefined && conditionBase.trim() != "" && conditionBase.trim() != "null" && conditionBase.trim() != "undefined")) {
        isNew = 0;
    }
    var dialog = $.dialog({
        url: _ctxPath + "/workflow/designer.do?method=showWorkflowBranchSettingPage&appName=" + appName + "&subAppName=" + subAppName + "&urlParams=" + urlParams + "&linkId=" + linkId + "&isNew=" + isNew + "&conditionBase=" + conditionBase + "&formApp=" + formAppId + "&wendanId=" + wendanId,
        width: 560,
        height: 520,
        title: $.i18n('workflow.formBranch.validate.16'),//"分支条件设置",
        minParam: {show: false},
        maxParam: {show: false},
        transParams: {
            "appName": appName,
            "linkId": linkId,
            "isNew": isNew,
            "conditionId": conditionId,
            "formCondition": formCondition,
            "conditionTitle": conditionTitle,
            "conditionType": conditionType,
            "isForce": isForce,
            "conditionBase": conditionBase,
            "conditionDesc": conditionDesc,
            "isCircle": isCircle,
            "submitStyle": submitStyle
        },
        targetWindow: getCtpTop(),
        closeParam: {
            show: true,
            autoClose: true,
            handler: function () {
                _cancelDesignerCommand();
            }
        },
        buttons: [{
            text: $.i18n('common.button.reset.label'),
            handler: function () {
                dialog.getReturnValue({"reset": true});
            }
        }, {
            text: $.i18n('workflow.designer.page.button.ok'),
            isEmphasize: true,
            handler: function () {
                var condition = dialog.getReturnValue({"innerButtonId": "ok"});
                //alert(condition);
                try {
                    condition = $.parseJSON(condition);
                } catch (e) {
                    condition = null;
                }
                if (condition) {
                    var tempCondition = condition[1];
                    if ("office" != appName) {
                        if (tempCondition.search(/{[^{}]+}/) > -1) {
                            //$.alert("系统忙，请重新设置分支条件或者点击取消！");
                            $.alert($.i18n('workflow.formBranch.validate.17'));
                            return;
                        }
                    }
                    if (condition[5] != true) {
                        //$.alert("校验失败，请重新设置分支条件或者点击取消！");
                        $.alert($.i18n('workflow.formBranch.validate.25'));
                        return;
                    }
                    //环形分支必须选择提交模式
                    if (isCircle == 'true' && condition[6] == '') {
                        $.alert($.i18n("workflow.branch.condition.select.alert.js"));
                        return false;
                    }
                    var callbackResult = [1, condition[1], condition[0], condition[4], condition[3], condition[2], condition[6]];
                    dialog.close();
                    callFlashAction("autoCondition", callbackResult);
                }
            }
        }, {
            text: $.i18n('workflow.designer.page.button.cancel'),
            handler: function () {
                _cancelDesignerCommand();
                dialog.close();
            }
        }]
    });
}

/**
 * 设置手动分支条件
 */
function handOption(args) {
    args = decodeArgsFromFlash(args);
    var linkId = args[0];
    //verifyTermOfSkipBeforeBranch(linkId);
    //var rs= wfAjax.hasAutoSkipNodeBeforeSetCondition(temp_process_xml,linkId);
    //if(rs[0]=="TRUE" || rs[0]=="true"){//当前分支之前有设置自动跳过节点
    //$.alert(rs[1]);
    //return;
    //}
    var id = "" + getUUID();
    var callbackResult = [2, "", id, ""];
    callFlashAction("handCondition", callbackResult);
}

/**
 * 删除分支条件
 */
function delOption(args) {
    args = decodeArgsFromFlash(args);
    var linkId = args[0];
    var conditionId = args[1];
    var conditionType = args[2] + "";
    if (
        (conditionId != null && conditionId != undefined && conditionId.trim() != "" && conditionId.trim() != "null" && conditionId.trim() != "undefined")
        && (conditionType != null && conditionType != undefined && conditionType.trim() != "" && conditionType.trim() != "null" && conditionType.trim() != "undefined")
    ) {
        var confirmTip = $.i18n('workflow.branch.confirmdelcondition');
        var confirm = $.confirm({
            'msg': confirmTip,
            ok_fn: function () {
                var monitorObj = _getFlashMonitor();
                monitorObj.delCondition(3, "", "", "", "");
            },
            cancel_fn: function () {
            }
        });
    } else {
        showFlashAlert($.i18n('workflow.branch.firstsetcondition'));
        return;
    }
}

/**
 * 删除分支条件
 */
function delCircleOption(args) {

    var confirmTip = $.i18n('workflow.branch.confirmdelcondition');
    var confirm = $.confirm({
        'msg': confirmTip,
        ok_fn: function () {
            var monitorObj = _getFlashMonitor();
            monitorObj.delCondition(3, "", "", "", "");
        },
        cancel_fn: function () {
        }
    });

}

/**
 * 更多条件
 */
function moreOption(args) {
    args = decodeArgsFromFlash(args);
    var linkId = args[0];
    var conditionId = args[1];
    var conditionDesc = args[2];
    if (conditionId == undefined || conditionId == null || conditionId == "" || conditionId == "null" || conditionId == "undefined") {
        showFlashAlert($.i18n('workflow.branch.firstsetcondition'));
        return;
    }
    conditionDesc = conditionDesc == null || conditionDesc == undefined ? "" : conditionDesc;
    var readonly = false;
    moreCondition(linkId, conditionId, conditionDesc, readonly);
}

/**
 * 弹出分支描述页面
 */
function moreCondition(linkId, conditionId, conditionDesc, readonly) {
    var title = $.i18n('workflow.branch.desc.label');
    if (readonly == true) {
        title = $.i18n('workflow.branch.desc');
    }
    var dialog = $
        .dialog({
            url: _ctxPath + "/workflow/designer.do?method=showWorkflowBranchMoreOption&linkId=" + linkId + "&readonly=" + readonly,
            width: 300,
            height: 200,
            title: title,
            transParams: {
                "linkId": linkId,
                "conditionId": conditionId,
                "conditionDesc": conditionDesc
            },
            targetWindow: window.parent,
            closeParam: {
                show: true,
                autoClose: true,
                handler: function () {
                    _cancelDesignerCommand();
                }
            },
            buttons: [{
                id: 'okButton',
                hide: readonly,
                text: $.i18n('workflow.designer.page.button.ok'),
                handler: function () {
                    var conditionDescContents = dialog.getReturnValue({"innerButtonId": "ok"});
                    if (conditionDescContents) {
                        var conditionDescContent =
                            conditionDescContents[0] == null
                            || conditionDescContents[0] == undefined
                            || conditionDescContents[0].trim() == "" ? "" : conditionDescContents[0];
                        if (conditionDescContent.trim() != "\t") {
                            var callbackResult = [linkId, conditionId, conditionDescContent];
                            callFlashAction("setConditionDesc", callbackResult);
                        }
                        dialog.close();
                    }
                }
            }, {
                id: 'cancelButton',
                text: $.i18n('workflow.designer.page.button.cancel'),
                handler: function () {
                    _cancelDesignerCommand();
                    dialog.close();
                }
            }]
        });
}

var defaultPanels = "Department,Team,Post";
var defaultSelectType = "Member,Post,Department,Team,Account";
var myAppName = appName;
if (myAppName == 'edocSend' || myAppName == 'sendEdoc' || myAppName == 'signReport' || myAppName == 'recEdoc' || myAppName == 'edocRec' || myAppName == 'sendInfo' || myAppName == 'info' || myAppName == 'edocSign' || myAppName == 'edoc' || myAppName == 'edocSupervise') {
    defaultPanels += ",Outworker,RelatePeople,JoinOrganization";
} else {
    defaultPanels += ",Outworker,RelatePeople,JoinOrganization";
}

/**
 * 督办：添加流程节点
 */
function edocAddNodeItem(args) {
    args = decodeArgsFromFlash(args);
    var arr = [];
    if (typeof args == 'object') {
        arr = args;
    } else if (typeof args == 'string') {
        arr = args.split(",");
    }
    var _processId = arr[0];
    var _activityId = arr[1];
    var canAddResult = wfAjax.validateCanAddNode(_processId, caseId, _activityId, processXml_supervise);
    if (canAddResult[0] == 'false') {
        $.alert($.i18n('workflow.designer.flashtip.unAN1'));
        return;
    }
    var _operationType = arr[2];
    var _currentNodeStateStr = arr[3]; //当前节点的处理状态 1-为分配 2-待办
    var nodeType = args[4];//节点类型
    var isTemplete = isTemplate == "true" || WorkflowUtil.context.isTemplete;//args[5]=="true";//是否为模版流程
    var appName = args[6];//应用类型
    var myflowtype = "sequence";
    var isNeedCheckLevelScope = true;
    var showAllOuterDepartment = false;
    if (isTemplete) {//串发
        myflowtype = "sequence";
        isNeedCheckLevelScope = false;
        showAllOuterDepartment = true;
    } else {//并发
        myflowtype = "concurrent";
    }
    if (appName == 'edocSend' || appName == 'signReport' || appName == 'recEdoc' || appName == 'edoc' || appName == 'sendInfo' || appName == 'info') {//串发
        myflowtype = "sequence";
    }
    var hiddenColAssignRadio = false;
    if (nodeType == "StartNode" || nodeType == "SynNode") {
        hiddenColAssignRadio = true;
    } else {
        hiddenColAssignRadio = false;
    }
    $.selectPeople({
        /* mode:'open', */
        type: 'selectPeople',
        targetWindow: getCtpTop(),
        panels: defaultPanels,
        selectType: defaultSelectType,
        showFlowTypeRadio: true,
        hiddenMultipleRadio: true,
        showOriginalElement: false,
        unallowedSelectEmptyGroup: true,
        hiddenColAssignRadio: hiddenColAssignRadio,
        hiddenGroupLevel: true,
        isConfirmExcludeSubDepartment: true,
        isNeedCheckLevelScope: isNeedCheckLevelScope,
        showAllOuterDepartment: showAllOuterDepartment,
        excludeElements: excludeElements,
        flowtype: myflowtype,//默认并发
        isCanSelectGroupAccount: false,
        params: {
            value: ''
        },
        callback: function (ret) {

            if (ret.obj) {
                var flowType = ret.obj[1];
                var superviseEelments = ret.obj;
                //当前是待办/暂存待办节点，会签一个节点，那么_operationType应该等同于在已办后面增加节点
                if (flowType == 3 && (_currentNodeStateStr == 2 || _currentNodeStateStr == 7 || _currentNodeStateStr == 61)) {
                    _operationType = 2;
                }
                superviseSelectPeopleCallBack(_processId, _activityId, _operationType, superviseEelments, "addNode");
            }
        }
    });
}

/**
 * 督办：ajax更新节点信息
 * @processId
 * @activityId
 * @operationType
 * @elements
 * @commandType
 * @manualSelectNodeId
 * @peopleArr
 * @summaryId
 * @conditions
 * @nodes
 * @iscol
 */
function updateFlash(processId, activityId, operationType, elements, commandType, manualSelectNodeId, peopleArr, conditions, nodes, iscol) {
    var rs = null;
    var str = "";
    var idArr = new Array();
    var typeArr = new Array();
    var nameArr = new Array();
    var accountIdArr = new Array();
    var accountShortNameArr = new Array();
    var selecteNodeIdArr = new Array();
    var _peopleArr = new Array();
    var conditionArr = new Array();
    var nodesArr = new Array();
    var userExcludeChildDepartmentArr = new Array();
    if (commandType == "addNode" || commandType == "replaceNode") {
        if (!elements) {
            return false;
        }
        var personList = elements[0] || [];
        var flowType = elements[1] || 0;
        var isShowShortName = elements[2] || "false";
        var process_desc_by = "people";
        str = processId + "," + activityId + "," + operationType + "," + flowType + "," + isShowShortName + "," + process_desc_by + "," + caseId;
        for (var i = 0; i < personList.length; i++) {
            var person = personList[i];
            idArr.push(person.id);
            typeArr.push(person.type);
            nameArr.push(person.name);
            accountIdArr.push(person.accountId);
            accountShortNameArr.push(person.accountShortname);
            userExcludeChildDepartmentArr.push(person.excludeChildDepartment + "");
            selecteNodeIdArr = [];
            _peopleArr = [];
        }
    } else if (commandType == "delNode") {
        str = processId + "," + activityId + "," + operationType + "," + null + "," + null + "," + null + "," + caseId;
        idArr = [];
        typeArr = [];
        nameArr = [];
        accountIdArr = [];
        accountShortNameArr = [];
        userExcludeChildDepartmentArr = [];
        if (manualSelectNodeId && peopleArr && manualSelectNodeId.length != 0 && peopleArr.length != 0) {
            selecteNodeIdArr = arrayToArray(manualSelectNodeId);
            _peopleArr = arrayToArray(peopleArr);
        } else {
            selecteNodeIdArr = [];
            _peopleArr = [];
        }
        if (conditions && conditions.length != 0) {
            conditionArr = arrayToArray(conditions);
            nodesArr = arrayToArray(nodes);
        } else {
            conditionArr = [];
            nodesArr = [];
        }
    }
    try {
        rs = wfAjax.changeCaseProcess(str, idArr, typeArr, nameArr, accountIdArr, accountShortNameArr, selecteNodeIdArr,
            _peopleArr, conditionArr, nodesArr, iscol, userExcludeChildDepartmentArr, processXml_supervise == "" ? initialize_processXml : processXml_supervise,
            orginalReadyObjectJson_supervise, oldProcessLogJson_supervise, defaultPolicyId, defaultPolicyName);
    } catch (ex1) {
        alert("Exception : " + ex1);
    }
    processXml_supervise = rs[0];
    orginalReadyObjectJson_supervise = rs[3];
    oldProcessLogJson_supervise = rs[4];
    return rs;
}

/**
 * 督办：删除节点
 */
function edocDelNodeItem(args) {
    args = decodeArgsFromFlash(args);
    var confirm = $.confirm({
        'msg': $.i18n("workflow.delete.confirm.label"),
        ok_fn: function () {
            var arr;
            if (typeof args == 'object') {
                arr = args;
            } else if (typeof args == 'string') {
                arr = args.split(",");
            }
            var _processId = arr[0];
            var _activityId = arr[1];
            var _operationType = arr[2];
            var matchPeople = arr[3];
            var peopleArr = new Array();
            var manualSelector = new Array();
            var conditions = new Array();
            var nodes = new Array();
            superviseSelectPeopleCallBack(_processId, _activityId, _operationType, null, "delNode");
        },
        cancel_fn: function () {
            _cancelDesignerCommand();
        }
    });
}


function removeNewfolw(nodeId) {
    try {
        var __nodeId = decodeArgsFromFlash(nodeId);

        if (globalDRWindow._window && !globalDRWindow._window.closed && globalDRWindow._nodeId == __nodeId) {
            globalDRWindow._window.close();
        }

    } catch (e) {

    }

}

/**
 * 督办:替换节点
 */
function edocReplaceNodeItem(args) {
    args = decodeArgsFromFlash(args);
    var arr = [];
    if (typeof args == 'object') {
        arr = args;
    } else if (typeof args == 'string') {
        arr = args.split(",");
    }
    var _processId = arr[0];
    var _activityId = arr[1];
    var _operationType = arr[2];
    var isNeedCheckLevelScope = true;
    var showAllOuterDepartment = false;
    if (isTemplate == "true") {
        isNeedCheckLevelScope = false;
        showAllOuterDepartment = true;
    }

    $.selectPeople({
        /* mode:'open', */
        type: 'selectPeople',
        targetWindow: getCtpTop(),
        panels: defaultPanels,
        selectType: defaultSelectType,
        showFlowTypeRadio: false,
        hiddenFlowTypeRadio: true,
        showOriginalElement: false,
        unallowedSelectEmptyGroup: true,
        isConfirmExcludeSubDepartment: true,
        isNeedCheckLevelScope: isNeedCheckLevelScope,
        showAllOuterDepartment: showAllOuterDepartment,
        hiddenGroupLevel: true,
        maxSize: 1,
        hiddenMultipleRadio: true,
        hiddenColAssignRadio: true,
        excludeElements: excludeElements,
        hiddenGroupLevel: true,
        isCanSelectGroupAccount: false,
        params: {
            value: ''
        },
        callback: function (ret) {

            if (ret.obj) {
                var superviseEelments = new Array();
                superviseEelments.push(ret.obj);
                superviseEelments.push(null);
                superviseEelments.push(null);
                if (ret.obj.length != 1) {
                    $.alert($.i18n('workflow.designer.flashtip.unRN2'));
                    return false;
                }
                superviseSelectPeopleCallBack(_processId, _activityId, _operationType, superviseEelments, "replaceNode");
            }
        }
    });
}

/**
 * 督办:公共回调函数
 */
function superviseSelectPeopleCallBack(_processId, _activityId, _operationType, superviseEelments, commandType) {
    var _result = null;
    if (appName == "collaboration" || appName == "form") {
        _result = updateFlash(_processId, _activityId, _operationType, superviseEelments, commandType, null, null, null, null, true);
    } else {
        _result = updateFlash(_processId, _activityId, _operationType, superviseEelments, commandType, null, null, null, null, false);
    }
    if (_result != null) {
        var callbackResult = [_result[0], _result[1], _result[2]];
        callFlashAction("edocRefresh_Flash", callbackResult);
    } else {
        _cancelDesignerCommand();
    }
}

/**
 * 督办:节点属性设置
 */
function setEdocPolicy(args) {//2017-02-14节点权限设置页面打开回调方法（供公文使用，该处args长度为45）-后续修改请维护该备注

    args = decodeArgsFromFlash(args);
    var propArr = [];
    if (typeof args == 'object') {
        propArr = args;
    } else if (typeof args == 'string') {
        propArr = args.split(",");
    }
    //args长度目前为45
    var result = null;
    try {
        var dealTerm = "";
        var remindTime = "";
        var cycleRemindTime = "";
        var excuteMode = "";
        var isEditor = "";
        var formApp = "";
        var filterInformPolicy = null;
        var hasBranch = false;

        var nodeName = propArr[0];
        var policyName = propArr[1];
        if (propArr[2] != null && propArr[2] != "null") {
            dealTerm = propArr[2];
        }
        if (propArr[3] != null && propArr[3] != "null") {
            remindTime = propArr[3];
        }
        processMode = propArr[4];
        if (propArr[5] != null && propArr[5] != "null" && propArr[5] != "undefined") {
            formApp = propArr[5];
        }
        var formViewOperation = propArr[6];
        //没有用 7
        isEditor = propArr[8];
        var nodeId = propArr[9];
        var _processId = propArr[10];
        var _activityId = propArr[11];
        var _operationType = propArr[12];
        var partyId = propArr[13];
        var partyType = propArr[14];
        var matchScope = propArr[15];
        var nodeState = propArr[16]; //当前节点的状态
        if (propArr[17] != "" && propArr[17] != null && propArr[17] != "null") {
            hasBranch = propArr[17] == "true";
        }
        if (propArr[17] == "supervise_pending") {
            filterInformPolicy = "inform";
        }
        //没有用18

        var desc = propArr[19];
        var dealTermType = propArr[20];
        var dealTermUserId = propArr[21];
        var dealTermUserName = propArr[22];
        var isTemplete = propArr[23] == "true";
        var appName = propArr[24];
        if (null != formViewOperation && formViewOperation != "null" && formViewOperation != undefined
            && formViewOperation != "undefined" && formViewOperation != "-1") {

            appName = "form";
        }
        var isProIncludeChild = propArr[25];
        var scene = propArr[26];
        var formfield = propArr[27];
        var hst = propArr[28];
        var hstv = propArr[29];
        var copyFrom = propArr[30];

        //没有用31
        var pasteTo = propArr[32];
        var rup = propArr[33];
        var pup = propArr[34];
        var na = propArr[35];
        var tolerantModel = propArr[36];
        var queryIds = propArr[37];
        var statisticsIds = propArr[38];
        var rScope = propArr[39];
        var mu = propArr[40];
        var na_i = propArr[41];
        var na_b = propArr[42];
        cycleRemindTime = propArr[43];
        var customName = propArr[44];

        //flash中没有传值过来，先定义变量，方法中占位
        var vjoin = "";
        var mergeDealType = "";
        var isOrderExecute = propArr[45];//是否开启多人按序执行
        var hasCircleBranch = propArr[46];//是否设置了环形分支
        var hasAdvanceEvent = propArr[47];//是否设置了高级事件
        if (queryIds == null || queryIds == "null") {
            queryIds = "";
        }
        if (statisticsIds == null || statisticsIds == "null") {
            statisticsIds = "";
        }
        result = selectPolicy(
            partyId, //节点上角色ID
            partyType,//节点上角 色名称
            nodeName,//节点名称
            policyName,//节点权限名称
            dealTerm,//节点上处理期限
            remindTime,//节点上提醒时间
            cycleRemindTime,//节点上多次提醒时间
            processMode,//节点上处理模式
            matchScope,//节点上匹配范围
            isTemplete,//是否为模版流程
            appName,//应用类型
            formApp,//表单应用ID
            formViewOperation,//表单操作权限
            null,//节点类型
            isEditor,//是否可编辑
            filterInformPolicy,//默认节点权限ID
            false, //是否显示发起者名称
            nodeState, //节点状态
            false, //是否有新流程
            hasBranch,//是否有分支
            null,//是否显示应用到所有节点
            formfield,//表单字段
            caseId,//协同ID
            nodeId,//节点ID
            desc,//节点描述信息
            dealTermType,//处理期限到处理类型
            dealTermUserId,//转给指定人ID
            dealTermUserName,//转给指定人名称
            1, _processId, _activityId, _operationType, isProIncludeChild, scene,
            hst, hstv,
            copyFrom,
            rup,
            pup,
            na,
            tolerantModel,
            queryIds,
            statisticsIds,
            rScope,
            mu,
            na_i,
            na_b,
            vjoin,
            mergeDealType,
            customName,
            isOrderExecute,
            pasteTo,
            hasCircleBranch,
            hasAdvanceEvent);
    } catch (e) {
        alert(e);
    }
}

/**
 * 督办：ajax更新流程节点属性信息
 */
function updateFlash1(flowProp, policyStr, caseId, iscol) {
    var rs = null;
    try {
        rs = wfAjax.changeCaseProcessNodeProperty(flowProp, policyStr, caseId, iscol,
            processXml_supervise == "" ? initialize_processXml : processXml_supervise,
            orginalReadyObjectJson_supervise, oldProcessLogJson_supervise);
    } catch (ex1) {
        alert("Exception : " + ex1);
    }
    processXml_supervise = rs[0];
    orginalReadyObjectJson_supervise = rs[3];
    oldProcessLogJson_supervise = rs[4];
    return rs;
}

function onClickToMe(type) {
    if (type == 1) {
        $("#tomeTipInfo").html("<span class=\"ico16 risk_16\"></span><font color='red'>" + $.i18n('workflow.special.stepback.label5') + "</font>");

        var traceSpanArea = parent.document.getElementById("traceSpanArea");
        if (traceSpanArea) {

            traceSpanArea.setAttribute("style", "display:none");
            var traceCheckBox = parent.document.getElementById("trackWorkflow");

            if (traceCheckBox && !traceCheckBox.disabled) {

                traceCheckBox.checked = false;
            }
        }
    } else {
        $("#tomeTipInfo").html("");

        var traceSpanArea = parent.document.getElementById("traceSpanArea");
        if (traceSpanArea) {
            traceSpanArea.setAttribute("style", "display:block");
        }
    }
}

var stepBackTargetNodeId = null;
var stepBackTargetNodeName = null;

/**
 * 指定回退：flash回调此函数
 */
function setStepBackTargetNode(args) {
    args = decodeArgsFromFlash(args);
    var currentSelectedNodeId = args[0];
    var currentSelectedNodeName = args[1];
    var currentStepbackNodeId = args[2];
    var nodePolicyId = args[3];
    var isBlankNode = args[4] == "true";
    var stepBackType = args[5];
    var rs = null;

    try {
        rs = wfAjax.validateCurrentSelectedNode(caseId, currentSelectedNodeId, currentSelectedNodeName, currentStepbackNodeId, initialize_processXml, permissionAccountId, configCategory, '');
    } catch (ex1) {
        alert("setStepBackTargetNode_Exception : " + ex1);
    }
    if (rs[0] == 'true') {
        if ((nodePolicyId == "zhihui" || nodePolicyId == "inform" || nodePolicyId == "vouch") || isBlankNode) {
            if (nodePolicyId == "zhihui" || nodePolicyId == "inform") {
                showFlashAlert($.i18n('workflow.special.stepback.alert9'));
            } else if (nodePolicyId == "vouch") {
                showFlashAlert($.i18n('workflow.special.stepback.alert10'));
            } else if (isBlankNode) {
                showFlashAlert($.i18n('workflow.special.stepback.alert9'));
            }
            rs[1] = "fail";
        } else if (stepBackType == "2") {
            //设置节点不可以回退
            showFlashAlert($.i18n('workflow.special.stepback.alert14.js'));
            rs[1] = "fail";
        } else {
            if (processState == 2 || processState == 3 || processState == 5 || stepBackCount > 0) {
                if (rs[2] == 'true' || rs[8] == 'true') {
                    // $("#rego").removeAttr("checked");
                    //$("#rego").removeAttr("checked");
                    //$("#tome").removeAttr("checked");
                    //$("#rego").attr("disabled", "disabled");
                    //$("#tome").attr("disabled", "disabled");
                    //$("#tomeTipInfo").html("");
                    //$("#theStepBackNodeInfoAction").show();
                    //$("#tomelabelId").show();
                    /* 流程处于【直接提交给我】状态，当前节点只能进行【直接提交给我】的指定回退操作，但当前节点与被选择节点之间存在分支条件或子流程，因此不能选择该节点进行指定回退操作！ */
                    showFlashAlert($.i18n('workflow.special.stepback.alert7'));
                    rs[1] = "fail";
                } else {
                    if (showToMe == "false") {
                        /* 多次指定回退状态下，提交方式只允许选择“提交给我”，该节点权限未配置此种提交方式，请联系管理员。 */
                        showFlashAlert($.i18n('workflow.special.stepback.alert8'));
                        rs[1] = "fail";
                    }
                    $("#theStepBackNodeInfoAction").show();
                    $("#tomelabelId").show();
                    $("#tome").removeAttr("disabled");
                    $("#tome").attr("checked", "checked");
                    $("#rego").removeAttr("checked");
                    $("#rego").attr("disabled", "disabled");
                    $("#theStepBackNodeTR").show();
                    _relayoutPage4StepBackTargetNode(true);
                    /* 已选择： */
                    $("#theStepBackNodeInfo").html($.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                    /* 处理后： */
                    $("#theStepBackNodeInfoAction").html(currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                    /* 已选择： */
                    $("#theStepBackNodeInfo").attr("title", $.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                    /* 处理后： */
                    $("#theStepBackNodeInfoAction").attr("title", currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                    if ($("#tome").attr("checked")) {
                        /* 此模式下被回退节点提交后将绕过中间已办节点，造成流程不严谨，请慎用！ */
                        $("#tomeTipInfo").html("<span class=\"ico16 risk_16\"></span><font color='red'>" + $.i18n('workflow.special.stepback.label5') + "</font>");
                    } else {
                        $("#tomeTipInfo").html("");
                    }
                    stepBackTargetNodeId = currentSelectedNodeId;
                    stepBackTargetNodeName = currentSelectedNodeName;
                }
            } else {
                if (rs[2] == 'true' || rs[8] == 'true') {
                    if (showReGo == "false") {
                        // showFlashAlert($.i18n('workflow.special.stepback.label12'));
                        $("#theStepBackNodeTR").show();
                        _relayoutPage4StepBackTargetNode(true);
                        /* 当前节点与回退节点间有触发子流程节点，只能选择流程重走，但当前节点的节点权限没有配置流程重走，请联系单位管理员进行调整。 */
                        $("#theStepBackNodeInfo").html("<span class=\"ico16 risk_16\"></span><font color='red'>" + $.i18n('workflow.special.stepback.label12') + "</font>");
                        $("#rego").attr("checked", "checked");
                        $("#tome").removeAttr("checked");
                        $("#rego").attr("disabled", "disabled");
                        $("#tome").attr("disabled", "disabled");
                        $("#tomeTipInfo").html("");
                        $("#theStepBackNodeInfoAction").hide();
                        $("#tomelabelId").hide();
                        rs[1] = "fail";
                    } else {
                        $("#theStepBackNodeInfoAction").show();
                        $("#tomelabelId").show();
                        $("#rego").attr("checked", "checked");
                        $("#tome").removeAttr("checked");
                        $("#rego").attr("disabled", "disabled");
                        $("#tome").attr("disabled", "disabled");
                        /* 当前节点与回退节点间有触发子流程节点，只能选择流程重走，不能选择提交给我模式！ */
                        $("#tomeTipInfo").html("<span class=\"ico16 risk_16\"></span><font color='red'>" + $.i18n('workflow.special.stepback.alert13') + "</font>");
                        $("#theStepBackNodeTR").show();
                        _relayoutPage4StepBackTargetNode(true);
                        /* 已选择： */
                        $("#theStepBackNodeInfo").html($.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                        /* 处理后： */
                        $("#theStepBackNodeInfoAction").html(currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                        /* 已选择： */
                        $("#theStepBackNodeInfo").attr("title", $.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                        /* 处理后： */
                        $("#theStepBackNodeInfoAction").attr("title", currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                        stepBackTargetNodeId = currentSelectedNodeId;
                        stepBackTargetNodeName = currentSelectedNodeName;
                    }
                } else {
                    $("#theStepBackNodeInfoAction").show();
                    $("#tomelabelId").show();
                    $("#rego").removeAttr("disabled");
                    $("#tome").removeAttr("disabled");
                    $("#theStepBackNodeTR").show();
                    _relayoutPage4StepBackTargetNode(true);
                    /* 已选择： */
                    $("#theStepBackNodeInfo").html($.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                    /* 处理后： */
                    $("#theStepBackNodeInfoAction").html(currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                    /* 已选择： */
                    $("#theStepBackNodeInfo").attr("title", $.i18n('workflow.special.stepback.label4') + currentSelectedNodeName);
                    /* 处理后： */
                    $("#theStepBackNodeInfoAction").attr("title", currentSelectedNodeName + $.i18n('workflow.special.stepback.label3'));
                    if ($("#tome").attr("checked")) {
                        /* 此模式下被回退节点提交后将绕过中间已办节点，造成流程不严谨，请慎用！ */
                        $("#tomeTipInfo").html("<span class=\"ico16 risk_16\"></span><font color='red'>" + $.i18n('workflow.special.stepback.label5') + "</font>");
                    } else {
                        $("#tomeTipInfo").html("");
                    }
                    stepBackTargetNodeId = currentSelectedNodeId;
                    stepBackTargetNodeName = currentSelectedNodeName;
                }
            }
        }
    } else {
        if (rs[14] == 'true') {
            showFlashAlert($.i18n('workflow.validate.stepback.msg15'));
        } else if (rs[13] == 'true') {
            showFlashAlert($.i18n('workflow.validate.stepback.msg13'));
        } else if (rs[12] == 'true') {
            showFlashAlert($.i18n('workflow.validate.stepback.msg4'));
        } else if (rs[11] == 'true') {
            showFlashAlert($.i18n('workflow.special.stepback.alert11'));
        } else if (rs[10] == 'true' && (nodePolicyId == "zhihui" || nodePolicyId == "inform" || nodePolicyId == "vouch" || isBlankNode)) {
            if (nodePolicyId == "zhihui" || nodePolicyId == "inform") {
                showFlashAlert($.i18n('workflow.special.stepback.alert9'));
            } else if (nodePolicyId == "vouch") {
                showFlashAlert($.i18n('workflow.special.stepback.alert10'));
            } else if (isBlankNode) {
                showFlashAlert($.i18n('workflow.special.stepback.alert9'));
            }
            rs[1] = "fail";
        } else {
            if (rs[3] == 'true') {// 有封发节点
                showFlashAlert($.i18n('workflow.special.stepback.alert1'));
            } else if (rs[4] == 'true') {// 有核定节点
                showFlashAlert($.i18n('workflow.special.stepback.alert2'));
            } else if (rs[15] == 'true') {//不允许回退
                showFlashAlert($.i18n('workflow.special.stepback.alert15.js'));
            } else if (rs[16] == 'true') {//不允跨超级节点指定回退
                showFlashAlert($.i18n('workflow.special.stepback.alert16.js'));
            } else if (rs[5] == 'true') {// 有表单审核节点
                showFlashAlert($.i18n('workflow.special.stepback.alert3'));
            } else if (rs[6] == 'true') {// 有子流程结束节点
                if (rs[9] == 'true') {
                    showFlashAlert($.i18n('workflow.special.stepback.alert4'));
                } else {
                    showFlashAlert($.i18n('workflow.special.stepback.alert5'));
                }
            } else if (rs[7] == 'true') {// 当前流程为新流程，不允许选择开始节点进行指定回退操作！
                showFlashAlert($.i18n('workflow.special.stepback.alert6'));
            }
        }
    }

    // 判断是否显示流程追述的按钮：区域可见 && 有流程重走的选项 && 流程重走被选中
    if ($("#theStepBackNodeTR").is(":visible") && $("#rego")[0] && $("#rego").attr("checked")) {
        var traceSpanArea = parent.document.getElementById("traceSpanArea");
        if (traceSpanArea) {
            traceSpanArea.setAttribute("style", "display:block");
        }
    }

    callFlashAction("setStepBackTargetNode", rs[1]);
    // }
}

function showStepBackMsg() {
    // if($("#tome").attr("disabled")=="disabled"){
    // showFlashAlert($.i18n('workflow.special.stepback.alert13'));
    // }
}

/**
 * 清除设置的值
 */
function clearStepBackTargetNode(args) {
    args = decodeArgsFromFlash(args);

    //还原页面设置
    var disableRego = processState == 2 || processState == 3 || processState == 5 || stepBackCount > 0;
    if (showReGo == "true") {
        if (!disableRego) {
            $("#rego").attr("checked", "checked");
            $("#tome").removeAttr("checked");
        }
    }

    $("#theStepBackNodeTR").hide();
    _relayoutPage4StepBackTargetNode(false);
    stepBackTargetNodeId = null;
    stepBackTargetNodeName = null;
}

var translateCacheObj = {};

function translateBranchFunction(args) {
    if (args.length >= 2) {
        var linkid = args[0];
        var branch = args[1];

        if (linkid == null || branch == null || branch == 'null') {
            return;
        }
        branch = flashArgDecode(branch);
        var monitorObj_2 = _getFlashMonitor();
        var title = translateCacheObj[linkid] || '';
        var branchOld = translateCacheObj["branch_" + linkid] || '';
        if (title == '' || branchOld != branch) {
            var formApp = '';
            var tempAppName = appName;
            if (formAppId != null && formAppId != "" && formAppId != "-1" && formAppId != "null" && formAppId != "undefined") {
                tempAppName = 'form';
                formApp = formAppId;
            } else if (wendanId != null && wendanId != "" && wendanId != "-1" && wendanId != "null" && wendanId != "undefined") {
                tempAppName = 'edoc';
                formApp = wendanId;
            } else if (tempAppName == '') {
                tempAppName = 'collaboration';
            }
            var result = wfAjax.branchTranslateBranchExpression(tempAppName, formApp, branch);
            if (result != null && result.length > 0) {
                if ("true" == result[0]) {
                    title = result[3];
                    translateCacheObj[linkid] = title;
                    translateCacheObj["branch_" + linkid] = branch;
                }
            }
        }
        if (isSVG) {
            return title;
        } else {
            // flash再绕一圈
            monitorObj_2.translateBranchFunction(title);
        }
    }
}

function submitGetHandSelectOptions(nodeid, hs_type) {
    var monitorObj_2 = _getFlashMonitor();
    monitorObj_2.getHandSelectOptions(nodeid, hs_type);
}

var hsbObj = {};
hsbObj.isHasHandCodition = false;
hsbObj.optionStr = "";

function getHandSelectOptions(args) {
    var xml;
    if (typeof args == 'object') {
        xml = args;
    } else if (typeof args == 'string') {
        xml = args.split(",");
    }
    xml = decodeArgsFromFlash(xml);
    if (!xml || !xml[1] || xml[1] == "null") {
        //alert(v3x.getMessage("collaborationLang.collaboration_selectWorkflow"));
    } else {
        try {
            var rs = wfAjax.getHandSelectOptions(xml[1], xml[3], xml[4]);
            if (rs != null && (rs[0] == "TRUE" || rs[0] == "true")) {//当前节点之后有设置shoudong分支
                hsbObj.isHasHandCodition = true;
                hsbObj.optionStr = rs[1];
            } else {//当前节点之后mei有设置shoudong分支
                hsbObj.isHasHandCodition = false;
                hsbObj.msg = rs[1];
            }
        } catch (ex1) {
            alert("Exception : " + ex1);
            return;
        }
    }
}

/*
 * 完成流程编辑
 */
function finish() {
    wfAjax.releaseWorkflow(currentProcesssId, currentUserId);
    window.close();
}

function saveSuperviousContent() {
    var rs = saveSuperviousWFCaseContent();
    if (rs[0] == 'false') {
        alert(rs[1]);
        return;
    }
    wfAjax.releaseWorkflow(currentProcesssId, currentUserId);
    window.close();
}

/**
 * 校验当前节点是否可以进行粘贴替换——flash回调方法
 */
function isCanPasteAndReplaceNode(args) {
    args = decodeArgsFromFlash(args);
    var copyNodeId = args[0];
    var currentNodeId = args[1];
    var processXml = args[2];
    var rs = null;
    try {
        rs = wfAjax.isCanPasteAndReplaceNode(copyNodeId, currentNodeId, processXml);
    } catch (ex1) {
        alert("Exception : " + ex1);
    }
    if (rs == true) {
        callFlashAction("pasteAndReplaceNode");
    } else {
        _cancelDesignerCommand();
        alert($.i18n('workflow.validate.pastereplace.msg'));
    }
}

/**
 * fixed:OA-54853 firefox下编辑流程时，滚动鼠标时，流程图无法放大缩小
 * 对特定浏览器的鼠标滚动事件进行处理
 */
function addMouseWheelListener() {
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {//
        if ($.browser.msie) {//ie

        } else if ($.browser.chrome) {//chrome

        } else if ($.browser.opera) {//opera

        } else if ($.browser.mozilla) {//firefox
            monitorObj.addEventListener('DOMMouseScroll', mouseWheelFireFox, false);
        } else if ($.browser.safari) {//safari
            monitorObj.addEventListener('mousewheel', mouseWheelFireFox, false);
        }
    }
}

/**
 * 响应事件的方法
 */
function mouseWheelFireFox(event) {
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        //Flash给JS提供的接口
        var detal = 0;
        if ($.browser.msie) {//ie

        } else if ($.browser.chrome) {//chrome

        } else if ($.browser.opera) {//opera

        } else if ($.browser.mozilla) {//firefox
            detal = event.detail;
        } else if ($.browser.safari) {//safari
            detal = event.wheelDelta / 40;
        }
        //alert(1+"       "+detal);
        monitorObj.mouseWheelHandler(detal);
    }
}

/**
 * 滚动条模式
 */
function showScrollModeWorkflowDiagram() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        monitorObj.showScrollModeWorkflowDiagram();
    }
    $("#scrollModeWorkflowDiagram").attr("checked", "checked");
    $("#scrollModeWorkflowDiagram").attr("disabled", "disabled");
    $("#dragModeDiagramWorkflowDiagram").removeAttr("checked");
    $("#dragModeDiagramWorkflowDiagram").removeAttr("disabled");
    $("#dragModeDiagramWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_drag.png");
    $("#scrollModeWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_scroll_checked.png");
    //$("#showResetWorkflowDiagram").css("display","none");
    $("#showWorkflowBigger").css("display", "none");
    $("#showWorkflowSmaller").css("display", "none");
}

/**
 * 拖拽模式
 */
function showDragModeWorkflowDiagram() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        monitorObj.showDragModeWorkflowDiagram();
    }
    $("#dragModeDiagramWorkflowDiagram").attr("checked", "checked");
    $("#dragModeDiagramWorkflowDiagram").attr("disabled", "disabled");
    $("#scrollModeWorkflowDiagram").removeAttr("checked");
    $("#scrollModeWorkflowDiagram").removeAttr("disabled");
    //$("#showResetWorkflowDiagram").css("display","block");
    $("#dragModeDiagramWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_drag_checked.png");
    $("#scrollModeWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_scroll.png");
    $("#showWorkflowBigger").css("display", "block");
    $("#showWorkflowSmaller").css("display", "block");
}

/**
 * 复位
 */
function showResetWorkflowDiagram() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        monitorObj.showResetWorkflowDiagram();
    }
}

/**
 * 放大流程图
 */
function showWorkflowBiggerFunction() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        monitorObj.mouseWheelHandler(-3, svgDesigner);
    }
}

/**
 * 缩小流程图
 */
function showWorkflowSmallerFunction() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        monitorObj.mouseWheelHandler(3, svgDesigner);
    }
}

/**
 * 导出流程图
 */
var __exporting = false;

function exportWorkflowDiagramFunc() {
    if (__exporting == true) {
        $.alert($.i18n("workflow.export.exporting"));
        return;
    }
    __exporting = true;
    saveWFContent();
    if (resultFlag == false) {
        __exporting = false;
        $.alert($.i18n("workflow.export.cannotExport"));
        return;
    }
    var processXML = returnValue[1];
    /*
    OA-77497先设置分支，再修改表单字段类型。导出流程图的时候，导出来的是修改前的
    经刘丹确认，不再添加校验
	var temp = window.dialogArguments;
	if(temp!=null && temp.isClone==true){
		var validateResult = wfAjax.validateFormTemplate(temp.appName, temp.formApp, processXML, "");
		if(validateResult==null||validateResult.length<2){
			__exporting = false;
            $.alert("校验失败，请重新点击确定按钮！");
            return;
        }
        if("false"==validateResult[0]){
            //$.alert("流程中仍旧有不合法的地方，请重新设置！");
            //var xmlRoot = $("<xml>"+validateResult[1]+"</xml>");
            var xmlRoot = $($.parseXML(validateResult[1].substr(38)));
            var nodeFalses = xmlRoot.find("n[s=false]");
            var valiResult = true, text="";
            if(nodeFalses.size()>0){
                loopFlag1 : for(var index=0, len=nodeFalses.size(); index<len; index++){
                    var tempNode = nodeFalses.eq(index);
                    for(var i=0; i<5; i++){
                        if('false'==tempNode.find("r"+i).text()){
                            valiResult = false;
                            text = tempNode.find("r"+i+"msg").text();
                            break loopFlag1;
                        }
                    }
                }
            }
            var linkFalses = xmlRoot.find("l[s=false]");
            if(valiResult && linkFalses.size()>0){
                loopFlag2 : for(var index=0, len=linkFalses.size(); index<len; index++){
                    var tempLink = linkFalses.eq(index);
                    if('false'==tempLink.find("r0").text()){
                        valiResult = false;
                        text = tempLink.find("r0msg").text();
                        break loopFlag2;
                    }
                }
            }
            xmlRoot = nodeFalses = linkFalses = tempNode = tempLink = null;
            if(!valiResult){
                __exporting = false;
                //$.alert("流程图设置有误，请修改后再导出！");
                $.alert($.i18n("workflow.export.cannotex2")+text);
                return;
            }
        }
	}
	temp = null;
	*/
    var formObj = $("#downLoadForm");
    formObj.attr("action", _ctxPath + "/workflow/cie.do");
    formObj.find("input[name=data]").val(processXML);
    formObj.submit();
    formObj = null;
    var index = 0;
    var temp = window.setInterval(function () {
        index++;
        var iframeState = $("#downLoadIframe").contents()[0].readyState;
        if (index > 60 || iframeState == "complete" || iframeState == "interactive") {
            __exporting = false;
            window.clearInterval(temp);
        }
    }, 1000);
}

/**
 * 同步更改工作页面菜单面板的选项状态
 * @param args
 */
function changeHtmlWorkflowMenuPanel(args) {
    var workflowDisplayMode = args[0];//流程图显示模式：0为拖拽模式,1为滚动条模式
    var menuOperationType = args[1];//鼠标右键菜单操作类型：0为复位,1为按窗口大小显示,2为按实际大小显示,3缩放中
    if (workflowDisplayMode == "0") {
        $("#dragModeDiagramWorkflowDiagram").attr("checked", "checked");
        $("#dragModeDiagramWorkflowDiagram").attr("disabled", "disabled");
        $("#scrollModeWorkflowDiagram").removeAttr("checked");
        $("#scrollModeWorkflowDiagram").removeAttr("disabled");
        //$("#showResetWorkflowDiagram").css("display","block");
        $("#showWorkflowBigger").css("display", "block");
        $("#showWorkflowSmaller").css("display", "block");
        $("#dragModeDiagramWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_drag_checked.png");
        $("#scrollModeWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_scroll.png");
    } else if (workflowDisplayMode == "1") {
        $("#scrollModeWorkflowDiagram").attr("checked", "checked");
        $("#scrollModeWorkflowDiagram").attr("disabled", "disabled");
        $("#dragModeDiagramWorkflowDiagram").removeAttr("checked");
        $("#dragModeDiagramWorkflowDiagram").removeAttr("disabled");
        //$("#showResetWorkflowDiagram").css("display","none");
        $("#showWorkflowBigger").css("display", "none");
        $("#showWorkflowSmaller").css("display", "none");
        $("#dragModeDiagramWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_drag.png");
        $("#scrollModeWorkflowDiagram").attr("src", _ctxPath + "/common/images/mode_scroll_checked.png");
    }
    if (menuOperationType == "1") {
        $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_actual.png");
        $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.actual.title'));
        $("#twoSeeMode").attr("modeType", "0");
    } else if (menuOperationType == "2") {
        $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_window.png");
        $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.window.title'));
        $("#twoSeeMode").attr("modeType", "1");
    } else if (menuOperationType == "3") {
        var modeType = $("#twoSeeMode").attr("modeType");
        if (modeType == "1") {
            $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_actual.png");
            $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.actual.title'));
            $("#twoSeeMode").attr("modeType", "0");
        } else {
            $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_window.png");
            $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.window.title'));
            $("#twoSeeMode").attr("modeType", "1");
        }
    }
}

/**
 *
 */
function toggleTwoSeeMode() {
    if (!isInstallFlashPlugin()) {
        alert($.i18n('workflow.designer.unInstallFlash.label'));//您的浏览器未安装Flash插件，请先安装！");
        return;
    }
    var modeType = $("#twoSeeMode").attr("modeType");
    var monitorObj = _getFlashMonitor();
    if (monitorObj) {
        if (modeType == "1") {
            $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_actual.png");
            $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.actual.title'));
            $("#twoSeeMode").attr("modeType", "0");
            monitorObj.showWorkflowDiagramByWindowSize(svgDesigner);
        } else {
            $("#twoSeeMode").attr("src", _ctxPath + "/common/images/size_window.png");
            $("#twoSeeMode").attr("title", $.i18n('workflow.designer.menupanel.size.window.title'));
            $("#twoSeeMode").attr("modeType", "1");
            monitorObj.showWorkflowDiagramByActualSize(svgDesigner);
        }
    }
}

/**
 * 导入流程xml
 */
function importWorkflowDiagram() {
    var dialog = $.dialog({
        url: _ctxPath + '/workflow/cie.do?method=importProcessPre',
        width: 800,
        height: 300,
        title: 'Import XML',
        buttons: [
            {
                text: $.i18n('common.button.ok.label'),
                handler: function () {
                    try {
                        var result = dialog.getReturnValue();
                        if (result != null) {
                            if (result != "importStart") {
                                result = $.parseJSON(result);
                                var xml = result.process_xml;
                                if (result.checkForm) {
                                    if (result.nodes && result.nodes.length > 0) {
                                        var wfAjax = new WFAjax();
                                        var res = wfAjax.validateFailReSelectPeople(result.process_xml, result.nodes);
                                        xml = res.process_xml;
                                    }
                                    $("input[name=templateId]").val(result.templateId);
                                    $("#process_xml").val(xml);
                                    dialog.close();
                                }
                            }
                        }
                    } catch (e) {
                        $.alert(e.message);
                    }
                }
            },
            {
                text: $.i18n('common.button.cancel.label'),
                handler: function () {
                    dialog.close();
                }
            }
        ],
        targetWindow: getCtpTop()
    });
}

function openEventAdvancedSetting() {
    var processEventStr = "";
    if (processEventObj) {
        processEventStr = processEventObj["global"];
    }
    var dialog = $.dialog({
        id: "workflow_dialog_advancedSetting_id",
        url: _ctxPath + "/workflow/designer.do?method=advancedSetting&appName=" + appName + "&from=global&processId=" + processId,
        title: $.i18n('workflow.advance.event.name'), //"开发高级"
        width: 900,
        height: 400,
        transParams: {
            "process_event": processEventStr
        },
        targetWindow: getCtpTop(),
        buttons: [{
            text: $.i18n("common.button.ok.label"),
            id: "workflowEventAdvancedSetting",
            handler: function () {
                var returnValue = dialog.getReturnValue();
                if (returnValue == "error") {
                    return;
                }
                processEventObj["global"] = returnValue;
                dialog.close();
            }
        }, {
            text: $.i18n("common.button.delete.label"),
            handler: function () {
                delete processEventObj["global"];
                dialog.close();
            }
        }, {
            text: $.i18n("common.button.cancel.label"),
            id: "exit",
            handler: function () {
                dialog.close();
            }
        }]
    });
}

function escapeSpecialChar(str) {
    if (!str) {
        return str;
    }
    str = str.replace(/\&/g, "&amp;").replace(/\</g, "&lt;").replace(/\>/g, "&gt;").replace(/\"/g, "&quot;");
    str = str.replace(/\'/g, "&#039;").replace(/"/g, "&#034;");
    return str;
}


function isNewBrowser() {
    var ret = false;
    if ($.browser.msie) {
        if (parseInt($.browser.version) >= 11) {
            ret = true;
        } else {
            var br = navigator.userAgent.toLowerCase();
            var browserVer = (br.match(/.+(?:rv)[\/: ]([\d.]+)/) || [0, '0'])[1];
            if (parseInt(browserVer) >= 11) {
                ret = true;
            }
        }
    } else {
        ret = true;
    }
    return ret;
}

function _onloadFunc() {
    //更新流程锁的时长  30s执行一次
    if (addWorkflowExpirationTimeLock && addWorkflowExpirationTimeLock == "true") {
        setInterval("activeLockTime()", 30 * 1000);
    }
}

function activeLockTime() {
    try {
        wfAjax.updateLockExpirationTime(processId, currentUserId, action, "pc");
    } catch (e) {
        alert(e.message);
    }
}


function showWFTemplateList4Clone() {
    var myUrl = _ctxPath + '/workflow/designer.do?method=showWorkflowTemplateList4Clone';
    myUrl = myUrl + '&formAppId=' + formAppId + '&appName=' + appName + "&currentUserId=" + currentUserId;
    myUrl = myUrl + '&processId=' + oldProcessTemplateId;

    var dialog = $.dialog({
        url: myUrl,
        title: $.i18n('workflow.copy.title.js'),
        targetWindow: getCtpTop(),
        width: 800,
        height: 500,
        buttons: [{
            text: $.i18n('common.button.ok.label'),
            id: "sure",
            isEmphasize: true,
            handler: function () {
                var re = dialog.getReturnValue();
                if (re != null && re.length == 1) {
                    if (!$.browser.msie) {
                        dialog.close();
                    } else {
                        dialog.hideDialog();
                    }
                    if (typeof (cloneWorkflowFunc) == "function") {
                        cloneWorkflowFunc(re[0].workflowId);
                    } else if (typeof (parent.cloneWorkflowFunc) == "function") {
                        parent.cloneWorkflowFunc(re[0].workflowId);
                    }

                }
            }
        }, {
            text: $.i18n('common.button.cancel.label'),
            id: "exit",
            handler: function () {
                if (!$.browser.msie) {
                    dialog.close();
                } else {
                    dialog.hideDialog();
                }
            }
        }]
    });
}

function showCloneTemplateWorkflowProcessXml() {
    var cloneResult = wfAjax.selectProcessTemplateXMLForClone(appName, processId, formApp, oldWendanId || "-1", wendanId || "-1", "{}");
    if (cloneResult == null || cloneResult.length < 2) {
        $.alert($.i18n("workflow.label.dialog.cloneFailure.js")/* "克隆失败，请重新选择要克隆的模版！" */);
    }
    initialize_processXml = cloneResult[0];
    verifyResultXml = cloneResult[1];
    //复制的时候需要将子流程信息清空
    subProcessSettingJson = "";
    processEventJson = "";
    processEventObj = new Object();
    processEventJson = "";
    processEventObj = new Object();

    $("#NewflowDIV").html("");
}

var hasPolicyId = false;

function hasPolicy(policyId) {
    var monitorObj = _getFlashMonitor();
    if (monitorObj.hasPolicy) {
        var ret = monitorObj.hasPolicy(policyId);
        if (isSVG) {
            return ret;
        } else {
            // flash 还要绕一圈， 到 putHasPolicy 修改 hasPolicyId 的值
        }
    }
    return hasPolicyId;
}

function putHasPolicy(args) {
    hasPolicyId = decodeArgsFromFlash(args);
}

/**
 * 从流程图中获取所有的超级节点的名字,供CIP集成使用
 */
var wfSuperNodeNames = new Array();

function getAllWFSuperNodeNames() {
    var monitorObj = _getFlashMonitor();
    if (monitorObj && monitorObj.getAllWFSuperNodeNames) {
        if (isSVG) {
            wfSuperNodeNames = monitorObj.getAllWFSuperNodeNames();
        } else {
            //flash走putWFSuperNodeNames逻辑设置值
        }
    }
    return wfSuperNodeNames;
}

function putWFSuperNodeNames(names) {
    wfSuperNodeNames = decodeArgsFromFlash(names);
}


/**
 * 从流程图中获取所有的超级节点的id,供CIP集成使用
 */
var wfSuperNodePartyIds = new Array();

function getAllWFSuperNodePartyIds() {
    var monitorObj = _getFlashMonitor();
    if (monitorObj && monitorObj.getAllWFSuperNodePartyIds) {
        if (isSVG) {
            wfSuperNodePartyIds = monitorObj.getAllWFSuperNodePartyIds();
        } else {
            //flash走putWFSuperNodePartyIds逻辑设置值
        }
    }
    return wfSuperNodePartyIds;
}

function putWFSuperNodePartyIds(ids) {
    wfSuperNodePartyIds = decodeArgsFromFlash(ids);
}


/** 获取flash操作对象 **/
function _getFlashMonitor() {

    if (isSVG) {
        return window.svgMonitorObj;
    } else {
        // flash
        if (!isInstallFlashPlugin()) {
            $.alert($.i18n('workflow.designer.unInstallFlash.label'));// 您的浏览器未安装Flash插件，请先安装！");
            return false;
        }

        return isInternetExplorer ? document.all.monitor : document.monitor;
    }
}

/**
 * 调整窗口大小
 * @returns
 */
function _windowResize() {

    // 刷新页面
    var $flashContainer = $('#flashContainer');
    dwidth = $flashContainer.width();
    dheight = $flashContainer.height();

    loadFlash(isModalDialog == "true");
}

/**
 * 加载SVG
 */
var initOfficeLayout = false;

function _initSVGDesigner() {

    var $monitor = $("#monitor");
    if ($monitor.parent().height() == 0 || (!initOfficeLayout && appName == 'office')) {
        initOfficeLayout = true;

        if (appName == 'office' && scene == "0") {
            var isNewBrowser = false;
            if ($.browser.msie) {
                if (parseInt($.browser.version) >= 11) {
                    isNewBrowser = true;
                } else {
                    var br = navigator.userAgent.toLowerCase();
                    var browserVer = (br.match(/.+(?:rv)[\/: ]([\d.]+)/) || [0, '0'])[1];
                    if (parseInt(browserVer) >= 11) {
                        isNewBrowser = true;
                    }
                }
            } else {
                isNewBrowser = true;
            }

            // IE8 才设置
            if (!isNewBrowser) {

                var htmlHeight = $("html").height();
                var flashNewHeight = htmlHeight - 20;
                $("#flashContainerTD").height(flashNewHeight);
            }
        }


        setTimeout(_initSVGDesigner, 10);
        return;
    }

    var config = {
        target: "#monitor",
        bottomBar: "#bottombar",
        eagleEye: "#eagleEye",
        toolbar: "#toolbar",
        // width : dwidth,
        // height : dheight,
        language: "zh_CN",//国际化配置
        debug: false,//是否为调试模式
        scene: scene,//状态
        context: _transformFlashArgsXml2Context(initialize_flashArgsXml)
    };

    config.context.iconRoot = _ctxPath + "/common/workflow/images/";
    config.context.webRoot = _ctxPath;


    svgDesigner = new WorkflowApp(config);

    var processXML = _getProcessXML();

    var options = {
        processXml: processXML,
        caseLogXml: initialize_caseLogXml,
        caseWorkitemLogXml: initialize_caseWorkitemLogXml,
        verifyResultXml: verifyResultXml
    };

    svgDesigner.loadWorkflow(options);
}

/**
 * 页面重新布局
 * @param showBackNodeTR
 * @returns
 */
function _relayoutPage4StepBackTargetNode(showBackNodeTR) {

    var isNewBrowser = false;
    if ($.browser.msie) {
        if (parseInt($.browser.version) >= 11) {
            isNewBrowser = true;
        } else {
            var br = navigator.userAgent.toLowerCase();
            var browserVer = (br.match(/.+(?:rv)[\/: ]([\d.]+)/) || [0, '0'])[1];
            if (parseInt(browserVer) >= 11) {
                isNewBrowser = true;
            }
        }
    } else {
        isNewBrowser = true;
    }

    // IE8 才设置
    if (!isNewBrowser) {

        var htmlHeight = $("html").height();

        var flashNewHeight = 0;
        if (showBackNodeTR === true) {
            flashNewHeight = htmlHeight - ($("#workflowMessageTD").css("display") == "none" ? 70 : 140);
        } else {
            flashNewHeight = htmlHeight - ($("#workflowMessageTD").css("display") == "none" ? 0 : 60);
        }
        $("#flashContainerTD").height(flashNewHeight);
    }
}


/** 根据场景获取XML数据 **/
function _getProcessXML() {

    var parent_process_xml = null;
    if (returnValueWindow) {
        if (scene != "6") {
            parent_process_xml = $("#process_xml", returnValueWindow.document)[0];
        }
    }
    // iframe加载方式从父页面加载process_xml
    if (openFromIframe && openFromIframe == "true") {
        parent_process_xml = parent.document.getElementById("process_xml");
    }

    var process_xml = initialize_processXml;
    if (parent_process_xml && parent_process_xml.value && parent_process_xml.value != '') {
        process_xml = parent_process_xml.value;
    }
    return process_xml;
}


/**
 * 切换到SVG/flash设计器
 * 测试方法
 */
function switchDesigner(designerName) {

    var thisHref = window.location.href;
    var type = "_designerType=flash";
    var andType = "&" + type;

    if (designerName === "flash") {

        // 使用flash设计器
        if (isSVG) {
            if (thisHref.indexOf(andType) == -1) {
                thisHref += andType;
            }
            window.location.href = thisHref;
        }
    } else {

        // 使用svg设计器
        if (!isSVG) {
            thisHref = thisHref.replace(andType, "");
            window.location.href = thisHref;
        }
    }
}

function designerCallAddNodeItem(params) {
    _transBlankToNull(params);
    var args = [params.nodeType, params.isTemplete, params.appName, params.isAddBranchModel];
    addNodeItem(args);
}

function designerCallMoreOption(params) {
    _transBlankToNull(params);

    var args = [params.linkId, params.conditionId, params.conditionDesc];
    moreOption(args);
}

function designerCallAutoOption(params) {
    _transBlankToNull(params);
    var args = [params.linkId, params.appName, params.conditionId, params.formCondition,
        params.conditionTitle, params.conditionType, params.isForce, params.conditionBase,
        params.conditionDesc, params.isCircle, params.submitStyle];
    autoOption(args);
}

function designerCallRelationData(params) {
    _transBlankToNull(params);
    var args = [params.activityId, params.activityName, params.policyId,
        params.policyName, params.nodeIdAndDRLists, params.DR];
    relationData(args);
}

function designerCallReplaceNode(params) {
    _transBlankToNull(params);
    var args = ["", params.isTemplete, ""];
    replaceNode(args);
}

function designerCallCallNewflowSetting(params) {
    _transBlankToNull(params);
    var args = [params.nodeId];
    callNewflowSetting(args);
}

function designerCallCheckPropertyItem(params) {
    _transBlankToNull(params);

    var args = [params.stateStr, params.nodeName, params.nodePolicy, params.receiveTime, params.completeTime,
        params.overtopTime, params.processMode, params.policyId, params.dealTime, params.remindTime,
        params.partyId, params.partyType, params.matchScope, params.nodeId, params.desc,
        params.formApp, params.formViewOperation, params.dealTermType, params.dealTermUserId, params.dealTermUserName,
        params.hst, params.hstv, params.rup, params.pup, params.na,
        params.tolerantModel, params.queryIds, params.statisticsIds, params.rScope, params.mu,
        params.na_i, params.na_b, params.cycleRemindTime, params.vjoin, params.mergeDealType, params.customName,
        params.isOrderExecute, params.stepBackType, params.finishedCondition, params.executeFinishedValue];

    checkPropertyItem(args);
}

function designerCallSelectPolicy(params) {
    _transBlankToNull(params);

    var args = [params.nodeName, params.policyName, params.dealTerm, params.remindTime, params.processMode,
        params.formApp, params.formViewOperation, params.nodeType, params.isEditor, params.nodeId,
        params.partyId, params.partyType, params.matchScope, params.nodeState, params.hasNewflow,
        params.hasBranch, params.formfiled, params.desc, params.dealTermType, params.dealTermUserId,
        params.dealTermUserName, params.isTemplete, params.appName, params.isProIncludeChild, params.scene,
        params.hst, params.hstv, params.copyFrom, "", params.pasteTo,
        params.rup, params.pup, params.na, params.tolerantModel, params.queryIds,
        params.statisticsIds, params.rScope, params.mu, params.na_i, params.na_b,
        params.cycleRemindTime, params.vjoin, params.mergeDealType, params.customName, params.isOrderExecute,
        params.hasCircleBranch, params.hasAdvanceEvent, params.naUserId, params.naUserName, params.finishedCondition,
        params.executeFinishedValue, params.messageRule, params.sealMethod];

    propertyItem(args);
}

function _transBlankToNull(params) {

    var keys = ["statisticsIds", "nodeId", "queryIds", "formId", "formApp", "form", "formViewOperation"];
    if (params) {
        for (var i = 0; i < keys.length; i++) {
            if (params[keys[i]] == "") {
                params[keys[i]] = null;
            }
        }
    }
}


/**
 * 取消执行操作
 */
function _cancelDesignerCommand() {

    var monitorObj = _getFlashMonitor();
    if (monitorObj.cancelExeCommand) {
        monitorObj.cancelExeCommand();
    }
}
