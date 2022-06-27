package com.vince.xq.utils;

import scala.reflect.internal.Trees;

import java.io.IOException;
import java.util.Properties;

public class CommonParaUtil {

    public static Properties paraUtil() {
        try {
            Properties properties = new Properties();
            properties.load(CommonParaUtil.class.getClassLoader().getResourceAsStream("config.properties"));
            //System.out.println("端口号：" + properties.getProperty("redis.port"));
            return properties;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
