package com.seeyon.apps.ext.Portal190724.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019/7/24
 */
public class ReadConfigTools {
    private  Properties properties;

    public ReadConfigTools() {
        InputStream inputStream = ReadConfigTools.class.getClassLoader().getResourceAsStream("law/lowPlugin.properties");

        this.properties = new Properties();
        try {
            this.properties.load(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getString(String key) {
        if ((key == null) || (key.equals("")) || (key.equals("null"))) {
            return "";
        }
        String result = "";
        try {
            result = properties.getProperty(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
