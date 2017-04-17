package com.study.container.util;

import com.alibaba.fastjson.JSON;
import com.study.container.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author cangxing
 * @date 2017-03-09 20:37
 */
public class ConfigFileUtil {
    private static final String LIB = "lib";
    private static final String CONF = "config.json";

    //${classpath}/container/bundles/${bundle}/lib
    public static String middlewareLib(String middlewareRootPath) {
        return middlewareRootPath + File.separator + LIB;
    }

    //${classpath}/container/bundles/${bundle}/config.json
    public static String middlewareConfigPath(String middlewareRootPath) {
        return middlewareRootPath + File.separator + CONF;
    }

    //${classpath}/container/bundles/${bundle}/lib/${exportJar}
    public static String middlewareExportJar(String middlewareRootPath, String exportJar) {
        return middlewareRootPath + File.separator + LIB + File.separator + exportJar;
    }

    public static String readStringFromFile(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            //size为字串的长度,这里一次性读完
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            return new String(buffer, "UTF-8");
        } finally {
            in.close();
        }
    }

    public static Config toJson(String jsonPath) throws IOException {
        return JSON.parseObject(new FileInputStream(jsonPath), Config.class);
    }
}
