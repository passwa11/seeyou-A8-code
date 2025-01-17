package com.seeyon.ctp.common.filemanager.domain;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 替换正文中base64图片字符串结果
 * Created by zjg on 2019-1-26.
 */
public class ReplaceBase64Result extends BasePO{
    private boolean isConvertBase64Img;// 是否将正文中base64字符串替换为了url地址
    private String html;// 替换后的新正文

    public boolean isConvertBase64Img() {
        return isConvertBase64Img;
    }

    public void setConvertBase64Img(boolean convertBase64Img) {
        isConvertBase64Img = convertBase64Img;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
