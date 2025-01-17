<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script type="text/javascript" src="/seeyon/common/toastr/toastr.min.js"></script>
<link href="/seeyon/common/toastr/toastr.min.css" rel="stylesheet" type="text/css"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>263邮箱账户设置</title>
    <style type="text/css">

        .button {
            background-color: #4CAF50;
            border: none;
            color: white;
            padding: 10px 20px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }

    </style>
</head>

<body>
<center>
    <form id="lawForm" name="lawForm" method="post">

        <div style="width: 500px;margin-top: 50px;" align="center">
            <fieldset>
                <legend style="font-size: 16px;color: #030303">263邮箱账户设置</legend>
                <br>
                <table width="70%" border="0" cellspacing="0" cellpadding="0" height="100px;"
                       align="center">
                    <tr>
                        <td class="bg-gray" width="20%" nowrap="nowrap">用户名:</td>
                        <td class="new-column" width="80%">
                            <input class="input-100per" style="width: 200px;" type="text" name="mail263Name" id="mail263Name"
                                   maxlength="50" value="${requestScope.member.mail263Name}" onfocus="clearInput()"/>
                        </td>
                    </tr>
<%--                    <tr>--%>
<%--                        <td class="bg-gray" width="20%" nowrap="nowrap">密码:</td>--%>
<%--                        <td class="new-column" width="80%">--%>
<%--                            <input class="input-100per" style="height: 28px;width: 200px;" type="password"--%>
<%--                                   name="law_pas" id="law_pas"--%>
<%--                                   maxlength="50" value="${requestScope.userPas.law_pas}"/>--%>
<%--                        </td>--%>
<%--                    </tr>--%>
                    <tr>
                        <td colspan="2" style="text-align: center;color: red">
                            <span id="errInfo" style="font-size: 30px;"></span>
                        </td>
                    </tr>
                </table>
            </fieldset>
            <div id="btnDiv">
                <input type="reset" style="border: none;color: #030303;padding: 10px 20px;text-align: center;
            text-decoration: none;display: inline-block;font-size: 16px;margin: 4px 2px;cursor: pointer;" value="取消"/>
                <input type="button" class="button" onclick="setLaw()" value="确定"/>&nbsp;&nbsp;
            </div>

        </div>
    </form>
</center>
</body>
<script type="text/javascript">
    toastr.options = {
        closeButton: false,  	//是否显示关闭按钮（提示框右上角关闭按钮）。
        debug: false,  			//是否为调试。
        progressBar: false,  	//是否显示进度条（设置关闭的超时时间进度条）
        positionClass: "toast-top-center",  	//消息框在页面显示的位置
        onclick: null,  		//点击消息框自定义事件
        showDuration: "300",  	//显示动作时间
        hideDuration: "1000",  	//隐藏动作时间
        timeOut: "2000",  		//自动关闭超时时间
        extendedTimeOut: "1000",
        showEasing: "swing",
        hideEasing: "linear",
        showMethod: "fadeIn",  	//显示的方式，和jquery相同
        hideMethod: "fadeOut"  	//隐藏的方式，和jquery相同
        //等其他参数
    };
    function clearInput() {
        $("#errInfo").html("");
    }
    function setLaw() {
        var mail263Name = document.getElementById('mail263Name').value;
        if (mail263Name == '') {
            alert('请输入正确的用户名');
            return false;
        }
        $.post("/seeyon/ext/xk263Email.do?method=doSave263", {
            mail263Name: mail263Name
        }, function (data) {
            if (data.code == 0) {
                $("#btnDiv").hide();
                toastr.success('设置成功');
            } else {
                toastr.error('设置失败');
                $("#errInfo").html(data.msg);
            }
        });
//        var url = '/seeyon/ext/setUserController.do?method=setResult';
//        document.lawForm.action = url;
//        document.lawForm.submit();
    }
</script>
</html>
