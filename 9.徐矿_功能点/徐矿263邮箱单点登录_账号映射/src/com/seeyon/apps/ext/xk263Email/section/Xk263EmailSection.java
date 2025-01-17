package com.seeyon.apps.ext.xk263Email.section;

import com.seeyon.apps.ext.xk263Email.manager.OrgMember263EmailMapperManager;
import com.seeyon.apps.ext.xk263Email.manager.OrgMember263EmailMapperManagerImpl;
import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.apps.ext.xk263Email.util.ZCommonUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.portal.section.BaseSectionImpl;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.HtmlTemplete;
import com.seeyon.ctp.util.Strings;

import java.util.Map;

/**
 * 周刘成   2019/5/28
 */
public class Xk263EmailSection extends BaseSectionImpl {

    private OrgMember263EmailMapperManager mapperManager = new OrgMember263EmailMapperManagerImpl();


    @Override
    public String getId() {
        //栏目ID，必须与spring配置文件中的ID相同;如果是原栏目改造，请尽量保持与原栏目ID一致
        return "xk263EmailSection";
    }

    @Override
    public String getName(Map<String, String> map) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = "我的邮件";
        if (Strings.isBlank(name)) {
            return "我的邮件";
        } else {
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> map) {
        //栏目需要展现总数据条数时重写
        return null;
    }

    @Override
    public String getIcon() {
        // 栏目图标，暂不需要实现
        return null;
    }

    /**
     * 栏目模板列表
     * <p>
     * HtmlTemplete 直接输出HTML代码片断
     * CalendarFourColumnTemplete 日程事件的四列模板：标题 开始时间结束时间 状态
     * ChessboardTemplete 棋盘式 * 左边小图标(默认16*16)+右边标题 *上边大图标(默认32*32)+下边标题 * 标题可以有浮动菜单
     * ChessMultiRowThreeColumnTemplete 棋盘式 、3列
     * MonthCalendarTemplate 月历式栏目
     * MoveMultiRowThreeColumnTemplete 多行3列滚动式
     * MultiIconCategoryItem 多行的，图标，分类，文本的展现形式图表是32px * 32px的，在左边，右边的上面是分类(Category),下面是若干个项
     * MultiRowFourColumnTemplete 多行3列模板，依次是：subject createDate createMemberName category
     * MultiRowThreeColumnTemplete 多行3列模板，依次是：subject createDate category
     * MultiRowVariableColumnTemplete 成倍行,不定列 模板 适用于　三或四列标准列表模板满足不了需要的情况下* 可以自定义列数、宽度、单元格样式、链接地址
     * MultiSubjectSummary 多行的，显示标题和摘要，常用新闻、公告
     * OneImageAndListTemplete 图片加列表模板 * 第一列为图片居左 右边为标题加摘要下面为列表 * 列表内容为 * 标题 发起时间 所属板块 * 列表参数可配置
     * OneItemUseTwoRowTemplete 两行展现一项 模板适用于　如集团空间调查栏目 * 第１行　标题，另起一行　发布时间和类型
     * OnePictureTemplete 图片滚动式
     * OneSummaryAndMultiList 显示模式：一条显示为“标题+时间+(类别)+摘要”，下面是若干行列表
     * PictureTemplete图片基础模板
     * PictureTitleAndBriefTemplete 标题加摘要的新闻模板
     */
    @Override
    public BaseSectionTemplete projection(Map<String, String> map) {
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        OrgMember263EmailMapper member263EmailMapper = null;
        try {
            member263EmailMapper = mapperManager.selectByUserId(userId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String loginUrl = "";
        if (null != member263EmailMapper) {
            loginUrl = ZCommonUtil.get263LoginUrl(member263EmailMapper.getMail263Name());
        }

        String count=ZCommonUtil.getUnreadCount();
        //栏目解析主方法

        HtmlTemplete ht = new HtmlTemplete();
        StringBuilder html = new StringBuilder();

//        String userStr=ZCommonUtil.get263UserList();

        String tmp = "<div style=\"overflow: visible;margin-top:250px;float:right;\">" +
                "<span onclick=\"winOpen=window.open('" + loginUrl + "');\"" +
                " class=\"common_button common_button_icon hand margin_r_5\">    " +
                "<i class=\"font_size14 margin_r_5 vportal vp-markedAsRead\"></i>进入邮箱</span></p></div>";
        tmp += "<div><h5>你有&nbsp;&nbsp;<span style=\"color:red\">"+count+"</span>&nbsp;&nbsp;封未读邮件。</h5></div>";

        html.append(tmp);
        ht.setHeight("230");
        ht.setHtml(html.toString());
        ht.setModel(HtmlTemplete.ModelType.inner);
        ht.setShowBottomButton(true);
        ht.addBottomButton("<button>进入邮箱</button>", "javascript:var rv = v3x.openWindow({url: \"https://www.baidu.com/\",dialogType:'open',workSpace: 'yes'});");
        return ht;
    }
}
