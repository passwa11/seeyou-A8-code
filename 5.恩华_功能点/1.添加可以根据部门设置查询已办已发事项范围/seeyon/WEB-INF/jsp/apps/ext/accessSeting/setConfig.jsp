<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <%@ include file="../../../common/common.jsp" %>
    <link type="text/css" rel="stylesheet" href="<c:url value="/common/js/xtree/xtree.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/zTree/css/zTreeStyle/zTreeStyle.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/layui.css" />">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/layui/layui.all.js" />"></script>

</head>
<body>
<div class="content_wrap">
    <div style="float: left;width: 40%;height: 90%;background-color: #fdfffd;">
        <div style="width: 95%;height: 100%;margin-left: 10px;">
            <div style="text-align: center" id="btnShow">
                <div style="margin-top: 20px;"></div>
                <div class="form_area">
                    <div class="one_row">
                        <table border="0" cellspacing="0" cellpadding="0">
                            <tbody>
                            <tr>
                                <th nowrap="nowrap">
                                    <label class="margin_r_10" for="dayNum">设置最近天数:</label></th>
                                <td colspan="3">
                                    <div class="common_txtbox_wrap">
                                        <input id="dayNum" type="number"/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th nowrap="nowrap">
                                    <label class="margin_r_10" for="text">选中人员:</label></th>
                                <td width="100%" colspan="3">
                                    <input type="hidden" id="memberId"/>
                                    <div class="common_txtbox  clearfix">
                                        <textarea cols="30" rows="7" id="names" readonly class="w100b "></textarea>
                                    </div>
                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="layui-input-inline" style="margin-top: 20px;">
                    <button class="common_button common_button_emphasize" id="saveRange">保存</button>
                    <button class="common_button common_button_gray" id="reset">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
<script type="text/javascript">
    layui.use(['form', 'table', 'layer', 'layedit', 'laydate'], function () {
        var $ = layui.$
            , form = layui.form
            , table = layui.table
            , layer = layui.layer
            , layedit = layui.layedit
            , laydate = layui.laydate;


        $(function () {
            var arr = parent.layui.table.checkStatus('memberTableId').data;
            var memIds = "";
            var names = ""
            for (let i = 0; i < arr.length; i++) {
                memIds += arr[i]["id"] + ",";
                names += arr[i]["name"] + "；";
            }
            $("#memberId").val(memIds);
            $("#names").text(names);

            $("#saveRange").on('click', function () {
                var obj = {};
                obj['ids'] = $("#memberId").val() + "";
                obj['dayNum'] = $("#dayNum").val() + "";
                $.post("/seeyon/ext/accessSetting.do?method=saveDepartmentViewTimeRange", obj, function (ref) {
                    if (ref.code == 0) {
                        parent.layui.table.reload('memberTableId');
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                        parent.layer.msg('设置成功！', {icon: 6})
                    } else {
                        layer.msg('设置失败!请联系管理员', {icon: 5});
                    }
                });

            });
            $("#reset").on('click', function () {
                var index = parent.layer.getFrameIndex(window.name);
                parent.layer.close(index);
            });
        });
    });


</script>
</html>
